package com.ruoyi.web.controller.bang;


import com.ruoyi.bang.common.R;
import com.ruoyi.bang.domain.Topic;
import com.ruoyi.bang.service.TopicFollowService;
import com.ruoyi.bang.service.TopicService;
import com.ruoyi.bang.utils.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * (Topic)表控制层
 *
 * @author makejava
 * @since 2023-04-20 20:18:21
 */
@Api(tags = "话题接口", value = "话题接口")
@RestController
@CrossOrigin
@RequestMapping("bang/topic")
public class TopicController {
    /**
     * 服务对象
     */
    @Resource
    private TopicService topicService;
    @Resource
    JwtUtil jwtUtil;

    @Resource
    private TopicFollowService topicFollowService;
    @ApiOperation("新增分类")
    @PostMapping("newOne")
    public R<String> newTopic(@RequestBody Topic topic){
        return topicService.newTopic(topic);
    }

    @ApiOperation("话题列表")
    @GetMapping("list")
    public R<List<Topic>> getList(@RequestParam(value = "search",required = false)String search){
        return topicService.getList(search);
    }

    @ApiOperation("关注/取关话题")
    @GetMapping("follow/{topicId}")
    public R<String> followTopic(@RequestHeader("Authorization") String header,@PathVariable("topicId")String topicId) {
        String openid = jwtUtil.getOpenidFromToken(header);
        return topicService.followTopic(openid,topicId);
    }

}

