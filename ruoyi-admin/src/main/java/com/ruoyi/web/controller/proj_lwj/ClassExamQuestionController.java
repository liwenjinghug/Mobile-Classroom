package com.ruoyi.web.controller.proj_lwj;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.proj_lwj.domain.ClassExamQuestion;
import com.ruoyi.proj_lwj.service.IClassExamQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

// JSON parsing for options validation
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@RestController
@RequestMapping("/proj_lwj/exam-question")
public class ClassExamQuestionController extends BaseController {

    @Autowired
    private IClassExamQuestionService service;

    private static final ObjectMapper OM = new ObjectMapper();

    @PreAuthorize("@ss.hasPermi('projlwj:examq:list')")
    @GetMapping("/list")
    public TableDataInfo list(ClassExamQuestion q) {
        startPage();
        return getDataTable(service.list(q));
    }

    @PreAuthorize("@ss.hasPermi('projlwj:examq:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return AjaxResult.success(service.get(id));
    }

    @PreAuthorize("@ss.hasPermi('projlwj:examq:add')")
    @Log(title = "考试题目", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ClassExamQuestion q) {
        String err = validate(q, true);
        if (err != null) return AjaxResult.error(err);
        q.setCreateBy(getUsername());
        return toAjax(service.insert(q));
    }

    @PreAuthorize("@ss.hasPermi('projlwj:examq:edit')")
    @Log(title = "考试题目", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ClassExamQuestion q) {
        String err = validate(q, false);
        if (err != null) return AjaxResult.error(err);
        q.setUpdateBy(getUsername());
        return toAjax(service.update(q));
    }

    @PreAuthorize("@ss.hasPermi('projlwj:examq:remove')")
    @Log(title = "考试题目", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        return toAjax(service.delete(id));
    }

    @PreAuthorize("@ss.hasPermi('projlwj:examq:remove')")
    @Log(title = "考试题目", businessType = BusinessType.DELETE)
    @DeleteMapping("/batch")
    public AjaxResult removeBatch(@RequestBody Long[] ids) {
        return toAjax(service.deleteBatch(ids));
    }

    @PreAuthorize("@ss.hasPermi('projlwj:examq:edit')")
    @Log(title = "考试题目排序", businessType = BusinessType.UPDATE)
    @PutMapping("/reorder")
    public AjaxResult reorder(@RequestBody List<ClassExamQuestion> items) {
        if (items == null || items.isEmpty()) return AjaxResult.error("排序项不能为空");
        for (ClassExamQuestion e : items) {
            if (e.getId() == null || e.getSortOrder() == null) return AjaxResult.error("排序项缺少 id 或 sortOrder");
            e.setUpdateBy(getUsername());
        }
        return toAjax(service.batchReorder(items));
    }

    private String validate(ClassExamQuestion q, boolean isCreate) {
        if (q == null) return "参数不能为空";
        if (isCreate && (q.getExamId() == null)) return "缺少考试ID";
        if (q.getQuestionType() == null) return "请选择题型";
        int t = q.getQuestionType();
        // 仅支持：1单选、3判断、5简答
        if (t != 1 && t != 3 && t != 5) return "题型仅支持：单选、判断、简答";
        if (q.getQuestionContent() == null || q.getQuestionContent().trim().isEmpty()) return "请输入题目内容";
        if (q.getScore() == null || q.getScore().compareTo(new BigDecimal("0")) <= 0) return "分值需大于0";

        // 单选题：需要 options 至少2项、正确答案必须在 options 中
        if (t == 1) {
            if (isBlank(q.getQuestionOptions())) return "单选题需提供选项（JSON数组）";
            if (isBlank(q.getCorrectAnswer())) return "单选题需设置正确答案";
            try {
                List<String> opts = OM.readValue(q.getQuestionOptions().trim(), new TypeReference<List<String>>(){});
                if (opts == null || opts.size() < 2) return "单选题选项至少2个";
                String ans = q.getCorrectAnswer().trim();
                boolean in = false;
                for (String s : opts) { if (s != null && s.equals(ans)) { in = true; break; } }
                if (!in) return "正确答案必须在选项中";
            } catch (Exception e) {
                return "选项需为JSON数组，如：[\"A\",\"B\"]";
            }
        }
        // 判断题：答案需为 true/false 或 1/0
        if (t == 3) {
            if (isBlank(q.getCorrectAnswer())) return "判断题需设置正确答案";
            String v = q.getCorrectAnswer().trim().toLowerCase();
            if (!("true".equals(v) || "false".equals(v) || "1".equals(v) || "0".equals(v))) {
                return "判断题答案仅支持 true/false 或 1/0";
            }
        }
        // 简答题：允许无参考答案（通常走人工批改）
        // 学科分类可选：长度限制
        if (q.getSubject() != null && q.getSubject().length() > 100) return "学科分类过长";
        return null;
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
