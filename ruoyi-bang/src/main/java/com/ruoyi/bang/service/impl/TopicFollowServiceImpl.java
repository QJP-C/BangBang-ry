package com.ruoyi.bang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.bang.mapper.TopicFollowMapper;
import com.ruoyi.bang.domain.TopicFollow;
import com.ruoyi.bang.service.TopicFollowService;
import org.springframework.stereotype.Service;

/**
 * (TopicFollow)表服务实现类
 *
 * @author makejava
 * @since 2023-04-20 21:27:43
 */
@Service("topicFollowService")
public class TopicFollowServiceImpl extends ServiceImpl<TopicFollowMapper, TopicFollow> implements TopicFollowService {
    /**
     * 加入该话题的人数
     * @param topicId
     * @return
     */
    @Override
    public int joinNum(String topicId) {
        LambdaQueryWrapper<TopicFollow> qw = new LambdaQueryWrapper<>();
        qw.eq(TopicFollow::getTopicId,topicId);
        return this.count(qw);
    }
}

