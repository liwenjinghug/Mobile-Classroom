package com.ruoyi.web.controller.proj_qhy;

// [修改] 使用自定义 Log 注解
import com.ruoyi.proj_cyq.annotation.Log;
import com.ruoyi.common.enums.BusinessType;
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
     * (注意：这里使用 POST + Form-Data 混合接收)
     */
    @Log(title = "论坛-发布帖子", businessType = BusinessType.INSERT)
    @PostMapping("/post/publish")
    public AjaxResult publishPost(ForumPost post, @RequestParam(value = "files", required = false) MultipartFile[] files) {
        // service层已处理用户信息
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
     * 修改帖子
     * (注意：这里使用 PUT + Form-Data 混合接收)
     */
    @Log(title = "论坛-修改帖子", businessType = BusinessType.UPDATE)
    @PutMapping("/post/update")
    public AjaxResult updatePost(ForumPost post, @RequestParam(value = "files", required = false) MultipartFile[] files) {
        // service层已处理用户信息和鉴权
        return AjaxResult.success(postService.updatePost(post, files));
    }

    /**
     * 删除帖子
     */
    @Log(title = "论坛-删除帖子", businessType = BusinessType.DELETE)
    @DeleteMapping("/post/delete/{postId}")
    public AjaxResult deletePost(@PathVariable Long postId) {
        // service层已处理用户信息和鉴权
        return AjaxResult.success(postService.deletePost(postId));
    }


    // --- 以下是点赞、评论、通知的API ---

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
    @Log(title = "论坛-点赞", businessType = BusinessType.INSERT)
    @PostMapping("/like/{postId}")
    public AjaxResult likePost(@PathVariable Long postId) {
        Long userId = SecurityUtils.getUserId();
        String username = SecurityUtils.getUsername();
        return AjaxResult.success(likeService.likePost(postId, userId, username));
    }

    /**
     * 取消点赞
     */
    @Log(title = "论坛-取消点赞", businessType = BusinessType.DELETE)
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
    @Log(title = "论坛-评论", businessType = BusinessType.INSERT)
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