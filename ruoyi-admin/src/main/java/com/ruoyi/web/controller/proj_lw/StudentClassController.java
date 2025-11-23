package com.ruoyi.web.controller.proj_lw;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.ruoyi.proj_lw.domain.ClassStudentLw;
import com.ruoyi.proj_lw.mapper.ClassStudentLwMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.proj_lw.domain.ClassJoinApplication;
import com.ruoyi.proj_lw.domain.ClassSession;
import com.ruoyi.proj_lw.domain.ClassSessionStudent;
import com.ruoyi.proj_lw.mapper.ClassSessionMapper;
import com.ruoyi.proj_lw.mapper.ClassSessionStudentMapper;
import com.ruoyi.proj_lw.service.IClassJoinApplicationService;
import com.ruoyi.proj_lw.service.IStudentClassService;

/**
 * 学生课堂管理Controller
 */
@RestController
@RequestMapping("/proj_lw/student/class")
public class StudentClassController extends BaseController {

    @Autowired
    private IClassJoinApplicationService classJoinApplicationService;

    @Autowired
    private IStudentClassService studentClassService;

    @Autowired
    private ClassSessionMapper classSessionMapper;

    @Autowired
    private ClassSessionStudentMapper classSessionStudentMapper;

    @Autowired
    private ClassStudentLwMapper classStudentLwMapper;

    /**
     * 申请加入课堂
     */
    @PostMapping("/apply/{sessionId}")
    public AjaxResult applyJoinClass(@PathVariable("sessionId") Long sessionId) {
        try {
            Long userId = SecurityUtils.getUserId(); // 这是用户ID，不是学生ID
            int result = classJoinApplicationService.applyJoinClass(sessionId, userId);
            return result > 0 ? AjaxResult.success("申请成功") : AjaxResult.error("申请失败");
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 我的申请列表
     */
    @GetMapping("/applications")
    public TableDataInfo getMyApplications() {
        Long userId = SecurityUtils.getUserId();
        startPage();
        List<ClassJoinApplication> list = classJoinApplicationService.getMyApplications(userId);
        // 确保list不为null
        if (list == null) {
            list = new ArrayList<>();
        }
        return getDataTable(list);
    }

    /**
     * 获取我已加入的课堂
     */
    @GetMapping("/joined")
    public TableDataInfo getJoinedClasses() {
        Long userId = SecurityUtils.getUserId();
        startPage();
        List<ClassSessionStudent> list = studentClassService.getJoinedClasses(userId);
        // 确保list不为null
        if (list == null) {
            list = new ArrayList<>();
        }
        return getDataTable(list);
    }

    /**
     * 获取可申请的课堂 - 修复版本
     */
    @GetMapping("/available")
    public TableDataInfo getAvailableClasses(ClassSession queryParams) {
        System.out.println("=== 开始查询可申请课堂 ===");
        System.out.println("查询参数: " + queryParams);

        try {
            Long studentId = SecurityUtils.getUserId();
            System.out.println("当前学生ID: " + studentId);

            startPage();

            // 直接查询所有课堂，不限制courseId
            List<ClassSession> allSessions = classSessionMapper.selectSessionList(queryParams);
            System.out.println("数据库查询结果数量: " + (allSessions != null ? allSessions.size() : "null"));

            if (allSessions != null && !allSessions.isEmpty()) {
                for (ClassSession session : allSessions) {
                    System.out.println("课堂数据: ID=" + session.getSessionId() +
                            ", 名称=" + session.getClassName() +
                            ", 教师=" + session.getTeacher() +
                            ", 状态=" + session.getStatus() +
                            ", courseId=" + session.getCourseId());
                }
            } else {
                System.out.println("数据库查询结果为空！");
            }

            // 获取学生已加入的课堂ID
            List<Long> joinedSessionIds = classSessionStudentMapper.selectJoinedSessionIds(studentId);
            System.out.println("学生已加入的课堂ID: " + joinedSessionIds);

            // 过滤已加入的课堂
            List<ClassSession> availableSessions = new ArrayList<>();
            if (allSessions != null) {
                for (ClassSession session : allSessions) {
                    if (!joinedSessionIds.contains(session.getSessionId())) {
                        availableSessions.add(session);
                    }
                }
            }

            System.out.println("最终可申请课堂数量: " + availableSessions.size());


            // 在返回前添加这个调试
            TableDataInfo result = getDataTable(availableSessions);
            System.out.println("getDataTable返回的数据类型: " + result.getClass().getName());
            System.out.println("getDataTable的rows字段类型: " + (result.getRows() != null ? result.getRows().getClass().getName() : "null"));
            System.out.println("getDataTable的total字段值: " + result.getTotal());

            // 手动创建一个明确的数据结构测试
            Map<String, Object> testData = new HashMap<>();
            testData.put("rows", availableSessions);
            testData.put("total", availableSessions.size());
            testData.put("code", 200);
            testData.put("msg", "success");

            System.out.println("手动创建的数据: " + testData);

            return result;

        } catch (Exception e) {
            System.err.println("查询可申请课堂异常: " + e.getMessage());
            e.printStackTrace();
            return getDataTable(new ArrayList<>());
        }
    }

    /**
     * 取消申请
     */
    @PostMapping("/application/cancel/{applicationId}")
    public AjaxResult cancelApplication(@PathVariable("applicationId") Long applicationId) {
        try {
            Long userId = SecurityUtils.getUserId();

            // 检查申请是否存在且属于当前用户
            ClassJoinApplication application = classJoinApplicationService.selectClassJoinApplicationByApplicationId(applicationId);
            if (application == null) {
                return AjaxResult.error("申请记录不存在");
            }

            // 根据用户ID查询学生信息
            ClassStudentLw student = classStudentLwMapper.selectClassStudentByUserId(userId);
            if (student == null) {
                return AjaxResult.error("学生信息不存在");
            }

            Long studentId = student.getStudentId();
            if (!application.getStudentId().equals(studentId)) {
                return AjaxResult.error("无权操作此申请");
            }

            if (!"0".equals(application.getStatus())) {
                return AjaxResult.error("只能取消待审核的申请");
            }

            int result = classJoinApplicationService.deleteClassJoinApplicationByApplicationId(applicationId);
            return result > 0 ? AjaxResult.success("取消申请成功") : AjaxResult.error("取消申请失败");
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 退出课堂
     */
    @PostMapping("/quit/{sessionId}")
    public AjaxResult quitClass(@PathVariable("sessionId") Long sessionId) {
        try {
            Long studentId = SecurityUtils.getUserId();
            int result = classJoinApplicationService.quitClass(sessionId, studentId);
            return result > 0 ? AjaxResult.success("退出课堂成功") : AjaxResult.error("退出课堂失败");
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }
}