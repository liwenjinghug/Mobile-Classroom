package com.ruoyi.proj_fz.mapper;

import com.ruoyi.proj_fz.domain.HomeworkStatisticsDTO;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface HomeworkStatisticsMapper {

    @Select("SELECT " +
            "    ch.homework_id as homeworkId, " +
            "    ch.title as homeworkTitle, " +
            "    cc.course_name as courseName, " +
            "    cs.class_name as className, " +
            "    ch.total_score as totalScore, " +
            "    ch.deadline as deadline, " +
            "    ch.create_time as createTime, " +
            "    (SELECT COUNT(*) FROM class_session_student WHERE session_id = ch.session_id) as totalStudents, " +
            "    (SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2')) as submittedCount, " +
            "    ((SELECT COUNT(*) FROM class_session_student WHERE session_id = ch.session_id) - " +
            "     (SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2'))) as notSubmittedCount, " +
            "    (SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2') AND submit_time > ch.deadline) as overdueCount, " +
            "    (SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status = '2') as gradedCount, " +
            "    CASE " +
            "        WHEN (SELECT COUNT(*) FROM class_session_student WHERE session_id = ch.session_id) > 0 " +
            "        THEN ROUND((SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2')) * 100.0 / " +
            "                  (SELECT COUNT(*) FROM class_session_student WHERE session_id = ch.session_id), 2) " +
            "        ELSE 0 " +
            "    END as submissionRate, " +
            "    (SELECT ROUND(AVG(grade), 2) FROM class_student_homework WHERE homework_id = ch.homework_id AND grade IS NOT NULL AND status = '2') as averageScore, " +
            "    ch.create_by as createBy " +
            "FROM class_homework ch " +
            "LEFT JOIN class_course cc ON ch.course_id = cc.course_id " +
            "LEFT JOIN class_session cs ON ch.session_id = cs.session_id " +
            "ORDER BY ch.create_time DESC")
    @Results({
            @Result(property = "homeworkId", column = "homeworkId"),
            @Result(property = "homeworkTitle", column = "homeworkTitle"),
            @Result(property = "courseName", column = "courseName"),
            @Result(property = "className", column = "className"),
            @Result(property = "totalScore", column = "totalScore"),
            @Result(property = "deadline", column = "deadline"),
            @Result(property = "createTime", column = "createTime"),
            @Result(property = "totalStudents", column = "totalStudents"),
            @Result(property = "submittedCount", column = "submittedCount"),
            @Result(property = "notSubmittedCount", column = "notSubmittedCount"),
            @Result(property = "overdueCount", column = "overdueCount"),
            @Result(property = "gradedCount", column = "gradedCount"),
            @Result(property = "submissionRate", column = "submissionRate"),
            @Result(property = "averageScore", column = "averageScore"),
            @Result(property = "createBy", column = "createBy")
    })
    List<HomeworkStatisticsDTO> selectHomeworkStatisticsList();

    @Select("<script>" +
            "SELECT " +
            "    ch.homework_id as homeworkId, " +
            "    ch.title as homeworkTitle, " +
            "    cc.course_name as courseName, " +
            "    cs.class_name as className, " +
            "    ch.total_score as totalScore, " +
            "    ch.deadline as deadline, " +
            "    ch.create_time as createTime, " +
            "    (SELECT COUNT(*) FROM class_session_student WHERE session_id = ch.session_id) as totalStudents, " +
            "    (SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2')) as submittedCount, " +
            "    ((SELECT COUNT(*) FROM class_session_student WHERE session_id = ch.session_id) - " +
            "     (SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2'))) as notSubmittedCount, " +
            "    (SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2') AND submit_time > ch.deadline) as overdueCount, " +
            "    (SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status = '2') as gradedCount, " +
            "    CASE " +
            "        WHEN (SELECT COUNT(*) FROM class_session_student WHERE session_id = ch.session_id) > 0 " +
            "        THEN ROUND((SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2')) * 100.0 / " +
            "                  (SELECT COUNT(*) FROM class_session_student WHERE session_id = ch.session_id), 2) " +
            "        ELSE 0 " +
            "    END as submissionRate, " +
            "    (SELECT ROUND(AVG(grade), 2) FROM class_student_homework WHERE homework_id = ch.homework_id AND grade IS NOT NULL AND status = '2') as averageScore, " +
            "    ch.create_by as createBy " +
            "FROM class_homework ch " +
            "LEFT JOIN class_course cc ON ch.course_id = cc.course_id " +
            "LEFT JOIN class_session cs ON ch.session_id = cs.session_id " +
            "WHERE 1=1 " +
            "<if test='courseId != null'>" +
            "   AND ch.course_id = #{courseId} " +
            "</if>" +
            "<if test='sessionId != null'>" +
            "   AND ch.session_id = #{sessionId} " +
            "</if>" +
            "ORDER BY ch.create_time DESC" +
            "</script>")
    @Results({
            @Result(property = "homeworkId", column = "homeworkId"),
            @Result(property = "homeworkTitle", column = "homeworkTitle"),
            @Result(property = "courseName", column = "courseName"),
            @Result(property = "className", column = "className"),
            @Result(property = "totalScore", column = "totalScore"),
            @Result(property = "deadline", column = "deadline"),
            @Result(property = "createTime", column = "createTime"),
            @Result(property = "totalStudents", column = "totalStudents"),
            @Result(property = "submittedCount", column = "submittedCount"),
            @Result(property = "notSubmittedCount", column = "notSubmittedCount"),
            @Result(property = "overdueCount", column = "overdueCount"),
            @Result(property = "gradedCount", column = "gradedCount"),
            @Result(property = "submissionRate", column = "submissionRate"),
            @Result(property = "averageScore", column = "averageScore"),
            @Result(property = "createBy", column = "createBy")
    })
    List<HomeworkStatisticsDTO> selectHomeworkStatisticsListByFilter(Map<String, Object> params);

    @Select("SELECT " +
            "    ch.homework_id as homeworkId, " +
            "    ch.title as homeworkTitle, " +
            "    cc.course_name as courseName, " +
            "    cs.class_name as className, " +
            "    ch.total_score as totalScore, " +
            "    ch.deadline as deadline, " +
            "    ch.create_time as createTime, " +
            "    (SELECT COUNT(*) FROM class_session_student WHERE session_id = ch.session_id) as totalStudents, " +
            "    (SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2')) as submittedCount, " +
            "    ((SELECT COUNT(*) FROM class_session_student WHERE session_id = ch.session_id) - " +
            "     (SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2'))) as notSubmittedCount, " +
            "    (SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2') AND submit_time > ch.deadline) as overdueCount, " +
            "    (SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status = '2') as gradedCount, " +
            "    CASE " +
            "        WHEN (SELECT COUNT(*) FROM class_session_student WHERE session_id = ch.session_id) > 0 " +
            "        THEN ROUND((SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2')) * 100.0 / " +
            "                  (SELECT COUNT(*) FROM class_session_student WHERE session_id = ch.session_id), 2) " +
            "        ELSE 0 " +
            "    END as submissionRate, " +
            "    (SELECT ROUND(AVG(grade), 2) FROM class_student_homework WHERE homework_id = ch.homework_id AND grade IS NOT NULL AND status = '2') as averageScore, " +
            "    (SELECT MAX(grade) FROM class_student_homework WHERE homework_id = ch.homework_id AND grade IS NOT NULL AND status = '2') as maxScore, " +
            "    (SELECT MIN(grade) FROM class_student_homework WHERE homework_id = ch.homework_id AND grade IS NOT NULL AND status = '2') as minScore, " +
            "    ch.create_by as createBy " +
            "FROM class_homework ch " +
            "LEFT JOIN class_course cc ON ch.course_id = cc.course_id " +
            "LEFT JOIN class_session cs ON ch.session_id = cs.session_id " +
            "WHERE ch.homework_id = #{homeworkId}")
    @Results({
            @Result(property = "homeworkId", column = "homeworkId"),
            @Result(property = "homeworkTitle", column = "homeworkTitle"),
            @Result(property = "courseName", column = "courseName"),
            @Result(property = "className", column = "className"),
            @Result(property = "totalScore", column = "totalScore"),
            @Result(property = "deadline", column = "deadline"),
            @Result(property = "createTime", column = "createTime"),
            @Result(property = "totalStudents", column = "totalStudents"),
            @Result(property = "submittedCount", column = "submittedCount"),
            @Result(property = "notSubmittedCount", column = "notSubmittedCount"),
            @Result(property = "overdueCount", column = "overdueCount"),
            @Result(property = "gradedCount", column = "gradedCount"),
            @Result(property = "submissionRate", column = "submissionRate"),
            @Result(property = "averageScore", column = "averageScore"),
            @Result(property = "maxScore", column = "maxScore"),
            @Result(property = "minScore", column = "minScore"),
            @Result(property = "createBy", column = "createBy")
    })
    HomeworkStatisticsDTO selectHomeworkStatisticsById(@Param("homeworkId") Long homeworkId);

    @Select("SELECT " +
            "    CASE " +
            "        WHEN grade BETWEEN 0 AND 59 THEN '0-59' " +
            "        WHEN grade BETWEEN 60 AND 69 THEN '60-69' " +
            "        WHEN grade BETWEEN 70 AND 79 THEN '70-79' " +
            "        WHEN grade BETWEEN 80 AND 89 THEN '80-89' " +
            "        WHEN grade BETWEEN 90 AND 100 THEN '90-100' " +
            "        ELSE '未批改' " +
            "    END as scoreRange, " +
            "    COUNT(DISTINCT student_id) as count " +
            "FROM class_student_homework " +
            "WHERE homework_id = #{homeworkId} AND status = '2' " +
            "GROUP BY " +
            "    CASE " +
            "        WHEN grade BETWEEN 0 AND 59 THEN '0-59' " +
            "        WHEN grade BETWEEN 60 AND 69 THEN '60-69' " +
            "        WHEN grade BETWEEN 70 AND 79 THEN '70-79' " +
            "        WHEN grade BETWEEN 80 AND 89 THEN '80-89' " +
            "        WHEN grade BETWEEN 90 AND 100 THEN '90-100' " +
            "        ELSE '未批改' " +
            "    END")
    List<Map<String, Object>> selectScoreDistribution(@Param("homeworkId") Long homeworkId);

    @Select("SELECT " +
            "    DATE_FORMAT(submit_time, '%Y-%m-%d %H:00') as hour, " +
            "    COUNT(DISTINCT student_id) as count " +
            "FROM class_student_homework " +
            "WHERE homework_id = #{homeworkId} AND status IN ('1','2') " +
            "GROUP BY DATE_FORMAT(submit_time, '%Y-%m-%d %H:00') " +
            "ORDER BY hour")
    List<Map<String, Object>> selectSubmissionTrend(@Param("homeworkId") Long homeworkId);

    @Select("SELECT " +
            "    cc.course_name as courseName, " +
            "    COUNT(DISTINCT ch.homework_id) as homeworkCount, " +
            "    SUM((SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2'))) as totalSubmissions, " +
            "    CASE " +
            "        WHEN SUM((SELECT COUNT(*) FROM class_session_student WHERE session_id = ch.session_id)) > 0 " +
            "        THEN ROUND(AVG((SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2')) * 100.0 / " +
            "                      (SELECT COUNT(*) FROM class_session_student WHERE session_id = ch.session_id)), 2) " +
            "        ELSE 0 " +
            "    END as avgSubmissionRate " +
            "FROM class_homework ch " +
            "LEFT JOIN class_course cc ON ch.course_id = cc.course_id " +
            "GROUP BY cc.course_id, cc.course_name")
    List<Map<String, Object>> selectTeacherHomeworkOverview();

    @Select("SELECT " +
            "    cs.session_id as sessionId, " +
            "    cs.class_name as className, " +
            "    cc.course_name as courseName, " +
            "    COUNT(DISTINCT ch.homework_id) as homeworkCount, " +
            "    SUM((SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2'))) as totalSubmissions, " +
            "    CASE " +
            "        WHEN SUM((SELECT COUNT(*) FROM class_session_student WHERE session_id = cs.session_id)) > 0 " +
            "        THEN ROUND(AVG((SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2')) * 100.0 / " +
            "                      (SELECT COUNT(*) FROM class_session_student WHERE session_id = cs.session_id)), 2) " +
            "        ELSE 0 " +
            "    END as avgSubmissionRate " +
            "FROM class_session cs " +
            "LEFT JOIN class_homework ch ON cs.session_id = ch.session_id " +
            "LEFT JOIN class_course cc ON ch.course_id = cc.course_id " +
            "GROUP BY cs.session_id, cs.class_name, cc.course_name " +
            "ORDER BY cs.session_id DESC")
    List<Map<String, Object>> selectSessionHomeworkOverview();

    @Select("SELECT " +
            "    csh.id as id, " +
            "    csh.student_id as studentId, " +
            "    csh.student_name as studentName, " +
            "    cs.student_no as studentNo, " +
            "    csh.submit_time as submitTime, " +
            "    csh.grade as grade, " +
            "    csh.status as status, " +
            "    csh.grade_comment as gradeComment, " +
            "    csh.submission_files as submissionFiles " +
            "FROM class_student_homework csh " +
            "LEFT JOIN class_student cs ON csh.student_id = cs.student_id " +
            "WHERE csh.homework_id = #{homeworkId} " +
            "ORDER BY csh.submit_time DESC")
    List<Map<String, Object>> selectStudentSubmissionDetails(@Param("homeworkId") Long homeworkId);

    @Select("SELECT " +
            "    COUNT(DISTINCT ch.homework_id) as totalHomeworkCount, " +
            "    SUM((SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2'))) as totalSubmissionCount, " +
            "    COUNT(DISTINCT ch.course_id) as totalCourseCount, " +
            "    (SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE status = '2') as gradedCount " +
            "FROM class_homework ch")
    Map<String, Object> selectDashboardOverview();

    @Select("SELECT homework_id, title, session_id, create_by FROM class_homework ORDER BY homework_id")
    List<Map<String, Object>> selectAllHomeworkBasicInfo();

    @Select("SELECT homework_id, COUNT(*) as total_submissions, " +
            "COUNT(DISTINCT student_id) as unique_students " +
            "FROM class_student_homework " +
            "WHERE status IN ('1','2') " +
            "GROUP BY homework_id")
    List<Map<String, Object>> selectHomeworkSubmissionStats();

    @Select("SELECT DISTINCT course_id as courseId, course_name as courseName FROM class_course WHERE status = '0'")
    List<Map<String, Object>> selectCourseList();

    @Select("SELECT DISTINCT session_id as sessionId, class_name as className FROM class_session")
    List<Map<String, Object>> selectSessionList();

    @Select("SELECT COUNT(*) FROM class_homework WHERE DATE(deadline) = CURDATE() AND status = '0'")
    Integer selectTodayDeadlineCount();

    @Select("<script>" +
            "SELECT " +
            "    ch.homework_id as homeworkId, " +
            "    ch.title as homeworkTitle, " +
            "    cc.course_name as courseName, " +
            "    cs.class_name as className, " +
            "    ch.total_score as totalScore, " +
            "    ch.deadline as deadline, " +
            "    ch.create_time as createTime, " +
            "    (SELECT COUNT(*) FROM class_session_student WHERE session_id = ch.session_id) as totalStudents, " +
            "    (SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2')) as submittedCount, " +
            "    ((SELECT COUNT(*) FROM class_session_student WHERE session_id = ch.session_id) - " +
            "     (SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2'))) as notSubmittedCount, " +
            "    (SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2') AND submit_time > ch.deadline) as overdueCount, " +
            "    (SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status = '2') as gradedCount, " +
            "    CASE " +
            "        WHEN (SELECT COUNT(*) FROM class_session_student WHERE session_id = ch.session_id) > 0 " +
            "        THEN ROUND((SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2')) * 100.0 / " +
            "                  (SELECT COUNT(*) FROM class_session_student WHERE session_id = ch.session_id), 2) " +
            "        ELSE 0 " +
            "    END as submissionRate, " +
            "    (SELECT ROUND(AVG(grade), 2) FROM class_student_homework WHERE homework_id = ch.homework_id AND grade IS NOT NULL AND status = '2') as averageScore, " +
            "    ch.create_by as createBy " +
            "FROM class_homework ch " +
            "LEFT JOIN class_course cc ON ch.course_id = cc.course_id " +
            "LEFT JOIN class_session cs ON ch.session_id = cs.session_id " +
            "WHERE 1=1 " +
            "<if test='homeworkTitle != null and homeworkTitle != \"\"'>" +
            "   AND ch.title LIKE CONCAT('%', #{homeworkTitle}, '%') " +
            "</if>" +
            "<if test='courseId != null'>" +
            "   AND ch.course_id = #{courseId} " +
            "</if>" +
            "<if test='sessionId != null'>" +
            "   AND ch.session_id = #{sessionId} " +
            "</if>" +
            "<if test='createTimeStart != null and createTimeStart != \"\"'>" +
            "   AND DATE(ch.create_time) &gt;= #{createTimeStart} " +
            "</if>" +
            "<if test='createTimeEnd != null and createTimeEnd != \"\"'>" +
            "   AND DATE(ch.create_time) &lt;= #{createTimeEnd} " +
            "</if>" +
            "<if test='deadlineStart != null and deadlineStart != \"\"'>" +
            "   AND DATE(ch.deadline) &gt;= #{deadlineStart} " +
            "</if>" +
            "<if test='deadlineEnd != null and deadlineEnd != \"\"'>" +
            "   AND DATE(ch.deadline) &lt;= #{deadlineEnd} " +
            "</if>" +
            "<if test='expireStatus != null and expireStatus != \"\"'>" +
            "   <choose>" +
            "       <when test='expireStatus == \"expired\"'>" +
            "           AND ch.deadline &lt; NOW() " +
            "       </when>" +
            "       <when test='expireStatus == \"notExpired\"'>" +
            "           AND ch.deadline &gt;= NOW() " +
            "       </when>" +
            "   </choose>" +
            "</if>" +
            "<if test='gradeStatus != null and gradeStatus != \"\"'>" +
            "   <choose>" +
            "       <when test='gradeStatus == \"graded\"'>" +
            "           AND (SELECT COUNT(*) FROM class_student_homework WHERE homework_id = ch.homework_id AND status = '2') > 0 " +
            "       </when>" +
            "       <when test='gradeStatus == \"notGraded\"'>" +
            "           AND (SELECT COUNT(*) FROM class_student_homework WHERE homework_id = ch.homework_id AND status = '2') = 0 " +
            "       </when>" +
            "   </choose>" +
            "</if>" +
            "<if test='completionStatus != null and completionStatus != \"\"'>" +
            "   <choose>" +
            "       <when test='completionStatus == \"completed\"'>" +
            "           AND (SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2')) > 0 " +
            "       </when>" +
            "       <when test='completionStatus == \"onTime\"'>" +
            "           AND (SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2') AND submit_time &lt;= ch.deadline) > 0 " +
            "       </when>" +
            "       <when test='completionStatus == \"overdue\"'>" +
            "           AND (SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2') AND submit_time > ch.deadline) > 0 " +
            "       </when>" +
            "       <when test='completionStatus == \"notCompleted\"'>" +
            "           AND (SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE homework_id = ch.homework_id AND status IN ('1','2')) = 0 " +
            "       </when>" +
            "   </choose>" +
            "</if>" +
            "ORDER BY ch.create_time DESC" +
            "</script>")
    @Results({
            @Result(property = "homeworkId", column = "homeworkId"),
            @Result(property = "homeworkTitle", column = "homeworkTitle"),
            @Result(property = "courseName", column = "courseName"),
            @Result(property = "className", column = "className"),
            @Result(property = "totalScore", column = "totalScore"),
            @Result(property = "deadline", column = "deadline"),
            @Result(property = "createTime", column = "createTime"),
            @Result(property = "totalStudents", column = "totalStudents"),
            @Result(property = "submittedCount", column = "submittedCount"),
            @Result(property = "notSubmittedCount", column = "notSubmittedCount"),
            @Result(property = "overdueCount", column = "overdueCount"),
            @Result(property = "gradedCount", column = "gradedCount"),
            @Result(property = "submissionRate", column = "submissionRate"),
            @Result(property = "averageScore", column = "averageScore"),
            @Result(property = "createBy", column = "createBy")
    })
    List<HomeworkStatisticsDTO> selectHomeworkStatisticsListByAdvancedFilter(Map<String, Object> params);
}