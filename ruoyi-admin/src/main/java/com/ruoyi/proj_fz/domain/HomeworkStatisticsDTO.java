package com.ruoyi.proj_fz.domain;

import java.util.List;
import java.util.Map;

public class HomeworkStatisticsDTO {
    private Long homeworkId;
    private String homeworkTitle;
    private String courseName;
    private String className;

    // 基础统计
    private Integer totalStudents;
    private Integer submittedCount;
    private Integer notSubmittedCount;
    private Integer overdueCount;
    private Integer gradedCount;

    // 比例
    private Double submissionRate;
    private Double overdueRate;
    private Double gradingRate;

    // 成绩分布
    private Map<String, Integer> scoreDistribution;
    private Double averageScore;
    private Double totalScore;
    private Double maxScore;
    private Double minScore;

    // 时间信息
    private String deadline;

    // 图表数据
    private List<Map<String, Object>> chartData;

    // 新增字段
    private String createBy;

    // getters and setters
    public Long getHomeworkId() {
        return homeworkId;
    }
    public void setHomeworkId(Long homeworkId) {
        this.homeworkId = homeworkId;
    }

    public String getHomeworkTitle() {
        return homeworkTitle;
    }
    public void setHomeworkTitle(String homeworkTitle) {
        this.homeworkTitle = homeworkTitle;
    }

    public String getCourseName() {
        return courseName;
    }
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getTotalStudents() {
        return totalStudents;
    }
    public void setTotalStudents(Integer totalStudents) {
        this.totalStudents = totalStudents;
    }

    public Integer getSubmittedCount() {
        return submittedCount;
    }
    public void setSubmittedCount(Integer submittedCount) {
        this.submittedCount = submittedCount;
    }

    public Integer getNotSubmittedCount() {
        return notSubmittedCount;
    }
    public void setNotSubmittedCount(Integer notSubmittedCount) {
        this.notSubmittedCount = notSubmittedCount;
    }

    public Integer getOverdueCount() {
        return overdueCount;
    }
    public void setOverdueCount(Integer overdueCount) {
        this.overdueCount = overdueCount;
    }

    public Integer getGradedCount() {
        return gradedCount;
    }
    public void setGradedCount(Integer gradedCount) {
        this.gradedCount = gradedCount;
    }

    public Double getSubmissionRate() {
        return submissionRate;
    }
    public void setSubmissionRate(Double submissionRate) {
        this.submissionRate = submissionRate;
    }

    public Double getOverdueRate() {
        return overdueRate;
    }
    public void setOverdueRate(Double overdueRate) {
        this.overdueRate = overdueRate;
    }

    public Double getGradingRate() {
        return gradingRate;
    }
    public void setGradingRate(Double gradingRate) {
        this.gradingRate = gradingRate;
    }

    public Map<String, Integer> getScoreDistribution() {
        return scoreDistribution;
    }
    public void setScoreDistribution(Map<String, Integer> scoreDistribution) {
        this.scoreDistribution = scoreDistribution;
    }

    public Double getAverageScore() {
        return averageScore;
    }
    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Double getTotalScore() {
        return totalScore;
    }
    public void setTotalScore(Double totalScore) {
        this.totalScore = totalScore;
    }

    public Double getMaxScore() {
        return maxScore;
    }
    public void setMaxScore(Double maxScore) {
        this.maxScore = maxScore;
    }

    public Double getMinScore() {
        return minScore;
    }
    public void setMinScore(Double minScore) {
        this.minScore = minScore;
    }

    public String getDeadline() {
        return deadline;
    }
    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public List<Map<String, Object>> getChartData() {
        return chartData;
    }
    public void setChartData(List<Map<String, Object>> chartData) {
        this.chartData = chartData;
    }

    public String getCreateBy() {
        return createBy;
    }
    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    // 在HomeworkStatisticsDTO类中添加以下字段和方法

    private String createTime;

    // getters and setters
    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}