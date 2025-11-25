package com.ruoyi.proj_fz.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.proj_fz.domain.DashboardDTO;
import com.ruoyi.proj_fz.mapper.DashboardMapper;
import com.ruoyi.proj_fz.service.IDashboardService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements IDashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardServiceImpl.class);

    @Autowired
    private DashboardMapper dashboardMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public DashboardDTO getDashboardData() {
        DashboardDTO dashboard = new DashboardDTO();
        dashboard.setWeather(getWeatherInfo());
        dashboard.setCoreMetrics(getCoreMetrics());
        dashboard.setChartData(getChartData());
        return dashboard;
    }

    @Override
    public DashboardDTO.WeatherInfo getWeatherInfo() {
        try {
            // 使用高德天气API的Web服务Key
            String apiKey = "3ae6faeef4eb83bb9c4881d9ec2d12cf";
            String city = "成都";

            // 获取城市编码（使用高德地理编码API）
            String geoUrl = "https://restapi.amap.com/v3/geocode/geo?address=" +
                    URLEncoder.encode(city, StandardCharsets.UTF_8.toString()) +
                    "&key=" + apiKey;

            ResponseEntity<String> geoResponse = restTemplate.getForEntity(geoUrl, String.class);
            String geoBody = geoResponse.getBody();
            log.info("GaoDe Geo API Response: {}", geoBody);

            if (geoBody != null) {
                JSONObject geoJson = JSON.parseObject(geoBody);
                if ("1".equals(geoJson.getString("status"))) {
                    JSONArray geocodes = geoJson.getJSONArray("geocodes");
                    if (geocodes != null && !geocodes.isEmpty()) {
                        String adcode = geocodes.getJSONObject(0).getString("adcode");

                        // 使用城市编码获取天气信息
                        String weatherUrl = "https://restapi.amap.com/v3/weather/weatherInfo?city=" +
                                adcode + "&key=" + apiKey + "&extensions=all";

                        ResponseEntity<String> weatherResponse = restTemplate.getForEntity(weatherUrl, String.class);
                        String weatherBody = weatherResponse.getBody();
                        log.info("GaoDe Weather API Response: {}", weatherBody);

                        if (weatherBody != null) {
                            JSONObject jsonResponse = JSON.parseObject(weatherBody);
                            if ("1".equals(jsonResponse.getString("status"))) {

                                // 解析实时天气
                                JSONArray lives = jsonResponse.getJSONArray("lives");
                                if (lives != null && !lives.isEmpty()) {
                                    JSONObject liveWeather = lives.getJSONObject(0);

                                    // 解析天气预报
                                    JSONArray forecastsJson = jsonResponse.getJSONArray("forecasts");
                                    List<DashboardDTO.WeatherForecast> forecastList = new ArrayList<>();

                                    if (forecastsJson != null && !forecastsJson.isEmpty()) {
                                        JSONArray casts = forecastsJson.getJSONObject(0).getJSONArray("casts");
                                        forecastList = casts.stream()
                                                .map(item -> {
                                                    JSONObject day = (JSONObject) item;
                                                    DashboardDTO.WeatherForecast forecast = new DashboardDTO.WeatherForecast();
                                                    forecast.setDate(day.getString("date"));
                                                    forecast.setTempMin(day.getString("nighttemp"));
                                                    forecast.setTempMax(day.getString("daytemp"));
                                                    forecast.setWeather(day.getString("dayweather"));
                                                    return forecast;
                                                })
                                                .limit(3)
                                                .collect(Collectors.toList());
                                    }

                                    // 封装数据到DTO
                                    DashboardDTO.WeatherInfo weatherInfo = new DashboardDTO.WeatherInfo();
                                    weatherInfo.setCity(liveWeather.getString("city"));
                                    weatherInfo.setTemperature(liveWeather.getString("temperature"));
                                    weatherInfo.setWeather(liveWeather.getString("weather"));
                                    weatherInfo.setHumidity(liveWeather.getString("humidity") + "%");
                                    weatherInfo.setWind(liveWeather.getString("winddirection") + "风 " +
                                            liveWeather.getString("windpower") + "级");
                                    weatherInfo.setForecast(forecastList);

                                    return weatherInfo;
                                }
                            } else {
                                log.warn("高德天气API调用失败: {}", jsonResponse.getString("info"));
                            }
                        }
                    }
                }
            }
        } catch (HttpClientErrorException e) {
            log.error("调用高德天气API时发生HTTP客户端错误: {} - {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("调用高德天气API时发生未知异常", e);
        }

        // 如果API调用失败，返回默认数据或空数据
        return createDefaultWeatherInfo();
    }

    private DashboardDTO.WeatherInfo createDefaultWeatherInfo() {
        DashboardDTO.WeatherInfo weatherInfo = new DashboardDTO.WeatherInfo();
        weatherInfo.setCity("成都");
        weatherInfo.setTemperature("25");
        weatherInfo.setWeather("晴");
        weatherInfo.setHumidity("60%");
        weatherInfo.setWind("东南风 3级");

        // 创建默认的天气预报
        List<DashboardDTO.WeatherForecast> forecastList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < 3; i++) {
            DashboardDTO.WeatherForecast forecast = new DashboardDTO.WeatherForecast();
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            forecast.setDate(DateUtils.parseDateToStr("yyyy-MM-dd", calendar.getTime()));
            forecast.setTempMin("20");
            forecast.setTempMax("28");
            forecast.setWeather("多云");
            forecastList.add(forecast);
        }

        weatherInfo.setForecast(forecastList);
        return weatherInfo;
    }

    @Override
    public DashboardDTO.CoreMetrics getCoreMetrics() {
        DashboardDTO.CoreMetrics coreMetrics = new DashboardDTO.CoreMetrics();

        coreMetrics.setTotalCourses(dashboardMapper.countTotalCourses());
        coreMetrics.setActiveClasses(dashboardMapper.countActiveSessions());
        coreMetrics.setTodayDeadline(dashboardMapper.countTodayDeadline());
        coreMetrics.setOngoingExams(dashboardMapper.countOngoingExams());
        coreMetrics.setPendingTodos(dashboardMapper.countPendingTodos());
        coreMetrics.setGradedCount(dashboardMapper.countGradedAssignments());

        int submittedAssignments = dashboardMapper.countSubmittedAssignments();
        int gradedCount = coreMetrics.getGradedCount();
        long gradingRate = submittedAssignments > 0 ? Math.round((gradedCount * 100.0) / submittedAssignments) : 0;
        coreMetrics.setGradingRate((int) gradingRate);

        coreMetrics.setCourseTrend(calculateTrend(
                dashboardMapper.getCourseCountBetweenDays(0, 7),
                dashboardMapper.getCourseCountBetweenDays(7, 14)
        ));
        coreMetrics.setClassTrend(calculateTrend(
                dashboardMapper.getActiveClassCountBetweenDays(0, 7),
                dashboardMapper.getActiveClassCountBetweenDays(7, 14)
        ));
        coreMetrics.setHomeworkTrend(calculateTrend(
                dashboardMapper.getHomeworkCountBetweenDays(0, 7),
                dashboardMapper.getHomeworkCountBetweenDays(7, 14)
        ));
        coreMetrics.setExamTrend(calculateTrend(
                dashboardMapper.getOngoingExamCountBetweenDays(0, 7),
                dashboardMapper.getOngoingExamCountBetweenDays(7, 14)
        ));
        coreMetrics.setTodoTrend(calculateTrend(
                dashboardMapper.getPendingTodoCountBetweenDays(0, 7),
                dashboardMapper.getPendingTodoCountBetweenDays(7, 14)
        ));
        coreMetrics.setGradingTrend(calculateTrend(
                dashboardMapper.getGradedCountBetweenDays(0, 7),
                dashboardMapper.getGradedCountBetweenDays(7, 14)
        ));

        return coreMetrics;
    }

    private int calculateTrend(int currentPeriodCount, int previousPeriodCount) {
        if (previousPeriodCount == 0) {
            return currentPeriodCount > 0 ? 100 : 0;
        }
        double trend = ((double) (currentPeriodCount - previousPeriodCount) / previousPeriodCount) * 100;
        return (int) Math.round(trend);
    }

    @Override
    public DashboardDTO.ChartData getChartData() {
        DashboardDTO.ChartData chartData = new DashboardDTO.ChartData();
        chartData.setTrendData(dashboardMapper.getSubmissionTrend(7));

        DashboardDTO.SubmissionStatus submissionStatus = new DashboardDTO.SubmissionStatus();
        int totalAssignments = dashboardMapper.countTotalAssignments();
        int submittedCount = dashboardMapper.countSubmittedAssignments();
        int gradedCount = dashboardMapper.countGradedAssignments();
        submissionStatus.setSubmitted(submittedCount);
        submissionStatus.setPending(totalAssignments - submittedCount);
        submissionStatus.setGraded(gradedCount);
        chartData.setSubmissionStatus(submissionStatus);

        DashboardDTO.ScoreStats scoreStats = new DashboardDTO.ScoreStats();
        Double avgScore = dashboardMapper.getAverageScore();
        scoreStats.setAverage(avgScore != null ? Math.round(avgScore * 100.0) / 100.0 : 0.0);
        Double maxScore = dashboardMapper.getMaxScore();
        scoreStats.setMax(maxScore != null ? Math.round(maxScore * 100.0) / 100.0 : 0.0);
        int passCount = dashboardMapper.countPassAssignments();
        long passRate = submittedCount > 0 ? Math.round((passCount * 100.0) / submittedCount) : 0;
        scoreStats.setPassRate((int) passRate);
        chartData.setScoreStats(scoreStats);

        chartData.setScoreDistribution(dashboardMapper.getScoreDistribution());
        chartData.setCourseScores(dashboardMapper.getCourseScores());
        chartData.setAttendanceRates(dashboardMapper.getAttendanceRates());
        chartData.setParticipationHeat(dashboardMapper.getParticipationHeat());

        return chartData;
    }

    @Override
    public List<Map<String, Object>> getTodos() {
        return dashboardMapper.getTodos();
    }

    @Override
    public List<Map<String, Object>> getMessages() {
        return dashboardMapper.getMessages();
    }

    @Override
    public List<Map<String, Object>> getHomeworkDetails(Map<String, Object> params) {
        return dashboardMapper.getHomeworkDetails(params);
    }

    @Override
    public List<Map<String, Object>> getNotices(Map<String, Object> params) {
        return dashboardMapper.getNotices(params);
    }

    @Override
    public List<Map<String, Object>> getOperationLogs(Map<String, Object> params) {
        return dashboardMapper.getOperationLogs(params);
    }

    @Override
    public List<Map<String, Object>> getHomeworkByStatus(String status) {
        Map<String, Object> params = new HashMap<>();
        if ("pending".equals(status)) {
            params.put("status", "pending");
        } else if ("submitted".equals(status)) {
            params.put("status", "submitted");
        } else if ("graded".equals(status)) {
            params.put("status", "graded");
        }
        return dashboardMapper.getHomeworkDetails(params);
    }

    @Override
    public Map<String, Object> getExamStatistics() {
        return dashboardMapper.getExamStatistics();
    }

    @Override
    public Map<String, Object> getStudentActivity() {
        return dashboardMapper.getStudentActivity();
    }

    @Override
    public void exportHomeworkDetails(Map<String, Object> params, HttpServletResponse response) {
        List<Map<String, Object>> list = dashboardMapper.getHomeworkDetails(params);
        String[] headers = {"作业名称", "课程", "发布时间", "截止时间", "已提交", "未提交", "状态"};
        String fileName = "作业明细";

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(fileName);
            createExcel(workbook, sheet, headers, list, (row, item) -> {
                row.createCell(0).setCellValue(Objects.toString(item.get("title"), ""));
                row.createCell(1).setCellValue(Objects.toString(item.get("course"), ""));
                row.createCell(2).setCellValue(formatDateForExcel(item.get("publishTime")));
                row.createCell(3).setCellValue(formatDateForExcel(item.get("deadline")));
                row.createCell(4).setCellValue(Objects.toString(item.get("submittedCount"), ""));
                row.createCell(5).setCellValue(Objects.toString(item.get("pendingCount"), ""));
                row.createCell(6).setCellValue(Objects.toString(item.get("status"), ""));
            });
            writeToResponse(response, workbook, fileName);
        } catch (Exception e) {
            log.error("导出作业明细失败", e);
            throw new RuntimeException("导出作业明细失败", e);
        }
    }

    @Override
    public void exportNotices(Map<String, Object> params, HttpServletResponse response) {
        List<Map<String, Object>> list = dashboardMapper.getNotices(params);
        String[] headers = {"标题", "发布人", "发布时间"};
        String fileName = "最新公告";

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(fileName);
            createExcel(workbook, sheet, headers, list, (row, item) -> {
                row.createCell(0).setCellValue(Objects.toString(item.get("title"), ""));
                row.createCell(1).setCellValue(Objects.toString(item.get("author"), ""));
                row.createCell(2).setCellValue(formatDateForExcel(item.get("createTime")));
            });
            writeToResponse(response, workbook, fileName);
        } catch (Exception e) {
            log.error("导出最新公告失败", e);
            throw new RuntimeException("导出最新公告失败", e);
        }
    }

    @Override
    public void exportOperationLogs(Map<String, Object> params, HttpServletResponse response) {
        List<Map<String, Object>> list = dashboardMapper.getOperationLogs(params);
        String[] headers = {"操作内容", "操作人", "操作时间", "IP地址", "操作类型"};
        String fileName = "操作日志";

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(fileName);
            createExcel(workbook, sheet, headers, list, (row, item) -> {
                row.createCell(0).setCellValue(Objects.toString(item.get("title"), ""));
                row.createCell(1).setCellValue(Objects.toString(item.get("operator"), ""));
                row.createCell(2).setCellValue(formatDateForExcel(item.get("operateTime")));
                row.createCell(3).setCellValue(Objects.toString(item.get("ip"), ""));
                row.createCell(4).setCellValue(getBusinessTypeText(item.get("businessType")));
            });
            writeToResponse(response, workbook, fileName);
        } catch (Exception e) {
            log.error("导出操作日志失败", e);
            throw new RuntimeException("导出操作日志失败", e);
        }
    }

    private String formatDateForExcel(Object dateObj) {
        if (dateObj instanceof Date) {
            return DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, (Date) dateObj);
        }
        return "";
    }

    private <T> void createExcel(Workbook workbook, Sheet sheet, String[] headers, List<T> list, RowFiller<T> filler) {
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (T item : list) {
            Row row = sheet.createRow(rowNum++);
            filler.fill(row, item);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void writeToResponse(HttpServletResponse response, Workbook workbook, String fileName) throws Exception {
        String encodedFileName = URLEncoder.encode(fileName + ".xlsx", StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        workbook.write(response.getOutputStream());
    }

    @FunctionalInterface
    private interface RowFiller<T> {
        void fill(Row row, T item);
    }

    private String getBusinessTypeText(Object type) {
        if (type == null) return "其他";
        String typeStr = type.toString();
        switch (typeStr) {
            case "0": return "新增";
            case "1": return "修改";
            case "2": return "删除";
            case "10": return "登录";
            default: return "其他";
        }
    }
}