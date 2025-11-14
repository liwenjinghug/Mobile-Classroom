// Java
package com.ruoyi.proj_cyq.domain;

import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 密码重置令牌表 class_password_reset
 */
public class ClassPasswordReset extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String email;
    private String token;
    private Date expireTime;
    private Integer usedFlag;

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }
    public Integer getUsedFlag() { return usedFlag; }
    public void setUsedFlag(Integer usedFlag) { this.usedFlag = usedFlag; }
}