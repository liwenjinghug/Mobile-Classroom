package com.ruoyi.web.controller.proj_fz;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.proj_fz.domain.HomeworkStatisticsDTO;
import com.ruoyi.proj_fz.service.IHomeworkStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// 【核心修改】添加 import
import com.ruoyi.proj_cyq.annotation.Log;
import com.ruoyi.common.enums.BusinessType;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/proj_fz/homeworkStatistics")
public class HomeworkStatisticsController extends BaseController {

    @Autowired
    private IHomeworkStatisticsService homeworkStatisticsService;

    // ... (保留查询类方法) ...

    /**
     * 获取作业统计列表
     */
    @GetMapping("/list")
    public AjaxResult list() {
        try {
            List<HomeworkStatisticsDTO> list = homeworkStatisticsService.getHomeworkStatisticsList();
            return AjaxResult.success("查询成功", list);
        } catch (Exception e) {
            logger.error("查询作业统计列表失败", e);
            return AjaxResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据条件获取作业统计列表
     */
    @GetMapping("/listByFilter")
    public AjaxResult listByFilter(@RequestParam(required = false) Long courseId,
                                   @RequestParam(required = false) Long sessionId) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (courseId != null) {
                params.put("courseId", courseId);
            }
            if (sessionId != null) {
                params.put("sessionId", sessionId);
            }
            List<HomeworkStatisticsDTO> list = homeworkStatisticsService.getHomeworkStatisticsListByFilter(params);
            return AjaxResult.success("查询成功", list);
        } catch (Exception e) {
            logger.error("根据条件查询作业统计列表失败", e);
            return AjaxResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取作业详细统计
     */
    @GetMapping("/{homeworkId}")
    public AjaxResult getStatistics(@PathVariable Long homeworkId) {
        try {
            HomeworkStatisticsDTO statistics = homeworkStatisticsService.getHomeworkStatisticsById(homeworkId);
            return AjaxResult.success("查询成功", statistics);
        } catch (Exception e) {
            logger.error("查询作业详细统计失败", e);
            return AjaxResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取成绩分布
     */
    @GetMapping("/scoreDistribution/{homeworkId}")
    public AjaxResult getScoreDistribution(@PathVariable Long homeworkId) {
        try {
            List<Map<String, Object>> distribution = homeworkStatisticsService.getScoreDistribution(homeworkId);
            return AjaxResult.success("查询成功", distribution);
        } catch (Exception e) {
            logger.error("查询成绩分布失败", e);
            return AjaxResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取提交趋势
     */
    @GetMapping("/submissionTrend/{homeworkId}")
    public AjaxResult getSubmissionTrend(@PathVariable Long homeworkId) {
        try {
            List<Map<String, Object>> trend = homeworkStatisticsService.getSubmissionTrend(homeworkId);
            return AjaxResult.success("查询成功", trend);
        } catch (Exception e) {
            logger.error("查询提交趋势失败", e);
            return AjaxResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取教师作业概览
     */
    @GetMapping("/teacherOverview")
    public AjaxResult getOverview() {
        try {
            List<Map<String, Object>> overview = homeworkStatisticsService.getTeacherHomeworkOverview();
            return AjaxResult.success("查询成功", overview);
        } catch (Exception e) {
            logger.error("查询教师作业概览失败", e);
            return AjaxResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生提交详情列表
     */
    @GetMapping("/studentSubmissions/{homeworkId}")
    public AjaxResult getStudentSubmissionDetails(@PathVariable Long homeworkId) {
        try {
            List<Map<String, Object>> details = homeworkStatisticsService.getStudentSubmissionDetails(homeworkId);
            return AjaxResult.success("查询成功", details);
        } catch (Exception e) {
            logger.error("查询学生提交详情失败", e);
            return AjaxResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取看板概览数据
     */
    @GetMapping("/dashboardOverview")
    public AjaxResult getDashboardOverview() {
        try {
            Map<String, Object> overview = homeworkStatisticsService.getDashboardOverview();
            return AjaxResult.success("查询成功", overview);
        } catch (Exception e) {
            logger.error("查询看板概览失败", e);
            return AjaxResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取课程列表
     */
    @GetMapping("/courseList")
    public AjaxResult getCourseList() {
        try {
            List<Map<String, Object>> courses = homeworkStatisticsService.getCourseList();
            return AjaxResult.success("查询成功", courses);
        } catch (Exception e) {
            logger.error("查询课程列表失败", e);
            return AjaxResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取课堂列表
     */
    @GetMapping("/sessionList")
    public AjaxResult getSessionList() {
        try {
            List<Map<String, Object>> sessions = homeworkStatisticsService.getSessionList();
            return AjaxResult.success("查询成功", sessions);
        } catch (Exception e) {
            logger.error("查询课堂列表失败", e);
            return AjaxResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取课堂作业概览
     */
    @GetMapping("/sessionOverview")
    public AjaxResult getSessionOverview() {
        try {
            List<Map<String, Object>> overview = homeworkStatisticsService.getSessionHomeworkOverview();
            return AjaxResult.success("查询成功", overview);
        } catch (Exception e) {
            logger.error("查询课堂作业概览失败", e);
            return AjaxResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 导出作业数据
     */
    // 【核心修改】添加 Log 注解
    @Log(title = "作业统计", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void exportHomeworkData(@RequestParam(required = false) Long courseId,
                                   @RequestParam(required = false) Long sessionId,
                                   @RequestParam(required = false) String createTimeStart,
                                   @RequestParam(required = false) String createTimeEnd,
                                   @RequestParam(required = false) String deadlineStart,
                                   @RequestParam(required = false) String deadlineEnd,
                                   @RequestParam(required = false) String expireStatus,
                                   @RequestParam(required = false) String gradeStatus,
                                   @RequestParam(required = false) String completionStatus,
                                   HttpServletResponse response) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (courseId != null) params.put("courseId", courseId);
            if (sessionId != null) params.put("sessionId", sessionId);
            if (createTimeStart != null && !createTimeStart.isEmpty()) params.put("createTimeStart", createTimeStart);
            if (createTimeEnd != null && !createTimeEnd.isEmpty()) params.put("createTimeEnd", createTimeEnd);
            if (deadlineStart != null && !deadlineStart.isEmpty()) params.put("deadlineStart", deadlineStart);
            if (deadlineEnd != null && !deadlineEnd.isEmpty()) params.put("deadlineEnd", deadlineEnd);
            if (expireStatus != null && !expireStatus.isEmpty()) params.put("expireStatus", expireStatus);
            if (gradeStatus != null && !gradeStatus.isEmpty()) params.put("gradeStatus", gradeStatus);
            if (completionStatus != null && !completionStatus.isEmpty()) params.put("completionStatus", completionStatus);

            logger.info("导出作业数据参数: {}", params);
            homeworkStatisticsService.exportHomeworkData(params, response);
        } catch (Exception e) {
            logger.error("导出作业数据失败", e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    /**
     * 调试接口：获取所有作业基本信息
     */
    @GetMapping("/debug/homeworkInfo")
    public AjaxResult getDebugHomeworkInfo() {
        try {
            List<Map<String, Object>> homeworkInfo = homeworkStatisticsService.getAllHomeworkBasicInfo();
            List<Map<String, Object>> submissionStats = homeworkStatisticsService.getHomeworkSubmissionStats();

            Map<String, Object> result = new HashMap<>();
            result.put("homeworkInfo", homeworkInfo);
            result.put("submissionStats", submissionStats);

            return AjaxResult.success("调试信息查询成功", result);
        } catch (Exception e) {
            logger.error("查询调试信息失败", e);
            return AjaxResult.error("查询调试信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取今日截止作业数
     */
    @GetMapping("/todayDeadlineCount")
    public AjaxResult getTodayDeadlineCount() {
        try {
            Integer count = homeworkStatisticsService.getTodayDeadlineCount();
            return AjaxResult.success("查询成功", count);
        } catch (Exception e) {
            logger.error("查询今日截止作业数失败", e);
            return AjaxResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据高级筛选条件获取作业统计列表
     */
    @GetMapping("/listByAdvancedFilter")
    public AjaxResult listByAdvancedFilter(@RequestParam(required = false) Long courseId,
                                           @RequestParam(required = false) Long sessionId,
                                           @RequestParam(required = false) String homeworkTitle,  // 确保这个参数存在
                                           @RequestParam(required = false) String createTimeStart,
                                           @RequestParam(required = false) String createTimeEnd,
                                           @RequestParam(required = false) String deadlineStart,
                                           @RequestParam(required = false) String deadlineEnd,
                                           @RequestParam(required = false) String expireStatus,
                                           @RequestParam(required = false) String gradeStatus,
                                           @RequestParam(required = false) String completionStatus) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (courseId != null) params.put("courseId", courseId);
            if (sessionId != null) params.put("sessionId", sessionId);
            if (homeworkTitle != null && !homeworkTitle.isEmpty()) params.put("homeworkTitle", homeworkTitle);
            if (createTimeStart != null && !createTimeStart.isEmpty()) params.put("createTimeStart", createTimeStart);
            if (createTimeEnd != null && !createTimeEnd.isEmpty()) params.put("createTimeEnd", createTimeEnd);
            if (deadlineStart != null && !deadlineStart.isEmpty()) params.put("deadlineStart", deadlineStart);
            if (deadlineEnd != null && !deadlineEnd.isEmpty()) params.put("deadlineEnd", deadlineEnd);
            if (expireStatus != null && !expireStatus.isEmpty()) params.put("expireStatus", expireStatus);
            if (gradeStatus != null && !gradeStatus.isEmpty()) params.put("gradeStatus", gradeStatus);
            if (completionStatus != null && !completionStatus.isEmpty()) params.put("completionStatus", completionStatus);

            logger.info("高级筛选参数: {}", params);
            List<HomeworkStatisticsDTO> list = homeworkStatisticsService.getHomeworkStatisticsListByAdvancedFilter(params);
            return AjaxResult.success("查询成功", list);
        } catch (Exception e) {
            logger.error("根据高级筛选条件查询作业统计列表失败", e);
            return AjaxResult.error("查询失败: " + e.getMessage());
        }
    }
}