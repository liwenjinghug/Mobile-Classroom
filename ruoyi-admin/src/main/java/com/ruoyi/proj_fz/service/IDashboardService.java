package com.ruoyi.proj_fz.service;

import com.ruoyi.proj_fz.domain.DashboardDTO;

import java.util.List;
import java.util.Map;

public interface IDashboardService {

    /**
     * 获取驾驶舱数据
     */
    DashboardDTO getDashboardData();

    /**
     * 获取天气信息
     */
    DashboardDTO.WeatherInfo getWeatherInfo();

    /**
     * 获取核心指标数据
     */
    DashboardDTO.CoreMetrics getCoreMetrics();

    /**
     * 获取图表数据
     */
    DashboardDTO.ChartData getChartData();

    /**
     * 获取待办事项
     */
    List<Map<String, Object>> getTodos();

    /**
     * 获取消息列表
     */
    List<Map<String, Object>> getMessages();

    /**
     * 获取作业明细（带筛选）
     */
    List<Map<String, Object>> getHomeworkDetails(Map<String, Object> params);

    /**
     * 获取公告列表（带筛选）
     */
    List<Map<String, Object>> getNotices(Map<String, Object> params);

    /**
     * 获取操作日志（带筛选）
     */
    List<Map<String, Object>> getOperationLogs(Map<String, Object> params);

    /**
     * 根据状态筛选作业
     */
    List<Map<String, Object>> getHomeworkByStatus(String status);

    /**
     * 获取考试统计
     */
    Map<String, Object> getExamStatistics();

    /**
     * 获取学生活跃度统计
     */
    Map<String, Object> getStudentActivity();
}