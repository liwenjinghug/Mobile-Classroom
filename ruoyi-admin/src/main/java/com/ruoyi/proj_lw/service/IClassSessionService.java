package com.ruoyi.proj_lw.service;

import com.ruoyi.proj_lw.domain.ClassSession;
import java.util.List;

public interface IClassSessionService {
    List<ClassSession> selectSessionList(ClassSession session);
    ClassSession selectSessionById(Long sessionId);
    int insertSession(ClassSession session);
    int updateSession(ClassSession session);
    int deleteSessionById(Long sessionId);
    int deleteSessionByIds(Long[] sessionIds);
    List<ClassSession> selectSessionsByClassNumber(Integer classNumber);
}