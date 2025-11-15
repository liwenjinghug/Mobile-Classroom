package com.ruoyi.proj_lwj.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.Date;

/** 学生答案记录 */
public class ClassExamAnswer extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id; // 答案ID
    private Long examId;
    private Long studentId;
    private String studentNo;
    private Long questionId;
    private String studentAnswer; // 学生答案
    private String answerFiles; // 附件文件(多个逗号分隔)
    private Integer isCorrect; // 是否正确(客观题)
    private BigDecimal score; // 得分
    private Long correctorId; // 批改人ID
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date correctTime; // 批改时间
    private String correctComment; // 批改评语
    private Integer answerDuration; // 耗时(秒)

    // 快照字段
    private String questionContent;
    private String questionOptions;
    private String correctAnswer; // 正确答案快照

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getStudentAnswer() { return studentAnswer; }
    public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }
    public String getAnswerFiles() { return answerFiles; }
    public void setAnswerFiles(String answerFiles) { this.answerFiles = answerFiles; }
    public Integer getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Integer isCorrect) { this.isCorrect = isCorrect; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public Long getCorrectorId() { return correctorId; }
    public void setCorrectorId(Long correctorId) { this.correctorId = correctorId; }
    public Date getCorrectTime() { return correctTime; }
    public void setCorrectTime(Date correctTime) { this.correctTime = correctTime; }
    public String getCorrectComment() { return correctComment; }
    public void setCorrectComment(String correctComment) { this.correctComment = correctComment; }
    public Integer getAnswerDuration() { return answerDuration; }
    public void setAnswerDuration(Integer answerDuration) { this.answerDuration = answerDuration; }
    public String getQuestionContent() { return questionContent; }
    public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }
    public String getQuestionOptions() { return questionOptions; }
    public void setQuestionOptions(String questionOptions) { this.questionOptions = questionOptions; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
}

