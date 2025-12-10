package com.ruoyi.proj_myx.mapper;

import com.ruoyi.proj_myx.domain.Attendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AttendanceMapper {

    @Select("WITH LatestAttendance AS ( " +
            "    SELECT " +
            "        a.*, " +
            "        ROW_NUMBER() OVER(PARTITION BY a.session_id, a.student_id ORDER BY a.updated_at DESC) as row_num " +
            "    FROM class_attendance a " +
            "    WHERE a.session_id = #{sessionId} " +
            ") " +
            "SELECT " +
            "    la.attendance_id AS attendanceId, " +
            "    la.session_id AS sessionId, " +
            "    la.task_id AS taskId, " +
            "    la.student_id AS studentId, " +
            "    la.attendance_time AS attendanceTime, " +
            "    la.attendance_status AS attendanceStatus, " +
            "    la.created_at AS createdAt, " +
            "    la.updated_at AS updatedAt, " +
            "    la.device_ip AS deviceIp, " +
            "    la.device_type AS deviceType, " +
            "    la.location AS location, " +
            "    s.student_name AS studentName, " +
            "    s.student_no AS studentNo " +
            "FROM LatestAttendance la " +
            "LEFT JOIN class_student s ON la.student_id = s.student_id " +
            "WHERE la.row_num = 1 AND la.attendance_status = 1")
    List<Attendance> selectSignedBySession(Long sessionId);

    @Select("SELECT a.attendance_id AS attendanceId, a.session_id AS sessionId, a.student_id AS studentId, a.attendance_time AS attendanceTime, a.attendance_status AS attendanceStatus, a.created_at AS createdAt, a.updated_at AS updatedAt, a.device_ip AS deviceIp, a.device_type AS deviceType, a.location AS location FROM class_attendance a WHERE a.session_id = #{sessionId} AND a.student_id = #{studentId} ORDER BY updated_at DESC LIMIT 1")
    Attendance selectBySessionAndStudent(Long sessionId, Long studentId);

    // 新增：查询课堂所有学生（基于显式关联表 class_session_student），并返回该学生在当前 session 的签到记录（若无则 attendance_status 默认 0 / attendanceTime null）
    @Select("SELECT s.student_id AS studentId, s.student_no AS studentNo, s.student_name AS studentName, COALESCE(a.attendance_status, 0) AS attendanceStatus, a.attendance_time AS attendanceTime, a.device_ip AS deviceIp, a.location AS location FROM class_session_student ss JOIN class_student s ON ss.student_id = s.student_id LEFT JOIN class_attendance a ON s.student_id = a.student_id AND a.session_id = #{sessionId} WHERE ss.session_id = #{sessionId} ORDER BY s.student_name")
    List<Attendance> selectAllBySession(Long sessionId);

    @Select("SELECT a.attendance_id AS attendanceId, a.session_id AS sessionId, a.task_id AS taskId, a.student_id AS studentId, a.attendance_time AS attendanceTime, a.attendance_status AS attendanceStatus, a.created_at AS createdAt, a.updated_at AS updatedAt, a.device_ip AS deviceIp, a.device_type AS deviceType, a.location AS location FROM class_attendance a WHERE a.task_id = #{taskId} AND a.student_id = #{studentId} LIMIT 1")
    Attendance selectByTaskAndStudent(@Param("taskId") Long taskId, @Param("studentId") Long studentId);

    @Select("SELECT s.student_id AS studentId, s.student_no AS studentNo, s.student_name AS studentName, COALESCE(a.attendance_status, 0) AS attendanceStatus, a.attendance_time AS attendanceTime, a.device_ip AS deviceIp, a.location AS location FROM class_session_student ss JOIN class_student s ON ss.student_id = s.student_id LEFT JOIN class_attendance a ON s.student_id = a.student_id AND a.task_id = #{taskId} WHERE ss.session_id = (SELECT session_id FROM class_attendance_task WHERE task_id = #{taskId}) ORDER BY s.student_name")
    List<Attendance> selectAllByTask(Long taskId);

    @Insert("INSERT INTO class_attendance(task_id, session_id, student_id, attendance_time, attendance_status, created_at, updated_at, device_ip, device_type, location) VALUES(#{taskId}, #{sessionId}, #{studentId}, #{attendanceTime}, #{attendanceStatus}, #{createdAt}, #{updatedAt}, #{deviceIp}, #{deviceType}, #{location})")
    int insertAttendance(Attendance attendance);

    @Update("UPDATE class_attendance SET attendance_time = #{attendanceTime}, attendance_status = #{attendanceStatus}, session_id = #{sessionId}, updated_at = #{updatedAt}, device_ip = #{deviceIp}, device_type = #{deviceType}, location = #{location} WHERE attendance_id = #{attendanceId}")
    int updateAttendance(Attendance attendance);

    @Select("<script>" +
            "SELECT a.attendance_id AS attendanceId, " +
            "       COALESCE(a.session_id, t.session_id) AS sessionId, " +
            "       a.task_id AS taskId, " +
            "       a.student_id AS studentId, " +
            "       a.attendance_time AS attendanceTime, " +
            "       a.attendance_status AS attendanceStatus, " +
            "       a.created_at AS createdAt, " +
            "       a.updated_at AS updatedAt, " +
            "       a.device_ip AS deviceIp, " +
            "       a.device_type AS deviceType, " +
            "       a.location AS location, " +
            "       c.course_name AS courseName " +
            "FROM class_attendance a " +
            "LEFT JOIN class_attendance_task t ON a.task_id = t.task_id " +
            "LEFT JOIN class_session cs ON COALESCE(a.session_id, t.session_id) = cs.session_id " +
            "LEFT JOIN class_course c ON cs.course_id = c.course_id " +
            "WHERE a.student_id = #{studentId} " +
            "<if test='sessionId != null'> AND COALESCE(a.session_id, t.session_id) = #{sessionId} </if>" +
            "ORDER BY a.attendance_time DESC" +
            "</script>")
    List<Attendance> selectByStudent(@Param("studentId") Long studentId, @Param("sessionId") Long sessionId);

    @Select("SELECT " +
            "    t.task_id AS taskId, " +
            "    t.session_id AS sessionId, " +
            "    t.title AS title, " +
            "    t.type AS type, " +
            "    t.start_time AS startTime, " +
            "    COALESCE(t.title, CONCAT(CASE WHEN t.type = 'location' THEN '位置签到' ELSE '二维码签到' END, ' ', DATE_FORMAT(t.create_time, '%Y-%m-%d %H:%i'))) AS taskTitle, " +
            "    t.create_time AS createdAt, " +
            "    COALESCE(a.attendance_status, 0) AS attendanceStatus, " +
            "    a.attendance_time AS attendanceTime, " +
            "    a.attendance_id AS attendanceId, " +
            "    c.course_name AS courseName " +
            "FROM class_attendance_task t " +
            "LEFT JOIN class_attendance a ON t.task_id = a.task_id AND a.student_id = #{studentId} " +
            "LEFT JOIN class_session cs ON t.session_id = cs.session_id " +
            "LEFT JOIN class_course c ON cs.course_id = c.course_id " +
            "WHERE t.session_id = #{sessionId} " +
            "ORDER BY t.create_time DESC")
    List<Attendance> selectTasksWithStatusBySession(@Param("studentId") Long studentId, @Param("sessionId") Long sessionId);
}
