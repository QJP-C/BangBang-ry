package com.ruoyi.bang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.bang.domain.Post;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * (Post)表数据库访问层
 *
 * @author makejava
 * @since 2023-04-21 20:44:28
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {
    /**
     * 查询帖子审核
     *
     * @param id 帖子审核主键
     * @return 帖子审核
     */
    public Post selectPostById(String id);

    /**
     * 查询帖子审核列表
     *
     * @param post 帖子审核
     * @return 帖子审核集合
     */
    public List<Post> selectPostList(Post post);

    /**
     * 新增帖子审核
     *
     * @param post 帖子审核
     * @return 结果
     */
    public int insertPost(Post post);

    /**
     * 修改帖子审核
     *
     * @param post 帖子审核
     * @return 结果
     */
    public int updatePost(Post post);

    /**
     * 删除帖子审核
     *
     * @param id 帖子审核主键
     * @return 结果
     */
    public int deletePostById(String id);

    /**
     * 批量删除帖子审核
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePostByIds(String[] ids);
}

