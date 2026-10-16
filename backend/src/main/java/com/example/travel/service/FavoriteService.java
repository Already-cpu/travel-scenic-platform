package com.example.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.travel.common.AuthUtils;
import com.example.travel.common.BusinessException;
import com.example.travel.entity.Favorite;
import com.example.travel.entity.Scenic;
import com.example.travel.mapper.FavoriteMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteService {
    private final FavoriteMapper favoriteMapper;
    private final ScenicService scenicService;

    public FavoriteService(FavoriteMapper favoriteMapper, ScenicService scenicService) {
        this.favoriteMapper = favoriteMapper;
        this.scenicService = scenicService;
    }

    public void add(Long scenicId) {
        scenicService.getForOrder(scenicId);
        if (find(scenicId) != null) {
            throw new BusinessException("该景点已经收藏");
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(AuthUtils.currentId());
        favorite.setScenicId(scenicId);
        favoriteMapper.insert(favorite);
    }

    public void remove(Long scenicId) {
        favoriteMapper.delete(new QueryWrapper<Favorite>().eq("user_id", AuthUtils.currentId())
                .eq("scenic_id", scenicId));
    }

    public List<Scenic> mine() {
        return favoriteMapper.selectList(new QueryWrapper<Favorite>().eq("user_id", AuthUtils.currentId())
                        .orderByDesc("create_time")).stream()
                .map(favorite -> scenicService.publicInfo(favorite.getScenicId()))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    public boolean check(Long scenicId) {
        return find(scenicId) != null;
    }

    private Favorite find(Long scenicId) {
        return favoriteMapper.selectOne(new QueryWrapper<Favorite>().eq("user_id", AuthUtils.currentId())
                .eq("scenic_id", scenicId));
    }
}
