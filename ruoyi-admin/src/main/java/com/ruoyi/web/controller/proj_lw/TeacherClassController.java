package com.ruoyi.web.controller.proj_lw;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.proj_lw.domain.ClassJoinApplication;
import com.ruoyi.proj_lw.domain.ClassSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.proj_lw.service.IClassJoinApplicationService;
import com.ruoyi.proj_lw.service.IClassSessionService;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 教师课堂管理Controller
 */
@RestController
@RequestMapping("/proj_lw/teacher/class")
public class TeacherClassController extends BaseController {

    @Autowired
    private IClassJoinApplicationService classJoinApplicationService;

    @Autowired
    private IClassSessionService classSessionService;

    /**
     * 获取我管理的课堂 - 通过用户名匹配教师名称
     */
    @GetMapping("/my")
    public TableDataInfo getMyTeachingClasses() {
        try {
            // 获取当前登录用户的用户名
            String username = SecurityUtils.getUsername();
            System.out.println("=== 获取教师课堂 ===");
            System.out.println("当前教师用户名: " + username);

            startPage();

            // 根据用户名查询课堂
            ClassSession query = new ClassSession();
            query.setTeacher(username); // 使用用户名匹配课堂表中的教师名称
            List<ClassSession> list = classSessionService.selectSessionList(query);

            System.out.println("查询到的教师课堂数量: " + (list != null ? list.size() : "null"));
            if (list != null && !list.isEmpty()) {
                for (ClassSession session : list) {
                    System.out.println("教师课堂: ID=" + session.getSessionId() +
                            ", 名称=" + session.getClassName() +
                            ", 教师=" + session.getTeacher());
                }
            } else {
                System.out.println("未找到教师管理的课堂");
            }

            // 确保list不为null
            if (list == null) {
                list = new ArrayList<>();
            }

            TableDataInfo result = getDataTable(list);
            System.out.println("返回给前端的数据: rows=" + result.getRows().size() + ", total=" + result.getTotal());
            return result;

        } catch (Exception e) {
            System.err.println("获取教师课堂失败: " + e.getMessage());
            e.printStackTrace();
            return getDataTable(new ArrayList<>());
        }
    }

    /**
     * 获取待审核申请列表 - 只获取当前教师管理的课堂的申请
     */
    @GetMapping("/applications/pending")
    public TableDataInfo getPendingApplications() {
        try {
            // 获取当前登录用户的用户名
            String username = SecurityUtils.getUsername();
            System.out.println("=== 获取待审核申请 ===");
            System.out.println("当前教师用户名: " + username);

            startPage();
            List<ClassJoinApplication> list = classJoinApplicationService.getPendingApplicationsByTeacher(username);

            System.out.println("待审核申请数量: " + (list != null ? list.size() : "null"));
            if (list != null && !list.isEmpty()) {
                for (ClassJoinApplication application : list) {
                    System.out.println("待审核申请: ID=" + application.getApplicationId() +
                            ", 课堂=" + application.getClassName() +
                            ", 学生=" + application.getStudentName());
                }
            } else {
                System.out.println("没有待审核的申请");
            }

            // 确保list不为null
            if (list == null) {
                list = new ArrayList<>();
            }

            TableDataInfo result = getDataTable(list);
            System.out.println("返回给前端的数据: rows=" + result.getRows().size() + ", total=" + result.getTotal());
            return result;

        } catch (Exception e) {
            System.err.println("获取待审核申请失败: " + e.getMessage());
            e.printStackTrace();
            return getDataTable(new ArrayList<>());
        }
    }

    /**
     * 审核申请
     */
    @PostMapping("/application/audit")
    public AjaxResult auditApplication(@RequestParam Long applicationId,
                                       @RequestParam String status,
                                       @RequestParam(required = false) String remark) {
        try {
            System.out.println("=== 审核申请 ===");
            System.out.println("申请ID: " + applicationId + ", 状态: " + status + ", 备注: " + remark);

            // 获取当前用户ID作为审核人
            Long auditUserId = SecurityUtils.getUserId();
            String currentUsername = SecurityUtils.getUsername();

            // 先检查申请是否属于当前教师的课堂
            ClassJoinApplication application = classJoinApplicationService.selectClassJoinApplicationByApplicationId(applicationId);
            if (application == null) {
                System.out.println("申请记录不存在: " + applicationId);
                return AjaxResult.error("申请记录不存在");
            }

            System.out.println("申请详情: 课堂ID=" + application.getSessionId() + ", 学生=" + application.getStudentName());

            // 验证当前用户是否有权限审核这个申请
            if (!classJoinApplicationService.isTeacherOfSession(application.getSessionId(), currentUsername)) {
                System.out.println("无权审核此申请: " + applicationId);
                return AjaxResult.error("无权审核此申请");
            }

            int result = classJoinApplicationService.auditApplication(applicationId, status, auditUserId, remark);
            System.out.println("审核结果: " + result);

            return result > 0 ? AjaxResult.success("审核成功") : AjaxResult.error("审核失败");
        } catch (Exception e) {
            System.err.println("审核申请失败: " + e.getMessage());
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 批量审核申请
     */
    @PostMapping("/application/batchAudit")
    public AjaxResult batchAuditApplications(@RequestParam Long[] applicationIds,
                                             @RequestParam String status) {
        try {
            System.out.println("=== 批量审核申请 ===");
            System.out.println("申请IDs: " + Arrays.toString(applicationIds) + ", 状态: " + status);

            Long auditUserId = SecurityUtils.getUserId();
            String currentUsername = SecurityUtils.getUsername();
            List<Long> idList = Arrays.asList(applicationIds);

            // 验证所有申请都属于当前教师的课堂
            for (Long applicationId : applicationIds) {
                ClassJoinApplication application = classJoinApplicationService.selectClassJoinApplicationByApplicationId(applicationId);
                if (application == null) {
                    System.out.println("申请记录不存在: " + applicationId);
                    return AjaxResult.error("申请记录不存在: " + applicationId);
                }
                if (!classJoinApplicationService.isTeacherOfSession(application.getSessionId(), currentUsername)) {
                    System.out.println("无权审核申请: " + applicationId);
                    return AjaxResult.error("无权审核申请: " + applicationId);
                }
            }

            int result = classJoinApplicationService.batchAuditApplications(idList, status, auditUserId);
            System.out.println("批量审核结果: " + result);

            return result > 0 ? AjaxResult.success("批量审核成功") : AjaxResult.error("批量审核失败");
        } catch (Exception e) {
            System.err.println("批量审核申请失败: " + e.getMessage());
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 测试接口 - 直接返回数据，不使用分页
     */
    @GetMapping("/test/no-page")
    public AjaxResult testNoPage() {
        try {
            String username = SecurityUtils.getUsername();
            System.out.println("=== 测试接口(不分页) ===");
            System.out.println("当前教师用户名: " + username);

            // 根据用户名查询课堂
            ClassSession query = new ClassSession();
            query.setTeacher(username);
            List<ClassSession> list = classSessionService.selectSessionList(query);

            System.out.println("查询到的课堂数量: " + (list != null ? list.size() : "null"));

            // 直接返回，不使用 getDataTable()
            return AjaxResult.success("测试成功", list != null ? list : new ArrayList<>());

        } catch (Exception e) {
            System.err.println("测试接口失败: " + e.getMessage());
            return AjaxResult.error("测试失败");
        }
    }

    /**
     * 测试接口 - 检查数据序列化
     */
    @GetMapping("/test/serialize")
    public String testSerialize() {
        try {
            String username = SecurityUtils.getUsername();
            System.out.println("=== 测试序列化接口 ===");

            ClassSession query = new ClassSession();
            query.setTeacher(username);
            List<ClassSession> list = classSessionService.selectSessionList(query);

            // 手动创建返回数据
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("rows", list != null ? list : new ArrayList<>());
            result.put("total", list != null ? list.size() : 0);
            result.put("code", 200);
            result.put("msg", "success");

            // 手动序列化
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = mapper.writeValueAsString(result);

            System.out.println("手动序列化的JSON: " + json);
            return json;

        } catch (Exception e) {
            System.err.println("序列化测试失败: " + e.getMessage());
            return "{\"code\":500,\"msg\":\"error\"}";
        }
    }
}