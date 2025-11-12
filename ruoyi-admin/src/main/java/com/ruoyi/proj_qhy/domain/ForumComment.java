package com.ruoyi.proj_qhy.domain;

import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 论坛评论表（class_forum_comment）
 */
public class ForumComment extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 评论ID */
    private Long commentId;

    /** 帖子ID */
    private Long postId;

    /** 评论用户ID */
    private Long userId;

    /** 父评论ID（0表示顶级评论） */
    private Long parentId;

    /** 回复目标用户ID */
    private Long replyToUserId;

    /** 评论内容 */
    private String content;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    // 非数据库字段，用于前端展示
    private String nickName;       // 评论用户昵称
    private String replyToNickName; // 回复目标用户昵称

    // 手动实现getter/setter
    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getReplyToUserId() {
        return replyToUserId;
    }

    public void setReplyToUserId(Long replyToUserId) {
        this.replyToUserId = replyToUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getReplyToNickName() {
        return replyToNickName;
    }

    public void setReplyToNickName(String replyToNickName) {
        this.replyToNickName = replyToNickName;
    }

    @Override
    public String getCreateBy() {
        return super.getCreateBy();
    }

    @Override
    public void setCreateBy(String createBy) {
        super.setCreateBy(createBy);
    }

    @Override
    public Date getCreateTime() {
        return super.getCreateTime();
    }

    @Override
    public void setCreateTime(Date createTime) {
        super.setCreateTime(createTime);
    }
}