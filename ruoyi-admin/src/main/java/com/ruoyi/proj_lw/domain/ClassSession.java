package com.ruoyi.proj_lw.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

public class ClassSession extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long sessionId;
    private String className;
    private Long teacherId;

    @JsonFormat(pattern = "HH:mm")
    private String startTime;  // 修改为String类型，只存储时间

    @JsonFormat(pattern = "HH:mm")
    private String endTime;    // 修改为String类型，只存储时间

    private String weekDay;    // 新增：星期几（1-7）
    private Integer classDuration; // 新增：每堂课时长（分钟）

    private Integer status;
    private Integer totalStudents;
    private String teacher;
    private Long courseId;
    private Integer classNumber;  // 添加这个字段定义

    // getter 和 setter 方法
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

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

    public Integer getClassNumber() { return classNumber; }  // 添加getter
    public void setClassNumber(Integer classNumber) { this.classNumber = classNumber; }  // 添加setter
}