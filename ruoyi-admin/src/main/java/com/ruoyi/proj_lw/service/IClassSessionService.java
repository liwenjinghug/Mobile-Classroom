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

    // 删除原来的基于classNumber的方法
    // List<ClassSession> selectSessionsByClassNumber(Integer classNumber);

    // 新增基于courseId的查询方法（可选，因为selectSessionList已经可以按courseId查询）
    // 如果不需要单独的方法，可以直接使用selectSessionList
}