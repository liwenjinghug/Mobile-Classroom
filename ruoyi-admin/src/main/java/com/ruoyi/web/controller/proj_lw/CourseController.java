package com.ruoyi.web.controller.proj_lw;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.proj_lw.domain.Course;
import com.ruoyi.proj_lw.service.ICourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/proj_lw/course")
public class CourseController extends BaseController {

    @Autowired
    private ICourseService courseService;

    /**
     * 查询课程列表
     */
    @PreAuthorize("@ss.hasPermi('projlw:course:list')")  // 权限标识改为projlw
    @GetMapping("/list")
    public TableDataInfo list(Course course) {
        startPage();
        List<Course> list = courseService.selectCourseList(course);
        return getDataTable(list);
    }

    /**
     * 导出课程列表
     */
    @PreAuthorize("@ss.hasPermi('projlw:course:export')")  // 权限标识改为projlw
    @Log(title = "课程管理", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(Course course) {
        List<Course> list = courseService.selectCourseList(course);
        ExcelUtil<Course> util = new ExcelUtil<Course>(Course.class);
        return util.exportExcel(list, "课程数据");
    }

    /**
     * 获取课程详细信息
     */
    @PreAuthorize("@ss.hasPermi('projlw:course:query')")  // 权限标识改为projlw
    @GetMapping(value = "/{courseId}")
    public AjaxResult getInfo(@PathVariable("courseId") Long courseId) {
        return success(courseService.selectCourseById(courseId));  // 使用success方法
    }

    /**
     * 新增课程
     */
    @PreAuthorize("@ss.hasPermi('projlw:course:add')")  // 权限标识改为projlw
    @Log(title = "课程管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Course course) {
        if (!"0".equals(courseService.checkCourseCodeUnique(course))) {
            return error("新增课程'" + course.getCourseName() + "'失败，课程编号已存在");  // 使用error方法
        }
        course.setCreateBy(getUsername());
        return toAjax(courseService.insertCourse(course));
    }

    /**
     * 修改课程
     */
    @PreAuthorize("@ss.hasPermi('projlw:course:edit')")  // 权限标识改为projlw
    @Log(title = "课程管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Course course) {
        if (!"0".equals(courseService.checkCourseCodeUnique(course))) {
            return error("修改课程'" + course.getCourseName() + "'失败，课程编号已存在");  // 使用error方法
        }
        course.setUpdateBy(getUsername());
        return toAjax(courseService.updateCourse(course));
    }

    /**
     * 删除课程
     */
    @PreAuthorize("@ss.hasPermi('projlw:course:remove')")  // 权限标识改为projlw
    @Log(title = "课程管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{courseIds}")
    public AjaxResult remove(@PathVariable Long[] courseIds) {
        return toAjax(courseService.deleteCourseByIds(courseIds));
    }
}