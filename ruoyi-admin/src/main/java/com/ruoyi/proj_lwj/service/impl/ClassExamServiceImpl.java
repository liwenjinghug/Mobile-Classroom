package com.ruoyi.proj_lwj.service.impl;

import com.ruoyi.proj_lwj.domain.ClassExam;
import com.ruoyi.proj_lwj.mapper.ClassExamMapper;
import com.ruoyi.proj_lwj.mapper.ClassExamQuestionMapper;
import com.ruoyi.proj_lwj.mapper.ClassExamParticipantMapper;
import com.ruoyi.proj_lwj.mapper.ClassExamAnswerMapper;
import com.ruoyi.proj_lwj.mapper.ClassExamMonitorMapper;
import com.ruoyi.proj_lwj.mapper.ClassExamSessionMapper;
import com.ruoyi.proj_lwj.service.IClassExamService;
import com.ruoyi.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassExamServiceImpl implements IClassExamService {

    @Autowired
    private ClassExamMapper examMapper;
    @Autowired
    private ClassExamQuestionMapper questionMapper;
    @Autowired
    private ClassExamParticipantMapper participantMapper;
    @Autowired
    private ClassExamAnswerMapper answerMapper;
    @Autowired
    private ClassExamMonitorMapper monitorMapper;
    @Autowired
    private ClassExamSessionMapper sessionMapper;

    @Override
    public List<ClassExam> selectExamList(ClassExam exam) {
        return examMapper.selectExamList(exam);
    }

    @Override
    public ClassExam selectExamById(Long id) {
        return examMapper.selectExamById(id);
    }

    private void assertUniqueExamName(ClassExam exam, boolean isUpdate) {
        if (exam == null) return;
        Long sessionId = exam.getSessionId();
        String examName = exam.getExamName();
        if (sessionId == null || examName == null || examName.trim().isEmpty()) return;
        Long excludeId = isUpdate ? exam.getId() : null;
        int dup = examMapper.countBySessionAndName(sessionId, examName.trim(), excludeId);
        if (dup > 0) {
            throw new ServiceException("同一课堂下考试名称已存在，请更换名称");
        }
    }

    @Override
    public int insertExam(ClassExam exam) {
        assertUniqueExamName(exam, false);
        return examMapper.insertExam(exam);
    }

    @Override
    public int updateExam(ClassExam exam) {
        assertUniqueExamName(exam, true);
        return examMapper.updateExam(exam);
    }

    @Override
    public int deleteExamById(Long id) {
        // 级联删除：先删子表再删考试
        answerMapper.deleteByExamId(id);
        participantMapper.deleteByExamId(id);
        try {
            monitorMapper.deleteByExamId(id);
        } catch (Exception ignore) {
            // 监控表不存在或删除异常时不中断主流程
        }
        questionMapper.deleteByExamId(id);
        try {
            sessionMapper.deleteByExamId(id);
        } catch (Exception ignore) {
            // 关联表不存在或删除异常时不中断主流程
        }
        return examMapper.deleteExamById(id);
    }

    @Override
    public int deleteExamByIds(Long[] ids) {
        int count = 0;
        if (ids != null) {
            for (Long id : ids) {
                if (id == null) continue;
                count += deleteExamById(id);
            }
        }
        return count;
    }

    @Override
    public int refreshQuestionCount(Long examId) {
        int cnt = questionMapper.countByExamId(examId);
        ClassExam ex = examMapper.selectExamById(examId);
        if (ex != null) {
            ex.setQuestionCount(cnt);
            examMapper.updateExam(ex);
        }
        return cnt;
    }

    @Override
    public List<ClassExam> selectAvailableByStudentNo(String studentNo) {
        return examMapper.selectAvailableByStudentNo(studentNo);
    }
}
