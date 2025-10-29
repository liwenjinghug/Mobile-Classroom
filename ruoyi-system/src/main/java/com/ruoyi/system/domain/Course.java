package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class Course extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long courseId;
    private String courseName;
    private String courseCode;
    private String courseType;
    private String college;
    private Double credit;
    private String introduction;
    private String status;

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseType() { return courseType; }
    public void setCourseType(String courseType) { this.courseType = courseType; }

    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }

    public Double getCredit() { return credit; }
    public void setCredit(Double credit) { this.credit = credit; }

    public String getIntroduction() { return introduction; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}