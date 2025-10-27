package com.ruoyi.proj_myx.domain;

import java.io.Serializable;
import java.util.Date;

public class RandomPickRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long rpickId;
    private Long sessionId;
    private Long teacherId;
    private Long studentId;
    private Date pickTime;
    private Integer roundNo;
    private String remark;

    // 联表字段
    private String studentName;
    private String studentNo;

    public Long getRpickId() {
        return rpickId;
    }

    public void setRpickId(Long rpickId) {
        this.rpickId = rpickId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Date getPickTime() {
        return pickTime;
    }

    public void setPickTime(Date pickTime) {
        this.pickTime = pickTime;
    }

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }
}
