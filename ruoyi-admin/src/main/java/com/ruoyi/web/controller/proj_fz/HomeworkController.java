package com.ruoyi.web.controller.proj_fz;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.proj_lwj.domain.ClassHomework;
import com.ruoyi.proj_lwj.domain.ClassStudent;
import com.ruoyi.proj_lwj.domain.ClassStudentHomework;
import com.ruoyi.proj_lwj.mapper.ClassStudentMapper;
import com.ruoyi.proj_lwj.service.IClassHomeworkService;
import com.ruoyi.proj_lwj.service.IClassStudentHomeworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 学生作业Controller - 无权限限制版本
 * 专门给小程序学生端使用
 *
 * @author proj_fz
 */
@RestController
@RequestMapping("/proj_fz/homework")
public class HomeworkController extends BaseController {

    @Autowired
    private IClassHomeworkService homeworkService;

    @Autowired
    private IClassStudentHomeworkService studentHomeworkService;

    @Autowired
    private ClassStudentMapper classStudentMapper;

    /**
     * 获取作业列表（学生端）
     */
    @GetMapping("/list")
    public TableDataInfo list(ClassHomework hw) {
        startPage();
        List<ClassHomework> list = homeworkService.selectHomeworkList(hw);
        return getDataTable(list);
    }

    /**
     * 获取作业详情（学生端）
     */
    @GetMapping("/{homeworkId}")
    public AjaxResult getInfo(@PathVariable Long homeworkId) {
        ClassHomework homework = homeworkService.selectHomeworkById(homeworkId);
        return AjaxResult.success(homework);
    }

    /**
     * 获取我的作业提交记录
     * 同时通过 studentId（用户ID）、class_student.student_id 和 studentNo（用户名/学号）查询，确保都能匹配到
     */
    @GetMapping("/my-submissions")
    public AjaxResult getMySubmissions() {
        Long userId = getUserId();
        String username = getUsername();

        // 用于去重的Set
        Set<Long> seenIds = new HashSet<>();
        List<ClassStudentHomework> result = new ArrayList<>();

        // 0. 先通过学号查找 class_student 表中的 student_id
        Long classStudentId = null;
        try {
            ClassStudent cs = classStudentMapper.selectByStudentNo(username);
            if (cs != null && cs.getStudentId() != null) {
                classStudentId = cs.getStudentId();
                logger.info("getMySubmissions: 通过学号 {} 找到 class_student.student_id = {}", username, classStudentId);
            }
        } catch (Exception e) {
            logger.warn("通过学号查找class_student失败", e);
        }

        // 1. 通过 class_student.student_id 查询（优先，网页端使用这个ID）
        if (classStudentId != null) {
            try {
                List<ClassStudentHomework> listByClassStudentId = studentHomeworkService.selectByStudentId(classStudentId);
                if (listByClassStudentId != null) {
                    for (ClassStudentHomework item : listByClassStudentId) {
                        if (item.getStudentHomeworkId() != null && !seenIds.contains(item.getStudentHomeworkId())) {
                            seenIds.add(item.getStudentHomeworkId());
                            result.add(item);
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("通过classStudentId查询提交记录失败", e);
            }
        }

        // 2. 通过 sys_user.user_id 查询（小程序之前使用这个ID）
        if (!userId.equals(classStudentId)) {
            try {
                List<ClassStudentHomework> listById = studentHomeworkService.selectByStudentId(userId);
                if (listById != null) {
                    for (ClassStudentHomework item : listById) {
                        if (item.getStudentHomeworkId() != null && !seenIds.contains(item.getStudentHomeworkId())) {
                            seenIds.add(item.getStudentHomeworkId());
                            result.add(item);
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("通过userId查询提交记录失败", e);
            }
        }

        // 3. 通过 studentNo（用户名）查询
        if (username != null && !username.isEmpty()) {
            try {
                List<ClassStudentHomework> listByNo = studentHomeworkService.selectByStudentNo(username);
                if (listByNo != null) {
                    for (ClassStudentHomework item : listByNo) {
                        if (item.getStudentHomeworkId() != null && !seenIds.contains(item.getStudentHomeworkId())) {
                            seenIds.add(item.getStudentHomeworkId());
                            result.add(item);
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("通过studentNo查询提交记录失败", e);
            }

            // 4. 通过 studentIdentifier（学号或姓名）再次查询
            try {
                List<ClassStudentHomework> listByIdent = studentHomeworkService.selectByStudentIdentifier(username);
                if (listByIdent != null) {
                    for (ClassStudentHomework item : listByIdent) {
                        if (item.getStudentHomeworkId() != null && !seenIds.contains(item.getStudentHomeworkId())) {
                            seenIds.add(item.getStudentHomeworkId());
                            result.add(item);
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("通过studentIdentifier查询提交记录失败", e);
            }
        }

        return AjaxResult.success(result);
    }

    /**
     * 提交作业（学生端）
     */
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody ClassStudentHomework shw) {
        String username = getUsername();
        Long userId = getUserId();

        shw.setCreateBy(username);

        if (shw.getHomeworkId() == null) {
            return AjaxResult.error("作业ID不能为空");
        }

        // 自动设置学号为用户名
        if (shw.getStudentNo() == null || shw.getStudentNo().isEmpty()) {
            shw.setStudentNo(username);
        }

        // 尝试通过学号查找 class_student 表中的 student_id
        // 这样网页端通过 class_student.student_id 查询时能找到记录
        Long classStudentId = null;
        try {
            ClassStudent cs = classStudentMapper.selectByStudentNo(username);
            if (cs != null && cs.getStudentId() != null) {
                classStudentId = cs.getStudentId();
                logger.info("通过学号 {} 找到 class_student.student_id = {}", username, classStudentId);
            }
        } catch (Exception e) {
            logger.warn("通过学号查找class_student失败", e);
        }

        // 设置学生ID：优先使用 class_student.student_id，否则使用 sys_user.user_id
        if (shw.getStudentId() == null || shw.getStudentId() == 0L) {
            if (classStudentId != null) {
                shw.setStudentId(classStudentId);
            } else {
                shw.setStudentId(userId);
            }
        }

        // 设置提交时间和状态
        if (shw.getSubmitTime() == null) {
            shw.setSubmitTime(new java.util.Date());
        }
        if (shw.getStatus() == null) {
            shw.setStatus(1); // 1=已提交
        }

        // 检查是否已有提交 - 同时通过多种方式查找
        ClassStudentHomework existingSubmission = null;
        Set<Long> checkedIds = new HashSet<>();

        // 1. 通过当前设置的 studentId 查找
        try {
            List<ClassStudentHomework> existingById = studentHomeworkService.selectByStudentId(shw.getStudentId());
            if (existingById != null) {
                for (ClassStudentHomework e : existingById) {
                    if (e.getHomeworkId() != null && e.getHomeworkId().equals(shw.getHomeworkId())) {
                        existingSubmission = e;
                        break;
                    }
                    if (e.getStudentHomeworkId() != null) {
                        checkedIds.add(e.getStudentHomeworkId());
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("通过studentId查找已有提交失败", e);
        }

        // 2. 如果没找到，且 classStudentId 与 userId 不同，也通过 userId 查找
        if (existingSubmission == null && classStudentId != null && !classStudentId.equals(userId)) {
            try {
                List<ClassStudentHomework> existingByUserId = studentHomeworkService.selectByStudentId(userId);
                if (existingByUserId != null) {
                    for (ClassStudentHomework e : existingByUserId) {
                        if (e.getHomeworkId() != null && e.getHomeworkId().equals(shw.getHomeworkId())) {
                            if (e.getStudentHomeworkId() == null || !checkedIds.contains(e.getStudentHomeworkId())) {
                                existingSubmission = e;
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("通过userId查找已有提交失败", e);
            }
        }

        // 3. 如果还没找到，通过 studentNo 查找
        if (existingSubmission == null && username != null && !username.isEmpty()) {
            try {
                List<ClassStudentHomework> existingByNo = studentHomeworkService.selectByStudentNo(username);
                if (existingByNo != null) {
                    for (ClassStudentHomework e : existingByNo) {
                        if (e.getHomeworkId() != null && e.getHomeworkId().equals(shw.getHomeworkId())) {
                            if (e.getStudentHomeworkId() == null || !checkedIds.contains(e.getStudentHomeworkId())) {
                                existingSubmission = e;
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("通过studentNo查找已有提交失败", e);
            }
        }

        int r;
        if (existingSubmission != null) {
            // 更新提交
            shw.setStudentHomeworkId(existingSubmission.getStudentHomeworkId());
            shw.setUpdateBy(username);
            shw.setUpdateTime(new java.util.Date());
            r = studentHomeworkService.update(shw);
        } else {
            // 新增提交
            r = studentHomeworkService.insert(shw);
        }

        return toAjax(r);
    }
}

