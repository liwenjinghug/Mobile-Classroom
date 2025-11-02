package com.ruoyi.proj_cyq.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

public class Todo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Excel(name = "待办ID")
    private Long todoId;

    @Excel(name = "用户ID")
    private Long userId;

    @Excel(name = "连续编号")
    private Integer sequenceNumber;

    @Excel(name = "待办标题")
    private String title;

    @Excel(name = "待办内容")
    private String content;

    @Excel(name = "待办类型")
    private String todoType;

    @Excel(name = "优先级")
    private String priority;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "结束时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @Excel(name = "状态")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "提醒时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date remindTime;

    @Excel(name = "是否已提醒")
    private String isReminded;

    @Excel(name = "消息状态")
    private String messageStatus;

    @Excel(name = "消息是否已读")
    private String messageRead;

    // getter 和 setter
    public void setTodoId(Long todoId) { this.todoId = todoId; }
    public Long getTodoId() { return todoId; }

    public void setUserId(Long userId) { this.userId = userId; }
    public Long getUserId() { return userId; }

    public void setSequenceNumber(Integer sequenceNumber) { this.sequenceNumber = sequenceNumber; }
    public Integer getSequenceNumber() { return sequenceNumber; }

    public void setTitle(String title) { this.title = title; }
    public String getTitle() { return title; }

    public void setContent(String content) { this.content = content; }
    public String getContent() { return content; }

    public void setTodoType(String todoType) { this.todoType = todoType; }
    public String getTodoType() { return todoType; }

    public void setPriority(String priority) { this.priority = priority; }
    public String getPriority() { return priority; }

    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getStartTime() { return startTime; }

    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public Date getEndTime() { return endTime; }

    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }

    public void setRemindTime(Date remindTime) { this.remindTime = remindTime; }
    public Date getRemindTime() { return remindTime; }

    public void setIsReminded(String isReminded) { this.isReminded = isReminded; }
    public String getIsReminded() { return isReminded; }

    public void setMessageStatus(String messageStatus) { this.messageStatus = messageStatus; }
    public String getMessageStatus() { return messageStatus; }

    public void setMessageRead(String messageRead) { this.messageRead = messageRead; }
    public String getMessageRead() { return messageRead; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("todoId", getTodoId())
                .append("userId", getUserId())
                .append("sequenceNumber", getSequenceNumber())
                .append("title", getTitle())
                .append("content", getContent())
                .append("todoType", getTodoType())
                .append("priority", getPriority())
                .append("startTime", getStartTime())
                .append("endTime", getEndTime())
                .append("status", getStatus())
                .append("remindTime", getRemindTime())
                .append("isReminded", getIsReminded())
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