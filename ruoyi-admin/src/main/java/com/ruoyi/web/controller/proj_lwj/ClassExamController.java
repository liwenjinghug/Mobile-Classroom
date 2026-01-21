package com.ruoyi.web.controller.proj_lwj;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
// [修改] 引入自定义的 Log 注解
import com.ruoyi.proj_cyq.annotation.Log;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
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

    // 新增：根据课堂ID获取考试列表
    @GetMapping("/list-by-session/{sessionId}")
    public AjaxResult listBySession(@PathVariable Long sessionId) {
        if (sessionId == null) {
            return AjaxResult.error("课堂ID不能为空");
        }
        ClassExam query = new ClassExam();
        query.setSessionId(sessionId);
        List<ClassExam> list = examService.selectExamList(query);
        // 仅返回已发布及以后状态的考试（隐藏草稿）
        if (list != null) {
            List<ClassExam> filtered = new java.util.ArrayList<>();
            for (ClassExam ex : list) {
                Integer st = ex == null ? null : ex.getStatus();
                if (st != null && st != 0) { // 非草稿
                    filtered.add(ex);
                }
            }
            list = filtered;
        }
        return AjaxResult.success(list);
    }

    // 获取考试详情
    @GetMapping("/{id}")
    public AjaxResult info(@PathVariable Long id) {
        ClassExam ex = examService.selectExamById(id);
        if (ex == null) {
            AjaxResult res = AjaxResult.success("考试不存在或已删除", null);
            res.put("missing", true);
            res.put("id", id);
            return res;
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
        ClassExam exist = examService.selectExamById(exam.getId());
        if (exist == null) return AjaxResult.error("考试不存在或已删除");
        Integer st = exist.getStatus();
        if (st != null && st == 2) return AjaxResult.error("进行中的考试不可编辑，请先结束考试");
        if (st != null && st == 3) return AjaxResult.error("已结束考试不可编辑");
        String err = validate(exam);
        if (err != null) return AjaxResult.error(err);
        // 保持原状态不被前端错误覆盖（仅允许通过专门的状态接口变更）
        exam.setStatus(exist.getStatus());
        exam.setUpdateBy(getUsername());
        int rows = examService.updateExam(exam);
        return toAjax(rows);
    }

    // 删除
    @PreAuthorize("@ss.hasPermi('projlwj:exam:remove')")
    @Log(title = "考试管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        // 仅禁止删除进行中(status=2)考试；草稿/已发布/已结束均可删除
        if (ids != null) {
            for (Long examId : ids) {
                if (examId == null) continue;
                ClassExam ex = examService.selectExamById(examId);
                if (ex == null) continue;
                Integer st = ex.getStatus();
                if (st != null && st == 2) {
                    String name = ex.getExamName() == null ? String.valueOf(examId) : ex.getExamName();
                    return AjaxResult.error("考试[" + name + "]进行中，暂不可删除，请先结束考试");
                }
            }
        }
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
    @Log(title = "考试关联课堂", businessType = BusinessType.UPDATE)
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
            // 包含已结束考试（状态3），前端自行分组显示；隐藏草稿（状态0）
            List<ClassExam> exams = examService.selectAvailableByStudentNo(studentNo.trim());
            if (exams != null) {
                List<ClassExam> filtered = new java.util.ArrayList<>();
                for (ClassExam ex : exams) {
                    Integer st = ex == null ? null : ex.getStatus();
                    if (st != null && st != 0) { // 非草稿
                        filtered.add(ex);
                    }
                }
                exams = filtered;
            }
            return AjaxResult.success(exams);
        } catch (Exception ex) {
            return AjaxResult.success(java.util.Collections.emptyList()).put("error", ex.getMessage());
        }
    }

    /**
     * 获取当前登录用户可参加的考试列表（基于登录账号，无需学号）
     */
    @GetMapping("/my-available")
    public AjaxResult myAvailable() {
        try {
            String username = getUsername();
            Long userId = getUserId();
            logger.info("myAvailable called for userId={}, username={}", userId, username);

            // 先通过 userId 获取学生信息，获取真实的 studentNo
            ClassStudent student = classStudentMapper.selectByUserId(userId);
            String studentNo;
            if (student != null && student.getStudentNo() != null && !student.getStudentNo().trim().isEmpty()) {
                studentNo = student.getStudentNo();
                logger.info("myAvailable: Found studentNo={} for userId={}", studentNo, userId);
            } else {
                // 如果通过 userId 找不到，回退使用 username
                studentNo = username;
                logger.info("myAvailable: No student found for userId={}, fallback to username={}", userId, username);
            }

            return available(studentNo);
        } catch (Exception ex) {
            logger.error("myAvailable error", ex);
            return AjaxResult.success(java.util.Collections.emptyList()).put("error", ex.getMessage());
        }
    }

    // 学生综合考试列表：包含可参加考试 + 已参与记录(成绩/状态)
    @GetMapping("/my")
    public AjaxResult myExams(@RequestParam(value = "studentNo", required = false) String studentNo) {
        try {
            if (studentNo == null || studentNo.trim().isEmpty()) {
                return AjaxResult.success(java.util.Collections.emptyList());
            }
            studentNo = studentNo.trim();
            ClassStudent stu = classStudentMapper.selectByStudentNo(studentNo);
            Long studentId = stu != null ? stu.getStudentId() : null;
            List<ClassExam> exams = examService.selectAvailableByStudentNo(studentNo);
            List<Map<String,Object>> data = new ArrayList<>();
            for (ClassExam ex : exams) {
                Map<String,Object> row = new LinkedHashMap<>();
                row.put("examId", ex.getId());
                row.put("examName", ex.getExamName());
                row.put("examType", ex.getExamType());
                row.put("courseId", ex.getCourseId());
                row.put("courseName", ex.getCourseName());
                row.put("startTime", ex.getStartTime());
                row.put("endTime", ex.getEndTime());
                row.put("status", ex.getStatus());
                row.put("totalScoreSetting", ex.getTotalScore());
                row.put("passScore", ex.getPassScore());
                // 检查是否包含主观题
                boolean hasSubjective = false;
                try {
                    ClassExamQuestion qFilter = new ClassExamQuestion();
                    qFilter.setExamId(ex.getId());
                    List<ClassExamQuestion> qList = questionService.selectQuestionList(qFilter);
                    if (qList != null) {
                        for (ClassExamQuestion q : qList) {
                            Integer t = q.getQuestionType();
                            // 新编码：3=简答题（主观题）
                            if (t != null && t == 3) { hasSubjective = true; break; }
                        }
                        row.put("questionCount", qList.size());
                    } else {
                        row.put("questionCount", 0);
                    }
                } catch (Exception ignore) {
                    row.put("questionCount", null);
                }
                row.put("hasSubjective", hasSubjective);
                ClassExamParticipant part = null;
                if (studentId != null) part = participantService.selectByExamStudent(ex.getId(), studentId);
                if (part != null) {
                    row.put("participantId", part.getId());
                    row.put("participantStatus", part.getParticipantStatus());
                    row.put("objectiveScore", part.getObjectiveScore());
                    row.put("correctStatus", part.getCorrectStatus()); // 1 已批改, 0 未完成
                    row.put("startTimeActual", part.getStartTime());
                    row.put("submitTime", part.getSubmitTime());
                    row.put("timeUsed", part.getTimeUsed());
                    row.put("entered", true);
                    // 仅在全部批改完成或没有主观题时才返回主观题/总分/及格信息
                    boolean canRevealFinal = (part.getCorrectStatus() != null && part.getCorrectStatus() == 1) || !hasSubjective;
                    if (canRevealFinal) {
                        row.put("subjectiveScore", part.getSubjectiveScore());
                        row.put("totalScore", part.getTotalScore());
                        row.put("passStatus", part.getPassStatus());
                        Integer ps = part.getPassStatus();
                        row.put("passStatusLabel", ps==null?"—":(ps==1?"及格":"不及格"));
                    } else {
                        row.put("subjectiveScore", null);
                        row.put("totalScore", null);
                        row.put("passStatus", null);
                        row.put("passStatusLabel", "待批改");
                    }
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

    /**
     * 获取当前登录用户的综合考试列表（基于登录账号，无需学号）
     */
    @GetMapping("/my-exams")
    public AjaxResult myOwnExams() {
        try {
            String username = getUsername();
            Long userId = getUserId();
            logger.info("myOwnExams called for userId={}, username={}", userId, username);

            // 使用username作为studentNo查询
            return myExams(username);
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
                // 只显示已提交的考试记录（participantStatus=2），未完成的不显示
                if (participant.getParticipantStatus() == null || participant.getParticipantStatus() != 2) {
                    continue;
                }

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
                    map.put("courseId", examRef.getCourseId()); // 添加课程ID用于筛选
                    // 根据需求：我的考试记录里不需要有课堂，但 participant/list 表格保留 className 以便区分
                    map.put("className", examRef.getClassName());
                    map.put("sessionId", examRef.getSessionId()); // 添加课堂ID用于筛选
                    map.put("examType", examRef.getExamType());
                    map.put("examTotalScore", examRef.getTotalScore());
                    map.put("passScore", examRef.getPassScore());
                }
                result.add(map);
            }
            return AjaxResult.success(result);
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
        Map<String, Object> submitPayload = new HashMap<>();
        submitPayload.put("examId", examId);
        submitPayload.put("studentNo", studentNo);
        return submitExam(submitPayload);
    }

    @Log(title = "考试参与", businessType = BusinessType.INSERT)
    @PostMapping("/participant/start")
    public AjaxResult startExam(@RequestBody ClassExamParticipant payload) {
        if (payload.getExamId() == null) return AjaxResult.error("examId不能为空");

        Long studentId = payload.getStudentId();
        String studentNo = payload.getStudentNo();

        // 如果没有提供studentNo，使用当前登录用户的username
        if (studentNo == null || studentNo.trim().isEmpty()) {
            try {
                studentNo = getUsername();
                payload.setStudentNo(studentNo);
                logger.info("Using logged-in username as studentNo: {}", studentNo);
            } catch (Exception e) {
                logger.warn("Failed to get logged-in username", e);
            }
        }

        if ((studentId == null || studentNo == null) && studentNo != null) {
            ClassStudent s = classStudentMapper.selectByStudentNo(studentNo);
            if (s != null) {
                studentId = s.getStudentId();
                if (payload.getStudentName() == null) payload.setStudentName(s.getStudentName());
            }
        }

        // 如果仍然没有studentId，尝试使用当前登录用户的userId
        if (studentId == null) {
            try {
                Long userId = getUserId();
                String username = getUsername();

                // 尝试通过username查找class_student
                ClassStudent cs = classStudentMapper.selectByStudentNo(username);
                if (cs != null) {
                    studentId = cs.getStudentId();
                    studentNo = cs.getStudentNo();
                    payload.setStudentNo(studentNo);
                    if (payload.getStudentName() == null) payload.setStudentName(cs.getStudentName());
                    logger.info("Found class_student for username {}: studentId={}", username, studentId);
                } else {
                    // 如果找不到，使用userId作为studentId
                    studentId = userId;
                    studentNo = username;
                    payload.setStudentNo(username);
                    logger.info("Using userId as studentId: {}", studentId);
                }
            } catch (Exception e) {
                logger.warn("Failed to get user identity", e);
            }
        }

        if (studentId == null) {
            // 未能解析出学生身份 -> 明确提示
            return AjaxResult.error("学号未找到，无法开始考试");
        }

        // 反查补齐 studentNo/studentName，避免 DB 约束失败
        if (payload.getStudentNo() == null) {
            ClassStudent s2 = classStudentMapper.selectClassStudentById(studentId);
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
    public AjaxResult submitExam(@RequestBody Map<String, Object> payload) {
        // 1. 解析基本信息
        Long examId = payload.get("examId") != null ? Long.valueOf(String.valueOf(payload.get("examId"))) : null;
        if (examId == null) return AjaxResult.error("examId不能为空");

        String studentNo = (String) payload.get("studentNo");
        Long studentId = null;

        // 如果没有提供studentNo，使用当前登录用户的username
        if (studentNo == null || studentNo.trim().isEmpty()) {
            try {
                studentNo = getUsername();
                logger.info("Using logged-in username as studentNo for exam submission: {}", studentNo);
            } catch (Exception e) {
                logger.warn("Failed to get logged-in username", e);
            }
        }

        if (studentNo != null) {
            ClassStudent s = classStudentMapper.selectByStudentNo(studentNo);
            if (s != null) {
                studentId = s.getStudentId();
            } else {
                // 如果找不到class_student，尝试使用当前登录用户的userId
                try {
                    Long userId = getUserId();
                    studentId = userId;
                    logger.info("Class_student not found for studentNo {}, using userId {} as studentId", studentNo, userId);
                } catch (Exception e) {
                    logger.warn("Failed to get userId", e);
                }
            }
        }

        if (studentId == null) {
            try {
                studentId = getUserId();
                logger.info("Using logged-in userId as studentId: {}", studentId);
            } catch (Exception e) {
                logger.warn("Failed to get userId", e);
            }
        }

        // 2. 获取参与记录
        ClassExamParticipant existing = participantService.selectByExamStudent(examId, studentId);
        if (existing == null) return AjaxResult.error(HttpStatus.NOT_FOUND, "未开始考试或记录不存在");
        if (existing.getParticipantStatus() == 2) return AjaxResult.error("已提交，不能重复提交");

        // 3. 处理提交的答案数据
        List<Map<String, Object>> answers = (List<Map<String, Object>>) payload.get("answers");
        if (answers != null && !answers.isEmpty()) {
            // 获取考试题目列表，用于获取正确答案和分值
            ClassExamQuestion queryQuestion = new ClassExamQuestion();
            queryQuestion.setExamId(examId);
            List<ClassExamQuestion> questions = questionService.selectQuestionList(queryQuestion);
            Map<Long, ClassExamQuestion> questionMap = new HashMap<>();
            for (ClassExamQuestion q : questions) {
                questionMap.put(q.getId(), q);
            }

            // 保存每道题的答案
            for (Map<String, Object> answerData : answers) {
                Long questionId = answerData.get("questionId") != null ?
                        Long.valueOf(String.valueOf(answerData.get("questionId"))) : null;
                Object studentAnswerObj = answerData.get("answer");

                if (questionId == null) continue;

                // 转换答案为字符串
                String studentAnswer = null;
                if (studentAnswerObj != null) {
                    if (studentAnswerObj instanceof List) {
                        // 多选题答案是数组
                        studentAnswer = String.join(",", (List<String>) studentAnswerObj);
                    } else {
                        studentAnswer = String.valueOf(studentAnswerObj);
                    }
                }

                // 查找或创建答案记录
                ClassExamAnswer existingAnswer = answerService.selectByExamStudentQuestion(examId, studentId, questionId);
                ClassExamQuestion question = questionMap.get(questionId);

                if (existingAnswer == null) {
                    // 创建新答案记录
                    existingAnswer = new ClassExamAnswer();
                    existingAnswer.setExamId(examId);
                    existingAnswer.setQuestionId(questionId);
                    existingAnswer.setStudentId(studentId);
                    existingAnswer.setStudentNo(studentNo); // 添加学号
                }

                existingAnswer.setStudentAnswer(studentAnswer);

                // 如果有题目信息，保存正确答案
                if (question != null) {
                    existingAnswer.setCorrectAnswer(question.getCorrectAnswer());

                    // 自动判分（仅客观题：1=判断 2=选择）
                    Integer qType = question.getQuestionType();
                    if (qType != null && (qType == 1 || qType == 2)) {
                        // 客观题自动判分
                        autoJudgeObjective(existingAnswer);
                    }
                }

                // 保存或更新答案
                if (existingAnswer.getId() == null) {
                    answerService.insert(existingAnswer);
                } else {
                    answerService.update(existingAnswer);
                }
            }
        }

        // 4. 更新参与记录状态
        existing.setParticipantStatus(2);
        existing.setSubmitTime(new Date());
        if (existing.getStartTime() != null) {
            existing.setTimeUsed((int) ((existing.getSubmitTime().getTime() - existing.getStartTime().getTime()) / 1000));
        }

        // 5. 计算得分：汇总该考生本次考试的所有答题记录
        ClassExamAnswer queryAnswer = new ClassExamAnswer();
        queryAnswer.setExamId(examId);
        queryAnswer.setStudentId(studentId);
        List<ClassExamAnswer> answerList = answerService.selectList(queryAnswer);

        // 获取考试题目列表以判断题型
        ClassExamQuestion queryQuestion = new ClassExamQuestion();
        queryQuestion.setExamId(examId);
        List<ClassExamQuestion> questions = questionService.selectQuestionList(queryQuestion);
        java.util.Map<Long, ClassExamQuestion> qMap = new java.util.HashMap<>();
        boolean hasSubjectiveQuestions = false; // 是否包含主观题
        for (ClassExamQuestion q : questions) {
            qMap.put(q.getId(), q);
            Integer t = q.getQuestionType();
            // 新编码：3=简答题（主观题）
            if (t != null && t == 3) hasSubjectiveQuestions = true;
        }

        BigDecimal objectiveScore = BigDecimal.ZERO;  // 客观题得分
        BigDecimal subjectiveScore = BigDecimal.ZERO; // 主观题得分
        boolean hasUnscoredSubjective = false; // 是否有未批改的主观题

        for (ClassExamAnswer ans : answerList) {
            ClassExamQuestion q = qMap.get(ans.getQuestionId());
            if (q == null) continue;

            Integer qType = q.getQuestionType();
            BigDecimal ansScore = ans.getScore();
            if (ansScore == null) ansScore = BigDecimal.ZERO;

            // 新编码：1=判断 2=选择 -> 客观题；3=简答 -> 主观题
            if (qType != null && (qType == 1 || qType == 2)) {
                objectiveScore = objectiveScore.add(ansScore);
            } else if (qType != null && qType == 3) {
                if (ans.getCorrectorId() == null) {
                    hasUnscoredSubjective = true; // 未批改
                } else {
                    subjectiveScore = subjectiveScore.add(ansScore);
                }
            }
        }

        // 当考试没有主观题时：主观题得分=客观题得分
        if (!hasSubjectiveQuestions) {
            subjectiveScore = objectiveScore;
        }
        BigDecimal totalScore = hasSubjectiveQuestions ? objectiveScore.add(subjectiveScore) : objectiveScore;
        existing.setObjectiveScore(objectiveScore);
        existing.setSubjectiveScore(subjectiveScore);
        existing.setTotalScore(totalScore);

        // 判断批改状态：如果有未批改的主观题，状态为0；否则为1
        existing.setCorrectStatus(hasUnscoredSubjective ? 0 : 1);

        // 判断及格状态：仅在所有题目已批改完成或没有主观题时才判断
        ClassExam exam = examService.selectExamById(examId);
        if (exam != null && exam.getPassScore() != null && !hasUnscoredSubjective) {
            existing.setPassStatus(totalScore.compareTo(exam.getPassScore()) >= 0 ? 1 : 0);
        } else {
            existing.setPassStatus(0); // 未完成批改或无及格线，暂不及格
        }

        participantService.update(existing);

        // 构造返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("participantId", existing.getId());
        result.put("examId", existing.getExamId());
        result.put("participantStatus", existing.getParticipantStatus());
        result.put("submitTime", existing.getSubmitTime());

        // 如果有未批改的简答题，不返回分数，前端显示"待批改"
        if (hasUnscoredSubjective) {
            result.put("correctStatus", 0); // 待批改
            result.put("hasUnscoredSubjective", true);
            result.put("message", "已提交，等待批改");
        } else {
            // 无简答题或简答题已批改，返回总分
            result.put("correctStatus", 1); // 已批改
            result.put("totalScore", totalScore);
            result.put("objectiveScore", objectiveScore);
            result.put("subjectiveScore", subjectiveScore);
            result.put("passStatus", existing.getPassStatus());
            result.put("hasUnscoredSubjective", false);
        }

        return AjaxResult.success(result);
    }

    // ===== 答案记录 =====
    @PreAuthorize("@ss.hasPermi('projlwj:examAnswer:list')")
    @GetMapping("/answer/list")
    public AjaxResult answerList(ClassExamAnswer a) {
        // 权限检查：学生只能查看自己的答案
        try {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser != null && loginUser.getUser() != null) {
                String currentUserNo = loginUser.getUser().getUserName();

                // 如果请求中没有指定学号，默认设置为当前用户
                if (a != null && (a.getStudentNo() == null || a.getStudentNo().trim().isEmpty())) {
                    a.setStudentNo(currentUserNo);
                } else if (a != null && a.getStudentNo() != null) {
                    // 如果指定了学号，检查是否是本人或管理员/教师
                    if (!a.getStudentNo().equals(currentUserNo)) {
                        // 检查是否是管理员
                        if (!SecurityUtils.isAdmin(loginUser.getUserId())) {
                            // 不是管理员，只能查看自己的答案
                            return AjaxResult.error(403, "只能查看自己的答案");
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 如果获取用户信息失败，继续执行（保持向后兼容）
            e.printStackTrace();
        }

        // 支持以 studentNo 过滤：解析为 studentId
        if (a != null && a.getStudentId() == null && a.getStudentNo() != null) {
            ClassStudent s = classStudentMapper.selectByStudentNo(a.getStudentNo());
            if (s != null) { a.setStudentId(s.getStudentId()); }
        }
        List<ClassExamAnswer> list = answerService.selectList(a);

        // 补充提交时间和姓名：若带有 examId 参数，则按参与者映射补齐 submitTime、studentName
        try {
            Long examId = a != null ? a.getExamId() : null;
            if (examId != null) {
                ClassExamParticipant pf = new ClassExamParticipant();
                pf.setExamId(examId);
                List<ClassExamParticipant> parts = participantService.selectList(pf);
                Map<Long, ClassExamParticipant> partByStuId = new HashMap<>();
                if (parts != null) {
                    for (ClassExamParticipant p : parts) {
                        if (p.getStudentId() != null) partByStuId.put(p.getStudentId(), p);
                    }
                }
                for (ClassExamAnswer ans : list) {
                    ClassExamParticipant p = ans.getStudentId() == null ? null : partByStuId.get(ans.getStudentId());
                    if (p != null) {
                        // 统一用参与记录的提交时间
                        if (ans.getSubmitTime() == null) ans.setSubmitTime(p.getSubmitTime());
                        // 补充姓名
                        if (ans.getStudentName() == null || ans.getStudentName().trim().isEmpty()) {
                            ans.setStudentName(p.getStudentName());
                        }
                        // 补充学号
                        if ((ans.getStudentNo() == null || ans.getStudentNo().trim().isEmpty()) && p.getStudentNo() != null) {
                            ans.setStudentNo(p.getStudentNo());
                        }
                    }
                }
            }
        } catch (Exception ignore) {}

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
            // 新编码：1=判断 2=选择(客观题) 3=简答(主观题)
            // 仅客观题自动判分；简答题(3)不做自动判定，保持 score / isCorrect 为空，等待教师批改
            if (qType != null && (qType == 1 || qType == 2)) {
                String canonCorrect = canonicalAnswerForCompare(qType, a.getCorrectAnswer());
                String canonStudent = canonicalAnswerForCompare(qType, a.getStudentAnswer());
                boolean correct = canonCorrect != null && canonCorrect.equalsIgnoreCase(canonStudent);
                if (correct) {
                    a.setIsCorrect(1);
                    a.setScore(qScore != null ? qScore : BigDecimal.ONE);
                } else {
                    a.setIsCorrect(0);
                    a.setScore(BigDecimal.ZERO);
                }
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
        if (answers == null) answers = java.util.Collections.emptyList();

        ClassExamQuestion qFilter = new ClassExamQuestion();
        qFilter.setExamId(examId);
        List<ClassExamQuestion> qList = questionService.selectQuestionList(qFilter);
        boolean hasSubjectiveQuestions = false;
        java.util.Map<Long, ClassExamQuestion> qMap = new java.util.HashMap<>();
        for (ClassExamQuestion q : qList) {
            qMap.put(q.getId(), q);
            Integer t = q.getQuestionType();
            // 新编码：3=简答题（主观题）
            if (t != null && t == 3) hasSubjectiveQuestions = true;
        }
        java.math.BigDecimal obj = java.math.BigDecimal.ZERO;
        java.math.BigDecimal subj = java.math.BigDecimal.ZERO;
        boolean hasUnscoredSubjective = false;
        for (ClassExamAnswer ans : answers) {
            ClassExamQuestion q = qMap.get(ans.getQuestionId());
            java.math.BigDecimal s = ans.getScore() == null ? java.math.BigDecimal.ZERO : ans.getScore();
            if (q != null) {
                Integer t = q.getQuestionType();
                // 新编码：1=判断 2=选择（客观题）
                if (t != null && (t == 1 || t == 2)) {
                    obj = obj.add(s);
                } else if (t != null && t == 3) {
                    // 简答题（主观题）：correctorId 为空即视为未批改
                    if (ans.getCorrectorId() == null) {
                        hasUnscoredSubjective = true;
                    } else {
                        subj = subj.add(s);
                    }
                }
            }
        }
        if (!hasSubjectiveQuestions) subj = obj; // 无主观题时主观题得分=客观题得分
        part.setObjectiveScore(obj);
        part.setSubjectiveScore(subj);
        part.setTotalScore(hasSubjectiveQuestions ? obj.add(subj) : obj);
        part.setCorrectStatus(hasUnscoredSubjective ? 0 : 1);
        ClassExam ex = examService.selectExamById(examId);
        if (ex != null && ex.getPassScore() != null && !hasUnscoredSubjective) {
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
            ClassStudent stu = classStudentMapper.selectClassStudentById(studentId);
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
                Integer t = q.getQuestionType();
                // 新编码：1=判断 2=选择(客观题) 3=简答(主观题)
                // 仅客观题自动判分；主观题强制不保存分数，避免误判满分
                if (t != null && (t == 1 || t == 2)) {
                    if (payload.getScore() == null && payload.getStudentAnswer() != null && payload.getCorrectAnswer() != null) {
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
                } else if (t != null && t == 3) {
                    // 简答题（主观题）：如果前端误传了分数，清除，等待教师批改
                    payload.setScore(null);
                    payload.setIsCorrect(null);
                }
            }
        } else {
            // 如果快照完整但题型为主观题仍需确保不提前赋分
            try {
                ClassExamQuestion qTypeCheck = questionService.selectById(payload.getQuestionId());
                if (qTypeCheck != null) {
                    Integer t2 = qTypeCheck.getQuestionType();
                    // 简答题且未批改时，清除分数
                    if (t2 != null && t2 == 3 && payload.getCorrectorId() == null) {
                        payload.setScore(null);
                        payload.setIsCorrect(null);
                    }
                }
            } catch (Exception ignore) {}
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
        // 根据题型重新判定是否正确（新编码：1=判断 2=选择 3=简答）
        // 主观题：满分视为正确；客观题：分数==题目分值视为正确
        try {
            if (exist.getQuestionId() != null) {
                ClassExamQuestion q = questionService.selectById(exist.getQuestionId());
                if (q != null && q.getScore() != null && exist.getScore() != null) {
                    Integer t = q.getQuestionType();
                    boolean subjective = (t != null) && (t == 3); // 简答题是主观题
                    boolean fullScore = exist.getScore().compareTo(q.getScore()) == 0;
                    if (subjective) {
                        exist.setIsCorrect(fullScore ? 1 : 0);
                    } else if (t != null && (t == 1 || t == 2)) { // 判断和选择是客观题
                        exist.setIsCorrect(fullScore ? 1 : 0);
                    }
                }
            }
        } catch (Exception ignore) {}
        answerService.update(exist);
        refreshParticipantScores(exist.getExamId(), exist.getStudentId());
        return AjaxResult.success(exist);
    }

    @PreAuthorize("@ss.hasPermi('projlwj:examAnswer:grade')")
    @Log(title = "批改答案(批量)", businessType = BusinessType.UPDATE)
    @PostMapping("/answer/gradeBatch")
    public AjaxResult gradeBatch(@RequestBody List<ClassExamAnswer> payload) {
        if (payload == null || payload.isEmpty()) return AjaxResult.success("无数据");
        int success = 0;
        Set<String> affected = new HashSet<>(); // examId:studentId 组合
        for (ClassExamAnswer a : payload) {
            if (a.getId() == null) continue;
            try {
                ClassExamAnswer exist = answerService.selectById(a.getId());
                if (exist == null) continue;
                exist.setScore(a.getScore());
                exist.setCorrectComment(a.getCorrectComment());
                exist.setCorrectorId(getUserId());
                exist.setCorrectTime(new Date());
                exist.setUpdateBy(getUsername());
                // 判定正确
                try {
                    if (exist.getQuestionId() != null) {
                        ClassExamQuestion q = questionService.selectById(exist.getQuestionId());
                        if (q != null && q.getScore() != null && exist.getScore() != null) {
                            Integer t = q.getQuestionType();
                            boolean subjective = (t != null) && (t == 4 || t == 5 || t == 6);
                            boolean fullScore = exist.getScore().compareTo(q.getScore()) == 0;
                            if (subjective || (t != null && (t == 1 || t == 2 || t == 3))) {
                                exist.setIsCorrect(fullScore ? 1 : 0);
                            }
                        }
                    }
                } catch (Exception ignore) {}
                answerService.update(exist);
                success++;
                if (exist.getExamId() != null && exist.getStudentId() != null) {
                    affected.add(exist.getExamId() + ":" + exist.getStudentId());
                }
            } catch (Exception ignore) {}
        }
        // 刷新分数
        for (String key : affected) {
            String[] parts = key.split(":");
            Long examId = Long.valueOf(parts[0]);
            Long studentId = Long.valueOf(parts[1]);
            refreshParticipantScores(examId, studentId);
        }
        return AjaxResult.success("批量成功:" + success);
    }

    // ===== 题目正确情况统计 =====
    @GetMapping("/{examId}/questionCorrectSummary")
    public AjaxResult questionCorrectSummary(@PathVariable Long examId) {
        ClassExam exam = examService.selectExamById(examId);
        if (exam == null) return AjaxResult.error(HttpStatus.NOT_FOUND, "考试不存在");
        // 题目列表
        ClassExamQuestion qFilter = new ClassExamQuestion();
        qFilter.setExamId(examId);
        List<ClassExamQuestion> questions = questionService.selectQuestionList(qFilter);
        // 答案分组
        ClassExamAnswer aFilter = new ClassExamAnswer();
        aFilter.setExamId(examId);
        List<ClassExamAnswer> answers = answerService.selectList(aFilter);
        Map<Long, List<ClassExamAnswer>> answerGroup = new HashMap<>();
        if (answers != null) {
            for (ClassExamAnswer a : answers) {
                if (a.getQuestionId() == null) continue;
                answerGroup.computeIfAbsent(a.getQuestionId(), k -> new ArrayList<>()).add(a);
            }
        }
        // ===== 课堂学生集合（统计基准） =====
        Set<Long> sessionIds = new LinkedHashSet<>();
        if (exam.getSessionId() != null) sessionIds.add(exam.getSessionId());
        try {
            List<ClassExamSession> extraSessions = examSessionService.selectByExamId(examId);
            if (extraSessions != null) {
                for (ClassExamSession es : extraSessions) {
                    if (es.getSessionId() != null) sessionIds.add(es.getSessionId());
                }
            }
        } catch (Exception ignore) {}
        Map<Long, Map<String, Object>> studentBaseMap = new LinkedHashMap<>(); // studentId -> info
        for (Long sid : sessionIds) {
            try {
                List<ClassStudent> sessionStudents = classStudentMapper.selectBySessionId(sid);
                if (sessionStudents != null) {
                    for (ClassStudent stu : sessionStudents) {
                        if (stu.getStudentId() == null) continue;
                        // 避免重复课堂的同一个学生重复计数
                        if (!studentBaseMap.containsKey(stu.getStudentId())) {
                            Map<String,Object> info = new LinkedHashMap<>();
                            info.put("studentId", stu.getStudentId());
                            info.put("studentNo", stu.getStudentNo());
                            info.put("studentName", stu.getStudentName());
                            info.put("sessionId", sid);
                            studentBaseMap.put(stu.getStudentId(), info);
                        }
                    }
                }
            } catch (Exception e) {
                // 单个课堂查询失败不影响整体，继续
            }
        }
        // 如果课堂没有学生（异常或未建立关联），退化为参与者集合
        if (studentBaseMap.isEmpty()) {
            ClassExamParticipant pFilter = new ClassExamParticipant();
            pFilter.setExamId(examId);
            List<ClassExamParticipant> participants = participantService.selectList(pFilter);
            if (participants != null) {
                for (ClassExamParticipant p : participants) {
                    if (p.getStudentId() == null) continue;
                    if (!studentBaseMap.containsKey(p.getStudentId())) {
                        Map<String,Object> info = new LinkedHashMap<>();
                        info.put("studentId", p.getStudentId());
                        info.put("studentNo", p.getStudentNo());
                        info.put("studentName", p.getStudentName());
                        info.put("sessionId", exam.getSessionId());
                        studentBaseMap.put(p.getStudentId(), info);
                    }
                }
            }
        }
        // 额外回退：若仍然没有学生（例如课堂与学生关联未建立），尝试按课程查找去重学生
        if (studentBaseMap.isEmpty() && exam.getCourseId() != null) {
            try {
                List<ClassStudent> courseStudents = classStudentMapper.selectDistinctStudentsByCourseId(exam.getCourseId());
                if (courseStudents != null) {
                    for (ClassStudent stu : courseStudents) {
                        if (stu.getStudentId() == null) continue;
                        if (!studentBaseMap.containsKey(stu.getStudentId())) {
                            Map<String,Object> info = new LinkedHashMap<>();
                            info.put("studentId", stu.getStudentId());
                            info.put("studentNo", stu.getStudentNo());
                            info.put("studentName", stu.getStudentName());
                            info.put("sessionId", exam.getSessionId());
                            studentBaseMap.put(stu.getStudentId(), info);
                        }
                    }
                }
            } catch (Exception ignore) {}
        }
        int baseStudentCount = studentBaseMap.size();
        // 参与者（作答过任一题或有参与记录的学生）映射：用于补齐姓名
        Map<Long, ClassExamParticipant> participantMap = new HashMap<>();
        try {
            ClassExamParticipant partFilter = new ClassExamParticipant();
            partFilter.setExamId(examId);
            List<ClassExamParticipant> participants = participantService.selectList(partFilter);
            if (participants != null) {
                for (ClassExamParticipant p : participants) {
                    if (p.getStudentId() != null) participantMap.put(p.getStudentId(), p);
                }
            }
        } catch (Exception ignore) {}
        List<Map<String, Object>> result = new ArrayList<>();
        boolean hasSubjective = false;
        for (ClassExamQuestion q : questions) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("questionId", q.getId());
            row.put("questionType", q.getQuestionType());
            row.put("questionContent", q.getQuestionContent());
            row.put("score", q.getScore());
            row.put("correctAnswer", q.getCorrectAnswer());
            Integer qt = q.getQuestionType();
            // 新编码：3=简答题（主观题）
            boolean subjective = qt != null && qt == 3;
            if (subjective) hasSubjective = true;
            List<ClassExamAnswer> list = answerGroup.getOrDefault(q.getId(), Collections.emptyList());
            List<Map<String, Object>> correctStudents = new ArrayList<>();
            List<Map<String, Object>> incorrectStudents = new ArrayList<>();
            List<Map<String, Object>> ungradedStudents = new ArrayList<>();
            Set<Long> answeredStudentIds = new HashSet<>();
            for (ClassExamAnswer a : list) {
                if (a.getStudentId() == null) continue;
                answeredStudentIds.add(a.getStudentId());
                Map<String,Object> stuInfo = new LinkedHashMap<>();
                Map<String,Object> baseInfo = studentBaseMap.get(a.getStudentId());
                ClassExamParticipant part = participantMap.get(a.getStudentId());
                stuInfo.put("studentId", a.getStudentId());
                stuInfo.put("studentNo", a.getStudentNo() != null ? a.getStudentNo() : (baseInfo != null ? baseInfo.get("studentNo") : null));
                stuInfo.put("studentName", part != null ? part.getStudentName() : (baseInfo != null ? baseInfo.get("studentName") : null));
                if (subjective) {
                    // correctorId 为空即待批改
                    if (a.getCorrectorId() == null) {
                        ungradedStudents.add(stuInfo);
                    } else if (q.getScore() != null && a.getScore() != null && a.getScore().compareTo(q.getScore()) == 0) {
                        correctStudents.add(stuInfo);
                    } else {
                        incorrectStudents.add(stuInfo);
                    }
                } else {
                    boolean isCorrect = a.getIsCorrect() != null && a.getIsCorrect() == 1;
                    if (isCorrect) correctStudents.add(stuInfo); else incorrectStudents.add(stuInfo);
                }
            }
            // 未答：课堂基准学生中不在 answeredStudentIds 的集合
            List<Map<String,Object>> unansweredStudents = new ArrayList<>();
            for (Map.Entry<Long, Map<String,Object>> entry : studentBaseMap.entrySet()) {
                Long sid = entry.getKey();
                if (!answeredStudentIds.contains(sid)) {
                    unansweredStudents.add(entry.getValue());
                }
            }
            int correctCount = correctStudents.size();
            int incorrectCount = incorrectStudents.size();
            int ungradedCount = ungradedStudents.size();
            int unansweredCount = unansweredStudents.size();
            double correctRate = baseStudentCount == 0 ? 0.0 : (correctCount * 100.0 / baseStudentCount);
            row.put("participantsCount", baseStudentCount);
            row.put("correctCount", correctCount);
            row.put("incorrectCount", incorrectCount);
            row.put("ungradedCount", ungradedCount);
            row.put("unansweredCount", unansweredCount);
            row.put("correctRate", correctRate);
            row.put("correctStudents", correctStudents);
            row.put("incorrectStudents", incorrectStudents);
            row.put("ungradedStudents", ungradedStudents);
            row.put("unansweredStudents", unansweredStudents);
            result.add(row);
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("examId", examId);
        payload.put("examName", exam.getExamName());
        payload.put("hasSubjective", hasSubjective);
        payload.put("totalQuestions", questions == null ? 0 : questions.size());
        payload.put("questions", result);
        payload.put("participantsCount", baseStudentCount);
        // 调试信息（可用于前端或日志）：返回参与统计/课堂ID列表大小，帮助定位无数据原因
        Map<String,Object> _debug = new LinkedHashMap<>();
        _debug.put("sessionIdsCount", sessionIds.size());
        _debug.put("studentBaseCount", studentBaseMap.size());
        _debug.put("answerCount", answers == null ? 0 : answers.size());
        _debug.put("questionCount", questions == null ? 0 : questions.size());
        payload.put("_debug", _debug);
        // 新增：课堂明细统计
        List<Map<String, Object>> sessionDetails = new ArrayList<>();
        for (Long sid : sessionIds) {
            try {
                List<ClassStudent> sessionStudents = classStudentMapper.selectBySessionId(sid);
                if (sessionStudents != null && !sessionStudents.isEmpty()) {
                    Map<String, Object> sessionInfo = new LinkedHashMap<>();
                    sessionInfo.put("sessionId", sid);
                    // 尝试获取课堂名称（可以从exam或其他地方获取）
                    String sessionName = "课堂" + sid;
                    // TODO: 如果有session service可以查询课堂名称，这里补充
                    sessionInfo.put("sessionName", sessionName);
                    sessionInfo.put("studentCount", sessionStudents.size());
                    sessionDetails.add(sessionInfo);
                }
            } catch (Exception ignore) {}
        }
        payload.put("sessionDetails", sessionDetails);
        payload.put("sessionCount", sessionDetails.size());

        return AjaxResult.success(payload);
    }


    @GetMapping("/{examId}/ungraded")
    public AjaxResult ungraded(@PathVariable Long examId) {
        // 获取考试题目，筛选出主观题ID集合（新编码：3=简答）
        ClassExamQuestion qFilter = new ClassExamQuestion();
        qFilter.setExamId(examId);
        List<ClassExamQuestion> questions = questionService.selectQuestionList(qFilter);
        Set<Long> subjectiveIds = new HashSet<>();
        for (ClassExamQuestion q : questions) {
            Integer t = q.getQuestionType();
            if (t != null && t == 3) { // 3=简答题（主观题）
                subjectiveIds.add(q.getId());
            }
        }
        if (subjectiveIds.isEmpty()) {
            return AjaxResult.success(Collections.emptyList());
        }
        // 拉取该考试全部主观题答案
        ClassExamAnswer aFilter = new ClassExamAnswer();
        aFilter.setExamId(examId);
        List<ClassExamAnswer> allAnswers = answerService.selectList(aFilter);
        List<ClassExamAnswer> ungraded = new ArrayList<>();
        for (ClassExamAnswer a : allAnswers) {
            if (!subjectiveIds.contains(a.getQuestionId())) continue; // 仅主观题
            // 未批改：score 为 null 或 (score=0 且 correctorId 为空)
            if (a.getScore() == null || (a.getScore().compareTo(BigDecimal.ZERO) == 0 && a.getCorrectorId() == null)) {
                ungraded.add(a);
            }
        }
        return AjaxResult.success(ungraded);
    }
}

