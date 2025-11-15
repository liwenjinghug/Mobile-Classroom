package com.ruoyi.proj_lwj.service;

import com.ruoyi.proj_lwj.domain.ClassExam;
import java.util.List;

public interface IClassExamService {
    List<ClassExam> selectExamList(ClassExam exam);
    ClassExam selectExamById(Long id);
    int insertExam(ClassExam exam);
    int updateExam(ClassExam exam);
    int deleteExamById(Long id);
    int deleteExamByIds(Long[] ids);
    int refreshQuestionCount(Long examId);
    List<ClassExam> selectAvailableByStudentNo(String studentNo);
}
