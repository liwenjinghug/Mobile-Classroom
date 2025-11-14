package com.ruoyi.proj_lwj.service;

import com.ruoyi.proj_lwj.domain.ClassExam;
import java.util.List;

public interface IClassExamService {
    List<ClassExam> selectExamList(ClassExam query);
    ClassExam selectExamById(Long id);
    int insertExam(ClassExam exam);
    int updateExam(ClassExam exam);
    int deleteExamByIds(Long[] ids);
    int updateExamStatus(Long id, Integer status, String updateBy);
}

