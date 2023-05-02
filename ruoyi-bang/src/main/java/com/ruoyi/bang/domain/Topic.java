package com.ruoyi.bang.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * (Topic)表实体类
 *
 * @author makejava
 * @since 2023-04-20 20:18:21
 */
@Data
@ApiModel("话题")
public class Topic {

    private String id;
    //话题名称
    @ApiModelProperty("话题名称")
    private String name;
    //话题标签
    @ApiModelProperty("话题标签")
    private String tags;
    //话题头像
    @ApiModelProperty("话题头像")
    private String head;
    //话题背景
    @ApiModelProperty("话题背景")
    private String bc;
    //相关帖子条数
    @ApiModelProperty("相关帖子条数")
    @TableField(exist=false)
    private Integer num;
    @ApiModelProperty("是否加入")
    @TableField(exist=false)
    private boolean isJoin;
}

