package com.ruoyi.proj_lwj.service.impl;

import com.ruoyi.proj_lwj.domain.ClassStudent;
import com.ruoyi.proj_lwj.mapper.ClassStudentMapper;
import com.ruoyi.proj_lwj.service.IClassStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassStudentServiceImpl implements IClassStudentService {

    @Autowired
    private ClassStudentMapper classStudentMapper;

    @Override
    public List<ClassStudent> selectBySessionId(Long sessionId) {
        return classStudentMapper.selectBySessionId(sessionId);
    }

    @Override
    public List<Long> selectSessionIdsByCourseId(Long courseId) {
        return classStudentMapper.selectSessionIdsByCourseId(courseId);
    }

    @Override
    public List<ClassStudent> selectDistinctStudentsByCourseId(Long courseId) {
        return classStudentMapper.selectDistinctStudentsByCourseId(courseId);
    }

    @Override
    public ClassStudent selectByStudentNo(String studentNo) {
        return classStudentMapper.selectByStudentNo(studentNo);
    }

    // Implement interface method
    @Override
    public ClassStudent selectByStudentId(Long studentId) {
        return classStudentMapper.selectClassStudentById(studentId);
    }

    // Backward-compatible alias (not part of interface)
    public ClassStudent selectClassStudentById(Long studentId) {
        return classStudentMapper.selectClassStudentById(studentId);
    }
}
