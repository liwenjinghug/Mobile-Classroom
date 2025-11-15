package com.ruoyi.proj_lwj.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 考试基本信息实体
 */
public class ClassExam extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id; // exam id
    private String examName;
    private Integer examType; // 1期中 2期末 3测验 4模拟考 5课堂测验
    private Long courseId;
    private Long sessionId; // 可选：针对单一课堂，如发布到多个课堂后可拆分，此处先保留一个
    private BigDecimal totalScore; // 总分
    private BigDecimal passScore; // 及格分
    private Integer examDuration; // 时长(分钟)

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private Integer examMode; // 1定时考试 2随到随考
    private Integer antiCheat; // 0关闭 1开启
    private Integer questionOrder; // 0正常 1随机
    private Integer showAnswer; // 0不显示 1立即显示 2结束后
    private Integer status; // 0草稿 1已发布 2进行中 3已结束

    // 扩展配置字段
    private Integer lateSubmit; // 允许迟交(0否 1是)
    private Integer lateTime; // 迟交时间(分钟)
    private Integer autoSubmit; // 自动交卷(0否 1是)
    private Integer studentCount; // 参与学生数(缓存统计)
    private Integer questionCount; // 题目数量(缓存统计)

    // 展示用字段（关联课程/课堂名称）
    private String courseName;
    private String className;

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
}

