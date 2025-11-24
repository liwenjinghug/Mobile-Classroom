package com.ruoyi.proj_fz.service.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.proj_fz.mapper.ParticipationHeatMapper;
import com.ruoyi.proj_fz.service.IParticipationHeatService;

@Service
public class ParticipationHeatServiceImpl implements IParticipationHeatService
{
    @Autowired
    private ParticipationHeatMapper participationHeatMapper;

    @Override
    public List<Map<String, Object>> selectParticipationHeatData()
    {
        List<Map<String, Object>> heatData = participationHeatMapper.selectParticipationHeatData();
        return calculateParticipationScores(heatData);
    }

    @Override
    public Map<String, Object> selectParticipationStats()
    {
        return participationHeatMapper.selectParticipationStats();
    }

    @Override
    public Map<String, Object> selectParticipationDistribution(Map<String, Object> params) {
        String studentNo = (String) params.get("studentNo");
        String studentName = (String) params.get("studentName");
        return participationHeatMapper.selectParticipationDistributionByFilter(studentNo, studentName);
    }

    @Override
    public List<Map<String, Object>> selectParticipationHeatDataByFilter(Map<String, Object> params)
    {
        // 获取基础数据
        String studentNo = (String) params.get("studentNo");
        String studentName = (String) params.get("studentName");

        List<Map<String, Object>> allData = participationHeatMapper.selectParticipationHeatDataByCondition(studentNo, studentName);
        List<Map<String, Object>> calculatedData = calculateParticipationScores(allData);

        // 在Service层进行排序
        String sortField = (String) params.get("sortField");
        String sortOrder = (String) params.get("sortOrder");
        if (sortField != null && !sortField.isEmpty()) {
            calculatedData = sortData(calculatedData, sortField, sortOrder);
        }

        // 根据参与等级和分数范围进行筛选
        return filterByParticipationLevelAndScore(calculatedData, params);
    }

    @Override
    public void exportParticipationHeatData(Map<String, Object> params, HttpServletResponse response)
    {
        try {
            // 使用筛选后的数据导出
            List<Map<String, Object>> dataList = this.selectParticipationHeatDataByFilter(params);

            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("学生参与热力数据");

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
                    "学号", "姓名", "签到率(%)", "作业提交率(%)", "论坛活跃度(分)",
                    "综合参与度(分)", "参与等级"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowNum = 1;
            for (Map<String, Object> data : dataList) {
                Row row = sheet.createRow(rowNum++);

                int colNum = 0;
                row.createCell(colNum++).setCellValue(getStringValue(data.get("student_no")));
                row.createCell(colNum++).setCellValue(getStringValue(data.get("student_name")));
                row.createCell(colNum++).setCellValue(getNumericValue(data.get("attendance_rate")));
                row.createCell(colNum++).setCellValue(getNumericValue(data.get("homework_rate")));
                row.createCell(colNum++).setCellValue(getNumericValue(data.get("forum_score")));
                row.createCell(colNum++).setCellValue(getNumericValue(data.get("participation_score")));
                row.createCell(colNum++).setCellValue(getStringValue(data.get("participation_level")));

                // 设置数据行样式
                for (int i = 0; i < headers.length; i++) {
                    if (row.getCell(i) != null) {
                        row.getCell(i).setCellStyle(dataStyle);
                    }
                }
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 设置响应头
            String fileName = URLEncoder.encode("学生参与热力数据.xlsx", StandardCharsets.UTF_8.toString());
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

    @Override
    public Map<String, Object> getFilterOptions()
    {
        Map<String, Object> options = new HashMap<>();

        // 获取参与等级选项
        List<Map<String, Object>> levelOptions = new ArrayList<>();
        String[] levels = {"极高", "高", "中等", "一般", "低"};
        for (String level : levels) {
            Map<String, Object> levelOption = new HashMap<>();
            levelOption.put("label", level);
            levelOption.put("value", level);
            levelOptions.add(levelOption);
        }
        options.put("participationLevels", levelOptions);

        // 获取分数范围选项
        List<Map<String, Object>> scoreRangeOptions = new ArrayList<>();
        String[] scoreRanges = {"90-100", "80-89", "70-79", "60-69", "0-59"};
        for (String range : scoreRanges) {
            Map<String, Object> rangeOption = new HashMap<>();
            rangeOption.put("label", range);
            rangeOption.put("value", range);
            scoreRangeOptions.add(rangeOption);
        }
        options.put("scoreRanges", scoreRangeOptions);

        return options;
    }

    /**
     * 在Service层进行数据排序
     */
    private List<Map<String, Object>> sortData(List<Map<String, Object>> data, String sortField, String sortOrder) {
        return data.stream()
                .sorted((a, b) -> {
                    Object valueA = a.get(sortField);
                    Object valueB = b.get(sortField);

                    if (valueA == null && valueB == null) return 0;
                    if (valueA == null) return 1;
                    if (valueB == null) return -1;

                    int result = 0;
                    if (valueA instanceof Number && valueB instanceof Number) {
                        double numA = ((Number) valueA).doubleValue();
                        double numB = ((Number) valueB).doubleValue();
                        result = Double.compare(numA, numB);
                    } else {
                        result = valueA.toString().compareTo(valueB.toString());
                    }

                    return "DESC".equalsIgnoreCase(sortOrder) ? -result : result;
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据参与等级和分数范围进行筛选
     */
    private List<Map<String, Object>> filterByParticipationLevelAndScore(List<Map<String, Object>> data, Map<String, Object> params) {
        String participationLevel = (String) params.get("participationLevel");
        String scoreRange = (String) params.get("scoreRange");

        // 如果没有参与等级和分数范围的筛选，直接返回数据
        if ((participationLevel == null || participationLevel.isEmpty()) &&
                (scoreRange == null || scoreRange.isEmpty())) {
            return data;
        }

        // 过滤数据
        return data.stream()
                .filter(item -> {
                    boolean levelMatch = true;
                    boolean scoreMatch = true;

                    // 参与等级筛选
                    if (participationLevel != null && !participationLevel.isEmpty()) {
                        String dataLevel = (String) item.get("participation_level");
                        levelMatch = participationLevel.equals(dataLevel);
                    }

                    // 分数范围筛选
                    if (scoreRange != null && !scoreRange.isEmpty()) {
                        int score = getIntValue(item.get("participation_score"));
                        scoreMatch = isScoreInRange(score, scoreRange);
                    }

                    return levelMatch && scoreMatch;
                })
                .collect(Collectors.toList());
    }

    /**
     * 判断分数是否在指定范围内
     */
    private boolean isScoreInRange(int score, String scoreRange) {
        switch (scoreRange) {
            case "90-100": return score >= 90 && score <= 100;
            case "80-89": return score >= 80 && score < 90;
            case "70-79": return score >= 70 && score < 80;
            case "60-69": return score >= 60 && score < 70;
            case "0-59": return score >= 0 && score < 60;
            default: return true;
        }
    }

    /**
     * 计算参与度分数和等级
     * 权重：签到率40% + 作业提交率30% + 论坛活跃度30%
     */
    private List<Map<String, Object>> calculateParticipationScores(List<Map<String, Object>> heatData) {
        for (Map<String, Object> data : heatData) {
            int attendanceScore = getIntValue(data.get("attendance_rate"));
            int homeworkScore = getIntValue(data.get("homework_rate"));
            int forumScore = getIntValue(data.get("forum_score"));

            // 计算综合参与度分数（0-100分）
            int totalScore = (int) (attendanceScore * 0.4 + homeworkScore * 0.3 + forumScore * 0.3);
            data.put("participation_score", totalScore);

            // 确定参与等级
            String level;
            if (totalScore >= 90) level = "极高";
            else if (totalScore >= 80) level = "高";
            else if (totalScore >= 70) level = "中等";
            else if (totalScore >= 60) level = "一般";
            else level = "低";
            data.put("participation_level", level);
        }
        return heatData;
    }

    private int getIntValue(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String getStringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private double getNumericValue(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}