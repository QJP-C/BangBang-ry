package com.ruoyi.web;


import cn.hutool.core.collection.ListUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.ruoyi.bang.domain.OnlineMs;
import com.ruoyi.bang.domain.Post;
import com.ruoyi.bang.domain.Task;
import com.ruoyi.bang.domain.User;
import com.ruoyi.bang.dto.MsgDto;
import com.ruoyi.bang.dto.PostNewParamDto;
import com.ruoyi.bang.dto.RedisMsgDto;
import com.ruoyi.bang.dto.TaskListResDto;
import com.ruoyi.bang.mapper.TaskMapper;
import com.ruoyi.bang.service.OnlineMsService;
import com.ruoyi.bang.service.PostService;
import com.ruoyi.bang.service.TaskService;
import com.ruoyi.bang.service.UserService;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static com.ruoyi.bang.common.Constants.REDIS_MSG_KEY;

//@RunWith(SpringRunner.class)
@Slf4j
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
    @Resource
    private OnlineMsService onlineMsService;
    @Resource
    private UserService userService;
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
    void testXXX(){
        // 按消息与每个人的最后一条查
        LambdaQueryWrapper<OnlineMs> wrap = new LambdaQueryWrapper<>();
        String openid = "oI1vd5BUGnDzqKfkJbprGklQnIDk";
        wrap.groupBy(OnlineMs::getFromId).groupBy(OnlineMs::getToId).select(OnlineMs::getFromId, OnlineMs::getSendTime, OnlineMs::getIsRead,
                OnlineMs::getLastContext, OnlineMs::getToId, OnlineMs::getId);
        wrap.eq(OnlineMs::getFromId, openid)
                .or()
                .eq(OnlineMs::getToId, openid);
        List<OnlineMs> list = onlineMsService.list(wrap);
        ListUtil.sortByProperty(list,"sendTime");
        ListUtil.reverse(list);
        list.forEach(System.out::println);
        HashMap<String, OnlineMs> map = new HashMap<>();
        for (OnlineMs onlineMs : list) {
            if (Objects.equals(onlineMs.getFromId(),openid)){
                map.put(onlineMs.getToId(),onlineMs);
            }else if (Objects.equals(onlineMs.getToId(),openid)){
                map.put(onlineMs.getFromId(),onlineMs);
            }
        }
        Set<String> userIds = map.keySet();
        userIds.forEach(key->{
            System.out.println(key);
            System.out.println(map.get(key));
        });
        System.out.println("================");
        List<MsgDto> msgList = new ArrayList<>();
        for (String userId : userIds) {
            LambdaQueryWrapper<OnlineMs> qw = new LambdaQueryWrapper<>();
            qw.eq(OnlineMs::getFromId,userId).or().eq(OnlineMs::getToId,userId).orderByDesc(OnlineMs::getSendTime).last("limit 1");
            OnlineMs one = onlineMsService.getOne(qw);
            MsgDto msgDto = new MsgDto();
            msgDto.setLastMsg(one.getLastContext());
            msgDto.setSendTime(one.getSendTime());
            Map<String, String> oneInfo = userService.getOneInfo(one.getFromId());
            msgDto.setHead(oneInfo.get("head"));
            msgDto.setName(oneInfo.get("username"));
            msgDto.setId(one.getFromId());
            msgList.add(msgDto);
        }
        for (MsgDto msgDto : msgList) {
            LambdaQueryWrapper<OnlineMs> wrap1 = new LambdaQueryWrapper<>();
            wrap1.eq(OnlineMs::getFromId, openid)
                    .eq(OnlineMs::getToId, msgDto.getId())
                    .or()
                    .eq(OnlineMs::getToId, openid)
                    .eq(OnlineMs::getFromId, msgDto.getId())
                    .orderByDesc(OnlineMs::getSendTime).last("limit 1");
            OnlineMs one = onlineMsService.getOne(wrap1);
            msgDto.setSendTime(one.getSendTime());
            msgDto.setLastMsg(one.getLastContext());

            //未读消息
            LambdaQueryWrapper<OnlineMs> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OnlineMs::getToId, openid)
                    .eq(OnlineMs::getFromId, msgDto.getId())
                    .eq(OnlineMs::getIsRead, 0);
            int count = onlineMsService.count(wrapper);
            msgDto.setUnread(count);
        }
        msgList.forEach(System.out::println);
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
    @Test
    void djsj(){
//        Task task = new Task();
//        Date date = Date.from(taskNewDto.getLimitTime().atZone(ZoneId.systemDefault()).toInstant());
//        task.setLimitTime(date);
//        task.setFromId("1");
//        task.setReleaseTime(time);
//        task.setState(0);//已发布
//        Date date = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
//        System.out.println(date.getTime());
//        DateTime date1 = DateUtil.parse(date,"yyyy-MM-dd HH:mm:ss");
//        String tt = "2023-05-10 08:00:01";
//        LocalDateTime parse = LocalDateTimeUtil.parse(tt,"yyyy-MM-dd HH:mm:ss");
//        System.out.println(parse);."1653749793849102337"
        String typeId = "1653749793849102337";
        LambdaQueryWrapper<Task> qw = new LambdaQueryWrapper<>();
        String search = "胶州";
        qw.like(!StringUtil.isNullOrEmpty(search), Task::getLocation, search)
        .or()
        .like(!StringUtil.isNullOrEmpty(search), Task::getDetails, search)
        .or()
        .like(!StringUtil.isNullOrEmpty(search), Task::getTitle, search);
        qw.eq(!StringUtil.isNullOrEmpty(typeId),Task::getTypeId,typeId).eq(Task::getState, 1).orderByDesc(Task::getReleaseTime);
//        qw.eq(!StringUtil.isNullOrEmpty(typeId), Task::getTypeId, typeId).like(!StringUtil.isNullOrEmpty(search), Task::getLocation, search).eq(Task::getState, 1).orderByDesc(Task::getReleaseTime)
//                .or()
//                .eq(!StringUtil.isNullOrEmpty(typeId), Task::getTypeId, typeId).like(!StringUtil.isNullOrEmpty(search), Task::getDetails, search).eq(Task::getState, 1).orderByDesc(Task::getReleaseTime)
//                .or()
//                .eq(!StringUtil.isNullOrEmpty(typeId), Task::getTypeId, typeId).like(!StringUtil.isNullOrEmpty(search), Task::getTitle, search).eq(Task::getState, 1).orderByDesc(Task::getReleaseTime);
        List<Task> taskList = taskService.list(qw);
        System.out.println(taskList);
    }
    @Test
    void nvdni(){

        RedisMsgDto redisMsgDto = new RedisMsgDto();
        redisMsgDto.setMsg("message");
        redisMsgDto.setSendTime(LocalDateTime.now());
        stringRedisTemplate.opsForHash().put(REDIS_MSG_KEY+"key","hashkey", JSON.toJSONString(redisMsgDto));
        Object o = stringRedisTemplate.opsForHash().get(REDIS_MSG_KEY + "key", "hashkey");
        RedisMsgDto redisMsgDtoss = JSON.to(RedisMsgDto.class, o);
        System.out.println(redisMsgDtoss);
//        String limitTimeStr = DateUtil.formatLocalDateTime(LocalDateTime.now());
//        System.out.println(limitTimeStr);
//        Date limitTimed = DateUtil.parse(limitTimeStr);
//        System.out.println(limitTimed);
//        Task task = new Task();
//        task.setId("1653977026547265538");
//        task.setLimitTime(limitTimed);
//        taskService.updateById(task);
    }
    @Resource
    TaskMapper taskMapper;
    @Test
    void nsni(){

//// 使用 Lambda 表达式
//        T result = taskMapper.list(new QueryWrapper<Task>().lambda().orderByAsc(Func.rand()).last("LIMIT 1")).get(0);
//
//// 使用 Wrapper
//        T result = taskMapper.selectList(new QueryWrapper<T>().orderByAsc(Func.rand()).last("LIMIT 1")).get(0);
//
//        // 使用 Lambda 表达式
//        T result = taskMapper.selectOne(new QueryWrapper<T>().lambda().orderByAsc(Func.rand()).last("LIMIT 1"));
//
//// 使用 Wrapper
//        T result = baseMapper.selectOne(new QueryWrapper<T>().orderByAsc(Func.rand()).last("LIMIT 1"));

    }
    @Test
    void contextLoads() {
        String str = "915950092@qq.com" ;

        String[] result = str.split("@") ;
        String sss= result[0];
        System.out.println(sss);
//        for (int i = 0; i < result.length; i++) {
////            String[] temp = result[i].split("=") ;
////            System.out.println(temp[0]+" = "+temp[1]);
//        }
    }
    @Test
    public void register() throws IOException {
//        {
//            try {
//                String qq = "1480069996";
//                //api url地址
//                String url = "https://api.lixingyong.com/api/qq?id="+qq;
//                // 封装参数，千万不要替换为Map与HashMap，否则参数无法传递
//                MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
//                params.add("id",qq);
//                System.out.println("发送数据：" + params);
//                //发送http请求并返回结果
//                String result = HttpRestUtils.get(url,params);
//                System.out.println(result);
//                String[] attribute =result.split("\\,");
//                String un= attribute[1];
//                String replace = un.replace("\"", "");
//                System.out.println(replace);
//                String username= replace.substring(9);
//                System.out.println(username);
//                String hd =attribute[2];
//                String replace1 = hd.replace("\"", "");
//                String head = replace1.substring(7);
//                System.out.println(head);
//                System.out.print("接收反馈：" + result);
////                return result;
//            } catch (Exception e) {
//                log.info("获取qq头像出现错误");
//                log.info(e.getMessage());
//                System.out.println("------------- " + this.getClass().toString() + ".PostData() : 出现异常 Exception -------------");
//                System.out.println(e.getMessage());
////                return "获取qq头像出现错误";
//            }
//        }
    }

    @Test
    public void register1() throws IOException {
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        // 封装参数，千万不要替换为Map与HashMap，否则参数无法传递
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        String appid = "";
        params.add("appid", appid);
        String secret = "";
        params.add("secret",secret);
        String js_code = "";
        params.add("js_code",js_code);
        String grant_type = "authorization_code";
        params.add("grant_type",grant_type);
//        String result = HttpRestUtils.get(url,params);
//        System.out.println(result);
    }

    @Test
    void sads(){
        String s="{\"qq\":\"949516815\",\"nickname\":\".\",\"avatar\":\"https://q1.qlogo.cn/g?b=qq&nk=949516815&s=40\",\"email\":\"949516815@qq.com\",\"url\":\"https://user.qzone.qq.com/949516815\"}";
        com.alibaba.fastjson.JSONObject jsonObject= com.alibaba.fastjson.JSON.parseObject(s);
        System.out.println(jsonObject.getString("qq"));
    }
    @Test
    void pmpm(){
        try {
            String qq = "1480069996";
            //api url地址
            String url = "https://api.lixingyong.com/api/qq?id="+qq;
//            String url = "https://api.lixingyong.com/api/qq";
            //post请求
//                HttpMethod method = HttpMethod.GET;
            // 封装参数，千万不要替换为Map与HashMap，否则参数无法传递
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.set("id",qq);
            System.out.print("发送数据：" + params);
            //发送http请求并返回结果
//            String result = HttpRestUtils.get(url, params);

//            System.out.println(result);
//            com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSON.parseObject(result);
//            String username= jsonObject.getString("nickname");
//            log.info("Username:[{}]",username);
            String head = "http://q1.qlogo.cn/g?b=qq&nk="+qq+"&s=640";
            log.info("head:[{}]",head);
//            System.out.print("接收反馈：" + result);
            User user = new User();
//            user.setUsername(username);
//            user.setHead(head);

        } catch (Exception e) {
            log.info("获取qq头像出现错误");
            log.info(e.getMessage());

        }

    }
    @Test
    void sfon(){
        //get
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().set(1,new StringHttpMessageConverter(StandardCharsets.UTF_8));
        String uri = "https://apis.map.qq.com/ws/location/v1/ip?key=VSXBZ-S76LQ-N6K5O-G3BNK-WCTYF-WSF2T";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/json;charset=UTF-8"));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        String body = response.getBody();
        System.out.println("body:"+body);
        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSON.parseObject(body);
        String result = jsonObject.getString("result");
        com.alibaba.fastjson.JSONObject jsonObject1 = com.alibaba.fastjson.JSON.parseObject(result);
        String ad_info = jsonObject1.getString("ad_info");
        System.out.println("地址："+ad_info);
        System.out.println(response);
        System.out.println(response.getBody());

        System.out.println("result:"+result);
        //post
//  MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//        params.add("user", "你好");
//
//        // 以表单的方式提交
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        //将请求头部和参数合成一个请求
//        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
//
//        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
//
//        return response.getBody();

//        JSONObject jsonObject = JSON.parseObject(result);
//        String ad_info= jsonObject.getString("ad_info");
//        JSONObject jsonObject2 = JSON.parseObject(ad_info);
//        String address=jsonObject2.getString("nation")+jsonObject.getString("province")
//                +jsonObject.getString("city")+jsonObject.getString("district");
//        System.out.println(address);
    }
    @Test
    void sdlkfnion(){
//        Set keys1 = redisTemplate.boundHashOps("HashKey").keys();
//        System.out.println(keys1);
//        BoundHashOperations hashKey = redisTemplate.boundHashOps("26");
//        Set keys2 = hashKey.keys();
//        System.out.println(keys2);
//        Object o = redisTemplate.opsForHash().get("26", "500");
//        System.out.println(o);
//        RedisOperations operations = redisTemplate.opsForHash().getOperations();
//        System.out.println(operations);

        Set<String> keys = redisTemplate.keys("M"+"*");  //获取M开头的key
        if(!keys.isEmpty()){
            for (int i = 0; i < keys.size(); i++) {
                System.out.println(keys.toArray()[i]);//变为数组取第一个
                String o = (String) keys.toArray()[i];
                String m = o.replaceFirst("M", "");
                System.out.println(m);
            }
        }
        System.out.println( keys);
//        Iterator<String> it1 = keys.iterator();
//        System.out.println("toString: " + it1.toString());
//        while (it1.hasNext()) {
//            resultMap =	redisTemplate.opsForHash().entries(it1.next());
//            System.out.println("isEmpty :  " + resultMap.isEmpty());
//            for (Map.Entry<Object, Object> entry : resultMap.entrySet()) {
//                //这里可以写自己的操作
//                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue() + ";");
//            }

    }
    @Test
    void skmndo(){
        //连接阿里云
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "LTAI5tEgEn2SFs9NTeMH2QJ1", "5qoYF5ptXC2F8j0WXGoFC27CmHBt8K");
        /** use STS Token
         DefaultProfile profile = DefaultProfile.getProfile(
         "<your-region-id>",           // The region ID
         "<your-access-key-id>",       // The AccessKey ID of the RAM account
         "<your-access-key-secret>",   // The AccessKey Secret of the RAM account
         "<your-sts-token>");          // STS Token
         **/
        IAcsClient client = new DefaultAcsClient(profile);

        //构建请求
        SendSmsRequest request = new SendSmsRequest();
        request.setPhoneNumbers("");
        request.setSignName("");
        request.setTemplateCode("");

        try {
            SendSmsResponse response = client.getAcsResponse(request);
            System.out.println(new Gson().toJson(response));
        } catch (ClientException e) {
            System.out.println("ErrCode:" + e.getErrCode());
            System.out.println("ErrMsg:" + e.getErrMsg());
            System.out.println("RequestId:" + e.getRequestId());
        }



    }
    @Test
    void  ssdsd(){
        HashMap<String, String> map = new HashMap<>();
        map.put("phone","18119451226");
        map.put("code","5837");
        int ss=0;
        for (String key : map.keySet()) {
            String value = map.get(key);
            if (key.equals("phone")){
                ss=1;
            }
            System.out.println("key: "+key);
            System.out.println(key + "  " + value);
        }
        System.out.println("ss: "+ss);
    }
    @Test
    void  sdsafcv(){
        Set<String> keys = redisTemplate.keys("TaskKind"+"*");  //获取M开头的所有key
        if(!keys.isEmpty()){
            for (int i = 0; i < keys.size(); i++) {
                String m = (String) keys.toArray()[i];
                Boolean delete = redisTemplate.delete(m);
                log.info("TaskKind:[{}]",delete);
            }
        }
    }
    @Test
    void sxbxc(){
        ArrayList<String> cc = new ArrayList<>();
        cc.add("aa");
        cc.add("bb");
        cc.add("cc");
        System.out.println(cc);
    }
    @Test
    void nmi(){
        LocalDateTime now = LocalDateTime.now();
        log.info("当前时间：{}",now);
//        int i = limitTime.getSecond() - now.getSecond();
        LocalDateTime limitTime = LocalDateTime.now().plusHours(3);
        log.info("之后:{}",limitTime);
        Duration between = Duration.between(now, limitTime);
        long i = between.toMinutes();
        log.info("时间差：{}",i);
    }
    @Test
    void dnis(){
        String ss = "2022-10-28 01:15:27";
        LocalDateTime ss1 = LocalDateTime.parse("2022-10-28T01:15:27");
        LambdaQueryWrapper<Task> wrap = new LambdaQueryWrapper<>();
        wrap.eq(Task::getReleaseTime,ss1);
        Task one = taskService.getOne(wrap);
        System.out.println(one);
    }
    @Test
    void vxfd(){
        int ss=100;
        for (int i = 0; i < 11; i++) {
            ss= (int) (ss*1.5);
            System.out.println(ss);
        }
    }
    @Test
    void  kncn(){
        System.out.println(LocalDateTime.now().toLocalDate());
    }
    @Test
    void fni(){
        String condition = "饭";
        LambdaQueryWrapper<Task> wrap = new LambdaQueryWrapper<>();
//        wrap.like(null != condition,Task::getName,condition)
//                .or()
//                .like(null != condition,Task::getLocation,condition);
        List<Task> list = taskService.list(wrap);
        log.info(list.toString());
    }
}
