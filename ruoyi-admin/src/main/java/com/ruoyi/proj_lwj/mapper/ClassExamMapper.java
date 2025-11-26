package com.ruoyi.proj_lwj.mapper;

import com.ruoyi.proj_lwj.domain.ClassExam;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface ClassExamMapper {

    // ==================== 原有基础 CRUD 方法 (保持不变) ====================

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
            "LEFT JOIN class_exam_participant p ON p.exam_id = e.id AND p.student_id = s.student_id " +
            "WHERE s.student_no = #{studentNo} " +
            "AND e.status IN (1,2) " +
            "AND (e.end_time IS NULL OR e.end_time > NOW()) " +
            "AND (p.id IS NULL OR p.participant_status <> 2) " +
            "ORDER BY e.start_time DESC")
    List<ClassExam> selectAvailableByStudentNo(String studentNo);

    @Select("<script>SELECT COUNT(1) FROM class_exam WHERE session_id=#{sessionId} AND exam_name=#{examName} <if test='excludeId != null'>AND id != #{excludeId}</if></script>")
    int countBySessionAndName(@Param("sessionId") Long sessionId, @Param("examName") String examName, @Param("excludeId") Long excludeId);


    // ==================== 【消息中心专用查询 (已修复)】 ====================

    /**
     * 查询用户的考试消息 (包含 message_read)
     * 修复：管理员直接返回已读 '1'，避免查不到状态显示未读
     */
    @Select("<script>" +
            "SELECT DISTINCT e.id, e.exam_name, e.start_time, e.end_time, e.exam_duration, e.create_time, " +
            // 【核心修复】管理员默认返回 '1' (已读)，学生查表返回
            "<choose>" +
            "  <when test='isAdmin'> '1' as messageRead </when>" +
            "  <otherwise> IFNULL(p.message_read, '0') as messageRead </otherwise>" +
            "</choose>" +
            "FROM class_exam e " +
            "<if test='!isAdmin'>" +
            "  INNER JOIN class_session_student css ON e.session_id = css.session_id " +
            "  INNER JOIN class_student s ON css.student_id = s.student_id " +
            "  LEFT JOIN class_exam_participant p ON e.id = p.exam_id AND p.student_id = s.student_id " +
            "</if>" +
            "WHERE e.status IN (1, 2) " +
            "AND e.end_time > NOW() " +
            "<if test='!isAdmin'>" +
            "  AND s.user_id = #{userId} " +
            "</if>" +
            "ORDER BY e.create_time DESC" +
            "</script>")
    List<ClassExam> selectExamMessages(@Param("userId") Long userId, @Param("isAdmin") boolean isAdmin);

    /**
     * 统计未读数量
     * 修复：管理员不统计考试未读数 (返回0)
     */
    @Select("<script>" +
            "SELECT COUNT(DISTINCT e.id) " +
            "FROM class_exam e " +
            // 仅学生执行关联查询
            "<if test='!isAdmin'>" +
            "  INNER JOIN class_session_student css ON e.session_id = css.session_id " +
            "  INNER JOIN class_student s ON css.student_id = s.student_id " +
            "  LEFT JOIN class_exam_participant p ON e.id = p.exam_id AND p.student_id = s.student_id " +
            "</if>" +
            "WHERE e.status IN (1, 2) " +
            "AND e.end_time > NOW() " +
            // 仅学生统计未读，管理员直接跳过（返回0条记录）
            "<if test='!isAdmin'>" +
            "  AND s.user_id = #{userId} " +
            "  AND (p.message_read IS NULL OR p.message_read = '0') " +
            "</if>" +
            "<if test='isAdmin'>" +
            "  AND 1=0 " + // 管理员强制返回0条
            "</if>" +
            "</script>")
    int selectUnreadExamCount(@Param("userId") Long userId, @Param("isAdmin") boolean isAdmin);

    // ========== 标记已读辅助方法 ==========

    @Select("SELECT student_id FROM class_student WHERE user_id = #{userId} LIMIT 1")
    Long selectStudentIdByUserId(Long userId);

    @Select("SELECT * FROM class_student WHERE student_id = #{studentId}")
    Map<String, Object> selectStudentById(Long studentId);

    @Select("SELECT id FROM class_exam_participant WHERE exam_id = #{examId} AND student_id = #{studentId}")
    Long selectParticipantId(@Param("examId") Long examId, @Param("studentId") Long studentId);

    @Update("UPDATE class_exam_participant SET message_read = '1' WHERE id = #{id}")
    int updateExamReadStatus(Long id);

    @Insert("INSERT INTO class_exam_participant (exam_id, student_id, student_no, student_name, participant_status, message_read, create_time) " +
            "VALUES (#{examId}, #{studentId}, #{studentNo}, #{studentName}, 0, '1', NOW())")
    int insertReadRecord(@Param("examId") Long examId,
                         @Param("studentId") Long studentId,
                         @Param("studentNo") String studentNo,
                         @Param("studentName") String studentName);
}