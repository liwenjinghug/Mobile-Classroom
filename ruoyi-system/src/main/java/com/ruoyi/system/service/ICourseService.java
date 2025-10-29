package com.ruoyi.system.service;

import com.ruoyi.system.domain.Course;
import java.util.List;

public interface ICourseService {

    List<Course> selectCourseList(Course course);

    Course selectCourseById(Long courseId);

    int insertCourse(Course course);

    int updateCourse(Course course);

    int deleteCourseById(Long courseId);

    int deleteCourseByIds(Long[] courseIds);

    String checkCourseCodeUnique(Course course);
}
