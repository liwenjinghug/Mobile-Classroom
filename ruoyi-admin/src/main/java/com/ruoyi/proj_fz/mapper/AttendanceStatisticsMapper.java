package com.ruoyi.proj_fz.mapper;

import com.ruoyi.proj_fz.domain.AttendanceStatisticsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

@Mapper
public interface AttendanceStatisticsMapper {

    // 按课堂维度统计
    @Select("<script>" +
            "SELECT " +
            "    cs.session_id as sessionId, " +
            "    cs.class_name as className, " +
            "    COUNT(DISTINCT css.student_id) as totalStudents, " +
            "    COUNT(CASE WHEN ca.attendance_status = 1 THEN 1 END) as signedCount, " +
            "    COUNT(CASE WHEN ca.attendance_status = 0 THEN 1 END) as absentCount, " +
            "    COUNT(CASE WHEN ca.attendance_status = 2 THEN 1 END) as lateCount, " +
            "    COUNT(CASE WHEN ca.attendance_status = 3 THEN 1 END) as leaveCount, " +
            "    ROUND(COUNT(CASE WHEN ca.attendance_status = 1 THEN 1 END) * 100.0 / COUNT(DISTINCT css.student_id), 2) as attendanceRate " +
            "FROM class_session cs " +
            "LEFT JOIN class_session_student css ON cs.session_id = css.session_id " +
            "LEFT JOIN class_attendance ca ON cs.session_id = ca.session_id AND css.student_id = ca.student_id " +
            "<where>" +
            "    <if test='sessionId != null'>" +
            "        AND cs.session_id = #{sessionId} " +
            "    </if>" +
            "    <if test='startDate != null'>" +
            "        AND DATE(ca.attendance_time) &gt;= DATE(#{startDate}) " +
            "    </if>" +
            "    <if test='endDate != null'>" +
            "        AND DATE(ca.attendance_time) &lt;= DATE(#{endDate}) " +
            "    </if>" +
            "</where>" +
            "GROUP BY cs.session_id, cs.class_name" +
            "</script>")
    List<AttendanceStatisticsDTO> selectSessionStatistics(@Param("sessionId") Long sessionId,
                                                          @Param("startDate") Date startDate,
                                                          @Param("endDate") Date endDate);

    // 按时间维度统计（每日/每周）
    @Select("<script>" +
            "SELECT " +
            "    <choose>" +
            "        <when test='groupBy == \"day\"'>" +
            "            DATE(ca.attendance_time) as statDate, " +
            "        </when>" +
            "        <when test='groupBy == \"week\"'>" +
            "            CONCAT(YEAR(ca.attendance_time), '-W', WEEK(ca.attendance_time)) as statWeek, " +
            "        </when>" +
            "    </choose>" +
            "    COUNT(CASE WHEN ca.attendance_status = 1 THEN 1 END) as dailySigned, " +
            "    COUNT(CASE WHEN ca.attendance_status = 0 THEN 1 END) as dailyAbsent, " +
            "    COUNT(CASE WHEN ca.attendance_status = 2 THEN 1 END) as dailyLate, " +
            "    COUNT(DISTINCT css.student_id) as totalStudents, " +
            "    ROUND(COUNT(CASE WHEN ca.attendance_status = 1 THEN 1 END) * 100.0 / COUNT(DISTINCT css.student_id), 2) as attendanceRate " +
            "FROM class_session cs " +
            "LEFT JOIN class_session_student css ON cs.session_id = css.session_id " +
            "LEFT JOIN class_attendance ca ON cs.session_id = ca.session_id AND css.student_id = ca.student_id " +
            "<where>" +
            "    <if test='sessionId != null'>" +
            "        AND cs.session_id = #{sessionId} " +
            "    </if>" +
            "    <if test='startDate != null'>" +
            "        AND DATE(ca.attendance_time) &gt;= DATE(#{startDate}) " +
            "    </if>" +
            "    <if test='endDate != null'>" +
            "        AND DATE(ca.attendance_time) &lt;= DATE(#{endDate}) " +
            "    </if>" +
            "</where>" +
            "GROUP BY " +
            "    <choose>" +
            "        <when test='groupBy == \"day\"'>" +
            "            DATE(ca.attendance_time) " +
            "        </when>" +
            "        <when test='groupBy == \"week\"'>" +
            "            CONCAT(YEAR(ca.attendance_time), '-W', WEEK(ca.attendance_time)) " +
            "        </when>" +
            "    </choose>" +
            "ORDER BY statDate" +
            "</script>")
    List<AttendanceStatisticsDTO> selectTimeStatistics(@Param("sessionId") Long sessionId,
                                                       @Param("startDate") Date startDate,
                                                       @Param("endDate") Date endDate,
                                                       @Param("groupBy") String groupBy);

    // 签到明细查询
    @Select("<script>" +
            "SELECT " +
            "    cs.session_id as sessionId, " +
            "    cs.class_name as className, " +
            "    s.student_name as studentName, " +
            "    s.student_no as studentNo, " +
            "    ca.attendance_time as attendanceTime, " +
            "    ca.attendance_status as attendanceStatus, " +
            "    CASE ca.attendance_status " +
            "        WHEN 0 THEN '缺勤' " +
            "        WHEN 1 THEN '已签到' " +
            "        WHEN 2 THEN '迟到' " +
            "        WHEN 3 THEN '请假' " +
            "        WHEN 4 THEN '早退' " +
            "        ELSE '未知' " +
            "    END as statusText, " +
            "    CASE " +
            "        WHEN ca.device_type IS NOT NULL THEN ca.device_type " +
            "        ELSE '未知' " +
            "    END as attendanceMethod " +
            "FROM class_attendance ca " +
            "JOIN class_student s ON ca.student_id = s.student_id " +
            "JOIN class_session cs ON ca.session_id = cs.session_id " +
            "<where>" +
            "    <if test='sessionId != null'>" +
            "        AND ca.session_id = #{sessionId} " +
            "    </if>" +
            "    <if test='startDate != null'>" +
            "        AND DATE(ca.attendance_time) &gt;= DATE(#{startDate}) " +
            "    </if>" +
            "    <if test='endDate != null'>" +
            "        AND DATE(ca.attendance_time) &lt;= DATE(#{endDate}) " +
            "    </if>" +
            "    <if test='attendanceStatus != null'>" +
            "        AND ca.attendance_status = #{attendanceStatus} " +
            "    </if>" +
            "</where>" +
            "ORDER BY ca.attendance_time DESC" +
            "</script>")
    List<AttendanceStatisticsDTO> selectAttendanceDetails(@Param("sessionId") Long sessionId,
                                                          @Param("startDate") Date startDate,
                                                          @Param("endDate") Date endDate,
                                                          @Param("attendanceStatus") Integer attendanceStatus);

    // 周报表统计
    @Select("SELECT " +
            "    cs.session_id as sessionId, " +
            "    cs.class_name as className, " +
            "    COUNT(DISTINCT css.student_id) as totalStudents, " +
            "    COUNT(CASE WHEN ca.attendance_status = 1 THEN 1 END) as signedCount, " +
            "    ROUND(COUNT(CASE WHEN ca.attendance_status = 1 THEN 1 END) * 100.0 / COUNT(DISTINCT css.student_id), 2) as avgAttendanceRate, " +
            "    CONCAT(DATE_FORMAT(#{startDate}, '%Y-%m-%d'), ' 至 ', DATE_FORMAT(#{endDate}, '%Y-%m-%d')) as weekRange " +
            "FROM class_session cs " +
            "LEFT JOIN class_session_student css ON cs.session_id = css.session_id " +
            "LEFT JOIN class_attendance ca ON cs.session_id = ca.session_id AND css.student_id = ca.student_id " +
            "WHERE ca.attendance_time BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY cs.session_id, cs.class_name " +
            "ORDER BY avgAttendanceRate DESC")
    List<AttendanceStatisticsDTO> selectWeeklyReport(@Param("startDate") Date startDate,
                                                     @Param("endDate") Date endDate);

    // 缺勤次数最多的学生排名
    @Select("SELECT " +
            "    s.student_name as studentName, " +
            "    s.student_no as studentNo, " +
            "    COUNT(CASE WHEN ca.attendance_status = 0 THEN 1 END) as absenceCount, " +
            "    ROW_NUMBER() OVER (ORDER BY COUNT(CASE WHEN ca.attendance_status = 0 THEN 1 END) DESC) as absenceRank " +
            "FROM class_attendance ca " +
            "JOIN class_student s ON ca.student_id = s.student_id " +
            "WHERE ca.attendance_time BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY s.student_id, s.student_name, s.student_no " +
            "ORDER BY absenceCount DESC " +
            "LIMIT #{limit}")
    List<AttendanceStatisticsDTO> selectTopAbsenceStudents(@Param("startDate") Date startDate,
                                                           @Param("endDate") Date endDate,
                                                           @Param("limit") Integer limit);

    // 全校平均签到率
    @Select("SELECT " +
            "    ROUND(COUNT(CASE WHEN ca.attendance_status = 1 THEN 1 END) * 100.0 / COUNT(DISTINCT CONCAT(ca.session_id, '-', ca.student_id)), 2) as schoolAvgRate " +
            "FROM class_attendance ca " +
            "WHERE ca.attendance_time BETWEEN #{startDate} AND #{endDate}")
    Double selectSchoolAverageRate(@Param("startDate") Date startDate,
                                   @Param("endDate") Date endDate);

    // 本周平均签到率
    @Select("SELECT " +
            "    ROUND(COUNT(CASE WHEN ca.attendance_status = 1 THEN 1 END) * 100.0 / COUNT(DISTINCT CONCAT(ca.session_id, '-', ca.student_id)), 2) as weekAvgRate " +
            "FROM class_attendance ca " +
            "WHERE YEARWEEK(ca.attendance_time, 1) = YEARWEEK(CURDATE(), 1) " +
            "AND ca.attendance_time BETWEEN #{startDate} AND #{endDate}")
    Double selectWeekAverageRate(@Param("startDate") Date startDate,
                                 @Param("endDate") Date endDate);

    // 今日平均签到率
    @Select("SELECT " +
            "    ROUND(COUNT(CASE WHEN ca.attendance_status = 1 THEN 1 END) * 100.0 / COUNT(DISTINCT CONCAT(ca.session_id, '-', ca.student_id)), 2) as todayAvgRate " +
            "FROM class_attendance ca " +
            "WHERE DATE(ca.attendance_time) = CURDATE() " +
            "AND ca.attendance_time BETWEEN #{startDate} AND #{endDate}")
    Double selectTodayAverageRate(@Param("startDate") Date startDate,
                                  @Param("endDate") Date endDate);

    // 缺勤次数Top5课堂
    @Select("SELECT " +
            "    cs.session_id as sessionId, " +
            "    cs.class_name as className, " +
            "    COUNT(CASE WHEN ca.attendance_status = 0 THEN 1 END) as absenceCount, " +
            "    ROUND(COUNT(CASE WHEN ca.attendance_status = 1 THEN 1 END) * 100.0 / COUNT(DISTINCT css.student_id), 2) as attendanceRate " +
            "FROM class_attendance ca " +
            "JOIN class_session cs ON ca.session_id = cs.session_id " +
            "LEFT JOIN class_session_student css ON cs.session_id = css.session_id " +
            "WHERE ca.attendance_time BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY cs.session_id, cs.class_name " +
            "ORDER BY absenceCount DESC " +
            "LIMIT #{limit}")
    List<AttendanceStatisticsDTO> selectTopAbsenceSessions(@Param("startDate") Date startDate,
                                                           @Param("endDate") Date endDate,
                                                           @Param("limit") Integer limit);

    // 总课堂数
    @Select("SELECT COUNT(*) as totalSessions FROM class_session WHERE status = 1")
    Integer selectTotalSessions();

    // 活跃课堂数（今天有签到记录的课堂）
    @Select("SELECT COUNT(DISTINCT session_id) as activeSessions FROM class_attendance WHERE DATE(attendance_time) = CURDATE()")
    Integer selectActiveSessions();

    // 总学生数
    @Select("SELECT COUNT(*) as totalStudents FROM class_student WHERE status = 0")
    Integer selectTotalStudents();

    // 缺勤课堂数（签到率低于60%的课堂）
    @Select("SELECT COUNT(*) as absenceSessions FROM (" +
            "SELECT cs.session_id, " +
            "       ROUND(COUNT(CASE WHEN ca.attendance_status = 1 THEN 1 END) * 100.0 / COUNT(DISTINCT css.student_id), 2) as rate " +
            "FROM class_session cs " +
            "LEFT JOIN class_session_student css ON cs.session_id = css.session_id " +
            "LEFT JOIN class_attendance ca ON cs.session_id = ca.session_id AND css.student_id = ca.student_id " +
            "WHERE ca.attendance_time BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY cs.session_id " +
            "HAVING rate &lt; 60" +
            ") as low_attendance_sessions")
    Integer selectAbsenceSessions(@Param("startDate") Date startDate,
                                  @Param("endDate") Date endDate);

    // 驾驶舱综合指标
    @Select("<script>" +
            "SELECT " +
            "    (SELECT COUNT(*) FROM class_session WHERE status = 1) as totalSessions, " +
            "    (SELECT COUNT(DISTINCT session_id) FROM class_attendance WHERE DATE(attendance_time) = CURDATE()) as activeSessions, " +
            "    (SELECT COUNT(*) FROM class_student WHERE status = 0) as totalStudents, " +
            "    (SELECT ROUND(COUNT(CASE WHEN attendance_status = 1 THEN 1 END) * 100.0 / COUNT(DISTINCT CONCAT(session_id, '-', student_id)), 2) " +
            "     FROM class_attendance WHERE DATE(attendance_time) = CURDATE()) as todayAvgRate, " +
            "    (SELECT ROUND(COUNT(CASE WHEN attendance_status = 1 THEN 1 END) * 100.0 / COUNT(DISTINCT CONCAT(session_id, '-', student_id)), 2) " +
            "     FROM class_attendance WHERE YEARWEEK(attendance_time, 1) = YEARWEEK(CURDATE(), 1)) as weekAvgRate, " +
            "    (SELECT COUNT(*) FROM (" +
            "        SELECT cs.session_id, " +
            "               ROUND(COUNT(CASE WHEN ca.attendance_status = 1 THEN 1 END) * 100.0 / COUNT(DISTINCT css.student_id), 2) as rate " +
            "        FROM class_session cs " +
            "        LEFT JOIN class_session_student css ON cs.session_id = css.session_id " +
            "        LEFT JOIN class_attendance ca ON cs.session_id = ca.session_id AND css.student_id = ca.student_id " +
            "        WHERE ca.attendance_time BETWEEN #{startDate} AND #{endDate} " +
            "        GROUP BY cs.session_id " +
            "        HAVING rate &lt; 60" +
            "    ) as low_attendance) as absenceSessions " +
            "</script>")
    AttendanceStatisticsDTO selectDashboardMetrics(@Param("startDate") Date startDate,
                                                   @Param("endDate") Date endDate);
}