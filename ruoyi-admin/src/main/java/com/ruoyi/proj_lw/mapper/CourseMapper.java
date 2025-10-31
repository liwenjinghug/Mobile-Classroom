package com.ruoyi.proj_lw.mapper;

import com.ruoyi.proj_lw.domain.Course;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CourseMapper {

    // 动态查询课程列表（使用 class_course 表）
    @Select("<script>" +
            "SELECT * FROM class_course " +
            "<where>" +
            "   <if test=\"courseName != null and courseName != ''\"> AND course_name LIKE CONCAT('%', #{courseName}, '%')</if> " +
            "   <if test=\"courseCode != null and courseCode != ''\"> AND course_code LIKE CONCAT('%', #{courseCode}, '%')</if> " +
            "   <if test=\"courseType != null and courseType != ''\"> AND course_type = #{courseType}</if> " +
            "   <if test=\"college != null and college != ''\"> AND college LIKE CONCAT('%', #{college}, '%')</if> " +
            "   <if test=\"status != null and status != ''\"> AND status = #{status}</if> " +
            "</where>" +
            "</script>")
    @Results({
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "courseName", column = "course_name"),
            @Result(property = "courseCode", column = "course_code"),
            @Result(property = "courseType", column = "course_type"),
            @Result(property = "college", column = "college"),
            @Result(property = "credit", column = "credit"),
            @Result(property = "introduction", column = "introduction"),
            @Result(property = "status", column = "status"),
            @Result(property = "classNumber", column = "class_number"),
            @Result(property = "createBy", column = "create_by"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateBy", column = "update_by"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<Course> selectCourseList(Course course);

    @Select("SELECT * FROM class_course WHERE course_id = #{courseId}")
    @Results({
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "courseName", column = "course_name"),
            @Result(property = "courseCode", column = "course_code"),
            @Result(property = "courseType", column = "course_type"),
            @Result(property = "college", column = "college"),
            @Result(property = "credit", column = "credit"),
            @Result(property = "introduction", column = "introduction"),
            @Result(property = "status", column = "status"),
            @Result(property = "classNumber", column = "class_number"),
            @Result(property = "createBy", column = "create_by"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateBy", column = "update_by"),
            @Result(property = "updateTime", column = "update_time")
    })
    Course selectCourseById(Long courseId);

    @Insert("INSERT INTO class_course (course_name, course_code, course_type, college, credit, introduction, status, class_number, create_by, create_time) " +
            "VALUES (#{courseName}, #{courseCode}, #{courseType}, #{college}, #{credit}, #{introduction}, #{status}, #{classNumber}, #{createBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "courseId")
    int insertCourse(Course course);

    @Update("UPDATE class_course SET " +
            "course_name = #{courseName}, " +
            "course_code = #{courseCode}, " +
            "course_type = #{courseType}, " +
            "college = #{college}, " +
            "credit = #{credit}, " +
            "introduction = #{introduction}, " +
            "status = #{status}, " +
            "class_number = #{classNumber}, " +
            "update_by = #{updateBy}, " +
            "update_time = NOW() " +
            "WHERE course_id = #{courseId}")
    int updateCourse(Course course);

    @Delete("DELETE FROM class_course WHERE course_id = #{courseId}")
    int deleteCourseById(Long courseId);

    @Delete("<script>DELETE FROM class_course WHERE course_id IN " +
            "<foreach item='courseId' collection='array' open='(' separator=',' close=')'>" +
            "#{courseId}" +
            "</foreach></script>")
    int deleteCourseByIds(Long[] courseIds);

    @Select("SELECT * FROM class_course WHERE course_code = #{courseCode} LIMIT 1")
    @Results({
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "courseName", column = "course_name"),
            @Result(property = "courseCode", column = "course_code"),
            @Result(property = "courseType", column = "course_type"),
            @Result(property = "college", column = "college"),
            @Result(property = "credit", column = "credit"),
            @Result(property = "introduction", column = "introduction"),
            @Result(property = "status", column = "status"),
            @Result(property = "classNumber", column = "class_number")
    })
    Course checkCourseCodeUnique(String courseCode);
}