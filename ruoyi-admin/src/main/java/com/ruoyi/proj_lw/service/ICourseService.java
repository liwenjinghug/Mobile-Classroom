package com.ruoyi.proj_lw.service;

import com.ruoyi.proj_lw.domain.Course;

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
