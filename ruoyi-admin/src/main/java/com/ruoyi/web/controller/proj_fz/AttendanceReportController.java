package com.ruoyi.web.controller.proj_fz;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.proj_fz.domain.AttendanceStatisticsDTO;
import com.ruoyi.proj_fz.service.IAttendanceStatisticsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/proj_fz/attendanceReport")
public class AttendanceReportController extends BaseController {

    @Autowired
    private IAttendanceStatisticsService attendanceStatisticsService;

    /**
     * 课堂维度统计
     */
    @PreAuthorize("@ss.hasPermi('proj_fz:attendanceReport:list')")
    @GetMapping("/sessionStatistics")
    public TableDataInfo sessionStatistics(@RequestParam(required = false) Long sessionId,
                                           @RequestParam(required = false) String startDate,
                                           @RequestParam(required = false) String endDate) {
        Date start = StringUtils.isNotEmpty(startDate) ? DateUtils.parseDate(startDate) : null;
        Date end = StringUtils.isNotEmpty(endDate) ? DateUtils.parseDate(endDate) : null;
        startPage();
        List<AttendanceStatisticsDTO> list = attendanceStatisticsService.getSessionStatistics(sessionId, start, end);
        return getDataTable(list);
    }

    /**
     * 时间维度统计
     */
    @PreAuthorize("@ss.hasPermi('proj_fz:attendanceReport:list')")
    @GetMapping("/timeStatistics")
    public AjaxResult timeStatistics(@RequestParam(required = false) Long sessionId,
                                     @RequestParam(required = false) String startDate,
                                     @RequestParam(required = false) String endDate,
                                     @RequestParam(defaultValue = "day") String groupBy) {
        Date start = StringUtils.isNotEmpty(startDate) ? DateUtils.parseDate(startDate) : null;
        Date end = StringUtils.isNotEmpty(endDate) ? DateUtils.parseDate(endDate) : null;
        Map<String, Object> result = attendanceStatisticsService.getTimeStatistics(sessionId, start, end, groupBy);
        return AjaxResult.success(result);
    }

    /**
     * 签到明细
     */
    @PreAuthorize("@ss.hasPermi('proj_fz:attendanceReport:list')")
    @GetMapping("/attendanceDetails")
    public TableDataInfo attendanceDetails(@RequestParam(required = false) Long sessionId,
                                           @RequestParam(required = false) String startDate,
                                           @RequestParam(required = false) String endDate,
                                           @RequestParam(required = false) Integer attendanceStatus) {
        Date start = StringUtils.isNotEmpty(startDate) ? DateUtils.parseDate(startDate) : null;
        Date end = StringUtils.isNotEmpty(endDate) ? DateUtils.parseDate(endDate) : null;
        startPage();
        List<AttendanceStatisticsDTO> list = attendanceStatisticsService.getAttendanceDetails(sessionId, start, end, attendanceStatus);
        return getDataTable(list);
    }

    /**
     * 周报表
     */
    @PreAuthorize("@ss.hasPermi('proj_fz:attendanceReport:list')")
    @GetMapping("/weeklyReport")
    public AjaxResult weeklyReport(@RequestParam(required = false) String startDate,
                                   @RequestParam(required = false) String endDate) {
        Date start = StringUtils.isNotEmpty(startDate) ? DateUtils.parseDate(startDate) :
                DateUtils.addDays(DateUtils.getNowDate(), -7);
        Date end = StringUtils.isNotEmpty(endDate) ? DateUtils.parseDate(endDate) : DateUtils.getNowDate();
        Map<String, Object> result = attendanceStatisticsService.getWeeklyReport(start, end);
        return AjaxResult.success(result);
    }

    /**
     * 驾驶舱指标
     */
    @PreAuthorize("@ss.hasPermi('proj_fz:attendanceReport:list')")
    @GetMapping("/dashboardMetrics")
    public AjaxResult dashboardMetrics(@RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate) {
        Date start = StringUtils.isNotEmpty(startDate) ? DateUtils.parseDate(startDate) :
                DateUtils.addDays(DateUtils.getNowDate(), -7);
        Date end = StringUtils.isNotEmpty(endDate) ? DateUtils.parseDate(endDate) : DateUtils.getNowDate();
        Map<String, Object> metrics = attendanceStatisticsService.getDashboardMetrics(start, end);
        return AjaxResult.success(metrics);
    }

    /**
     * 导出考勤报表
     */
    @Log(title = "考勤报表", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('proj_fz:attendanceReport:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response,
                       @RequestParam(required = false) Long sessionId,
                       @RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate,
                       @RequestParam String exportType) {
        Date start = StringUtils.isNotEmpty(startDate) ? DateUtils.parseDate(startDate) : null;
        Date end = StringUtils.isNotEmpty(endDate) ? DateUtils.parseDate(endDate) : null;

        List<AttendanceStatisticsDTO> list;
        String sheetName;

        if ("details".equals(exportType)) {
            list = attendanceStatisticsService.getAttendanceDetails(sessionId, start, end, null);
            sheetName = "考勤明细";
        } else if ("time".equals(exportType)) {
            // 处理时间维度导出
            Map<String, Object> result = attendanceStatisticsService.getTimeStatistics(sessionId, start, end, "day");
            list = (List<AttendanceStatisticsDTO>) result.get("statistics");
            sheetName = "时间趋势统计";
        } else {
            list = attendanceStatisticsService.getSessionStatistics(sessionId, start, end);
            sheetName = "课堂统计";
        }

        if (list == null || list.isEmpty()) {
            throw new RuntimeException("没有数据可以导出");
        }

        ExcelUtil<AttendanceStatisticsDTO> util = new ExcelUtil<>(AttendanceStatisticsDTO.class);
        util.exportExcel(response, list, sheetName);
    }
}