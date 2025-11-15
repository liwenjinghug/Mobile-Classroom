package com.ruoyi.proj_lwj.mapper;

import com.ruoyi.proj_lwj.domain.ClassExamSession;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ClassExamSessionMapper {

    @Select("SELECT * FROM class_exam_session WHERE exam_id=#{examId}")
    List<ClassExamSession> selectByExamId(Long examId);

    @Insert("INSERT INTO class_exam_session (exam_id, session_id) VALUES (#{examId}, #{sessionId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ClassExamSession rel);

    @Delete("DELETE FROM class_exam_session WHERE exam_id=#{examId}")
    int deleteByExamId(Long examId);

    @Delete("DELETE FROM class_exam_session WHERE exam_id=#{examId} AND session_id=#{sessionId}")
    int deleteOne(@Param("examId") Long examId, @Param("sessionId") Long sessionId);
}

