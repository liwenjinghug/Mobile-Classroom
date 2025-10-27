package com.ruoyi.proj_myx.service.impl;

import com.ruoyi.proj_myx.domain.Attendance;
import com.ruoyi.proj_myx.domain.RandomPickRecord;
import com.ruoyi.proj_myx.mapper.AttendanceMapper;
import com.ruoyi.proj_myx.mapper.RandomPickMapper;
import com.ruoyi.proj_myx.service.IRandomPickService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RandomPickServiceImpl implements IRandomPickService {

    @Autowired
    private AttendanceMapper attendanceMapper;
    @Autowired
    private RandomPickMapper randomPickMapper;

    @Override
    public List<Attendance> getEligibleStudents(Long sessionId) {
        List<Attendance> signed = attendanceMapper.selectSignedBySession(sessionId);
        List<Long> picked = randomPickMapper.selectPickedStudentIdsBySession(sessionId);
        Set<Long> pickedSet = new HashSet<>(picked == null ? Collections.emptyList() : picked);
        return signed == null ? Collections.emptyList() : signed.stream().filter(a -> !pickedSet.contains(a.getStudentId())).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RandomPickRecord pickRandomAndSave(Long sessionId, Long teacherId) {
        List<Attendance> eligible = getEligibleStudents(sessionId);
        if (eligible.isEmpty()) {
            return null;
        }
        Attendance chosen = eligible.get(new Random().nextInt(eligible.size()));
        RandomPickRecord record = new RandomPickRecord();
        record.setSessionId(sessionId);
        record.setTeacherId(teacherId == null ? 0L : teacherId);
        record.setStudentId(chosen.getStudentId());
        record.setPickTime(new Date());
        // roundNo 可在前端或业务层计算，暂不设置
        randomPickMapper.insertRecord(record);
        // 查询联表返回的记录（简易方式：把名字/学号手动填充）
        record.setStudentName(chosen.getStudentName());
        record.setStudentNo(chosen.getStudentNo());
        return record;
    }

    @Override
    public int savePick(RandomPickRecord record) {
        record.setPickTime(new Date());
        return randomPickMapper.insertRecord(record);
    }

    @Override
    public List<RandomPickRecord> getHistory(Long sessionId) {
        return randomPickMapper.selectHistoryBySession(sessionId);
    }
}
