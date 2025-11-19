package com.ruoyi.proj_fz.service.impl;

import com.ruoyi.proj_fz.domain.DashboardDTO;
import com.ruoyi.proj_fz.mapper.DashboardMapper;
import com.ruoyi.proj_fz.service.IDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DashboardServiceImpl implements IDashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public DashboardDTO getDashboardData() {
        DashboardDTO dashboard = new DashboardDTO();

        // 设置天气信息
        dashboard.setWeather(getWeatherInfo());

        // 设置核心指标
        dashboard.setCoreMetrics(getCoreMetrics());

        // 设置图表数据
        dashboard.setChartData(getChartData());

        return dashboard;
    }

    @Override
    public DashboardDTO.WeatherInfo getWeatherInfo() {
        DashboardDTO.WeatherInfo weatherInfo = new DashboardDTO.WeatherInfo();
        try {
            // 使用高德地图天气API
            String apiKey = "3ae6faeef4eb83bb9c4881d9ec2d12cf";
            String city = "成都";

            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8.toString());
            String url = "https://restapi.amap.com/v3/weather/weatherInfo?city=" + encodedCity + "&key=" + apiKey + "&extensions=all";

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String responseBody = response.getBody();

            if (responseBody != null) {
                JSONObject jsonResponse = JSON.parseObject(responseBody);

                if ("1".equals(jsonResponse.getString("status"))) {
                    // 当前天气
                    JSONObject lives = jsonResponse.getJSONArray("lives").getJSONObject(0);
                    weatherInfo.setCity(lives.getString("city"));
                    weatherInfo.setTemperature(lives.getString("temperature") + "°C");
                    weatherInfo.setWeather(lives.getString("weather"));
                    weatherInfo.setHumidity(lives.getString("humidity") + "%");
                    weatherInfo.setWind(lives.getString("winddirection") + "风 " + lives.getString("windpower") + "级");

                    // 天气预报
                    List<DashboardDTO.WeatherForecast> forecastList = new ArrayList<>();
                    if (jsonResponse.getJSONArray("forecasts") != null) {
                        JSONObject forecast = jsonResponse.getJSONArray("forecasts").getJSONObject(0);
                        if (forecast.getJSONArray("casts") != null) {
                            for (int i = 0; i < Math.min(3, forecast.getJSONArray("casts").size()); i++) {
                                JSONObject cast = forecast.getJSONArray("casts").getJSONObject(i);
                                DashboardDTO.WeatherForecast weatherForecast = new DashboardDTO.WeatherForecast();
                                weatherForecast.setDate(cast.getString("date"));
                                weatherForecast.setTempMin(cast.getString("nighttemp"));
                                weatherForecast.setTempMax(cast.getString("daytemp"));
                                weatherForecast.setWeather(cast.getString("dayweather"));
                                forecastList.add(weatherForecast);
                            }
                        }
                    }
                    weatherInfo.setForecast(forecastList);
                } else {
                    setDefaultWeather(weatherInfo);
                }
            } else {
                setDefaultWeather(weatherInfo);
            }
        } catch (Exception e) {
            setDefaultWeather(weatherInfo);
        }
        return weatherInfo;
    }

    @Override
    public DashboardDTO.CoreMetrics getCoreMetrics() {
        DashboardDTO.CoreMetrics coreMetrics = new DashboardDTO.CoreMetrics();

        // 获取课程相关指标
        coreMetrics.setTotalCourses(dashboardMapper.countTotalCourses());
        coreMetrics.setActiveClasses(dashboardMapper.countActiveSessions());
        coreMetrics.setTodayDeadline(dashboardMapper.countTodayDeadline());
        coreMetrics.setOngoingExams(dashboardMapper.countOngoingExams());
        coreMetrics.setPendingTodos(dashboardMapper.countPendingTodos());
        coreMetrics.setGradedCount(dashboardMapper.countGradedAssignments());

        // 计算批改率
        int totalAssignments = dashboardMapper.countTotalAssignments();
        int gradedCount = dashboardMapper.countGradedAssignments();
        long gradingRate = totalAssignments > 0 ? Math.round((gradedCount * 100.0) / totalAssignments) : 0;
        coreMetrics.setGradingRate((int) gradingRate);

        // 设置趋势数据（这里使用随机数据模拟，实际应该从数据库计算）
        Random random = new Random();
        coreMetrics.setCourseTrend(random.nextInt(10) - 2);
        coreMetrics.setClassTrend(random.nextInt(8) - 1);
        coreMetrics.setHomeworkTrend(random.nextInt(15) - 5);
        coreMetrics.setExamTrend(random.nextInt(5) - 1);
        coreMetrics.setTodoTrend(random.nextInt(20) - 10);
        coreMetrics.setGradingTrend(random.nextInt(12) - 3);

        return coreMetrics;
    }

    @Override
    public DashboardDTO.ChartData getChartData() {
        DashboardDTO.ChartData chartData = new DashboardDTO.ChartData();

        // 提交趋势数据
        List<DashboardDTO.TrendData> trendData = dashboardMapper.getSubmissionTrend(7);
        chartData.setTrendData(trendData);

        // 提交状态数据
        DashboardDTO.SubmissionStatus submissionStatus = new DashboardDTO.SubmissionStatus();
        int totalAssignments = dashboardMapper.countTotalAssignments();
        int submittedCount = dashboardMapper.countSubmittedAssignments();
        int gradedCount = dashboardMapper.countGradedAssignments();

        submissionStatus.setSubmitted(submittedCount);
        submissionStatus.setPending(totalAssignments - submittedCount);
        submissionStatus.setGraded(gradedCount);
        chartData.setSubmissionStatus(submissionStatus);

        // 成绩统计数据
        DashboardDTO.ScoreStats scoreStats = new DashboardDTO.ScoreStats();
        Double avgScore = dashboardMapper.getAverageScore();
        scoreStats.setAverage(avgScore != null ? Math.round(avgScore * 100.0) / 100.0 : 0.0);

        Double maxScore = dashboardMapper.getMaxScore();
        scoreStats.setMax(maxScore != null ? Math.round(maxScore * 100.0) / 100.0 : 0.0);

        int passCount = dashboardMapper.countPassAssignments();
        long passRate = submittedCount > 0 ? Math.round((passCount * 100.0) / submittedCount) : 0;
        scoreStats.setPassRate((int) passRate);
        chartData.setScoreStats(scoreStats);

        // 成绩分布数据
        List<DashboardDTO.ScoreDistribution> scoreDistribution = dashboardMapper.getScoreDistribution();
        chartData.setScoreDistribution(scoreDistribution);

        // 课程平均分对比
        List<DashboardDTO.CourseScore> courseScores = dashboardMapper.getCourseScores();
        chartData.setCourseScores(courseScores);

        // 签到率数据
        List<DashboardDTO.AttendanceRate> attendanceRates = dashboardMapper.getAttendanceRates();
        chartData.setAttendanceRates(attendanceRates);

        // 参与热力图数据
        List<DashboardDTO.ParticipationHeat> participationHeat = dashboardMapper.getParticipationHeat();
        chartData.setParticipationHeat(participationHeat);

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

    private void setDefaultWeather(DashboardDTO.WeatherInfo weatherInfo) {
        weatherInfo.setCity("成都");
        weatherInfo.setTemperature("25°C");
        weatherInfo.setWeather("晴");
        weatherInfo.setHumidity("65%");
        weatherInfo.setWind("东南风 3级");

        // 设置默认天气预报
        List<DashboardDTO.WeatherForecast> forecastList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 0; i < 3; i++) {
            DashboardDTO.WeatherForecast forecast = new DashboardDTO.WeatherForecast();
            forecast.setDate(LocalDate.now().plusDays(i).format(formatter));
            forecast.setTempMin("20");
            forecast.setTempMax("28");
            forecast.setWeather("晴");
            forecastList.add(forecast);
        }
        weatherInfo.setForecast(forecastList);
    }
}