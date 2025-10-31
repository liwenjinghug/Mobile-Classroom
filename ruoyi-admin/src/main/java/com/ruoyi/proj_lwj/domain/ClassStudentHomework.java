package com.ruoyi.proj_lwj.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

public class ClassStudentHomework extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long studentHomeworkId;
    private Long homeworkId;
    private Long studentId;
    private String submissionFiles; // 存储学生上传文件路径或名称，逗号分隔

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    private Integer score;
    private Integer status; // 0=未提交 1=已提交 2=已批改
    private String remark;

    // 新增字段：学生姓名（或学号显示用字段）
    private String studentName;

    public Long getStudentHomeworkId() { return studentHomeworkId; }
    public void setStudentHomeworkId(Long studentHomeworkId) { this.studentHomeworkId = studentHomeworkId; }

    public Long getHomeworkId() { return homeworkId; }
    public void setHomeworkId(Long homeworkId) { this.homeworkId = homeworkId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getSubmissionFiles() { return submissionFiles; }
    public void setSubmissionFiles(String submissionFiles) { this.submissionFiles = submissionFiles; }

    public Date getSubmitTime() { return submitTime; }
    public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
}
