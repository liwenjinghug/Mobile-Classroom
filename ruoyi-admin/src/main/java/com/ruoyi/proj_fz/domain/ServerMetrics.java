package com.ruoyi.proj_fz.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 服务器监控指标
 *
 * @author proj_fz
 */
public class ServerMetrics {

    /** CPU使用率 */
    private Double cpuUsage;

    /** 内存使用率 */
    private Double memoryUsage;

    /** 内存总量（GB） */
    private Double totalMemory;

    /** 内存已用（GB） */
    private Double usedMemory;

    /** 磁盘总量（GB） */
    private Double totalDisk;

    /** 磁盘已用（GB） */
    private Double usedDisk;

    /** 磁盘使用率 */
    private Double diskUsage;

    /** 网络上传速率（KB/s） */
    private Double uploadSpeed;

    /** 网络下载速率（KB/s） */
    private Double downloadSpeed;

    /** JVM堆内存使用率 */
    private Double jvmHeapUsage;

    /** JVM堆内存总量（MB） */
    private Double jvmHeapTotal;

    /** JVM堆内存已用（MB） */
    private Double jvmHeapUsed;

    /** 线程数 */
    private Integer threadCount;

    /** 监控时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date monitorTime;

    public Double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public Double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(Double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public Double getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(Double totalMemory) {
        this.totalMemory = totalMemory;
    }

    public Double getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(Double usedMemory) {
        this.usedMemory = usedMemory;
    }

    public Double getTotalDisk() {
        return totalDisk;
    }

    public void setTotalDisk(Double totalDisk) {
        this.totalDisk = totalDisk;
    }

    public Double getUsedDisk() {
        return usedDisk;
    }

    public void setUsedDisk(Double usedDisk) {
        this.usedDisk = usedDisk;
    }

    public Double getDiskUsage() {
        return diskUsage;
    }

    public void setDiskUsage(Double diskUsage) {
        this.diskUsage = diskUsage;
    }

    public Double getUploadSpeed() {
        return uploadSpeed;
    }

    public void setUploadSpeed(Double uploadSpeed) {
        this.uploadSpeed = uploadSpeed;
    }

    public Double getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(Double downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    public Double getJvmHeapUsage() {
        return jvmHeapUsage;
    }

    public void setJvmHeapUsage(Double jvmHeapUsage) {
        this.jvmHeapUsage = jvmHeapUsage;
    }

    public Double getJvmHeapTotal() {
        return jvmHeapTotal;
    }

    public void setJvmHeapTotal(Double jvmHeapTotal) {
        this.jvmHeapTotal = jvmHeapTotal;
    }

    public Double getJvmHeapUsed() {
        return jvmHeapUsed;
    }

    public void setJvmHeapUsed(Double jvmHeapUsed) {
        this.jvmHeapUsed = jvmHeapUsed;
    }

    public Integer getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(Integer threadCount) {
        this.threadCount = threadCount;
    }

    public Date getMonitorTime() {
        return monitorTime;
    }

    public void setMonitorTime(Date monitorTime) {
        this.monitorTime = monitorTime;
    }
}

