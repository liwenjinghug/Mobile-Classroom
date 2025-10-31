package com.ruoyi.proj_lwj.service;

import com.ruoyi.proj_lwj.domain.ClassStudentHomework;
import java.util.List;

public interface IClassStudentHomeworkService {
    List<ClassStudentHomework> selectByHomeworkId(Long homeworkId);
    ClassStudentHomework selectById(Long id);
    int insert(ClassStudentHomework shw);
    int update(ClassStudentHomework shw);
    int deleteById(Long id);
}
