package com.ruoyi.web.controller.proj_fz;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.proj_fz.domain.DashboardDTO;
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
     * 获取驾驶舱所有数据
     */
    @GetMapping("/data")
    public AjaxResult getDashboardData() {
        DashboardDTO dashboardData = dashboardService.getDashboardData();
        return AjaxResult.success(dashboardData);
    }

    /**
     * 获取天气信息（真实数据，失败时返回错误）
     */
    @GetMapping("/weather")
    public AjaxResult getWeatherInfo() {
        DashboardDTO.WeatherInfo weatherInfo = dashboardService.getWeatherInfo();
        if (weatherInfo == null || weatherInfo.getCity() == null || weatherInfo.getTemperature() == null) {
            return AjaxResult.error("天气接口调用失败，请确认高德Key的IP白名单或更新有效Key");
        }
        return AjaxResult.success(weatherInfo);
    }

    /**
     * 获取当前天气配置（仅用于前端展示与编辑）
     */
    @GetMapping("/weather/config")
    public AjaxResult getWeatherConfig() {
        return AjaxResult.success(dashboardService.getWeatherConfig());
    }

    /**
     * 更新天气配置（动态更新 Key 和城市/城市编码）
     * body: {"apiKey":"xxx","city":"成都"} 或 {"apiKey":"xxx","adcode":"510100"}
     */
    @PostMapping("/weather/config")
    public AjaxResult updateWeatherConfig(@RequestBody Map<String, String> config) {
        String apiKey = config.get("apiKey");
        String city = config.get("city");
        String adcode = config.get("adcode");
        if ((apiKey == null || apiKey.isEmpty()) && (city == null && adcode == null)) {
            return AjaxResult.error("缺少必要参数");
        }
        dashboardService.updateWeatherConfig(apiKey, city, adcode);
        return AjaxResult.success("更新成功");
    }

    /**
     * 获取核心指标
     */
    @GetMapping("/core-metrics")
    public AjaxResult getCoreMetrics() {
        DashboardDTO.CoreMetrics coreMetrics = dashboardService.getCoreMetrics();
        return AjaxResult.success(coreMetrics);
    }

    /**
     * 获取图表数据
     */
    @GetMapping("/chart-data")
    public AjaxResult getChartData() {
        DashboardDTO.ChartData chartData = dashboardService.getChartData();
        return AjaxResult.success(chartData);
    }

    /**
     * 获取待办事项
     */
    @GetMapping("/todos")
    public AjaxResult getTodos() {
        List<Map<String, Object>> todos = dashboardService.getTodos();
        return AjaxResult.success(todos);
    }

    /**
     * 获取消息列表
     */
    @GetMapping("/messages")
    public AjaxResult getMessages() {
        List<Map<String, Object>> messages = dashboardService.getMessages();
        return AjaxResult.success(messages);
    }

    /**
     * 获取作业明细（带筛选）
     */
    @PostMapping("/homework-details")
    public AjaxResult getHomeworkDetails(@RequestBody Map<String, Object> params) {
        List<Map<String, Object>> homeworkDetails = dashboardService.getHomeworkDetails(params);
        return AjaxResult.success(homeworkDetails);
    }

    /**
     * 获取公告列表（带筛选）
     */
    @PostMapping("/notices")
    public AjaxResult getNotices(@RequestBody Map<String, Object> params) {
        List<Map<String, Object>> notices = dashboardService.getNotices(params);
        return AjaxResult.success(notices);
    }

    /**
     * 获取操作日志（带筛选）
     */
    @PostMapping("/operation-logs")
    public AjaxResult getOperationLogs(@RequestBody Map<String, Object> params) {
        List<Map<String, Object>> operationLogs = dashboardService.getOperationLogs(params);
        return AjaxResult.success(operationLogs);
    }

    /**
     * 根据状态筛选作业
     */
    @GetMapping("/homework-by-status/{status}")
    public AjaxResult getHomeworkByStatus(@PathVariable String status) {
        List<Map<String, Object>> homeworkDetails = dashboardService.getHomeworkByStatus(status);
        return AjaxResult.success(homeworkDetails);
    }

    /**
     * 获取考试统计
     */
    @GetMapping("/exam-statistics")
    public AjaxResult getExamStatistics() {
        Map<String, Object> examStatistics = dashboardService.getExamStatistics();
        return AjaxResult.success(examStatistics);
    }

    /**
     * 获取学生活跃度统计
     */
    @GetMapping("/student-activity")
    public AjaxResult getStudentActivity() {
        Map<String, Object> studentActivity = dashboardService.getStudentActivity();
        return AjaxResult.success(studentActivity);
    }
}