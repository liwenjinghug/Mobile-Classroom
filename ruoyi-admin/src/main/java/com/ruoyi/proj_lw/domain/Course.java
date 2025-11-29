package com.ruoyi.proj_lw.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.annotation.Excel;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Course extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 课程ID */
    @Excel(name = "课程ID")
    private Long courseId;

    /** 课程名称 */
    @Excel(name = "课程名称")
    private String courseName;

    /** 课程编号 */
    @Excel(name = "课程编号")
    private String courseCode;

    /** 课程类型 */
    @Excel(name = "课程类型")
    private String courseType;

    /** 所属学院 */
    @Excel(name = "所属学院")
    private String college;

    /** 学分 */
    @Excel(name = "学分")
    private Double credit;

    /** 课程简介 */
    @Excel(name = "课程简介")
    private String introduction;

    /** 状态（0正常 1停授） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停授")
    private String status;

    /** 班级数量 - 不导出此字段 */
    private Integer classNumber;

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

    public Integer getClassNumber() { return classNumber; }
    public void setClassNumber(Integer classNumber) { this.classNumber = classNumber; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("courseId", getCourseId())
                .append("courseName", getCourseName())
                .append("courseCode", getCourseCode())
                .append("courseType", getCourseType())
                .append("college", getCollege())
                .append("credit", getCredit())
                .append("introduction", getIntroduction())
                .append("status", getStatus())
                .append("classNumber", getClassNumber())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}