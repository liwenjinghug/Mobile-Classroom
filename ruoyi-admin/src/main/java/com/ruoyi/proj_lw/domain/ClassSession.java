package com.ruoyi.proj_lw.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

public class ClassSession extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long sessionId;
    private String className;
    private Long teacherId;

    // 修改为 String 类型
    // @JsonFormat(pattern = "HH:mm")
    private String startTime;

    // @JsonFormat(pattern = "HH:mm")
    private String endTime;

    private String weekDay;
    private Integer classDuration;
    private Integer status;
    private Integer totalStudents;
    private String teacher;
    private Long courseId;
    private Integer classNumber;

    // getter 和 setter 方法
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    // 修改 getter/setter 为 String 类型
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getWeekDay() { return weekDay; }
    public void setWeekDay(String weekDay) { this.weekDay = weekDay; }

    public Integer getClassDuration() { return classDuration; }
    public void setClassDuration(Integer classDuration) { this.classDuration = classDuration; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getTotalStudents() { return totalStudents; }
    public void setTotalStudents(Integer totalStudents) { this.totalStudents = totalStudents; }

    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Integer getClassNumber() { return classNumber; }
    public void setClassNumber(Integer classNumber) { this.classNumber = classNumber; }
}