package com.ruoyi.proj_lwj.service;

import com.ruoyi.proj_lwj.domain.ClassStudent;
import java.util.List;

public interface IClassStudentService {
    // 根据课堂 ID 查询学生列表
    List<ClassStudent> selectBySessionId(Long sessionId);

    // 根据课程 ID 查询课堂 ID 列表
    List<Long> selectSessionIdsByCourseId(Long courseId);

    // 根据课程 ID 查询去重学生列表
    List<ClassStudent> selectDistinctStudentsByCourseId(Long courseId);

    // 按学号和按 studentId 查询基础信息，供其他模块复用
    ClassStudent selectByStudentNo(String studentNo);

    ClassStudent selectByStudentId(Long studentId);
}
