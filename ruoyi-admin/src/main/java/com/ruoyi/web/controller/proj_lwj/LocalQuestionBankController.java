package com.ruoyi.web.controller.proj_lwj;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.proj_lwj.domain.ClassLocalQuestionBank;
import com.ruoyi.proj_lwj.service.IClassLocalQuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/proj_lwj/local-question")
public class LocalQuestionBankController extends BaseController {

    @Autowired
    private IClassLocalQuestionBankService service;

    @GetMapping("/list")
    public AjaxResult list(@RequestParam(required = false) String keyword,
                           @RequestParam(required = false) Integer questionType,
                           @RequestParam(required = false) Integer difficulty,
                           @RequestParam(required = false) String category,
                           @RequestParam(required = false) Integer status,
                           @RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "50") Integer pageSize) {
        int page = pageNum == null || pageNum < 1 ? 0 : pageNum - 1;
        // status 为空则不加状态过滤
        List<ClassLocalQuestionBank> list = service.search(keyword, questionType, difficulty, category, status, page, pageSize);
        return AjaxResult.success(list);
    }

    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id){
        ClassLocalQuestionBank q = service.get(id);
        return q == null ? AjaxResult.error("题目不存在") : AjaxResult.success(q);
    }

    @PostMapping
    @Log(title="本地题库", businessType = BusinessType.INSERT)
    public AjaxResult add(@RequestBody ClassLocalQuestionBank q){
        if (q.getQuestionContent()==null || q.getQuestionContent().trim().isEmpty()) return AjaxResult.error("题目内容不能为空");
        if (q.getQuestionType()==null) return AjaxResult.error("题型不能为空");
        if (q.getScore()==null) q.setScore(java.math.BigDecimal.valueOf(2));
        if (q.getStatus()==null) q.setStatus(1);
        int rows = service.add(q);
        return toAjax(rows);
    }

    @PutMapping
    @Log(title="本地题库", businessType = BusinessType.UPDATE)
    public AjaxResult edit(@RequestBody ClassLocalQuestionBank q){
        if (q.getId()==null) return AjaxResult.error("缺少ID");
        int rows = service.edit(q);
        return toAjax(rows);
    }

    @DeleteMapping("/{id}")
    @Log(title="本地题库", businessType = BusinessType.DELETE)
    public AjaxResult remove(@PathVariable Long id){
        return toAjax(service.remove(id));
    }

    @GetMapping("/categories")
    public AjaxResult categories(){
        return AjaxResult.success(service.categories());
    }

    @GetMapping("/statistics")
    public AjaxResult statistics(){
        java.util.Map<String,Object> m = new java.util.LinkedHashMap<>();
        m.put("total", service.countAll());
        m.put("enabled", service.countEnabled());
        return AjaxResult.success(m);
    }
}
