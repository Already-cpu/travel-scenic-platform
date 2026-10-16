package com.example.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.travel.common.BusinessException;
import com.example.travel.common.PageResult;
import com.example.travel.dto.ScenicRequest;
import com.example.travel.dto.TicketRequest;
import com.example.travel.entity.Scenic;
import com.example.travel.entity.ScenicTicket;
import com.example.travel.mapper.ScenicMapper;
import com.example.travel.mapper.ScenicTicketMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScenicService {
    private final ScenicMapper scenicMapper;
    private final ScenicTicketMapper ticketMapper;

    public ScenicService(ScenicMapper scenicMapper, ScenicTicketMapper ticketMapper) {
        this.scenicMapper = scenicMapper;
        this.ticketMapper = ticketMapper;
    }

    public PageResult<Scenic> publicPage(long current, long size, String keyword, String type,
                                         String province, String sort) {
        return page(current, size, keyword, type, province, sort, true);
    }

    public PageResult<Scenic> adminPage(long current, long size, String keyword, String type, String province) {
        PageResult<Scenic> result = page(current, size, keyword, type, province, "", false);
        result.getRecords().forEach(this::attachTickets);
        return result;
    }

    private PageResult<Scenic> page(long current, long size, String keyword, String type,
                                    String province, String sort, boolean publicOnly) {
        Page<Scenic> page = new Page<>(current, Math.min(size, 100));
        QueryWrapper<Scenic> query = new QueryWrapper<>();
        if (publicOnly) query.eq("status", 1);
        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper.like("name", keyword).or().like("intro", keyword)
                    .or().like("city", keyword).or().like("province", keyword));
        }
        if (StringUtils.hasText(type)) query.eq("type", type);
        if (StringUtils.hasText(province)) query.eq("province", province);
        if ("priceAsc".equals(sort)) query.orderByAsc("price");
        else if ("priceDesc".equals(sort)) query.orderByDesc("price");
        else if ("views".equals(sort)) query.orderByDesc("views");
        else query.orderByDesc("create_time");
        Page<Scenic> result = scenicMapper.selectPage(page, query);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    public Scenic detail(Long id) {
        Scenic scenic = scenicMapper.selectById(id);
        if (scenic == null || !Integer.valueOf(1).equals(scenic.getStatus())) {
            throw new BusinessException("景点不存在或暂未开放");
        }
        scenicMapper.update(null, new UpdateWrapper<Scenic>().eq("id", id).setSql("views = views + 1"));
        Scenic result = scenicMapper.selectById(id);
        attachTickets(result);
        return result;
    }

    public List<Scenic> hot() {
        return scenicMapper.selectList(new QueryWrapper<Scenic>().eq("status", 1)
                .orderByDesc("views").last("LIMIT 6"));
    }

    public Map<String, Object> filters(boolean publicOnly) {
        QueryWrapper<Scenic> query = new QueryWrapper<Scenic>().select("province", "type");
        if (publicOnly) query.eq("status", 1);
        List<Scenic> rows = scenicMapper.selectList(query);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("provinces", rows.stream().map(Scenic::getProvince).filter(StringUtils::hasText).distinct().sorted().collect(Collectors.toList()));
        result.put("types", rows.stream().map(Scenic::getType).filter(StringUtils::hasText).distinct().sorted().collect(Collectors.toList()));
        result.put("total", rows.size());
        return result;
    }

    public Scenic getForOrder(Long id) {
        Scenic scenic = scenicMapper.selectById(id);
        if (scenic == null || !Integer.valueOf(1).equals(scenic.getStatus())) {
            throw new BusinessException("景点不存在或已下架");
        }
        return scenic;
    }

    public Scenic publicInfo(Long id) {
        Scenic scenic = scenicMapper.selectById(id);
        if (scenic != null && Integer.valueOf(1).equals(scenic.getStatus())) {
            attachTickets(scenic);
            return scenic;
        }
        return null;
    }

    public List<ScenicTicket> ticketsFor(Long scenicId, boolean enabledOnly) {
        QueryWrapper<ScenicTicket> query = new QueryWrapper<ScenicTicket>().eq("scenic_id", scenicId);
        if (enabledOnly) query.eq("status", 1);
        return ticketMapper.selectList(query.orderByAsc("price").orderByAsc("id"));
    }

    public ScenicTicket ticketForOrder(Long scenicId, Long ticketId, String ticketName) {
        QueryWrapper<ScenicTicket> query = new QueryWrapper<ScenicTicket>()
                .eq("scenic_id", scenicId).eq("status", 1);
        if (ticketId != null) query.eq("id", ticketId);
        else if (StringUtils.hasText(ticketName)) query.eq("name", ticketName.trim());
        else query.orderByAsc("price").last("LIMIT 1");
        ScenicTicket ticket = ticketMapper.selectOne(query);
        if (ticket == null) throw new BusinessException("所选票种不存在或已停售");
        return ticket;
    }

    @Transactional
    public Scenic create(ScenicRequest request) {
        Scenic scenic = fromRequest(request);
        scenic.setViews(0L);
        scenic.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        scenicMapper.insert(scenic);
        saveTickets(scenic.getId(), request);
        attachTickets(scenic);
        return scenic;
    }

    @Transactional
    public Scenic update(ScenicRequest request) {
        if (request.getId() == null) throw new BusinessException("景点编号不能为空");
        Scenic scenic = scenicMapper.selectById(request.getId());
        if (scenic == null) throw new BusinessException("景点不存在");
        Scenic next = fromRequest(request);
        next.setId(request.getId());
        next.setViews(scenic.getViews());
        next.setStatus(request.getStatus() == null ? scenic.getStatus() : request.getStatus());
        scenicMapper.updateById(next);
        saveTickets(next.getId(), request);
        Scenic result = scenicMapper.selectById(request.getId());
        attachTickets(result);
        return result;
    }

    public void delete(Long id) {
        if (scenicMapper.deleteById(id) == 0) throw new BusinessException("景点不存在");
    }

    public void updateStatus(Long id, Integer status) {
        Scenic scenic = scenicMapper.selectById(id);
        if (scenic == null) throw new BusinessException("景点不存在");
        scenic.setStatus(status);
        scenicMapper.updateById(scenic);
    }

    private Scenic fromRequest(ScenicRequest request) {
        validateOpenTime(request.getOpenTime());
        Scenic scenic = new Scenic();
        scenic.setName(request.getName().trim());
        scenic.setProvince(request.getProvince().trim());
        scenic.setCity(request.getCity().trim());
        scenic.setAddress(request.getAddress().trim());
        scenic.setType(request.getType().trim());
        BigDecimal lowest = request.getTickets() == null ? request.getPrice() : request.getTickets().stream()
                .filter(ticket -> ticket.getPrice() != null && !Integer.valueOf(0).equals(ticket.getStatus()))
                .map(ticket -> ticket.getPrice()).min(BigDecimal::compareTo).orElse(request.getPrice());
        scenic.setPrice(lowest == null ? BigDecimal.ZERO : lowest);
        scenic.setIntro(request.getIntro().trim());
        scenic.setImage(request.getImage());
        scenic.setOpenTime(request.getOpenTime());
        return scenic;
    }

    private void validateOpenTime(String openTime) {
        if (!StringUtils.hasText(openTime) || "全天开放".equals(openTime.trim())) return;
        String[] parts = openTime.trim().split("-", -1);
        if (parts.length != 2) throw new BusinessException("开放时间格式应为 HH:mm-HH:mm");
        try {
            LocalTime start = LocalTime.parse(parts[0]);
            LocalTime end = LocalTime.parse(parts[1]);
            if (!start.isBefore(end)) throw new BusinessException("开放开始时间必须早于结束时间");
        } catch (java.time.format.DateTimeParseException ex) {
            throw new BusinessException("开放时间格式应为 HH:mm-HH:mm");
        }
    }

    private void attachTickets(Scenic scenic) {
        if (scenic != null) scenic.setTickets(ticketsFor(scenic.getId(), false));
    }

    private void saveTickets(Long scenicId, ScenicRequest request) {
        List<ScenicTicket> existing = ticketMapper.selectList(
                new QueryWrapper<ScenicTicket>().eq("scenic_id", scenicId));
        Map<Long, ScenicTicket> byId = new HashMap<>();
        Map<String, ScenicTicket> byName = new HashMap<>();
        existing.forEach(ticket -> {
            byId.put(ticket.getId(), ticket);
            byName.put(ticket.getName(), ticket);
        });
        Set<Long> retainedIds = new HashSet<>();

        if (request.getTickets() == null || request.getTickets().isEmpty()) {
            ScenicTicket fallback = byName.get("成人票");
            if (fallback == null) fallback = new ScenicTicket();
            fallback.setScenicId(scenicId);
            fallback.setName("成人票");
            fallback.setPrice(request.getPrice() == null ? BigDecimal.ZERO : request.getPrice());
            if (fallback.getStock() == null) fallback.setStock(500);
            fallback.setStatus(1);
            if (fallback.getId() == null) ticketMapper.insert(fallback);
            else ticketMapper.updateById(fallback);
            retainedIds.add(fallback.getId());
            disableRemovedTickets(existing, retainedIds);
            return;
        }

        Set<String> names = new HashSet<>();
        for (TicketRequest item : request.getTickets()) {
            String name = item.getName().trim();
            if (!names.add(name)) throw new BusinessException("票种名称不能重复");
            ScenicTicket ticket = item.getId() == null ? null : byId.get(item.getId());
            if (item.getId() != null && ticket == null) throw new BusinessException("票种不存在或不属于当前景点");
            if (ticket == null) ticket = byName.get(name);
            ScenicTicket sameName = byName.get(name);
            if (sameName != null && ticket != sameName) throw new BusinessException("票种名称已存在");
            if (ticket == null) ticket = new ScenicTicket();
            ticket.setScenicId(scenicId);
            ticket.setName(name);
            ticket.setPrice(item.getPrice());
            ticket.setStock(item.getStock());
            ticket.setStatus(item.getStatus() == null ? 1 : item.getStatus());
            if (ticket.getId() == null) ticketMapper.insert(ticket);
            else ticketMapper.updateById(ticket);
            retainedIds.add(ticket.getId());
        }
        disableRemovedTickets(existing, retainedIds);
    }

    private void disableRemovedTickets(List<ScenicTicket> existing, Set<Long> retainedIds) {
        existing.stream().filter(ticket -> !retainedIds.contains(ticket.getId())).forEach(ticket -> {
            ticket.setStatus(0);
            ticketMapper.updateById(ticket);
        });
    }
}
