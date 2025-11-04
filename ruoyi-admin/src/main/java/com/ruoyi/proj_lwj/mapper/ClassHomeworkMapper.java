package com.ruoyi.proj_lwj.mapper;

import com.ruoyi.proj_lwj.domain.ClassHomework;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ClassHomeworkMapper {

    @Select("<script>SELECT ch.*, cc.course_name AS course_name, cs.class_name AS class_name FROM class_homework ch LEFT JOIN class_course cc ON ch.course_id = cc.course_id LEFT JOIN class_session cs ON ch.session_id = cs.session_id <where> " +
            "<if test=\"title != null and title != ''\"> AND ch.title LIKE CONCAT('%', #{title}, '%')</if> " +
            "<if test=\"courseId != null\"> AND ch.course_id = #{courseId}</if> " +
            "<if test=\"sessionId != null\"> AND ch.session_id = #{sessionId}</if> " +
            "</where> ORDER BY ch.create_time DESC</script>")
    @Results({
            @Result(property = "homeworkId", column = "homework_id"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "sessionId", column = "session_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "totalScore", column = "total_score"),
            @Result(property = "deadline", column = "deadline"),
            @Result(property = "attachments", column = "attachments"),
            @Result(property = "createBy", column = "create_by"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "courseName", column = "course_name"),
            @Result(property = "className", column = "class_name")
    })
    List<ClassHomework> selectHomeworkList(ClassHomework hw);

    @Select("SELECT ch.*, cc.course_name AS course_name, cs.class_name AS class_name FROM class_homework ch LEFT JOIN class_course cc ON ch.course_id = cc.course_id LEFT JOIN class_session cs ON ch.session_id = cs.session_id WHERE ch.homework_id = #{homeworkId}")
    @Results({
            @Result(property = "homeworkId", column = "homework_id"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "sessionId", column = "session_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "totalScore", column = "total_score"),
            @Result(property = "deadline", column = "deadline"),
            @Result(property = "attachments", column = "attachments"),
            @Result(property = "createBy", column = "create_by"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "courseName", column = "course_name"),
            @Result(property = "className", column = "class_name")
    })
    ClassHomework selectHomeworkById(Long homeworkId);

    @Insert("INSERT INTO class_homework (course_id, session_id, title, content, total_score, deadline, attachments, create_by, create_time) " +
            "VALUES (#{courseId}, #{sessionId}, #{title}, #{content}, #{totalScore}, #{deadline}, #{attachments}, #{createBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "homeworkId")
    int insertHomework(ClassHomework hw);

    @Update("UPDATE class_homework SET course_id=#{courseId}, session_id=#{sessionId}, title=#{title}, content=#{content}, total_score=#{totalScore}, deadline=#{deadline}, attachments=#{attachments}, update_by=#{updateBy}, update_time=NOW() WHERE homework_id=#{homeworkId}")
    int updateHomework(ClassHomework hw);

    @Delete("DELETE FROM class_homework WHERE homework_id = #{homeworkId}")
    int deleteHomeworkById(Long homeworkId);

    @Delete("<script>DELETE FROM class_homework WHERE homework_id IN <foreach item='id' collection='array' open='(' separator=',' close=')'> #{id} </foreach></script>")
    int deleteHomeworkByIds(Long[] homeworkIds);
}