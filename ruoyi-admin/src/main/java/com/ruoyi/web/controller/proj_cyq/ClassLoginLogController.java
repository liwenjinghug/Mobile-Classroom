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
    @PreAuthorize("@ss.hasPermi('proj_cyq:loginlog:list')")
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
    @PreAuthorize("@ss.hasPermi('proj_cyq:loginlog:export')")
    @Log(title = "登录日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ClassLoginLog classLoginLog) {
        List<ClassLoginLog> list = classLoginLogService.selectClassLoginLogList(classLoginLog);

        // 调试信息
        System.out.println("导出登录日志数据条数: " + (list != null ? list.size() : 0));
        if (list != null && !list.isEmpty()) {
            System.out.println("第一条数据: " + list.get(0));
        }

        ExcelUtil<ClassLoginLog> util = new ExcelUtil<ClassLoginLog>(ClassLoginLog.class);
        util.exportExcel(response, list, "系统登录日志数据");
    }

    /**
     * 获取系统登录日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('proj_cyq:loginlog:query')")
    @Log(title = "登录日志", businessType = BusinessType.OTHER)
    @GetMapping(value = "/{loginId}")
    public AjaxResult getInfo(@PathVariable("loginId") Long loginId) {
        return success(classLoginLogService.selectClassLoginLogById(loginId));
    }

    /**
     * 删除系统登录日志
     */
    @PreAuthorize("@ss.hasPermi('proj_cyq:loginlog:remove')")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{loginIds}")
    public AjaxResult remove(@PathVariable Long[] loginIds) {
        return toAjax(classLoginLogService.deleteClassLoginLogByIds(loginIds));
    }

    /**
     * 清空登录日志
     */
    @PreAuthorize("@ss.hasPermi('proj_cyq:loginlog:remove')")
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public AjaxResult clean() {
        classLoginLogService.cleanClassLoginLog();
        return success();
    }
}