package com.ruoyi.proj_cyq.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.proj_cyq.mapper.ClassOperLogMapper;
import com.ruoyi.proj_cyq.domain.ClassOperLog;
import com.ruoyi.proj_cyq.service.IClassOperLogService;

/**
 * 操作日志记录Service业务层处理
 */
@Service
public class ClassOperLogServiceImpl implements IClassOperLogService {
    @Autowired
    private ClassOperLogMapper classOperLogMapper;

    /**
     * 查询操作日志记录
     */
    @Override
    public ClassOperLog selectClassOperLogById(Long operId) {
        return classOperLogMapper.selectClassOperLogById(operId);
    }

    /**
     * 查询操作日志记录列表
     */
    @Override
    public List<ClassOperLog> selectClassOperLogList(ClassOperLog classOperLog) {
        return classOperLogMapper.selectClassOperLogList(classOperLog);
    }

    /**
     * 新增操作日志记录
     */
    @Override
    public int insertClassOperLog(ClassOperLog classOperLog) {
        return classOperLogMapper.insertClassOperLog(classOperLog);
    }

    /**
     * 修改操作日志记录
     */
    @Override
    public int updateClassOperLog(ClassOperLog classOperLog) {
        return classOperLogMapper.updateClassOperLog(classOperLog);
    }

    /**
     * 批量删除操作日志记录
     */
    @Override
    public int deleteClassOperLogByIds(Long[] operIds) {
        return classOperLogMapper.deleteClassOperLogByIds(operIds);
    }

    /**
     * 删除操作日志记录信息
     */
    @Override
    public int deleteClassOperLogById(Long operId) {
        return classOperLogMapper.deleteClassOperLogById(operId);
    }

    /**
     * 清空操作日志
     */
    @Override
    public void cleanClassOperLog() {
        classOperLogMapper.cleanClassOperLog();
    }
}