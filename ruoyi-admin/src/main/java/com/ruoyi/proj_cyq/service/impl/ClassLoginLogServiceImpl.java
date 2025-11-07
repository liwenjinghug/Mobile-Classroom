package com.ruoyi.proj_cyq.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.proj_cyq.mapper.ClassLoginLogMapper;
import com.ruoyi.proj_cyq.domain.ClassLoginLog;
import com.ruoyi.proj_cyq.service.IClassLoginLogService;

/**
 * 系统登录日志Service业务层处理
 */
@Service
public class ClassLoginLogServiceImpl implements IClassLoginLogService {
    @Autowired
    private ClassLoginLogMapper classLoginLogMapper;

    /**
     * 查询系统登录日志
     */
    @Override
    public ClassLoginLog selectClassLoginLogById(Long loginId) {
        return classLoginLogMapper.selectClassLoginLogById(loginId);
    }

    /**
     * 查询系统登录日志列表
     */
    @Override
    public List<ClassLoginLog> selectClassLoginLogList(ClassLoginLog classLoginLog) {
        return classLoginLogMapper.selectClassLoginLogList(classLoginLog);
    }

    /**
     * 新增系统登录日志
     */
    @Override
    public int insertClassLoginLog(ClassLoginLog classLoginLog) {
        return classLoginLogMapper.insertClassLoginLog(classLoginLog);
    }

    /**
     * 修改系统登录日志
     */
    @Override
    public int updateClassLoginLog(ClassLoginLog classLoginLog) {
        return classLoginLogMapper.updateClassLoginLog(classLoginLog);
    }

    /**
     * 批量删除系统登录日志
     */
    @Override
    public int deleteClassLoginLogByIds(Long[] loginIds) {
        return classLoginLogMapper.deleteClassLoginLogByIds(loginIds);
    }

    /**
     * 删除系统登录日志信息
     */
    @Override
    public int deleteClassLoginLogById(Long loginId) {
        return classLoginLogMapper.deleteClassLoginLogById(loginId);
    }

    /**
     * 清空登录日志
     */
    @Override
    public void cleanClassLoginLog() {
        classLoginLogMapper.cleanClassLoginLog();
    }
}