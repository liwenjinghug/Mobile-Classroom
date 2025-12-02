package com.ruoyi.proj_lwj.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.Date;

/** 题目实体 */
public class ClassExamQuestion extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id; // question id
    private Long examId;
    private Integer questionType; // 1=判断 2=选择 3=简答
    private String questionContent; // 题目内容
    private String questionOptions; // 选项 JSON
    private String correctAnswer; // 正确答案
    private String analysis; // 解析
    private BigDecimal score; // 分值
    private Integer difficulty; // 难度(1简单 2一般 3困难)
    private Integer sortOrder; // 排序序号
    private String subject; // 学科/分类

    // 快照扩展（当需要保留历史）暂时不使用单独表，在 answer 中复制

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public Integer getQuestionType() { return questionType; }
    public void setQuestionType(Integer questionType) { this.questionType = questionType; }
    public String getQuestionContent() { return questionContent; }
    public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }
    public String getQuestionOptions() { return questionOptions; }
    public void setQuestionOptions(String questionOptions) { this.questionOptions = questionOptions; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
}
