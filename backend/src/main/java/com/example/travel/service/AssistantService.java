package com.example.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.travel.entity.Scenic;
import com.example.travel.mapper.ScenicMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AssistantService {
    private static final List<String> TYPES = Arrays.asList("自然风光", "山岳风光", "湖泊山水", "历史古迹", "古镇人文", "海滨休闲", "海岛度假", "亲子研学", "博物馆", "主题乐园");
    private final ScenicMapper scenicMapper;

    public AssistantService(ScenicMapper scenicMapper) {
        this.scenicMapper = scenicMapper;
    }

    public Map<String, Object> chat(String message) {
        String text = message == null ? "" : message.trim();
        QueryWrapper<Scenic> query = new QueryWrapper<Scenic>().eq("status", 1);
        List<Scenic> all = scenicMapper.selectList(new QueryWrapper<Scenic>().eq("status", 1));

        String matchedPlace = all.stream()
                .flatMap(item -> Arrays.asList(item.getProvince(), item.getCity()).stream())
                .filter(place -> place != null && text.contains(place.replace("省", "").replace("市", "")))
                .findFirst().orElse(null);
        if (matchedPlace != null) query.and(q -> q.eq("province", matchedPlace).or().eq("city", matchedPlace));

        String matchedType = TYPES.stream().filter(text::contains).findFirst().orElse(null);
        if (matchedType == null) {
            if (containsAny(text, "亲子", "孩子", "家庭")) matchedType = "亲子研学";
            else if (containsAny(text, "海边", "海滩", "看海")) matchedType = "海滨休闲";
            else if (containsAny(text, "古镇", "人文")) matchedType = "古镇人文";
            else if (containsAny(text, "爬山", "山景")) matchedType = "山岳风光";
        }
        if (matchedType != null) query.eq("type", matchedType);

        BigDecimal budget = extractBudget(text);
        if (budget != null) query.le("price", budget);
        query.orderByDesc("views").last("LIMIT 4");
        List<Scenic> recommendations = scenicMapper.selectList(query);
        if (recommendations.isEmpty()) {
            recommendations = scenicMapper.selectList(new QueryWrapper<Scenic>().eq("status", 1)
                    .orderByDesc("views").last("LIMIT 4"));
        }

        String answer;
        if (containsAny(text, "怎么评价", "评价条件")) {
            answer = "订单已完成，或订单已经支付并到达游览日期后，就可以在景点详情页发表评价，还能上传最多 6 张实景照片。";
        } else if (containsAny(text, "怎么买票", "如何购票", "预订")) {
            answer = "请先登录用户账号，再进入景点详情选择日期、票种和数量。不同票种价格与剩余库存会实时显示，库存不足时不能下单。";
        } else if (containsAny(text, "管理员", "后台")) {
            answer = "管理员后台与用户端完全分开，请从 /admin/login 使用管理员账号登录，用户账号不能进入后台。";
        } else {
            String names = recommendations.stream().map(Scenic::getName).collect(Collectors.joining("、"));
            answer = "结合你的描述，我更推荐 " + names + "。可以点开卡片查看开放时间、不同票种价格、库存和游客图文评价。";
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("answer", answer);
        result.put("recommendations", recommendations);
        result.put("suggestions", Arrays.asList("适合周末的景点", "预算 100 元以内", "亲子游推荐", "如何发表评价"));
        return result;
    }

    private boolean containsAny(String text, String... keywords) {
        return Arrays.stream(keywords).anyMatch(text::contains);
    }

    private BigDecimal extractBudget(String text) {
        Matcher matcher = Pattern.compile("(?:预算|不超过|以内|低于)?\\s*(\\d{1,4})\\s*元").matcher(text);
        return matcher.find() ? new BigDecimal(matcher.group(1)) : null;
    }
}
