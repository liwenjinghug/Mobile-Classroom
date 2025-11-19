package com.ruoyi.proj_fz.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.proj_fz.service.IDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/proj_fz/dashboard")
public class DashboardController extends BaseController {

    @Autowired
    private IDashboardService dashboardService;

    /**
     * 获取驾驶舱数据
     */
    @GetMapping("/metrics")
    public AjaxResult getDashboardMetrics() {
        return success(dashboardService.getDashboardData());
    }

    /**
     * 获取天气数据
     */
    @GetMapping("/weather")
    public AjaxResult getWeatherData() {
        return success(dashboardService.getWeatherInfo());
    }

    /**
     * 获取核心指标数据
     */
    @GetMapping("/core-metrics")
    public AjaxResult getCoreMetrics() {
        return success(dashboardService.getCoreMetrics());
    }

    /**
     * 获取图表数据
     */
    @GetMapping("/chart-data")
    public AjaxResult getChartData() {
        return success(dashboardService.getChartData());
    }

    /**
     * 获取待办事项
     */
    @GetMapping("/todos")
    public AjaxResult getTodos() {
        return success(dashboardService.getTodos());
    }

    /**
     * 获取消息列表
     */
    @GetMapping("/messages")
    public AjaxResult getMessages() {
        return success(dashboardService.getMessages());
    }

    /**
     * 获取作业明细（带筛选）
     */
    @PostMapping("/homework-details")
    public AjaxResult getHomeworkDetails(@RequestBody Map<String, Object> params) {
        return success(dashboardService.getHomeworkDetails(params));
    }

    /**
     * 获取公告列表（带筛选）
     */
    @PostMapping("/notices")
    public AjaxResult getNotices(@RequestBody Map<String, Object> params) {
        return success(dashboardService.getNotices(params));
    }

    /**
     * 获取操作日志（带筛选）
     */
    @PostMapping("/operation-logs")
    public AjaxResult getOperationLogs(@RequestBody Map<String, Object> params) {
        return success(dashboardService.getOperationLogs(params));
    }

    /**
     * 根据状态筛选作业
     */
    @GetMapping("/homework-by-status/{status}")
    public AjaxResult getHomeworkByStatus(@PathVariable String status) {
        return success(dashboardService.getHomeworkByStatus(status));
    }

    /**
     * 获取考试统计
     */
    @GetMapping("/exam-statistics")
    public AjaxResult getExamStatistics() {
        return success(dashboardService.getExamStatistics());
    }

    /**
     * 获取学生活跃度统计
     */
    @GetMapping("/student-activity")
    public AjaxResult getStudentActivity() {
        return success(dashboardService.getStudentActivity());
    }
}