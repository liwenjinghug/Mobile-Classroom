package com.ruoyi.proj_lw.mapper;

import com.ruoyi.proj_lw.domain.ClassSession;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ClassSessionMapper {

    @Select("<script>" +
            "SELECT * FROM class_session " +
            "<where>" +
            "   <if test='className != null and className != \"\"'> AND class_name LIKE CONCAT('%', #{className}, '%')</if>" +
            "   <if test='classNumber != null'> AND class_number = #{classNumber}</if>" +
            "   <if test='teacherId != null'> AND teacher_id = #{teacherId}</if>" +
            "   <if test='status != null'> AND status = #{status}</if>" +
            "</where>" +
            "ORDER BY start_time DESC" +
            "</script>")
    @Results({
            @Result(property = "sessionId", column = "session_id"),
            @Result(property = "className", column = "class_name"),
            @Result(property = "teacherId", column = "teacher_id"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "status", column = "status"),
            @Result(property = "totalStudents", column = "total_students"),
            @Result(property = "teacher", column = "teacher"),
            @Result(property = "classNumber", column = "class_number"),
            @Result(property = "createBy", column = "create_by"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateBy", column = "update_by"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<ClassSession> selectSessionList(ClassSession session);

    @Select("SELECT * FROM class_session WHERE session_id = #{sessionId}")
    @Results({
            @Result(property = "sessionId", column = "session_id"),
            @Result(property = "className", column = "class_name"),
            @Result(property = "teacherId", column = "teacher_id"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "status", column = "status"),
            @Result(property = "totalStudents", column = "total_students"),
            @Result(property = "teacher", column = "teacher"),
            @Result(property = "classNumber", column = "class_number"),
            @Result(property = "createBy", column = "create_by"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateBy", column = "update_by"),
            @Result(property = "updateTime", column = "update_time")
    })
    ClassSession selectSessionById(Long sessionId);

    @Select("SELECT * FROM class_session WHERE class_number = #{classNumber} ORDER BY start_time DESC")
    @Results({
            @Result(property = "sessionId", column = "session_id"),
            @Result(property = "className", column = "class_name"),
            @Result(property = "teacherId", column = "teacher_id"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "status", column = "status"),
            @Result(property = "totalStudents", column = "total_students"),
            @Result(property = "teacher", column = "teacher"),
            @Result(property = "classNumber", column = "class_number"),
            @Result(property = "createBy", column = "create_by"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateBy", column = "update_by"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<ClassSession> selectSessionsByClassNumber(Integer classNumber);

    @Insert("INSERT INTO class_session (class_name, teacher_id, start_time, end_time, status, total_students, teacher, class_number, create_by, create_time) " +
            "VALUES (#{className}, #{teacherId}, #{startTime}, #{endTime}, #{status}, #{totalStudents}, #{teacher}, #{classNumber}, #{createBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "sessionId")
    int insertSession(ClassSession session);

    @Update("UPDATE class_session SET " +
            "class_name = #{className}, " +
            "teacher_id = #{teacherId}, " +
            "start_time = #{startTime}, " +
            "end_time = #{endTime}, " +
            "status = #{status}, " +
            "total_students = #{totalStudents}, " +
            "teacher = #{teacher}, " +
            "class_number = #{classNumber}, " +
            "update_by = #{updateBy}, " +
            "update_time = NOW() " +
            "WHERE session_id = #{sessionId}")
    int updateSession(ClassSession session);

    @Delete("DELETE FROM class_session WHERE session_id = #{sessionId}")
    int deleteSessionById(Long sessionId);

    @Delete("<script>" +
            "DELETE FROM class_session WHERE session_id IN " +
            "<foreach item='sessionId' collection='array' open='(' separator=',' close=')'>" +
            "#{sessionId}" +
            "</foreach>" +
            "</script>")
    int deleteSessionByIds(Long[] sessionIds);
}