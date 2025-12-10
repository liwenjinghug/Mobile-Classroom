package com.ruoyi.proj_myx.service;

import com.ruoyi.proj_myx.domain.AttendanceTask;
import com.ruoyi.proj_myx.domain.Attendance;

import java.util.List;

public interface IAttendanceService {
    AttendanceTask createTask(AttendanceTask task);
    List<AttendanceTask> getTasksBySession(Long sessionId);
    AttendanceTask getTask(Long taskId);
    int closeTask(Long taskId);
    int startTask(Long taskId);
    int deleteTask(Long taskId);

    // statistics
    List<Attendance> getAllByTask(Long taskId);

    // 学生签到接口：二维码签到与位置签到
    int signByQr(Long taskId, Long studentId, String token);
    int signByLocation(Long taskId, Long studentId, Double lat, Double lng);

    int updateAttendanceStatus(Long taskId, Long studentId, Integer status);

    /**
     * 获取某课堂下所有活跃（进行中）的签到任务，并附带当前用户的签到状态
     */
    List<AttendanceTask> getActiveTasksBySession(Long sessionId, Long userId);

    /**
     * 获取某学生的所有签到记录
     */
    List<Attendance> getStudentHistory(Long studentId, Long sessionId);
}
