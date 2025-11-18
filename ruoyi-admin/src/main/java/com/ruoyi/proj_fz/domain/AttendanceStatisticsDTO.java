package com.ruoyi.proj_fz.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class AttendanceStatisticsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // 基础统计字段
    private Long sessionId;
    private String className;
    private Integer totalStudents;
    private Integer signedCount;
    private Integer absentCount;
    private Integer lateCount;
    private Integer leaveCount;
    private Integer earlyLeaveCount;
    private Double attendanceRate;

    // 时间维度统计
    private Date statDate;
    private String statWeek;
    private Integer dailySigned;
    private Integer dailyAbsent;
    private Integer dailyLate;
    private Integer dailyLeave;
    private Integer dailyEarlyLeave;

    // 明细字段
    private String studentName;
    private String studentNo;
    private Date attendanceTime;
    private String attendanceMethod;
    private Integer attendanceStatus;
    private String statusText;

    // 周报字段
    private String weekRange;
    private Double avgAttendanceRate;
    private Integer absenceRank;

    // 驾驶舱指标
    private Double schoolAvgRate;
    private Integer absenceCount;

    // 用于图表的数据
    private List<String> dateList;
    private List<Double> rateList;
    private List<Integer> signedList;
    private List<Integer> absentList;

    // getter和setter方法
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public Integer getTotalStudents() { return totalStudents; }
    public void setTotalStudents(Integer totalStudents) { this.totalStudents = totalStudents; }

    public Integer getSignedCount() { return signedCount; }
    public void setSignedCount(Integer signedCount) { this.signedCount = signedCount; }

    public Integer getAbsentCount() { return absentCount; }
    public void setAbsentCount(Integer absentCount) { this.absentCount = absentCount; }

    public Integer getLateCount() { return lateCount; }
    public void setLateCount(Integer lateCount) { this.lateCount = lateCount; }

    public Integer getLeaveCount() { return leaveCount; }
    public void setLeaveCount(Integer leaveCount) { this.leaveCount = leaveCount; }

    public Integer getEarlyLeaveCount() { return earlyLeaveCount; }
    public void setEarlyLeaveCount(Integer earlyLeaveCount) { this.earlyLeaveCount = earlyLeaveCount; }

    public Double getAttendanceRate() { return attendanceRate; }
    public void setAttendanceRate(Double attendanceRate) { this.attendanceRate = attendanceRate; }

    public Date getStatDate() { return statDate; }
    public void setStatDate(Date statDate) { this.statDate = statDate; }

    public String getStatWeek() { return statWeek; }
    public void setStatWeek(String statWeek) { this.statWeek = statWeek; }

    public Integer getDailySigned() { return dailySigned; }
    public void setDailySigned(Integer dailySigned) { this.dailySigned = dailySigned; }

    public Integer getDailyAbsent() { return dailyAbsent; }
    public void setDailyAbsent(Integer dailyAbsent) { this.dailyAbsent = dailyAbsent; }

    public Integer getDailyLate() { return dailyLate; }
    public void setDailyLate(Integer dailyLate) { this.dailyLate = dailyLate; }

    public Integer getDailyLeave() { return dailyLeave; }
    public void setDailyLeave(Integer dailyLeave) { this.dailyLeave = dailyLeave; }

    public Integer getDailyEarlyLeave() { return dailyEarlyLeave; }
    public void setDailyEarlyLeave(Integer dailyEarlyLeave) { this.dailyEarlyLeave = dailyEarlyLeave; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }

    public Date getAttendanceTime() { return attendanceTime; }
    public void setAttendanceTime(Date attendanceTime) { this.attendanceTime = attendanceTime; }

    public String getAttendanceMethod() { return attendanceMethod; }
    public void setAttendanceMethod(String attendanceMethod) { this.attendanceMethod = attendanceMethod; }

    public Integer getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(Integer attendanceStatus) { this.attendanceStatus = attendanceStatus; }

    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }

    public String getWeekRange() { return weekRange; }
    public void setWeekRange(String weekRange) { this.weekRange = weekRange; }

    public Double getAvgAttendanceRate() { return avgAttendanceRate; }
    public void setAvgAttendanceRate(Double avgAttendanceRate) { this.avgAttendanceRate = avgAttendanceRate; }

    public Integer getAbsenceRank() { return absenceRank; }
    public void setAbsenceRank(Integer absenceRank) { this.absenceRank = absenceRank; }

    public Double getSchoolAvgRate() { return schoolAvgRate; }
    public void setSchoolAvgRate(Double schoolAvgRate) { this.schoolAvgRate = schoolAvgRate; }

    public Integer getAbsenceCount() { return absenceCount; }
    public void setAbsenceCount(Integer absenceCount) { this.absenceCount = absenceCount; }

    public List<String> getDateList() { return dateList; }
    public void setDateList(List<String> dateList) { this.dateList = dateList; }

    public List<Double> getRateList() { return rateList; }
    public void setRateList(List<Double> rateList) { this.rateList = rateList; }

    public List<Integer> getSignedList() { return signedList; }
    public void setSignedList(List<Integer> signedList) { this.signedList = signedList; }

    public List<Integer> getAbsentList() { return absentList; }
    public void setAbsentList(List<Integer> absentList) { this.absentList = absentList; }
}