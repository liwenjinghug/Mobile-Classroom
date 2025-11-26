package com.ruoyi.proj_fz.service;

import com.ruoyi.proj_fz.domain.SystemMonitor;
import com.ruoyi.proj_fz.domain.ServerMetrics;
import com.ruoyi.proj_fz.domain.DatabaseMetrics;
import java.util.List;
import java.util.Map;

/**
 * 系统监控Service接口
 *
 * @author proj_fz
 */
public interface ISystemMonitorService {

    /**
     * 查询系统监控列表
     */
    List<SystemMonitor> selectSystemMonitorList(SystemMonitor systemMonitor);

    /**
     * 根据ID查询系统监控
     */
    SystemMonitor selectSystemMonitorById(Long monitorId);

    /**
     * 新增系统监控
     */
    int insertSystemMonitor(SystemMonitor systemMonitor);

    /**
     * 删除系统监控
     */
    int deleteSystemMonitorById(Long monitorId);

    /**
     * 批量删除系统监控
     */
    int deleteSystemMonitorByIds(Long[] monitorIds);

    /**
     * 更新处理状态
     */
    int updateHandleStatus(SystemMonitor systemMonitor);

    /**
     * 获取实时服务器指标
     */
    ServerMetrics getRealTimeServerMetrics();

    /**
     * 获取服务器历史指标
     */
    List<ServerMetrics> getServerMetricsHistory(Integer hours);

    /**
     * 获取实时数据库指标
     */
    DatabaseMetrics getRealTimeDatabaseMetrics();

    /**
     * 获取数据库历史指标
     */
    List<DatabaseMetrics> getDatabaseMetricsHistory(Integer hours);

    /**
     * 获取监控统计数据
     */
    Map<String, Object> getMonitorStatistics(Integer days);

    /**
     * 获取未处理告警数量
     */
    int getUnhandledAlertCount();

    /**
     * 执行系统监控采集（定时任务）
     */
    void collectSystemMetrics();
}
