package com.ruoyi.proj_cyq.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;

public class Homework extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Excel(name = "作业ID")
    private Long homeworkId;

    @Excel(name = "课程ID")
    private Long courseId;

    @Excel(name = "课堂ID")
    private Long sessionId;

    @Excel(name = "作业标题")
    private String title;

    @Excel(name = "作业内容")
    private String content;

    @Excel(name = "作业要求")
    private String requirement;

    @Excel(name = "作业总分")
    private BigDecimal totalScore;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "截止时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date deadline;

    @Excel(name = "附件路径")
    private String attachments;

    @Excel(name = "附件原名")
    private String attachmentNames;

    @Excel(name = "状态")
    private String status;

    @Excel(name = "消息状态")
    private String messageStatus;

    @Excel(name = "消息是否已读")
    private String messageRead;

    // getter 和 setter
    public void setHomeworkId(Long homeworkId) { this.homeworkId = homeworkId; }
    public Long getHomeworkId() { return homeworkId; }

    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getCourseId() { return courseId; }

    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public Long getSessionId() { return sessionId; }

    public void setTitle(String title) { this.title = title; }
    public String getTitle() { return title; }

    public void setContent(String content) { this.content = content; }
    public String getContent() { return content; }

    public void setRequirement(String requirement) { this.requirement = requirement; }
    public String getRequirement() { return requirement; }

    public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }
    public BigDecimal getTotalScore() { return totalScore; }

    public void setDeadline(Date deadline) { this.deadline = deadline; }
    public Date getDeadline() { return deadline; }

    public void setAttachments(String attachments) { this.attachments = attachments; }
    public String getAttachments() { return attachments; }

    public void setAttachmentNames(String attachmentNames) { this.attachmentNames = attachmentNames; }
    public String getAttachmentNames() { return attachmentNames; }

    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }

    public void setMessageStatus(String messageStatus) { this.messageStatus = messageStatus; }
    public String getMessageStatus() { return messageStatus; }

    public void setMessageRead(String messageRead) { this.messageRead = messageRead; }
    public String getMessageRead() { return messageRead; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("homeworkId", getHomeworkId())
                .append("courseId", getCourseId())
                .append("sessionId", getSessionId())
                .append("title", getTitle())
                .append("content", getContent())
                .append("requirement", getRequirement())
                .append("totalScore", getTotalScore())
                .append("deadline", getDeadline())
                .append("attachments", getAttachments())
                .append("attachmentNames", getAttachmentNames())
                .append("status", getStatus())
                .append("messageStatus", getMessageStatus())
                .append("messageRead", getMessageRead())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}