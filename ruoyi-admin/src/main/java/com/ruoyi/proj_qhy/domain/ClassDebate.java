package com.ruoyi.proj_qhy.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;
import java.util.List;

public class ClassDebate extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String proViewpoint;
    private String conViewpoint;
    private String inviteCode;
    private String status; // 0:未开始, 1:进行中, 2:已结束
    private String winner;

    // 业务字段
    private boolean isJoined; // 当前用户是否已加入
    private String currentUserRole; // 当前用户身份
    private Long proCount; // 正方人数
    private Long conCount; // 反方人数
    private Long proVotes; // 正方票数
    private Long conVotes; // 反方票数

    // 新增字段
    private Integer speechLimit; // 秒
    private Integer currentTurn;
    private String currentRole;  // 1 或 2
    private Date turnStartTime;

    // 业务辅助字段 (用于前端展示选手列表)
    private List<String> proPlayerNames; // 正方选手名字列表
    private List<String> conPlayerNames; // 反方选手名字列表

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getProViewpoint() { return proViewpoint; }
    public void setProViewpoint(String proViewpoint) { this.proViewpoint = proViewpoint; }
    public String getConViewpoint() { return conViewpoint; }
    public void setConViewpoint(String conViewpoint) { this.conViewpoint = conViewpoint; }
    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }
    public boolean isJoined() { return isJoined; }
    public void setJoined(boolean joined) { isJoined = joined; }
    public String getCurrentUserRole() { return currentUserRole; }
    public void setCurrentUserRole(String currentUserRole) { this.currentUserRole = currentUserRole; }
    public Long getProCount() { return proCount; }
    public void setProCount(Long proCount) { this.proCount = proCount; }
    public Long getConCount() { return conCount; }
    public void setConCount(Long conCount) { this.conCount = conCount; }
    public Long getProVotes() { return proVotes; }
    public void setProVotes(Long proVotes) { this.proVotes = proVotes; }
    public Long getConVotes() { return conVotes; }
    public void setConVotes(Long conVotes) { this.conVotes = conVotes; }

    // Getters and Setters
    public Integer getSpeechLimit() { return speechLimit; }
    public void setSpeechLimit(Integer speechLimit) { this.speechLimit = speechLimit; }

    public Integer getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(Integer currentTurn) { this.currentTurn = currentTurn; }

    public String getCurrentRole() { return currentRole; }
    public void setCurrentRole(String currentRole) { this.currentRole = currentRole; }

    public Date getTurnStartTime() { return turnStartTime; }
    public void setTurnStartTime(Date turnStartTime) { this.turnStartTime = turnStartTime; }

    public List<String> getProPlayerNames() { return proPlayerNames; }
    public void setProPlayerNames(List<String> proPlayerNames) { this.proPlayerNames = proPlayerNames; }

    public List<String> getConPlayerNames() { return conPlayerNames; }
    public void setConPlayerNames(List<String> conPlayerNames) { this.conPlayerNames = conPlayerNames; }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("title", title).toString();
    }
}