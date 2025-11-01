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
    private ClassStudentMapper mapper;

    @Override
    public List<ClassStudent> selectByCourseCodeAndSessionId(String courseCode, Long sessionId) {
        return mapper.selectByCourseCodeAndSessionId(courseCode, sessionId);
    }
}

