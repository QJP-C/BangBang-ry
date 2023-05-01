package com.ruoyi.bang.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.bang.domain.OnlineMs;

/**
 * @author qjp
 */
public interface OnlineMsService extends IService<OnlineMs> {
    void message(String from,String to,String message);
    String offline(String from,String to,String message);
    void fa(String id,String msg);
}
