package com.example.travel.service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.travel.common.BusinessException;
import com.example.travel.entity.Scenic;
import com.example.travel.mapper.*;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class StatsService {
    private final UserMapper userMapper;
    private final ScenicMapper scenicMapper;
    private final OrderService orderService;
    private final CommentService commentService;
    private final StatsMapper statsMapper;
    public StatsService(UserMapper userMapper,ScenicMapper scenicMapper,OrderService orderService,CommentService commentService,StatsMapper statsMapper) {
        this.userMapper=userMapper;this.scenicMapper=scenicMapper;this.orderService=orderService;this.commentService=commentService;this.statsMapper=statsMapper;
    }
    public Map<String,Object> overview() {
        Map<String,Object> data=new LinkedHashMap<>();
        data.put("userCount",userMapper.selectCount(null)); data.put("scenicCount",scenicMapper.selectCount(null));
        data.put("orderCount",orderService.count()); data.put("commentCount",commentService.count());
        // Extended analytics share the overview response; initial dashboard uses five parallel requests.
        data.put("hotness",hotness()); data.put("ratings",statsMapper.ratingByScenic());
        data.put("users",userStatistics()); data.put("orders",orderStatistics("month",null,null)); return data;
    }
    public List<Scenic> scenicViews() {
        return scenicMapper.selectList(new QueryWrapper<Scenic>().orderByDesc("views").orderByAsc("id").last("LIMIT 10"));
    }
    @SuppressWarnings("unchecked")
    public List<Map<String,Object>> orderTrend() {
        List<Map<String,Object>> rows=(List<Map<String,Object>>)orderStatistics("month",null,null).get("records");
        for(Map<String,Object> row:rows) row.put("month",row.get("period"));
        return rows;
    }
    public Map<String,Object> orderStatistics(String granularity,String startDate,String endDate) {
        if(!Arrays.asList("day","month","year").contains(granularity)) throw new BusinessException("统计粒度只支持 day、month、year");
        LocalDate today=LocalDate.now(),start,end;
        try {
            start=startDate==null||startDate.isEmpty()?("day".equals(granularity)?today.minusDays(29):"year".equals(granularity)?today.withDayOfYear(1).minusYears(4):today.withDayOfMonth(1).minusMonths(11)):LocalDate.parse(startDate);
            end=endDate==null||endDate.isEmpty()?today:LocalDate.parse(endDate);
        } catch(Exception e) { throw new BusinessException("日期格式应为 yyyy-MM-dd"); }
        if(start.isAfter(end)||end.isAfter(today)) throw new BusinessException("请选择有效的历史日期范围");
        long days=ChronoUnit.DAYS.between(start,end);
        if(days>3660||("day".equals(granularity)&&days>366)) throw new BusinessException("日统计最多 367 天，月/年统计最多 10 年");
        String pattern="day".equals(granularity)?"yyyy-MM-dd":"year".equals(granularity)?"yyyy":"yyyy-MM";
        String sqlPattern="day".equals(granularity)?"%Y-%m-%d":"year".equals(granularity)?"%Y":"%Y-%m";
        Map<String,Map<String,Object>> index=new HashMap<>();
        for(Map<String,Object> row:statsMapper.orders(sqlPattern,start.atStartOfDay(),end.plusDays(1).atStartOfDay())) index.put(row.get("period").toString(),row);
        List<Map<String,Object>> records=new ArrayList<>();
        LocalDate cursor="day".equals(granularity)?start:"year".equals(granularity)?start.withDayOfYear(1):start.withDayOfMonth(1);
        while(!cursor.isAfter(end)) {
            String period=cursor.format(DateTimeFormatter.ofPattern(pattern));
            Map<String,Object> row=index.get(period);
            if(row==null) { row=new LinkedHashMap<>();row.put("period",period);row.put("count",0);row.put("amount",0);row.put("paidAmount",0); }
            records.add(row);
            cursor="day".equals(granularity)?cursor.plusDays(1):"year".equals(granularity)?cursor.plusYears(1):cursor.plusMonths(1);
        }
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("granularity",granularity);result.put("startDate",start.toString());result.put("endDate",end.toString());result.put("records",records);return result;
    }
    public Map<String,Object> userStatistics() {
        Map<String,Object> result=new LinkedHashMap<>(statsMapper.userSummary());
        for(String key:Arrays.asList("registered","newToday","activeToday","active30Days")) if(result.get(key)==null) result.put(key,0);
        LocalDate first=LocalDate.now().withDayOfMonth(1).minusMonths(11);
        Map<String,Object> counts=new HashMap<>();
        for(Map<String,Object> row:statsMapper.registrations(first.atStartOfDay(),LocalDate.now().plusDays(1).atStartOfDay())) counts.put(row.get("month").toString(),row.get("count"));
        List<Map<String,Object>> trend=new ArrayList<>();
        for(int i=0;i<12;i++) { String month=first.plusMonths(i).format(DateTimeFormatter.ofPattern("yyyy-MM"));Map<String,Object> row=new LinkedHashMap<>();row.put("month",month);row.put("count",counts.getOrDefault(month,0));trend.add(row); }
        result.put("trend",trend);return result;
    }
    public List<Map<String,Object>> regionDistribution(){return statsMapper.regionDistribution();}
    public List<Map<String,Object>> hotness(){return statsMapper.hotness();}
    public List<Map<String,Object>> ratingDistribution(){
        List<Map<String,Object>> rows=statsMapper.ratingByScenic(), result=new ArrayList<>();
        for(int score=1;score<=5;score++){long total=0;for(Map<String,Object> row:rows)total+=((Number)row.get("score"+score)).longValue();Map<String,Object> item=new LinkedHashMap<>();item.put("name",score);item.put("value",total);result.add(item);}
        return result;
    }
}
