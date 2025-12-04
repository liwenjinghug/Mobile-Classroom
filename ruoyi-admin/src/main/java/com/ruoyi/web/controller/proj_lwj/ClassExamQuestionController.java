package com.ruoyi.web.controller.proj_lwj;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
// [修改] 引入自定义的 Log 注解
import com.ruoyi.proj_cyq.annotation.Log;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.proj_lwj.domain.ClassExamQuestion;
import com.ruoyi.proj_lwj.service.IClassExamQuestionService;
import com.ruoyi.proj_lwj.service.IClassExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * 考试题目控制器：题库 API 导入 + 列表 + 发布校验辅助
 */
@RestController
@RequestMapping("/proj_lwj/exam/question")
public class ClassExamQuestionController extends BaseController {

    @Autowired
    private IClassExamQuestionService questionService;
    @Autowired
    private IClassExamService examService;

    /** 题目列表 */
    @GetMapping("/list")
    public AjaxResult list(ClassExamQuestion q){
        List<ClassExamQuestion> list = questionService.selectQuestionList(q);
        return AjaxResult.success(list);
    }

    /** 单题详情 */
    @GetMapping("/{id}")
    public AjaxResult info(@PathVariable Long id){
        ClassExamQuestion q = questionService.selectById(id);
        if (q == null) return AjaxResult.error("题目不存在");
        return AjaxResult.success(q);
    }

    /** 新增题目 */
    @Log(title="考试题目", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ClassExamQuestion q){
        if (q.getExamId()==null) return AjaxResult.error("examId不能为空");
        if (q.getQuestionContent()==null || q.getQuestionContent().trim().isEmpty()) return AjaxResult.error("题目内容不能为空");
        int rows = questionService.insertQuestion(q);
        examService.refreshQuestionCount(q.getExamId());
        return toAjax(rows);
    }

    /** 编辑题目 */
    @Log(title="考试题目", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ClassExamQuestion q){
        if (q.getId()==null) return AjaxResult.error("缺少题目ID");
        int rows = questionService.updateQuestion(q);
        if (q.getExamId()!=null) examService.refreshQuestionCount(q.getExamId());
        return toAjax(rows);
    }

    /** 删除题目 */
    @Log(title="考试题目", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids){
        int rows = questionService.deleteQuestionByIds(ids);
        return toAjax(rows);
    }

    /** 批量删除题目 */
    @Log(title="考试题目", businessType = BusinessType.DELETE)
    @DeleteMapping("/batch")
    public AjaxResult removeBatch(@RequestBody Long[] ids){
        int rows = questionService.deleteQuestionByIds(ids);
        return toAjax(rows);
    }

    /** 计算考试题目总分 */
    @GetMapping("/{examId}/totalScore")
    public AjaxResult total(@PathVariable Long examId){
        return AjaxResult.success(questionService.calculateTotalScore(examId));
    }

    /** 批量重排序号 */
    @Log(title="题目重排", businessType = BusinessType.UPDATE)
    @PostMapping("/reorder")
    public AjaxResult reorder(@RequestBody List<ClassExamQuestion> list){
        int rows = questionService.batchReorder(list);
        return AjaxResult.success("已更新排序数量:" + rows);
    }

    /** 考试题目得分汇总（当前已选题总分、设置总分、是否满足发布条件、还差多少） */
    @GetMapping("/{examId}/score-summary")
    public AjaxResult scoreSummary(@PathVariable Long examId){
        if (examId == null) return AjaxResult.error("examId不能为空");
        BigDecimal actual = questionService.calculateTotalScore(examId);
        com.ruoyi.proj_lwj.domain.ClassExam exam = examService.selectExamById(examId);
        if (exam == null) return AjaxResult.error("考试不存在");
        BigDecimal configured = exam.getTotalScore() == null ? BigDecimal.ZERO : exam.getTotalScore();
        BigDecimal remaining = configured.subtract(actual);
        Map<String,Object> data = new LinkedHashMap<>();
        data.put("examId", examId);
        data.put("actualTotalScore", actual);
        data.put("configuredTotalScore", configured);
        data.put("remainingToPublish", remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO);
        data.put("canPublish", remaining.compareTo(BigDecimal.ZERO) <= 0);
        data.put("questionCount", exam.getQuestionCount());
        return AjaxResult.success(data);
    }

    /** 从本地题库导入题目 */
    @Log(title="题库导入", businessType = BusinessType.IMPORT)
    @PostMapping("/import-local")
    public AjaxResult importLocal(@RequestBody Map<String,Object> payload){
        Object examIdObj = payload.get("examId");
        Object idsObj = payload.get("bankIds");
        if (examIdObj == null) return AjaxResult.error("examId不能为空");
        Long examId;
        try { examId = Long.valueOf(String.valueOf(examIdObj)); } catch (Exception e){ return AjaxResult.error("examId格式不正确"); }
        if (!(idsObj instanceof Collection)) return AjaxResult.error("bankIds必须为数组");
        List<Long> bankIds = new ArrayList<>();
        for (Object o : (Collection<?>) idsObj){
            if (o == null) continue;
            try { bankIds.add(Long.valueOf(String.valueOf(o))); } catch (Exception ignore) {}
        }
        IClassExamQuestionService.ImportResult result = questionService.importFromLocalBank(examId, bankIds, getUsername());
        Map<String,Object> data = new LinkedHashMap<>();
        data.put("requested", result.requested);
        data.put("imported", result.imported);
        data.put("skippedDuplicate", result.skippedDuplicate);
        data.put("totalScoreAfter", result.totalScoreAfter);
        return AjaxResult.success(data);
    }
}