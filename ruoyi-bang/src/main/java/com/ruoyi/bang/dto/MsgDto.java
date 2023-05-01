package com.ruoyi.bang.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author qjp
 */
@Data
@ApiModel("消息列表参数")
public class MsgDto {
    @ApiModelProperty("对方用户id")
    private String id;
    @ApiModelProperty("对方用户头像")
    private String head;
    @ApiModelProperty("对方用户的名字")
    private String name;
    @ApiModelProperty("最后一条消息")
    private String lastMsg;
    @ApiModelProperty("最后一条消息时间")
    private LocalDateTime sendTime;
    @ApiModelProperty("未读消息条数")
    private int unread;
//    private boolean isLogin;

}
