package com.ruoyi.proj_lwj.service.impl;

import com.ruoyi.proj_lwj.domain.ClassExamQuestion;
import com.ruoyi.proj_lwj.mapper.ClassExamQuestionMapper;
import com.ruoyi.proj_lwj.service.IClassExamQuestionService;
import com.ruoyi.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.math.BigDecimal;

@Service
public class ClassExamQuestionServiceImpl implements IClassExamQuestionService {

    @Autowired
    private ClassExamQuestionMapper mapper;
    @Autowired
    private com.ruoyi.proj_lwj.mapper.LocalQuestionBankMapper localBankMapper;
    @Autowired(required = false)
    private com.ruoyi.proj_lwj.service.IClassExamService examService;

    @Override
    public List<ClassExamQuestion> selectQuestionList(ClassExamQuestion q) {
        return mapper.selectQuestionList(q);
    }

    @Override
    public ClassExamQuestion selectById(Long id) {
        return mapper.selectById(id);
    }

    private void assertNoDuplicateQuestion(ClassExamQuestion q, boolean isUpdate) {
        if (q == null) return;
        Long examId = q.getExamId();
        String content = q.getQuestionContent();
        if (examId != null && content != null && !content.trim().isEmpty()) {
            Long excludeId = isUpdate ? q.getId() : null;
            int dup = mapper.countByExamAndContent(examId, content.trim(), excludeId);
            if (dup > 0) throw new ServiceException("同一考试下题目内容重复，请修改题目");
        }
        Integer type = q.getQuestionType();
        if (type != null && (type == 1 || type == 2 || type == 3)) {
            String optionsJson = q.getQuestionOptions();
            if (optionsJson != null && !optionsJson.trim().isEmpty()) {
                String raw = optionsJson.trim();
                if (raw.startsWith("[") && raw.endsWith("]")) {
                    raw = raw.substring(1, raw.length()-1);
                }
                if (!raw.isEmpty()) {
                    String[] parts = raw.split(",");
                    Set<String> set = new HashSet<>();
                    for (String p : parts) {
                        String v = p.trim();
                        if ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'"))) {
                            v = v.substring(1, v.length()-1);
                        }
                        v = v.trim();
                        String key = v.toLowerCase(Locale.ROOT);
                        if (!set.add(key)) {
                            throw new ServiceException("题目选项存在重复项：" + v);
                        }
                    }
                }
            }
        }
    }

    @Override
    public int insertQuestion(ClassExamQuestion q) {
        assertNoDuplicateQuestion(q, false);
        return mapper.insertQuestion(q);
    }

    @Override
    public int updateQuestion(ClassExamQuestion q) {
        assertNoDuplicateQuestion(q, true);
        return mapper.updateQuestion(q);
    }

    @Override
    public int deleteQuestionById(Long id) {
        return mapper.deleteQuestionById(id);
    }

    @Override
    public int deleteQuestionByIds(Long[] ids) {
        return mapper.deleteQuestionByIds(ids);
    }

    @Override
    public int countByExamId(Long examId) {
        return mapper.countByExamId(examId);
    }

    @Override
    public int batchReorder(List<ClassExamQuestion> items) {
        if (items == null || items.isEmpty()) return 0;
        return mapper.batchUpdateSortOrder(items);
    }

    @Override
    public boolean existsByContent(Long examId, String questionContent) {
        if (examId == null || questionContent == null || questionContent.trim().isEmpty()) {
            return false;
        }
        int count = mapper.countByExamAndContent(examId, questionContent.trim(), null);
        return count > 0;
    }

    @Override
    public BigDecimal calculateTotalScore(Long examId) {
        if (examId == null) return BigDecimal.ZERO;

        ClassExamQuestion query = new ClassExamQuestion();
        query.setExamId(examId);
        List<ClassExamQuestion> questions = mapper.selectQuestionList(query);

        BigDecimal total = BigDecimal.ZERO;
        for (ClassExamQuestion q : questions) {
            if (q.getScore() != null) {
                total = total.add(q.getScore());
            }
        }
        return total;
    }

    @Override
    public ImportResult importFromLocalBank(Long examId, List<Long> bankIds, String operator) {
        if (examId == null) throw new ServiceException("examId不能为空");
        if (bankIds == null || bankIds.isEmpty()) throw new ServiceException("未选择题目");
        // 查询题库题目
        List<Long> distinctIds = new ArrayList<>(new LinkedHashSet<>(bankIds));
        List<com.ruoyi.proj_lwj.domain.LocalQuestionBank> bankQuestions = localBankMapper.selectByIds(distinctIds);
        if (bankQuestions == null || bankQuestions.isEmpty()) throw new ServiceException("题库中未找到对应题目");

        int imported = 0;
        int skipped = 0;
        // 计算当前最大排序值
        ClassExamQuestion probe = new ClassExamQuestion();
        probe.setExamId(examId);
        List<ClassExamQuestion> existing = mapper.selectQuestionList(probe);
        int maxSort = 0;
        for (ClassExamQuestion e : existing) {
            if (e.getSortOrder() != null && e.getSortOrder() > maxSort) maxSort = e.getSortOrder();
        }

        for (com.ruoyi.proj_lwj.domain.LocalQuestionBank bq : bankQuestions) {
            if (bq == null) continue;
            String content = bq.getQuestionContent();
            if (content == null || content.trim().isEmpty()) continue;
            // 去重：同一考试下题目内容不能重复
            if (existsByContent(examId, content)) {
                skipped++; continue;
            }
            ClassExamQuestion newQ = new ClassExamQuestion();
            newQ.setExamId(examId);
            // 题型映射：本地题库 1-单选 2-判断 3-简答; 假设考试题型: 1单选 3判断 5简答（根据注释）
            Integer srcType = bq.getQuestionType();
            int examType;
            if (srcType != null) {
                switch (srcType) {
                    case 1: examType = 1; break; // 单选
                    case 2: examType = 3; break; // 判断
                    case 3: examType = 5; break; // 简答
                    default: examType = 1; // 默认单选
                }
            } else { examType = 1; }
            newQ.setQuestionType(examType);
            newQ.setQuestionContent(content);
            // 选项字段
            newQ.setQuestionOptions(bq.getOptionsJson());
            newQ.setCorrectAnswer(bq.getCorrectAnswer());
            newQ.setAnalysis(bq.getAnalysis());
            // 分值
            BigDecimal score = bq.getScore();
            if (score == null) score = BigDecimal.valueOf(2);
            newQ.setScore(score);
            newQ.setDifficulty(bq.getDifficulty());
            newQ.setSortOrder(++maxSort);
            newQ.setCreateBy(operator == null ? "system" : operator);
            // 插入
            assertNoDuplicateQuestion(newQ, false);
            mapper.insertQuestion(newQ);
            imported++;
        }
        // 刷新考试题目数量
        if (examService != null) {
            try { examService.refreshQuestionCount(examId); } catch (Exception ignore) {}
        }
        BigDecimal totalAfter = calculateTotalScore(examId);
        return new ImportResult(bankIds.size(), imported, skipped, totalAfter);
    }
}
