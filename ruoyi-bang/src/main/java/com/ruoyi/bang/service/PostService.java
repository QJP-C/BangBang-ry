package com.ruoyi.bang.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.bang.common.R;
import com.ruoyi.bang.domain.Post;
import com.ruoyi.bang.dto.PostDetDto;
import com.ruoyi.bang.dto.PostListResDto;
import com.ruoyi.bang.dto.PostNewParamDto;

import java.util.List;

/**
 * (Post)表服务接口
 *
 * @author makejava
 * @since 2023-04-21 20:44:28
 */
public interface PostService extends IService<Post> {
    /**
     * 发布帖子
     *
     * @param openid
     * @param postNewParamDto
     * @param post
     * @return
     */
    R<String> savePost(String openid, PostNewParamDto postNewParamDto, Post post);

    /**
     * 帖子详情
     * @param openid
     * @param postId
     * @return
     */
    R<PostDetDto> onePost(String openid, String postId);

    /**
     * 点赞/取消帖子
     * @param openid
     * @param postId
     * @return
     */
    R<String> likePost(String openid, String postId);

    R<String> collectPost(String openid, String postId);

    /**
     * 按话题查(热门)
     * @param openid
     * @param topicId
     * @return
     */
    R<Page<PostListResDto>> pageByHotTopic(String openid, String topicId, int page, int pageSize);

    /**
     * 关注的用户动态
     * @param openid
     * @param max
     * @param offset
     * @param pageSize
     * @return
     */
    R queryPostOfFollow(String openid, Long max, Integer offset, Integer pageSize);

    /**
     * 按话题查(新帖)
     * @param openid
     * @param topicId
     * @param page
     * @param pageSize
     * @return
     */
    R<Page<PostListResDto>> pageByNewTopic(String openid, String topicId, int page, int pageSize);

    /**
     * 推荐
     *
     * @param openid
     * @param page
     * @param pageSize
     * @param search
     * @return
     */
    R queryPostOfRecommend(String openid, int page, int pageSize, String search);

    /**
     * 图文
     *
     * @param openid
     * @param page
     * @param pageSize
     * @param search
     * @return
     */
    R queryPostOfImageText(String openid, int page, int pageSize, String search);

    /**
     * 个人动态
     * @param openid
     * @param page
     * @param pageSize
     * @return
     */
    R queryPostOfPersonal(String openid, int page, int pageSize);

    /**
     * 我的收藏(帖子)
     * @param openid
     * @param page
     * @param pageSize
     * @return
     */
    R queryPostOfMyCollect(String openid, int page, int pageSize);

    /**
     * 评论帖子
     *
     * @param openid
     * @param postId
     * @param text
     * @return
     */
    R commentPost(String openid, String postId, String text);

    /**
     * 点赞/取消帖子
     * @param openid
     * @param postCommentId
     * @return
     */
    R likeComment(String openid, String postCommentId);

    R commentList(String openid, String postId, int page, int pageSize);



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
     * 批量删除帖子审核
     *
     * @param ids 需要删除的帖子审核主键集合
     * @return 结果
     */
    public int deletePostByIds(String[] ids);

    /**
     * 删除帖子审核信息
     *
     * @param id 帖子审核主键
     * @return 结果
     */
    public int deletePostById(String id);

    /**
     * 指定话题的帖子条数
     * @param id
     * @return
     */
    int topicNum(String id);

    /**
     * 话题列表(个人主页)
     * @param openid
     * @return
     */
    R personalTopic(String openid);

    /**
     * 个人评论(个人主页)
     * @param openid
     * @return
     */
    R personalComment(String openid);


}

