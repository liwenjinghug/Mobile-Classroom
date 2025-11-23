package com.ruoyi.web.controller.proj_lwj;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.proj_lwj.domain.ClassStudent;
import com.ruoyi.proj_lwj.service.IClassStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/proj_lw/class_student")
public class ClassStudentController {

    @Autowired
    private IClassStudentService classStudentService;

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
