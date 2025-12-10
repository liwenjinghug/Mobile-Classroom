package com.ruoyi.proj_myx.service.impl;

import com.ruoyi.proj_myx.domain.AttendanceTask;
import com.ruoyi.proj_myx.domain.Attendance;
import com.ruoyi.proj_myx.domain.AttendanceQr;
import com.ruoyi.proj_myx.mapper.AttendanceTaskMapper;
import com.ruoyi.proj_myx.mapper.AttendanceMapper;
import com.ruoyi.proj_myx.mapper.AttendanceQrMapper;
import com.ruoyi.proj_lwj.mapper.ClassStudentMapper;
import com.ruoyi.proj_lwj.domain.ClassStudent;
import com.ruoyi.proj_myx.service.IAttendanceService;
import com.ruoyi.proj_myx.service.IAttendanceQrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements IAttendanceService {

    @Autowired
    private AttendanceTaskMapper taskMapper;

    @Autowired
    private AttendanceMapper attendanceMapper;

    @Autowired
    private IAttendanceQrService qrService;

    @Autowired
    private ClassStudentMapper classStudentMapper;

    @Override
    @Transactional
    public AttendanceTask createTask(AttendanceTask task) {
        taskMapper.insertTask(task);
        return taskMapper.selectById(task.getTaskId());
    }

    @Override
    public List<AttendanceTask> getTasksBySession(Long sessionId) {
        List<AttendanceTask> tasks = taskMapper.selectBySession(sessionId);
        if (tasks == null || tasks.isEmpty()) return tasks;

        Date now = new Date();
        for (AttendanceTask task : tasks) {
            Integer currentStatus = task.getStatus();
            Integer newStatus = currentStatus;

            Date start = task.getStartTime();
            Date end = task.getEndTime();

            // Auto-correct status based on time
            if (end != null && now.after(end)) {
                newStatus = 2; // Ended
            } else if (start != null && now.before(start)) {
                newStatus = 0; // Not Started
            } else {
                newStatus = 1; // In Progress
            }

            if (newStatus != null && !newStatus.equals(currentStatus)) {
                task.setStatus(newStatus);
                taskMapper.updateStatusOnly(task);
            }
        }
        return tasks;
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
        t.setEndTime(new Date());
        return taskMapper.updateStatus(t);
    }

    @Override
    public int startTask(Long taskId) {
        AttendanceTask t = new AttendanceTask();
        t.setTaskId(taskId);
        t.setStatus(1);
        t.setStartTime(new Date());
        return taskMapper.updateStatusAndStartTime(t);
    }

    @Override
    public int deleteTask(Long taskId) {
        return taskMapper.deleteById(taskId);
    }

    @Override
    public List<Attendance> getAllByTask(Long taskId) {
        // reuse existing mapper to fetch all students for the session (include signed and unsigned)
        // Note: The mapper method name might be selectAllByTask or similar based on previous read
        return attendanceMapper.selectAllByTask(taskId);
    }

    @Override
    public List<AttendanceTask> getActiveTasksBySession(Long sessionId, Long userId) {
        List<AttendanceTask> allTasks = getTasksBySession(sessionId);
        if (allTasks == null) return Collections.emptyList();

        // Resolve studentId
        Long studentId = null;
        if (userId != null) {
            ClassStudent s = classStudentMapper.selectByUserId(userId);
            if (s != null) studentId = s.getStudentId();
        }
        final Long finalStudentId = studentId;

        // Filter for status = 1 (In Progress)
        return allTasks.stream()
                .filter(t -> t.getStatus() != null && t.getStatus() == 1)
                .peek(t -> {
                    // Generate a default title if missing
                    if (t.getTitle() == null) {
                        String timeStr = t.getCreateTime() != null ? String.format("%tH:%tM", t.getCreateTime(), t.getCreateTime()) : "";
                        String typeStr = "location".equals(t.getType()) ? "位置签到" : "扫码签到";
                        t.setTitle(typeStr + " " + timeStr);
                    }
                    // Check attendance status
                    if (finalStudentId != null) {
                        Attendance a = attendanceMapper.selectByTaskAndStudent(t.getTaskId(), finalStudentId);
                        if (a != null) {
                            t.setAttendanceStatus(a.getAttendanceStatus());
                        } else {
                            t.setAttendanceStatus(0);
                        }
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Attendance> getStudentHistory(Long studentId, Long sessionId) {
        List<Attendance> list;
        if (sessionId != null) {
            list = attendanceMapper.selectTasksWithStatusBySession(studentId, sessionId);
        } else {
            list = attendanceMapper.selectByStudent(studentId, null);
        }
        
        if (list != null) {
            for (Attendance a : list) {
                if (a.getTaskTitle() == null) {
                    a.setTaskTitle("签到任务");
                }
            }
        }
        return list;
    }

    @Transactional
    public int setAttendanceStatus(Long sessionId, Long studentId, Integer status) {
        if (sessionId == null || studentId == null || status == null) return 0;
        Date now = new Date();
        // Try to find existing attendance record for session+student
        Attendance existing = attendanceMapper.selectBySessionAndStudent(sessionId, studentId);
        if (existing == null) {
            Attendance a = new Attendance();
            a.setSessionId(sessionId);
            a.setStudentId(studentId);
            a.setAttendanceStatus(status);
            // Get the latest task for the session and associate it with the attendance record
            AttendanceTask latestTask = taskMapper.selectLatestTaskBySession(sessionId);
            if (latestTask != null) {
                a.setTaskId(latestTask.getTaskId());
            }
            if (status != null && status == 1) {
                a.setAttendanceTime(now);
            }
            a.setCreatedAt(now);
            a.setUpdatedAt(now);
            return attendanceMapper.insertAttendance(a);
        } else {
            existing.setAttendanceStatus(status);
            if (status != null && status == 1) {
                existing.setAttendanceTime(now);
            } else if (status != null && status == 0) {
                existing.setAttendanceTime(null);
            }
            existing.setUpdatedAt(now);
            return attendanceMapper.updateAttendance(existing);
        }
    }

    @Override
    @Transactional
    public int signByQr(Long taskId, Long studentId, String token) {
        // Resolve studentId from userId
        ClassStudent s = classStudentMapper.selectByUserId(studentId);
        if (s == null) {
             System.out.println("SignByQr Fail: User " + studentId + " is not a student.");
             return 0;
        }
        Long realStudentId = s.getStudentId();

        // validate token
        AttendanceQr qr = qrService.findByToken(token);
        if (qr == null) return 0; // token invalid
        if (!qr.getTaskId().equals(taskId)) return 0; // token not for this task
        if (qr.getExpireTime() != null && qr.getExpireTime().before(new Date())) return 0; // expired
        if (qr.getUsed() != null && qr.getUsed() == 1) return 0; // already used

        // insert/update attendance record: set attendance_time = now, attendance_status = 1
        int rows = upsertAttendanceRecord(taskId, realStudentId, new Date(), 1, null);
        if (rows > 0) {
            qrService.markUsed(qr.getQrId());
        }
        return rows;
    }

    @Override
    @Transactional
    public int signByLocation(Long taskId, Long studentId, Double lat, Double lng) {
        // Resolve studentId from userId
        ClassStudent s = classStudentMapper.selectByUserId(studentId);
        if (s == null) {
             System.out.println("SignByLocation Fail: User " + studentId + " is not a student.");
             return 0;
        }
        Long realStudentId = s.getStudentId();

        AttendanceTask t = taskMapper.selectById(taskId);
        if (t == null) {
            System.out.println("SignByLocation Fail: Task not found: " + taskId);
            return 0;
        }
        if (!"location".equalsIgnoreCase(t.getType())) {
            System.out.println("SignByLocation Fail: Task type mismatch: " + t.getType());
            return 0; 
        }
        if (t.getCenterLat() == null || t.getCenterLng() == null || t.getRadius() == null) {
             System.out.println("SignByLocation Fail: Task location info missing");
             return 0;
        }

        double dist = haversineDistanceMeters(t.getCenterLat(), t.getCenterLng(), lat, lng);
        System.out.println(String.format("SignByLocation: TaskCenter=(%f, %f), Student=(%f, %f), Dist=%f, Radius=%f", 
            t.getCenterLat(), t.getCenterLng(), lat, lng, dist, t.getRadius().doubleValue()));

        if (dist > t.getRadius()) return 0; // out of range

        // mark attendance
        return upsertAttendanceRecord(taskId, realStudentId, new Date(), 1, String.format("%f,%f", lat, lng));
    }

    @Override
    @Transactional
    public int updateAttendanceStatus(Long taskId, Long studentId, Integer status) {
        if (taskId == null || studentId == null || status == null) return 0;
        Attendance existing = attendanceMapper.selectByTaskAndStudent(taskId, studentId);
        Date now = new Date();
        if (existing != null) {
            existing.setAttendanceStatus(status);
            // 1=已签到, 2=迟到 都应有签到时间；0=未签到 清空时间；其它状态保留原时间
            if (status == 1 || status == 2) {
                existing.setAttendanceTime(now);
            } else if (status == 0 || status == 3 || status == 4) {
                existing.setAttendanceTime(null);
            }
            existing.setUpdatedAt(now);
            return attendanceMapper.updateAttendance(existing);
        } else {
            Attendance a = new Attendance();
            a.setTaskId(taskId);
            a.setStudentId(studentId);
            a.setAttendanceStatus(status);
            a.setCreatedAt(now);
            a.setUpdatedAt(now);
            if (status == 1 || status == 2) {
                a.setAttendanceTime(now);
            }
            // also set session_id from task
            AttendanceTask t = taskMapper.selectById(taskId);
            if (t != null) a.setSessionId(t.getSessionId());
            return attendanceMapper.insertAttendance(a);
        }
    }

    // helper: insert or update class_attendance unique by task_id + student_id
    private int upsertAttendanceRecord(Long taskId, Long studentId, Date attendanceTime, int status, String location) {
        // check existing attendance record by taskId + studentId
        Attendance existing = attendanceMapper.selectByTaskAndStudent(taskId, studentId);
        if (existing == null) {
            // insert new
            Attendance a = new Attendance();
            a.setTaskId(taskId);
            // also set session_id from task
            AttendanceTask t = taskMapper.selectById(taskId);
            if (t != null) a.setSessionId(t.getSessionId());
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
            // also set session_id from task
            AttendanceTask t = taskMapper.selectById(taskId);
            if (t != null) existing.setSessionId(t.getSessionId());
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
