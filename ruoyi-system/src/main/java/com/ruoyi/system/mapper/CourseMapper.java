package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.Course;
import java.util.List;

public interface CourseMapper {
    List<Course> selectCourseList(Course course);
    Course selectCourseById(Long courseId);
    int insertCourse(Course course);
    int updateCourse(Course course);
    int deleteCourseById(Long courseId);
    int deleteCourseByIds(Long[] courseIds);
    Course checkCourseCodeUnique(String courseCode);
}