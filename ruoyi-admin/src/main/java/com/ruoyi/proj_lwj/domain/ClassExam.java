package com.ruoyi.proj_lwj.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;

public class ClassExam extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String examName;
    private Integer examType;
    private Long courseId;
    private Long sessionId;
    private BigDecimal totalScore;
    private BigDecimal passScore;
    private Integer examDuration;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private Integer examMode;
    private Integer antiCheat;
    private Integer questionOrder;
    private Integer showAnswer;
    private Integer status;
    private Integer lateSubmit;
    private Integer lateTime;
    private Integer autoSubmit;
    private Integer studentCount;
    private Integer questionCount;
    private String courseName;
    private String className;

    // 【新增】消息是否已读 (只用于消息中心展示，非数据库字段)
    private String messageRead;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }
    public Integer getExamType() { return examType; }
    public void setExamType(Integer examType) { this.examType = examType; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public BigDecimal getTotalScore() { return totalScore; }
    public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }
    public BigDecimal getPassScore() { return passScore; }
    public void setPassScore(BigDecimal passScore) { this.passScore = passScore; }
    public Integer getExamDuration() { return examDuration; }
    public void setExamDuration(Integer examDuration) { this.examDuration = examDuration; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public Integer getExamMode() { return examMode; }
    public void setExamMode(Integer examMode) { this.examMode = examMode; }
    public Integer getAntiCheat() { return antiCheat; }
    public void setAntiCheat(Integer antiCheat) { this.antiCheat = antiCheat; }
    public Integer getQuestionOrder() { return questionOrder; }
    public void setQuestionOrder(Integer questionOrder) { this.questionOrder = questionOrder; }
    public Integer getShowAnswer() { return showAnswer; }
    public void setShowAnswer(Integer showAnswer) { this.showAnswer = showAnswer; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getLateSubmit() { return lateSubmit; }
    public void setLateSubmit(Integer lateSubmit) { this.lateSubmit = lateSubmit; }
    public Integer getLateTime() { return lateTime; }
    public void setLateTime(Integer lateTime) { this.lateTime = lateTime; }
    public Integer getAutoSubmit() { return autoSubmit; }
    public void setAutoSubmit(Integer autoSubmit) { this.autoSubmit = autoSubmit; }
    public Integer getStudentCount() { return studentCount; }
    public void setStudentCount(Integer studentCount) { this.studentCount = studentCount; }
    public Integer getQuestionCount() { return questionCount; }
    public void setQuestionCount(Integer questionCount) { this.questionCount = questionCount; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    // 【新增】Getter/Setter
    public String getMessageRead() { return messageRead; }
    public void setMessageRead(String messageRead) { this.messageRead = messageRead; }
}