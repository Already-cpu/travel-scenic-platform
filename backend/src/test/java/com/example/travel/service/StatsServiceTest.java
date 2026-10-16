package com.example.travel.service;

import com.example.travel.common.BusinessException;
import com.example.travel.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class StatsServiceTest {
    private StatsMapper mapper;
    private StatsService service;

    @BeforeEach void setup() {
        mapper=mock(StatsMapper.class);
        when(mapper.orders(anyString(),any(LocalDateTime.class),any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        service=new StatsService(mock(UserMapper.class),mock(ScenicMapper.class),mock(OrderService.class),mock(CommentService.class),mapper);
    }
    @SuppressWarnings("unchecked")
    private List<Map<String,Object>> rows(Map<String,Object> result) { return (List<Map<String,Object>>) result.get("records"); }

    @Test void defaultMonthRangeContainsTwelveCalendarMonths() {
        List<Map<String,Object>> records=rows(service.orderStatistics("month",null,null));
        assertEquals(12,records.size());
        assertEquals(LocalDate.now().minusMonths(11).format(DateTimeFormatter.ofPattern("yyyy-MM")),records.get(0).get("period"));
        assertEquals(0,records.get(0).get("count"));
        assertEquals(0,records.get(0).get("paidAmount"));
    }
    @Test void includesLeapDayAndZeroFillsGaps() {
        Map<String,Object> row=new LinkedHashMap<>();row.put("period","2024-02-29");row.put("count",2);row.put("amount",80);row.put("paidAmount",30);
        when(mapper.orders(anyString(),any(LocalDateTime.class),any(LocalDateTime.class))).thenReturn(Collections.singletonList(row));
        List<Map<String,Object>> result=rows(service.orderStatistics("day","2024-02-28","2024-03-01"));
        assertEquals(3,result.size());assertEquals(0,result.get(0).get("count"));assertEquals(2,result.get(1).get("count"));assertEquals(30,result.get(1).get("paidAmount"));
        verify(mapper).orders("%Y-%m-%d",LocalDateTime.parse("2024-02-28T00:00:00"),LocalDateTime.parse("2024-03-02T00:00:00"));
    }
    @Test void fillsYearsAndDefaultDays() {
        assertEquals(5,rows(service.orderStatistics("year",null,null)).size());
        assertEquals(30,rows(service.orderStatistics("day",null,null)).size());
    }
    @Test void rejectsUnsupportedGranularityAndInvalidRanges() {
        assertThrows(BusinessException.class,()->service.orderStatistics("week",null,null));
        assertThrows(BusinessException.class,()->service.orderStatistics("day","2024-02-30",null));
        assertThrows(BusinessException.class,()->service.orderStatistics("day","2024-03-02","2024-03-01"));
        assertThrows(BusinessException.class,()->service.orderStatistics("day",null,LocalDate.now().plusDays(1).toString()));
        assertThrows(BusinessException.class,()->service.orderStatistics("day","2020-01-01","2021-02-01"));
        verifyNoInteractions(mapper);
    }
    @Test void userTrendHasTwelveMonthsAndNullSumsBecomeZero() {
        Map<String,Object> summary=new HashMap<>();summary.put("registered",0);summary.put("activeToday",null);
        when(mapper.userSummary()).thenReturn(summary);
        when(mapper.registrations(any(LocalDateTime.class),any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        Map<String,Object> data=service.userStatistics();
        assertEquals(0,data.get("activeToday"));assertEquals(0,data.get("active30Days"));assertEquals(12,((List<?>)data.get("trend")).size());
    }
}
