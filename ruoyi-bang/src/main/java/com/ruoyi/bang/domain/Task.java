package com.ruoyi.bang.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * (Task)表实体类
 *
 * @author makejava
 * @since 2023-04-17 11:34:28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@ApiModel("任务实体类")
public class Task extends BaseEntity {
    //任务id
    private String id;
    //发布人id
    @Excel(name = "发布人id")
    private String fromId;
    //接单人id
    @Excel(name = "接单人id")
    private String toId;
    //任务标题
    @Excel(name = "任务标题")
    private String title;
    //任务详情
    @Excel(name = "任务详情")
    private String details;
    //是否加急  1:加急  0:不急
    @Excel(name = "是否加急  1:加急  0:不急")
    private Integer urgent;
    //任务状态   1:已发布  2:已接取  3:已完成   0:已逾期
    @Excel(name = "任务状态  0:待审核  1:已发布  2:已接取  3:已完成   4:已逾期")
    private Integer state;
    //类型
    @Excel(name = "类型id")
    private String typeId;
    //赏金
    @Excel(name = "赏金")
    private Double money;
    //地址
    @Excel(name = "地址")
    private String location;
    //发布时间
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "发布时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date releaseTime;
    //截止时间
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "截止时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date limitTime;

    @TableField(exist=false)
    private String[] fromUrls;

}

