package com.ruoyi.proj_fz.service.impl;

import com.ruoyi.proj_fz.domain.HomeworkStatisticsDTO;
import com.ruoyi.proj_fz.mapper.HomeworkStatisticsMapper;
import com.ruoyi.proj_fz.service.IHomeworkStatisticsService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class HomeworkStatisticsServiceImpl implements IHomeworkStatisticsService {

    @Autowired
    private HomeworkStatisticsMapper statisticsMapper;

    @Override
    public List<HomeworkStatisticsDTO> getHomeworkStatisticsList() {
        return statisticsMapper.selectHomeworkStatisticsList();
    }

    @Override
    public List<HomeworkStatisticsDTO> getHomeworkStatisticsListByFilter(Map<String, Object> params) {
        return statisticsMapper.selectHomeworkStatisticsListByFilter(params);
    }

    @Override
    public HomeworkStatisticsDTO getHomeworkStatisticsById(Long homeworkId) {
        return statisticsMapper.selectHomeworkStatisticsById(homeworkId);
    }

    @Override
    public List<Map<String, Object>> getScoreDistribution(Long homeworkId) {
        return statisticsMapper.selectScoreDistribution(homeworkId);
    }

    @Override
    public List<Map<String, Object>> getSubmissionTrend(Long homeworkId) {
        return statisticsMapper.selectSubmissionTrend(homeworkId);
    }

    @Override
    public List<Map<String, Object>> getTeacherHomeworkOverview() {
        return statisticsMapper.selectTeacherHomeworkOverview();
    }

    @Override
    public List<Map<String, Object>> getStudentSubmissionDetails(Long homeworkId) {
        return statisticsMapper.selectStudentSubmissionDetails(homeworkId);
    }

    @Override
    public Map<String, Object> getDashboardOverview() {
        return statisticsMapper.selectDashboardOverview();
    }

    @Override
    public List<Map<String, Object>> getAllHomeworkBasicInfo() {
        return statisticsMapper.selectAllHomeworkBasicInfo();
    }

    @Override
    public List<Map<String, Object>> getHomeworkSubmissionStats() {
        return statisticsMapper.selectHomeworkSubmissionStats();
    }

    @Override
    public List<Map<String, Object>> getCourseList() {
        return statisticsMapper.selectCourseList();
    }

    @Override
    public List<Map<String, Object>> getSessionList() {
        return statisticsMapper.selectSessionList();
    }

    @Override
    public List<Map<String, Object>> getSessionHomeworkOverview() {
        return statisticsMapper.selectSessionHomeworkOverview();
    }

    @Override
    public void exportHomeworkData(Map<String, Object> params, HttpServletResponse response) {
        try {
            List<HomeworkStatisticsDTO> dataList;

            // 统一使用高级筛选方法，支持所有筛选条件
            dataList = this.getHomeworkStatisticsListByAdvancedFilter(params);

            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("作业统计数据");

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

            // 创建表头
            String[] headers = {
                    "作业ID", "作业名称", "课程", "课堂", "发布时间", "截止时间",
                    "总人数", "已提交", "未提交", "逾期提交", "已批改",
                    "提交率(%)", "平均分", "总分", "发布者"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowNum = 1;
            for (HomeworkStatisticsDTO data : dataList) {
                Row row = sheet.createRow(rowNum++);

                int colNum = 0;
                row.createCell(colNum++).setCellValue(data.getHomeworkId() != null ? data.getHomeworkId() : 0);
                row.createCell(colNum++).setCellValue(data.getHomeworkTitle() != null ? data.getHomeworkTitle() : "");
                row.createCell(colNum++).setCellValue(data.getCourseName() != null ? data.getCourseName() : "");
                row.createCell(colNum++).setCellValue(data.getClassName() != null ? data.getClassName() : "");
                row.createCell(colNum++).setCellValue(data.getCreateTime() != null ? data.getCreateTime() : "");
                row.createCell(colNum++).setCellValue(data.getDeadline() != null ? data.getDeadline() : "");
                row.createCell(colNum++).setCellValue(data.getTotalStudents() != null ? data.getTotalStudents() : 0);
                row.createCell(colNum++).setCellValue(data.getSubmittedCount() != null ? data.getSubmittedCount() : 0);
                row.createCell(colNum++).setCellValue(data.getNotSubmittedCount() != null ? data.getNotSubmittedCount() : 0);
                row.createCell(colNum++).setCellValue(data.getOverdueCount() != null ? data.getOverdueCount() : 0);
                row.createCell(colNum++).setCellValue(data.getGradedCount() != null ? data.getGradedCount() : 0);
                row.createCell(colNum++).setCellValue(data.getSubmissionRate() != null ? data.getSubmissionRate() : 0);
                row.createCell(colNum++).setCellValue(data.getAverageScore() != null ? data.getAverageScore() : 0);
                row.createCell(colNum++).setCellValue(data.getTotalScore() != null ? data.getTotalScore() : 0);
                row.createCell(colNum++).setCellValue(data.getCreateBy() != null ? data.getCreateBy() : "");
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 设置响应头
            String fileName = URLEncoder.encode("作业数据统计.xlsx", StandardCharsets.UTF_8.toString());
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

    private boolean hasAdvancedFilter(Map<String, Object> params) {
        return params.containsKey("createTimeStart") || params.containsKey("createTimeEnd") ||
                params.containsKey("deadlineStart") || params.containsKey("deadlineEnd") ||
                params.containsKey("expireStatus") || params.containsKey("gradeStatus") ||
                params.containsKey("completionStatus");
    }

    @Override
    public Integer getTodayDeadlineCount() {
        return statisticsMapper.selectTodayDeadlineCount();
    }

    @Override
    public List<HomeworkStatisticsDTO> getHomeworkStatisticsListByAdvancedFilter(Map<String, Object> params) {
        return statisticsMapper.selectHomeworkStatisticsListByAdvancedFilter(params);
    }
}