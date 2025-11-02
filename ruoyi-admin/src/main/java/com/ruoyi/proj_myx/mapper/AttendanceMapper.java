package com.ruoyi.proj_myx.mapper;

import com.ruoyi.proj_myx.domain.Attendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AttendanceMapper {

    @Select("SELECT a.attendance_id AS attendanceId, a.session_id AS sessionId, a.student_id AS studentId, a.attendance_time AS attendanceTime, a.attendance_status AS attendanceStatus, a.created_at AS createdAt, a.updated_at AS updatedAt, a.device_ip AS deviceIp, a.device_type AS deviceType, a.location AS location, s.student_name AS studentName, s.student_no AS studentNo FROM class_attendance a LEFT JOIN class_student s ON a.student_id = s.student_id WHERE a.session_id = #{sessionId} AND a.attendance_status = 1")
    List<Attendance> selectSignedBySession(Long sessionId);

    @Select("SELECT a.attendance_id AS attendanceId, a.session_id AS sessionId, a.student_id AS studentId, a.attendance_time AS attendanceTime, a.attendance_status AS attendanceStatus, a.created_at AS createdAt, a.updated_at AS updatedAt, a.device_ip AS deviceIp, a.device_type AS deviceType, a.location AS location FROM class_attendance a WHERE a.session_id = #{sessionId} AND a.student_id = #{studentId} LIMIT 1")
    Attendance selectBySessionAndStudent(Long sessionId, Long studentId);

    // 新增：查询课堂所有学生（基于显式关联表 class_session_student），并返回该学生在当前 session 的签到记录（若无则 attendance_status 默认 0 / attendanceTime null）
    @Select("SELECT s.student_id AS studentId, s.student_no AS studentNo, s.student_name AS studentName, COALESCE(a.attendance_status, 0) AS attendanceStatus, a.attendance_time AS attendanceTime, a.device_ip AS deviceIp, a.location AS location FROM class_session_student ss JOIN class_student s ON ss.student_id = s.student_id LEFT JOIN class_attendance a ON s.student_id = a.student_id AND a.session_id = #{sessionId} WHERE ss.session_id = #{sessionId} ORDER BY s.student_name")
    List<Attendance> selectAllBySession(Long sessionId);

    @Insert("INSERT INTO class_attendance(session_id, student_id, attendance_time, attendance_status, created_at, updated_at, device_ip, device_type, location) VALUES(#{sessionId}, #{studentId}, #{attendanceTime}, #{attendanceStatus}, #{createdAt}, #{updatedAt}, #{deviceIp}, #{deviceType}, #{location})")
    int insertAttendance(Attendance attendance);

    @Update("UPDATE class_attendance SET attendance_time = #{attendanceTime}, attendance_status = #{attendanceStatus}, updated_at = #{updatedAt}, device_ip = #{deviceIp}, device_type = #{deviceType}, location = #{location} WHERE attendance_id = #{attendanceId}")
    int updateAttendance(Attendance attendance);
}
