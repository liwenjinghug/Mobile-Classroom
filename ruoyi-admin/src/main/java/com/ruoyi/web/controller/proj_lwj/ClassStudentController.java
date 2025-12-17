package com.ruoyi.web.controller.proj_lwj;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.proj_lwj.domain.ClassStudent;
import com.ruoyi.proj_lwj.service.IClassStudentService;
import com.ruoyi.proj_lwj.mapper.ClassStudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/proj_lw/student/class")
public class ClassStudentController extends BaseController {

    @Autowired
    private IClassStudentService classStudentService;

    @Autowired
    private ClassStudentMapper classStudentMapper;

    /**
     * 获取当前登录用户的学生信息
     */
    @GetMapping("/current-student")
    public AjaxResult getCurrentStudent() {
        try {
            String username = getUsername();
            Long userId = getUserId();

            logger.info("getCurrentStudent called for userId={}, username={}", userId, username);

            // 优先通过userId查找class_student表
            ClassStudent student = classStudentMapper.selectByUserId(userId);

            // 如果通过userId找不到，再尝试通过username查找
            if (student == null) {
                student = classStudentMapper.selectByStudentNo(username);
            }

            if (student != null) {
                return AjaxResult.success(student);
            } else {
                // 如果找不到，返回一个包含username的基本信息
                Map<String, Object> data = new HashMap<>();
                data.put("studentNo", username);
                data.put("userId", userId);
                return AjaxResult.success(data);
            }
        } catch (Exception e) {
            logger.error("Failed to get current student", e);
            return AjaxResult.error("获取学生信息失败: " + e.getMessage());
        }
    }

    // 根据课堂 ID 查询该课堂下所有学生
    @GetMapping("/bySession")
    public AjaxResult listBySession(@RequestParam Long sessionId) {
        List<ClassStudent> list = classStudentService.selectBySessionId(sessionId);
        return AjaxResult.success(list);
    }

    // 根据课程 ID 查询该课程下所有课堂 ID 列表
    @GetMapping("/sessionsByCourse")
    public AjaxResult sessionsByCourse(@RequestParam Long courseId) {
        List<Long> sessionIds = classStudentService.selectSessionIdsByCourseId(courseId);
        return AjaxResult.success(sessionIds);
    }

    // 根据课程 ID 查询该课程下所有去重后的学生
    @GetMapping("/studentsByCourse")
    public AjaxResult studentsByCourse(@RequestParam Long courseId) {
        List<ClassStudent> students = classStudentService.selectDistinctStudentsByCourseId(courseId);
        return AjaxResult.success(students);
    }

    // 综合接口：根据课程 ID 同时返回课堂列表和学生列表，便于作业/ 考试模块使用
    @GetMapping("/overviewByCourse")
    public AjaxResult overviewByCourse(@RequestParam Long courseId) {
        List<Long> sessionIds = classStudentService.selectSessionIdsByCourseId(courseId);
        List<ClassStudent> students = classStudentService.selectDistinctStudentsByCourseId(courseId);
        Map<String, Object> data = new HashMap<>();
        data.put("sessionIds", sessionIds);
        data.put("students", students);
        return AjaxResult.success(data);
    }
}
