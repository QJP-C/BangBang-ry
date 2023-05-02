package com.ruoyi.web;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.bang.domain.Post;
import com.ruoyi.bang.domain.Task;
import com.ruoyi.bang.dto.PostNewParamDto;
import com.ruoyi.bang.dto.RedisMsgDto;
import com.ruoyi.bang.dto.TaskListResDto;
import com.ruoyi.bang.service.PostService;
import com.ruoyi.bang.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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
    @Resource
    private PostService postService;
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
    @Test
    void testZjsave(){
        PostNewParamDto postNewParamDto = new PostNewParamDto();
        postNewParamDto.setUrls("sss");
        postNewParamDto.setLocation("青岛");
        postNewParamDto.setText("打一下肖禹树");
        postNewParamDto.setIsVideo(0);
        postNewParamDto.setTopicId("1649038958394376193");
        postService.savePost("oI1vd5DC3H0lVyJizpK58ZPS9Mz8",postNewParamDto,new Post());
    }

    @Test
    void  gpt(){
        Map<String,String> headers = new HashMap<String,String>();
        headers.put("Content-Type","application/json;charset=UTF-8");

        JSONObject json = new JSONObject();
        //选择模型
        json.set("model","text-davinci-003");
        //添加我们需要输入的内容
        json.set("prompt","在中国一个25岁的男生应该有多少存款？");
        json.set("temperature",0.9);
        json.set("max_tokens",2048);
        json.set("top_p",1);
        json.set("frequency_penalty",0.0);
        json.set("presence_penalty",0.6);

        HttpResponse response = HttpRequest.post("https://api.openai.com/v1/completions")
                .headerMap(headers, false)
                .bearerAuth("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")
                .body(String.valueOf(json))
                .timeout(5 * 60 * 1000)
                .execute();

        System.out.println(response.body());
    }
}
