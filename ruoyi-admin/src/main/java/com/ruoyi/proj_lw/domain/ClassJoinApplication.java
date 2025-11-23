package com.ruoyi.proj_lw.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import java.util.Date;

/**
 * 课堂加入申请对象 class_join_application
 */
public class ClassJoinApplication {
    private static final long serialVersionUID = 1L;

    /** 申请ID */
    private Long applicationId;

    /** 课堂ID */
    @Excel(name = "课堂ID")
    private Long sessionId;

    /** 学生ID */
    @Excel(name = "学生ID")
    private Long studentId;

    /** 学号 */
    @Excel(name = "学号")
    private String studentNo;

    /** 学生姓名 */
    @Excel(name = "学生姓名")
    private String studentName;

    /** 状态（0待审核 1通过 2拒绝） */
    @Excel(name = "状态", readConverterExp = "0=待审核,1=通过,2=拒绝")
    private String status;

    /** 申请时间 */
    @Excel(name = "申请时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime;

    /** 审核时间 */
    @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审核人ID */
    @Excel(name = "审核人ID")
    private Long auditUserId;

    /** 备注 */
    @Excel(name = "备注")
    private String remark;

    // 关联字段
    /** 课堂名称 */
    @Excel(name = "课堂名称")
    private String className;

    /** 教师姓名 */
    @Excel(name = "教师姓名")
    private String teacherName;

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Date applyTime) {
        this.applyTime = applyTime;
    }

    public Date getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(Date auditTime) {
        this.auditTime = auditTime;
    }

    public Long getAuditUserId() {
        return auditUserId;
    }

    public void setAuditUserId(Long auditUserId) {
        this.auditUserId = auditUserId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("applicationId", getApplicationId())
                .append("sessionId", getSessionId())
                .append("studentId", getStudentId())
                .append("studentNo", getStudentNo())
                .append("studentName", getStudentName())
                .append("status", getStatus())
                .append("applyTime", getApplyTime())
                .append("auditTime", getAuditTime())
                .append("auditUserId", getAuditUserId())
                .append("remark", getRemark())
                .toString();
    }
}