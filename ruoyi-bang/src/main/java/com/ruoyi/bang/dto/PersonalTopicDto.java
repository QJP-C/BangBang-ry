package com.ruoyi.bang.dto;

import com.ruoyi.bang.domain.Topic;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class PersonalTopicDto {
    private List<Topic> res;
    @ApiModelProperty("关注数")
    private Integer followNum;
}
