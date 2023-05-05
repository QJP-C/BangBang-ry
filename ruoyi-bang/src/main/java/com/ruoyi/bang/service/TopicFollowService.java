package com.ruoyi.bang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.bang.domain.TopicFollow;

/**
 * (TopicFollow)表服务接口
 *
 * @author makejava
 * @since 2023-04-20 21:27:43
 */
public interface TopicFollowService extends IService<TopicFollow> {
    /**
     * 加入该话题的人数
     * @param id
     * @return
     */
    int joinNum(String topicId);
}

