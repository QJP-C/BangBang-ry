package com.ruoyi.bang.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("个人主页评论列表参数")
public class PersonalCommentDto {
    @ApiModelProperty("用户id")
    private String userId;
    @ApiModelProperty("发评论用户头像")
    private String head;
    @ApiModelProperty("发评论用户昵称")
    private String username;
    @ApiModelProperty("评论id")
    private String commentId;
    @ApiModelProperty("评论内容")
    private String commentText;
    @ApiModelProperty("评论时间")
    private LocalDateTime commentTime;
    @ApiModelProperty("发贴用户昵称")
    private String toUsername;
    @ApiModelProperty("帖子id")
    private String postId;
    @ApiModelProperty("帖子文本")
    private String postText;

}
