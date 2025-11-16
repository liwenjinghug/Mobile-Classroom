package com.ruoyi.proj_qhy.service;

import com.ruoyi.proj_qhy.domain.ForumPost;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IForumPostService {
    /**
     * 查询帖子列表
     */
    List<ForumPost> selectPostList(Long userId);

    /**
     * 发布帖子
     */
    boolean publishPost(ForumPost post, MultipartFile[] files);

    /**
     * 刷新帖子列表
     */
    List<ForumPost> refreshPostList(Long userId);

    /**
     * 修改帖子
     */
    boolean updatePost(ForumPost post, MultipartFile[] files);

    /**
     * 删除帖子
     */
    boolean deletePost(Long postId);
}