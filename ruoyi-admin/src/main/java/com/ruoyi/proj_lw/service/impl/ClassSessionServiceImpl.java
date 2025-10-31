package com.ruoyi.proj_lw.service.impl;

import com.ruoyi.proj_lw.domain.ClassSession;
import com.ruoyi.proj_lw.mapper.ClassSessionMapper;
import com.ruoyi.proj_lw.service.IClassSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ClassSessionServiceImpl implements IClassSessionService {

    @Autowired
    private ClassSessionMapper sessionMapper;

    @Override
    public List<ClassSession> selectSessionList(ClassSession session) {
        return sessionMapper.selectSessionList(session);
    }

    @Override
    public ClassSession selectSessionById(Long sessionId) {
        return sessionMapper.selectSessionById(sessionId);
    }

    @Override
    public int insertSession(ClassSession session) {
        return sessionMapper.insertSession(session);
    }

    @Override
    public int updateSession(ClassSession session) {
        return sessionMapper.updateSession(session);
    }

    @Override
    public int deleteSessionById(Long sessionId) {
        return sessionMapper.deleteSessionById(sessionId);
    }

    @Override
    public int deleteSessionByIds(Long[] sessionIds) {
        return sessionMapper.deleteSessionByIds(sessionIds);
    }

    @Override
    public List<ClassSession> selectSessionsByClassNumber(Integer classNumber) {
        return sessionMapper.selectSessionsByClassNumber(classNumber);
    }
}