package com.ruoyi.proj_cyq.service.impl;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
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

    @Override
    public ClassLoginLog selectClassLoginLogById(Long loginId) {
        return classLoginLogMapper.selectClassLoginLogById(loginId);
    }

    @Override
    public List<ClassLoginLog> selectClassLoginLogList(ClassLoginLog classLoginLog) {
        return classLoginLogMapper.selectClassLoginLogList(classLoginLog);
    }

    @Override
    public int insertClassLoginLog(ClassLoginLog classLoginLog) {
        return classLoginLogMapper.insertClassLoginLog(classLoginLog);
    }

    @Override
    public int updateClassLoginLog(ClassLoginLog classLoginLog) {
        return classLoginLogMapper.updateClassLoginLog(classLoginLog);
    }

    @Override
    public int deleteClassLoginLogByIds(Long[] loginIds) {
        return classLoginLogMapper.deleteClassLoginLogByIds(loginIds);
    }

    @Override
    public int deleteClassLoginLogById(Long loginId) {
        return classLoginLogMapper.deleteClassLoginLogById(loginId);
    }

    @Override
    public void cleanClassLoginLog() {
        classLoginLogMapper.cleanClassLoginLog();
    }

    // ========== 统计逻辑实现 ==========
    @Override
    public Map<String, Object> getLoginLogStats() {
        Map<String, Object> result = new HashMap<>();

        // 1. 获取原始统计数据
        List<Map<String, Object>> statusStatsRaw = classLoginLogMapper.selectLoginLogStatsByStatus();
        List<Map<String, Object>> browserStats = classLoginLogMapper.selectLoginLogStatsByBrowser();
        List<Map<String, Object>> osStats = classLoginLogMapper.selectLoginLogStatsByOs();

        // 2. 计算总数并处理状态名称 (0=成功, 1=失败)
        int totalCount = 0;
        List<Map<String, Object>> statusStats = new ArrayList<>();

        for (Map<String, Object> item : statusStatsRaw) {
            Object valueObj = item.get("value");
            if (valueObj != null) {
                totalCount += Integer.parseInt(valueObj.toString());
            }

            // 转换状态码为中文
            Object nameObj = item.get("name");
            if (nameObj != null) {
                int statusCode = Integer.parseInt(nameObj.toString());
                Map<String, Object> map = new HashMap<>();
                map.put("name", statusCode == 0 ? "成功" : "失败");
                map.put("value", item.get("value"));
                statusStats.add(map);
            }
        }

        result.put("totalCount", totalCount);
        result.put("statusStats", statusStats);
        result.put("browserStats", browserStats);
        result.put("osStats", osStats);

        return result;
    }
}