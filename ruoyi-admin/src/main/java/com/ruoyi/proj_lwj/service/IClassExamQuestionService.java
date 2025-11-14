package com.ruoyi.proj_lwj.service;

import com.ruoyi.proj_lwj.domain.ClassExamQuestion;

import java.util.List;

public interface IClassExamQuestionService {
    List<ClassExamQuestion> list(ClassExamQuestion q);
    ClassExamQuestion get(Long id);
    int insert(ClassExamQuestion q);
    int update(ClassExamQuestion q);
    int delete(Long id);
    int deleteBatch(Long[] ids);
    int batchReorder(List<ClassExamQuestion> items);
}

