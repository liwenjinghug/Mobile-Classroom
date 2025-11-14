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
            "    ROUND(COUNT(CASE WHEN ca.attendance_status = 1 THEN 1 END) * 100.0 / COUNT(DISTINCT css.student_id), 2) as attendanceRate " +
            "FROM class_session cs " +
            "LEFT JOIN class_session_student css ON cs.session_id = css.session_id " +
            "LEFT JOIN class_attendance ca ON cs.session_id = ca.session_id AND css.student_id = ca.student_id " +
            "<where>" +
            "    <if test='sessionId != null'>" +
            "        AND cs.session_id = #{sessionId} " +
            "    </if>" +
            "    <if test='startDate != null'>" +
            "        AND DATE(ca.attendance_time) >= DATE(#{startDate}) " +
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
            "        AND DATE(ca.attendance_time) >= DATE(#{startDate}) " +
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
            "        AND DATE(ca.attendance_time) >= DATE(#{startDate}) " +
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

    // 缺勤次数Top5课堂
    @Select("SELECT " +
            "    cs.session_id as sessionId, " +
            "    cs.class_name as className, " +
            "    COUNT(CASE WHEN ca.attendance_status = 0 THEN 1 END) as absenceCount " +
            "FROM class_attendance ca " +
            "JOIN class_session cs ON ca.session_id = cs.session_id " +
            "WHERE ca.attendance_time BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY cs.session_id, cs.class_name " +
            "ORDER BY absenceCount DESC " +
            "LIMIT #{limit}")
    List<AttendanceStatisticsDTO> selectTopAbsenceSessions(@Param("startDate") Date startDate,
                                                           @Param("endDate") Date endDate,
                                                           @Param("limit") Integer limit);
}