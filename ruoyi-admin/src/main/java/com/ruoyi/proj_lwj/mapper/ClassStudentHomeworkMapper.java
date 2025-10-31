package com.ruoyi.proj_lwj.mapper;

import com.ruoyi.proj_lwj.domain.ClassStudentHomework;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ClassStudentHomeworkMapper {

    @Select("SELECT csh.*, su.nick_name AS student_name FROM class_student_homework csh LEFT JOIN sys_user su ON csh.student_id = su.user_id WHERE csh.homework_id = #{homeworkId}")
    @Results({
            @Result(property = "studentHomeworkId", column = "student_homework_id"),
            @Result(property = "homeworkId", column = "homework_id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "submissionFiles", column = "submission_files"),
            @Result(property = "submitTime", column = "submit_time"),
            @Result(property = "score", column = "score"),
            @Result(property = "status", column = "status"),
            @Result(property = "remark", column = "remark"),
            @Result(property = "studentName", column = "student_name")
    })
    List<ClassStudentHomework> selectByHomeworkId(Long homeworkId);

    @Select("SELECT csh.*, su.nick_name AS student_name FROM class_student_homework csh LEFT JOIN sys_user su ON csh.student_id = su.user_id WHERE csh.student_homework_id = #{id}")
    ClassStudentHomework selectById(Long id);

    @Insert("INSERT INTO class_student_homework (homework_id, student_id, submission_files, submit_time, status, create_by, create_time) " +
            "VALUES (#{homeworkId}, #{studentId}, #{submissionFiles}, #{submitTime}, #{status}, #{createBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "studentHomeworkId")
    int insert(ClassStudentHomework shw);

    @Update("UPDATE class_student_homework SET submission_files=#{submissionFiles}, submit_time=#{submitTime}, status=#{status}, score=#{score}, remark=#{remark}, update_by=#{updateBy}, update_time=NOW() WHERE student_homework_id=#{studentHomeworkId}")
    int update(ClassStudentHomework shw);

    @Delete("DELETE FROM class_student_homework WHERE student_homework_id = #{id}")
    int deleteById(Long id);
}
