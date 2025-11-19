package com.ruoyi.proj_fz.domain;

import java.util.List;
import java.util.Map;

public class DashboardDTO {

    private WeatherInfo weather;
    private CoreMetrics coreMetrics;
    private ChartData chartData;

    // Getters and Setters
    public WeatherInfo getWeather() { return weather; }
    public void setWeather(WeatherInfo weather) { this.weather = weather; }

    public CoreMetrics getCoreMetrics() { return coreMetrics; }
    public void setCoreMetrics(CoreMetrics coreMetrics) { this.coreMetrics = coreMetrics; }

    public ChartData getChartData() { return chartData; }
    public void setChartData(ChartData chartData) { this.chartData = chartData; }

    // 天气信息类
    public static class WeatherInfo {
        private String city;
        private String temperature;
        private String weather;
        private String humidity;
        private String wind;
        private List<WeatherForecast> forecast;

        // Getters and Setters
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getTemperature() { return temperature; }
        public void setTemperature(String temperature) { this.temperature = temperature; }
        public String getWeather() { return weather; }
        public void setWeather(String weather) { this.weather = weather; }
        public String getHumidity() { return humidity; }
        public void setHumidity(String humidity) { this.humidity = humidity; }
        public String getWind() { return wind; }
        public void setWind(String wind) { this.wind = wind; }
        public List<WeatherForecast> getForecast() { return forecast; }
        public void setForecast(List<WeatherForecast> forecast) { this.forecast = forecast; }
    }

    // 天气预报类
    public static class WeatherForecast {
        private String date;
        private String tempMin;
        private String tempMax;
        private String weather;

        // Getters and Setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getTempMin() { return tempMin; }
        public void setTempMin(String tempMin) { this.tempMin = tempMin; }
        public String getTempMax() { return tempMax; }
        public void setTempMax(String tempMax) { this.tempMax = tempMax; }
        public String getWeather() { return weather; }
        public void setWeather(String weather) { this.weather = weather; }
    }

    // 核心指标类
    public static class CoreMetrics {
        private int totalCourses;
        private int activeClasses;
        private int todayDeadline;
        private int ongoingExams;
        private int pendingTodos;
        private int gradedCount;
        private int gradingRate;
        private int courseTrend;
        private int classTrend;
        private int homeworkTrend;
        private int examTrend;
        private int todoTrend;
        private int gradingTrend;

        // Getters and Setters
        public int getTotalCourses() { return totalCourses; }
        public void setTotalCourses(int totalCourses) { this.totalCourses = totalCourses; }
        public int getActiveClasses() { return activeClasses; }
        public void setActiveClasses(int activeClasses) { this.activeClasses = activeClasses; }
        public int getTodayDeadline() { return todayDeadline; }
        public void setTodayDeadline(int todayDeadline) { this.todayDeadline = todayDeadline; }
        public int getOngoingExams() { return ongoingExams; }
        public void setOngoingExams(int ongoingExams) { this.ongoingExams = ongoingExams; }
        public int getPendingTodos() { return pendingTodos; }
        public void setPendingTodos(int pendingTodos) { this.pendingTodos = pendingTodos; }
        public int getGradedCount() { return gradedCount; }
        public void setGradedCount(int gradedCount) { this.gradedCount = gradedCount; }
        public int getGradingRate() { return gradingRate; }
        public void setGradingRate(int gradingRate) { this.gradingRate = gradingRate; }
        public int getCourseTrend() { return courseTrend; }
        public void setCourseTrend(int courseTrend) { this.courseTrend = courseTrend; }
        public int getClassTrend() { return classTrend; }
        public void setClassTrend(int classTrend) { this.classTrend = classTrend; }
        public int getHomeworkTrend() { return homeworkTrend; }
        public void setHomeworkTrend(int homeworkTrend) { this.homeworkTrend = homeworkTrend; }
        public int getExamTrend() { return examTrend; }
        public void setExamTrend(int examTrend) { this.examTrend = examTrend; }
        public int getTodoTrend() { return todoTrend; }
        public void setTodoTrend(int todoTrend) { this.todoTrend = todoTrend; }
        public int getGradingTrend() { return gradingTrend; }
        public void setGradingTrend(int gradingTrend) { this.gradingTrend = gradingTrend; }
    }

    // 图表数据类
    public static class ChartData {
        private List<TrendData> trendData;
        private SubmissionStatus submissionStatus;
        private ScoreStats scoreStats;
        private List<ScoreDistribution> scoreDistribution;
        private List<CourseScore> courseScores;
        private List<AttendanceRate> attendanceRates;
        private List<ParticipationHeat> participationHeat;

        // Getters and Setters
        public List<TrendData> getTrendData() { return trendData; }
        public void setTrendData(List<TrendData> trendData) { this.trendData = trendData; }
        public SubmissionStatus getSubmissionStatus() { return submissionStatus; }
        public void setSubmissionStatus(SubmissionStatus submissionStatus) { this.submissionStatus = submissionStatus; }
        public ScoreStats getScoreStats() { return scoreStats; }
        public void setScoreStats(ScoreStats scoreStats) { this.scoreStats = scoreStats; }
        public List<ScoreDistribution> getScoreDistribution() { return scoreDistribution; }
        public void setScoreDistribution(List<ScoreDistribution> scoreDistribution) { this.scoreDistribution = scoreDistribution; }
        public List<CourseScore> getCourseScores() { return courseScores; }
        public void setCourseScores(List<CourseScore> courseScores) { this.courseScores = courseScores; }
        public List<AttendanceRate> getAttendanceRates() { return attendanceRates; }
        public void setAttendanceRates(List<AttendanceRate> attendanceRates) { this.attendanceRates = attendanceRates; }
        public List<ParticipationHeat> getParticipationHeat() { return participationHeat; }
        public void setParticipationHeat(List<ParticipationHeat> participationHeat) { this.participationHeat = participationHeat; }
    }

    // 趋势数据类
    public static class TrendData {
        private String date;
        private int submissions;

        // Getters and Setters
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public int getSubmissions() { return submissions; }
        public void setSubmissions(int submissions) { this.submissions = submissions; }
    }

    // 提交状态类
    public static class SubmissionStatus {
        private int submitted;
        private int pending;
        private int graded;

        // Getters and Setters
        public int getSubmitted() { return submitted; }
        public void setSubmitted(int submitted) { this.submitted = submitted; }
        public int getPending() { return pending; }
        public void setPending(int pending) { this.pending = pending; }
        public int getGraded() { return graded; }
        public void setGraded(int graded) { this.graded = graded; }
    }

    // 成绩统计类
    public static class ScoreStats {
        private double average;
        private double max;
        private int passRate;

        // Getters and Setters
        public double getAverage() { return average; }
        public void setAverage(double average) { this.average = average; }
        public double getMax() { return max; }
        public void setMax(double max) { this.max = max; }
        public int getPassRate() { return passRate; }
        public void setPassRate(int passRate) { this.passRate = passRate; }
    }

    // 成绩分布类
    public static class ScoreDistribution {
        private String range;
        private int count;

        // Getters and Setters
        public String getRange() { return range; }
        public void setRange(String range) { this.range = range; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    // 课程平均分类
    public static class CourseScore {
        private String courseName;
        private double averageScore;

        // Getters and Setters
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public double getAverageScore() { return averageScore; }
        public void setAverageScore(double averageScore) { this.averageScore = averageScore; }
    }

    // 签到率类
    public static class AttendanceRate {
        private String className;
        private double attendanceRate;

        // Getters and Setters
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        public double getAttendanceRate() { return attendanceRate; }
        public void setAttendanceRate(double attendanceRate) { this.attendanceRate = attendanceRate; }
    }

    // 参与热力类
    public static class ParticipationHeat {
        private String studentName;
        private int homeworkCount;
        private int attendanceCount;
        private int forumCount;

        // Getters and Setters
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public int getHomeworkCount() { return homeworkCount; }
        public void setHomeworkCount(int homeworkCount) { this.homeworkCount = homeworkCount; }
        public int getAttendanceCount() { return attendanceCount; }
        public void setAttendanceCount(int attendanceCount) { this.attendanceCount = attendanceCount; }
        public int getForumCount() { return forumCount; }
        public void setForumCount(int forumCount) { this.forumCount = forumCount; }
    }
}