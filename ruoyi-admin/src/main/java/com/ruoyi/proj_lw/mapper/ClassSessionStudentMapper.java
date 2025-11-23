package com.ruoyi.proj_lw.mapper;

import java.util.List;

import org.apache.ibatis.annotations.*;
import com.ruoyi.proj_lw.domain.ClassSessionStudent;

/**
 * 课堂学生关联Mapper接口
 */
@Mapper
public interface ClassSessionStudentMapper {
    /**
     * 查询课堂学生关联
     */
    @Select("SELECT * FROM class_session_student WHERE session_id = #{sessionId} AND student_id = #{studentId}")
    ClassSessionStudent selectClassSessionStudent(@Param("sessionId") Long sessionId, @Param("studentId") Long studentId);

    /**
     * 查询课堂学生关联列表
     */
    @Select("SELECT css.*, cs.student_name, cs.student_no, cs.gender, cs.class_name, cs.status " +
            "FROM class_session_student css " +
            "LEFT JOIN class_student cs ON css.student_id = cs.student_id " +
            "LEFT JOIN class_session s ON css.session_id = s.session_id " +
            "WHERE css.session_id = #{sessionId}")
    List<ClassSessionStudent> selectClassSessionStudentList(@Param("sessionId") Long sessionId);

    /**
     * 新增课堂学生关联
     */
    @Insert("INSERT INTO class_session_student (session_id, student_id, assigned_at) " +
            "VALUES (#{sessionId}, #{studentId}, #{assignedAt})")
    int insertClassSessionStudent(ClassSessionStudent classSessionStudent);

    /**
     * 删除课堂学生关联
     */
    @Delete("DELETE FROM class_session_student WHERE session_id = #{sessionId} AND student_id = #{studentId}")
    int deleteClassSessionStudent(@Param("sessionId") Long sessionId, @Param("studentId") Long studentId);

    // 自定义方法

    /**
     * 批量插入学生到课堂
     */
    @Insert({"<script>",
            "INSERT INTO class_session_student (session_id, student_id, assigned_at) VALUES ",
            "<foreach collection='list' item='item' separator=','>",
            "(#{item.sessionId}, #{item.studentId}, #{item.assignedAt})",
            "</foreach>",
            "</script>"})
    int batchInsertClassSessionStudent(@Param("list") List<ClassSessionStudent> list);

    /**
     * 检查学生是否已在课堂中
     */
    @Select("SELECT COUNT(1) FROM class_session_student " +
            "WHERE session_id = #{sessionId} AND student_id = #{studentId}")
    int checkStudentInSession(@Param("sessionId") Long sessionId, @Param("studentId") Long studentId);

    /**
     * 获取学生已加入的课堂ID列表
     */
    @Select("SELECT session_id FROM class_session_student WHERE student_id = #{studentId}")
    List<Long> selectJoinedSessionIds(@Param("studentId") Long studentId);

    /**
     * 根据学号查找学生ID
     */
    @Select("SELECT student_id FROM class_student WHERE student_no = #{studentNo}")
    Long selectStudentIdByStudentNo(@Param("studentNo") String studentNo);

    /**
     * 根据课堂ID删除所有学生关联
     */
    @Delete("DELETE FROM class_session_student WHERE session_id = #{sessionId}")
    int deleteClassSessionStudentBySessionId(Long sessionId);

    /**
     * 查询学生已加入的课堂详细信息（包含上课时间）
     */
    @Select("SELECT css.*, s.class_name, s.teacher, s.week_day, s.start_time, s.end_time, s.class_duration, s.status " +
            "FROM class_session_student css " +
            "LEFT JOIN class_session s ON css.session_id = s.session_id " +
            "WHERE css.student_id = #{studentId}")
    @Results({
            @Result(property = "sessionId", column = "session_id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "assignedAt", column = "assigned_at"),
            @Result(property = "className", column = "class_name"),
            @Result(property = "teacher", column = "teacher"),
            @Result(property = "weekDay", column = "week_day"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "classDuration", column = "class_duration"),
            @Result(property = "status", column = "status")
    })
    List<ClassSessionStudent> selectJoinedClassesWithDetail(@Param("studentId") Long studentId);
}