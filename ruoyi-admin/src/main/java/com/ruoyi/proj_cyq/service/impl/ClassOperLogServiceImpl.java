package com.ruoyi.proj_cyq.service.impl;

import java.util.List;
import java.util.Map; // 【新增】
import java.util.HashMap; // 【新增】
import java.util.ArrayList; // 【新增】
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

    @Override
    public ClassOperLog selectClassOperLogById(Long operId) {
        return classOperLogMapper.selectClassOperLogById(operId);
    }

    @Override
    public List<ClassOperLog> selectClassOperLogList(ClassOperLog classOperLog) {
        return classOperLogMapper.selectClassOperLogList(classOperLog);
    }

    @Override
    public int insertClassOperLog(ClassOperLog classOperLog) {
        return classOperLogMapper.insertClassOperLog(classOperLog);
    }

    @Override
    public int updateClassOperLog(ClassOperLog classOperLog) {
        return classOperLogMapper.updateClassOperLog(classOperLog);
    }

    @Override
    public int deleteClassOperLogByIds(Long[] operIds) {
        return classOperLogMapper.deleteClassOperLogByIds(operIds);
    }

    @Override
    public int deleteClassOperLogById(Long operId) {
        return classOperLogMapper.deleteClassOperLogById(operId);
    }

    @Override
    public void cleanClassOperLog() {
        classOperLogMapper.cleanClassOperLog();
    }

    // ========== 【新增】统计逻辑实现 ==========
    @Override
    public Map<String, Object> getOperLogStats() {
        Map<String, Object> result = new HashMap<>();

        // 1. 获取原始统计数据 (数据库返回的是数字代码)
        List<Map<String, Object>> titleStats = classOperLogMapper.selectOperLogStatsByTitle();
        List<Map<String, Object>> typeStatsRaw = classOperLogMapper.selectOperLogStatsByType();
        List<Map<String, Object>> statusStatsRaw = classOperLogMapper.selectOperLogStatsByStatus();

        // 2. 计算总数
        int totalCount = 0;
        for (Map<String, Object> item : statusStatsRaw) {
            totalCount += Integer.parseInt(item.get("value").toString());
        }
        result.put("totalCount", totalCount);

        // 3. 模块统计 (直接使用数据库结果)
        result.put("moduleStats", titleStats);

        // 4. 类型统计 (数字转中文)
        List<Map<String, Object>> typeStats = new ArrayList<>();
        // 对应 BusinessType 枚举: 0=其它, 1=新增, 2=修改, 3=删除, 4=授权, 5=导出, 6=导入, 7=强退, 8=生成代码, 9=清空数据
        String[] types = {"其它", "新增", "修改", "删除", "授权", "导出", "导入", "强退", "生成代码", "清空数据"};

        for (Map<String, Object> item : typeStatsRaw) {
            Object nameObj = item.get("name");
            if (nameObj != null) {
                int typeCode = Integer.parseInt(nameObj.toString());
                String name = (typeCode >= 0 && typeCode < types.length) ? types[typeCode] : "未知(" + typeCode + ")";
                Map<String, Object> map = new HashMap<>();
                map.put("name", name);
                map.put("value", item.get("value"));
                typeStats.add(map);
            }
        }
        result.put("typeStats", typeStats);

        // 5. 状态统计 (数字转中文)
        List<Map<String, Object>> statusStats = new ArrayList<>();
        for (Map<String, Object> item : statusStatsRaw) {
            Object nameObj = item.get("name");
            if (nameObj != null) {
                int statusCode = Integer.parseInt(nameObj.toString());
                Map<String, Object> map = new HashMap<>();
                map.put("name", statusCode == 0 ? "成功" : "失败"); // 0正常, 1异常
                map.put("value", item.get("value"));
                statusStats.add(map);
            }
        }
        result.put("statusStats", statusStats);

        return result;
    }
}