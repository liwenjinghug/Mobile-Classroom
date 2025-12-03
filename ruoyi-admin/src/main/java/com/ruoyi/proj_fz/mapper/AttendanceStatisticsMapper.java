package com.ruoyi.proj_fz.mapper;

import com.ruoyi.proj_fz.domain.AttendanceStatisticsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

@Mapper
public interface AttendanceStatisticsMapper {

    // 按课堂维度统计 - 修复查询，计算平均值
    @Select("<script>" +
            "SELECT " +
            "    cs.session_id as sessionId, " +
            "    cs.class_name as className, " +
            "    COALESCE(ROUND(AVG(total_count), 0), 0) as totalStudents, " +
            "    COALESCE(ROUND(AVG(signed_count), 0), 0) as signedCount, " +
            "    COALESCE(ROUND(AVG(absent_count), 0), 0) as absentCount, " +
            "    COALESCE(ROUND(AVG(late_count), 0), 0) as lateCount, " +
            "    COALESCE(ROUND(AVG(leave_count), 0), 0) as leaveCount, " +
            "    COALESCE(ROUND(AVG(early_leave_count), 0), 0) as earlyLeaveCount, " +
            "    COALESCE(ROUND(AVG(attendance_rate), 2), 0) as attendanceRate " +
            "FROM class_session cs " +
            "LEFT JOIN (" +
            "    SELECT " +
            "        ca.session_id, " +
            "        ca.task_id, " +
            "        COUNT(*) as total_count, " +
            "        COUNT(CASE WHEN ca.attendance_status = 1 THEN 1 END) as signed_count, " +
            "        COUNT(CASE WHEN ca.attendance_status = 0 THEN 1 END) as absent_count, " +
            "        COUNT(CASE WHEN ca.attendance_status = 2 THEN 1 END) as late_count, " +
            "        COUNT(CASE WHEN ca.attendance_status = 3 THEN 1 END) as leave_count, " +
            "        COUNT(CASE WHEN ca.attendance_status = 4 THEN 1 END) as early_leave_count, " +
            "        ROUND(COUNT(CASE WHEN ca.attendance_status IN (1, 2) THEN 1 END) * 100.0 / GREATEST(COUNT(*), 1), 2) as attendance_rate " +
            "    FROM class_attendance ca " +
            "    WHERE 1=1 " +
            "        <if test='startDate != null'>" +
            "            AND DATE(ca.created_at) >= DATE(#{startDate}) " +
            "        </if>" +
            "        <if test='endDate != null'>" +
            "            AND DATE(ca.created_at) &lt;= DATE(#{endDate}) " +
            "        </if>" +
            "    GROUP BY ca.session_id, ca.task_id" +
            ") as task_stats ON cs.session_id = task_stats.session_id " +
            "WHERE 1=1 " +
            "    <if test='sessionId != null'>" +
            "        AND cs.session_id = #{sessionId} " +
            "    </if>" +
            "GROUP BY cs.session_id, cs.class_name " +
            "ORDER BY cs.session_id" +
            "</script>")
    List<AttendanceStatisticsDTO> selectSessionStatistics(@Param("sessionId") Long sessionId,
                                                          @Param("startDate") Date startDate,
                                                          @Param("endDate") Date endDate);

    // 按时间维度统计 - 修复课堂名称关联问题
    @Select("<script>" +
            "SELECT " +
            "    cat.task_id as taskId, " +
            "    cat.create_time as statDate, " +
            "    cs.class_name as className, " +
            "    cs.session_id as sessionId, " +
            "    COUNT(CASE WHEN ca.attendance_status = 1 THEN 1 END) as dailySigned, " +
            "    COUNT(CASE WHEN ca.attendance_status = 0 THEN 1 END) as dailyAbsent, " +
            "    COUNT(CASE WHEN ca.attendance_status = 2 THEN 1 END) as dailyLate, " +
            "    COUNT(CASE WHEN ca.attendance_status = 3 THEN 1 END) as dailyLeave, " +
            "    COUNT(CASE WHEN ca.attendance_status = 4 THEN 1 END) as dailyEarlyLeave, " +
            "    COUNT(DISTINCT ca.student_id) as totalStudents, " +
            "    ROUND(COUNT(CASE WHEN ca.attendance_status IN (1, 2) THEN 1 END) * 100.0 / GREATEST(COUNT(DISTINCT ca.student_id), 1), 2) as attendanceRate " +
            "FROM class_attendance_task cat " +
            "LEFT JOIN class_attendance ca ON cat.task_id = ca.task_id " +
            "LEFT JOIN class_session cs ON cat.session_id = cs.session_id " +
            "WHERE 1=1 " +
            "    <if test='sessionId != null'>" +
            "        AND cat.session_id = #{sessionId} " +
            "    </if>" +
            "    <if test='startDate != null'>" +
            "        AND DATE(cat.create_time) >= DATE(#{startDate}) " +
            "    </if>" +
            "    <if test='endDate != null'>" +
            "        AND DATE(cat.create_time) &lt;= DATE(#{endDate}) " +
            "    </if>" +
            "GROUP BY cat.task_id, cat.create_time, cs.class_name, cs.session_id " +
            "ORDER BY cat.create_time ASC" +
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
            "        ELSE 'Web' " +
            "    END as attendanceMethod " +
            "FROM class_attendance ca " +
            "JOIN class_student s ON ca.student_id = s.student_id " +
            "JOIN class_session cs ON ca.session_id = cs.session_id " +
            "WHERE 1=1 " +
            "    <if test='sessionId != null'>" +
            "        AND ca.session_id = #{sessionId} " +
            "    </if>" +
            "    <if test='startDate != null'>" +
            "        AND DATE(ca.created_at) >= DATE(#{startDate}) " +
            "    </if>" +
            "    <if test='endDate != null'>" +
            "        AND DATE(ca.created_at) &lt;= DATE(#{endDate}) " +
            "    </if>" +
            "    <if test='attendanceStatus != null'>" +
            "        AND ca.attendance_status = #{attendanceStatus} " +
            "    </if>" +
            "ORDER BY ca.created_at DESC" +
            "</script>")
    List<AttendanceStatisticsDTO> selectAttendanceDetails(@Param("sessionId") Long sessionId,
                                                          @Param("startDate") Date startDate,
                                                          @Param("endDate") Date endDate,
                                                          @Param("attendanceStatus") Integer attendanceStatus);

    // 导出用的查询方法 - 课堂维度统计
    @Select("<script>" +
            "SELECT " +
            "    cs.session_id as sessionId, " +
            "    cs.class_name as className, " +
            "    COUNT(DISTINCT css.student_id) as totalStudents, " +
            "    COALESCE(ROUND(AVG(signed_count), 0), 0) as signedCount, " +
            "    COALESCE(ROUND(AVG(absent_count), 0), 0) as absentCount, " +
            "    COALESCE(ROUND(AVG(late_count), 0), 0) as lateCount, " +
            "    COALESCE(ROUND(AVG(leave_count), 0), 0) as leaveCount, " +
            "    COALESCE(ROUND(AVG(early_leave_count), 0), 0) as earlyLeaveCount, " +
            "    COALESCE(ROUND(AVG(attendance_rate), 2), 0) as attendanceRate " +
            "FROM class_session cs " +
            "LEFT JOIN class_session_student css ON cs.session_id = css.session_id " +
            "LEFT JOIN (" +
            "    SELECT " +
            "        ca.session_id, " +
            "        ca.task_id, " +
            "        COUNT(CASE WHEN ca.attendance_status = 1 THEN 1 END) as signed_count, " +
            "        COUNT(CASE WHEN ca.attendance_status = 0 THEN 1 END) as absent_count, " +
            "        COUNT(CASE WHEN ca.attendance_status = 2 THEN 1 END) as late_count, " +
            "        COUNT(CASE WHEN ca.attendance_status = 3 THEN 1 END) as leave_count, " +
            "        COUNT(CASE WHEN ca.attendance_status = 4 THEN 1 END) as early_leave_count, " +
            "        ROUND(COUNT(CASE WHEN ca.attendance_status IN (1, 2) THEN 1 END) * 100.0 / GREATEST(COUNT(*), 1), 2) as attendance_rate " +
            "    FROM class_attendance ca " +
            "    WHERE 1=1 " +
            "        <if test='startDate != null'>" +
            "            AND DATE(ca.created_at) >= DATE(#{startDate}) " +
            "        </if>" +
            "        <if test='endDate != null'>" +
            "            AND DATE(ca.created_at) &lt;= DATE(#{endDate}) " +
            "        </if>" +
            "    GROUP BY ca.session_id, ca.task_id" +
            ") as task_stats ON cs.session_id = task_stats.session_id " +
            "WHERE 1=1 " +
            "    <if test='sessionId != null'>" +
            "        AND cs.session_id = #{sessionId} " +
            "    </if>" +
            "GROUP BY cs.session_id, cs.class_name " +
            "ORDER BY cs.session_id" +
            "</script>")
    List<AttendanceStatisticsDTO> selectExportSessionStatistics(@Param("sessionId") Long sessionId,
                                                                @Param("startDate") Date startDate,
                                                                @Param("endDate") Date endDate);

    // 导出时间维度统计 - 修复课堂名称问题
    @Select("<script>" +
            "SELECT " +
            "    cat.task_id as taskId, " +
            "    cat.create_time as statDate, " +
            "    cs.class_name as className, " +
            "    cs.session_id as sessionId, " +
            "    COUNT(CASE WHEN ca.attendance_status = 1 THEN 1 END) as dailySigned, " +
            "    COUNT(CASE WHEN ca.attendance_status = 0 THEN 1 END) as dailyAbsent, " +
            "    COUNT(CASE WHEN ca.attendance_status = 2 THEN 1 END) as dailyLate, " +
            "    COUNT(CASE WHEN ca.attendance_status = 3 THEN 1 END) as dailyLeave, " +
            "    COUNT(CASE WHEN ca.attendance_status = 4 THEN 1 END) as dailyEarlyLeave, " +
            "    COUNT(DISTINCT ca.student_id) as totalStudents, " +
            "    ROUND(COUNT(CASE WHEN ca.attendance_status IN (1, 2) THEN 1 END) * 100.0 / GREATEST(COUNT(DISTINCT ca.student_id), 1), 2) as attendanceRate " +
            "FROM class_attendance_task cat " +
            "LEFT JOIN class_attendance ca ON cat.task_id = ca.task_id " +
            "LEFT JOIN class_session cs ON cat.session_id = cs.session_id " +
            "WHERE 1=1 " +
            "    <if test='sessionId != null'>" +
            "        AND cat.session_id = #{sessionId} " +
            "    </if>" +
            "    <if test='startDate != null'>" +
            "        AND DATE(cat.create_time) >= DATE(#{startDate}) " +
            "    </if>" +
            "    <if test='endDate != null'>" +
            "        AND DATE(cat.create_time) &lt;= DATE(#{endDate}) " +
            "    </if>" +
            "GROUP BY cat.task_id, cat.create_time, cs.class_name, cs.session_id " +
            "ORDER BY cat.create_time ASC" +
            "</script>")
    List<AttendanceStatisticsDTO> selectExportTimeStatistics(@Param("sessionId") Long sessionId,
                                                             @Param("startDate") Date startDate,
                                                             @Param("endDate") Date endDate);

    // 导出明细查询
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
            "        ELSE 'Web' " +
            "    END as attendanceMethod " +
            "FROM class_attendance ca " +
            "JOIN class_student s ON ca.student_id = s.student_id " +
            "JOIN class_session cs ON ca.session_id = cs.session_id " +
            "WHERE 1=1 " +
            "    <if test='sessionId != null'>" +
            "        AND ca.session_id = #{sessionId} " +
            "    </if>" +
            "    <if test='startDate != null'>" +
            "        AND DATE(ca.created_at) >= DATE(#{startDate}) " +
            "    </if>" +
            "    <if test='endDate != null'>" +
            "        AND DATE(ca.created_at) &lt;= DATE(#{endDate}) " +
            "    </if>" +
            "    <if test='attendanceStatus != null'>" +
            "        AND ca.attendance_status = #{attendanceStatus} " +
            "    </if>" +
            "ORDER BY ca.created_at DESC" +
            "</script>")
    List<AttendanceStatisticsDTO> selectExportAttendanceDetails(@Param("sessionId") Long sessionId,
                                                                @Param("startDate") Date startDate,
                                                                @Param("endDate") Date endDate,
                                                                @Param("attendanceStatus") Integer attendanceStatus);

    // 周报表统计
    @Select("<script>" +
            "SELECT " +
            "    cs.session_id as sessionId, " +
            "    cs.class_name as className, " +
            "    COUNT(DISTINCT css.student_id) as totalStudents, " +
            "    COUNT(CASE WHEN ca.attendance_status IN (1, 2) THEN 1 END) as signedCount, " +
            "    ROUND(COUNT(CASE WHEN ca.attendance_status IN (1, 2) THEN 1 END) * 100.0 / GREATEST(COUNT(DISTINCT css.student_id), 1), 2) as avgAttendanceRate, " +
            "    CONCAT(DATE_FORMAT(#{startDate}, '%Y-%m-%d'), ' 至 ', DATE_FORMAT(#{endDate}, '%Y-%m-%d')) as weekRange " +
            "FROM class_session cs " +
            "LEFT JOIN class_session_student css ON cs.session_id = css.session_id " +
            "LEFT JOIN class_attendance ca ON cs.session_id = ca.session_id AND css.student_id = ca.student_id " +
            "WHERE 1=1 " +
            "    <if test='startDate != null'>" +
            "        AND DATE(ca.created_at) >= DATE(#{startDate}) " +
            "    </if>" +
            "    <if test='endDate != null'>" +
            "        AND DATE(ca.created_at) &lt;= DATE(#{endDate}) " +
            "    </if>" +
            "GROUP BY cs.session_id, cs.class_name " +
            "ORDER BY avgAttendanceRate DESC" +
            "</script>")
    List<AttendanceStatisticsDTO> selectWeeklyReport(@Param("startDate") Date startDate,
                                                     @Param("endDate") Date endDate);

    // 缺勤次数最多的学生排名
    @Select("<script>" +
            "SELECT " +
            "    s.student_name as studentName, " +
            "    s.student_no as studentNo, " +
            "    COUNT(CASE WHEN ca.attendance_status = 0 THEN 1 END) as absenceCount, " +
            "    ROW_NUMBER() OVER (ORDER BY COUNT(CASE WHEN ca.attendance_status = 0 THEN 1 END) DESC) as absenceRank " +
            "FROM class_attendance ca " +
            "JOIN class_student s ON ca.student_id = s.student_id " +
            "WHERE 1=1 " +
            "    <if test='startDate != null'>" +
            "        AND DATE(ca.created_at) >= DATE(#{startDate}) " +
            "    </if>" +
            "    <if test='endDate != null'>" +
            "        AND DATE(ca.created_at) &lt;= DATE(#{endDate}) " +
            "    </if>" +
            "GROUP BY s.student_id, s.student_name, s.student_no " +
            "ORDER BY absenceCount DESC " +
            "LIMIT #{limit}" +
            "</script>")
    List<AttendanceStatisticsDTO> selectTopAbsenceStudents(@Param("startDate") Date startDate,
                                                           @Param("endDate") Date endDate,
                                                           @Param("limit") Integer limit);

    // 全校平均签到率
    @Select("<script>" +
            "SELECT " +
            "    ROUND(COUNT(CASE WHEN ca.attendance_status IN (1, 2) THEN 1 END) * 100.0 / GREATEST(COUNT(*), 1), 2) as schoolAvgRate " +
            "FROM class_attendance ca " +
            "WHERE 1=1 " +
            "    <if test='startDate != null'>" +
            "        AND DATE(ca.created_at) >= DATE(#{startDate}) " +
            "    </if>" +
            "    <if test='endDate != null'>" +
            "        AND DATE(ca.created_at) &lt;= DATE(#{endDate}) " +
            "    </if>" +
            "</script>")
    Double selectSchoolAverageRate(@Param("startDate") Date startDate,
                                   @Param("endDate") Date endDate);

    @Select("<script>" +
            "SELECT " +
            "    ROUND(COUNT(CASE WHEN ca.attendance_status IN (1, 2) THEN 1 END) * 100.0 / GREATEST(COUNT(*), 1), 2) as weekAvgRate " +
            "FROM class_attendance ca " +
            "WHERE YEARWEEK(ca.created_at, 1) = YEARWEEK(CURDATE(), 1) " +
            "    <if test='startDate != null'>" +
            "        AND DATE(ca.created_at) >= DATE(#{startDate}) " +
            "    </if>" +
            "    <if test='endDate != null'>" +
            "        AND DATE(ca.created_at) &lt;= DATE(#{endDate}) " +
            "    </if>" +
            "</script>")
    Double selectWeekAverageRate(@Param("startDate") Date startDate,
                                 @Param("endDate") Date endDate);

    @Select("<script>" +
            "SELECT " +
            "    ROUND(COUNT(CASE WHEN ca.attendance_status IN (1, 2) THEN 1 END) * 100.0 / GREATEST(COUNT(*), 1), 2) as todayAvgRate " +
            "FROM class_attendance ca " +
            "WHERE DATE(ca.created_at) = CURDATE() " +
            "    <if test='startDate != null'>" +
            "        AND DATE(ca.created_at) >= DATE(#{startDate}) " +
            "    </if>" +
            "    <if test='endDate != null'>" +
            "        AND DATE(ca.created_at) &lt;= DATE(#{endDate}) " +
            "    </if>" +
            "</script>")
    Double selectTodayAverageRate(@Param("startDate") Date startDate,
                                  @Param("endDate") Date endDate);

    @Select("<script>" +
            "SELECT " +
            "    cs.session_id as sessionId, " +
            "    cs.class_name as className, " +
            "    COUNT(CASE WHEN ca.attendance_status = 0 THEN 1 END) as absenceCount, " +
            "    ROUND(COUNT(CASE WHEN ca.attendance_status IN (1, 2) THEN 1 END) * 100.0 / GREATEST(COUNT(DISTINCT css.student_id), 1), 2) as attendanceRate " +
            "FROM class_attendance ca " +
            "JOIN class_session cs ON ca.session_id = cs.session_id " +
            "LEFT JOIN class_session_student css ON cs.session_id = css.session_id " +
            "WHERE 1=1 " +
            "    <if test='startDate != null'>" +
            "        AND DATE(ca.created_at) >= DATE(#{startDate}) " +
            "    </if>" +
            "    <if test='endDate != null'>" +
            "        AND DATE(ca.created_at) &lt;= DATE(#{endDate}) " +
            "    </if>" +
            "GROUP BY cs.session_id, cs.class_name " +
            "ORDER BY absenceCount DESC " +
            "LIMIT #{limit}" +
            "</script>")
    List<AttendanceStatisticsDTO> selectTopAbsenceSessions(@Param("startDate") Date startDate,
                                                           @Param("endDate") Date endDate,
                                                           @Param("limit") Integer limit);

    @Select("SELECT COUNT(*) as totalSessions FROM class_session WHERE status = 1")
    Integer selectTotalSessions();

    @Select("SELECT COUNT(DISTINCT session_id) as activeSessions FROM class_attendance WHERE DATE(created_at) = CURDATE()")
    Integer selectActiveSessions();

    @Select("SELECT COUNT(*) as totalStudents FROM class_student WHERE status = 0")
    Integer selectTotalStudents();

    @Select("<script>" +
            "SELECT COUNT(*) as absenceSessions FROM (" +
            "SELECT cs.session_id, " +
            "       ROUND(COUNT(CASE WHEN ca.attendance_status IN (1, 2) THEN 1 END) * 100.0 / GREATEST(COUNT(DISTINCT css.student_id), 1), 2) as rate " +
            "FROM class_session cs " +
            "LEFT JOIN class_session_student css ON cs.session_id = css.session_id " +
            "LEFT JOIN class_attendance ca ON cs.session_id = ca.session_id AND css.student_id = ca.student_id " +
            "WHERE 1=1 " +
            "    <if test='startDate != null'>" +
            "        AND DATE(ca.created_at) >= DATE(#{startDate}) " +
            "    </if>" +
            "    <if test='endDate != null'>" +
            "        AND DATE(ca.created_at) &lt;= DATE(#{endDate}) " +
            "    </if>" +
            "GROUP BY cs.session_id " +
            "HAVING rate &lt; 60" +
            ") as low_attendance_sessions" +
            "</script>")
    Integer selectAbsenceSessions(@Param("startDate") Date startDate,
                                  @Param("endDate") Date endDate);
}