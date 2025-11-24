package com.ruoyi.proj_lwj.service.impl;

import com.ruoyi.proj_lwj.domain.ClassExamSession;
import com.ruoyi.proj_lwj.mapper.ClassExamSessionMapper;
import com.ruoyi.proj_lwj.service.IClassExamSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClassExamSessionServiceImpl implements IClassExamSessionService {

    @Autowired
    private ClassExamSessionMapper mapper;

    @Override
    public List<ClassExamSession> selectByExamId(Long examId) {
        return mapper.selectByExamId(examId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int replaceExamSessions(Long examId, List<Long> sessionIds) {
        mapper.deleteByExamId(examId);
        int count = 0;
        if (sessionIds != null) {
            for (Long sid : sessionIds) {
                if (sid == null) continue;
                ClassExamSession rel = new ClassExamSession();
                rel.setExamId(examId);
                rel.setSessionId(sid);
                mapper.insert(rel);
                count++;
            }
        }
        return count;
    }

    @Override
    public int deleteByExamId(Long examId) {
        if (examId == null) return 0;
        return mapper.deleteByExamId(examId);
    }
}
