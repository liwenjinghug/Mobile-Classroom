package com.ruoyi.proj_cyq.service;

import java.util.List;
import java.util.Map; // 【新增】
import com.ruoyi.proj_cyq.domain.ClassLoginLog;

/**
 * 系统登录日志Service接口
 */
public interface IClassLoginLogService {
    public ClassLoginLog selectClassLoginLogById(Long loginId);
    public List<ClassLoginLog> selectClassLoginLogList(ClassLoginLog classLoginLog);
    public int insertClassLoginLog(ClassLoginLog classLoginLog);
    public int updateClassLoginLog(ClassLoginLog classLoginLog);
    public int deleteClassLoginLogByIds(Long[] loginIds);
    public int deleteClassLoginLogById(Long loginId);
    public void cleanClassLoginLog();

    /**
     * 【新增】获取登录日志统计信息
     */
    public Map<String, Object> getLoginLogStats();
}