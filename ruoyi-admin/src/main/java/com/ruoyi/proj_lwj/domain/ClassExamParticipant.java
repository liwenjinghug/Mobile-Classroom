package com.ruoyi.proj_lwj.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.Date;

/** 考试参与记录 */
public class ClassExamParticipant extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id; // 记录ID
    private Long examId;
    private String examName; // 新增：用于 MyBatis 查询结果映射（考试名称）
    private String courseName; // 新增：用于 MyBatis 查询结果映射（课程名称）
    private Long studentId;
    private String studentNo;
    private String studentName;
    private Integer participantStatus; // 0未开始 1进行中 2已完成 3缺考
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;
    private String ipAddress;
    private String deviceInfo;
    private BigDecimal totalScore; // 总得分
    private Integer timeUsed; // 用时(秒)
    private BigDecimal objectiveScore; // 客观题得分
    private BigDecimal subjectiveScore; // 主观题得分
    private Integer correctStatus; // 批改状态(0未批改 1已批改)
    private Integer passStatus; // 及格状态(0不及格 1及格)
    // 新增: 文字描述（不持久化）
    private transient String passStatusLabel;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public Integer getParticipantStatus() { return participantStatus; }
    public void setParticipantStatus(Integer participantStatus) { this.participantStatus = participantStatus; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getSubmitTime() { return submitTime; }
    public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
    public BigDecimal getTotalScore() { return totalScore; }
    public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }
    public Integer getTimeUsed() { return timeUsed; }
    public void setTimeUsed(Integer timeUsed) { this.timeUsed = timeUsed; }
    public BigDecimal getObjectiveScore() { return objectiveScore; }
    public void setObjectiveScore(BigDecimal objectiveScore) { this.objectiveScore = objectiveScore; }
    public BigDecimal getSubjectiveScore() { return subjectiveScore; }
    public void setSubjectiveScore(BigDecimal subjectiveScore) { this.subjectiveScore = subjectiveScore; }
    public Integer getCorrectStatus() { return correctStatus; }
    public void setCorrectStatus(Integer correctStatus) { this.correctStatus = correctStatus; }
    public Integer getPassStatus() { return passStatus; }
    public void setPassStatus(Integer passStatus) { this.passStatus = passStatus; }

    /** 及格状态文字描述 */
    public String getPassStatusLabel() {
        if (passStatus == null) return "未知";
        switch (passStatus) {
            case 0: return "不及格";
            case 1: return "及格";
            default: return "未知";
        }
    }
}
