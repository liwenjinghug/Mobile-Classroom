package com.ruoyi.proj_lwj.service;

import com.ruoyi.proj_lwj.domain.ClassExamSession;
import java.util.List;

public interface IClassExamSessionService {
    List<ClassExamSession> selectByExamId(Long examId);
    int replaceExamSessions(Long examId, List<Long> sessionIds);
}

