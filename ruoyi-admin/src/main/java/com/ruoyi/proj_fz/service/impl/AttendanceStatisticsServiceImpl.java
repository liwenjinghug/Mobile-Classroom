package com.ruoyi.proj_fz.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.proj_fz.domain.AttendanceStatisticsDTO;
import com.ruoyi.proj_fz.mapper.AttendanceStatisticsMapper;
import com.ruoyi.proj_fz.service.IAttendanceStatisticsService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    @Override
    public void exportAttendanceData(Map<String, Object> params, HttpServletResponse response) {
        try {
            List<AttendanceStatisticsDTO> dataList;
            String sheetName = "考勤统计报表";

            // 根据导出类型获取数据
            String exportType = (String) params.get("exportType");
            Long sessionId = (Long) params.get("sessionId");
            Date startDate = params.get("startDate") != null ?
                    DateUtils.parseDate((String) params.get("startDate")) : null;
            Date endDate = params.get("endDate") != null ?
                    DateUtils.parseDate((String) params.get("endDate")) : null;
            Integer attendanceStatus = (Integer) params.get("attendanceStatus");

            if ("details".equals(exportType)) {
                dataList = this.getExportAttendanceDetails(sessionId, startDate, endDate, attendanceStatus);
                sheetName = "考勤明细数据";
            } else if ("trend".equals(exportType)) {
                dataList = this.getExportTimeStatistics(sessionId, startDate, endDate);
                sheetName = "时间趋势统计";
            } else {
                dataList = this.getExportSessionStatistics(sessionId, startDate, endDate);
                sheetName = "课堂维度统计";
            }

            if (dataList == null || dataList.isEmpty()) {
                throw new RuntimeException("没有数据可以导出");
            }

            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet(sheetName);

            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 创建数据行样式
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // 根据导出类型创建不同的表头
            String[] headers;
            if ("details".equals(exportType)) {
                headers = new String[]{
                        "课堂名称", "学生姓名", "学号", "签到时间", "签到方式", "状态"
                };
            } else if ("trend".equals(exportType)) {
                headers = new String[]{
                        "课堂名称", "签到时间", "签到人数", "缺勤人数", "迟到人数",
                        "请假人数", "早退人数", "总人数", "签到率(%)"
                };
            } else {
                headers = new String[]{
                        "课堂名称", "总人数", "平均已签到", "平均缺勤", "平均迟到",
                        "平均请假", "平均早退", "平均签到率(%)"
                };
            }

            // 创建表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowNum = 1;
            for (AttendanceStatisticsDTO data : dataList) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                if ("details".equals(exportType)) {
                    // 明细数据
                    row.createCell(colNum++).setCellValue(data.getClassName() != null ? data.getClassName() : "");
                    row.createCell(colNum++).setCellValue(data.getStudentName() != null ? data.getStudentName() : "");
                    row.createCell(colNum++).setCellValue(data.getStudentNo() != null ? data.getStudentNo() : "");
                    row.createCell(colNum++).setCellValue(data.getAttendanceTime() != null ?
                            DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss", data.getAttendanceTime()) : "未签到");
                    row.createCell(colNum++).setCellValue(data.getAttendanceMethod() != null ? data.getAttendanceMethod() : "未知");
                    row.createCell(colNum++).setCellValue(data.getStatusText() != null ? data.getStatusText() : "未知");
                } else if ("trend".equals(exportType)) {
                    // 趋势数据
                    row.createCell(colNum++).setCellValue(data.getClassName() != null ? data.getClassName() : "");
                    row.createCell(colNum++).setCellValue(data.getStatDate() != null ?
                            DateUtils.parseDateToStr("yyyy-MM-dd HH:mm", data.getStatDate()) : "");
                    row.createCell(colNum++).setCellValue(data.getDailySigned() != null ? data.getDailySigned() : 0);
                    row.createCell(colNum++).setCellValue(data.getDailyAbsent() != null ? data.getDailyAbsent() : 0);
                    row.createCell(colNum++).setCellValue(data.getDailyLate() != null ? data.getDailyLate() : 0);
                    row.createCell(colNum++).setCellValue(data.getDailyLeave() != null ? data.getDailyLeave() : 0);
                    row.createCell(colNum++).setCellValue(data.getDailyEarlyLeave() != null ? data.getDailyEarlyLeave() : 0);
                    row.createCell(colNum++).setCellValue(data.getTotalStudents() != null ? data.getTotalStudents() : 0);
                    row.createCell(colNum++).setCellValue(data.getAttendanceRate() != null ? data.getAttendanceRate() : 0);
                } else {
                    // 汇总数据
                    row.createCell(colNum++).setCellValue(data.getClassName() != null ? data.getClassName() : "");
                    row.createCell(colNum++).setCellValue(data.getTotalStudents() != null ? data.getTotalStudents() : 0);
                    row.createCell(colNum++).setCellValue(data.getSignedCount() != null ? data.getSignedCount() : 0);
                    row.createCell(colNum++).setCellValue(data.getAbsentCount() != null ? data.getAbsentCount() : 0);
                    row.createCell(colNum++).setCellValue(data.getLateCount() != null ? data.getLateCount() : 0);
                    row.createCell(colNum++).setCellValue(data.getLeaveCount() != null ? data.getLeaveCount() : 0);
                    row.createCell(colNum++).setCellValue(data.getEarlyLeaveCount() != null ? data.getEarlyLeaveCount() : 0);
                    row.createCell(colNum++).setCellValue(data.getAttendanceRate() != null ? data.getAttendanceRate() : 0);
                }
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 设置响应头
            String fileName = URLEncoder.encode(sheetName + ".xlsx", StandardCharsets.UTF_8.toString());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            response.setCharacterEncoding("UTF-8");

            // 写入响应流
            workbook.write(response.getOutputStream());
            workbook.close();

        } catch (Exception e) {
            throw new RuntimeException("导出失败: " + e.getMessage(), e);
        }
    }
}