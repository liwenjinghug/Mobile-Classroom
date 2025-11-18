package com.ruoyi.proj_lwj.service;

import com.ruoyi.proj_lwj.domain.ClassExamAnswer;
import java.util.List;

public interface IClassExamAnswerService {
    List<ClassExamAnswer> selectList(ClassExamAnswer a);
    ClassExamAnswer selectById(Long id);
    ClassExamAnswer selectByExamStudentQuestion(Long examId, Long studentId, Long questionId);
    int insert(ClassExamAnswer a);
    int update(ClassExamAnswer a);
    int deleteById(Long id);
    int deleteByIds(Long[] ids);
    int deleteByExamId(Long examId);
}
