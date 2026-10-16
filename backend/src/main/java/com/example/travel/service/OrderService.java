package com.example.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.travel.common.AuthUtils;
import com.example.travel.common.BusinessException;
import com.example.travel.common.PageResult;
import com.example.travel.dto.OrderCreateRequest;
import com.example.travel.dto.OrderStatusRequest;
import com.example.travel.entity.Scenic;
import com.example.travel.entity.ScenicTicket;
import com.example.travel.entity.TravelOrder;
import com.example.travel.entity.User;
import com.example.travel.mapper.ScenicTicketMapper;
import com.example.travel.mapper.TravelOrderMapper;
import com.example.travel.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
public class OrderService {
    private static final List<String> STATUSES = Arrays.asList("PENDING", "PAID", "CANCELLED", "COMPLETED");
    private final TravelOrderMapper orderMapper;
    private final ScenicTicketMapper ticketMapper;
    private final ScenicService scenicService;
    private final UserMapper userMapper;

    public OrderService(TravelOrderMapper orderMapper, ScenicTicketMapper ticketMapper, ScenicService scenicService,
                        UserMapper userMapper) {
        this.orderMapper = orderMapper;
        this.ticketMapper = ticketMapper;
        this.scenicService = scenicService;
        this.userMapper = userMapper;
    }

    @Transactional
    public TravelOrder create(OrderCreateRequest request) {
        if (request.getVisitDate().isBefore(LocalDate.now())) {
            throw new BusinessException("游览日期不能早于今天");
        }
        Scenic scenic = scenicService.getForOrder(request.getScenicId());
        ScenicTicket ticket = scenicService.ticketForOrder(scenic.getId(), request.getTicketId(), request.getTicketType());
        reserveStock(ticket.getId(), request.getQuantity());

        TravelOrder order = new TravelOrder();
        order.setOrderNo("TR" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000)));
        order.setUserId(AuthUtils.currentId());
        order.setScenicId(scenic.getId());
        order.setScenicName(scenic.getName());
        order.setVisitDate(request.getVisitDate());
        order.setTicketId(ticket.getId());
        order.setTicketType(ticket.getName());
        order.setUnitPrice(ticket.getPrice());
        order.setQuantity(request.getQuantity());
        order.setTotalPrice(ticket.getPrice().multiply(java.math.BigDecimal.valueOf(request.getQuantity())));
        order.setContactName(request.getContactName().trim());
        order.setContactPhone(request.getContactPhone().trim());
        order.setStatus("PENDING");
        orderMapper.insert(order);
        return order;
    }

    public List<TravelOrder> mine() {
        return orderMapper.selectList(new QueryWrapper<TravelOrder>()
                .eq("user_id", AuthUtils.currentId()).orderByDesc("create_time"));
    }

    @Transactional
    public void cancel(Long id) {
        TravelOrder order = ownerOrder(id);
        if (!"PENDING".equals(order.getStatus())) throw new BusinessException("只有待支付订单可以取消");
        order.setStatus("CANCELLED");
        orderMapper.updateById(order);
        restoreStock(order);
    }

    @Transactional
    public BigDecimal pay(Long id, String method) {
        if (!"wallet".equalsIgnoreCase(method) && !"scan".equalsIgnoreCase(method)) {
            throw new BusinessException("不支持的支付方式");
        }
        TravelOrder order = ownerOrder(id);
        if (!"PENDING".equals(order.getStatus())) throw new BusinessException("当前订单无法支付");
        Long userId = AuthUtils.currentId();
        if ("wallet".equalsIgnoreCase(method)) {
            int deducted = userMapper.update(null, new UpdateWrapper<User>()
                    .eq("id", userId)
                    .ge("wallet_balance", order.getTotalPrice())
                    .setSql("wallet_balance = wallet_balance - " + order.getTotalPrice().toPlainString()));
            if (deducted == 0) throw new BusinessException("钱包余额不足，请选择扫码支付");
        }
        int paid = orderMapper.update(null, new UpdateWrapper<TravelOrder>()
                .eq("id", order.getId()).eq("status", "PENDING")
                .set("payment_method", method.toLowerCase()).set("status", "PAID"));
        if (paid == 0) throw new BusinessException("当前订单无法支付");
        User user = userMapper.selectById(userId);
        return user == null || user.getWalletBalance() == null ? BigDecimal.ZERO : user.getWalletBalance();
    }

    public PageResult<TravelOrder> adminPage(long current, long size, String status, String keyword,
                                             String startDate, String endDate) {
        Page<TravelOrder> page = new Page<>(current, Math.min(size, 100));
        QueryWrapper<TravelOrder> query = new QueryWrapper<>();
        if (StringUtils.hasText(status)) query.eq("status", status);
        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper.like("order_no", keyword).or().like("scenic_name", keyword)
                    .or().like("user_id", keyword).or().like("contact_name", keyword).or().like("contact_phone", keyword));
        }
        if (StringUtils.hasText(startDate)) query.ge("create_time", startDate + " 00:00:00");
        if (StringUtils.hasText(endDate)) query.le("create_time", endDate + " 23:59:59");
        query.orderByDesc("create_time");
        Page<TravelOrder> result = orderMapper.selectPage(page, query);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Transactional
    public void updateStatus(OrderStatusRequest request) {
        if (!STATUSES.contains(request.getStatus())) throw new BusinessException("无效的订单状态");
        TravelOrder order = orderMapper.selectById(request.getId());
        if (order == null) throw new BusinessException("订单不存在");
        String before = order.getStatus();
        String after = request.getStatus();
        if ("CANCELLED".equals(before) && !"CANCELLED".equals(after) && order.getTicketId() != null) {
            reserveStock(order.getTicketId(), order.getQuantity());
        }
        order.setStatus(after);
        orderMapper.updateById(order);
        if (!"CANCELLED".equals(before) && "CANCELLED".equals(after)) restoreStock(order);
    }

    public long count() { return orderMapper.selectCount(null); }

    public List<TravelOrder> recent(int limit) {
        return orderMapper.selectList(new QueryWrapper<TravelOrder>().orderByDesc("create_time").last("LIMIT " + limit));
    }

    public List<TravelOrder> byScenic(Long scenicId) {
        return orderMapper.selectList(new QueryWrapper<TravelOrder>().eq("scenic_id", scenicId));
    }

    private void reserveStock(Long ticketId, Integer quantity) {
        int updated = ticketMapper.update(null, new UpdateWrapper<ScenicTicket>()
                .eq("id", ticketId).eq("status", 1).ge("stock", quantity)
                .setSql("stock = stock - " + quantity));
        if (updated == 0) throw new BusinessException("当前票种库存不足，请减少数量或选择其他票种");
    }

    private void restoreStock(TravelOrder order) {
        if (order.getTicketId() != null && order.getQuantity() != null) {
            ticketMapper.update(null, new UpdateWrapper<ScenicTicket>().eq("id", order.getTicketId())
                    .setSql("stock = stock + " + order.getQuantity()));
        }
    }

    private TravelOrder ownerOrder(Long id) {
        TravelOrder order = orderMapper.selectOne(new QueryWrapper<TravelOrder>()
                .eq("id", id).eq("user_id", AuthUtils.currentId()));
        if (order == null) throw new BusinessException("订单不存在");
        return order;
    }
}
