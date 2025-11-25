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

    // 天气配置（内存保存，避免修改外部配置文件）
    private volatile String weatherApiKey = "3ae6faeef4eb83bb9c4881d9ec2d12cf";
    private volatile String weatherCity = "成都";
    private volatile String weatherAdcode = "510100"; // 默认成都

    public DashboardServiceImpl() {
        // 配置超时时间，避免天气接口网络阻塞影响整体加载
        try {
            org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(3000);
            factory.setReadTimeout(3000);
            this.restTemplate.setRequestFactory(factory);
        } catch (Exception e) {
            log.warn("初始化 RestTemplate 超时配置失败: {}", e.getMessage());
        }
    }

    @Override
    public DashboardDTO getDashboardData() {
        DashboardDTO dashboard = new DashboardDTO();
        dashboard.setWeather(getWeatherInfo());
        dashboard.setCoreMetrics(getCoreMetrics());
        dashboard.setChartData(getChartData());
        return dashboard;
    }

    @Override
    public Object getWeatherConfig() {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put("apiKey", weatherApiKey);
        cfg.put("city", weatherCity);
        cfg.put("adcode", weatherAdcode);
        return cfg;
    }

    @Override
    public void updateWeatherConfig(String apiKey, String city, String adcode) {
        if (apiKey != null && !apiKey.isEmpty()) {
            this.weatherApiKey = apiKey.trim();
        }
        if (city != null && !city.isEmpty()) {
            this.weatherCity = city.trim();
            // 清除手动传 adcode 时的城市编码优先级
            if (adcode == null || adcode.isEmpty()) {
                // 尝试解析新的城市编码
                String resolved = resolveAdcode(this.weatherCity, this.weatherApiKey);
                if (resolved != null && !resolved.isEmpty()) {
                    this.weatherAdcode = resolved;
                }
            }
        }
        if (adcode != null && !adcode.isEmpty()) {
            this.weatherAdcode = adcode.trim();
        } else if (city != null && !city.isEmpty()) {
            // 若只更新 city，则尝试自动更新 adcode
            String resolved = resolveAdcode(this.weatherCity, this.weatherApiKey);
            if (resolved != null && !resolved.isEmpty()) {
                this.weatherAdcode = resolved;
            }
        }
        log.info("天气配置已更新: apiKey={}, city={}, adcode={}", maskApiKey(this.weatherApiKey), this.weatherCity, this.weatherAdcode);
    }

    private String maskApiKey(String key) {
        if (key == null) return "";
        if (key.length() <= 6) return "***";
        return key.substring(0, 3) + "***" + key.substring(key.length() - 3);
    }

    @Override
    public DashboardDTO.WeatherInfo getWeatherInfo() {
        String apiKey = this.weatherApiKey; // 使用可更新的配置
        String cityName = this.weatherCity;
        String fixedAdcode = this.weatherAdcode;
        DashboardDTO.WeatherInfo result = new DashboardDTO.WeatherInfo();
        try {
            // 若有已缓存的 adcode 直接使用；否则解析一次
            String adcode = (fixedAdcode != null && !fixedAdcode.isEmpty()) ? fixedAdcode : resolveAdcode(cityName, apiKey);
            if (adcode == null || adcode.isEmpty()) {
                log.warn("未能解析城市adcode, city={}, 使用默认成都", cityName);
                adcode = "510100"; // 成都
            } else {
                this.weatherAdcode = adcode; // 缓存解析结果
            }

            // 2. 获取实时天气 (extensions=base)
            JSONObject liveJson = callWeatherApi(adcode, apiKey, false);
            if (liveJson != null) {
                JSONArray lives = liveJson.getJSONArray("lives");
                if (lives != null && !lives.isEmpty()) {
                    JSONObject live = lives.getJSONObject(0);
                    result.setCity(live.getString("city"));
                    result.setTemperature(live.getString("temperature")); // 实时温度
                    result.setWeather(live.getString("weather"));
                    result.setHumidity(live.getString("humidity"));
                    // winddirection 例如 "西北"， windpower 例如 "3级"
                    String windDirection = live.getString("winddirection");
                    String windPower = live.getString("windpower");
                    result.setWind((windDirection != null ? windDirection : "") + "风 " + (windPower != null ? windPower : ""));
                }
            }

            // 3. 获取预报天气 (extensions=all)
            JSONObject forecastJson = callWeatherApi(adcode, apiKey, true);
            if (forecastJson != null) {
                JSONArray forecasts = forecastJson.getJSONArray("forecasts");
                if (forecasts != null && !forecasts.isEmpty()) {
                    JSONObject forecastCity = forecasts.getJSONObject(0);
                    JSONArray casts = forecastCity.getJSONArray("casts");
                    if (casts != null && !casts.isEmpty()) {
                        List<DashboardDTO.WeatherForecast> forecastList = casts.stream()
                                .map(o -> {
                                    JSONObject cast = (JSONObject) o;
                                    DashboardDTO.WeatherForecast f = new DashboardDTO.WeatherForecast();
                                    f.setDate(cast.getString("date"));
                                    f.setTempMin(cast.getString("nighttemp"));
                                    f.setTempMax(cast.getString("daytemp"));
                                    f.setWeather(cast.getString("dayweather"));
                                    return f;
                                })
                                .limit(3) // 未来3天
                                .collect(Collectors.toList());
                        result.setForecast(forecastList);
                        // 如果实时接口失败导致城市为空，用预报城市补充
                        if (result.getCity() == null) {
                            result.setCity(forecastCity.getString("city"));
                        }
                        if (result.getWeather() == null && casts.size() > 0) {
                            result.setWeather(casts.getJSONObject(0).getString("dayweather"));
                        }
                    }
                }
            }

            // 4. 校验最终结果，确保没有虚拟数据
            if (result.getCity() == null || result.getTemperature() == null) {
                log.warn("实时天气关键字段缺失，返回空对象供前端判定失败");
                return new DashboardDTO.WeatherInfo();
            }
            log.info("天气信息获取成功: city={}, temp={}, weather={}, humidity={}, wind={}",
                    result.getCity(), result.getTemperature(), result.getWeather(), result.getHumidity(), result.getWind());
            return result;
        } catch (Exception e) {
            log.error("获取天气信息异常: {}", e.getMessage(), e);
            return new DashboardDTO.WeatherInfo(); // 失败返回空，前端显示加载失败
        }
    }

    /**
     * 通过地理编码接口解析城市 adcode
     */
    private String resolveAdcode(String cityName, String apiKey) {
        try {
            String geoUrl = "https://restapi.amap.com/v3/geocode/geo?address=" +
                    URLEncoder.encode(cityName, StandardCharsets.UTF_8.toString()) +
                    "&key=" + apiKey;
            ResponseEntity<String> resp = restTemplate.getForEntity(geoUrl, String.class);
            String body = resp.getBody();
            log.debug("Geocode response: {}", body);
            if (body != null) {
                JSONObject json = JSON.parseObject(body);
                if ("1".equals(json.getString("status"))) {
                    JSONArray geocodes = json.getJSONArray("geocodes");
                    if (geocodes != null && !geocodes.isEmpty()) {
                        return geocodes.getJSONObject(0).getString("adcode");
                    }
                } else {
                    log.warn("地理编码接口调用失败: info={}", json.getString("info"));
                }
            }
        } catch (Exception ex) {
            log.warn("解析城市 adcode 失败: {}", ex.getMessage());
        }
        return null;
    }

    /**
     * 调用天气接口
     *
     * @param adcode  城市编码
     * @param apiKey  高德Key
     * @param forecast 是否获取预报 (true=预报, false=实时)
     */
    private JSONObject callWeatherApi(String adcode, String apiKey, boolean forecast) {
        String url = "https://restapi.amap.com/v3/weather/weatherInfo?city=" + adcode +
                "&key=" + apiKey + (forecast ? "&extensions=all" : "&extensions=base");
        int maxRetry = 2;
        for (int i = 0; i <= maxRetry; i++) {
            try {
                ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
                String body = resp.getBody();
                log.debug("Weather API ({} attempt {}) response: {}", forecast ? "forecast" : "live", i + 1, body);
                if (body != null) {
                    JSONObject json = JSON.parseObject(body);
                    if ("1".equals(json.getString("status"))) {
                        return json;
                    } else {
                        log.warn("天气接口返回错误: type={}, info={}", forecast ? "forecast" : "live", json.getString("info"));
                        return null; // 不重试业务错误
                    }
                }
            } catch (Exception ex) {
                log.warn("调用天气接口失败 ({} attempt {}): {}", forecast ? "forecast" : "live", i + 1, ex.getMessage());
                if (i == maxRetry) {
                    log.error("天气接口重试失败终止");
                }
            }
        }
        return null;
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

