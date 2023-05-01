package com.ruoyi.bang.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.bang.mapper.TaskAuditMapper;
import com.ruoyi.bang.domain.TaskAudit;
import com.ruoyi.bang.service.TaskAuditService;
import org.springframework.stereotype.Service;

/**
 * (TaskAudit)表服务实现类
 *
 * @author makejava
 * @since 2023-04-18 21:41:53
 */
@Service("taskAuditService")
public class TaskAuditServiceImpl extends ServiceImpl<TaskAuditMapper, TaskAudit> implements TaskAuditService {

}

