package com.ruoyi.proj_lwj.mapper;

import com.ruoyi.proj_lwj.domain.ClassExamQuestion;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ClassExamQuestionMapper {

    @Select("<script>SELECT * FROM class_exam_question <where>" +
            "<if test='examId != null'> AND exam_id = #{examId}</if>" +
            "<if test='questionType != null'> AND question_type = #{questionType}</if>" +
            "</where> ORDER BY sort_order ASC, id ASC</script>")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "examId", column = "exam_id"),
            @Result(property = "questionType", column = "question_type"),
            @Result(property = "questionContent", column = "question_content"),
            @Result(property = "questionOptions", column = "question_options"),
            @Result(property = "correctAnswer", column = "correct_answer"),
            @Result(property = "analysis", column = "analysis"),
            @Result(property = "score", column = "score"),
            @Result(property = "difficulty", column = "difficulty"),
            @Result(property = "sortOrder", column = "sort_order"),
            @Result(property = "createBy", column = "create_by"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateBy", column = "update_by"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<ClassExamQuestion> selectQuestionList(ClassExamQuestion q);

    @Select("SELECT * FROM class_exam_question WHERE id=#{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "examId", column = "exam_id"),
            @Result(property = "questionType", column = "question_type"),
            @Result(property = "questionContent", column = "question_content"),
            @Result(property = "questionOptions", column = "question_options"),
            @Result(property = "correctAnswer", column = "correct_answer"),
            @Result(property = "analysis", column = "analysis"),
            @Result(property = "score", column = "score"),
            @Result(property = "difficulty", column = "difficulty"),
            @Result(property = "sortOrder", column = "sort_order"),
            @Result(property = "createBy", column = "create_by"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateBy", column = "update_by"),
            @Result(property = "updateTime", column = "update_time")
    })
    ClassExamQuestion selectById(Long id);

    @Insert("INSERT INTO class_exam_question (exam_id, question_type, question_content, question_options, correct_answer, analysis, score, difficulty, sort_order, create_by, create_time) VALUES (#{examId}, #{questionType}, #{questionContent}, #{questionOptions}, #{correctAnswer}, #{analysis}, #{score}, #{difficulty}, #{sortOrder}, #{createBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertQuestion(ClassExamQuestion q);

    @Update("UPDATE class_exam_question SET question_type=#{questionType}, question_content=#{questionContent}, question_options=#{questionOptions}, correct_answer=#{correctAnswer}, analysis=#{analysis}, score=#{score}, difficulty=#{difficulty}, sort_order=#{sortOrder}, update_by=#{updateBy}, update_time=NOW() WHERE id=#{id}")
    int updateQuestion(ClassExamQuestion q);

    @Delete("DELETE FROM class_exam_question WHERE id=#{id}")
    int deleteQuestionById(Long id);

    @Delete("<script>DELETE FROM class_exam_question WHERE id IN <foreach item='i' collection='array' open='(' separator=',' close=')'>#{i}</foreach></script>")
    int deleteQuestionByIds(Long[] ids);

    @Select("SELECT COUNT(0) FROM class_exam_question WHERE exam_id=#{examId}")
    int countByExamId(Long examId);

    @Update("<script><foreach collection='list' item='item' separator=';'>UPDATE class_exam_question SET sort_order=#{item.sortOrder}, update_by=#{item.updateBy}, update_time=NOW() WHERE id=#{item.id}</foreach></script>")
    int batchUpdateSortOrder(@Param("list") List<ClassExamQuestion> list);

    @Delete("DELETE FROM class_exam_question WHERE exam_id=#{examId}")
    int deleteByExamId(Long examId);

    // New: count duplicate question content within an exam (optionally excluding a question id)
    @Select("<script>SELECT COUNT(1) FROM class_exam_question WHERE exam_id=#{examId} AND question_content=#{questionContent} <if test='excludeId != null'>AND id != #{excludeId}</if></script>")
    int countByExamAndContent(@Param("examId") Long examId, @Param("questionContent") String questionContent, @Param("excludeId") Long excludeId);
}
