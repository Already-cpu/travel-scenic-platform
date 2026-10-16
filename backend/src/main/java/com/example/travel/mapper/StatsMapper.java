package com.example.travel.mapper;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.*;

@Mapper
public interface StatsMapper {
    @Select("SELECT DATE_FORMAT(create_time, #{format}) AS period, COUNT(*) AS count, COALESCE(SUM(total_price),0) AS amount, " +
            "COALESCE(SUM(CASE WHEN status IN ('PAID','COMPLETED') THEN total_price ELSE 0 END),0) AS paidAmount " +
            "FROM `order` WHERE create_time >= #{start} AND create_time < #{end} GROUP BY DATE_FORMAT(create_time, #{format}) ORDER BY period")
    List<Map<String,Object>> orders(@Param("format") String format,@Param("start") LocalDateTime start,@Param("end") LocalDateTime end);

    @Select("SELECT province AS name,COUNT(*) AS value FROM scenic GROUP BY province ORDER BY value DESC,name")
    List<Map<String,Object>> regionDistribution();

    @Select("SELECT s.id,s.name,s.views," +
            "(SELECT COUNT(*) FROM `order` o WHERE o.scenic_id=s.id AND o.status IN ('PAID','COMPLETED')) AS reservations," +
            "(SELECT COUNT(*) FROM favorite f WHERE f.scenic_id=s.id) AS favorites FROM scenic s ORDER BY s.views DESC,s.id")
    List<Map<String,Object>> hotness();

    @Select("SELECT s.id AS scenicId,s.name,COUNT(c.id) AS total,COALESCE(ROUND(AVG(c.score),2),0) AS average," +
            "SUM(CASE WHEN c.score=1 THEN 1 ELSE 0 END) AS score1,SUM(CASE WHEN c.score=2 THEN 1 ELSE 0 END) AS score2," +
            "SUM(CASE WHEN c.score=3 THEN 1 ELSE 0 END) AS score3,SUM(CASE WHEN c.score=4 THEN 1 ELSE 0 END) AS score4," +
            "SUM(CASE WHEN c.score=5 THEN 1 ELSE 0 END) AS score5 FROM scenic s LEFT JOIN `comment` c ON c.scenic_id=s.id AND c.status=1 " +
            "GROUP BY s.id,s.name ORDER BY total DESC,s.id")
    List<Map<String,Object>> ratingByScenic();

    @Select("SELECT COUNT(*) AS registered,SUM(CASE WHEN create_time>=CURDATE() THEN 1 ELSE 0 END) AS newToday," +
            "SUM(CASE WHEN status=1 AND last_login_time>=CURDATE() THEN 1 ELSE 0 END) AS activeToday," +
            "SUM(CASE WHEN status=1 AND last_login_time>=DATE_SUB(NOW(),INTERVAL 30 DAY) THEN 1 ELSE 0 END) AS active30Days FROM `user`")
    Map<String,Object> userSummary();

    @Select("SELECT DATE_FORMAT(create_time,'%Y-%m') AS month,COUNT(*) AS count FROM `user` WHERE create_time>=#{start} AND create_time<#{end} " +
            "GROUP BY DATE_FORMAT(create_time,'%Y-%m') ORDER BY month")
    List<Map<String,Object>> registrations(@Param("start") LocalDateTime start,@Param("end") LocalDateTime end);
}
