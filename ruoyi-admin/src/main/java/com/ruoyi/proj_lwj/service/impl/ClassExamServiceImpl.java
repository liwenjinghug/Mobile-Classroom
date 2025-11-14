package com.ruoyi.proj_lwj.service.impl;

import com.ruoyi.proj_lwj.domain.ClassExam;
import com.ruoyi.proj_lwj.mapper.ClassExamMapper;
import com.ruoyi.proj_lwj.service.IClassExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassExamServiceImpl implements IClassExamService {

    @Autowired
    private ClassExamMapper examMapper;

    @Override
    public List<ClassExam> selectExamList(ClassExam query) {
        return examMapper.selectExamList(query);
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
    public int deleteExamByIds(Long[] ids) {
        return examMapper.deleteExamByIds(ids);
    }

    @Override
    public int updateExamStatus(Long id, Integer status, String updateBy) {
        return examMapper.updateExamStatus(id, status, updateBy);
    }
}

