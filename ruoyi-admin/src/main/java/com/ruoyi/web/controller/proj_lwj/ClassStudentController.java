package com.ruoyi.web.controller.proj_lwj;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.proj_lwj.domain.ClassStudent;
import com.ruoyi.proj_lwj.service.IClassStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/proj_lw/class_student")
public class ClassStudentController {

    @Autowired
    private IClassStudentService classStudentService;

    @GetMapping("/listByCourseAndSession")
    public AjaxResult listByCourseAndSession(@RequestParam String courseCode, @RequestParam Long sessionId) {
        List<ClassStudent> list = classStudentService.selectByCourseCodeAndSessionId(courseCode, sessionId);
        return AjaxResult.success(list);
    }
}

