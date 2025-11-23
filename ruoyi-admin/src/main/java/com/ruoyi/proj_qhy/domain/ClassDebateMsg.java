package com.ruoyi.proj_qhy.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

public class ClassDebateMsg extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long debateId;
    private Long userId;
    private String nickName;
    private String role; // 1正 2反
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    // Getters Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDebateId() { return debateId; }
    public void setDebateId(Long debateId) { this.debateId = debateId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    @Override
    public Date getCreateTime() { return createTime; }
    @Override
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}