package com.ruoyi.proj_lwj.service.impl;

import com.ruoyi.proj_lwj.domain.ClassExam;
import com.ruoyi.proj_lwj.mapper.ClassExamMapper;
import com.ruoyi.proj_lwj.mapper.ClassExamQuestionMapper;
import com.ruoyi.proj_lwj.service.IClassExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassExamServiceImpl implements IClassExamService {

    @Autowired
    private ClassExamMapper examMapper;
    @Autowired
    private ClassExamQuestionMapper questionMapper;

    @Override
    public List<ClassExam> selectExamList(ClassExam exam) {
        return examMapper.selectExamList(exam);
    }

    @Override
    public ClassExam selectExamById(Long id) {
        return examMapper.selectExamById(id);
    }

    @Override
    public int insertExam(ClassExam exam) {
        return examMapper.insertExam(exam);
    }

    @Override
    public int updateExam(ClassExam exam) {
        return examMapper.updateExam(exam);
    }

    @Override
    public int deleteExamById(Long id) {
        return examMapper.deleteExamById(id);
    }

    @Override
    public int deleteExamByIds(Long[] ids) {
        return examMapper.deleteExamByIds(ids);
    }

    @Override
    public int refreshQuestionCount(Long examId) {
        int cnt = questionMapper.countByExamId(examId);
        ClassExam ex = examMapper.selectExamById(examId);
        if (ex != null) {
            ex.setQuestionCount(cnt);
            examMapper.updateExam(ex);
        }
        return cnt;
    }

    @Override
    public List<ClassExam> selectAvailableByStudentNo(String studentNo) {
        return examMapper.selectAvailableByStudentNo(studentNo);
    }
}
