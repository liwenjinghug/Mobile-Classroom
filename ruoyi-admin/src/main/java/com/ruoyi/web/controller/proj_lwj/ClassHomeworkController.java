package com.ruoyi.web.controller.proj_lwj;

// [修改] 引入自定义的 Log 注解
import com.ruoyi.proj_cyq.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.proj_lwj.domain.ClassHomework;
import com.ruoyi.proj_lwj.domain.ClassStudentHomework;
import com.ruoyi.proj_lwj.service.IClassHomeworkService;
import com.ruoyi.proj_lwj.service.IClassStudentHomeworkService;
import com.ruoyi.proj_lwj.mapper.ClassStudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/proj_lwj/homework")
public class ClassHomeworkController extends BaseController {

    @Autowired
    private IClassHomeworkService homeworkService;

    @Autowired
    private IClassStudentHomeworkService studentHomeworkService;

    @Autowired
    private ClassStudentMapper classStudentMapper;

    /**
     * 查询作业列表
     * 修改点：添加了 or @ss.hasPermi('projlwj:homework:submit')
     * 允许拥有“提交权限”的用户（学生）也能查看作业列表
     */
    @PreAuthorize("@ss.hasPermi('projlwj:homework:list') or @ss.hasPermi('projlwj:homework:submit')")
    @GetMapping("/list")
    public TableDataInfo list(ClassHomework hw) {
        startPage();
        // 建议：此处未来可添加数据隔离逻辑，例如：如果用户是学生，强制设置 hw.setSessionId(...) 只查自己班级的作业
        java.util.List<ClassHomework> list = homeworkService.selectHomeworkList(hw);
        return getDataTable(list);
    }

    /**
     * 获取作业详细信息
     * 修改点：添加了 or @ss.hasPermi('projlwj:homework:submit')
     * 允许拥有“提交权限”的用户（学生）也能查看作业详情
     */
    @PreAuthorize("@ss.hasPermi('projlwj:homework:query') or @ss.hasPermi('projlwj:homework:submit')")
    @GetMapping("/{homeworkId}")
    public AjaxResult getInfo(@PathVariable Long homeworkId) {
        return AjaxResult.success(homeworkService.selectHomeworkById(homeworkId));
    }

    /**
     * 新增作业
     */
    @PreAuthorize("@ss.hasPermi('projlwj:homework:add')")
    @Log(title = "作业管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody com.ruoyi.proj_lwj.domain.ClassHomework hw) {
        hw.setCreateBy(getUsername());
        try {
            logger.info("添加作业请求 payload: {}", hw);

            if (hw.getSessionId() != null && hw.getTitle() != null && !hw.getTitle().trim().isEmpty()) {
                boolean exists = homeworkService.existsBySessionAndTitle(hw.getSessionId(), hw.getTitle(), null);
                if (exists) {
                    return AjaxResult.error("当前课堂已存在相同标题的作业，请修改标题后重试");
                }
            }

            int r = homeworkService.addHomework(hw);
            return toAjax(r);
        } catch (Exception e) {
            logger.error("添加作业失败", e);
            return AjaxResult.error("发布作业失败：" + e.getMessage());
        }
    }

    /**
     * 修改作业
     */
    @PreAuthorize("@ss.hasPermi('projlwj:homework:edit')")
    @Log(title = "作业管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody com.ruoyi.proj_lwj.domain.ClassHomework hw) {
        hw.setUpdateBy(getUsername());

        if (hw.getSessionId() != null && hw.getTitle() != null && !hw.getTitle().trim().isEmpty()) {
            boolean exists = homeworkService.existsBySessionAndTitle(hw.getSessionId(), hw.getTitle(), hw.getHomeworkId());
            if (exists) {
                return AjaxResult.error("当前课堂已存在相同标题的作业，请修改标题后重试");
            }
        }

        return toAjax(homeworkService.editHomework(hw));
    }

    /**
     * 删除作业
     */
    @PreAuthorize("@ss.hasPermi('projlwj:homework:remove')")
    @Log(title = "作业管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids, @RequestParam(required = false, defaultValue = "false") boolean cascade) {
        try {
            try {
                studentHomeworkService.deleteByHomeworkIds(ids);
            } catch (Exception ex) {
                logger.warn("Cascade delete of student submissions failed (non-fatal)", ex);
            }
        } catch (Exception ex) {
            logger.warn("Cascade delete of student submissions failed", ex);
        }
        return toAjax(homeworkService.removeHomeworkByIds(ids));
    }

    /**
     * 学生提交作业
     */
    @PreAuthorize("@ss.hasPermi('projlwj:homework:submit')")
    @Log(title = "作业提交", businessType = BusinessType.INSERT)
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody ClassStudentHomework shw) {
        shw.setCreateBy(getUsername());

        if (shw.getHomeworkId() == null) {
            return AjaxResult.error(HttpStatus.BAD_REQUEST, "提交失败：未指定作业ID");
        }
        ClassHomework targetHw = homeworkService.selectHomeworkById(shw.getHomeworkId());
        if (targetHw == null) {
            return AjaxResult.error(HttpStatus.NOT_FOUND, "该作业已被删除或不存在，无法提交");
        }

        if (shw.getStudentNo() != null && !shw.getStudentNo().trim().isEmpty()) {
            String sn = shw.getStudentNo().trim();
            com.ruoyi.proj_lwj.domain.ClassStudent cs = classStudentMapper.selectByStudentNo(sn);
            if (cs == null) {
                return AjaxResult.error("学号 " + sn + " 未找到对应的学生，请检查后重试。");
            }
            shw.setStudentId(cs.getStudentId());
            shw.setStudentNo(cs.getStudentNo());
        }

        if (shw.getStudentId() == null || shw.getStudentId() == 0L) {
            shw.setStudentId(getUserId());
        }

        try {
            List<ClassStudentHomework> existing = studentHomeworkService.selectByStudentId(shw.getStudentId());
            if (existing != null && !existing.isEmpty()) {
                for (ClassStudentHomework e : existing) {
                    if (e.getHomeworkId() != null && e.getHomeworkId().equals(shw.getHomeworkId())) {
                        if ((e.getIsGraded() != null && e.getIsGraded() == 1) || (e.getStatus() != null && String.valueOf(e.getStatus()).equals("2"))) {
                            return AjaxResult.error("该作业已被教师批改，无法再次提交或修改");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.warn("检查已存在提交时出错", ex);
        }

        if (shw.getSubmitTime() == null) {
            shw.setSubmitTime(new java.util.Date());
        }
        if (shw.getStatus() == null) {
            shw.setStatus(1);
        }

        logger.info("submit - ClassStudentHomework payload before insert: {}", shw);
        int r = studentHomeworkService.insert(shw);
        return toAjax(r);
    }

    /**
     * 更新提交
     */
    @PreAuthorize("@ss.hasPermi('projlwj:homework:submit')")
    @Log(title = "作业提交", businessType = BusinessType.UPDATE)
    @PutMapping("/submit")
    public AjaxResult updateSubmit(@RequestBody ClassStudentHomework shw) {
        if (shw.getStudentHomeworkId() == null) {
            return AjaxResult.error(HttpStatus.BAD_REQUEST, "studentHomeworkId 不能为空，无法更新提交记录");
        }

        if (shw.getStudentNo() != null && !shw.getStudentNo().trim().isEmpty()) {
            String sn = shw.getStudentNo().trim();
            com.ruoyi.proj_lwj.domain.ClassStudent cs = classStudentMapper.selectByStudentNo(sn);
            if (cs == null) {
                return AjaxResult.error(HttpStatus.NOT_FOUND, "学号 " + sn + " 未找到对应的学生，请检查后重试。");
            }
            shw.setStudentId(cs.getStudentId());
            shw.setStudentNo(cs.getStudentNo());
        }
        logger.info("updateSubmit - ClassStudentHomework payload before update: {}", shw);

        try {
            ClassStudentHomework exist = studentHomeworkService.selectById(shw.getStudentHomeworkId());
            if (exist != null) {
                Long hwId = (shw.getHomeworkId() != null) ? shw.getHomeworkId() : exist.getHomeworkId();
                if (hwId == null || homeworkService.selectHomeworkById(hwId) == null) {
                    return AjaxResult.error(HttpStatus.NOT_FOUND, "该作业已被删除，无法修改提交记录");
                }
                if ((exist.getIsGraded() != null && exist.getIsGraded() == 1) || (exist.getStatus() != null && String.valueOf(exist.getStatus()).equals("2"))) {
                    return AjaxResult.error("该提交已被教师批改，无法修改");
                }
            }
        } catch (Exception ex) {
            logger.warn("检查提交是否可编辑时出错", ex);
        }

        shw.setUpdateBy(getUsername());
        if (shw.getSubmitTime() == null) {
            shw.setSubmitTime(new java.util.Date());
        }
        int r = studentHomeworkService.update(shw);
        return toAjax(r);
    }

    /**
     * 教师批改
     */
    @PreAuthorize("@ss.hasPermi('projlwj:homework:grade')")
    @Log(title = "作业批改", businessType = BusinessType.UPDATE)
    @PutMapping("/grade")
    public AjaxResult gradeSubmission(@RequestBody ClassStudentHomework shw) {
        if (shw.getStudentHomeworkId() == null) {
            return AjaxResult.error(HttpStatus.BAD_REQUEST, "studentHomeworkId 不能为空，无法批改");
        }

        ClassStudentHomework exist = studentHomeworkService.selectById(shw.getStudentHomeworkId());
        if (exist == null) {
            return AjaxResult.error(HttpStatus.NOT_FOUND, "提交记录不存在，无法批改");
        }

        if (shw.getCorrectedBy() == null) {
            shw.setCorrectedBy(getUserId());
        }
        if (shw.getCorrectedTime() == null) {
            shw.setCorrectedTime(new java.util.Date());
        }
        shw.setIsGraded(1);
        shw.setStatus(2);
        shw.setUpdateBy(getUsername());

        int r = studentHomeworkService.updateGrade(shw);
        return toAjax(r);
    }

    @PreAuthorize("@ss.hasPermi('projlwj:homework:grade')")
    @GetMapping("/submissions/{homeworkId}")
    public AjaxResult submissions(@PathVariable Long homeworkId) {
        List<ClassStudentHomework> list = studentHomeworkService.selectByHomeworkId(homeworkId);
        ClassHomework hw = homeworkService.selectHomeworkById(homeworkId);
        boolean deleted = (hw == null);
        for (ClassStudentHomework sh : list) {
            sh.setHomeworkDeleted(deleted);
            if (deleted) {
                sh.setHomeworkTitle("该作业已被老师删除");
            } else if (sh.getHomeworkTitle() == null || sh.getHomeworkTitle().trim().isEmpty()) {
                sh.setHomeworkTitle(hw.getTitle());
            }
        }
        ensureStudentNames(list);
        resolveSubmissionFilePaths(list);
        return AjaxResult.success(list);
    }

    /**
     * 学生查看自己的提交记录
     * 修改点：添加了 or @ss.hasPermi('projlwj:homework:submit')
     * 确保学生在前端查看自己历史记录时不会报错
     */
    @PreAuthorize("@ss.hasPermi('projlwj:homework:query') or @ss.hasPermi('projlwj:homework:submit')")
    @GetMapping("/studentSubmissions")
    public AjaxResult studentSubmissions(@RequestParam Long studentId) {
        List<ClassStudentHomework> list = studentHomeworkService.selectByStudentId(studentId);
        for (ClassStudentHomework sh : list) {
            Long hwId = sh.getHomeworkId();
            if (hwId == null) {
                sh.setHomeworkDeleted(true);
                sh.setHomeworkTitle("该作业已被老师删除");
                continue;
            }
            ClassHomework hw = homeworkService.selectHomeworkById(hwId);
            if (hw == null) {
                sh.setHomeworkDeleted(true);
                sh.setHomeworkTitle("该作业已被老师删除");
            } else {
                sh.setHomeworkDeleted(false);
                if (sh.getHomeworkTitle() == null || sh.getHomeworkTitle().trim().isEmpty()) sh.setHomeworkTitle(hw.getTitle());
            }
        }
        list = filterOutDeletedHomework(list);
        ensureStudentNames(list);
        resolveSubmissionFilePaths(list);
        return AjaxResult.success(list);
    }

    /**
     * 获取当前登录用户的作业提交记录（基于登录账号，无需学号）
     */
    @GetMapping("/my-submissions")
    public AjaxResult getMySubmissions() {
        Long userId = getUserId();
        String username = getUsername();

        logger.info("getMySubmissions called for userId={}, username={}", userId, username);

        // 先通过username查找class_student表获取student_id
        Long classStudentId = null;
        try {
            com.ruoyi.proj_lwj.domain.ClassStudent cs = classStudentMapper.selectByStudentNo(username);
            if (cs != null && cs.getStudentId() != null) {
                classStudentId = cs.getStudentId();
                logger.info("Found class_student.student_id={} for username={}", classStudentId, username);
            }
        } catch (Exception e) {
            logger.warn("Failed to find class_student by username", e);
        }

        // 使用class_student_id或userId查询
        Long finalStudentId = classStudentId != null ? classStudentId : userId;
        List<ClassStudentHomework> list = studentHomeworkService.selectByStudentId(finalStudentId);

        // 如果没找到，再尝试用username作为studentNo查询
        if ((list == null || list.isEmpty()) && username != null) {
            try {
                list = studentHomeworkService.selectByStudentNo(username);
            } catch (Exception ex) {
                logger.warn("fallback selectByStudentNo failed", ex);
            }
        }

        if (list == null) {
            list = new ArrayList<>();
        }

        list = filterOutDeletedHomework(list);
        ensureStudentNames(list);
        resolveSubmissionFilePaths(list);
        return AjaxResult.success(list);
    }

    @GetMapping("/studentSubmissions/public")
    public AjaxResult publicStudentSubmissions(@RequestParam(required = false) Long studentId, @RequestParam(required = false) String studentNo, @RequestParam(required = false) String studentName) {
        logger.info("publicStudentSubmissions called with studentId={}, studentNo={}, studentName={}", studentId, studentNo, studentName);
        Long sid = studentId;
        if ((sid == null || sid == 0L) && studentNo != null && !studentNo.trim().isEmpty()) {
            com.ruoyi.proj_lwj.domain.ClassStudent cs = classStudentMapper.selectByStudentNo(studentNo.trim());
            if (cs != null) sid = cs.getStudentId();
        }
        if (sid != null) {
            List<ClassStudentHomework> list = studentHomeworkService.selectByStudentId(sid);
            list = filterOutDeletedHomework(list);
            ensureStudentNames(list);
            resolveSubmissionFilePaths(list);
            return AjaxResult.success(list);
        }

        if (studentNo != null && !studentNo.trim().isEmpty()) {
            try {
                List<ClassStudentHomework> list = studentHomeworkService.selectByStudentNo(studentNo.trim());
                if (list != null && !list.isEmpty()) {
                    list = filterOutDeletedHomework(list);
                    ensureStudentNames(list);
                    resolveSubmissionFilePaths(list);
                    return AjaxResult.success(list);
                }
            } catch (Exception ex) {
                logger.warn("fallback selectByStudentNo failed", ex);
            }
        }

        if (studentName != null && !studentName.trim().isEmpty()) {
            try {
                List<ClassStudentHomework> list = studentHomeworkService.selectByStudentIdentifier(studentName.trim());
                if (list != null && !list.isEmpty()) {
                    list = filterOutDeletedHomework(list);
                    ensureStudentNames(list);
                    resolveSubmissionFilePaths(list);
                    return AjaxResult.success(list);
                }
            } catch (Exception ex) {
                logger.warn("fallback selectByStudentIdentifier failed", ex);
            }
        }

        return AjaxResult.error("请提供 studentId 或 studentNo 以查询学生提交记录（或确认学号/姓名是否正确）");
    }

    @PreAuthorize("@ss.hasPermi('projlwj:homework:submit') or @ss.hasPermi('projlwj:homework:grade')")
    @Log(title = "作业提交", businessType = BusinessType.DELETE)
    @DeleteMapping("/submission/{id}")
    public AjaxResult deleteSubmission(@PathVariable Long id) {
        try {
            int r = studentHomeworkService.deleteById(id);
            return toAjax(r);
        } catch (Exception e) {
            logger.error("删除提交失败", e);
            return AjaxResult.error("删除提交失败：" + e.getMessage());
        }
    }

    private void resolveSubmissionFilePaths(List<ClassStudentHomework> list) {
        if (list == null || list.isEmpty()) return;
        String profile = RuoYiConfig.getProfile();
        if (profile == null) profile = "";
        Path profilePath = Paths.get(profile).toAbsolutePath();
        Path uploadBase = profilePath.resolve("upload");
        String resourcePrefix = Constants.RESOURCE_PREFIX;
        final DateTimeFormatter DATE_PATH_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        for (ClassStudentHomework sh : list) {
            String submission = sh.getSubmissionFiles();
            if (submission == null || submission.trim().isEmpty()) continue;
            String[] parts = submission.split(",");
            List<String> resolved = new ArrayList<>();
            for (String raw : parts) {
                String name = raw == null ? "" : raw.trim();
                if (name.isEmpty()) continue;
                if (name.startsWith(resourcePrefix + "/") || name.startsWith("http://") || name.startsWith("https://")) {
                    resolved.add(name);
                    continue;
                }

                String baseName = name;
                String ext = "";
                int dot = name.lastIndexOf('.');
                if (dot > 0 && dot < name.length() - 1) {
                    baseName = name.substring(0, dot);
                    ext = name.substring(dot + 1);
                }
                final String fBase = baseName;
                final String fExt = ext;
                final String fName = name;

                String expectedDatePath = null;
                if (sh.getSubmitTime() != null) {
                    try {
                        LocalDate ld = Instant.ofEpochMilli(sh.getSubmitTime().getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
                        expectedDatePath = DATE_PATH_FMT.format(ld);
                    } catch (Exception ignore) {}
                }

                String foundResource = null;
                try {
                    if (Files.exists(uploadBase)) {
                        if (expectedDatePath != null) {
                            Path dateDir = uploadBase.resolve(expectedDatePath);
                            File dateFolder = dateDir.toFile();
                            if (dateFolder.exists() && dateFolder.isDirectory()) {
                                File[] candidates = dateFolder.listFiles(f -> f.isFile() &&
                                        f.getName().toLowerCase().startsWith((fBase + "_").toLowerCase()) &&
                                        (fExt.isEmpty() || f.getName().toLowerCase().endsWith(("." + fExt).toLowerCase())));
                                if (candidates != null && candidates.length > 0) {
                                    File best = candidates[0];
                                    for (File c : candidates) {
                                        if (c.lastModified() > best.lastModified()) best = c;
                                    }
                                    Path rel = profilePath.relativize(best.toPath().toAbsolutePath());
                                    String relStr = rel.toString().replace(File.separatorChar, '/');
                                    if (!relStr.startsWith("/")) relStr = "/" + relStr;
                                    foundResource = resourcePrefix + relStr;
                                }
                            }
                        }
                        if (foundResource == null) {
                            try {
                                Path found = Files.walk(uploadBase, 6)
                                        .filter(p -> Files.isRegularFile(p))
                                        .filter(p -> {
                                            String fn = p.getFileName().toString();
                                            boolean prefix = fn.toLowerCase().startsWith((fBase + "_").toLowerCase()) || fn.equalsIgnoreCase(fName);
                                            boolean extOk = fExt.isEmpty() || fn.toLowerCase().endsWith(("." + fExt).toLowerCase());
                                            return prefix && extOk;
                                        })
                                        .sorted((a, b) -> {
                                            try {
                                                long la = Files.getLastModifiedTime(a).toMillis();
                                                long lb = Files.getLastModifiedTime(b).toMillis();
                                                return Long.compare(lb, la);
                                            } catch (IOException e) { return 0; }
                                        })
                                        .findFirst().orElse(null);
                                if (found != null) {
                                    Path rel = profilePath.relativize(found.toAbsolutePath());
                                    String relStr = rel.toString().replace(File.separatorChar, '/');
                                    if (!relStr.startsWith("/")) relStr = "/" + relStr;
                                    foundResource = resourcePrefix + relStr;
                                }
                            } catch (IOException e) {
                                logger.debug("resolveSubmissionFilePaths: filesystem broad search failed for {}", fName, e);
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.debug("resolveSubmissionFilePaths: error while trying to locate file {}", fName, ex);
                }

                if (foundResource == null) {
                    String best = resourcePrefix + "/upload/" + name;
                    resolved.add(best);
                } else {
                    resolved.add(foundResource);
                }
            }
            if (!resolved.isEmpty()) {
                sh.setSubmissionFiles(resolved.stream().collect(Collectors.joining(",")));
            }
        }
    }

    private void ensureStudentNames(List<ClassStudentHomework> list) {
        if (list == null || list.isEmpty()) return;
        for (ClassStudentHomework sh : list) {
            if (sh == null) continue;
            String currentName = sh.getStudentName();
            if (currentName != null && !currentName.trim().isEmpty()) continue;
            String studentNo = sh.getStudentNo();
            if (studentNo == null || studentNo.trim().isEmpty()) continue;
            try {
                com.ruoyi.proj_lwj.domain.ClassStudent cs = classStudentMapper.selectByStudentNo(studentNo.trim());
                if (cs != null && cs.getStudentName() != null && !cs.getStudentName().trim().isEmpty()) {
                    sh.setStudentName(cs.getStudentName().trim());
                }
            } catch (Exception ex) {
                logger.debug("ensureStudentNames: failed resolving name for studentNo {}", studentNo, ex);
            }
        }
    }

    private List<ClassStudentHomework> filterOutDeletedHomework(List<ClassStudentHomework> list){
        if(list == null || list.isEmpty()) return list;
        List<ClassStudentHomework> kept = new java.util.ArrayList<>();
        for(ClassStudentHomework sh : list){
            try{
                Long hwId = sh.getHomeworkId();
                if(hwId == null){
                    continue;
                }
                ClassHomework hw = homeworkService.selectHomeworkById(hwId);
                if(hw == null){
                    continue;
                }
                kept.add(sh);
            }catch(Exception ignore){}
        }
        return kept;
    }
}