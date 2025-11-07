package com.ruoyi.proj_cyq.service;

import java.util.List;
import com.ruoyi.proj_cyq.domain.ClassOperLog;

/**
 * 操作日志记录Service接口
 */
public interface IClassOperLogService {
    /**
     * 查询操作日志记录
     */
    public ClassOperLog selectClassOperLogById(Long operId);

    /**
     * 查询操作日志记录列表
     */
    public List<ClassOperLog> selectClassOperLogList(ClassOperLog classOperLog);

    /**
     * 新增操作日志记录
     */
    public int insertClassOperLog(ClassOperLog classOperLog);

    /**
     * 修改操作日志记录
     */
    public int updateClassOperLog(ClassOperLog classOperLog);

    /**
     * 批量删除操作日志记录
     */
    public int deleteClassOperLogByIds(Long[] operIds);

    /**
     * 删除操作日志记录信息
     */
    public int deleteClassOperLogById(Long operId);

    /**
     * 清空操作日志
     */
    public void cleanClassOperLog();
}