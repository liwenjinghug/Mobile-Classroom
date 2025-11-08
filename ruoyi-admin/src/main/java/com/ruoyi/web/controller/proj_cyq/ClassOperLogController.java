package com.ruoyi.web.controller.proj_cyq;

import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.proj_cyq.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.proj_cyq.domain.ClassOperLog;
import com.ruoyi.proj_cyq.service.IClassOperLogService;
import com.ruoyi.common.utils.poi.ExcelUtil;

/**
 * 操作日志记录Controller
 */
@RestController
@RequestMapping("/proj_cyq/operlog")
public class ClassOperLogController extends BaseController {
    @Autowired
    private IClassOperLogService classOperLogService;

    /**
     * 查询操作日志记录列表
     */
    @PreAuthorize("@ss.hasPermi('proj_cyq:operlog:list')")
    @Log(title = "操作日志", businessType = BusinessType.OTHER)
    @GetMapping("/list")
    public TableDataInfo list(ClassOperLog classOperLog) {
        startPage();
        List<ClassOperLog> list = classOperLogService.selectClassOperLogList(classOperLog);
        return getDataTable(list);
    }

    /**
     * 导出操作日志记录列表 - 支持 GET 和 POST 请求
     */
    @PreAuthorize("@ss.hasPermi('proj_cyq:operlog:export')")
    @Log(title = "操作日志", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    @PostMapping("/export") // 同时支持 GET 和 POST
    public void export(HttpServletResponse response, ClassOperLog classOperLog) {
        List<ClassOperLog> list = classOperLogService.selectClassOperLogList(classOperLog);
        ExcelUtil<ClassOperLog> util = new ExcelUtil<ClassOperLog>(ClassOperLog.class);
        util.exportExcel(response, list, "操作日志记录数据");
    }

    /**
     * 获取操作日志记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('proj_cyq:operlog:query')")
    @Log(title = "操作日志", businessType = BusinessType.OTHER)
    @GetMapping(value = "/{operId}")
    public AjaxResult getInfo(@PathVariable("operId") Long operId) {
        return success(classOperLogService.selectClassOperLogById(operId));
    }

    /**
     * 删除操作日志记录
     */
    @PreAuthorize("@ss.hasPermi('proj_cyq:operlog:remove')")
    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{operIds}")
    public AjaxResult remove(@PathVariable Long[] operIds) {
        return toAjax(classOperLogService.deleteClassOperLogByIds(operIds));
    }

    /**
     * 清空操作日志
     */
    @PreAuthorize("@ss.hasPermi('proj_cyq:operlog:remove')")
    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public AjaxResult clean() {
        classOperLogService.cleanClassOperLog();
        return success();
    }
}