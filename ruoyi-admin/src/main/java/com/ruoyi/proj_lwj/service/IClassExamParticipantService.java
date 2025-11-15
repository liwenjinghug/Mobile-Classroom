package com.ruoyi.proj_lwj.service;

import com.ruoyi.proj_lwj.domain.ClassExamParticipant;
import java.util.List;

public interface IClassExamParticipantService {
    List<ClassExamParticipant> selectList(ClassExamParticipant p);
    ClassExamParticipant selectById(Long id);
    ClassExamParticipant selectByExamStudent(Long examId, Long studentId);
    int insert(ClassExamParticipant p);
    int update(ClassExamParticipant p);
    int deleteById(Long id);
    int deleteByIds(Long[] ids);
}

