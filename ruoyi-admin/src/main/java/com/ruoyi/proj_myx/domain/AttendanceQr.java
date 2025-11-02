package com.ruoyi.proj_myx.domain;

import java.io.Serializable;
import java.util.Date;

public class AttendanceQr implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long qrId;
    private Long taskId;
    private String token;
    private Integer ttlSeconds;
    private Date expireTime;
    private Integer used;
    private String createBy;
    private Date createTime;

    public Long getQrId() { return qrId; }
    public void setQrId(Long qrId) { this.qrId = qrId; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Integer getTtlSeconds() { return ttlSeconds; }
    public void setTtlSeconds(Integer ttlSeconds) { this.ttlSeconds = ttlSeconds; }
    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }
    public Integer getUsed() { return used; }
    public void setUsed(Integer used) { this.used = used; }
    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}

