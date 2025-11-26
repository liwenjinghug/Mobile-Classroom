package com.ruoyi.proj_fz.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 数据库监控指标
 *
 * @author proj_fz
 */
public class DatabaseMetrics {

    /** 连接数 */
    private Integer connectionCount;

    /** 活跃连接数 */
    private Integer activeConnections;

    /** 空闲连接数 */
    private Integer idleConnections;

    /** QPS（每秒查询数） */
    private Integer qps;

    /** TPS（每秒事务数） */
    private Integer tps;

    /** 数据库大小（MB） */
    private Double databaseSize;

    /** 慢查询数量 */
    private Integer slowQueryCount;

    /** 查询响应时间（ms） */
    private Long queryResponseTime;

    /** 缓存命中率 */
    private Double cacheHitRate;

    /** 表空间使用率 */
    private Double tablespaceUsage;

    /** 监控时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date monitorTime;

    public Integer getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(Integer connectionCount) {
        this.connectionCount = connectionCount;
    }

    public Integer getActiveConnections() {
        return activeConnections;
    }

    public void setActiveConnections(Integer activeConnections) {
        this.activeConnections = activeConnections;
    }

    public Integer getIdleConnections() {
        return idleConnections;
    }

    public void setIdleConnections(Integer idleConnections) {
        this.idleConnections = idleConnections;
    }

    public Integer getQps() {
        return qps;
    }

    public void setQps(Integer qps) {
        this.qps = qps;
    }

    public Integer getTps() {
        return tps;
    }

    public void setTps(Integer tps) {
        this.tps = tps;
    }

    public Double getDatabaseSize() {
        return databaseSize;
    }

    public void setDatabaseSize(Double databaseSize) {
        this.databaseSize = databaseSize;
    }

    public Integer getSlowQueryCount() {
        return slowQueryCount;
    }

    public void setSlowQueryCount(Integer slowQueryCount) {
        this.slowQueryCount = slowQueryCount;
    }

    public Long getQueryResponseTime() {
        return queryResponseTime;
    }

    public void setQueryResponseTime(Long queryResponseTime) {
        this.queryResponseTime = queryResponseTime;
    }

    public Double getCacheHitRate() {
        return cacheHitRate;
    }

    public void setCacheHitRate(Double cacheHitRate) {
        this.cacheHitRate = cacheHitRate;
    }

    public Double getTablespaceUsage() {
        return tablespaceUsage;
    }

    public void setTablespaceUsage(Double tablespaceUsage) {
        this.tablespaceUsage = tablespaceUsage;
    }

    public Date getMonitorTime() {
        return monitorTime;
    }

    public void setMonitorTime(Date monitorTime) {
        this.monitorTime = monitorTime;
    }
}

