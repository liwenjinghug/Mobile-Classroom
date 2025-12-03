package com.ruoyi.web.controller.proj_fz;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.proj_lwj.domain.ClassHomework;
import com.ruoyi.proj_lwj.domain.ClassStudentHomework;
import com.ruoyi.proj_lwj.service.IClassHomeworkService;
import com.ruoyi.proj_lwj.service.IClassStudentHomeworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生作业Controller - 无权限限制版本
 * 专门给小程序学生端使用
 *
 * @author proj_fz
 */
@RestController
@RequestMapping("/proj_fz/homework")
public class HomeworkController extends BaseController {

    @Autowired
    private IClassHomeworkService homeworkService;

    @Autowired
    private IClassStudentHomeworkService studentHomeworkService;

    /**
     * 获取作业列表（学生端）
     */
    @GetMapping("/list")
    public TableDataInfo list(ClassHomework hw) {
        startPage();
        List<ClassHomework> list = homeworkService.selectHomeworkList(hw);
        return getDataTable(list);
    }

    /**
     * 获取作业详情（学生端）
     */
    @GetMapping("/{homeworkId}")
    public AjaxResult getInfo(@PathVariable Long homeworkId) {
        ClassHomework homework = homeworkService.selectHomeworkById(homeworkId);
        return AjaxResult.success(homework);
    }

    /**
     * 获取我的作业提交记录
     */
    @GetMapping("/my-submissions")
    public AjaxResult getMySubmissions() {
        Long studentId = getUserId();
        List<ClassStudentHomework> list = studentHomeworkService.selectByStudentId(studentId);
        return AjaxResult.success(list);
    }

    /**
     * 提交作业（学生端）
     */
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody ClassStudentHomework shw) {
        shw.setCreateBy(getUsername());

        if (shw.getHomeworkId() == null) {
            return AjaxResult.error("作业ID不能为空");
        }

        // 自动设置学生ID
        if (shw.getStudentId() == null || shw.getStudentId() == 0L) {
            shw.setStudentId(getUserId());
        }

        // 设置提交时间和状态
        if (shw.getSubmitTime() == null) {
            shw.setSubmitTime(new java.util.Date());
        }
        if (shw.getStatus() == null) {
            shw.setStatus(1); // 1=已提交
        }

        // 检查是否已有提交
        List<ClassStudentHomework> existing = studentHomeworkService.selectByStudentId(shw.getStudentId());
        ClassStudentHomework existingSubmission = null;
        if (existing != null && !existing.isEmpty()) {
            for (ClassStudentHomework e : existing) {
                if (e.getHomeworkId() != null && e.getHomeworkId().equals(shw.getHomeworkId())) {
                    existingSubmission = e;
                    break;
                }
            }
        }

        int r;
        if (existingSubmission != null) {
            // 更新提交
            shw.setStudentHomeworkId(existingSubmission.getStudentHomeworkId());
            shw.setUpdateBy(getUsername());
            shw.setUpdateTime(new java.util.Date());
            r = studentHomeworkService.update(shw);
        } else {
            // 新增提交
            r = studentHomeworkService.insert(shw);
        }

        return toAjax(r);
    }
}

