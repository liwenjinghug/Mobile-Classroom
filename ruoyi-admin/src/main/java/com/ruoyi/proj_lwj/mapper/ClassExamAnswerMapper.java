package com.ruoyi.proj_lwj.mapper;

import com.ruoyi.proj_lwj.domain.ClassExamAnswer;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ClassExamAnswerMapper {

    @Select("<script>SELECT * FROM class_exam_answer <where>" +
            "<if test='examId != null'> AND exam_id=#{examId}</if>" +
            "<if test='studentId != null'> AND student_id=#{studentId}</if>" +
            "<if test='questionId != null'> AND question_id=#{questionId}</if>" +
            "</where> ORDER BY id ASC</script>")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "examId", column = "exam_id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "studentNo", column = "student_no"),
            @Result(property = "questionId", column = "question_id"),
            @Result(property = "studentAnswer", column = "student_answer"),
            @Result(property = "answerFiles", column = "answer_files"),
            @Result(property = "isCorrect", column = "is_correct"),
            @Result(property = "score", column = "score"),
            @Result(property = "correctorId", column = "corrector_id"),
            @Result(property = "correctTime", column = "correct_time"),
            @Result(property = "correctComment", column = "correct_comment"),
            @Result(property = "answerDuration", column = "answer_duration"),
            @Result(property = "questionContent", column = "question_content"),
            @Result(property = "questionOptions", column = "question_options"),
            @Result(property = "correctAnswer", column = "correct_answer"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time")
    })
    List<ClassExamAnswer> selectList(ClassExamAnswer a);

    @Select("SELECT * FROM class_exam_answer WHERE id=#{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "examId", column = "exam_id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "studentNo", column = "student_no"),
            @Result(property = "questionId", column = "question_id"),
            @Result(property = "studentAnswer", column = "student_answer"),
            @Result(property = "answerFiles", column = "answer_files"),
            @Result(property = "isCorrect", column = "is_correct"),
            @Result(property = "score", column = "score"),
            @Result(property = "correctorId", column = "corrector_id"),
            @Result(property = "correctTime", column = "correct_time"),
            @Result(property = "correctComment", column = "correct_comment"),
            @Result(property = "answerDuration", column = "answer_duration"),
            @Result(property = "questionContent", column = "question_content"),
            @Result(property = "questionOptions", column = "question_options"),
            @Result(property = "correctAnswer", column = "correct_answer")
    })
    ClassExamAnswer selectById(Long id);

    @Select("SELECT * FROM class_exam_answer WHERE exam_id=#{examId} AND student_id=#{studentId} AND question_id=#{questionId} LIMIT 1")
    ClassExamAnswer selectByExamStudentQuestion(@Param("examId") Long examId, @Param("studentId") Long studentId, @Param("questionId") Long questionId);

    @Insert("INSERT INTO class_exam_answer (exam_id, student_id, student_no, question_id, student_answer, answer_files, is_correct, score, corrector_id, correct_time, correct_comment, answer_duration, question_content, question_options, correct_answer, create_time) VALUES (#{examId}, #{studentId}, #{studentNo}, #{questionId}, #{studentAnswer}, #{answerFiles}, #{isCorrect}, #{score}, #{correctorId}, #{correctTime}, #{correctComment}, #{answerDuration}, #{questionContent}, #{questionOptions}, #{correctAnswer}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ClassExamAnswer a);

    @Update("UPDATE class_exam_answer SET student_answer=#{studentAnswer}, answer_files=#{answerFiles}, is_correct=#{isCorrect}, score=#{score}, corrector_id=#{correctorId}, correct_time=#{correctTime}, correct_comment=#{correctComment}, answer_duration=#{answerDuration}, update_time=NOW() WHERE id=#{id}")
    int update(ClassExamAnswer a);

    @Delete("DELETE FROM class_exam_answer WHERE id=#{id}")
    int deleteById(Long id);

    @Delete("<script>DELETE FROM class_exam_answer WHERE id IN <foreach item='i' collection='array' open='(' separator=',' close=')'>#{i}</foreach></script>")
    int deleteByIds(Long[] ids);

    @Delete("DELETE FROM class_exam_answer WHERE exam_id=#{examId}")
    int deleteByExamId(Long examId);
}
