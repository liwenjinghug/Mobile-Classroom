package com.ruoyi.proj_myx.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 投票记录实体
 */
public class VoteRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long recordId;
    private Long voteId;
    private Long studentId;
    private String studentName;
    private Long optionId;
    private Date voteTime;

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }

    public Long getVoteId() { return voteId; }
    public void setVoteId(Long voteId) { this.voteId = voteId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public Long getOptionId() { return optionId; }
    public void setOptionId(Long optionId) { this.optionId = optionId; }

    public Date getVoteTime() { return voteTime; }
    public void setVoteTime(Date voteTime) { this.voteTime = voteTime; }
}
