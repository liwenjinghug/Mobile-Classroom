package com.ruoyi.proj_lwj.service;

import com.ruoyi.proj_lwj.domain.ClassStudent;
import java.util.List;

public interface IClassStudentService {
    List<ClassStudent> selectByCourseCodeAndSessionId(String courseCode, Long sessionId);
}

