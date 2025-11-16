package com.ruoyi.proj_qhy.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class GroupMember extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long memberId;
    private Long groupId;
    private Long userId;
    private String status;
    private Long lastReadMessageId;

    // 非数据库字段
    private String nickName; // 成员昵称
    private String avatar;   // 成员头像

    // Getters and Setters
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getLastReadMessageId() { return lastReadMessageId; }
    public void setLastReadMessageId(Long lastReadMessageId) { this.lastReadMessageId = lastReadMessageId; }
    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}