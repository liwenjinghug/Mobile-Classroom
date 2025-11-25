package com.ruoyi.web.controller.proj_cyq;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// 导入你自定义的 @Log 注解，确保记录到 class_oper_log 表
import com.ruoyi.proj_cyq.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.proj_cyq.domain.ClassLoginLog;
import com.ruoyi.proj_cyq.service.IClassLoginLogService;
import com.ruoyi.common.utils.poi.ExcelUtil;

/**
 * 系统登录日志Controller
 */
@RestController
@RequestMapping("/proj_cyq/loginlog")
public class ClassLoginLogController extends BaseController {
    @Autowired
    private IClassLoginLogService classLoginLogService;

    /**
     * 查询系统登录日志列表
     */
    @Log(title = "登录日志", businessType = BusinessType.OTHER)
    @GetMapping("/list")
    public TableDataInfo list(ClassLoginLog classLoginLog) {
        startPage();
        List<ClassLoginLog> list = classLoginLogService.selectClassLoginLogList(classLoginLog);
        return getDataTable(list);
    }

    /**
     * 导出系统登录日志列表
     */
    @Log(title = "登录日志", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(HttpServletResponse response, ClassLoginLog classLoginLog) {
        List<ClassLoginLog> list = classLoginLogService.selectClassLoginLogList(classLoginLog);
        ExcelUtil<ClassLoginLog> util = new ExcelUtil<ClassLoginLog>(ClassLoginLog.class);
        util.exportExcel(response, list, "系统登录日志数据");
    }

    /**
     * 获取系统登录日志详细信息
     */
    @Log(title = "登录日志", businessType = BusinessType.OTHER)
    @GetMapping(value = "/{loginId}")
    public AjaxResult getInfo(@PathVariable("loginId") Long loginId) {
        return success(classLoginLogService.selectClassLoginLogById(loginId));
    }

    /**
     * 删除系统登录日志
     */
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{loginIds}")
    public AjaxResult remove(@PathVariable Long[] loginIds) {
        return toAjax(classLoginLogService.deleteClassLoginLogByIds(loginIds));
    }

    /**
     * 清空登录日志
     */
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public AjaxResult clean() {
        classLoginLogService.cleanClassLoginLog();
        return success();
    }

    // ========== 统计接口 ==========
    @GetMapping("/stats")
    public AjaxResult getStats() {
        return success(classLoginLogService.getLoginLogStats());
    }
}