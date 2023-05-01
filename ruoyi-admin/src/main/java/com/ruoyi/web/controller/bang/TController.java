package com.ruoyi.web.controller.bang;

import com.ruoyi.bang.domain.Task;
import com.ruoyi.bang.service.TaskService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 任务审核Controller
 *
 * @author qjp
 * @date 2023-04-30
 */
@RestController
@RequestMapping("/bang/taskAudit")
public class TController extends BaseController
{
    @Autowired
    private TaskService taskService;

    /**
     * 查询任务审核列表
     */
    @PreAuthorize("@ss.hasPermi('bang:taskAudit:list')")
    @GetMapping("/list")
    public TableDataInfo list(Task task)
    {
        startPage();
        List<Task> list = taskService.selectTaskList(task);
        return getDataTable(list);
    }

    /**
     * 导出任务审核列表
     */
    @PreAuthorize("@ss.hasPermi('bang:taskAudit:export')")
    @Log(title = "任务审核", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Task task)
    {
        List<Task> list = taskService.selectTaskList(task);
        ExcelUtil<Task> util = new ExcelUtil<Task>(Task.class);
        util.exportExcel(response, list, "任务审核数据");
    }

    /**
     * 获取任务审核详细信息
     */
    @PreAuthorize("@ss.hasPermi('bang:taskAudit:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(taskService.selectTaskById(id));
    }

    /**
     * 新增任务审核
     */
    @PreAuthorize("@ss.hasPermi('bang:taskAudit:add')")
    @Log(title = "任务审核", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Task task)
    {
        return toAjax(taskService.insertTask(task));
    }

    /**
     * 修改任务审核
     */
    @PreAuthorize("@ss.hasPermi('bang:taskAudit:edit')")
    @Log(title = "任务审核", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Task task)
    {
        return toAjax(taskService.updateTask(task));
    }

    /**
     * 删除任务审核
     */
    @PreAuthorize("@ss.hasPermi('bang:taskAudit:remove')")
    @Log(title = "任务审核", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(taskService.deleteTaskByIds(ids));
    }
}
