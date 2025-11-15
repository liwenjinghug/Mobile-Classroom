package com.ruoyi.proj_lwj.service.impl;

import com.ruoyi.proj_lwj.domain.ClassExamMonitor;
import com.ruoyi.proj_lwj.mapper.ClassExamMonitorMapper;
import com.ruoyi.proj_lwj.service.IClassExamMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassExamMonitorServiceImpl implements IClassExamMonitorService {

    @Autowired
    private ClassExamMonitorMapper mapper;

    @Override
    public List<ClassExamMonitor> selectList(ClassExamMonitor m) {
        return mapper.selectList(m);
    }

    @Override
    public int insert(ClassExamMonitor m) {
        return mapper.insert(m);
    }

    @Override
    public int updateHandled(ClassExamMonitor m) {
        return mapper.updateHandled(m);
    }

    @Override
    public int deleteById(Long id) {
        return mapper.deleteById(id);
    }
}

