package com.ruoyi.web.controller.proj_lwj;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.proj_lwj.domain.ClassExam;
import com.ruoyi.proj_lwj.service.IClassExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/proj_lwj/exam")
public class ClassExamController extends BaseController {

    @Autowired
    private IClassExamService examService;

    // 列表查询
    @PreAuthorize("@ss.hasPermi('projlwj:exam:list')")
    @GetMapping("/list")
    public TableDataInfo list(ClassExam query) {
        startPage();
        List<ClassExam> list = examService.selectExamList(query);
        return getDataTable(list);
    }

    // 详情
    @PreAuthorize("@ss.hasPermi('projlwj:exam:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return AjaxResult.success(examService.selectExamById(id));
    }

    private String validate(ClassExam e) {
        if (e == null) return "参数不能为空";
        if (e.getCourseId() == null) return "请选择课程";
        if (e.getSessionId() == null) return "请选择课堂";
        if (e.getExamName() == null || e.getExamName().trim().isEmpty()) return "请输入考试名称";
        if (e.getExamType() == null) return "请选择考试类型";
        if (e.getExamDuration() == null || e.getExamDuration() <= 0) return "考试时长需大于0";
        if (e.getStartTime() == null || e.getEndTime() == null) return "请选择考试起止时间";
        if (e.getStartTime().after(e.getEndTime())) return "开始时间不能晚于结束时间";
        BigDecimal total = e.getTotalScore() == null ? BigDecimal.ZERO : e.getTotalScore();
        BigDecimal pass = e.getPassScore() == null ? BigDecimal.ZERO : e.getPassScore();
        if (total.compareTo(BigDecimal.ZERO) <= 0) return "总分需大于0";
        if (pass.compareTo(BigDecimal.ZERO) < 0) return "及格分不能小于0";
        if (pass.compareTo(total) > 0) return "及格分不能大于总分";
        return null;
    }

    // 新增（创建考试：草稿状态）
    @PreAuthorize("@ss.hasPermi('projlwj:exam:add')")
    @Log(title = "考试管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ClassExam exam) {
        if (exam.getStatus() == null) exam.setStatus(0); // 默认草稿
        String err = validate(exam);
        if (err != null) return AjaxResult.error(err);
        exam.setCreateBy(getUsername());
        int rows = examService.insertExam(exam);
        return toAjax(rows);
    }

    // 修改
    @PreAuthorize("@ss.hasPermi('projlwj:exam:edit')")
    @Log(title = "考试管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ClassExam exam) {
        if (exam.getId() == null) return AjaxResult.error("缺少考试ID");
        String err = validate(exam);
        if (err != null) return AjaxResult.error(err);
        exam.setUpdateBy(getUsername());
        int rows = examService.updateExam(exam);
        return toAjax(rows);
    }

    // 删除
    @PreAuthorize("@ss.hasPermi('projlwj:exam:remove')")
    @Log(title = "考试管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(examService.deleteExamByIds(ids));
    }

    // 发布考试：状态草稿->已发布
    @PreAuthorize("@ss.hasPermi('projlwj:exam:publish')")
    @Log(title = "考试发布", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/publish")
    public AjaxResult publish(@PathVariable Long id) {
        ClassExam exist = examService.selectExamById(id);
        if (exist == null) return AjaxResult.error("考试不存在");
        Integer st = exist.getStatus() == null ? 0 : exist.getStatus();
        if (st != 0) return AjaxResult.error("仅草稿状态可发布");
        int rows = examService.updateExamStatus(id, 1, getUsername());
        return toAjax(rows);
    }

    // 开始考试：状态已发布->进行中
    @PreAuthorize("@ss.hasPermi('projlwj:exam:start')")
    @Log(title = "考试开始", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/start")
    public AjaxResult start(@PathVariable Long id) {
        ClassExam exist = examService.selectExamById(id);
        if (exist == null) return AjaxResult.error("考试不存在");
        Integer st = exist.getStatus() == null ? 0 : exist.getStatus();
        if (st != 1) return AjaxResult.error("仅已发布的考试可开始");
        // 可选：校验当前时间与设置时间关系
        int rows = examService.updateExamStatus(id, 2, getUsername());
        return toAjax(rows);
    }

    // 结束考试：状态进行中->已结束
    @PreAuthorize("@ss.hasPermi('projlwj:exam:end')")
    @Log(title = "考试结束", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/end")
    public AjaxResult end(@PathVariable Long id) {
        ClassExam exist = examService.selectExamById(id);
        if (exist == null) return AjaxResult.error("考试不存在");
        Integer st = exist.getStatus() == null ? 0 : exist.getStatus();
        if (st != 2) return AjaxResult.error("仅进行中的考试可结束");
        int rows = examService.updateExamStatus(id, 3, getUsername());
        return toAjax(rows);
    }

    // 批量创建考试：按同一课程配置，一次性发布到多个课堂
    @PreAuthorize("@ss.hasPermi('projlwj:exam:add')")
    @Log(title = "考试管理-批量创建", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    public AjaxResult batchAdd(@RequestBody BatchExamReq req) {
        if (req == null || req.exam == null) return AjaxResult.error("参数不能为空");
        if (req.sessionIds == null || req.sessionIds.isEmpty()) return AjaxResult.error("请选择至少一个课堂");
        ClassExam tpl = req.exam;
        if (tpl.getCourseId() == null) return AjaxResult.error("请选择课程");
        int success = 0;
        for (Long sid : req.sessionIds) {
            if (sid == null) continue;
            try {
                ClassExam e = new ClassExam();
                // 复制模板字段
                e.setCourseId(tpl.getCourseId());
                e.setSessionId(sid);
                e.setExamName(tpl.getExamName());
                e.setExamType(tpl.getExamType());
                e.setTotalScore(tpl.getTotalScore());
                e.setPassScore(tpl.getPassScore());
                e.setExamDuration(tpl.getExamDuration());
                e.setStartTime(tpl.getStartTime());
                e.setEndTime(tpl.getEndTime());
                e.setExamMode(tpl.getExamMode());
                e.setAntiCheat(tpl.getAntiCheat());
                e.setQuestionOrder(tpl.getQuestionOrder());
                e.setShowAnswer(tpl.getShowAnswer());
                e.setRemark(tpl.getRemark());
                // 状态：沿用模板的 status（允许前端设置 0 草稿 / 1 已发布）
                e.setStatus(tpl.getStatus());
                String err = validate(e);
                if (err != null) return AjaxResult.error(err);
                e.setCreateBy(getUsername());
                int r = examService.insertExam(e);
                if (r > 0) success++;
            } catch (Exception ex) {
                logger.warn("batchAdd: 创建课堂 {} 考试失败", sid, ex);
            }
        }
        if (success == 0) return AjaxResult.error("批量创建失败");
        return AjaxResult.success("批量创建成功：" + success + " 个");
    }

    // 请求体 DTO：包含考试模板和多个课堂ID
    public static class BatchExamReq {
        public ClassExam exam;
        public java.util.List<Long> sessionIds;
    }
}
