package com.ruoyi.proj_lwj.service.impl;

import com.ruoyi.proj_lwj.domain.ClassExamAnswer;
import com.ruoyi.proj_lwj.mapper.ClassExamAnswerMapper;
import com.ruoyi.proj_lwj.service.IClassExamAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassExamAnswerServiceImpl implements IClassExamAnswerService {

    @Autowired
    private ClassExamAnswerMapper mapper;

    @Override
    public List<ClassExamAnswer> selectList(ClassExamAnswer a) {
        return mapper.selectList(a);
    }

    @Override
    public ClassExamAnswer selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public ClassExamAnswer selectByExamStudentQuestion(Long examId, Long studentId, Long questionId) {
        return mapper.selectByExamStudentQuestion(examId, studentId, questionId);
    }

    @Override
    public int insert(ClassExamAnswer a) {
        return mapper.insert(a);
    }

    @Override
    public int update(ClassExamAnswer a) {
        return mapper.update(a);
    }

    @Override
    public int deleteById(Long id) {
        return mapper.deleteById(id);
    }

    @Override
    public int deleteByIds(Long[] ids) {
        return mapper.deleteByIds(ids);
    }

    @Override
    public int deleteByExamId(Long examId) { return mapper.deleteByExamId(examId); }

    @Override
    public List<ClassExamAnswer> selectUngradedSubjective(Long examId) { return mapper.selectUngradedSubjective(examId); }

    @Override
    public int gradeOne(ClassExamAnswer a) { return mapper.gradeOne(a); }

    @Override
    public int gradeBatch(List<ClassExamAnswer> list) { return mapper.gradeBatch(list); }
}
