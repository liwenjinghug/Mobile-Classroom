package com.ruoyi.proj_qhy.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 论坛通知（非数据库表，用于前端展示）
 */
public class ForumNotice {
    private Long noticeId;         // 通知ID（自增）
    private Long postId;           // 关联帖子ID
    private Long operatorUserId;   // 操作用户ID
    private String operatorNickName; // 操作用户昵称
    private String operatorAvatar; // 操作用户头像
    private Integer noticeType;    // 通知类型（1点赞 2评论）
    private String commentContent; // 评论内容（仅评论通知有值）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // <--- 添加这一行
    private Date createTime;       // 通知时间

    // 手动实现getter/setter
    public Long getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(Long noticeId) {
        this.noticeId = noticeId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getOperatorUserId() {
        return operatorUserId;
    }

    public void setOperatorUserId(Long operatorUserId) {
        this.operatorUserId = operatorUserId;
    }

    public String getOperatorNickName() {
        return operatorNickName;
    }

    public void setOperatorNickName(String operatorNickName) {
        this.operatorNickName = operatorNickName;
    }

    public String getOperatorAvatar() {
        return operatorAvatar;
    }

    public void setOperatorAvatar(String operatorAvatar) {
        this.operatorAvatar = operatorAvatar;
    }

    public Integer getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(Integer noticeType) {
        this.noticeType = noticeType;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}