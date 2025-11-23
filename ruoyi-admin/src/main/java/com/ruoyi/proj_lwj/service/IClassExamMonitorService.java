package com.ruoyi.proj_lwj.service;

import com.ruoyi.proj_lwj.domain.ClassExamMonitor;
import java.util.List;

public interface IClassExamMonitorService {
    List<ClassExamMonitor> selectList(ClassExamMonitor m);
    int insert(ClassExamMonitor m);
    int updateHandled(ClassExamMonitor m);
    int deleteById(Long id);
    int deleteByExamId(Long examId);
}
