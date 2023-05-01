package com.ruoyi.bang.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * (Post)表实体类
 *
 * @author makejava
 * @since 2023-04-21 20:44:28
 */
@Data
@ApiModel("帖子")
public class Post extends BaseEntity {
    
    private String id;
    //发贴用户id
    @ApiModelProperty("发贴用户id")
    @Excel(name = "发贴用户id")
    private String userId;
    //文本
    @ApiModelProperty("文本")
    @Excel(name = "发贴用户id")
    private String text;
    //话题id
    @ApiModelProperty("话题id")
    @Excel(name = "发贴用户id")
    private String topicId;
    //发布时间
    @ApiModelProperty("发布时间")
    @Excel(name = "发贴用户id")
    private LocalDateTime releaseTime;
    //位置
    @ApiModelProperty("位置")
    @Excel(name = "发贴用户id")
    private String location;
    //是否有视频  1有视频 0无视频
    @ApiModelProperty("是否有视频  1有视频 0无视频")
    @Excel(name = "发贴用户id")
    private int isVideo;
    @TableField(exist=false)
    private String[] fromUrls;
}

