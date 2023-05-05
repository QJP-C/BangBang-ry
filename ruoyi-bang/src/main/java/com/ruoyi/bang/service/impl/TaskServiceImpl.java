package com.ruoyi.bang.service.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.bang.common.R;
import com.ruoyi.bang.domain.*;
import com.ruoyi.bang.dto.TaskDetailsResultDto;
import com.ruoyi.bang.dto.TaskListResDto;
import com.ruoyi.bang.dto.TaskNewDto;
import com.ruoyi.bang.exception.BangException;
import com.ruoyi.bang.mapper.TaskMapper;
import com.ruoyi.bang.service.*;
import com.ruoyi.common.utils.DateUtils;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ruoyi.bang.common.Constants.REDIS_COUNTDOWN_KEY;

/**
 * (Task)表服务实现类
 *
 * @author makejava
 * @since 2023-04-17 11:34:28
 */
@Service("taskService")
@Slf4j
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    @Resource
    private UserService userService;
    @Resource
    private TaskMapper taskMapper;
    @Resource
    private TaskClassService taskClassService;
    @Resource
    private FileService fileService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private TaskCollectService taskCollectService;
    @Resource
    private TaskHistoryService taskHistoryService;

    /**
     * 发布任务
     *
     * @param openid
     * @param taskNewDto
     * @return
     */
    @Override
    @Transactional
    public R<String> newTask(String openid, TaskNewDto taskNewDto) {

        LocalDateTime now = LocalDateTime.now();
        String s = DateUtil.formatLocalDateTime(now);
        Date time = DateUtil.parse(s);
        Task task = new Task();
        BeanUtils.copyProperties(taskNewDto, task);
        String limitTimeStr = DateUtil.formatLocalDateTime(taskNewDto.getLimitTime());
        Date limitTimed = DateUtil.parse(limitTimeStr);
        task.setLimitTime(limitTimed);
        task.setFromId(openid);
        task.setReleaseTime(time);
        task.setState(0);//已发布
        boolean insert = this.save(task);
        if (!insert) {
            BangException.cast("发布失败!");
        }
        String taskId = task.getId();
        String url = taskNewDto.getUrls();
        File file = new File();
        file.setAboutId(taskId);
        file.setBelong(1);//1发布任务附件
        file.setUrl(url);
        file.setCreateTime(now);
        fileService.save(file);
        if (taskNewDto.getLimitTime() != null) {
            boolean flag = countdown(taskNewDto.getLimitTime(), taskId);
            if (!flag) {
                BangException.cast("添加计时任务失败!  截止时间不能在当前时间之前");
            }
        }
        return R.success("发布成功");
    }

    /**
     * 任务详情
     *
     * @param openid
     * @param taskId
     * @return
     */
    @Override
    public R<TaskDetailsResultDto> taskDetails(String openid, String taskId) {
        Task task = this.getById(taskId);
        TaskDetailsResultDto dto = new TaskDetailsResultDto();
        BeanUtils.copyProperties(task, dto);
        //该任务当前用户是否收藏
        int like = isLike(openid, taskId);
        dto.setIsCollect(like);
        //用户信息
        Map<String, String> oneInfo = userService.getOneInfo(task.getFromId());
        dto.setFromHead(oneInfo.get("head"));
        dto.setFromName(oneInfo.get("username"));
        //分类信息
        dto.setType(className(task.getTypeId()));
        //获取附件url
        String[] fromFiles = files(taskId, "1");
        if (fromFiles != null) {
            dto.setFromUrls(fromFiles);
        }
        //查看是否有接单人
        if (!StringUtil.isNullOrEmpty(task.getToId())) {//有接单人
            //用户信息
            Map<String, String> toInfo = userService.getOneInfo(task.getToId());
            dto.setToHead(toInfo.get("head"));
            dto.setToName(toInfo.get("username"));
            //是否有提交的附件
            String[] toFiles = files(taskId, "2");
            if (!ArrayUtil.isEmpty(toFiles)) {
                dto.setToUrls(toFiles);
            }
        }

        //保存历史足迹
        boolean b = taskHistoryService.addHistory(openid, taskId);
        if (!b) BangException.cast("保持历史记录失败，请重试!");
        return R.success(dto);
    }

    /**
     * 任务列表
     *
     * @param openid
     * @param typeId
     * @param search
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public R<Page<TaskListResDto>> taskList(String openid, String typeId, String search, int page, int pageSize) {
        LambdaQueryWrapper<Task> qw = new LambdaQueryWrapper<>();
        qw.like(!StringUtil.isNullOrEmpty(search), Task::getLocation, search)
                .or()
                .like(!StringUtil.isNullOrEmpty(search), Task::getDetails, search)
                .or()
                .like(!StringUtil.isNullOrEmpty(search), Task::getTitle, search);
        qw.eq(!StringUtil.isNullOrEmpty(typeId), Task::getTypeId, typeId).eq(Task::getState, 1).orderByDesc(Task::getReleaseTime);
        return R.success(getListR(openid, qw, page, pageSize));
    }

    /**
     * 我的发布
     *
     * @param openid
     * @param status
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public R<Page<TaskListResDto>> myList(String openid, Integer status, int page, int pageSize) {
        LambdaQueryWrapper<Task> qw = new LambdaQueryWrapper<>();
        qw.eq(Task::getFromId, openid);
        //是否按状态查询
        if (status != null) {
            qw.eq(Task::getState, status);
        }
        qw.orderByDesc(Task::getReleaseTime);
        return R.success(getListR(openid, qw, page, pageSize));
    }

    /**
     * 我的足迹
     *
     * @param openid
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public R<Page<TaskListResDto>> history(String openid, int page, int pageSize) {
        LambdaQueryWrapper<TaskHistory> qw = new LambdaQueryWrapper<>();
        qw.eq(TaskHistory::getUserId, openid);
        qw.orderByDesc(TaskHistory::getBrowseTime);
        //分页构造器对象
        Page<TaskHistory> pageInfo = new Page<>(page, pageSize);
        pageInfo.setOptimizeCountSql(false);
        Page<TaskListResDto> dtoPage = new Page<>(page, pageSize);
        taskHistoryService.page(pageInfo, qw);
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        List<TaskHistory> records = pageInfo.getRecords();
        List<TaskListResDto> list = records.stream().map(i -> {
            Task task = this.getById(i.getTaskId());
            return getTaskListResDto(openid, task);
        }).collect(Collectors.toList());
        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    /**
     * 我的收藏
     *
     * @param openid
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public R<Page<TaskListResDto>> myCollect(String openid, int page, int pageSize) {
        //分页构造器对象
        Page<TaskCollect> pageInfo = new Page<>(page, pageSize);
        pageInfo.setOptimizeCountSql(false);
        Page<TaskListResDto> dtoPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<TaskCollect> qw = new LambdaQueryWrapper<>();
        qw.eq(TaskCollect::getUserId, openid).orderByDesc(TaskCollect::getCollectTime);
        taskCollectService.page(pageInfo, qw);
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        List<TaskCollect> records = pageInfo.getRecords();
        List<TaskListResDto> list = records.stream().map(i -> {
            Task task = this.getById(i.getTaskId());
            return getTaskListResDto(openid, task);
        }).collect(Collectors.toList());
        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    /**
     * 接任务
     *
     * @param openid
     * @param taskId
     * @return
     */
    @Override
    public R acceptTask(String openid, String taskId) {
        Task task = this.getById(taskId);
        if (ObjectUtil.isEmpty(task)) BangException.cast("任务不存在！");
        Integer state = task.getState();
        if (state != 1) {
            BangException.cast("该任务已被接取或正在审核！");
        }
        if (!StringUtil.isNullOrEmpty(task.getToId())) {
            BangException.cast("您手慢啦！该任务已经被别抢走啦！");
        }
        task.setState(2);
        task.setToId(openid);
        boolean b = this.updateById(task);
        return b ? R.success("接单成功！") : R.success("接单失败！");
    }

    /**
     * 完成任务
     *
     * @param openid
     * @param taskId
     * @param urls
     * @return
     */
    @Override
    public R finishAccept(String openid, String taskId, String[] urls) {
        Task task = this.getById(taskId);
        if (ObjectUtil.isEmpty(task)) BangException.cast("任务不存在！");
        if (!task.getToId().equals(openid)) BangException.cast("无法完成别人的任务！");
        task.setState(3);
        boolean b = this.updateById(task);
        if (b) {
            //添加完成任务附件
            for (String url : urls) {
                boolean flag = fileService.addTaskFile(url, taskId);
                if (!flag) BangException.cast("添加附件失败，请重新提交！");
            }
        }
        return R.success("提交任务成功！");
    }

    /**
     * 我的帮忙(任务)
     *
     * @param openid
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    @Override
    public R myHelp(String openid, int page, int pageSize, Integer status) {
        LambdaQueryWrapper<Task> qw = new LambdaQueryWrapper<>();
        qw.eq(Task::getToId, openid);
        if (status != null) {
            if (status == 3) {//已完成
                qw.eq(Task::getState, 3);
            } else if (status == 4) {//已逾期
                qw.eq(Task::getState, 0);
            } else {
                qw.eq(Task::getState, 2);
            }
        }
        qw.orderByDesc(Task::getReleaseTime);
        Page<TaskListResDto> listR = getListR(openid, qw, page, pageSize);
        return R.success(listR);
    }

    /**
     * 获取列表
     *
     * @param openid
     * @param qw
     * @return
     */
    @NotNull
    private Page<TaskListResDto> getListR(String openid, LambdaQueryWrapper<Task> qw, int page, int pageSize) {
        //分页构造器对象
        Page<Task> pageInfo = new Page<>(page, pageSize);
        pageInfo.setOptimizeCountSql(false);
        Page<TaskListResDto> dtoPage = new Page<>(page, pageSize);
        this.page(pageInfo, qw);
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        List<Task> list = pageInfo.getRecords();
        List<TaskListResDto> res = list.stream().map(task -> {
            return getTaskListResDto(openid, task);
        }).collect(Collectors.toList());
        dtoPage.setRecords(res);
        return dtoPage;
    }

    /**
     * 随机任务id
     * @return
     */
    @Override
    public R randomTask() {
        LambdaQueryWrapper<Task> qw = new LambdaQueryWrapper<>();
        qw.eq(Task::getState,1);
        List<Task> taskList = this.list(qw);
        Task task = taskList.get(new Random().nextInt(taskList.size()));
        return R.success(task.getId());
    }
    /**
     * 获取任务相关信息
     *
     * @param openid
     * @param task
     * @return
     */
    @NotNull
    private TaskListResDto getTaskListResDto(String openid, Task task) {
        TaskListResDto dto = new TaskListResDto();
        BeanUtils.copyProperties(task, dto);
        //用户信息
        Map<String, String> oneInfo = userService.getOneInfo(task.getFromId());
        dto.setHead(oneInfo.get("head"));
        dto.setUsername(oneInfo.get("username"));
        //是否收藏
        int like = isLike(openid, task.getId());
        dto.setIsCollect(like);
        //截止时间
        String limitTimeStr = DateUtil.format(task.getLimitTime(), "yyyy-MM-dd HH:mm:ss");
        LocalDateTime limitTimed = LocalDateTimeUtil.parse(limitTimeStr, "yyyy-MM-dd HH:mm:ss");
        dto.setLimitTime(limitTimed);
        //类型信息
        String typeId = task.getTypeId();
        if (StringUtil.isNullOrEmpty(typeId)) BangException.cast("请选择类型");
        String typeName = taskClassService.typesName(typeId);
        dto.setType(typeName);
        return dto;
    }

    /**
     * 获取文件数组
     *
     * @param taskId
     * @param beLong
     * @return
     */
    private String[] files(String taskId, String beLong) {
        LambdaQueryWrapper<File> qw = new LambdaQueryWrapper<>();
        qw.eq(File::getAboutId, taskId).eq(File::getBelong, beLong);
        int count = fileService.count();
        if (count <= 0) {
            return null;
        }
        List<File> list = fileService.list(qw);
        String[] res = new String[list.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = list.get(i).getUrl();
        }
        return res;
    }

    /**
     * 获取该订单分类名称
     *
     * @param typeId
     * @return
     */
    private String className(String typeId) {
        TaskClass son = taskClassService.getById(typeId);
        return son.getName();
    }


    /**
     * 获取该用户是否收藏该任务
     *
     * @param openid
     * @param taskId
     * @return
     */
    private int isLike(String openid, String taskId) {
        LambdaQueryWrapper<TaskCollect> qw = new LambdaQueryWrapper<>();
        qw.eq(TaskCollect::getUserId, openid).eq(TaskCollect::getTaskId, taskId);
        return taskCollectService.count(qw);
    }

    /**
     * 向redis添加任务截止倒计时任务
     *
     * @param limitTime
     * @param taskId
     * @return
     */
    public boolean countdown(LocalDateTime limitTime, String taskId) {
        //计算时间差
        Duration between = Duration.between(LocalDateTime.now(), limitTime);
        long i = (int) between.toMillis();
        if (i <= 0L) {
            return false;
        }
        stringRedisTemplate.opsForValue().set(REDIS_COUNTDOWN_KEY + taskId, "任务逾期缓存", i, TimeUnit.MILLISECONDS);
        return true;
    }


    /**
     * 查询任务审核
     *
     * @param id 任务审核主键
     * @return 任务审核
     */
    @Override
    public Task selectTaskById(String id) {
        return taskMapper.selectTaskById(id);
    }

    /**
     * 查询任务审核列表
     *
     * @param task 任务审核
     * @return 任务审核
     */
    @Override
    public List<Task> selectTaskList(Task task) {
//        return taskMapper.selectTaskList(task);
        LambdaQueryWrapper<Task> qw = new LambdaQueryWrapper<>();
        qw.like(task.getDetails() != null, Task::getDetails, task.getDetails())
                .like(task.getTitle() != null, Task::getTitle, task.getTitle())
                .eq(task.getState() != null, Task::getState, task.getState())
                .eq(task.getUrgent() != null, Task::getUrgent, task.getUrgent())
                .like(task.getLocation() != null, Task::getLocation, task.getLocation())
                .like(task.getReleaseTime() != null, Task::getReleaseTime, task.getUrgent())
                .like(task.getLimitTime() != null, Task::getLimitTime, task.getReleaseTime());
        List<Task> taskList = this.list(qw);
        List<Task> collect = taskList.stream().map(task1 -> {
            task1.setFromUrls(fileService.getTaskFiles(task1.getId()));
            return task1;
        }).collect(Collectors.toList());
        ListUtil.sortByProperty(collect, "releaseTime");
        ListUtil.reverse(collect);
        return collect;
    }

    /**
     * 新增任务审核
     *
     * @param task 任务审核
     * @return 结果
     */
    @Override
    public int insertTask(Task task) {
        task.setCreateTime(DateUtils.getNowDate());
        return taskMapper.insertTask(task);
    }

    /**
     * 修改任务审核
     *
     * @param task 任务审核
     * @return 结果
     */
    @Override
    public int updateTask(Task task) {
        task.setUpdateTime((DateUtils.getNowDate()));
        return taskMapper.updateTask(task);
    }

    /**
     * 批量删除任务审核
     *
     * @param ids 需要删除的任务审核主键
     * @return 结果
     */
    @Override
    public int deleteTaskByIds(String[] ids) {
        return taskMapper.deleteTaskByIds(ids);
    }

    /**
     * 删除任务审核信息
     *
     * @param id 任务审核主键
     * @return 结果
     */
    @Override
    public int deleteTaskById(String id) {
        return taskMapper.deleteTaskById(id);
    }

}

