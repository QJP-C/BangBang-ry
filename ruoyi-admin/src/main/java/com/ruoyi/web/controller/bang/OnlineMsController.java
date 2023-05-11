package com.ruoyi.web.controller.bang;


import cn.hutool.core.collection.ListUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.bang.common.R;
import com.ruoyi.bang.domain.OnlineMs;
import com.ruoyi.bang.dto.MsgDto;
import com.ruoyi.bang.dto.RedisMsgDto;
import com.ruoyi.bang.service.OnlineMsService;
import com.ruoyi.bang.service.UserService;
import com.ruoyi.bang.utils.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

import static com.ruoyi.bang.common.Constants.REDIS_MSG_KEY;

/**
 * @author qjp
 */
@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
@RestController
@CrossOrigin
@Slf4j
@RequestMapping("bang/lt")
@Api(tags = "聊天相关接口",value = "聊天相关接口")
public class OnlineMsController {
    @Autowired
    private UserService userService;
    @Autowired
    private OnlineMsService onlineMsService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private MessageController messageController;
    @Resource
    JwtUtil jwtUtil;

    @Scheduled(fixedRate = 14400000)//  每隔4个小时将Redis缓存的数据转存到Mysql中去
    public void configureTasks() {
        System.out.println("=========开始将Redis中的聊天记录转存到Mysql==========");
        log.info("=========开始将Redis中的聊天记录转存到Mysql==========");
        Set<String> keys = stringRedisTemplate.keys(REDIS_MSG_KEY + "*");  //获取REDIS_MSG_KEY开头的所有key
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                //获取发消息人的id
                String fromId = key.replaceFirst(REDIS_MSG_KEY, "");
                //获取HashKey
                Set<Object> hashKeys = stringRedisTemplate.boundHashOps(key).keys();
                for (Object hashKey : hashKeys) {
                    String toId = hashKey.toString();
                    Object dto = stringRedisTemplate.opsForHash().get(key, toId);
                    RedisMsgDto redisMsgDto = JSON.to(RedisMsgDto.class, dto);
                    String msg = redisMsgDto.getMsg();
                    LocalDateTime sendTime = redisMsgDto.getSendTime();
                    OnlineMs onlineMs = new OnlineMs();
                    onlineMs.setFromId(fromId);
                    onlineMs.setToId(toId);
                    onlineMs.setSendTime(sendTime);
                    onlineMs.setLastContext(msg);
                    onlineMs.setIsRead(0);
                    onlineMsService.save(onlineMs);
                }
                stringRedisTemplate.delete(key);
            }
        }
        System.out.println("==================转存结束===================");
        log.info("==================转存结束===================");
    }

    /**
     * 查询与某人聊天记录
     *
     * @return
     */
    @GetMapping("/jl/{toId}")
    @ApiOperation("查询与某人聊天记录")
    public R<List<OnlineMs>> get(@RequestHeader("Authorization") String header,@PathVariable("toId")String toId) {
        String openid = jwtUtil.getOpenidFromToken(header);
        this.configureTasks();
        LambdaQueryWrapper<OnlineMs> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OnlineMs::getFromId, openid)
                .eq(OnlineMs::getToId, toId)
                .or()
                .eq(OnlineMs::getFromId, toId)
                .eq(OnlineMs::getToId, openid)
                .orderByAsc(OnlineMs::getSendTime);
        List<OnlineMs> list = onlineMsService.list(wrapper);
        for (OnlineMs onlineMs : list) {
            onlineMs.setIsRead(1);
        }
        onlineMsService.updateBatchById(list);
        return R.success(list);
    }

    /**
     * 删除聊天记录
     *
     * @param onlineMs
     * @return
     */
    @DeleteMapping("/delete")
    @ApiOperation("删除聊天记录")
    public R<String> delete(@RequestBody OnlineMs onlineMs) {
        this.configureTasks();
        boolean b = onlineMsService.removeById(onlineMs.getId());
        if (b) {
            return R.success("删除成功");
        } else {
            return R.error("删除失败");
        }
    }

    /**
     * 发送消息给客户端
     */
    @PostMapping("/fa")
    @ApiOperation("发送消息给客户端")
    public R<String> fa(@RequestParam(required = false) String id,
                        @RequestParam(required = false) String[] ids,
                        @RequestParam String msg,
                        @RequestParam String type) {
        log.info("ids:{}", (Object) ids);
        //创建业务消息信息
        JSONObject obj = new JSONObject();
        obj.put("type", type);//业务类型
        obj.put("msgId", id);//消息id
        obj.put("msgTxt", msg);//消息内容
        if (id != null) {
            //单个用户发送 (userId为用户id)
            messageController.sendOneMessage(id, obj.toJSONString());
            return R.success("消息已发送至" + id);
        }
        if (ids != null) {
            //多个用户发送 (userIds为多个用户id，逗号‘,’分隔)
            messageController.sendMoreMessage(ids, obj.toJSONString());
            return R.success("消息已发送至" + Arrays.toString(ids));
        }
        //全体发送
        messageController.sendAllMessage(obj.toJSONString());
        return R.success("消息已群发");
    }

    /**
     * 消息列表
     *
     * @return
     */
    @GetMapping("/lb")
    @ApiOperation("消息列表")
    public R<List> list(@RequestHeader("Authorization") String header) {
        String openid = jwtUtil.getOpenidFromToken(header);
        //同步缓存与数据库中的聊天记录
        this.configureTasks();
        //分组查询
        LambdaQueryWrapper<OnlineMs> wrap = new LambdaQueryWrapper<>();
        wrap.groupBy(OnlineMs::getFromId).groupBy(OnlineMs::getToId).select(OnlineMs::getFromId, OnlineMs::getSendTime, OnlineMs::getIsRead,
                OnlineMs::getLastContext, OnlineMs::getToId, OnlineMs::getId);
        wrap.eq(OnlineMs::getFromId, openid)
                .or()
                .eq(OnlineMs::getToId, openid);
        List<OnlineMs> list = onlineMsService.list(wrap);
        //按时间升序排序
        ListUtil.sortByProperty(list,"sendTime");
        ListUtil.reverse(list);
        //key为用户id,value为消息对象  最终留最后一条消息
        HashMap<String, OnlineMs> map = new HashMap<>();
        for (OnlineMs onlineMs : list) {
            //将对方的id存进去
            if (Objects.equals(onlineMs.getFromId(),openid)){
                map.put(onlineMs.getToId(),onlineMs);
            }else if (Objects.equals(onlineMs.getToId(),openid)){
                map.put(onlineMs.getFromId(),onlineMs);
            }
        }
        Set<String> userIds = map.keySet();
        List<MsgDto> msgList = new ArrayList<>();
        for (String userId : userIds) {
            OnlineMs onlineMs = map.get(userId);
            MsgDto msgDto = new MsgDto();
            msgDto.setLastMsg(onlineMs.getLastContext());
            msgDto.setSendTime(onlineMs.getSendTime());
            Map<String, String> oneInfo = userService.getOneInfo(userId);
            msgDto.setHead(oneInfo.get("head"));
            msgDto.setName(oneInfo.get("username"));
            msgDto.setId(userId);
            msgList.add(msgDto);
        }
        for (MsgDto msgDto : msgList) {
            //未读消息
            LambdaQueryWrapper<OnlineMs> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OnlineMs::getToId, openid)
                    .eq(OnlineMs::getFromId, msgDto.getId())
                    .eq(OnlineMs::getIsRead, 0);
            int count = onlineMsService.count(wrapper);
            msgDto.setUnread(count);
        }
        return R.success(msgList);
    }
}
