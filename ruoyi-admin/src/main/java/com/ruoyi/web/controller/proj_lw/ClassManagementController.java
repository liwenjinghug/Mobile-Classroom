package com.ruoyi.web.controller.proj_lw;

// [修改] 使用自定义 Log 注解
import com.ruoyi.proj_cyq.annotation.Log;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.proj_lw.domain.ClassSessionStudent;
import com.ruoyi.proj_lw.domain.ClassStudentLw;
import com.ruoyi.proj_lw.service.IClassManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

/**
 * 课堂管理Controller
 */
@RestController
@RequestMapping("/proj_lw/class/management")
public class ClassManagementController extends BaseController {

    @Autowired
    private IClassManagementService classManagementService;

    /**
     * 获取课堂学生列表
     */
    @GetMapping("/{sessionId}/students")
    public TableDataInfo getClassStudents(
            @PathVariable Long sessionId,
            @RequestParam(required = false) Boolean noPagination,  // 添加不分页标识
            ClassStudentLw query) {
        try {
            System.out.println("=== 获取课堂学生列表 ===");
            System.out.println("课堂ID: " + sessionId);
            System.out.println("不分页标识: " + noPagination);

            // 根据noPagination参数决定是否分页
            if (noPagination == null || !noPagination) {
                startPage();  // 默认分页
            }

            List<ClassSessionStudent> list = classManagementService.getClassStudents(sessionId, query);

            System.out.println("查询到的学生数量: " + (list != null ? list.size() : "null"));

            // 如果不分页，需要手动设置total
            TableDataInfo rspData = new TableDataInfo();
            rspData.setCode(200);
            rspData.setRows(list != null ? list : new ArrayList<>());

            if (noPagination != null && noPagination) {
                // 不分页时，total就是列表大小
                rspData.setTotal(list != null ? list.size() : 0);
            } else {
                // 分页时，getDataTable会处理total
                return getDataTable(list != null ? list : new ArrayList<>());
            }

            return rspData;

        } catch (Exception e) {
            System.err.println("获取课堂学生列表失败: " + e.getMessage());
            e.printStackTrace();
            TableDataInfo rspData = new TableDataInfo();
            rspData.setCode(500);
            rspData.setRows(new ArrayList<>());
            rspData.setTotal(0);
            return rspData;
        }
    }

    /**
     * 搜索所有学生
     */
    @GetMapping("/students/search")
    public TableDataInfo searchAllStudents(
            @RequestParam String keyword,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Boolean noPagination) {  // 添加不分页标识

        try {
            System.out.println("=== Controller: 搜索学生 ===");
            System.out.println("接收到的关键词: " + keyword);
            System.out.println("不分页标识: " + noPagination);

            // 根据noPagination参数决定是否分页
            if (noPagination == null || !noPagination) {
                startPage();  // 默认分页
            }

            List<ClassStudentLw> list = classManagementService.searchAllStudents(keyword, sessionId);

            System.out.println("Controller返回数量: " + (list != null ? list.size() : "null"));

            // 如果不分页，需要手动设置total
            TableDataInfo rspData = new TableDataInfo();
            rspData.setCode(200);
            rspData.setRows(list != null ? list : new ArrayList<>());

            if (noPagination != null && noPagination) {
                // 不分页时，total就是列表大小
                rspData.setTotal(list != null ? list.size() : 0);
            } else {
                // 分页时，getDataTable会处理total
                return getDataTable(list != null ? list : new ArrayList<>());
            }

            return rspData;

        } catch (Exception e) {
            System.err.println("搜索学生失败: " + e.getMessage());
            e.printStackTrace();
            TableDataInfo rspData = new TableDataInfo();
            rspData.setCode(500);
            rspData.setRows(new ArrayList<>());
            rspData.setTotal(0);
            return rspData;
        }
    }

    /**
     * 添加学生到课堂
     */
    @Log(title = "课堂添加学生", businessType = BusinessType.INSERT)
    @PostMapping("/{sessionId}/students/add")
    public AjaxResult addStudentsToClass(@PathVariable Long sessionId, @RequestBody List<Long> studentIds) {
        try {
            System.out.println("=== 添加学生到课堂 ===");
            System.out.println("课堂ID: " + sessionId + ", 学生IDs: " + studentIds);

            if (studentIds == null || studentIds.isEmpty()) {
                return AjaxResult.error("请选择要添加的学生");
            }

            int result = classManagementService.addStudentsToClass(sessionId, studentIds);
            return AjaxResult.success("成功添加 " + result + " 名学生");

        } catch (Exception e) {
            System.err.println("添加学生失败: " + e.getMessage());
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 从课堂移除学生
     */
    @Log(title = "课堂移除学生", businessType = BusinessType.DELETE)
    @PostMapping("/{sessionId}/students/{studentId}/remove")
    public AjaxResult removeStudentFromClass(@PathVariable Long sessionId, @PathVariable Long studentId) {
        try {
            System.out.println("=== 从课堂移除学生 ===");
            System.out.println("课堂ID: " + sessionId + ", 学生ID: " + studentId);

            int result = classManagementService.removeStudentFromClass(sessionId, studentId);
            return result > 0 ? AjaxResult.success("移除学生成功") : AjaxResult.error("移除学生失败");

        } catch (Exception e) {
            System.err.println("移除学生失败: " + e.getMessage());
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 批量移除学生
     */
    @Log(title = "课堂批量移除学生", businessType = BusinessType.DELETE)
    @PostMapping("/{sessionId}/students/batch-remove")
    public AjaxResult batchRemoveStudents(@PathVariable Long sessionId, @RequestBody List<Long> studentIds) {
        try {
            System.out.println("=== 批量移除学生 ===");
            System.out.println("课堂ID: " + sessionId + ", 学生IDs: " + studentIds);

            if (studentIds == null || studentIds.isEmpty()) {
                return AjaxResult.error("请选择要移除的学生");
            }

            int result = classManagementService.batchRemoveStudents(sessionId, studentIds);
            return AjaxResult.success("成功移除 " + result + " 名学生");

        } catch (Exception e) {
            System.err.println("批量移除学生失败: " + e.getMessage());
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }
}