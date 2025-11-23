package com.ruoyi.proj_lw.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.proj_lw.domain.ClassStudentLw;
import com.ruoyi.proj_lw.mapper.ClassStudentLwMapper;
import com.ruoyi.proj_lw.service.IClassStudentLwService;

/**
 * 学生信息Service业务层处理
 */
@Service("classStudentServiceLw")
public class ClassStudentLwServiceImpl implements IClassStudentLwService {

    private final ClassStudentLwMapper classStudentLwMapper;

    @Autowired
    public ClassStudentLwServiceImpl(ClassStudentLwMapper classStudentLwMapper) {
        this.classStudentLwMapper = classStudentLwMapper;
    }

    /**
     * 查询学生信息
     */
    @Override
    public ClassStudentLw selectClassStudentByStudentId(Long studentId) {
        return classStudentLwMapper.selectClassStudentByStudentId(studentId);
    }

    /**
     * 查询学生信息列表
     */
    @Override
    public List<ClassStudentLw> selectClassStudentList(ClassStudentLw classStudentLw) {
        return classStudentLwMapper.selectClassStudentList(classStudentLw);
    }

    /**
     * 新增学生信息
     */
    @Override
    public int insertClassStudent(ClassStudentLw classStudentLw) {
        return classStudentLwMapper.insertClassStudent(classStudentLw);
    }

    /**
     * 修改学生信息
     */
    @Override
    public int updateClassStudent(ClassStudentLw classStudentLw) {
        return classStudentLwMapper.updateClassStudent(classStudentLw);
    }

    /**
     * 批量删除学生信息
     */
    @Override
    public int deleteClassStudentByStudentIds(Long[] studentIds) {
        return classStudentLwMapper.deleteClassStudentByStudentIds(studentIds);
    }

    /**
     * 删除学生信息信息
     */
    @Override
    public int deleteClassStudentByStudentId(Long studentId) {
        return classStudentLwMapper.deleteClassStudentByStudentId(studentId);
    }

    /**
     * 根据学号查询学生
     */
    @Override
    public ClassStudentLw selectClassStudentByStudentNo(String studentNo) {
        return classStudentLwMapper.selectClassStudentByStudentNo(studentNo);
    }

    /**
     * 根据用户ID查询学生
     */
    @Override
    public ClassStudentLw selectClassStudentByUserId(Long userId) {
        return classStudentLwMapper.selectClassStudentByUserId(userId);
    }
}