package com.ruoyi.proj_lwj.mapper;

import com.ruoyi.proj_lwj.domain.ClassExam;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ClassExamMapper {

    @Select("<script>SELECT e.*, cc.course_name AS course_name, cs.class_name AS class_name FROM class_exam e " +
            "LEFT JOIN class_course cc ON e.course_id = cc.course_id " +
            "LEFT JOIN class_session cs ON e.session_id = cs.session_id " +
            "<where> " +
            "<if test='examName != null and examName != \"\"'> AND e.exam_name LIKE CONCAT('%', #{examName}, '%')</if> " +
            "<if test='courseId != null'> AND e.course_id = #{courseId}</if> " +
            "<if test='sessionId != null'> AND e.session_id = #{sessionId}</if> " +
            "<if test='status != null'> AND e.status = #{status}</if> " +
            "</where> ORDER BY e.start_time DESC</script>")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "examName", column = "exam_name"),
            @Result(property = "examType", column = "exam_type"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "sessionId", column = "session_id"),
            @Result(property = "totalScore", column = "total_score"),
            @Result(property = "passScore", column = "pass_score"),
            @Result(property = "examDuration", column = "exam_duration"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "examMode", column = "exam_mode"),
            @Result(property = "antiCheat", column = "anti_cheat"),
            @Result(property = "questionOrder", column = "question_order"),
            @Result(property = "showAnswer", column = "show_answer"),
            @Result(property = "status", column = "status"),
            @Result(property = "lateSubmit", column = "late_submit"),
            @Result(property = "lateTime", column = "late_time"),
            @Result(property = "autoSubmit", column = "auto_submit"),
            @Result(property = "studentCount", column = "student_count"),
            @Result(property = "questionCount", column = "question_count"),
            @Result(property = "courseName", column = "course_name"),
            @Result(property = "className", column = "class_name")
    })
    List<ClassExam> selectExamList(ClassExam exam);

    @Select("SELECT e.*, cc.course_name AS course_name, cs.class_name AS class_name FROM class_exam e LEFT JOIN class_course cc ON e.course_id = cc.course_id LEFT JOIN class_session cs ON e.session_id = cs.session_id WHERE e.id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "examName", column = "exam_name"),
            @Result(property = "examType", column = "exam_type"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "sessionId", column = "session_id"),
            @Result(property = "totalScore", column = "total_score"),
            @Result(property = "passScore", column = "pass_score"),
            @Result(property = "examDuration", column = "exam_duration"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "examMode", column = "exam_mode"),
            @Result(property = "antiCheat", column = "anti_cheat"),
            @Result(property = "questionOrder", column = "question_order"),
            @Result(property = "showAnswer", column = "show_answer"),
            @Result(property = "status", column = "status"),
            @Result(property = "lateSubmit", column = "late_submit"),
            @Result(property = "lateTime", column = "late_time"),
            @Result(property = "autoSubmit", column = "auto_submit"),
            @Result(property = "studentCount", column = "student_count"),
            @Result(property = "questionCount", column = "question_count"),
            @Result(property = "courseName", column = "course_name"),
            @Result(property = "className", column = "class_name")
    })
    ClassExam selectExamById(Long id);

    @Insert("INSERT INTO class_exam (exam_name, exam_type, course_id, session_id, total_score, pass_score, exam_duration, start_time, end_time, exam_mode, anti_cheat, question_order, show_answer, status, late_submit, late_time, auto_submit, student_count, question_count, create_by, create_time) VALUES (#{examName}, #{examType}, #{courseId}, #{sessionId}, #{totalScore}, #{passScore}, #{examDuration}, #{startTime}, #{endTime}, #{examMode}, #{antiCheat}, #{questionOrder}, #{showAnswer}, #{status}, #{lateSubmit}, #{lateTime}, #{autoSubmit}, #{studentCount}, #{questionCount}, #{createBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertExam(ClassExam exam);

    @Update("UPDATE class_exam SET exam_name=#{examName}, exam_type=#{examType}, course_id=#{courseId}, session_id=#{sessionId}, total_score=#{totalScore}, pass_score=#{passScore}, exam_duration=#{examDuration}, start_time=#{startTime}, end_time=#{endTime}, exam_mode=#{examMode}, anti_cheat=#{antiCheat}, question_order=#{questionOrder}, show_answer=#{showAnswer}, status=#{status}, late_submit=#{lateSubmit}, late_time=#{lateTime}, auto_submit=#{autoSubmit}, student_count=#{studentCount}, question_count=#{questionCount}, update_by=#{updateBy}, update_time=NOW() WHERE id=#{id}")
    int updateExam(ClassExam exam);

    @Delete("DELETE FROM class_exam WHERE id=#{id}")
    int deleteExamById(Long id);

    @Delete("<script>DELETE FROM class_exam WHERE id IN <foreach item='i' collection='array' open='(' separator=',' close=')'>#{i}</foreach></script>")
    int deleteExamByIds(Long[] ids);

    @Select("SELECT e.*, cc.course_name AS course_name, cs.class_name AS class_name FROM class_exam e " +
            "JOIN class_session_student ss ON e.session_id = ss.session_id " +
            "JOIN class_student s ON s.student_id = ss.student_id " +
            "LEFT JOIN class_course cc ON e.course_id = cc.course_id " +
            "LEFT JOIN class_session cs ON e.session_id = cs.session_id " +
            "WHERE s.student_no = #{studentNo} AND e.status IN (1,2,3) ORDER BY e.start_time DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "examName", column = "exam_name"),
            @Result(property = "examType", column = "exam_type"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "sessionId", column = "session_id"),
            @Result(property = "totalScore", column = "total_score"),
            @Result(property = "passScore", column = "pass_score"),
            @Result(property = "examDuration", column = "exam_duration"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "examMode", column = "exam_mode"),
            @Result(property = "antiCheat", column = "anti_cheat"),
            @Result(property = "questionOrder", column = "question_order"),
            @Result(property = "showAnswer", column = "show_answer"),
            @Result(property = "status", column = "status"),
            @Result(property = "lateSubmit", column = "late_submit"),
            @Result(property = "lateTime", column = "late_time"),
            @Result(property = "autoSubmit", column = "auto_submit"),
            @Result(property = "studentCount", column = "student_count"),
            @Result(property = "questionCount", column = "question_count"),
            @Result(property = "courseName", column = "course_name"),
            @Result(property = "className", column = "class_name")
    })
    List<ClassExam> selectAvailableByStudentNo(String studentNo);

    // New: count exams with same name within same session (optionally excluding an id)
    @Select("<script>SELECT COUNT(1) FROM class_exam WHERE session_id=#{sessionId} AND exam_name=#{examName} <if test='excludeId != null'>AND id != #{excludeId}</if></script>")
    int countBySessionAndName(@Param("sessionId") Long sessionId, @Param("examName") String examName, @Param("excludeId") Long excludeId);
}
