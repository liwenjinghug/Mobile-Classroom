package com.ruoyi.proj_lw.service;

import com.ruoyi.proj_lw.domain.ClassSession;
import com.ruoyi.proj_lw.domain.ClassSessionStudent;
import java.util.List;

public interface IStudentClassService {

    /**
     * 获取学生已加入的课堂
     */
    List<ClassSessionStudent> getJoinedClasses(Long userId);

    /**
     * 获取学生可申请的课堂
     */
    List<ClassSession> getAvailableClasses(Long userId);

    /**
     * 获取教师管理的课堂
     */
    List<ClassSession> getTeachingClasses(Long teacherId);
}