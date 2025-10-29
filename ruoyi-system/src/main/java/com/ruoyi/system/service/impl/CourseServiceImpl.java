package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.Course;
import com.ruoyi.system.mapper.CourseMapper;
import com.ruoyi.system.service.ICourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CourseServiceImpl implements ICourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Override
    public List<Course> selectCourseList(Course course) {
        return courseMapper.selectCourseList(course);
    }

    @Override
    public Course selectCourseById(Long courseId) {
        return courseMapper.selectCourseById(courseId);
    }

    @Override
    public int insertCourse(Course course) {
        return courseMapper.insertCourse(course);
    }

    @Override
    public int updateCourse(Course course) {
        return courseMapper.updateCourse(course);
    }

    @Override
    public int deleteCourseById(Long courseId) {
        return courseMapper.deleteCourseById(courseId);
    }

    @Override
    public int deleteCourseByIds(Long[] courseIds) {
        return courseMapper.deleteCourseByIds(courseIds);
    }

    @Override
    public String checkCourseCodeUnique(Course course) {
        Course info = courseMapper.checkCourseCodeUnique(course.getCourseCode());
        if (info != null && !info.getCourseId().equals(course.getCourseId())) {
            return "1";
        }
        return "0";
    }
}