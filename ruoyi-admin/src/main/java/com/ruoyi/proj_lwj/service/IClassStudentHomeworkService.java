package com.ruoyi.proj_lwj.service;

import com.ruoyi.proj_lwj.domain.ClassStudentHomework;
import java.util.List;

public interface IClassStudentHomeworkService {
    List<ClassStudentHomework> selectByHomeworkId(Long homeworkId);
    ClassStudentHomework selectById(Long id);
    int insert(ClassStudentHomework shw);
    int update(ClassStudentHomework shw);
    int deleteById(Long id);
    List<ClassStudentHomework> selectByStudentId(Long studentId);
    List<ClassStudentHomework> selectByStudentNo(String studentNo);
    List<ClassStudentHomework> selectByStudentIdentifier(String ident);
    int deleteByHomeworkIds(Long[] homeworkIds);
    int updateGrade(ClassStudentHomework shw);
}
