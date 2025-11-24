// src/main/java/com/ruoyi/proj_cyq/service/impl/ClassNoticeServiceImpl.java
package com.ruoyi.proj_cyq.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.proj_cyq.domain.ClassNotice;
import com.ruoyi.proj_cyq.mapper.ClassNoticeMapper;
import com.ruoyi.proj_cyq.service.IClassNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map; // 【新增】
import java.util.HashMap; // 【新增】

@Service
public class ClassNoticeServiceImpl implements IClassNoticeService {

    @Autowired
    private ClassNoticeMapper classNoticeMapper;

    @Override
    public List<ClassNotice> selectClassNoticeList(ClassNotice notice) {
        return classNoticeMapper.selectClassNoticeList(notice);
    }

    @Override
    public ClassNotice selectClassNoticeById(Long noticeId) {
        return classNoticeMapper.selectClassNoticeById(noticeId);
    }

    @Override
    public int insertClassNotice(ClassNotice notice) {
        notice.setCreateBy(SecurityUtils.getUsername());
        return classNoticeMapper.insertClassNotice(notice);
    }

    @Override
    public int updateClassNotice(ClassNotice notice) {
        notice.setUpdateBy(SecurityUtils.getUsername());
        return classNoticeMapper.updateClassNotice(notice);
    }

    @Override
    public int deleteClassNoticeById(Long noticeId) {
        ClassNotice notice = new ClassNotice();
        notice.setNoticeId(noticeId);
        notice.setUpdateBy(SecurityUtils.getUsername());
        return classNoticeMapper.deleteClassNoticeById(notice);
    }

    @Override
    public int deleteClassNoticeByIds(Long[] noticeIds) {
        return classNoticeMapper.deleteClassNoticeByIds(noticeIds, SecurityUtils.getUsername());
    }

    // ========== 【新增】统计逻辑实现 ==========
    @Override
    public Map<String, Object> getNoticeStats() {
        Map<String, Object> result = new HashMap<>();

        // 1. 获取统计数据
        List<Map<String, Object>> creatorStats = classNoticeMapper.selectNoticeStatsByCreator();
        List<Map<String, Object>> dateStats = classNoticeMapper.selectNoticeStatsByDate();

        // 2. 计算总数
        int totalCount = 0;
        for (Map<String, Object> item : creatorStats) {
            totalCount += Integer.parseInt(item.get("value").toString());
        }

        result.put("totalCount", totalCount);
        result.put("creatorStats", creatorStats);
        result.put("dateStats", dateStats);

        return result;
    }
}