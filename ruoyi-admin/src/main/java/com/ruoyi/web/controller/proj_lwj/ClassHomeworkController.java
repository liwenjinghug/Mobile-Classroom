package com.ruoyi.web.controller.proj_lwj;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.proj_lwj.domain.ClassHomework;
import com.ruoyi.proj_lwj.domain.ClassStudentHomework;
import com.ruoyi.proj_lwj.service.IClassHomeworkService;
import com.ruoyi.proj_lwj.service.IClassStudentHomeworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/proj_lwj/homework")
public class ClassHomeworkController extends BaseController {

    @Autowired
    private IClassHomeworkService homeworkService;

    @Autowired
    private IClassStudentHomeworkService studentHomeworkService;

    @PreAuthorize("@ss.hasPermi('projlwj:homework:list')")
    @GetMapping("/list")
    public TableDataInfo list(com.ruoyi.proj_lwj.domain.ClassHomework hw) {
        startPage();
        java.util.List<com.ruoyi.proj_lwj.domain.ClassHomework> list = homeworkService.selectHomeworkList(hw);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('projlwj:homework:query')")
    @GetMapping("/{homeworkId}")
    public AjaxResult getInfo(@PathVariable Long homeworkId) {
        return AjaxResult.success(homeworkService.selectHomeworkById(homeworkId));
    }

    @PreAuthorize("@ss.hasPermi('projlwj:homework:add')")
    @Log(title = "作业管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody com.ruoyi.proj_lwj.domain.ClassHomework hw) {
        hw.setCreateBy(getUsername());
        try {
            // log incoming payload for debugging
            logger.info("添加作业请求 payload: {}", hw);
            int r = homeworkService.addHomework(hw);
            // TODO: 自动为班级学生生成 class_student_homework 记录（根据 session 或 course 关联学生）
            return toAjax(r);
        } catch (Exception e) {
            logger.error("添加作业失败", e);
            return AjaxResult.error("发布作业失败：" + e.getMessage());
        }
    }

    @PreAuthorize("@ss.hasPermi('projlwj:homework:edit')")
    @Log(title = "作业管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody com.ruoyi.proj_lwj.domain.ClassHomework hw) {
        hw.setUpdateBy(getUsername());
        return toAjax(homeworkService.editHomework(hw));
    }

    @PreAuthorize("@ss.hasPermi('projlwj:homework:remove')")
    @Log(title = "作业管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(homeworkService.removeHomeworkByIds(ids));
    }

    // 学生提交作业
    @PreAuthorize("@ss.hasPermi('projlwj:homework:submit')")
    @Log(title = "作业提交", businessType = BusinessType.INSERT)
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody ClassStudentHomework shw) {
        shw.setCreateBy(getUsername());
        // 如果前端没有传 studentId 或传的是空/无效，使用当前登录用户ID（确保与 sys_user.user_id 对应）
        if (shw.getStudentId() == null || shw.getStudentId() == 0L) {
            shw.setStudentId(getUserId());
        }
        // 保证提交时间与状态由后端设置，前端可不传
        if (shw.getSubmitTime() == null) {
            shw.setSubmitTime(new java.util.Date());
        }
        if (shw.getStatus() == null) {
            shw.setStatus(1); // 1=已提交
        }
        int r = studentHomeworkService.insert(shw);
        return toAjax(r);
    }

    // 更新学生已提交的作业（允许学生修改自己的提交）
    @PreAuthorize("@ss.hasPermi('projlwj:homework:submit')")
    @Log(title = "作业提交", businessType = BusinessType.UPDATE)
    @PutMapping("/submit")
    public AjaxResult updateSubmit(@RequestBody ClassStudentHomework shw) {
        // require studentHomeworkId to update
        if (shw.getStudentHomeworkId() == null) {
            return AjaxResult.error("studentHomeworkId 不能为空，无法更新提交记录");
        }
        shw.setUpdateBy(getUsername());
        // update submitTime to now if not provided
        if (shw.getSubmitTime() == null) {
            shw.setSubmitTime(new java.util.Date());
        }
        int r = studentHomeworkService.update(shw);
        return toAjax(r);
    }

    // 查看某作业的提交情况
    @PreAuthorize("@ss.hasPermi('projlwj:homework:query')")
    @GetMapping("/submissions/{homeworkId}")
    public AjaxResult submissions(@PathVariable Long homeworkId) {
        List<ClassStudentHomework> list = studentHomeworkService.selectByHomeworkId(homeworkId);
        return AjaxResult.success(list);
    }

    // 查看某学生的提交记录（通过学号 studentId）
    @PreAuthorize("@ss.hasPermi('projlwj:homework:query')")
    @GetMapping("/studentSubmissions")
    public AjaxResult studentSubmissions(@RequestParam Long studentId) {
        List<ClassStudentHomework> list = studentHomeworkService.selectByStudentId(studentId);
        return AjaxResult.success(list);
    }

    // 公共接口：某学生的提交记录（无需特殊权限），供前端学生确认学号使用
    @GetMapping("/studentSubmissions/public")
    public AjaxResult publicStudentSubmissions(@RequestParam Long studentId) {
        List<ClassStudentHomework> list = studentHomeworkService.selectByStudentId(studentId);
        return AjaxResult.success(list);
    }
}
