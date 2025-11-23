package com.ruoyi.proj_lw.service;

import com.ruoyi.proj_lw.domain.ClassSessionStudent;
import com.ruoyi.proj_lw.domain.ClassStudentLw;

import java.util.List;

/**
 * 课堂管理Service接口
 */
public interface IClassManagementService {

    /**
     * 获取课堂学生列表
     */
    List<ClassSessionStudent> getClassStudents(Long sessionId, ClassStudentLw query);

    /**
     * 搜索所有学生（排除已在指定课堂中的学生）
     */
    List<ClassStudentLw> searchAllStudents(String keyword, Long sessionId);

    /**
     * 添加学生到课堂
     */
    int addStudentsToClass(Long sessionId, List<Long> studentIds);

    /**
     * 从课堂移除学生
     */
    int removeStudentFromClass(Long sessionId, Long studentId);

    /**
     * 批量移除学生
     */
    int batchRemoveStudents(Long sessionId, List<Long> studentIds);
}