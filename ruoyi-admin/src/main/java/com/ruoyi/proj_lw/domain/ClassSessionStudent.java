package com.ruoyi.proj_lw.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import java.util.Date;
import java.io.Serializable;

/**
 * 课堂学生关联对象 class_session_student
 */
public class ClassSessionStudent {
    private static final long serialVersionUID = 1L;

    /** 课堂ID */
    private Long sessionId;

    /** 学生ID */
    private Long studentId;

    /** 分配时间 */
    private Date assignedAt;

    // 关联字段
    /** 学生姓名 */
    private String studentName;

    /** 学号 */
    private String studentNo;

    /** 课堂名称 */
    private String className;

    /** 教师 */
    private String teacher;

    /** 星期几 */
    private String weekDay;

    /** 开始时间 */
    private String startTime;

    /** 结束时间 */
    private String endTime;

    /** 课时长 */
    private Integer classDuration;

    /** 状态 */
    private Integer status;

    // getter 和 setter 方法
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Date getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Date assignedAt) { this.assignedAt = assignedAt; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }

    public String getWeekDay() { return weekDay; }
    public void setWeekDay(String weekDay) { this.weekDay = weekDay; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public Integer getClassDuration() { return classDuration; }
    public void setClassDuration(Integer classDuration) { this.classDuration = classDuration; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}