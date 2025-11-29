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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
     * 导出课程列表 - CSV格式
     */
    @PreAuthorize("@ss.hasPermi('projlw:course:export')")
    @Log(title = "课程管理", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(Course course, HttpServletResponse response) {
        try {
            List<Course> list = courseService.selectCourseList(course);
            System.out.println("导出数据条数: " + list.size());

            // 检查数据是否为空
            if (list.isEmpty()) {
                throw new Exception("没有数据可导出");
            }

            // 设置响应头 - CSV格式
            String filename = "课程数据.csv";
            String encodedFilename = java.net.URLEncoder.encode(filename, "UTF-8")
                    .replaceAll("\\+", "%20");

            response.setContentType("text/csv; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + encodedFilename);

            // 添加BOM头，解决中文乱码问题
            response.getOutputStream().write(0xEF);
            response.getOutputStream().write(0xBB);
            response.getOutputStream().write(0xBF);

            // 生成CSV内容
            StringBuilder csvContent = new StringBuilder();

            // 添加表头
            csvContent.append("课程ID,课程名称,课程编号,课程类型,所属学院,学分,课程简介,状态\n");

            // 添加数据行
            for (Course item : list) {
                csvContent.append(escapeCsvField(item.getCourseId() != null ? item.getCourseId().toString() : "")).append(",");
                csvContent.append(escapeCsvField(item.getCourseName())).append(",");
                csvContent.append(escapeCsvField(item.getCourseCode())).append(",");
                csvContent.append(escapeCsvField(item.getCourseType())).append(",");
                csvContent.append(escapeCsvField(item.getCollege())).append(",");
                csvContent.append(escapeCsvField(item.getCredit() != null ? item.getCredit().toString() : "")).append(",");
                csvContent.append(escapeCsvField(item.getIntroduction())).append(",");
                csvContent.append(escapeCsvField("0".equals(item.getStatus()) ? "正常" : "停授")).append("\n");
            }

            // 写入响应
            response.getWriter().write(csvContent.toString());
            response.getWriter().flush();

            System.out.println("CSV导出完成");

        } catch (Exception e) {
            System.err.println("导出异常: " + e.getMessage());
            e.printStackTrace();
            try {
                response.reset();
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"code\":500,\"msg\":\"导出失败: " + e.getMessage().replace("\"", "'") + "\"}");
            } catch (IOException ex) {
                System.err.println("写入错误响应失败: " + ex.getMessage());
            }
        }
    }

    /**
     * CSV字段转义处理
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        // 如果字段包含逗号、换行符或双引号，需要用双引号包围并转义内部的双引号
        if (field.contains(",") || field.contains("\n") || field.contains("\"") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
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