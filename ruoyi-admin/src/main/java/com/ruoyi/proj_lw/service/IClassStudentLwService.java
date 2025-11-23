package com.ruoyi.proj_lw.service;

import java.util.List;
import com.ruoyi.proj_lw.domain.ClassStudentLw;

/**
 * 学生信息Service接口
 */
public interface IClassStudentLwService {
    /**
     * 查询学生信息
     */
    public ClassStudentLw selectClassStudentByStudentId(Long studentId);

    /**
     * 查询学生信息列表
     */
    public List<ClassStudentLw> selectClassStudentList(ClassStudentLw classStudentLw);

    /**
     * 新增学生信息
     */
    public int insertClassStudent(ClassStudentLw classStudentLw);

    /**
     * 修改学生信息
     */
    public int updateClassStudent(ClassStudentLw classStudentLw);

    /**
     * 批量删除学生信息
     */
    public int deleteClassStudentByStudentIds(Long[] studentIds);

    /**
     * 删除学生信息信息
     */
    public int deleteClassStudentByStudentId(Long studentId);

    // 业务方法

    /**
     * 根据学号查询学生
     */
    ClassStudentLw selectClassStudentByStudentNo(String studentNo);

    /**
     * 根据用户ID查询学生
     */
    ClassStudentLw selectClassStudentByUserId(Long userId);
}