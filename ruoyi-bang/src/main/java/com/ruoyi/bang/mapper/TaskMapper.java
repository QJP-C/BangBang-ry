package com.ruoyi.bang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.bang.domain.Task;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * (Task)表数据库访问层
 *
 * @author makejava
 * @since 2023-04-17 11:34:28
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {
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
     * 删除任务审核
     *
     * @param id 任务审核主键
     * @return 结果
     */
    public int deleteTaskById(String id);

    /**
     * 批量删除任务审核
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTaskByIds(String[] ids);
}

