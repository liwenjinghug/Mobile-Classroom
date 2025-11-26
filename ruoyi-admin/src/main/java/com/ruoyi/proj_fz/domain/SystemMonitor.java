package com.ruoyi.proj_fz.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

/**
 * 系统监控记录对象
 *
 * @author proj_fz
 */
public class SystemMonitor extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 监控ID */
    @Excel(name = "监控ID")
    private Long monitorId;

    /** 监控类型：1-服务器监控 2-数据库监控 3-用户行为监控 4-功能模块监控 5-接口性能监控 6-异常监控 */
    @Excel(name = "监控类型", readConverterExp = "1=服务器监控,2=数据库监控,3=用户行为监控,4=功能模块监控,5=接口性能监控,6=异常监控")
    private Integer monitorType;

    /** 监控项名称 */
    @Excel(name = "监控项名称")
    private String monitorName;

    /** 监控指标JSON */
    private String metrics;

    /** 异常级别：0-正常 1-警告 2-严重 */
    @Excel(name = "告警级别", readConverterExp = "0=正常,1=警告,2=严重")
    private Integer alertLevel;

    /** 异常描述 */
    @Excel(name = "告警描述")
    private String alertDesc;

    /** 状态：0-正常 1-异常 */
    @Excel(name = "状态", readConverterExp = "0=正常,1=异常")
    private Integer status;

    /** 监控时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "监控时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date monitorTime;

    /** 是否已处理：0-未处理 1-已处理 */
    @Excel(name = "处理状态", readConverterExp = "0=未处理,1=已处理")
    private Integer handled;

    /** 处理人 */
    @Excel(name = "处理人")
    private String handler;

    /** 处理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "处理时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date handleTime;

    /** 处理备注 */
    @Excel(name = "处理备注")
    private String handleRemark;

    public Long getMonitorId() {
        return monitorId;
    }

    public void setMonitorId(Long monitorId) {
        this.monitorId = monitorId;
    }

    public Integer getMonitorType() {
        return monitorType;
    }

    public void setMonitorType(Integer monitorType) {
        this.monitorType = monitorType;
    }

    public String getMonitorName() {
        return monitorName;
    }

    public void setMonitorName(String monitorName) {
        this.monitorName = monitorName;
    }

    public String getMetrics() {
        return metrics;
    }

    public void setMetrics(String metrics) {
        this.metrics = metrics;
    }

    public Integer getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(Integer alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getAlertDesc() {
        return alertDesc;
    }

    public void setAlertDesc(String alertDesc) {
        this.alertDesc = alertDesc;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getMonitorTime() {
        return monitorTime;
    }

    public void setMonitorTime(Date monitorTime) {
        this.monitorTime = monitorTime;
    }

    public Integer getHandled() {
        return handled;
    }

    public void setHandled(Integer handled) {
        this.handled = handled;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public Date getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(Date handleTime) {
        this.handleTime = handleTime;
    }

    public String getHandleRemark() {
        return handleRemark;
    }

    public void setHandleRemark(String handleRemark) {
        this.handleRemark = handleRemark;
    }
}

