package com.ruoyi.proj_lwj.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.math.BigDecimal;

public class ClassStudentHomework extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long studentHomeworkId;
    private Long homeworkId;
    private Long studentId;
    private String submissionFiles; // 存储学生上传文件路径或名称，逗号分隔

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    // DB column `grade` is DECIMAL(5,2) — use BigDecimal in Java to preserve precision
    private BigDecimal score;
    private Integer status; // 0=未提交 1=已提交 2=已批改
    // DB 列 `grade_comment` 对应此属性（批改评语）
    // 注意：数据库列名为 grade_comment，Mapper 中以 column = "grade_comment" 映射到此属性 remark
    private String remark;

    // 新增字段：学生姓名（或学号显示用字段）
    private String studentName;

    // 新增字段：学号（student_no），供前端填写后端转换为 student_id
    private String studentNo;

    // 新增字段：作业标题，供提交记录显示
    private String homeworkTitle;

    // 新增的批改/统计字段
    /** 是否已批改：0-未批改 1-已批改 */
    private Integer isGraded;
    /** 批改教师ID */
    private Long correctedBy;
    /** 批改时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date correctedTime;
    /** 批改附件（批注文件等） */
    private String gradeAttachment;
    /** 评分细则 JSON 字符串 */
    private String rubricScores;
    /** 统计的字数 */
    private Integer wordCount;

    /** 非持久化字段：所关联作业是否已被删除（true = 已删除） */
    private Boolean homeworkDeleted;

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

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }

    public String getHomeworkTitle() { return homeworkTitle; }
    public void setHomeworkTitle(String homeworkTitle) { this.homeworkTitle = homeworkTitle; }

    public Integer getIsGraded() { return isGraded; }
    public void setIsGraded(Integer isGraded) { this.isGraded = isGraded; }

    public Long getCorrectedBy() { return correctedBy; }
    public void setCorrectedBy(Long correctedBy) { this.correctedBy = correctedBy; }

    public Date getCorrectedTime() { return correctedTime; }
    public void setCorrectedTime(Date correctedTime) { this.correctedTime = correctedTime; }

    public String getGradeAttachment() { return gradeAttachment; }
    public void setGradeAttachment(String gradeAttachment) { this.gradeAttachment = gradeAttachment; }

    public String getRubricScores() { return rubricScores; }
    public void setRubricScores(String rubricScores) { this.rubricScores = rubricScores; }

    public Integer getWordCount() { return wordCount; }
    public void setWordCount(Integer wordCount) { this.wordCount = wordCount; }

    public Boolean getHomeworkDeleted() { return homeworkDeleted; }
    public void setHomeworkDeleted(Boolean homeworkDeleted) { this.homeworkDeleted = homeworkDeleted; }
}
