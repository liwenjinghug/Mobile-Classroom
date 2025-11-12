package com.ruoyi.web.controller.proj_qhy;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.proj_qhy.domain.ForumComment;
import com.ruoyi.proj_qhy.domain.ForumLike;
import com.ruoyi.proj_qhy.domain.ForumNotice;
import com.ruoyi.proj_qhy.domain.ForumPost;
import com.ruoyi.proj_qhy.service.IForumCommentService;
import com.ruoyi.proj_qhy.service.IForumLikeService;
import com.ruoyi.proj_qhy.service.IForumNoticeService;
import com.ruoyi.proj_qhy.service.IForumPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 论坛模块控制器
 */
@RestController
@RequestMapping("/proj_qhy/forum")
public class ForumController {

    @Autowired
    private IForumPostService postService;

    @Autowired
    private IForumLikeService likeService;

    @Autowired
    private IForumCommentService commentService;

    @Autowired
    private IForumNoticeService noticeService;

    /**
     * 获取帖子列表
     */
    @GetMapping("/posts")
    public AjaxResult getPostList() {
        Long userId = SecurityUtils.getUserId();
        return AjaxResult.success(postService.selectPostList(userId));
    }

    /**
     * 发布帖子
     */
    @PostMapping("/post/publish")
    public AjaxResult publishPost(ForumPost post, @RequestParam(value = "files", required = false) MultipartFile[] files) {
        return AjaxResult.success(postService.publishPost(post, files));
    }

    /**
     * 刷新帖子列表
     */
    @GetMapping("/posts/refresh")
    public AjaxResult refreshPosts() {
        Long userId = SecurityUtils.getUserId();
        return AjaxResult.success(postService.refreshPostList(userId));
    }

    /**
     * 获取帖子的点赞列表
     */
    @GetMapping("/likes/{postId}")
    public AjaxResult getLikesByPostId(@PathVariable Long postId) {
        return AjaxResult.success(likeService.getLikesByPostId(postId));
    }

    /**
     * 点赞帖子
     */
    @PostMapping("/like/{postId}")
    public AjaxResult likePost(@PathVariable Long postId) {
        Long userId = SecurityUtils.getUserId();
        String username = SecurityUtils.getUsername();
        return AjaxResult.success(likeService.likePost(postId, userId, username));
    }

    /**
     * 取消点赞
     */
    @PostMapping("/like/cancel/{postId}")
    public AjaxResult cancelLike(@PathVariable Long postId) {
        Long userId = SecurityUtils.getUserId();
        String username = SecurityUtils.getUsername();
        return AjaxResult.success(likeService.cancelLike(postId, userId, username));
    }

    /**
     * 获取帖子的评论列表
     */
    @GetMapping("/comments/{postId}")
    public AjaxResult getCommentsByPostId(@PathVariable Long postId) {
        return AjaxResult.success(commentService.getCommentsByPostId(postId));
    }

    /**
     * 添加评论/回复
     */
    @PostMapping("/comment/add")
    public AjaxResult addComment(@RequestBody ForumComment comment) {
        return AjaxResult.success(commentService.addComment(comment));
    }

    /**
     * 获取用户通知
     */
    @GetMapping("/notices")
    public AjaxResult getUserNotices() {
        Long userId = SecurityUtils.getUserId();
        return AjaxResult.success(noticeService.getUserNotices(userId));
    }
}