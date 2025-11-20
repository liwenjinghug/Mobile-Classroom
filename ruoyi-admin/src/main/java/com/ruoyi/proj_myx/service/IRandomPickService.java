package com.ruoyi.proj_myx.service;

import com.ruoyi.proj_myx.domain.Attendance;
import com.ruoyi.proj_myx.domain.RandomPickRecord;

import java.util.List;

public interface IRandomPickService {
    List<Attendance> getEligibleStudents(Long sessionId);
    RandomPickRecord pickRandomAndSave(Long sessionId, Long teacherId);
    int savePick(RandomPickRecord record);
    List<RandomPickRecord> getHistory(Long sessionId);
    int deletePick(Long rpickId);
    int updatePick(RandomPickRecord record);
}
