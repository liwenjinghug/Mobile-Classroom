package com.ruoyi.proj_myx.service;

import com.ruoyi.proj_myx.domain.AttendanceQr;

public interface IAttendanceQrService {
    AttendanceQr generateToken(Long taskId, Integer ttlMinutes, String createBy);
    AttendanceQr findByToken(String token);
    int markUsed(Long qrId);
}

