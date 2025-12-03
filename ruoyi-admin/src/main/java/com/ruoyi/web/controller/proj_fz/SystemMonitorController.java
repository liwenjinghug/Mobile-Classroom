package com.ruoyi.web.controller.proj_fz;

// 使用自定义的 Log 注解
import com.ruoyi.proj_cyq.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.proj_fz.domain.DatabaseMetrics;
import com.ruoyi.proj_fz.domain.ServerMetrics;
import com.ruoyi.proj_fz.domain.SystemMonitor;
import com.ruoyi.proj_fz.service.ISystemMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 系统监控Controller
 *
 * @author proj_fz
 */
@RestController
@RequestMapping("/proj_fz/monitor")
public class SystemMonitorController extends BaseController {

    @Autowired
    private ISystemMonitorService systemMonitorService;

    /**
     * 查询系统监控列表
     */
    @PreAuthorize("@ss.hasPermi('proj_fz:monitor:list') or @ss.hasAnyRoles('teacher,student')")
    @Log(title = "系统监控", businessType = BusinessType.OTHER) // 【新增】记录列表查询
    @GetMapping("/list")
    public TableDataInfo list(SystemMonitor systemMonitor) {
        startPage();
        List<SystemMonitor> list = systemMonitorService.selectSystemMonitorList(systemMonitor);
        return getDataTable(list);
    }

    /**
     * 获取系统监控详细信息
     */
    @PreAuthorize("@ss.hasPermi('proj_fz:monitor:query') or @ss.hasAnyRoles('teacher,student')")
    @GetMapping(value = "/{monitorId}")
    public AjaxResult getInfo(@PathVariable("monitorId") Long monitorId) {
        return success(systemMonitorService.selectSystemMonitorById(monitorId));
    }

    /**
     * 新增系统监控
     */
    @PreAuthorize("@ss.hasPermi('proj_fz:monitor:add') or @ss.hasAnyRoles('teacher,student')")
    @Log(title = "系统监控", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SystemMonitor systemMonitor) {
        return toAjax(systemMonitorService.insertSystemMonitor(systemMonitor));
    }

    /**
     * 删除系统监控
     */
    @PreAuthorize("@ss.hasPermi('proj_fz:monitor:remove') or @ss.hasAnyRoles('teacher,student')")
    @Log(title = "系统监控", businessType = BusinessType.DELETE)
    @DeleteMapping("/{monitorIds}")
    public AjaxResult remove(@PathVariable Long[] monitorIds) {
        return toAjax(systemMonitorService.deleteSystemMonitorByIds(monitorIds));
    }

    /**
     * 处理告警
     */
    @PreAuthorize("@ss.hasPermi('proj_fz:monitor:edit') or @ss.hasAnyRoles('teacher,student')")
    @Log(title = "系统监控", businessType = BusinessType.UPDATE)
    @PutMapping("/handle")
    public AjaxResult handleAlert(@RequestBody SystemMonitor systemMonitor) {
        systemMonitor.setHandled(1);
        systemMonitor.setHandler(getUsername());
        return toAjax(systemMonitorService.updateHandleStatus(systemMonitor));
    }

    /**
     * 导出系统监控列表
     */
    @PreAuthorize("@ss.hasPermi('proj_fz:monitor:export') or @ss.hasAnyRoles('teacher,student')")
    @Log(title = "系统监控", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SystemMonitor systemMonitor) {
        List<SystemMonitor> list = systemMonitorService.selectSystemMonitorList(systemMonitor);
        ExcelUtil<SystemMonitor> util = new ExcelUtil<>(SystemMonitor.class);
        util.exportExcel(response, list, "系统监控数据");
    }

    /**
     * 获取实时服务器指标
     */
    @PreAuthorize("@ss.hasPermi('proj_fz:monitor:query') or @ss.hasAnyRoles('teacher,student')")
    @Log(title = "服务器监控", businessType = BusinessType.OTHER) // 【新增】记录查看服务器监控
    @GetMapping("/server/realtime")
    public AjaxResult getServerMetrics() {
        ServerMetrics metrics = systemMonitorService.getRealTimeServerMetrics();
        return success(metrics);
    }

    /**
     * 获取服务器历史指标
     */
    @PreAuthorize("@ss.hasPermi('proj_fz:monitor:query') or @ss.hasAnyRoles('teacher,student')")
    @Log(title = "服务器监控", businessType = BusinessType.OTHER) // 【新增】
    @GetMapping("/server/history")
    public AjaxResult getServerHistory(@RequestParam(defaultValue = "24") Integer hours) {
        List<ServerMetrics> list = systemMonitorService.getServerMetricsHistory(hours);
        return success(list);
    }

    /**
     * 获取实时数据库指标
     */
    @PreAuthorize("@ss.hasPermi('proj_fz:monitor:query') or @ss.hasAnyRoles('teacher,student')")
    @Log(title = "数据库监控", businessType = BusinessType.OTHER) // 【新增】记录查看数据库监控
    @GetMapping("/database/realtime")
    public AjaxResult getDatabaseMetrics() {
        DatabaseMetrics metrics = systemMonitorService.getRealTimeDatabaseMetrics();
        return success(metrics);
    }

    /**
     * 获取数据库历史指标
     */
    @PreAuthorize("@ss.hasPermi('proj_fz:monitor:query') or @ss.hasAnyRoles('teacher,student')")
    @Log(title = "数据库监控", businessType = BusinessType.OTHER) // 【新增】
    @GetMapping("/database/history")
    public AjaxResult getDatabaseHistory(@RequestParam(defaultValue = "24") Integer hours) {
        List<DatabaseMetrics> list = systemMonitorService.getDatabaseMetricsHistory(hours);
        return success(list);
    }

    /**
     * 获取监控统计数据 (监控记录)
     */
    @PreAuthorize("@ss.hasPermi('proj_fz:monitor:query') or @ss.hasAnyRoles('teacher,student')")
    @Log(title = "监控记录", businessType = BusinessType.OTHER) // 【新增】记录查看监控统计
    @GetMapping("/statistics")
    public AjaxResult getStatistics(@RequestParam(defaultValue = "7") Integer days) {
        Map<String, Object> stats = systemMonitorService.getMonitorStatistics(days);
        return success(stats);
    }

    /**
     * 获取未处理告警数量
     */
    @PreAuthorize("@ss.hasPermi('proj_fz:monitor:query') or @ss.hasAnyRoles('teacher,student')")
    @GetMapping("/alert/count")
    public AjaxResult getUnhandledAlertCount() {
        int count = systemMonitorService.getUnhandledAlertCount();
        return success(count);
    }

    /**
     * 手动执行监控采集
     */
    @PreAuthorize("@ss.hasPermi('proj_fz:monitor:collect') or @ss.hasAnyRoles('teacher,student')")
    @Log(title = "系统监控", businessType = BusinessType.OTHER)
    @PostMapping("/collect")
    public AjaxResult collectMetrics() {
        systemMonitorService.collectSystemMetrics();
        return success();
    }
}