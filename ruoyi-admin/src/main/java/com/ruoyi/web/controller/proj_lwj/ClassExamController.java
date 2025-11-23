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
import java.util.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/proj_lwj/exam")
public class ClassExamController extends BaseController {

    @Autowired
    private IClassExamService examService;
    @Autowired
    private IClassExamQuestionService questionService;
    @Autowired
    private IClassExamParticipantService participantService;
    @Autowired
    private IClassExamAnswerService answerService;
    @Autowired
    private IClassExamMonitorService monitorService;
    @Autowired
    private IClassExamSessionService examSessionService;
    @Autowired
    private ClassStudentMapper classStudentMapper;

    // 验证考试数据
    private String validate(ClassExam e) {
        if (e.getStartTime() != null && e.getEndTime() != null && e.getStartTime().after(e.getEndTime())) {
            return "开始时间不能晚于结束时间";
        }
        BigDecimal total = e.getTotalScore() == null ? BigDecimal.ZERO : e.getTotalScore();
        BigDecimal pass = e.getPassScore() == null ? BigDecimal.ZERO : e.getPassScore();
        if (pass.compareTo(BigDecimal.ZERO) < 0) return "及格分不能小于0";
        if (pass.compareTo(total) > 0) return "及格分不能大于总分";
        return null;
    }

    // 列表查询
    @PreAuthorize("@ss.hasPermi('projlwj:exam:list')")
    @GetMapping("/list")
    public TableDataInfo list(ClassExam query) {
        startPage();
        List<ClassExam> list = examService.selectExamList(query);
        return getDataTable(list);
    }

    // 获取考试详情
    @GetMapping("/{id}")
    public AjaxResult info(@PathVariable Long id) {
        ClassExam ex = examService.selectExamById(id);
        if (ex == null) {
            // 返回 200，避免前端弹出"访问资源不存在"
            return AjaxResult.success("考试不存在或��删除", null).put("missing", true).put("id", id);
        }
        return AjaxResult.success(ex);
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

    // 编辑
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
        // 删除前先检查状态：允许删除 草稿(0) 和 已结束(3) 的考试；禁止删除 已发布(1) / 进行中(2)
        if (ids != null) {
            for (Long examId : ids) {
                if (examId == null) continue;
                ClassExam ex = examService.selectExamById(examId);
                if (ex == null) continue; // 不存在的直接跳过
                Integer st = ex.getStatus();
                if (st != null && (st == 1 || st == 2)) {
                    // 有任一考试处于已发布或进行中，则整体拒绝删除，提示用户先结束考试
                    String name = ex.getExamName() == null ? String.valueOf(examId) : ex.getExamName();
                    return AjaxResult.error("考试[" + name + "]当前状态不允许删除，请先结束考试或改为草稿");
                }
            }
        }

        // 级联删除：先删答案、参与记录、监控与课堂关联，再删考试
        for (Long examId : ids) {
            try { answerService.deleteByExamId(examId); } catch (Exception ignore) {}
            try { participantService.deleteByExamId(examId); } catch (Exception ignore) {}
            try { monitorService.deleteByExamId(examId); } catch (Exception ignore) {}
            try { examSessionService.deleteByExamId(examId); } catch (Exception ignore) {}
        }
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
        // 计算题目总分
        ClassExamQuestion filter = new ClassExamQuestion();
        filter.setExamId(id);
        List<ClassExamQuestion> questions = questionService.selectQuestionList(filter);
        if (questions == null || questions.isEmpty()) {
            return AjaxResult.error("尚未配置题目，无法发布考试");
        }
        java.math.BigDecimal questionsTotal = java.math.BigDecimal.ZERO;
        for (ClassExamQuestion q : questions) {
            questionsTotal = questionsTotal.add(q.getScore() == null ? java.math.BigDecimal.ZERO : q.getScore());
        }
        if (exist.getTotalScore() == null) {
            return AjaxResult.error("考试未设置总分，请先在考试信息中填写总分");
        }
        // 必须严格相等
        if (questionsTotal.compareTo(exist.getTotalScore()) != 0) {
            return AjaxResult.error("题目总分(" + questionsTotal + ")与考试设定总分(" + exist.getTotalScore() + ")不一致，无法发布。请调整题目分值或考试总分。");
        }
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
        if (st != 1) return AjaxResult.error("仅已发布状态可开始");
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

    // 状态流转
    @PreAuthorize("@ss.hasPermi('projlwj:exam:status')")
    @Log(title = "考试状态更新", businessType = BusinessType.UPDATE)
    @PutMapping("/status/{id}/{status}")
    public AjaxResult changeStatus(@PathVariable Long id, @PathVariable Integer status) {
        ClassExam ex = examService.selectExamById(id);
        if (ex == null) return AjaxResult.error("考试不存在");
        ex.setStatus(status);
        ex.setUpdateBy(getUsername());
        int rows = examService.updateExam(ex);
        return toAjax(rows);
    }

    // 考试关联课堂
    @PreAuthorize("@ss.hasPermi('projlwj:exam:sessions')")
    @PutMapping("/{examId}/sessions")
    public AjaxResult bindSessions(@PathVariable Long examId, @RequestBody List<Long> sessionIds) {
        ClassExam ex = examService.selectExamById(examId);
        if (ex == null) return AjaxResult.error("考试不存在");
        int c = examSessionService.replaceExamSessions(examId, sessionIds);
        return AjaxResult.success("已关联课堂数量:" + c);
    }

    @GetMapping("/{examId}/sessions")
    public AjaxResult examSessions(@PathVariable Long examId) {
        return AjaxResult.success(examSessionService.selectByExamId(examId));
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

    // ===== 参与者相关 =====
    @GetMapping("/participant/list")
    public AjaxResult participantList(@RequestParam(value = "studentNo", required = false) String studentNo) {
        try {
            if (studentNo == null || studentNo.trim().isEmpty()) {
                return AjaxResult.success(java.util.Collections.emptyList());
            }

            ClassStudent stu = classStudentMapper.selectByStudentNo(studentNo.trim());
            if (stu == null) {
                return AjaxResult.success(java.util.Collections.emptyList());
            }

            ClassExamParticipant query = new ClassExamParticipant();
            query.setStudentId(stu.getStudentId());
            List<ClassExamParticipant> participants = participantService.selectList(query);

            List<Map<String, Object>> result = new ArrayList<>();
            for (ClassExamParticipant participant : participants) {
                // 检查考试是否还存在（避免显示已删除考试的记录）
                ClassExam examRef = participant.getExamId() != null ? examService.selectExamById(participant.getExamId()) : null;
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

    @Log(title = "考试参与", businessType = BusinessType.INSERT)
    @PostMapping("/participant/start")
    public AjaxResult startExam(@RequestBody ClassExamParticipant payload) {
        if (payload.getExamId() == null) return AjaxResult.error("examId不能为空");
        Long studentId = payload.getStudentId();
        if ((studentId == null || payload.getStudentNo() == null) && payload.getStudentNo() != null) {
            ClassStudent s = classStudentMapper.selectByStudentNo(payload.getStudentNo());
            if (s != null) {
                studentId = s.getStudentId();
                if (payload.getStudentName() == null) payload.setStudentName(s.getStudentName());
            }
        }
        if (studentId == null) {
            // 未能解析出学生身份 -> 明确提示
            return AjaxResult.error("学号未找到，无法开始考试");
        }
        // 反查补齐 studentNo/studentName，避免 DB 约束失败
        if (payload.getStudentNo() == null) {
            ClassStudent s2 = classStudentMapper.selectByStudentId(studentId);
            if (s2 != null) {
                payload.setStudentNo(s2.getStudentNo());
                if (payload.getStudentName() == null) payload.setStudentName(s2.getStudentName());
            }
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
            if (s2 != null) {
                payload.setStudentNo(s2.getStudentNo());
                if (payload.getStudentName() == null) payload.setStudentName(s2.getStudentName());
            }
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
        boolean hasSubjectiveQuestions = false; // 是否包含主观题
        for (ClassExamQuestion q : questions) {
            qMap.put(q.getId(), q);
            Integer t = q.getQuestionType();
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
        ClassExam exam = examService.selectExamById(payload.getExamId());
        if (exam != null && exam.getPassScore() != null && !hasUnscoredSubjective) {
            existing.setPassStatus(totalScore.compareTo(exam.getPassScore()) >= 0 ? 1 : 0);
        } else {
            existing.setPassStatus(0); // 未完成批改或无及格线，暂不及格
        }

        participantService.update(existing);
        return AjaxResult.success(existing);
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
            Integer qType = null;
            BigDecimal qScore = null;
            try {
                if (a.getQuestionId() != null) {
                    ClassExamQuestion q = questionService.selectById(a.getQuestionId());
                    if (q != null) { qType = q.getQuestionType(); qScore = q.getScore(); }
                }
            } catch (Exception ignore) {}
            String canonCorrect = canonicalAnswerForCompare(qType, a.getCorrectAnswer());
            String canonStudent = canonicalAnswerForCompare(qType, a.getStudentAnswer());
            boolean correct = canonCorrect != null && canonCorrect.equalsIgnoreCase(canonStudent);
            if (qType != null && (qType == 1 || qType == 2 || qType == 3)) { // objective
                if (correct) {
                    a.setIsCorrect(1);
                    a.setScore(qScore != null ? qScore : BigDecimal.ONE);
                } else {
                    a.setIsCorrect(0);
                    a.setScore(BigDecimal.ZERO);
                }
            } else { // subjective or unknown
                if (correct) a.setIsCorrect(1); else a.setIsCorrect(0);
                // score for subjective left as-is (teacher grading later)
            }
        }
    }

    // 将给定答案根据题型规范化为便于比较的形式
    private String canonicalAnswerForCompare(Integer questionType, String raw) {
        if (raw == null) return null;
        String t = raw.trim();
        if (t.isEmpty()) return t;
        // 常见布尔/判断的文本变体
        String lowered = t.toLowerCase();
        if (questionType != null && questionType == 3) {
            if (lowered.matches(".*(true|1|t|y|yes|对|正确|是).*")) return "true";
            if (lowered.matches(".*(false|0|f|n|no|错|错误|否).*")) return "false";
            return lowered;
        }


        // 处理单选/多选（A/B/C/D 等）——提取字母 A-Z 作为选项标识
        if (questionType != null && (questionType == 1 || questionType == 2)) {
            // 将常见分隔符替换为统一形式
            String normalized = t.replaceAll("[，；、/|;]+", ",").replaceAll("\\s+", "");
            // 提取所有英文字母作为选项
            Pattern p = Pattern.compile("[A-Za-z]");
            Matcher m = p.matcher(normalized);
            List<String> letters = new ArrayList<>();
            while (m.find()) {
                letters.add(m.group().toUpperCase());
            }
            if (letters.isEmpty()) {
                // 如果没有字母，尝试提取数字或单字符答案（例如 'true' 出现在混合字符串中）
                // 将数字或字母数字序列作为单选备选
                Pattern p2 = Pattern.compile("[0-9]+|[A-Za-z]+|");
                Matcher m2 = p2.matcher(normalized);
                List<String> tokens = new ArrayList<>();
                while (m2.find()) {
                    String g = m2.group();
                    if (g != null && !g.isEmpty()) tokens.add(g.toUpperCase());
                }
                if (!tokens.isEmpty()) {
                    if (questionType == 1) return tokens.get(0);
                    // 多选：合并去重排序
                    Set<String> s = new TreeSet<>(tokens);
                    return String.join(",", s);
                }
                return normalized.toUpperCase();
            }
            if (questionType == 1) {
                // 单选：取第一个字母
                return letters.get(0);
            }
            // 多选：去重、排序并用逗号连接，便于比较（例如 A,B）
            Set<String> set = new TreeSet<>(letters);
            return String.join(",", set);
        }

        // 其他题型：小写并去除空白以便比较
        return t.toLowerCase();
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

        BigDecimal obj = BigDecimal.ZERO;
        BigDecimal subj = BigDecimal.ZERO;
        for (ClassExamAnswer ans : answers) {
            ClassExamQuestion q = qMap.get(ans.getQuestionId());
            BigDecimal s = ans.getScore() == null ? BigDecimal.ZERO : ans.getScore();
            if (q != null) {
                Integer t = q.getQuestionType();
                if (t != null && (t == 1 || t == 2 || t == 3)) obj = obj.add(s);
                else subj = subj.add(s);
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
        if (payload.getExamId() == null || payload.getQuestionId() == null) {
            return AjaxResult.error("examId/questionId不能为空");
        }
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
        if (payload.getQuestionContent() == null || payload.getQuestionOptions() == null ||
            payload.getCorrectAnswer() == null || payload.getScore() == null) {
            ClassExamQuestion q = questionService.selectById(payload.getQuestionId());
            if (q != null) {
                if (payload.getQuestionContent() == null) payload.setQuestionContent(q.getQuestionContent());
                if (payload.getQuestionOptions() == null) payload.setQuestionOptions(q.getQuestionOptions());
                if (payload.getCorrectAnswer() == null) payload.setCorrectAnswer(q.getCorrectAnswer());
                // 如果是客观题且未设置分数，按题目分值赋分（正确时）
                if (payload.getScore() == null && payload.getStudentAnswer() != null && payload.getCorrectAnswer() != null) {
                    Integer t = q.getQuestionType();
                    if (t != null && (t == 1 || t == 2 || t == 3)) {
                        String canonStd = canonicalAnswerForCompare(t, payload.getCorrectAnswer());
                        String canonStu = canonicalAnswerForCompare(t, payload.getStudentAnswer());
                        if (canonStd != null && canonStd.equalsIgnoreCase(canonStu)) {
                            payload.setScore(q.getScore() != null ? q.getScore() : BigDecimal.ONE);
                            payload.setIsCorrect(1);
                        } else {
                            payload.setScore(BigDecimal.ZERO);
                            payload.setIsCorrect(0);
                        }
                    }
                }
            }
        }
        ClassExamAnswer existing = answerService.selectByExamStudentQuestion(payload.getExamId(), studentId, payload.getQuestionId());
        autoJudgeObjective(payload); // ensures objective score normalized even if score existed
        if (existing == null) {
            payload.setCreateBy(getUsername());
            answerService.insert(payload);
        } else {
            existing.setStudentAnswer(payload.getStudentAnswer());
            existing.setAnswerFiles(payload.getAnswerFiles());
            existing.setIsCorrect(payload.getIsCorrect());
            // 强制重算分数（防止遗留旧分值）
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
        int total = list.size();
        int started=0;
        int finished=0;
        int passed=0;
        int graded=0;
        for (ClassExamParticipant p : list) {
            Integer st = p.getParticipantStatus();
            if (st != null && st >= 1) started++;
            if (st != null && st == 2) finished++;
            if (p.getPassStatus() != null && p.getPassStatus() == 1) passed++;
            if (p.getCorrectStatus() != null && p.getCorrectStatus() == 1) graded++;
        }
        Map<String,Object> data = new HashMap<>();
        data.put("total", total);
        data.put("started", started);
        data.put("finished", finished);
        data.put("graded", graded);
        data.put("passed", passed);
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
            return Integer.compare(a.getTimeUsed()==null?Integer.MAX_VALUE:a.getTimeUsed(),
                                   b.getTimeUsed()==null?Integer.MAX_VALUE:b.getTimeUsed());
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
        return AjaxResult.success("批量创建成功��" + success + " 个");
    }

    // 请求体 DTO：包含考试模板和多个课堂ID
    public static class BatchExamReq {
        public ClassExam exam;
        public java.util.List<Long> sessionIds;
    }
}
