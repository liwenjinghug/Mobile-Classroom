package com.ruoyi.proj_fz.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.proj_fz.domain.AttendanceStatisticsDTO;
import com.ruoyi.proj_fz.mapper.AttendanceStatisticsMapper;
import com.ruoyi.proj_fz.service.IAttendanceStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AttendanceStatisticsServiceImpl implements IAttendanceStatisticsService {

    @Autowired
    private AttendanceStatisticsMapper attendanceStatisticsMapper;

    @Override
    public List<AttendanceStatisticsDTO> getSessionStatistics(Long sessionId, Date startDate, Date endDate) {
        // 如果没有日期范围，设置默认范围
        if (startDate == null) {
            startDate = DateUtils.addDays(new Date(), -30);
        }
        if (endDate == null) {
            endDate = new Date();
        }
        return attendanceStatisticsMapper.selectSessionStatistics(sessionId, startDate, endDate);
    }

    @Override
    public Map<String, Object> getTimeStatistics(Long sessionId, Date startDate, Date endDate, String groupBy) {
        // 如果没有日期范围，设置默认范围
        if (startDate == null) {
            startDate = DateUtils.addDays(new Date(), -7);
        }
        if (endDate == null) {
            endDate = new Date();
        }

        List<AttendanceStatisticsDTO> statistics = attendanceStatisticsMapper.selectTimeStatistics(sessionId, startDate, endDate, groupBy);

        Map<String, Object> result = new HashMap<>();
        List<String> dateList = new ArrayList<>();
        List<Double> rateList = new ArrayList<>();
        List<Integer> signedList = new ArrayList<>();
        List<Integer> absentList = new ArrayList<>();
        List<Integer> lateList = new ArrayList<>();
        List<Integer> leaveList = new ArrayList<>();
        List<Integer> earlyLeaveList = new ArrayList<>();

        for (AttendanceStatisticsDTO stat : statistics) {
            dateList.add(DateUtils.parseDateToStr("MM-dd HH:mm", stat.getStatDate()));
            rateList.add(stat.getAttendanceRate() != null ? stat.getAttendanceRate() : 0.0);
            signedList.add(stat.getDailySigned() != null ? stat.getDailySigned() : 0);
            absentList.add(stat.getDailyAbsent() != null ? stat.getDailyAbsent() : 0);
            lateList.add(stat.getDailyLate() != null ? stat.getDailyLate() : 0);
            leaveList.add(stat.getDailyLeave() != null ? stat.getDailyLeave() : 0);
            earlyLeaveList.add(stat.getDailyEarlyLeave() != null ? stat.getDailyEarlyLeave() : 0);
        }

        result.put("dateList", dateList);
        result.put("rateList", rateList);
        result.put("signedList", signedList);
        result.put("absentList", absentList);
        result.put("lateList", lateList);
        result.put("leaveList", leaveList);
        result.put("earlyLeaveList", earlyLeaveList);
        result.put("statistics", statistics);

        return result;
    }

    @Override
    public List<AttendanceStatisticsDTO> getAttendanceDetails(Long sessionId, Date startDate, Date endDate, Integer attendanceStatus) {
        // 如果没有日期范围，设置默认范围
        if (startDate == null) {
            startDate = DateUtils.addDays(new Date(), -30);
        }
        if (endDate == null) {
            endDate = new Date();
        }
        return attendanceStatisticsMapper.selectAttendanceDetails(sessionId, startDate, endDate, attendanceStatus);
    }

    @Override
    public Map<String, Object> getWeeklyReport(Date startDate, Date endDate) {
        if (startDate == null) {
            startDate = DateUtils.addDays(new Date(), -7);
        }
        if (endDate == null) {
            endDate = new Date();
        }

        List<AttendanceStatisticsDTO> weeklyData = attendanceStatisticsMapper.selectWeeklyReport(startDate, endDate);
        Map<String, Object> result = new HashMap<>();
        result.put("weeklyData", weeklyData != null ? weeklyData : new ArrayList<>());
        result.put("avgAttendanceRate", 0.0);
        result.put("top3Sessions", new ArrayList<>());
        result.put("bottom3Sessions", new ArrayList<>());

        return result;
    }

    @Override
    public Map<String, Object> getDashboardMetrics(Date startDate, Date endDate) {
        if (startDate == null) {
            startDate = DateUtils.addDays(new Date(), -7);
        }
        if (endDate == null) {
            endDate = new Date();
        }

        Map<String, Object> metrics = new HashMap<>();
        try {
            // 全校平均签到率
            Double schoolAvgRate = attendanceStatisticsMapper.selectSchoolAverageRate(startDate, endDate);
            metrics.put("schoolAvgRate", schoolAvgRate != null ? schoolAvgRate : 0.0);

            // 总课堂数
            Integer totalSessions = attendanceStatisticsMapper.selectTotalSessions();
            metrics.put("totalSessions", totalSessions != null ? totalSessions : 0);

            // 活跃课堂数
            Integer activeSessions = attendanceStatisticsMapper.selectActiveSessions();
            metrics.put("activeSessions", activeSessions != null ? activeSessions : 0);

            // 总学生数
            Integer totalStudents = attendanceStatisticsMapper.selectTotalStudents();
            metrics.put("totalStudents", totalStudents != null ? totalStudents : 0);

        } catch (Exception e) {
            // 如果查询失败，设置默认值
            metrics.put("schoolAvgRate", 0.0);
            metrics.put("totalSessions", 0);
            metrics.put("activeSessions", 0);
            metrics.put("totalStudents", 0);
        }

        return metrics;
    }

    @Override
    public void exportAttendanceReport(List<AttendanceStatisticsDTO> list, String sheetName) {
        // Excel导出逻辑将在Controller中实现
    }

    @Override
    public List<AttendanceStatisticsDTO> getExportSessionStatistics(Long sessionId, Date startDate, Date endDate) {
        if (startDate == null) {
            startDate = DateUtils.addDays(new Date(), -30);
        }
        if (endDate == null) {
            endDate = new Date();
        }
        return attendanceStatisticsMapper.selectExportSessionStatistics(sessionId, startDate, endDate);
    }

    @Override
    public List<AttendanceStatisticsDTO> getExportTimeStatistics(Long sessionId, Date startDate, Date endDate) {
        if (startDate == null) {
            startDate = DateUtils.addDays(new Date(), -7);
        }
        if (endDate == null) {
            endDate = new Date();
        }
        return attendanceStatisticsMapper.selectExportTimeStatistics(sessionId, startDate, endDate);
    }

    @Override
    public List<AttendanceStatisticsDTO> getExportAttendanceDetails(Long sessionId, Date startDate, Date endDate, Integer attendanceStatus) {
        if (startDate == null) {
            startDate = DateUtils.addDays(new Date(), -30);
        }
        if (endDate == null) {
            endDate = new Date();
        }
        return attendanceStatisticsMapper.selectExportAttendanceDetails(sessionId, startDate, endDate, attendanceStatus);
    }
}