package com.ruoyi.proj_fz.service;

import com.ruoyi.proj_fz.domain.AttendanceStatisticsDTO;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IAttendanceStatisticsService {

    // 课堂维度统计
    List<AttendanceStatisticsDTO> getSessionStatistics(Long sessionId, Date startDate, Date endDate);

    // 时间维度统计
    Map<String, Object> getTimeStatistics(Long sessionId, Date startDate, Date endDate, String groupBy);

    // 签到明细
    List<AttendanceStatisticsDTO> getAttendanceDetails(Long sessionId, Date startDate, Date endDate, Integer attendanceStatus);

    // 周报表
    Map<String, Object> getWeeklyReport(Date startDate, Date endDate);

    // 驾驶舱指标
    Map<String, Object> getDashboardMetrics(Date startDate, Date endDate);

    // 导出Excel
    void exportAttendanceReport(List<AttendanceStatisticsDTO> list, String sheetName);

    // 新增导出方法
    List<AttendanceStatisticsDTO> getExportSessionStatistics(Long sessionId, Date startDate, Date endDate);
    List<AttendanceStatisticsDTO> getExportTimeStatistics(Long sessionId, Date startDate, Date endDate);
    List<AttendanceStatisticsDTO> getExportAttendanceDetails(Long sessionId, Date startDate, Date endDate, Integer attendanceStatus);
}