package com.ruoyi.web.controller.proj_lwj;

import com.ruoyi.common.annotation.Log;
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
import com.ruoyi.common.utils.file.FileUtils;
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

    @PreAuthorize("@ss.hasPermi('projlwj:homework:list')")
    @GetMapping("/list")
    public TableDataInfo list(ClassHomework hw) {
        startPage();
        java.util.List<ClassHomework> list = homeworkService.selectHomeworkList(hw);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('projlwj:homework:query')")
    @GetMapping("/{homeworkId}")
    public AjaxResult getInfo(@PathVariable Long homeworkId) {
        return AjaxResult.success(homeworkService.selectHomeworkById(homeworkId));
    }

    @PreAuthorize("@ss.hasPermi('projlwj:homework:add')")
    @Log(title = "作业管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody com.ruoyi.proj_lwj.domain.ClassHomework hw) {
        hw.setCreateBy(getUsername());
        try {
            // log incoming payload for debugging
            logger.info("添加作业请求 payload: {}", hw);
            int r = homeworkService.addHomework(hw);
            // TODO: 自动为班级学生生成 class_student_homework 记录（根据 session 或 course 关联学生）
            return toAjax(r);
        } catch (Exception e) {
            logger.error("添加作业失败", e);
            return AjaxResult.error("发布作业失败：" + e.getMessage());
        }
    }

    @PreAuthorize("@ss.hasPermi('projlwj:homework:edit')")
    @Log(title = "作业管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody com.ruoyi.proj_lwj.domain.ClassHomework hw) {
        hw.setUpdateBy(getUsername());
        return toAjax(homeworkService.editHomework(hw));
    }

    @PreAuthorize("@ss.hasPermi('projlwj:homework:remove')")
    @Log(title = "作业管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids, @RequestParam(required = false, defaultValue = "false") boolean cascade) {
        try {
            // Always attempt to delete student submissions linked to these homework ids first.
            // This ensures student submission records are removed when a homework is deleted by teacher.
            // Any exception during cascade delete is logged but will not prevent homework deletion.
            try {
                studentHomeworkService.deleteByHomeworkIds(ids);
            } catch (Exception ex) {
                logger.warn("Cascade delete of student submissions failed (non-fatal)", ex);
            }
        } catch (Exception ex) {
            logger.warn("Cascade delete of student submissions failed", ex);
            // continue to attempt homework deletion even if cascade fails
        }
        return toAjax(homeworkService.removeHomeworkByIds(ids));
    }

    // 学生提交作业
    @PreAuthorize("@ss.hasPermi('projlwj:homework:submit')")
    @Log(title = "作业提交", businessType = BusinessType.INSERT)
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody ClassStudentHomework shw) {
        shw.setCreateBy(getUsername());

        // require homeworkId
        if (shw.getHomeworkId() == null) {
            return AjaxResult.error(HttpStatus.BAD_REQUEST, "提交失败：未指定作业ID");
        }
        // ensure homework exists (not deleted)
        ClassHomework targetHw = homeworkService.selectHomeworkById(shw.getHomeworkId());
        if (targetHw == null) {
            return AjaxResult.error(HttpStatus.NOT_FOUND, "该作业已被删除或不存在，无法提交");
        }

        // 如果前端传学号 studentNo，则根据学号查 student_id 并设置
        if (shw.getStudentNo() != null && !shw.getStudentNo().trim().isEmpty()) {
            String sn = shw.getStudentNo().trim();
            com.ruoyi.proj_lwj.domain.ClassStudent cs = classStudentMapper.selectByStudentNo(sn);
            if (cs == null) {
                return AjaxResult.error("学号 " + sn + " 未找到对应的学生，请检查后重试。");
            }
            shw.setStudentId(cs.getStudentId());
            // also ensure studentNo field is set to the canonical value from class_student
            shw.setStudentNo(cs.getStudentNo());
        }

        // 如果前端没有传 studentId 或传的是空/无效，使用当前登录用户ID（确保与 sys_user.user_id 对应）
        if (shw.getStudentId() == null || shw.getStudentId() == 0L) {
            shw.setStudentId(getUserId());
        }

        // Prevent submission if there's an existing graded submission for this student & homework
        try {
            // try to find latest submission for this student & homework
            List<ClassStudentHomework> existing = studentHomeworkService.selectByStudentId(shw.getStudentId());
            if (existing != null && !existing.isEmpty()) {
                for (ClassStudentHomework e : existing) {
                    if (e.getHomeworkId() != null && e.getHomeworkId().equals(shw.getHomeworkId())) {
                        // if any existing record for this homework is graded, reject new submission
                        if ((e.getIsGraded() != null && e.getIsGraded() == 1) || (e.getStatus() != null && String.valueOf(e.getStatus()).equals("2"))) {
                            return AjaxResult.error("该作业已被教师批改，无法再次提交或修改");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.warn("检查已存在提交时出错", ex);
        }

        // 保证提交时间与状态由后端设置，前端可不传
        if (shw.getSubmitTime() == null) {
            shw.setSubmitTime(new java.util.Date());
        }
        if (shw.getStatus() == null) {
            shw.setStatus(1); // 1=已提交
        }
        // Debug log: show the submission object to verify studentNo/studentId before persisting
        logger.info("submit - ClassStudentHomework payload before insert: {}", shw);
        int r = studentHomeworkService.insert(shw);
        return toAjax(r);
    }

    // 更新学生已提交的作业（允许学生修改自己的提交）
    @PreAuthorize("@ss.hasPermi('projlwj:homework:submit')")
    @Log(title = "作业提交", businessType = BusinessType.UPDATE)
    @PutMapping("/submit")
    public AjaxResult updateSubmit(@RequestBody ClassStudentHomework shw) {
        // require studentHomeworkId to update
        if (shw.getStudentHomeworkId() == null) {
            return AjaxResult.error(HttpStatus.BAD_REQUEST, "studentHomeworkId 不能为空，无法更新提交记录");
        }

        // If frontend provided studentNo, resolve to studentId same as in submit
        if (shw.getStudentNo() != null && !shw.getStudentNo().trim().isEmpty()) {
            String sn = shw.getStudentNo().trim();
            com.ruoyi.proj_lwj.domain.ClassStudent cs = classStudentMapper.selectByStudentNo(sn);
            if (cs == null) {
                return AjaxResult.error(HttpStatus.NOT_FOUND, "学号 " + sn + " 未找到对应的学生，请检查后重试。");
            }
            shw.setStudentId(cs.getStudentId());
            // ensure studentNo preserved/normalized from student record
            shw.setStudentNo(cs.getStudentNo());
        }
        // Debug log: show the submission object to verify studentNo/studentId before update
        logger.info("updateSubmit - ClassStudentHomework payload before update: {}", shw);

        // check existing record: do not allow update if already graded
        try {
            ClassStudentHomework exist = studentHomeworkService.selectById(shw.getStudentHomeworkId());
            if (exist != null) {
                // determine homework id: prefer provided value, fall back to existing record
                Long hwId = (shw.getHomeworkId() != null) ? shw.getHomeworkId() : exist.getHomeworkId();
                if (hwId == null || homeworkService.selectHomeworkById(hwId) == null) {
                    return AjaxResult.error(HttpStatus.NOT_FOUND, "该作业已被删除，无法修改提交记录");
                }
                // also prevent modification if already graded
                if ((exist.getIsGraded() != null && exist.getIsGraded() == 1) || (exist.getStatus() != null && String.valueOf(exist.getStatus()).equals("2"))) {
                    return AjaxResult.error("该提交已被教师批改，无法修改");
                }
            }
        } catch (Exception ex) {
            logger.warn("检查提交是否可编辑时出错", ex);
        }

        shw.setUpdateBy(getUsername());
        // update submitTime to now if not provided
        if (shw.getSubmitTime() == null) {
            shw.setSubmitTime(new java.util.Date());
        }
        int r = studentHomeworkService.update(shw);
        return toAjax(r);
    }

    // 教师批改：允许修改成绩和评语（可重复修改）
    // 权限检查已移除，允许所有用户访问批改接口（前端页面无需额外权限）
    @Log(title = "作业批改", businessType = BusinessType.UPDATE)
    @PutMapping("/grade")
    public AjaxResult gradeSubmission(@RequestBody ClassStudentHomework shw) {
        // require id
        if (shw.getStudentHomeworkId() == null) {
            return AjaxResult.error(HttpStatus.BAD_REQUEST, "studentHomeworkId 不能为空，无法批改");
        }

        // Ensure the submission exists
        ClassStudentHomework exist = studentHomeworkService.selectById(shw.getStudentHomeworkId());
        if (exist == null) {
            return AjaxResult.error(HttpStatus.NOT_FOUND, "提交记录不存在，无法批改");
        }

        // Debug log: show grading payload and existing record to verify we won't overwrite submission files
        logger.info("gradeSubmission payload: {}", shw);
        logger.info("existing submission before grade: {}", exist);

        // Only update grading-related fields. Set corrected_by and corrected_time if not provided
        if (shw.getCorrectedBy() == null) {
            shw.setCorrectedBy(getUserId());
        }
        if (shw.getCorrectedTime() == null) {
            shw.setCorrectedTime(new java.util.Date());
        }
        // mark as graded
        shw.setIsGraded(1);
        // set status to 2 (已批改)
        shw.setStatus(2);
        shw.setUpdateBy(getUsername());

        int r = studentHomeworkService.updateGrade(shw);
        return toAjax(r);
    }

    // 查看某作业的提交情况
    @PreAuthorize("@ss.hasPermi('projlwj:homework:query')")
    @GetMapping("/submissions/{homeworkId}")
    public AjaxResult submissions(@PathVariable Long homeworkId) {
        List<ClassStudentHomework> list = studentHomeworkService.selectByHomeworkId(homeworkId);
        // if the homework is deleted, mark each submission accordingly
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
        // resolve any stored filenames to resource-prefixed paths so frontend download works
        resolveSubmissionFilePaths(list);
        return AjaxResult.success(list);
    }

    // 查看某学生的提交记录（通过学号 studentId）
    @PreAuthorize("@ss.hasPermi('projlwj:homework:query')")
    @GetMapping("/studentSubmissions")
    public AjaxResult studentSubmissions(@RequestParam Long studentId) {
        List<ClassStudentHomework> list = studentHomeworkService.selectByStudentId(studentId);
        // populate homework deleted/title per submission
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
        resolveSubmissionFilePaths(list);
        return AjaxResult.success(list);
    }

    // 公共接口：某学生的提交记录（无需特殊权限），供前端学生确认学号使用
    @GetMapping("/studentSubmissions/public")
    public AjaxResult publicStudentSubmissions(@RequestParam(required = false) Long studentId, @RequestParam(required = false) String studentNo, @RequestParam(required = false) String studentName) {
        // Log incoming params to help troubleshoot student-side lookups
        logger.info("publicStudentSubmissions called with studentId={}, studentNo={}, studentName={}", studentId, studentNo, studentName);
        Long sid = studentId;
        if ((sid == null || sid == 0L) && studentNo != null && !studentNo.trim().isEmpty()) {
            com.ruoyi.proj_lwj.domain.ClassStudent cs = classStudentMapper.selectByStudentNo(studentNo.trim());
            if (cs != null) sid = cs.getStudentId();
        }
        // If we resolved a studentId, return by studentId (preferred)
        if (sid != null) {
            List<ClassStudentHomework> list = studentHomeworkService.selectByStudentId(sid);
            resolveSubmissionFilePaths(list);
            return AjaxResult.success(list);
        }

        // Fallback: if studentNo provided but no matching ClassStudent record found,
        // try to fetch submissions directly by studentNo (some records may store student_no without a class_student entry)
        if (studentNo != null && !studentNo.trim().isEmpty()) {
            try {
                List<ClassStudentHomework> list = studentHomeworkService.selectByStudentNo(studentNo.trim());
                if (list != null && !list.isEmpty()) {
                    resolveSubmissionFilePaths(list);
                    return AjaxResult.success(list);
                }
            } catch (Exception ex) {
                logger.warn("fallback selectByStudentNo failed", ex);
            }
        }

        // Additional fallback: allow searching by studentName (exact match) against submission records
        if (studentName != null && !studentName.trim().isEmpty()) {
            try {
                List<ClassStudentHomework> list = studentHomeworkService.selectByStudentIdentifier(studentName.trim());
                if (list != null && !list.isEmpty()) {
                    resolveSubmissionFilePaths(list);
                    return AjaxResult.success(list);
                }
            } catch (Exception ex) {
                logger.warn("fallback selectByStudentIdentifier failed", ex);
            }
        }

        return AjaxResult.error("请提供 studentId 或 studentNo 以查询学生提交记录（或确认学号/姓名是否正确）");
    }

    // 删除学生的提交记录（学生或教师可调用）
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

    /**
     * Helper: resolve submission file names (which may be stored as plain file names) to resource-prefixed paths
     * so the frontend download helper can use /common/download/resource or /common/download correctly.
     * This function mutates the provided list by replacing submissionFiles with comma-separated resource paths.
     */
    private void resolveSubmissionFilePaths(List<ClassStudentHomework> list) {
        if (list == null || list.isEmpty()) return;
        String profile = RuoYiConfig.getProfile();
        if (profile == null) profile = "";
        Path profilePath = Paths.get(profile).toAbsolutePath();
        Path uploadBase = profilePath.resolve("upload");
        String resourcePrefix = Constants.RESOURCE_PREFIX; // typically "/profile"
        final DateTimeFormatter DATE_PATH_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        for (ClassStudentHomework sh : list) {
            String submission = sh.getSubmissionFiles();
            if (submission == null || submission.trim().isEmpty()) continue;
            String[] parts = submission.split(",");
            List<String> resolved = new ArrayList<>();
            for (String raw : parts) {
                String name = raw == null ? "" : raw.trim();
                if (name.isEmpty()) continue;
                // if already looks like a resource path (/profile/...) or absolute HTTP URL, keep as-is
                if (name.startsWith(resourcePrefix + "/") || name.startsWith("http://") || name.startsWith("https://")) {
                    resolved.add(name);
                    continue;
                }

                // Derive baseName/ext for pattern matching baseName_* .ext
                String baseName = name;
                String ext = "";
                int dot = name.lastIndexOf('.');
                if (dot > 0 && dot < name.length() - 1) {
                    baseName = name.substring(0, dot);
                    ext = name.substring(dot + 1);
                }
                // create effectively-final copies for lambda usage
                final String fBase = baseName;
                final String fExt = ext;
                final String fName = name;

                String expectedDatePath = null;
                if (sh.getSubmitTime() != null) {
                    try {
                        LocalDate ld = Instant.ofEpochMilli(sh.getSubmitTime().getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
                        expectedDatePath = DATE_PATH_FMT.format(ld); // yyyy/MM/dd
                    } catch (Exception ignore) {}
                }

                String foundResource = null;
                // 1) Prefer searching inside expected date directory using pattern baseName_* .ext
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
                                    // pick the latest modified as best guess
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
                        // 2) If not found, broaden search within limited depth for pattern baseName_* .ext
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
                                        // pick the newest by lastModified
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

                // 3) Final fallback (should rarely be used): /profile/upload/<original name>
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

}
