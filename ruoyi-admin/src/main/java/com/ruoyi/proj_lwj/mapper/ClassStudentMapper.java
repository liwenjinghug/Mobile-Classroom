package com.ruoyi.proj_lwj.mapper;

import com.ruoyi.proj_lwj.domain.ClassStudent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ClassStudentMapper {

    @Select("SELECT student_id AS studentId, student_no AS studentNo, student_name AS studentName, course_code AS courseCode, session_id AS sessionId FROM class_student WHERE course_code = #{courseCode} AND session_id = #{sessionId} AND status = 1 ORDER BY student_no ASC")
    List<ClassStudent> selectByCourseCodeAndSessionId(@Param("courseCode") String courseCode, @Param("sessionId") Long sessionId);

    @Select("SELECT student_id AS studentId, student_no AS studentNo, student_name AS studentName, course_code AS courseCode, session_id AS sessionId FROM class_student WHERE student_no = #{studentNo} LIMIT 1")
    ClassStudent selectByStudentNo(@Param("studentNo") String studentNo);
}