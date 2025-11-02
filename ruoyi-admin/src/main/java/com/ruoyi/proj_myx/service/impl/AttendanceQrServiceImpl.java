package com.ruoyi.proj_myx.service.impl;

import com.ruoyi.proj_myx.domain.AttendanceQr;
import com.ruoyi.proj_myx.mapper.AttendanceQrMapper;
import com.ruoyi.proj_myx.service.IAttendanceQrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class AttendanceQrServiceImpl implements IAttendanceQrService {

    @Autowired
    private AttendanceQrMapper qrMapper;

    @Override
    public AttendanceQr generateToken(Long taskId, Integer ttlMinutes, String createBy) {
        AttendanceQr qr = new AttendanceQr();
        qr.setTaskId(taskId);
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        qr.setToken(token);
        qr.setTtlSeconds(ttlMinutes == null ? null : ttlMinutes * 60);
        if (ttlMinutes != null) {
            qr.setExpireTime(new Date(System.currentTimeMillis() + ttlMinutes * 60L * 1000L));
        }
        qr.setUsed(0);
        qr.setCreateBy(createBy);
        qrMapper.insertQr(qr);
        return qrMapper.selectById(qr.getQrId());
    }

    @Override
    public AttendanceQr findByToken(String token) {
        return qrMapper.selectByToken(token);
    }

    @Override
    public int markUsed(Long qrId) {
        AttendanceQr qr = new AttendanceQr();
        qr.setQrId(qrId);
        qr.setUsed(1);
        return qrMapper.updateUsed(qr);
    }
}

