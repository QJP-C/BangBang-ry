package com.ruoyi.bang.service.impl;


import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.bang.domain.OnlineMs;
import com.ruoyi.bang.domain.User;
import com.ruoyi.bang.dto.RedisMsgDto;
import com.ruoyi.bang.exception.BangException;
import com.ruoyi.bang.mapper.OnlineMsMapper;
import com.ruoyi.bang.service.OnlineMsService;
import com.ruoyi.bang.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static com.ruoyi.bang.common.Constants.REDIS_MSG_KEY;

/**
 * @author qjp
 */
@Slf4j
@Service
public class OnlineMsServiceImpl extends ServiceImpl<OnlineMsMapper, OnlineMs> implements OnlineMsService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserService userService;

    @Override
    public void message(String from, String to, String message) {
        RedisMsgDto redisMsgDto = new RedisMsgDto();
        redisMsgDto.setMsg(message);
        redisMsgDto.setSendTime(LocalDateTime.now());
        stringRedisTemplate.opsForHash().put(REDIS_MSG_KEY+from,to, JSON.toJSONString(redisMsgDto));
}

    @Override
    public String offline(String from, String to, String message) {
        User one1 = userService.getById(from);
        if (null!=one1) {
            OnlineMs onlineMs = new OnlineMs();
            onlineMs.setToId(to);
            onlineMs.setLastContext(message);
            onlineMs.setFromId(from);
            onlineMs.setSendTime(LocalDateTime.now());
            onlineMs.setIsRead(0);
            boolean save = this.save(onlineMs);
            if (!save){
                BangException.cast("消息存储失败！");
            }
            log.info("离线消息已存储");
            return "离线消息已存储";
        }
        return "该用户不存在！";
    }
    @Override
    public void fa(String id,String msg){

    }

}
