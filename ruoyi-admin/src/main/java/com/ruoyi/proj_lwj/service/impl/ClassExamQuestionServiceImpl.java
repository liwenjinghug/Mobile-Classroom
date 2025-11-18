package com.ruoyi.proj_lwj.service.impl;

import com.ruoyi.proj_lwj.domain.ClassExamQuestion;
import com.ruoyi.proj_lwj.mapper.ClassExamQuestionMapper;
import com.ruoyi.proj_lwj.service.IClassExamQuestionService;
import com.ruoyi.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ClassExamQuestionServiceImpl implements IClassExamQuestionService {

    @Autowired
    private ClassExamQuestionMapper mapper;

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
        // Validate options duplication for choice/judge types
        Integer type = q.getQuestionType();
        if (type != null && (type == 1 || type == 2 || type == 3)) {
            String optionsJson = q.getQuestionOptions();
            if (optionsJson != null && !optionsJson.trim().isEmpty()) {
                // Very light JSON parse: assume optionsJson is a JSON array like ["A","B",...]
                // Remove brackets and split by comma while respecting simple quotes
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
}
