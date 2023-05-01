package com.ruoyi.bang.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.bang.common.R;
import com.ruoyi.bang.domain.Task;
import com.ruoyi.bang.dto.TaskDetailsResultDto;
import com.ruoyi.bang.dto.TaskListResDto;
import com.ruoyi.bang.dto.TaskNewDto;

import java.util.List;

/**
 * (Task)表服务接口
 *
 * @author makejava
 * @since 2023-04-17 11:34:28
 */
public interface TaskService extends IService<Task> {
    /**
     * 发布任务
     * @param openid
     * @param taskNewDto
     * @return
     */
    R<String> newTask(String openid, TaskNewDto taskNewDto);

    /**
     * 发布任务
     * @param openid
     * @param taskId
     * @return
     */
    R<TaskDetailsResultDto> taskDetails(String openid, String taskId);

    /**
     * 任务详情
     * @param openid
     * @param typeId
     * @param search
     * @param page
     * @param pageSize
     * @return
     */
    R<Page<TaskListResDto>> taskList(String openid, String typeId, String search, int page, int pageSize);

    /**
     * 我的发布
     * @param openid
     * @param status
     * @param page
     * @param pageSize
     * @return
     */
    R<Page<TaskListResDto>> myList(String openid, Integer status, int page, int pageSize);

    /**
     * 我的足迹
     * @param openid
     * @param page
     * @param pageSize
     * @return
     */
    R<Page<TaskListResDto>> history(String openid, int page, int pageSize);

    /**
     * 我的收藏
     * @param openid
     * @param page
     * @param pageSize
     * @return
     */
    R<Page<TaskListResDto>> myCollect(String openid, int page, int pageSize);

    /**
     * 接任务
     * @param openid
     * @param taskId
     * @return
     */
    R acceptTask(String openid, String taskId);

    /**
     * 完成任务
     * @param openid
     * @param taskId
     * @param urls
     * @return
     */
    R finishAccept(String openid, String taskId, String[] urls);

    /**
     * 我的帮忙(任务)
     * @param openid
     * @param page
     * @param pageSize
     * @return
     */
    R myHelp(String openid, int page, int pageSize, Integer status);

    /**
     * 查询任务审核
     *
     * @param id 任务审核主键
     * @return 任务审核
     */
    public Task selectTaskById(String id);

    /**
     * 查询任务审核列表
     *
     * @param task 任务审核
     * @return 任务审核集合
     */
    public List<Task> selectTaskList(Task task);

    /**
     * 新增任务审核
     *
     * @param task 任务审核
     * @return 结果
     */
    public int insertTask(Task task);

    /**
     * 修改任务审核
     *
     * @param task 任务审核
     * @return 结果
     */
    public int updateTask(Task task);

    /**
     * 批量删除任务审核
     *
     * @param ids 需要删除的任务审核主键集合
     * @return 结果
     */
    public int deleteTaskByIds(String[] ids);

    /**
     * 删除任务审核信息
     *
     * @param id 任务审核主键
     * @return 结果
     */
    public int deleteTaskById(String id);
}

