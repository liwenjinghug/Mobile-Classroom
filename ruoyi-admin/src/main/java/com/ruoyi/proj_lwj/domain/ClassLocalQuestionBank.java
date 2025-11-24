package com.ruoyi.proj_lwj.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;

/** 本地题库实体 */
public class ClassLocalQuestionBank extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 原始题型: 1-单选 2-判断 3-简答 (与考试题型映射: 1->1, 2->3, 3->5) */
    private Integer questionType;
    private String questionContent;
    private Integer difficulty; // 1简单 2中等 3困难
    private String category; // 分类
    private String optionsJson; // 选项 JSON 数组
    private String correctAnswer; // 正确答案
    private String analysis; // 解析
    private BigDecimal score; // 默认分值
    private String tags; // 标签(逗号分隔)
    private Integer status; // 1-启用 0-禁用

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getQuestionType() { return questionType; }
    public void setQuestionType(Integer questionType) { this.questionType = questionType; }
    public String getQuestionContent() { return questionContent; }
    public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }
    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getOptionsJson() { return optionsJson; }
    public void setOptionsJson(String optionsJson) { this.optionsJson = optionsJson; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}

