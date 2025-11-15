package com.ruoyi.proj_lwj.service.impl;

import com.ruoyi.proj_lwj.domain.ClassExamQuestion;
import com.ruoyi.proj_lwj.mapper.ClassExamQuestionMapper;
import com.ruoyi.proj_lwj.service.IClassExamQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassExamQuestionServiceImpl implements IClassExamQuestionService {

    @Autowired
    private ClassExamQuestionMapper mapper;

    @Override
    public List<ClassExamQuestion> selectQuestionList(ClassExamQuestion q) {
        return mapper.selectQuestionList(q);
    }

    @Override
    public ClassExamQuestion selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public int insertQuestion(ClassExamQuestion q) {
        return mapper.insertQuestion(q);
    }

    @Override
    public int updateQuestion(ClassExamQuestion q) {
        return mapper.updateQuestion(q);
    }

    @Override
    public int deleteQuestionById(Long id) {
        return mapper.deleteQuestionById(id);
    }

    @Override
    public int deleteQuestionByIds(Long[] ids) {
        return mapper.deleteQuestionByIds(ids);
    }

    @Override
    public int countByExamId(Long examId) {
        return mapper.countByExamId(examId);
    }

    @Override
    public int batchReorder(List<ClassExamQuestion> items) {
        if (items == null || items.isEmpty()) return 0;
        return mapper.batchUpdateSortOrder(items);
    }
}
