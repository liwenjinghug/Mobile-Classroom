package com.ruoyi.proj_lwj.service;

import com.ruoyi.proj_lwj.domain.ClassExamQuestion;
import java.util.List;
import java.math.BigDecimal;

public interface IClassExamQuestionService {
    List<ClassExamQuestion> selectQuestionList(ClassExamQuestion q);
    ClassExamQuestion selectById(Long id);
    int insertQuestion(ClassExamQuestion q);
    int updateQuestion(ClassExamQuestion q);
    int deleteQuestionById(Long id);
    int deleteQuestionByIds(Long[] ids);
    int countByExamId(Long examId);
    default List<ClassExamQuestion> selectList(ClassExamQuestion q){ return selectQuestionList(q); }
    default List<ClassExamQuestion> list(ClassExamQuestion q){ return selectQuestionList(q); }
    default ClassExamQuestion get(Long id){ return selectById(id); }
    default int insert(ClassExamQuestion q){ return insertQuestion(q); }
    default int update(ClassExamQuestion q){ return updateQuestion(q); }
    default int delete(Long id){ return deleteQuestionById(id); }
    default int deleteBatch(Long[] ids){ return deleteQuestionByIds(ids); }
    int batchReorder(List<ClassExamQuestion> items);

    /**
     * 计算考试总分
     * @param examId 考试ID
     * @return 总分
     */
    BigDecimal calculateTotalScore(Long examId);

    // 题目重复校验
    boolean existsByContent(Long examId, String questionContent);

    /**
     * 从本地题库批量导入题目到指定考试
     * @param examId 考试ID
     * @param bankIds 题库题目ID集合
     * @param operator 操作人
     * @return 导入结果
     */
    ImportResult importFromLocalBank(Long examId, List<Long> bankIds, String operator);

    class ImportResult {
        public int requested; // 请求导入数量
        public int imported;  // 实际成功导入数量
        public int skippedDuplicate; // 因重复而跳过
        public BigDecimal totalScoreAfter; // 导入后考试题目总分
        public ImportResult(int requested, int imported, int skippedDuplicate, BigDecimal totalScoreAfter){
            this.requested = requested; this.imported = imported; this.skippedDuplicate = skippedDuplicate; this.totalScoreAfter = totalScoreAfter;
        }
    }
}
