package com.ruoyi.bang.dto;

import lombok.Data;

import java.util.List;

@Data
public class FollowListResDto {
    private List<?> list;
    private Long minTime;
    private Integer offset;
    private Integer size;
    private Integer total;
}
