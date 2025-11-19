package com.ruoyi.proj_lwj.service.impl;

import com.ruoyi.proj_lwj.domain.ClassExamParticipant;
import com.ruoyi.proj_lwj.mapper.ClassExamParticipantMapper;
import com.ruoyi.proj_lwj.service.IClassExamParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassExamParticipantServiceImpl implements IClassExamParticipantService {

    @Autowired
    private ClassExamParticipantMapper mapper;

    @Override
    public List<ClassExamParticipant> selectList(ClassExamParticipant p) {
        return mapper.selectList(p);
    }

    @Override
    public ClassExamParticipant selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public ClassExamParticipant selectByExamStudent(Long examId, Long studentId) {
        return mapper.selectByExamStudent(examId, studentId);
    }

    @Override
    public int insert(ClassExamParticipant p) {
        return mapper.insert(p);
    }

    @Override
    public int update(ClassExamParticipant p) {
        return mapper.update(p);
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
}
