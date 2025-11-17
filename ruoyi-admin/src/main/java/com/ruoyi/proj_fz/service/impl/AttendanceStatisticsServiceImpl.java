package com.ruoyi.proj_fz.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.proj_fz.domain.AttendanceStatisticsDTO;
import com.ruoyi.proj_fz.mapper.AttendanceStatisticsMapper;
import com.ruoyi.proj_fz.service.IAttendanceStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceStatisticsServiceImpl implements IAttendanceStatisticsService {

    @Autowired
    private AttendanceStatisticsMapper attendanceStatisticsMapper;

    @Override
    public List<AttendanceStatisticsDTO> getSessionStatistics(Long sessionId, Date startDate, Date endDate) {
        return attendanceStatisticsMapper.selectSessionStatistics(sessionId, startDate, endDate);
    }

    @Override
    public Map<String, Object> getTimeStatistics(Long sessionId, Date startDate, Date endDate, String groupBy) {
        List<AttendanceStatisticsDTO> statistics = attendanceStatisticsMapper.selectTimeStatistics(sessionId, startDate, endDate, groupBy);

        Map<String, Object> result = new HashMap<>();
        List<String> dateList = new ArrayList<>();
        List<Double> rateList = new ArrayList<>();
        List<Integer> signedList = new ArrayList<>();
        List<Integer> absentList = new ArrayList<>();

        for (AttendanceStatisticsDTO stat : statistics) {
            if ("day".equals(groupBy)) {
                dateList.add(DateUtils.parseDateToStr("MM-dd", stat.getStatDate()));
            } else {
                dateList.add(stat.getStatWeek());
            }
            rateList.add(stat.getAttendanceRate() != null ? stat.getAttendanceRate() : 0.0);
            signedList.add(stat.getDailySigned() != null ? stat.getDailySigned() : 0);
            absentList.add(stat.getDailyAbsent() != null ? stat.getDailyAbsent() : 0);
        }

        result.put("dateList", dateList);
        result.put("rateList", rateList);
        result.put("signedList", signedList);
        result.put("absentList", absentList);
        result.put("statistics", statistics);

        return result;
    }

    @Override
    public List<AttendanceStatisticsDTO> getAttendanceDetails(Long sessionId, Date startDate, Date endDate, Integer attendanceStatus) {
        return attendanceStatisticsMapper.selectAttendanceDetails(sessionId, startDate, endDate, attendanceStatus);
    }

    @Override
    public Map<String, Object> getWeeklyReport(Date startDate, Date endDate) {
        List<AttendanceStatisticsDTO> weeklyData = attendanceStatisticsMapper.selectWeeklyReport(startDate, endDate);
        List<AttendanceStatisticsDTO> topAbsenceStudents = attendanceStatisticsMapper.selectTopAbsenceStudents(startDate, endDate, 10);

        // 计算平均签到率
        Double avgRate = weeklyData.stream()
                .mapToDouble(stat -> stat.getAvgAttendanceRate() != null ? stat.getAvgAttendanceRate() : 0.0)
                .average()
                .orElse(0.0);

        // 获取Top3和Bottom3课堂
        List<AttendanceStatisticsDTO> top3Sessions = weeklyData.stream()
                .limit(3)
                .collect(Collectors.toList());

        List<AttendanceStatisticsDTO> bottom3Sessions = weeklyData.stream()
                .sorted((a, b) -> Double.compare(a.getAvgAttendanceRate() != null ? a.getAvgAttendanceRate() : 0.0,
                        b.getAvgAttendanceRate() != null ? b.getAvgAttendanceRate() : 0.0))
                .limit(3)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("weeklyData", weeklyData);
        result.put("topAbsenceStudents", topAbsenceStudents);
        result.put("avgAttendanceRate", avgRate);
        result.put("top3Sessions", top3Sessions);
        result.put("bottom3Sessions", bottom3Sessions);

        return result;
    }

    @Override
    public Map<String, Object> getDashboardMetrics(Date startDate, Date endDate) {
        Map<String, Object> metrics = new HashMap<>();

        try {
            // 全校平均签到率
            Double schoolAvgRate = attendanceStatisticsMapper.selectSchoolAverageRate(startDate, endDate);
            metrics.put("schoolAvgRate", schoolAvgRate != null ? schoolAvgRate : 0.0);

            // 本周平均签到率
            Double weekAvgRate = attendanceStatisticsMapper.selectWeekAverageRate(startDate, endDate);
            metrics.put("weekAvgRate", weekAvgRate != null ? weekAvgRate : 0.0);

            // 今日平均签到率
            Double todayAvgRate = attendanceStatisticsMapper.selectTodayAverageRate(startDate, endDate);
            metrics.put("todayAvgRate", todayAvgRate != null ? todayAvgRate : 0.0);

            // 总课堂数
            Integer totalSessions = attendanceStatisticsMapper.selectTotalSessions();
            metrics.put("totalSessions", totalSessions != null ? totalSessions : 0);

            // 活跃课堂数
            Integer activeSessions = attendanceStatisticsMapper.selectActiveSessions();
            metrics.put("activeSessions", activeSessions != null ? activeSessions : 0);

            // 总学生数
            Integer totalStudents = attendanceStatisticsMapper.selectTotalStudents();
            metrics.put("totalStudents", totalStudents != null ? totalStudents : 0);

            // 缺勤课堂数
            Integer absenceSessions = attendanceStatisticsMapper.selectAbsenceSessions(startDate, endDate);
            metrics.put("absenceSessions", absenceSessions != null ? absenceSessions : 0);

            // 缺勤次数Top5课堂
            List<AttendanceStatisticsDTO> topAbsenceSessions = attendanceStatisticsMapper.selectTopAbsenceSessions(startDate, endDate, 5);
            metrics.put("topAbsenceSessions", topAbsenceSessions != null ? topAbsenceSessions : new ArrayList<>());

        } catch (Exception e) {
            // 如果查询失败，设置默认值
            metrics.put("schoolAvgRate", 0.0);
            metrics.put("weekAvgRate", 0.0);
            metrics.put("todayAvgRate", 0.0);
            metrics.put("totalSessions", 0);
            metrics.put("activeSessions", 0);
            metrics.put("totalStudents", 0);
            metrics.put("absenceSessions", 0);
            metrics.put("topAbsenceSessions", new ArrayList<>());
        }

        return metrics;
    }

    @Override
    public void exportAttendanceReport(List<AttendanceStatisticsDTO> list, String sheetName) {
        // Excel导出逻辑将在Controller中实现
    }
}