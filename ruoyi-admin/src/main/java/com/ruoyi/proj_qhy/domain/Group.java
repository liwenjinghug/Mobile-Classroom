package com.ruoyi.proj_qhy.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.List;

public class Group extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long groupId;
    private String groupName;
    private Long ownerUserId;
    private String groupNumber;
    private String avatar;
    private String qrCode;
    private Long latestMessageId;

    // 非数据库字段，用于前端展示
    private GroupMessage latestMessage; // 最新消息详情
    private List<GroupMember> members;  // 成员列表 (用于详情)
    private int unreadCount;            // 未读消息数
    private String ownerNickName;       // 创建者昵称
    private boolean isMember;           // (非数据库字段) 当前用户是否已加入
    private String memberStatus;        // (非数据库字段) 列表用: '0' 或 '2'
    private String currentUserStatus;   // (非数据库字段) 详情用: '0' 或 '2'

    // Getters and Setters
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getGroupNumber() { return groupNumber; }
    public void setGroupNumber(String groupNumber) { this.groupNumber = groupNumber; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public Long getLatestMessageId() { return latestMessageId; }
    public void setLatestMessageId(Long latestMessageId) { this.latestMessageId = latestMessageId; }
    public GroupMessage getLatestMessage() { return latestMessage; }
    public void setLatestMessage(GroupMessage latestMessage) { this.latestMessage = latestMessage; }
    public List<GroupMember> getMembers() { return members; }
    public void setMembers(List<GroupMember> members) { this.members = members; }
    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
    public String getOwnerNickName() { return ownerNickName; }
    public void setOwnerNickName(String ownerNickName) { this.ownerNickName = ownerNickName; }
    public boolean getIsMember() {
        return isMember;
    }
    public void setIsMember(boolean isMember) {
        this.isMember = isMember;
    }
    public String getMemberStatus() { return memberStatus; }
    public void setMemberStatus(String memberStatus) { this.memberStatus = memberStatus; }
    public String getCurrentUserStatus() { return currentUserStatus; }
    public void setCurrentUserStatus(String currentUserStatus) { this.currentUserStatus = currentUserStatus; }
}