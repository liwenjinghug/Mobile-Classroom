package com.ruoyi.proj_fz.mapper;

import org.apache.ibatis.annotations.*;
import com.ruoyi.proj_fz.domain.DashboardDTO;

import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {

    // 课程相关统计
    @Select("SELECT COUNT(*) FROM class_course WHERE status = '0'")
    int countTotalCourses();

    @Select("SELECT COUNT(*) FROM class_session WHERE status = 1")
    int countActiveSessions();

    @Select("SELECT COUNT(*) FROM class_homework WHERE status = '0' AND DATE(deadline) = CURDATE()")
    int countTodayDeadline();

    @Select("SELECT COUNT(*) FROM class_exam WHERE status = 1")
    int countOngoingExams();

    @Select("SELECT COUNT(*) FROM class_todo WHERE status = '0'")
    int countPendingTodos();

    // 作业相关统计
    @Select("SELECT COUNT(*) FROM class_homework WHERE status = '0'")
    int countTotalAssignments();

    @Select("SELECT COUNT(DISTINCT student_id) FROM class_student_homework WHERE status IN ('1', '2', '3')")
    int countSubmittedAssignments();

    @Select("SELECT COUNT(*) FROM class_student_homework WHERE is_graded = 1")
    int countGradedAssignments();

    @Select("SELECT ROUND(AVG(grade), 2) FROM class_student_homework WHERE grade IS NOT NULL AND grade > 0")
    Double getAverageScore();

    @Select("SELECT MAX(grade) FROM class_student_homework WHERE grade IS NOT NULL AND grade > 0")
    Double getMaxScore();

    @Select("SELECT COUNT(*) FROM class_student_homework WHERE grade IS NOT NULL AND grade >= 60")
    int countPassAssignments();

    // 提交趋势数据
    @Select("SELECT DATE(create_time) as date, COUNT(*) as submissions " +
            "FROM class_student_homework " +
            "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "AND status IN ('1', '2', '3') " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date")
    @Results({
            @Result(property = "date", column = "date"),
            @Result(property = "submissions", column = "submissions")
    })
    List<DashboardDTO.TrendData> getSubmissionTrend(@Param("days") int days);

    // 成绩分布数据
    @Select("SELECT " +
            "CASE " +
            "WHEN grade < 60 THEN '0-59' " +
            "WHEN grade < 70 THEN '60-69' " +
            "WHEN grade < 80 THEN '70-79' " +
            "WHEN grade < 90 THEN '80-89' " +
            "ELSE '90-100' " +
            "END as score_range, " +
            "COUNT(*) as count " +
            "FROM class_student_homework " +
            "WHERE grade IS NOT NULL AND grade > 0 " +
            "GROUP BY " +
            "CASE " +
            "WHEN grade < 60 THEN '0-59' " +
            "WHEN grade < 70 THEN '60-69' " +
            "WHEN grade < 80 THEN '70-79' " +
            "WHEN grade < 90 THEN '80-89' " +
            "ELSE '90-100' " +
            "END " +
            "ORDER BY score_range")
    @Results({
            @Result(property = "range", column = "score_range"),
            @Result(property = "count", column = "count")
    })
    List<DashboardDTO.ScoreDistribution> getScoreDistribution();

    // 课程平均分对比
    @Select("SELECT c.course_name as courseName, ROUND(AVG(h.grade), 2) as averageScore " +
            "FROM class_course c " +
            "LEFT JOIN class_homework hw ON c.course_id = hw.course_id " +
            "LEFT JOIN class_student_homework h ON hw.homework_id = h.homework_id " +
            "WHERE h.grade IS NOT NULL AND h.grade > 0 " +
            "GROUP BY c.course_id, c.course_name " +
            "ORDER BY averageScore DESC")
    @Results({
            @Result(property = "courseName", column = "courseName"),
            @Result(property = "averageScore", column = "averageScore")
    })
    List<DashboardDTO.CourseScore> getCourseScores();

    // 签到率数据
    @Select("SELECT s.class_name as className, " +
            "ROUND(SUM(CASE WHEN a.attendance_status IN (1,2,4) THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as attendanceRate " +
            "FROM class_session s " +
            "LEFT JOIN class_attendance a ON s.session_id = a.session_id " +
            "GROUP BY s.session_id, s.class_name " +
            "ORDER BY attendanceRate DESC")
    @Results({
            @Result(property = "className", column = "className"),
            @Result(property = "attendanceRate", column = "attendanceRate")
    })
    List<DashboardDTO.AttendanceRate> getAttendanceRates();

    // 参与热力图数据
    @Select("SELECT cs.student_name as studentName, " +
            "COUNT(DISTINCT h.homework_id) as homeworkCount, " +
            "COUNT(DISTINCT a.attendance_id) as attendanceCount, " +
            "COUNT(DISTINCT f.post_id) as forumCount " +
            "FROM class_student cs " +
            "LEFT JOIN class_student_homework h ON cs.student_id = h.student_id " +
            "LEFT JOIN class_attendance a ON cs.student_id = a.student_id " +
            "LEFT JOIN class_forum_post f ON cs.student_id = f.user_id " +
            "GROUP BY cs.student_id, cs.student_name " +
            "ORDER BY (homeworkCount + attendanceCount + forumCount) DESC " +
            "LIMIT 10")
    @Results({
            @Result(property = "studentName", column = "studentName"),
            @Result(property = "homeworkCount", column = "homeworkCount"),
            @Result(property = "attendanceCount", column = "attendanceCount"),
            @Result(property = "forumCount", column = "forumCount")
    })
    List<DashboardDTO.ParticipationHeat> getParticipationHeat();

    // 待办事项
    @Select("SELECT todo_id as id, title, content, todo_type as type, priority, end_time as endTime, status " +
            "FROM class_todo " +
            "WHERE status IN ('0', '2') " +
            "ORDER BY end_time ASC " +
            "LIMIT 10")
    List<Map<String, Object>> getTodos();

    // 消息列表
    @Select("SELECT m.message_id as id, m.content, m.create_time as createTime, 'system' as type, '未读' as status " +
            "FROM class_group_message m " +
            "WHERE m.message_type = '9' " +
            "ORDER BY m.create_time DESC " +
            "LIMIT 10")
    List<Map<String, Object>> getMessages();

    // 新增：考试统计
    @Select("SELECT COUNT(*) as totalExams, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as publishedExams, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as ongoingExams " +
            "FROM class_exam")
    Map<String, Object> getExamStatistics();

    // 新增：学生活跃度统计
    @Select("SELECT COUNT(DISTINCT student_id) as activeStudents, " +
            "ROUND(AVG(homeworkCount), 2) as avgHomeworkCount " +
            "FROM ( " +
            "    SELECT cs.student_id, COUNT(DISTINCT h.homework_id) as homeworkCount " +
            "    FROM class_student cs " +
            "    LEFT JOIN class_student_homework h ON cs.student_id = h.student_id " +
            "    GROUP BY cs.student_id " +
            ") t")
    Map<String, Object> getStudentActivity();

    // 作业明细（带筛选）- 使用Provider方式
    @SelectProvider(type = DashboardSqlProvider.class, method = "getHomeworkDetailsSql")
    List<Map<String, Object>> getHomeworkDetails(Map<String, Object> params);

    // 公告列表（带筛选）- 使用Provider方式
    @SelectProvider(type = DashboardSqlProvider.class, method = "getNoticesSql")
    List<Map<String, Object>> getNotices(Map<String, Object> params);

    // 操作日志（带筛选）- 使用Provider方式
    @SelectProvider(type = DashboardSqlProvider.class, method = "getOperationLogsSql")
    List<Map<String, Object>> getOperationLogs(Map<String, Object> params);
}