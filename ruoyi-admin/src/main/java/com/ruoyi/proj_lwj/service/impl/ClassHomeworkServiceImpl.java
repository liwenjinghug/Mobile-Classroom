package com.ruoyi.proj_lwj.service.impl;

import com.ruoyi.proj_lwj.domain.ClassHomework;
import com.ruoyi.proj_lwj.mapper.ClassHomeworkMapper;
import com.ruoyi.proj_lwj.service.IClassHomeworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ClassHomeworkServiceImpl implements IClassHomeworkService {

    @Autowired
    private ClassHomeworkMapper homeworkMapper;

    @Override
    public List<ClassHomework> selectHomeworkList(ClassHomework hw) {
        return homeworkMapper.selectHomeworkList(hw);
    }

    @Override
    public ClassHomework selectHomeworkById(Long homeworkId) {
        return homeworkMapper.selectHomeworkById(homeworkId);
    }

    @Override
    public int addHomework(ClassHomework hw) {
        return homeworkMapper.insertHomework(hw);
    }

    @Override
    public int editHomework(ClassHomework hw) {
        return homeworkMapper.updateHomework(hw);
    }

    @Override
    public int removeHomeworkByIds(Long[] ids) {
        return homeworkMapper.deleteHomeworkByIds(ids);
    }
}
