package com.ruoyi.proj_myx.service;

import com.ruoyi.proj_myx.domain.AttendanceTask;
import com.ruoyi.proj_myx.domain.Attendance;

import java.util.List;

public interface IAttendanceService {
    AttendanceTask createTask(AttendanceTask task);
    List<AttendanceTask> getTasksBySession(Long sessionId);
    AttendanceTask getTask(Long taskId);
    int closeTask(Long taskId);

    // statistics
    List<Attendance> getAllByTask(Long taskId);

    // 学生签到接口：二维码签到与位置签到
    int signByQr(Long taskId, Long studentId, String token);
    int signByLocation(Long taskId, Long studentId, Double lat, Double lng);
}
