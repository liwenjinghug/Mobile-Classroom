package com.ruoyi.proj_cyq.service;

import java.util.List;
import java.util.Map; // 【新增】
import com.ruoyi.proj_cyq.domain.ClassOperLog;

/**
 * 操作日志记录Service接口
 */
public interface IClassOperLogService {
    public ClassOperLog selectClassOperLogById(Long operId);
    public List<ClassOperLog> selectClassOperLogList(ClassOperLog classOperLog);
    public int insertClassOperLog(ClassOperLog classOperLog);
    public int updateClassOperLog(ClassOperLog classOperLog);
    public int deleteClassOperLogByIds(Long[] operIds);
    public int deleteClassOperLogById(Long operId);
    public void cleanClassOperLog();

    /**
     * 【新增】获取操作日志统计信息
     */
    public Map<String, Object> getOperLogStats();
}