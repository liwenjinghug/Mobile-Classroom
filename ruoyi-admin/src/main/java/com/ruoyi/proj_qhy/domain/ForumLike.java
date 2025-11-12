package com.ruoyi.proj_qhy.domain;

import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 论坛点赞表（class_forum_like）
 */
public class ForumLike extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 点赞ID */
    private Long likeId;

    /** 帖子ID */
    private Long postId;

    /** 点赞用户ID */
    private Long userId;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    // 非数据库字段，用于前端展示
    private String nickName; // 点赞用户昵称

    // 手动实现getter/setter
    public Long getLikeId() {
        return likeId;
    }

    public void setLikeId(Long likeId) {
        this.likeId = likeId;
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