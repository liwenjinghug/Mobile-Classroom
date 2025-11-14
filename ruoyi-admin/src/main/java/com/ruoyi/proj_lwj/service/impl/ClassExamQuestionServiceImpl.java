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
    public List<ClassExamQuestion> list(ClassExamQuestion q) { return mapper.list(q); }

    @Override
    public ClassExamQuestion get(Long id) { return mapper.get(id); }

    @Override
    public int insert(ClassExamQuestion q) { return mapper.insert(q); }

    @Override
    public int update(ClassExamQuestion q) { return mapper.update(q); }

    @Override
    public int delete(Long id) { return mapper.delete(id); }

    @Override
    public int deleteBatch(Long[] ids) { return mapper.deleteBatch(ids); }

    @Override
    public int batchReorder(List<ClassExamQuestion> items) { return mapper.batchReorder(items); }
}

