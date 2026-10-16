package com.example.travel.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

public class CommentCreateRequest {
    @NotNull(message = "景点编号不能为空")
    private Long scenicId;
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为 1 分")
    @Max(value = 5, message = "评分最高为 5 分")
    private Integer score;
    @NotBlank(message = "评价内容不能为空")
    @javax.validation.constraints.Size(max = 1000, message = "评价最多 1000 字")
    private String content;
    @javax.validation.constraints.Size(max = 6, message = "评价图片最多上传 6 张")
    private List<String> images;

    public Long getScenicId() { return scenicId; }
    public void setScenicId(Long scenicId) { this.scenicId = scenicId; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}
