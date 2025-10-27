package com.ruoyi.proj_myx.mapper;

import com.ruoyi.proj_myx.domain.Attendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AttendanceMapper {

    @Select("SELECT a.attendance_id AS attendanceId, a.session_id AS sessionId, a.student_id AS studentId, a.attendance_time AS attendanceTime, a.attendance_status AS attendanceStatus, a.created_at AS createdAt, a.updated_at AS updatedAt, a.device_ip AS deviceIp, a.device_type AS deviceType, a.location AS location, s.student_name AS studentName, s.student_no AS studentNo FROM class_attendance a LEFT JOIN class_student s ON a.student_id = s.student_id WHERE a.session_id = #{sessionId} AND a.attendance_status = 1")
    List<Attendance> selectSignedBySession(Long sessionId);
}
