package com.ruoyi.proj_qhy.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class GroupMessage extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long messageId;
    private Long groupId;
    private Long senderUserId;
    private String messageType; // 0=文本, 1=图片, 9=系统
    private String content;

    // 非数据库字段
    private String senderNickName;
    private String senderAvatar;

    // Getters and Setters
    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public Long getSenderUserId() { return senderUserId; }
    public void setSenderUserId(Long senderUserId) { this.senderUserId = senderUserId; }
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSenderNickName() { return senderNickName; }
    public void setSenderNickName(String senderNickName) { this.senderNickName = senderNickName; }
    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }
}