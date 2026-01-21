package com.ruoyi.proj_lwj.mapper;

import com.ruoyi.proj_lwj.domain.ClassStudent;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ClassStudentMapper {

    // 根据课堂 ID 查询该课堂下所有学生
    @Select("SELECT s.student_id AS studentId, s.student_no AS studentNo, s.student_name AS studentName " +
            "FROM class_session_student css " +
            "JOIN class_student s ON css.student_id = s.student_id " +
            "WHERE css.session_id = #{sessionId} " +
            "ORDER BY s.student_no ASC")
    List<ClassStudent> selectBySessionId(@Param("sessionId") Long sessionId);

    // 根据课程 ID 查询该课程下所有课堂（session_id 列表）
    @Select("SELECT cs.session_id FROM class_session cs WHERE cs.course_id = #{courseId} ORDER BY cs.session_id ASC")
    List<Long> selectSessionIdsByCourseId(@Param("courseId") Long courseId);

    // 根据课程 ID 查询该课程下所有去重后的学生
    @Select("SELECT DISTINCT s.student_id AS studentId, s.student_no AS studentNo, s.student_name AS studentName " +
            "FROM class_session_student css " +
            "JOIN class_session cs ON css.session_id = cs.session_id " +
            "JOIN class_student s ON css.student_id = s.student_id " +
            "WHERE cs.course_id = #{courseId} " +
            "ORDER BY s.student_no ASC")
    List<ClassStudent> selectDistinctStudentsByCourseId(@Param("courseId") Long courseId);

    // 保留按学号查询学生基础信息（不依赖课程/课堂）
    @Select("SELECT student_id AS studentId, student_no AS studentNo, student_name AS studentName " +
            "FROM class_student WHERE student_no = #{studentNo} LIMIT 1")
    ClassStudent selectByStudentNo(@Param("studentNo") String studentNo);

    // 保留按 studentId 查询学生基础信息
    @Select("SELECT student_id AS studentId, student_no AS studentNo, student_name AS studentName " +
            "FROM class_student WHERE student_id = #{studentId} LIMIT 1")
    ClassStudent selectClassStudentById(@Param("studentId") Long studentId);

    @Select("SELECT student_id AS studentId, student_no AS studentNo, student_name AS studentName FROM class_student WHERE user_id = #{userId}")
    ClassStudent selectByUserId(@Param("userId") Long userId);
}