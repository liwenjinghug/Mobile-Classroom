package com.ruoyi.proj_fz.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.proj_fz.domain.DatabaseMetrics;
import com.ruoyi.proj_fz.domain.ServerMetrics;
import com.ruoyi.proj_fz.domain.SystemMonitor;
import com.ruoyi.proj_fz.mapper.SystemMonitorMapper;
import com.ruoyi.proj_fz.service.ISystemMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.util.Util;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * 系统监控Service实现
 *
 * @author proj_fz
 */
@Service
public class SystemMonitorServiceImpl implements ISystemMonitorService {

    @Autowired
    private SystemMonitorMapper systemMonitorMapper;

    @Autowired(required = false)
    private DataSource dataSource;

    private static final int OSHI_WAIT_SECOND = 1000;

    @Override
    public List<SystemMonitor> selectSystemMonitorList(SystemMonitor systemMonitor) {
        return systemMonitorMapper.selectSystemMonitorList(systemMonitor);
    }

    @Override
    public SystemMonitor selectSystemMonitorById(Long monitorId) {
        return systemMonitorMapper.selectSystemMonitorById(monitorId);
    }

    @Override
    public int insertSystemMonitor(SystemMonitor systemMonitor) {
        systemMonitor.setMonitorTime(new Date());
        systemMonitor.setHandled(0);
        return systemMonitorMapper.insertSystemMonitor(systemMonitor);
    }

    @Override
    public int deleteSystemMonitorById(Long monitorId) {
        return systemMonitorMapper.deleteSystemMonitorById(monitorId);
    }

    @Override
    public int deleteSystemMonitorByIds(Long[] monitorIds) {
        return systemMonitorMapper.deleteSystemMonitorByIds(monitorIds);
    }

    @Override
    public int updateHandleStatus(SystemMonitor systemMonitor) {
        systemMonitor.setHandleTime(new Date());
        return systemMonitorMapper.updateHandleStatus(systemMonitor);
    }

    @Override
    public ServerMetrics getRealTimeServerMetrics() {
        ServerMetrics metrics = new ServerMetrics();
        metrics.setMonitorTime(new Date());

        try {
            SystemInfo si = new SystemInfo();
            HardwareAbstractionLayer hal = si.getHardware();

            // CPU信息
            CentralProcessor processor = hal.getProcessor();
            long[] prevTicks = processor.getSystemCpuLoadTicks();
            Util.sleep(OSHI_WAIT_SECOND);
            long[] ticks = processor.getSystemCpuLoadTicks();
            long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
            long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
            long sys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
            long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
            long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
            long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
            long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
            long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
            long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;

            double cpuUsage = 100d * (totalCpu - idle) / totalCpu;
            metrics.setCpuUsage(Math.round(cpuUsage * 100.0) / 100.0);

            // 内存信息
            GlobalMemory memory = hal.getMemory();
            long totalMem = memory.getTotal();
            long availableMem = memory.getAvailable();
            long usedMem = totalMem - availableMem;

            metrics.setTotalMemory(Math.round(totalMem / 1024.0 / 1024.0 / 1024.0 * 100.0) / 100.0);
            metrics.setUsedMemory(Math.round(usedMem / 1024.0 / 1024.0 / 1024.0 * 100.0) / 100.0);
            metrics.setMemoryUsage(Math.round(100.0 * usedMem / totalMem * 100.0) / 100.0);

            // 磁盘信息
            FileSystem fileSystem = si.getOperatingSystem().getFileSystem();
            List<OSFileStore> fsArray = fileSystem.getFileStores();
            long totalDisk = 0;
            long usedDisk = 0;
            for (OSFileStore fs : fsArray) {
                long total = fs.getTotalSpace();
                long usable = fs.getUsableSpace();
                totalDisk += total;
                usedDisk += (total - usable);
            }

            metrics.setTotalDisk(Math.round(totalDisk / 1024.0 / 1024.0 / 1024.0 * 100.0) / 100.0);
            metrics.setUsedDisk(Math.round(usedDisk / 1024.0 / 1024.0 / 1024.0 * 100.0) / 100.0);
            if (totalDisk > 0) {
                metrics.setDiskUsage(Math.round(100.0 * usedDisk / totalDisk * 100.0) / 100.0);
            }

            // 网络速率（真实数据 - 通过NetworkIF获取）
            try {
                long totalBytesRecv = 0;
                long totalBytesSent = 0;

                si.getHardware().getNetworkIFs().forEach(net -> {
                    net.updateAttributes();
                });

                // 等待一秒后再次获取，计算速率
                Thread.sleep(1000);

                for (NetworkIF net : si.getHardware().getNetworkIFs()) {
                    net.updateAttributes();
                    totalBytesRecv += net.getBytesRecv();
                    totalBytesSent += net.getBytesSent();
                }

                // 转换为KB/s（因为等待了1秒）
                metrics.setDownloadSpeed(Math.round(totalBytesRecv / 1024.0 * 100.0) / 100.0);
                metrics.setUploadSpeed(Math.round(totalBytesSent / 1024.0 * 100.0) / 100.0);
            } catch (Exception netEx) {
                // 网络接口获取失败，设置为0
                metrics.setUploadSpeed(0.0);
                metrics.setDownloadSpeed(0.0);
            }

            // JVM信息
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            long heapMemoryUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
            long heapMemoryMax = memoryMXBean.getHeapMemoryUsage().getMax();

            metrics.setJvmHeapTotal(Math.round(heapMemoryMax / 1024.0 / 1024.0 * 100.0) / 100.0);
            metrics.setJvmHeapUsed(Math.round(heapMemoryUsed / 1024.0 / 1024.0 * 100.0) / 100.0);
            metrics.setJvmHeapUsage(Math.round(100.0 * heapMemoryUsed / heapMemoryMax * 100.0) / 100.0);

            // 线程数
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            metrics.setThreadCount(threadMXBean.getThreadCount());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return metrics;
    }

    @Override
    public List<ServerMetrics> getServerMetricsHistory(Integer hours) {
        // 从监控记录中查询真实历史数据
        List<ServerMetrics> list = new ArrayList<>();

        try {
            // 查询最近N小时的服务器监控记录
            List<SystemMonitor> records = systemMonitorMapper.selectSystemMonitorList(new SystemMonitor() {{
                setMonitorType(1); // 服务器监控
            }});

            // 只取最近hours小时的数据
            Date cutoffTime = new Date(System.currentTimeMillis() - hours * 60 * 60 * 1000);

            for (SystemMonitor record : records) {
                if (record.getMonitorTime() != null && record.getMonitorTime().after(cutoffTime)) {
                    if (record.getMetrics() != null && !record.getMetrics().isEmpty()) {
                        try {
                            ServerMetrics metrics = JSON.parseObject(record.getMetrics(), ServerMetrics.class);
                            metrics.setMonitorTime(record.getMonitorTime());
                            list.add(metrics);
                        } catch (Exception e) {
                            // 解析失败，跳过该条记录
                        }
                    }
                }
            }

            // 按时间排序
            list.sort(Comparator.comparing(ServerMetrics::getMonitorTime));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public DatabaseMetrics getRealTimeDatabaseMetrics() {
        DatabaseMetrics metrics = new DatabaseMetrics();
        metrics.setMonitorTime(new Date());

        if (dataSource == null) {
            return metrics;
        }

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // 查询连接数（真实数据）
            ResultSet rs = stmt.executeQuery("SHOW STATUS LIKE 'Threads_connected'");
            if (rs.next()) {
                metrics.setConnectionCount(rs.getInt(2));
            }
            rs.close();

            // 查询活跃连接（真实数据）
            rs = stmt.executeQuery("SHOW STATUS LIKE 'Threads_running'");
            if (rs.next()) {
                metrics.setActiveConnections(rs.getInt(2));
            }
            rs.close();

            // 计算空闲连接（真实数据）
            if (metrics.getConnectionCount() != null && metrics.getActiveConnections() != null) {
                metrics.setIdleConnections(metrics.getConnectionCount() - metrics.getActiveConnections());
            }

            // 查询QPS（真实数据 - 累计查询数）
            rs = stmt.executeQuery("SHOW STATUS LIKE 'Questions'");
            if (rs.next()) {
                metrics.setQps(rs.getInt(2));
            }
            rs.close();

            // 查询数据库大小（真实数据）
            rs = stmt.executeQuery("SELECT SUM(data_length + index_length) / 1024 / 1024 AS size FROM information_schema.TABLES WHERE table_schema = DATABASE()");
            if (rs.next()) {
                metrics.setDatabaseSize(rs.getDouble("size"));
            }
            rs.close();

            // 注意：以下指标设置为null，前端将不显示这些指标
            // 慢查询、响应时间、缓存命中率、TPS、表空间使用率需要更复杂的监控系统才能准确获取
            metrics.setSlowQueryCount(null);
            metrics.setQueryResponseTime(null);
            metrics.setCacheHitRate(null);
            metrics.setTps(null);
            metrics.setTablespaceUsage(null);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return metrics;
    }

    @Override
    public List<DatabaseMetrics> getDatabaseMetricsHistory(Integer hours) {
        // 从监控记录中查询真实历史数据
        List<DatabaseMetrics> list = new ArrayList<>();

        try {
            // 查询最近N小时的数据库监控记录
            List<SystemMonitor> records = systemMonitorMapper.selectSystemMonitorList(new SystemMonitor() {{
                setMonitorType(2); // 数据库监控
            }});

            // 只取最近hours小时的数据
            Date cutoffTime = new Date(System.currentTimeMillis() - hours * 60 * 60 * 1000);

            for (SystemMonitor record : records) {
                if (record.getMonitorTime() != null && record.getMonitorTime().after(cutoffTime)) {
                    if (record.getMetrics() != null && !record.getMetrics().isEmpty()) {
                        try {
                            DatabaseMetrics metrics = JSON.parseObject(record.getMetrics(), DatabaseMetrics.class);
                            metrics.setMonitorTime(record.getMonitorTime());
                            list.add(metrics);
                        } catch (Exception e) {
                            // 解析失败，跳过该条记录
                        }
                    }
                }
            }

            // 按时间排序
            list.sort(Comparator.comparing(DatabaseMetrics::getMonitorTime));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public Map<String, Object> getMonitorStatistics(Integer days) {
        Map<String, Object> result = new HashMap<>();

        // 监控类型统计
        List<Map<String, Object>> typeStats = systemMonitorMapper.selectMonitorTypeStats(days);
        result.put("typeStats", typeStats);

        // 告警级别统计
        List<Map<String, Object>> alertStats = systemMonitorMapper.selectAlertLevelStats(days);
        result.put("alertStats", alertStats);

        // 监控趋势
        List<Map<String, Object>> trend = systemMonitorMapper.selectMonitorTrend(24);
        result.put("trend", trend);

        // 未处理告警数
        int unhandledCount = systemMonitorMapper.selectUnhandledAlertCount();
        result.put("unhandledAlertCount", unhandledCount);

        return result;
    }

    @Override
    public int getUnhandledAlertCount() {
        return systemMonitorMapper.selectUnhandledAlertCount();
    }

    @Override
    public void collectSystemMetrics() {
        // 采集服务器指标
        ServerMetrics serverMetrics = getRealTimeServerMetrics();

        // 检查是否需要告警
        SystemMonitor monitor = new SystemMonitor();
        monitor.setMonitorType(1); // 服务器监控
        monitor.setMonitorName("服务器资源监控");
        monitor.setMetrics(JSON.toJSONString(serverMetrics));
        monitor.setStatus(0);
        monitor.setAlertLevel(0);

        // CPU告警
        if (serverMetrics.getCpuUsage() != null && serverMetrics.getCpuUsage() > 80) {
            monitor.setAlertLevel(serverMetrics.getCpuUsage() > 90 ? 2 : 1);
            monitor.setAlertDesc("CPU使用率过高：" + serverMetrics.getCpuUsage() + "%");
            monitor.setStatus(1);
        }

        // 内存告警
        if (serverMetrics.getMemoryUsage() != null && serverMetrics.getMemoryUsage() > 80) {
            monitor.setAlertLevel(serverMetrics.getMemoryUsage() > 90 ? 2 : 1);
            monitor.setAlertDesc((monitor.getAlertDesc() == null ? "" : monitor.getAlertDesc() + "; ")
                + "内存使用率过高：" + serverMetrics.getMemoryUsage() + "%");
            monitor.setStatus(1);
        }

        // 磁盘告警
        if (serverMetrics.getDiskUsage() != null && serverMetrics.getDiskUsage() > 85) {
            monitor.setAlertLevel(serverMetrics.getDiskUsage() > 95 ? 2 : 1);
            monitor.setAlertDesc((monitor.getAlertDesc() == null ? "" : monitor.getAlertDesc() + "; ")
                + "磁盘使用率过高：" + serverMetrics.getDiskUsage() + "%");
            monitor.setStatus(1);
        }

        insertSystemMonitor(monitor);

        // 采集数据库指标
        DatabaseMetrics dbMetrics = getRealTimeDatabaseMetrics();
        SystemMonitor dbMonitor = new SystemMonitor();
        dbMonitor.setMonitorType(2); // 数据库监控
        dbMonitor.setMonitorName("数据库性能监控");
        dbMonitor.setMetrics(JSON.toJSONString(dbMetrics));
        dbMonitor.setStatus(0);
        dbMonitor.setAlertLevel(0);

        // 慢查询告警
        if (dbMetrics.getSlowQueryCount() != null && dbMetrics.getSlowQueryCount() > 5) {
            dbMonitor.setAlertLevel(dbMetrics.getSlowQueryCount() > 10 ? 2 : 1);
            dbMonitor.setAlertDesc("慢查询数量过多：" + dbMetrics.getSlowQueryCount());
            dbMonitor.setStatus(1);
        }

        insertSystemMonitor(dbMonitor);
    }
}

