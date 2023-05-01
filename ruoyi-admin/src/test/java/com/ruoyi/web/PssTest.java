package com.ruoyi.web;


import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.bang.domain.Task;
import com.ruoyi.bang.dto.RedisMsgDto;
import com.ruoyi.bang.dto.TaskListResDto;
import com.ruoyi.bang.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static com.ruoyi.bang.common.Constants.REDIS_MSG_KEY;

//@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PssTest {
    @Resource
    private TaskService taskService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedisTemplate redisTemplate;

    @Test
    public void testMapper() {
        String s = "1651029218248720386";
        Task byId = taskService.getById(s);
        System.out.println(byId);

    }

    @Test
    public void testSelectDrug() {
        int page = 1,pageSize = 2;
        LambdaQueryWrapper<Task> qw = new LambdaQueryWrapper<>();

//        qw
//                .eq(!StringUtil.isNullOrEmpty(typeId), Task::getTypeId, typeId).like(!StringUtil.isNullOrEmpty(search), Task::getLocation, search)
//                .or()
//                .eq(!StringUtil.isNullOrEmpty(typeId), Task::getTypeId, typeId).like(!StringUtil.isNullOrEmpty(search), Task::getDetails, search)
//                .or()
//                .eq(!StringUtil.isNullOrEmpty(typeId), Task::getTypeId, typeId).like(!StringUtil.isNullOrEmpty(search), Task::getTitle, search);
        qw.eq(Task::getState, 1);
        qw.orderByDesc(Task::getReleaseTime);
        Page<Task> pageInfo = new Page<>(page, pageSize);
        Page<TaskListResDto> dtoPage = new Page<>(page, pageSize);
        pageInfo.setOptimizeCountSql(false);
//        IPage<LendList> lendListIPage = lendRecordMapper.getLendList(page, wrapper);
        taskService.page(pageInfo, qw);
        System.out.println(pageInfo.getRecords());
    }

    @Test
    public void  websorket(){//RedisMsgDto.class

        Object o = stringRedisTemplate.opsForHash().get(REDIS_MSG_KEY+"ss", "11");
        RedisMsgDto bean = JSON.to(RedisMsgDto.class,o);

        System.out.println(bean);
    }
    @Test
    public void dmsom(){
        RedisMsgDto redisMsgDto = new RedisMsgDto();
        redisMsgDto.setMsg("message");
        redisMsgDto.setSendTime(LocalDateTime.now());
        stringRedisTemplate.opsForHash().put(REDIS_MSG_KEY+"ss", "11", JSON.toJSONString(redisMsgDto));
    }
}
