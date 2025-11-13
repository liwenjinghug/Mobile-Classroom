// src/main/java/com/ruoyi/proj_cyq/service/impl/ClassNoticeServiceImpl.java
package com.ruoyi.proj_cyq.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.proj_cyq.domain.ClassNotice;
import com.ruoyi.proj_cyq.mapper.ClassNoticeMapper;
import com.ruoyi.proj_cyq.service.IClassNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
}