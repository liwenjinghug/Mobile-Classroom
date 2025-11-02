package com.ruoyi.proj_myx.service.impl;

import com.ruoyi.proj_myx.domain.AttendanceTask;
import com.ruoyi.proj_myx.domain.Attendance;
import com.ruoyi.proj_myx.domain.AttendanceQr;
import com.ruoyi.proj_myx.mapper.AttendanceTaskMapper;
import com.ruoyi.proj_myx.mapper.AttendanceMapper;
import com.ruoyi.proj_myx.mapper.AttendanceQrMapper;
import com.ruoyi.proj_myx.service.IAttendanceService;
import com.ruoyi.proj_myx.service.IAttendanceQrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class AttendanceServiceImpl implements IAttendanceService {

    @Autowired
    private AttendanceTaskMapper taskMapper;

    @Autowired
    private AttendanceMapper attendanceMapper;

    @Autowired
    private IAttendanceQrService qrService;

    @Override
    @Transactional
    public AttendanceTask createTask(AttendanceTask task) {
        taskMapper.insertTask(task);
        return taskMapper.selectById(task.getTaskId());
    }

    @Override
    public List<AttendanceTask> getTasksBySession(Long sessionId) {
        return taskMapper.selectBySession(sessionId);
    }

    @Override
    public AttendanceTask getTask(Long taskId) {
        return taskMapper.selectById(taskId);
    }

    @Override
    public int closeTask(Long taskId) {
        AttendanceTask t = new AttendanceTask();
        t.setTaskId(taskId);
        t.setStatus(2);
        return taskMapper.updateStatus(t);
    }

    @Override
    public List<Attendance> getAllByTask(Long taskId) {
        // reuse existing mapper to fetch all students for the session (include signed and unsigned)
        AttendanceTask t = taskMapper.selectById(taskId);
        if (t == null) return Collections.emptyList();
        return attendanceMapper.selectAllBySession(t.getSessionId());
    }

    @Override
    @Transactional
    public int signByQr(Long taskId, Long studentId, String token) {
        // validate token
        AttendanceQr qr = qrService.findByToken(token);
        if (qr == null) return 0; // token invalid
        if (!qr.getTaskId().equals(taskId)) return 0; // token not for this task
        if (qr.getExpireTime() != null && qr.getExpireTime().before(new Date())) return 0; // expired
        if (qr.getUsed() != null && qr.getUsed() == 1) return 0; // already used

        // insert/update attendance record: set attendance_time = now, attendance_status = 1
        int rows = upsertAttendanceRecord(taskId, studentId, new Date(), 1, null);
        if (rows > 0) {
            qrService.markUsed(qr.getQrId());
        }
        return rows;
    }

    @Override
    @Transactional
    public int signByLocation(Long taskId, Long studentId, Double lat, Double lng) {
        AttendanceTask t = taskMapper.selectById(taskId);
        if (t == null) return 0;
        if (!"location".equalsIgnoreCase(t.getType())) return 0; // not a location task
        if (t.getCenterLat() == null || t.getCenterLng() == null || t.getRadius() == null) return 0;

        double dist = haversineDistanceMeters(t.getCenterLat(), t.getCenterLng(), lat, lng);
        if (dist > t.getRadius()) return 0; // out of range

        // mark attendance
        return upsertAttendanceRecord(taskId, studentId, new Date(), 1, String.format("%f,%f", lat, lng));
    }

    // helper: insert or update class_attendance unique by session_id + student_id
    private int upsertAttendanceRecord(Long taskId, Long studentId, Date attendanceTime, int status, String location) {
        // find session from task
        AttendanceTask t = taskMapper.selectById(taskId);
        if (t == null) return 0;
        Long sessionId = t.getSessionId();
        // check existing attendance record
        Attendance existing = attendanceMapper.selectBySessionAndStudent(sessionId, studentId);
        if (existing == null) {
            // insert new
            Attendance a = new Attendance();
            a.setSessionId(sessionId);
            a.setStudentId(studentId);
            a.setAttendanceTime(attendanceTime);
            a.setAttendanceStatus(status);
            a.setCreatedAt(attendanceTime);
            a.setUpdatedAt(attendanceTime);
            a.setLocation(location);
            return attendanceMapper.insertAttendance(a);
        } else {
            existing.setAttendanceTime(attendanceTime);
            existing.setAttendanceStatus(status);
            existing.setUpdatedAt(attendanceTime);
            existing.setLocation(location);
            return attendanceMapper.updateAttendance(existing);
        }
    }

    // haversine formula to compute meters between two lat/lng points
    private double haversineDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        int R = 6371000; // Earth radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
}
