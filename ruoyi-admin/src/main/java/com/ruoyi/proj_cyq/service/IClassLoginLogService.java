package com.ruoyi.proj_cyq.service;

import java.util.List;
import com.ruoyi.proj_cyq.domain.ClassLoginLog;

/**
 * 系统登录日志Service接口
 */
public interface IClassLoginLogService {
    /**
     * 查询系统登录日志
     */
    public ClassLoginLog selectClassLoginLogById(Long loginId);

    /**
     * 查询系统登录日志列表
     */
    public List<ClassLoginLog> selectClassLoginLogList(ClassLoginLog classLoginLog);

    /**
     * 新增系统登录日志
     */
    public int insertClassLoginLog(ClassLoginLog classLoginLog);

    /**
     * 修改系统登录日志
     */
    public int updateClassLoginLog(ClassLoginLog classLoginLog);

    /**
     * 批量删除系统登录日志
     */
    public int deleteClassLoginLogByIds(Long[] loginIds);

    /**
     * 删除系统登录日志信息
     */
    public int deleteClassLoginLogById(Long loginId);

    /**
     * 清空登录日志
     */
    public void cleanClassLoginLog();
}