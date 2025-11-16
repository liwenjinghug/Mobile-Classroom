package com.ruoyi.web.controller.proj_cyq;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 【修复】导入你自定义的 @Log 注解
import com.ruoyi.proj_cyq.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.proj_cyq.domain.Todo;
import com.ruoyi.proj_cyq.domain.TodoStatsExport;
import com.ruoyi.proj_cyq.service.ITodoService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

@RestController
@RequestMapping("/proj_cyq/todo")
public class TodoController extends BaseController {
    @Autowired
    private ITodoService todoService;

    // (查询列表和GetInfo通常不记录操作日志，保持不变)
    @GetMapping("/list")
    public TableDataInfo list(Todo todo) {
        startPage();
        List<Todo> list = todoService.selectTodoList(todo);
        return getDataTable(list);
    }

    // 此注解现在会正确指向 ClassLogAspect
    @Log(title = "待办事项", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Todo todo) {
        List<Todo> list = todoService.selectTodoList(todo);
        ExcelUtil<Todo> util = new ExcelUtil<Todo>(Todo.class);
        util.exportExcel(response, list, "待办事项数据");
    }

    @GetMapping(value = "/{todoId}")
    public AjaxResult getInfo(@PathVariable("todoId") Long todoId) {
        return success(todoService.selectTodoById(todoId));
    }

    @Log(title = "待办事项", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Todo todo) {
        return toAjax(todoService.insertTodo(todo));
    }

    @Log(title = "待办事项", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Todo todo) {
        return toAjax(todoService.updateTodo(todo));
    }

    @Log(title = "待办事项", businessType = BusinessType.DELETE)
    @DeleteMapping("/{todoId}")
    public AjaxResult remove(@PathVariable("todoId") Long todoId) {
        return toAjax(todoService.deleteTodoById(todoId));
    }

    @Log(title = "待办事项", businessType = BusinessType.DELETE)
    @DeleteMapping("/batch/{todoIds}")
    public AjaxResult removeBatch(@PathVariable("todoIds") Long[] todoIds) {
        return toAjax(todoService.deleteTodoByIds(todoIds));
    }

    @GetMapping("/stats")
    public AjaxResult getStats(@RequestParam Map<String, Object> params) {
        return success(todoService.getTodoStats(params));
    }

    @GetMapping("/stats/detail")
    public AjaxResult getStatsDetail(@RequestParam Map<String, Object> params) {
        return success(todoService.getTodoStatsDetail(params));
    }

    @Log(title = "待办事项", businessType = BusinessType.EXPORT)
    @PostMapping("/exportStats")
    public void exportStats(HttpServletResponse response, @RequestParam Map<String, Object> params) {
        List<Map<String, Object>> list = todoService.getTodoStatsDetail(params);

        // ... (省略转换逻辑)
        List<TodoStatsExport> exportList = new ArrayList<>();
        int total = 0;
        for (Map<String, Object> item : list) {
            if (item.get("value") != null) {
                total += Integer.parseInt(item.get("value").toString());
            }
        }
        for (Map<String, Object> item : list) {
            String category = item.get("category") != null ? item.get("category").toString() : "";
            String name = item.get("name") != null ? item.get("name").toString() : "";
            Integer value = item.get("value") != null ? Integer.parseInt(item.get("value").toString()) : 0;
            double percentage = total > 0 ? (value.doubleValue() / total) * 100 : 0;

            TodoStatsExport exportItem = new TodoStatsExport(
                    category,
                    name,
                    value,
                    String.format("%.2f%%", percentage)
            );
            exportList.add(exportItem);
        }
        if (exportList.isEmpty()) {
            TodoStatsExport emptyItem = new TodoStatsExport("无数据", "无数据", 0, "0%");
            exportList.add(emptyItem);
        }

        ExcelUtil<TodoStatsExport> util = new ExcelUtil<>(TodoStatsExport.class);
        util.exportExcel(response, exportList, "待办事项统计");
    }
}