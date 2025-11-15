package com.ruoyi.web.controller.proj_lwj;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.proj_lwj.domain.*;
import com.ruoyi.proj_lwj.mapper.ClassStudentMapper;
import com.ruoyi.proj_lwj.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/proj_lwj/exam")
public class ClassExamController extends BaseController {
    @Autowired private IClassExamService examService;
    @Autowired private IClassExamQuestionService questionService;
    @Autowired private IClassExamParticipantService participantService;
    @Autowired private IClassExamAnswerService answerService;
    @Autowired private IClassExamMonitorService monitorService;
    @Autowired private IClassExamSessionService examSessionService;
    @Autowired private ClassStudentMapper classStudentMapper;

    // ===== 考试管理 =====
    @PreAuthorize("@ss.hasPermi('projlwj:exam:list')")
    @GetMapping("/list")
    public TableDataInfo list(ClassExam exam) {
        startPage();
        List<ClassExam> list = examService.selectExamList(exam);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('projlwj:exam:query')")
    @GetMapping("/{id}")
    public AjaxResult info(@PathVariable Long id) {
        ClassExam ex = examService.selectExamById(id);
        if (ex == null) return AjaxResult.error(HttpStatus.NOT_FOUND, "考试不存在");
        return AjaxResult.success(ex);
    }

    @PreAuthorize("@ss.hasPermi('projlwj:exam:add')")
    @Log(title = "考试管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ClassExam exam) {
        exam.setCreateBy(getUsername());
        if (exam.getStatus() == null) exam.setStatus(0); // 默认草稿
        int r = examService.insertExam(exam);
        return toAjax(r);
    }

    @PreAuthorize("@ss.hasPermi('projlwj:exam:edit')")
    @Log(title = "考试管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ClassExam exam) {
        exam.setUpdateBy(getUsername());
        int r = examService.updateExam(exam);
        return toAjax(r);
    }

    @PreAuthorize("@ss.hasPermi('projlwj:exam:remove')")
    @Log(title = "考试管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        int r = examService.deleteExamByIds(ids);
        return toAjax(r);
    }

    // 状态流转
    @PreAuthorize("@ss.hasPermi('projlwj:exam:status')")
    @Log(title = "考试状态更新", businessType = BusinessType.UPDATE)
    @PutMapping("/status/{id}/{status}")
    public AjaxResult changeStatus(@PathVariable Long id, @PathVariable Integer status) {
        ClassExam ex = examService.selectExamById(id);
        if (ex == null) return AjaxResult.error("考试不存在");
        ex.setStatus(status);
        ex.setUpdateBy(getUsername());
        examService.updateExam(ex);
        return AjaxResult.success();
    }

    // ===== 多课堂发布支持 =====
    @PreAuthorize("@ss.hasPermi('projlwj:exam:publishSessions')")
    @Log(title = "考试多课堂发布", businessType = BusinessType.UPDATE)
    @PutMapping("/{examId}/sessions")
    public AjaxResult publishToSessions(@PathVariable Long examId, @RequestBody List<Long> sessionIds) {
        ClassExam ex = examService.selectExamById(examId);
        if (ex == null) return AjaxResult.error("考试不存在");
        int c = examSessionService.replaceExamSessions(examId, sessionIds);
        return AjaxResult.success("已关联课堂数量:" + c);
    }
    @GetMapping("/{examId}/sessions")
    public AjaxResult examSessions(@PathVariable Long examId) {
        return AjaxResult.success(examSessionService.selectByExamId(examId));
    }

    // ===== 题目管理 =====
    @PreAuthorize("@ss.hasPermi('projlwj:examQuestion:list')")
    @GetMapping("/question/list")
    public AjaxResult questionList(ClassExamQuestion q) {
        List<ClassExamQuestion> list = questionService.selectQuestionList(q);
        return AjaxResult.success(list);
    }

    @PreAuthorize("@ss.hasPermi('projlwj:examQuestion:add')")
    @Log(title = "题目管理", businessType = BusinessType.INSERT)
    @PostMapping("/question")
    public AjaxResult addQuestion(@RequestBody ClassExamQuestion q) {
        q.setCreateBy(getUsername());
        int r = questionService.insertQuestion(q);
        examService.refreshQuestionCount(q.getExamId());
        return toAjax(r);
    }

    @PreAuthorize("@ss.hasPermi('projlwj:examQuestion:edit')")
    @Log(title = "题目管理", businessType = BusinessType.UPDATE)
    @PutMapping("/question")
    public AjaxResult editQuestion(@RequestBody ClassExamQuestion q) {
        q.setUpdateBy(getUsername());
        int r = questionService.updateQuestion(q);
        return toAjax(r);
    }

    @PreAuthorize("@ss.hasPermi('projlwj:examQuestion:remove')")
    @Log(title = "题目管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/question/{ids}")
    public AjaxResult removeQuestion(@PathVariable Long[] ids) {
        int r = questionService.deleteQuestionByIds(ids);
        return toAjax(r);
    }

    // ===== 考试参与 =====
    @PreAuthorize("@ss.hasPermi('projlwj:examParticipant:list')")
    @GetMapping("/participant/list")
    public AjaxResult participantList(ClassExamParticipant p) {
        List<ClassExamParticipant> list = participantService.selectList(p);
        return AjaxResult.success(list);
    }

    @Log(title = "考试参与", businessType = BusinessType.INSERT)
    @PostMapping("/participant/start")
    public AjaxResult startExam(@RequestBody ClassExamParticipant payload) {
        if (payload.getExamId() == null) return AjaxResult.error("examId不能为空");
        Long studentId = payload.getStudentId();
        if ((studentId == null || payload.getStudentNo() == null) && payload.getStudentNo() != null) {
            ClassStudent s = classStudentMapper.selectByStudentNo(payload.getStudentNo());
            if (s != null) { studentId = s.getStudentId(); if (payload.getStudentName() == null) payload.setStudentName(s.getStudentName()); }
        }
        if (studentId == null) studentId = getUserId();
        // 反查补齐 studentNo/studentName，避免 DB 约束失败
        if (payload.getStudentNo() == null) {
            ClassStudent s2 = classStudentMapper.selectByStudentId(studentId);
            if (s2 != null) { payload.setStudentNo(s2.getStudentNo()); if (payload.getStudentName() == null) payload.setStudentName(s2.getStudentName()); }
        }
        ClassExamParticipant existing = participantService.selectByExamStudent(payload.getExamId(), studentId);
        Date now = new Date();
        if (existing == null) {
            payload.setStudentId(studentId);
            payload.setParticipantStatus(1); // 进行中
            payload.setStartTime(now);
            payload.setCreateBy(getUsername());
            participantService.insert(payload);
            return AjaxResult.success(payload);
        } else {
            if (existing.getParticipantStatus() == 0) {
                existing.setParticipantStatus(1);
                existing.setStartTime(now);
                participantService.update(existing);
            }
            return AjaxResult.success(existing);
        }
    }

    @Log(title = "考试提交", businessType = BusinessType.UPDATE)
    @PostMapping("/participant/submit")
    public AjaxResult submitExam(@RequestBody ClassExamParticipant payload) {
        if (payload.getExamId() == null) return AjaxResult.error("examId不能为空");
        Long studentId = payload.getStudentId();
        if ((studentId == null || payload.getStudentNo() == null) && payload.getStudentNo() != null) {
            ClassStudent s = classStudentMapper.selectByStudentNo(payload.getStudentNo());
            if (s != null) studentId = s.getStudentId();
        }
        if (studentId == null) studentId = getUserId();
        if (payload.getStudentNo() == null) {
            ClassStudent s2 = classStudentMapper.selectByStudentId(studentId);
            if (s2 != null) { payload.setStudentNo(s2.getStudentNo()); if (payload.getStudentName() == null) payload.setStudentName(s2.getStudentName()); }
        }
        ClassExamParticipant existing = participantService.selectByExamStudent(payload.getExamId(), studentId);
        if (existing == null) return AjaxResult.error(HttpStatus.NOT_FOUND, "未开始考试或记录不存在");
        if (existing.getParticipantStatus() == 2) return AjaxResult.error("已提交，不能重复提交");
        existing.setParticipantStatus(2);
        existing.setSubmitTime(new Date());
        if (existing.getStartTime() != null) {
            existing.setTimeUsed((int) ((existing.getSubmitTime().getTime() - existing.getStartTime().getTime()) / 1000));
        }
        participantService.update(existing);
        return AjaxResult.success(existing);
    }

    // 学生考试入口：按学号列出可参加考试
    @GetMapping("/available")
    public AjaxResult available(@RequestParam String studentNo) {
        if (studentNo == null || studentNo.trim().isEmpty()) return AjaxResult.error("缺少学号");
        return AjaxResult.success(examService.selectAvailableByStudentNo(studentNo.trim()));
    }

    // 兼容前端 API：开始考试/提交试卷（路径无需 /participant 前缀）
    @PostMapping("/start")
    public AjaxResult start(@RequestBody(required = false) String raw,
                            @RequestParam(value = "examId", required = false) Long examId,
                            @RequestParam(value = "studentNo", required = false) String studentNo) {
        try {
            if (examId == null && raw != null) {
                String t = raw.trim();
                if (!t.isEmpty()) {
                    if (t.startsWith("{")) {
                        ObjectMapper om = new ObjectMapper();
                        JsonNode node = om.readTree(t);
                        if (node.has("examId")) examId = node.get("examId").asLong();
                        if (studentNo == null && node.has("studentNo")) studentNo = node.get("studentNo").asText(null);
                    } else {
                        // 兼容纯数字或带引号的数字
                        t = t.replace("\"", "");
                        examId = Long.valueOf(t);
                    }
                }
            }
        } catch (Exception e) {
            return AjaxResult.error("请求体解析失败: " + e.getMessage());
        }
        ClassExamParticipant p = new ClassExamParticipant();
        p.setExamId(examId);
        p.setStudentNo(studentNo);
        return startExam(p);
    }
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody(required = false) String raw,
                             @RequestParam(value = "examId", required = false) Long examId,
                             @RequestParam(value = "studentNo", required = false) String studentNo) {
        try {
            if (examId == null && raw != null) {
                String t = raw.trim();
                if (!t.isEmpty()) {
                    if (t.startsWith("{")) {
                        ObjectMapper om = new ObjectMapper();
                        JsonNode node = om.readTree(t);
                        if (node.has("examId")) examId = node.get("examId").asLong();
                        if (studentNo == null && node.has("studentNo")) studentNo = node.get("studentNo").asText(null);
                    } else {
                        t = t.replace("\"", "");
                        examId = Long.valueOf(t);
                    }
                }
            }
        } catch (Exception e) {
            return AjaxResult.error("请求体解析失败: " + e.getMessage());
        }
        ClassExamParticipant p = new ClassExamParticipant();
        p.setExamId(examId);
        p.setStudentNo(studentNo);
        return submitExam(p);
    }

    // ===== 答案记录 =====
    @PreAuthorize("@ss.hasPermi('projlwj:examAnswer:list')")
    @GetMapping("/answer/list")
    public AjaxResult answerList(ClassExamAnswer a) {
        List<ClassExamAnswer> list = answerService.selectList(a);
        return AjaxResult.success(list);
    }

    // 自动判分简易逻辑
    private void autoJudgeObjective(ClassExamAnswer a) {
        if (a.getCorrectAnswer() != null && a.getStudentAnswer() != null) {
            String std = a.getCorrectAnswer().trim();
            String stu = a.getStudentAnswer().trim();
            if (std.equalsIgnoreCase(stu)) {
                a.setIsCorrect(1);
                if (a.getScore() == null) {
                    a.setScore(new BigDecimal("1"));
                }
            } else {
                a.setIsCorrect(0);
                if (a.getScore() == null) a.setScore(BigDecimal.ZERO);
            }
        }
    }

    // 刷新参与者分数
    private void refreshParticipantScores(Long examId, Long studentId) {
        ClassExamParticipant part = participantService.selectByExamStudent(examId, studentId);
        if (part == null) return;
        ClassExamAnswer filter = new ClassExamAnswer();
        filter.setExamId(examId);
        filter.setStudentId(studentId);
        List<ClassExamAnswer> answers = answerService.selectList(filter);
        if (answers == null || answers.isEmpty()) return;
        BigDecimal obj = BigDecimal.ZERO; BigDecimal subj = BigDecimal.ZERO;
        for (ClassExamAnswer ans : answers) {
            BigDecimal s = ans.getScore() == null ? BigDecimal.ZERO : ans.getScore();
            if (ans.getIsCorrect() != null) obj = obj.add(s); else subj = subj.add(s);
        }
        part.setObjectiveScore(obj);
        part.setSubjectiveScore(subj);
        part.setTotalScore(obj.add(subj));
        ClassExam ex = examService.selectExamById(examId);
        if (ex != null && ex.getPassScore() != null) {
            part.setPassStatus(part.getTotalScore().compareTo(ex.getPassScore()) >= 0 ? 1 : 0);
        }
        participantService.update(part);
    }

    // 保存/更新答案并刷新分数
    @Log(title = "保存答案", businessType = BusinessType.INSERT)
    @PostMapping("/answer/save")
    public AjaxResult saveAnswer(@RequestBody ClassExamAnswer payload) {
        if (payload.getExamId() == null || payload.getQuestionId() == null) return AjaxResult.error("examId/questionId不能为空");
        Long studentId = payload.getStudentId() != null ? payload.getStudentId() : getUserId();
        // 补齐 studentNo
        if (payload.getStudentNo() == null) {
            ClassStudent stu = classStudentMapper.selectByStudentId(studentId);
            if (stu != null) payload.setStudentNo(stu.getStudentNo());
        }
        payload.setStudentId(studentId);
        // 快照字段填充（仅在不存在时）
        if (payload.getQuestionContent() == null || payload.getQuestionOptions() == null || payload.getCorrectAnswer() == null) {
            ClassExamQuestion q = questionService.selectById(payload.getQuestionId());
            if (q != null) {
                if (payload.getQuestionContent() == null) payload.setQuestionContent(q.getQuestionContent());
                if (payload.getQuestionOptions() == null) payload.setQuestionOptions(q.getQuestionOptions());
                if (payload.getCorrectAnswer() == null) payload.setCorrectAnswer(q.getCorrectAnswer());
            }
        }
        ClassExamAnswer existing = answerService.selectByExamStudentQuestion(payload.getExamId(), studentId, payload.getQuestionId());
        autoJudgeObjective(payload);
        if (existing == null) {
            payload.setCreateBy(getUsername());
            answerService.insert(payload);
        } else {
            existing.setStudentAnswer(payload.getStudentAnswer());
            existing.setAnswerFiles(payload.getAnswerFiles());
            existing.setIsCorrect(payload.getIsCorrect());
            existing.setScore(payload.getScore());
            existing.setAnswerDuration(payload.getAnswerDuration());
            existing.setUpdateBy(getUsername());
            answerService.update(existing);
            payload = existing;
        }
        refreshParticipantScores(payload.getExamId(), studentId);
        return AjaxResult.success(payload);
    }

    // 主观题批改
    @PreAuthorize("@ss.hasPermi('projlwj:examAnswer:grade')")
    @Log(title = "批改答案", businessType = BusinessType.UPDATE)
    @PostMapping("/answer/grade")
    public AjaxResult gradeAnswer(@RequestBody ClassExamAnswer payload) {
        if (payload.getId() == null) return AjaxResult.error("答案ID不能为空");
        ClassExamAnswer exist = answerService.selectById(payload.getId());
        if (exist == null) return AjaxResult.error(HttpStatus.NOT_FOUND, "答案不存在");
        exist.setScore(payload.getScore());
        exist.setCorrectComment(payload.getCorrectComment());
        exist.setCorrectorId(getUserId());
        exist.setCorrectTime(new Date());
        exist.setUpdateBy(getUsername());
        answerService.update(exist);
        refreshParticipantScores(exist.getExamId(), exist.getStudentId());
        return AjaxResult.success(exist);
    }

    // ===== 监控 =====
    @PreAuthorize("@ss.hasPermi('projlwj:examMonitor:list')")
    @GetMapping("/monitor/list")
    public AjaxResult monitorList(ClassExamMonitor m) {
        List<ClassExamMonitor> list = monitorService.selectList(m);
        return AjaxResult.success(list);
    }

    @Log(title = "监控事件", businessType = BusinessType.INSERT)
    @PostMapping("/monitor/event")
    public AjaxResult addEvent(@RequestBody ClassExamMonitor m) {
        if (m.getExamId() == null) return AjaxResult.error("examId不能为空");
        if (m.getEventType() == null) return AjaxResult.error("eventType不能为空");
        if (m.getEventTime() == null) m.setEventTime(new Date());
        m.setCreateBy(getUsername());
        monitorService.insert(m);
        return AjaxResult.success();
    }

    @PreAuthorize("@ss.hasPermi('projlwj:examMonitor:handle')")
    @Log(title = "处理监控事件", businessType = BusinessType.UPDATE)
    @PutMapping("/monitor/handle/{id}")
    public AjaxResult handleEvent(@PathVariable Long id) {
        ClassExamMonitor m = new ClassExamMonitor();
        m.setId(id);
        m.setHandled(1);
        monitorService.updateHandled(m);
        return AjaxResult.success();
    }

    // ===== 统计与强制交卷 =====
    @GetMapping("/{examId}/progress")
    public AjaxResult progress(@PathVariable Long examId) {
        ClassExamParticipant filter = new ClassExamParticipant();
        filter.setExamId(examId);
        List<ClassExamParticipant> list = participantService.selectList(filter);
        int total = list.size(); int started=0; int finished=0; int passed=0; int graded=0;
        for (ClassExamParticipant p : list) {
            Integer st = p.getParticipantStatus();
            if (st != null && st >= 1) started++;
            if (st != null && st == 2) finished++;
            if (p.getPassStatus() != null && p.getPassStatus() == 1) passed++;
            if (p.getCorrectStatus() != null && p.getCorrectStatus() == 1) graded++;
        }
        Map<String,Object> data = new HashMap<>();
        data.put("total", total); data.put("started", started); data.put("finished", finished);
        data.put("graded", graded); data.put("passed", passed);
        data.put("finishRate", total==0?0:(finished*100.0/total));
        return AjaxResult.success(data);
    }

    @GetMapping("/{examId}/ranking")
    public AjaxResult ranking(@PathVariable Long examId) {
        ClassExamParticipant filter = new ClassExamParticipant();
        filter.setExamId(examId);
        List<ClassExamParticipant> list = participantService.selectList(filter);
        list.sort((a,b)->{
            BigDecimal ta = a.getTotalScore()==null?BigDecimal.ZERO:a.getTotalScore();
            BigDecimal tb = b.getTotalScore()==null?BigDecimal.ZERO:b.getTotalScore();
            int cmp = tb.compareTo(ta);
            if (cmp!=0) return cmp;
            return Integer.compare(a.getTimeUsed()==null?Integer.MAX_VALUE:a.getTimeUsed(), b.getTimeUsed()==null?Integer.MAX_VALUE:b.getTimeUsed());
        });
        return AjaxResult.success(list);
    }

    @PreAuthorize("@ss.hasPermi('projlwj:exam:forceSubmit')")
    @Log(title="强制交卷", businessType = BusinessType.UPDATE)
    @PutMapping("/participant/force/{examId}/{studentId}")
    public AjaxResult forceSubmit(@PathVariable Long examId, @PathVariable Long studentId) {
        ClassExamParticipant existing = participantService.selectByExamStudent(examId, studentId);
        if (existing == null) return AjaxResult.error("记录不存在");
        if (existing.getParticipantStatus() == null || existing.getParticipantStatus() != 2) {
            existing.setParticipantStatus(2);
            existing.setSubmitTime(new Date());
            if (existing.getStartTime() != null) {
                existing.setTimeUsed((int)((existing.getSubmitTime().getTime()-existing.getStartTime().getTime())/1000));
            }
            participantService.update(existing);
        }
        return AjaxResult.success(existing);
    }
}
