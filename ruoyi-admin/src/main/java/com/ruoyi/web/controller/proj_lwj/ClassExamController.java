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
        if (ex == null) {
            // 返回 200，避免前端弹出“访问资源不存在”
            return AjaxResult.success("考试不存在或已删除", null).put("missing", true).put("id", id);
        }
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
        // 级联删除：先删答案与参与记录，再删考试
        for (Long examId : ids) {
            try { answerService.deleteByExamId(examId); } catch (Exception ignore) {}
            try { participantService.deleteByExamId(examId); } catch (Exception ignore) {}
        }
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

    // 兼容前端误用：支持以 JSON 传 id/status
    @PreAuthorize("@ss.hasPermi('projlwj:exam:status')")
    @Log(title = "考试状态更新", businessType = BusinessType.UPDATE)
    @PutMapping("/status")
    public AjaxResult changeStatusBody(@RequestBody Map<String, Object> body) {
        if (body == null) return AjaxResult.error("请求体为空");
        Long id = null; Integer status = null;
        Object oid = body.get("id"); Object ostatus = body.get("status");
        if (oid instanceof Number) id = ((Number) oid).longValue();
        else if (oid instanceof String) { try { id = Long.valueOf(((String) oid).trim()); } catch (Exception ignore) {} }
        if (ostatus instanceof Number) status = ((Number) ostatus).intValue();
        else if (ostatus instanceof String) { try { status = Integer.valueOf(((String) ostatus).trim()); } catch (Exception ignore) {} }
        if (id == null || status == null) return AjaxResult.error("id/status 不能为空");
        return changeStatus(id, status);
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
    @GetMapping("/participant/list")
    public AjaxResult participantList(ClassExamParticipant p) {
        try {
            // 当未传学号时，不返回任何记录，保持前端页面空列表但接口成功
            if (p == null || p.getStudentNo() == null || p.getStudentNo().trim().isEmpty()) {
                return AjaxResult.success(java.util.Collections.emptyList());
            }
            List<ClassExamParticipant> list = participantService.selectList(p);
            List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
            for (ClassExamParticipant participant : list) {
                // 过滤已被删除考试的记录（额外兜底，SQL 已做 EXISTS 过滤）
                ClassExam examRef = participant.getExamId() != null ? examService.selectById(participant.getExamId()) : null;
                if (examRef == null) continue;

                java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
                map.put("id", participant.getId());
                map.put("examId", participant.getExamId());
                map.put("studentId", participant.getStudentId());
                map.put("studentNo", participant.getStudentNo());
                map.put("studentName", participant.getStudentName());
                map.put("participantStatus", participant.getParticipantStatus());
                map.put("startTime", participant.getStartTime());
                map.put("submitTime", participant.getSubmitTime());
                map.put("ipAddress", participant.getIpAddress());
                map.put("deviceInfo", participant.getDeviceInfo());
                map.put("totalScore", participant.getTotalScore());
                map.put("timeUsed", participant.getTimeUsed());
                map.put("objectiveScore", participant.getObjectiveScore());
                map.put("subjectiveScore", participant.getSubjectiveScore());
                map.put("correctStatus", participant.getCorrectStatus());
                map.put("passStatus", participant.getPassStatus());
                // 计算 及格 文字描述
                String passLabel = "—";
                Integer ps = participant.getPassStatus();
                if (ps != null) passLabel = ps == 1 ? "及格" : "不及格";
                map.put("passStatusLabel", passLabel);
                map.put("createTime", participant.getCreateTime());
                map.put("updateTime", participant.getUpdateTime());

                // 补充考试信息
                if (examRef != null) {
                    map.put("examName", examRef.getExamName());
                    map.put("courseName", examRef.getCourseName());
                    // 根据需求：我的考试记录里不需要有课堂，但 participant/list 表格保留 className 以便区分
                    map.put("className", examRef.getClassName());
                    map.put("examType", examRef.getExamType());
                    map.put("examTotalScore", examRef.getTotalScore());
                    map.put("passScore", examRef.getPassScore());
                }
                result.add(map);
            }
            return AjaxResult.success(result);
        } catch (Exception ex) {
            // 任何异常均返回空列表，避免 500 影响前端
            return AjaxResult.success(java.util.Collections.emptyList()).put("error", ex.getMessage());
        }
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
        if (studentId == null) {
            // 未能解析出学生身份 -> 明确提示
            return AjaxResult.error("学号未找到，无法开始考试");
        }
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

        // 计算得分：汇总该考生本次考试的所有答题记录
        ClassExamAnswer queryAnswer = new ClassExamAnswer();
        queryAnswer.setExamId(payload.getExamId());
        queryAnswer.setStudentId(studentId);
        List<ClassExamAnswer> answers = answerService.selectList(queryAnswer);

        // 获取考试题目列表以判断题型
        ClassExamQuestion queryQuestion = new ClassExamQuestion();
        queryQuestion.setExamId(payload.getExamId());
        List<ClassExamQuestion> questions = questionService.selectQuestionList(queryQuestion);
        java.util.Map<Long, ClassExamQuestion> qMap = new java.util.HashMap<>();
        boolean hasObjectiveQuestions = false;  // 是否包含客观题
        boolean hasSubjectiveQuestions = false; // 是否包含主观题
        for (ClassExamQuestion q : questions) {
            qMap.put(q.getId(), q);
            Integer t = q.getQuestionType();
            if (t != null && (t == 1 || t == 2 || t == 3)) hasObjectiveQuestions = true;
            if (t != null && (t == 5 || t == 6)) hasSubjectiveQuestions = true;
        }

        BigDecimal objectiveScore = BigDecimal.ZERO;  // 客观题得分
        BigDecimal subjectiveScore = BigDecimal.ZERO; // 主观题得分
        boolean hasUnscoredSubjective = false; // 是否有未批改的主观题

        for (ClassExamAnswer ans : answers) {
            ClassExamQuestion q = qMap.get(ans.getQuestionId());
            if (q == null) continue;

            Integer qType = q.getQuestionType();
            BigDecimal ansScore = ans.getScore();
            if (ansScore == null) ansScore = BigDecimal.ZERO;

            // 判断题型：1单选 2多选 3判断 -> 客观题；5简答 6文件 -> 主观题
            if (qType != null && (qType == 1 || qType == 2 || qType == 3)) {
                objectiveScore = objectiveScore.add(ansScore);
            } else if (qType != null && (qType == 5 || qType == 6)) {
                // 主观题：如果未批改(score为null或0且无批改人)，标记为待批改
                if (ans.getScore() == null || (ans.getScore().compareTo(BigDecimal.ZERO) == 0 && ans.getCorrectorId() == null)) {
                    hasUnscoredSubjective = true;
                } else {
                    subjectiveScore = subjectiveScore.add(ansScore);
                }
            }
        }

        // 当考试没有主观题时：主观题得分=客观题得分，用于前端列表展示，但总分=客观题得分（避免重复累计）
        if (!hasSubjectiveQuestions) {
            subjectiveScore = objectiveScore;
        }

        BigDecimal totalScore = hasSubjectiveQuestions ? objectiveScore.add(subjectiveScore) : objectiveScore;
        existing.setObjectiveScore(objectiveScore);
        existing.setSubjectiveScore(subjectiveScore);
        existing.setTotalScore(totalScore);

        // 判断批改状态：如果有未批改的主观题，状态为0；否则为1（无主观题也视为已批改）
        existing.setCorrectStatus(hasUnscoredSubjective ? 0 : 1);

        // 判断及格状态：仅在所有题目已批改完成或没有主观题时才判断
        ClassExam exam = examService.selectById(payload.getExamId());
        if (exam != null && exam.getPassScore() != null && !hasUnscoredSubjective) {
            existing.setPassStatus(totalScore.compareTo(exam.getPassScore()) >= 0 ? 1 : 0);
        } else {
            existing.setPassStatus(0); // 未完成批改或无及格线，暂不及格
        }

        participantService.update(existing);
        return AjaxResult.success(existing);
    }

    // 学生考试入口：按学号列出可参加考试
    @GetMapping("/available")
    public AjaxResult available(@RequestParam(value = "studentNo", required = false) String studentNo) {
        try {
            // 学号缺失 -> 返回空列表且 200
            if (studentNo == null || studentNo.trim().isEmpty()) {
                return AjaxResult.success(java.util.Collections.emptyList());
            }
            // 包含已结束考试（状态3），前端自行分组显示
            List<ClassExam> exams = examService.selectAvailableByStudentNo(studentNo.trim());
            return AjaxResult.success(exams);
        } catch (Exception ex) {
            return AjaxResult.success(java.util.Collections.emptyList()).put("error", ex.getMessage());
        }
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
        // 支持以 studentNo 过滤：解析为 studentId
        if (a != null && a.getStudentId() == null && a.getStudentNo() != null) {
            ClassStudent s = classStudentMapper.selectByStudentNo(a.getStudentNo());
            if (s != null) { a.setStudentId(s.getStudentId()); }
        }
        List<ClassExamAnswer> list = answerService.selectList(a);
        return AjaxResult.success(list);
    }

    // 自动判分简易逻辑（基于传入答案与快照答案对比；正确则取题目分值）
    private void autoJudgeObjective(ClassExamAnswer a) {
        if (a.getCorrectAnswer() != null && a.getStudentAnswer() != null) {
            String std = a.getCorrectAnswer().trim();
            String stu = a.getStudentAnswer().trim();
            if (std.equalsIgnoreCase(stu)) {
                a.setIsCorrect(1);
                // 若未设置分数，则按题目分值给分（回填在上层获取的 questionContent 逻辑之后生效）
                if (a.getScore() == null) {
                    try {
                        // 从题目快照内容无法直接取分值，这里保持为空，交由保存时根据题库题目补齐
                        // 留空 -> 在 saveAnswer 中使用实际题目分值回填
                    } catch (Exception ignore) {}
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

        // 需要判断考试是否包含主观题/客观题
        ClassExamQuestion qFilter = new ClassExamQuestion();
        qFilter.setExamId(examId);
        List<ClassExamQuestion> qList = questionService.selectQuestionList(qFilter);
        boolean hasSubjectiveQuestions = false;
        java.util.Map<Long, ClassExamQuestion> qMap = new java.util.HashMap<>();
        for (ClassExamQuestion q : qList) {
            qMap.put(q.getId(), q);
            Integer t = q.getQuestionType();
            if (t != null && (t == 5 || t == 6)) { hasSubjectiveQuestions = true; }
        }

        BigDecimal obj = BigDecimal.ZERO; BigDecimal subj = BigDecimal.ZERO;
        for (ClassExamAnswer ans : answers) {
            ClassExamQuestion q = qMap.get(ans.getQuestionId());
            BigDecimal s = ans.getScore() == null ? BigDecimal.ZERO : ans.getScore();
            if (q != null) {
                Integer t = q.getQuestionType();
                if (t != null && (t == 1 || t == 2 || t == 3)) obj = obj.add(s); else subj = subj.add(s);
            }
        }
        // 无主观题：主观题得分=客观题得分；总分=客观题得分
        if (!hasSubjectiveQuestions) {
            subj = obj;
        }
        part.setObjectiveScore(obj);
        part.setSubjectiveScore(subj);
        part.setTotalScore(hasSubjectiveQuestions ? obj.add(subj) : obj);
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
        // 优先用学号反查 studentId，保证与参与记录一致
        Long studentId = payload.getStudentId();
        if ((studentId == null || studentId <= 0) && payload.getStudentNo() != null) {
            ClassStudent s = classStudentMapper.selectByStudentNo(payload.getStudentNo());
            if (s != null) { studentId = s.getStudentId(); }
        }
        // 若仍为空，明确报错，避免 DB 违反非空约束
        if (studentId == null) {
            return AjaxResult.error("学号未找到，无法保存答案");
        }
        // 关键：将解析出的 studentId 回写到 payload，避免插入时为 NULL
        if (payload.getStudentId() == null || !studentId.equals(payload.getStudentId())) {
            payload.setStudentId(studentId);
        }
        // 补齐 studentNo
        if (payload.getStudentNo() == null) {
            ClassStudent stu = classStudentMapper.selectByStudentId(studentId);
            if (stu != null) { payload.setStudentNo(stu.getStudentNo()); }
        }
        // 快照字段填充（仅在不存在时）
        if (payload.getQuestionContent() == null || payload.getQuestionOptions() == null || payload.getCorrectAnswer() == null || payload.getScore() == null) {
            ClassExamQuestion q = questionService.selectById(payload.getQuestionId());
            if (q != null) {
                if (payload.getQuestionContent() == null) payload.setQuestionContent(q.getQuestionContent());
                if (payload.getQuestionOptions() == null) payload.setQuestionOptions(q.getQuestionOptions());
                if (payload.getCorrectAnswer() == null) payload.setCorrectAnswer(q.getCorrectAnswer());
                // 如果是客观题且未设置分数，按题目分值赋分（正确时）
                if (payload.getScore() == null && payload.getStudentAnswer() != null && payload.getCorrectAnswer() != null) {
                    Integer t = q.getQuestionType();
                    if (t != null && (t == 1 || t == 2 || t == 3) && payload.getStudentAnswer().trim().equalsIgnoreCase(payload.getCorrectAnswer().trim())) {
                        if (q.getScore() != null) payload.setScore(q.getScore()); else payload.setScore(java.math.BigDecimal.ONE);
                        payload.setIsCorrect(1);
                    }
                }
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

    // 学生综合考试列表：包含可参加考试 + 已参与记录(成绩/状态)
    @GetMapping("/my")
    public AjaxResult myExams(@RequestParam(value = "studentNo", required = false) String studentNo) {
        try {
            // 学号缺失 -> 返回空列表且 200
            if (studentNo == null || studentNo.trim().isEmpty()) {
                return AjaxResult.success(java.util.Collections.emptyList());
            }
            studentNo = studentNo.trim();
            // 查询该学号的学生ID
            ClassStudent stu = classStudentMapper.selectByStudentNo(studentNo);
            Long studentId = stu != null ? stu.getStudentId() : null;
            // 可参加考试列表（已发布/进行中）
            List<ClassExam> exams = examService.selectAvailableByStudentNo(studentNo);
            List<Map<String,Object>> data = new ArrayList<>();
            for (ClassExam ex : exams) {
                Map<String,Object> row = new LinkedHashMap<>();
                row.put("examId", ex.getId());
                row.put("examName", ex.getExamName());
                row.put("examType", ex.getExamType());
                row.put("courseId", ex.getCourseId());
                // 根据需求：我的考试记录里不需要有课堂 -> 不再返回 sessionId / className
                row.put("courseName", ex.getCourseName());
                row.put("startTime", ex.getStartTime());
                row.put("endTime", ex.getEndTime());
                row.put("status", ex.getStatus());
                row.put("totalScoreSetting", ex.getTotalScore());
                row.put("passScore", ex.getPassScore());
                // 参与记录
                ClassExamParticipant part = null;
                if (studentId != null) part = participantService.selectByExamStudent(ex.getId(), studentId);
                if (part != null) {
                    row.put("participantId", part.getId());
                    row.put("participantStatus", part.getParticipantStatus());
                    row.put("objectiveScore", part.getObjectiveScore());
                    row.put("subjectiveScore", part.getSubjectiveScore());
                    row.put("totalScore", part.getTotalScore());
                    row.put("passStatus", part.getPassStatus());
                    // 计算 及格 文字描述
                    Integer ps = part.getPassStatus();
                    row.put("passStatusLabel", ps==null?"—":(ps==1?"及格":"不及格"));
                    row.put("correctStatus", part.getCorrectStatus());
                    row.put("startTimeActual", part.getStartTime());
                    row.put("submitTime", part.getSubmitTime());
                    row.put("timeUsed", part.getTimeUsed());
                    row.put("entered", true);
                } else {
                    row.put("entered", false);
                }
                data.add(row);
            }
            return AjaxResult.success(data);
        } catch (Exception ex) {
            return AjaxResult.success(java.util.Collections.emptyList()).put("error", ex.getMessage());
        }
    }
}
