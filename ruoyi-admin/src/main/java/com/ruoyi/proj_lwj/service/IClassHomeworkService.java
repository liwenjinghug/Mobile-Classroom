package com.ruoyi.proj_lwj.service;

import com.ruoyi.proj_lwj.domain.ClassHomework;
import java.util.List;

public interface IClassHomeworkService {
    List<ClassHomework> selectHomeworkList(ClassHomework hw);
    ClassHomework selectHomeworkById(Long homeworkId);
    int addHomework(ClassHomework hw);
    int editHomework(ClassHomework hw);
    int removeHomeworkByIds(Long[] ids);
}
