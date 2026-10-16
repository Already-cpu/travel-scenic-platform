package com.example.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.travel.common.AuthUtils;
import com.example.travel.common.BusinessException;
import com.example.travel.dto.CommentCreateRequest;
import com.example.travel.dto.CommentReplyRequest;
import com.example.travel.entity.Admin;
import com.example.travel.entity.Comment;
import com.example.travel.entity.CommentImage;
import com.example.travel.entity.CommentReply;
import com.example.travel.entity.Scenic;
import com.example.travel.mapper.AdminMapper;
import com.example.travel.mapper.CommentImageMapper;
import com.example.travel.mapper.CommentMapper;
import com.example.travel.mapper.CommentReplyMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private final CommentMapper commentMapper;
    private final CommentImageMapper imageMapper;
    private final CommentReplyMapper replyMapper;
    private final AdminMapper adminMapper;
    private final ScenicService scenicService;

    public CommentService(CommentMapper commentMapper, CommentImageMapper imageMapper,
                          CommentReplyMapper replyMapper, AdminMapper adminMapper,
                          ScenicService scenicService) {
        this.commentMapper = commentMapper;
        this.imageMapper = imageMapper;
        this.replyMapper = replyMapper;
        this.adminMapper = adminMapper;
        this.scenicService = scenicService;
    }

    @Transactional
    public Comment create(CommentCreateRequest request) {
        Scenic scenic = scenicService.getForOrder(request.getScenicId());
        Comment existed = commentMapper.selectOne(new QueryWrapper<Comment>()
                .eq("user_id", AuthUtils.currentId()).eq("scenic_id", request.getScenicId()));
        if (existed != null) throw new BusinessException("你已经评价过该景点");

        Comment comment = new Comment();
        comment.setUserId(AuthUtils.currentId());
        comment.setScenicId(request.getScenicId());
        comment.setScore(request.getScore());
        comment.setContent(request.getContent().trim());
        comment.setStatus(1);
        commentMapper.insert(comment);
        saveImages(comment.getId(), request.getImages());
        hydrate(comment);
        return comment;
    }

    public List<Comment> scenicComments(Long scenicId) {
        List<Comment> comments = commentMapper.selectList(new QueryWrapper<Comment>().eq("scenic_id", scenicId)
                .eq("status", 1).orderByDesc("create_time"));
        comments.forEach(this::hydrate);
        return comments;
    }

    public List<Comment> mine() {
        List<Comment> comments = commentMapper.selectList(new QueryWrapper<Comment>().eq("user_id", AuthUtils.currentId())
                .orderByDesc("create_time"));
        comments.forEach(this::hydrate);
        return comments;
    }

    public List<Comment> adminList(String keyword, Integer status) {
        QueryWrapper<Comment> query = new QueryWrapper<>();
        if (status != null) query.eq("status", status);
        if (StringUtils.hasText(keyword)) query.and(q -> q.like("content", keyword).or().like("reply", keyword));
        query.orderByDesc("create_time");
        List<Comment> comments = commentMapper.selectList(query);
        comments.forEach(this::hydrate);
        return comments;
    }

    public void delete(Long id) {
        if (commentMapper.deleteById(id) == 0) throw new BusinessException("评价不存在");
    }

    @Transactional
    public CommentReply addReply(CommentReplyRequest request) {
        Comment comment = commentMapper.selectById(request.getId());
        if (comment == null) throw new BusinessException("评价不存在");
        Admin admin = adminMapper.selectById(AuthUtils.currentId());
        CommentReply reply = new CommentReply();
        reply.setCommentId(comment.getId());
        reply.setAdminId(AuthUtils.currentId());
        reply.setAdminName(admin == null ? "景区客服" : admin.getNickname());
        reply.setContent(request.getReply().trim());
        replyMapper.insert(reply);
        comment.setReply(reply.getContent());
        comment.setStatus(1);
        commentMapper.updateById(comment);
        return reply;
    }

    public void updateStatus(Long id, Integer status) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) throw new BusinessException("评价不存在");
        comment.setStatus(status);
        commentMapper.updateById(comment);
    }

    public long count() { return commentMapper.selectCount(null); }

    private void saveImages(Long commentId, List<String> images) {
        if (images == null) return;
        int order = 0;
        for (String url : images) {
            if (!StringUtils.hasText(url) || !url.startsWith("/uploads/")) {
                throw new BusinessException("评价图片地址不合法");
            }
            CommentImage image = new CommentImage();
            image.setCommentId(commentId);
            image.setImageUrl(url.trim());
            image.setSortOrder(order++);
            imageMapper.insert(image);
        }
    }

    private void hydrate(Comment comment) {
        List<CommentImage> imageRows = imageMapper.selectList(new QueryWrapper<CommentImage>()
                .eq("comment_id", comment.getId()).orderByAsc("sort_order").orderByAsc("id"));
        comment.setImages(imageRows.stream().map(CommentImage::getImageUrl).collect(Collectors.toList()));
        List<CommentReply> replies = replyMapper.selectList(new QueryWrapper<CommentReply>()
                .eq("comment_id", comment.getId()).orderByAsc("create_time").orderByAsc("id"));
        if (replies.isEmpty() && StringUtils.hasText(comment.getReply())) {
            CommentReply legacy = new CommentReply();
            legacy.setCommentId(comment.getId());
            legacy.setAdminName("景区客服");
            legacy.setContent(comment.getReply());
            legacy.setCreateTime(comment.getCreateTime());
            replies = new ArrayList<>(Collections.singletonList(legacy));
        }
        comment.setReplies(replies);
    }
}
