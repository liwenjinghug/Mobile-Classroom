package com.ruoyi.proj_myx.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 投票任务实体
 */
public class Vote implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long voteId;
    private Long sessionId;
    private String title;
    private String type; // 1 single, 2 multiple
    private String status; // 0 not started, 1 in progress, 2 ended
    private String isAnonymous; // 0 no, 1 yes
    private Date startTime;
    private Date endTime;
    private String createBy;
    private Date createTime;
    private Date updateTime;

    private List<VoteOption> options; // 选项列表

    public Long getVoteId() { return voteId; }
    public void setVoteId(Long voteId) { this.voteId = voteId; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getIsAnonymous() { return isAnonymous; }
    public void setIsAnonymous(String isAnonymous) { this.isAnonymous = isAnonymous; }

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    public List<VoteOption> getOptions() { return options; }
    public void setOptions(List<VoteOption> options) { this.options = options; }
}
