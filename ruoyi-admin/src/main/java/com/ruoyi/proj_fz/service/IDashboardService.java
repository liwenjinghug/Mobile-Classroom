package com.ruoyi.proj_fz.service;

import com.ruoyi.proj_fz.domain.DashboardDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface IDashboardService {

    // ... (原有方法保持不变)

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

    /**
     * 新增：导出作业明细数据
     */
    void exportHomeworkDetails(Map<String, Object> params, HttpServletResponse response);

    /**
     * 新增：导出公告列表数据
     */
    void exportNotices(Map<String, Object> params, HttpServletResponse response);

    /**
     * 新增：导出操作日志数据
     */
    void exportOperationLogs(Map<String, Object> params, HttpServletResponse response);

    /**
     * 获取天气配置
     */
    Object getWeatherConfig();

    /**
     * 更新天气配置
     */
    void updateWeatherConfig(String apiKey, String city, String adcode);

    /**
     * 获取单个作业详情
     */
    Map<String, Object> getHomeworkDetailById(Long homeworkId);

    /**
     * 导出单个作业详情
     */
    void exportSingleHomework(Long homeworkId, HttpServletResponse response);
}
