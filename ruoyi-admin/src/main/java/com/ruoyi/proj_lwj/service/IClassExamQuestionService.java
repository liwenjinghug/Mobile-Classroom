package com.ruoyi.proj_lwj.service;

import com.ruoyi.proj_lwj.domain.ClassExamQuestion;
import java.util.List;

public interface IClassExamQuestionService {
    List<ClassExamQuestion> selectQuestionList(ClassExamQuestion q);
    ClassExamQuestion selectById(Long id);
    int insertQuestion(ClassExamQuestion q);
    int updateQuestion(ClassExamQuestion q);
    int deleteQuestionById(Long id);
    int deleteQuestionByIds(Long[] ids);
    int countByExamId(Long examId);
    default List<ClassExamQuestion> selectList(ClassExamQuestion q){ return selectQuestionList(q); }
    default List<ClassExamQuestion> list(ClassExamQuestion q){ return selectQuestionList(q); }
    default ClassExamQuestion get(Long id){ return selectById(id); }
    default int insert(ClassExamQuestion q){ return insertQuestion(q); }
    default int update(ClassExamQuestion q){ return updateQuestion(q); }
    default int delete(Long id){ return deleteQuestionById(id); }
    default int deleteBatch(Long[] ids){ return deleteQuestionByIds(ids); }
    int batchReorder(List<ClassExamQuestion> items);
}
