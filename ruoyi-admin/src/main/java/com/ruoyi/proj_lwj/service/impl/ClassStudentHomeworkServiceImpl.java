package com.ruoyi.proj_lwj.service.impl;

import com.ruoyi.proj_lwj.domain.ClassStudentHomework;
import com.ruoyi.proj_lwj.mapper.ClassStudentHomeworkMapper;
import com.ruoyi.proj_lwj.service.IClassStudentHomeworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ClassStudentHomeworkServiceImpl implements IClassStudentHomeworkService {

    @Autowired
    private ClassStudentHomeworkMapper mapper;

    @Override
    public List<ClassStudentHomework> selectByHomeworkId(Long homeworkId) {
        return mapper.selectByHomeworkId(homeworkId);
    }

    @Override
    public ClassStudentHomework selectById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public int insert(ClassStudentHomework shw) {
        return mapper.insert(shw);
    }

    @Override
    public int update(ClassStudentHomework shw) {
        return mapper.update(shw);
    }

    @Override
    public int deleteById(Long id) {
        return mapper.deleteById(id);
    }

    @Override
    public List<ClassStudentHomework> selectByStudentId(Long studentId) {
        return mapper.selectByStudentId(studentId);
    }

    @Override
    public List<ClassStudentHomework> selectByStudentNo(String studentNo) {
        return mapper.selectByStudentNo(studentNo);
    }

    @Override
    public List<ClassStudentHomework> selectByStudentIdentifier(String ident) {
        return mapper.selectByStudentIdentifier(ident);
    }

    @Override
    public int deleteByHomeworkIds(Long[] homeworkIds) {
        return mapper.deleteByHomeworkIds(homeworkIds);
    }

    @Override
    public int updateGrade(ClassStudentHomework shw) {
        return mapper.updateGrade(shw);
    }
}
