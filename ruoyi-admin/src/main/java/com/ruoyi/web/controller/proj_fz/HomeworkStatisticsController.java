package com.ruoyi.web.controller.proj_fz;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.proj_fz.domain.HomeworkStatisticsDTO;
import com.ruoyi.proj_fz.service.IHomeworkStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/proj_fz/homeworkStatistics")
public class HomeworkStatisticsController extends BaseController {

    @Autowired
    private IHomeworkStatisticsService homeworkStatisticsService;

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
    @GetMapping("/export")
    public void exportHomeworkData(@RequestParam(required = false) Long courseId,
                                   @RequestParam(required = false) Long sessionId,
                                   HttpServletResponse response) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (courseId != null) {
                params.put("courseId", courseId);
            }
            if (sessionId != null) {
                params.put("sessionId", sessionId);
            }
            homeworkStatisticsService.exportHomeworkData(params, response);
        } catch (Exception e) {
            logger.error("导出作业数据失败", e);
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
}