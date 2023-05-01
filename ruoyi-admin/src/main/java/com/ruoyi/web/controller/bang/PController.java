package com.ruoyi.web.controller.bang;

import com.ruoyi.bang.domain.Post;
import com.ruoyi.bang.service.PostService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


@RestController
@RequestMapping("/bang/postAudit")
public class PController  extends BaseController {

    @Resource
    private PostService postService;

    /**
     * 查询帖子审核列表
     */
    @PreAuthorize("@ss.hasPermi('bang:postAudit:list')")
    @GetMapping("/list")
    public TableDataInfo list(Post post)
    {
        startPage();
        List<Post> list = postService.selectPostList(post);
        return getDataTable(list);
    }

    /**
     * 导出帖子审核列表
     */
    @PreAuthorize("@ss.hasPermi('bang:postAudit:export')")
    @Log(title = "帖子审核", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Post post)
    {
        List<Post> list = postService.selectPostList(post);
        ExcelUtil<Post> util = new ExcelUtil<Post>(Post.class);
        util.exportExcel(response, list, "帖子审核数据");
    }

    /**
     * 获取帖子审核详细信息
     */
    @PreAuthorize("@ss.hasPermi('bang:postAudit:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id)
    {
        return success(postService.selectPostById(id));
    }

    /**
     * 新增帖子审核
     */
    @PreAuthorize("@ss.hasPermi('bang:postAudit:add')")
    @Log(title = "帖子审核", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Post post)
    {
        return toAjax(postService.insertPost(post));
    }

    /**
     * 修改帖子审核
     */
    @PreAuthorize("@ss.hasPermi('bang:postAudit:edit')")
    @Log(title = "帖子审核", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Post post)
    {
        return toAjax(postService.updatePost(post));
    }

    /**
     * 删除帖子审核
     */
    @PreAuthorize("@ss.hasPermi('bang:postAudit:remove')")
    @Log(title = "帖子审核", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids)
    {
        return toAjax(postService.deletePostByIds(ids));
    }
}
