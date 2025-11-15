package com.ruoyi.web.controller.proj_myx;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.proj_myx.domain.AttendanceTask;
import com.ruoyi.proj_myx.domain.Attendance;
import com.ruoyi.proj_myx.domain.AttendanceQr;
import com.ruoyi.proj_myx.service.IAttendanceService;
import com.ruoyi.proj_myx.service.IAttendanceQrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URLEncoder;

@RestController
@RequestMapping("/proj_myx/attendance")
public class AttendanceController extends BaseController {

    @Autowired
    private IAttendanceService attendanceService;

    @Autowired
    private IAttendanceQrService qrService;

    @PreAuthorize("@ss.hasPermi('proj_myx:attendance:list')")
    @GetMapping("/tasks")
    public AjaxResult listTasks(@RequestParam Long sessionId) {
        if (sessionId == null) return AjaxResult.error("sessionId 不能为空");
        List<AttendanceTask> tasks = attendanceService.getTasksBySession(sessionId);
        return AjaxResult.success(tasks);
    }

    @PreAuthorize("@ss.hasPermi('proj_myx:attendance:add')")
    @PostMapping("/task/create")
    public AjaxResult createTask(@RequestBody AttendanceTask task) {
        if (task == null || task.getSessionId() == null) return AjaxResult.error("参数不完整");
        try {
            // set defaults
            if (task.getStatus() == null) task.setStatus(1);
            task.setCreateBy(getUsername());
            AttendanceTask created = attendanceService.createTask(task);
            return created != null ? AjaxResult.success(created) : AjaxResult.error("签到创建失败!");
        } catch (Exception ex) {
            logger.error("创建签到任务异常", ex);
            return AjaxResult.error("签到创建失败: " + ex.getMessage());
        }
    }

    @PreAuthorize("@ss.hasPermi('proj_myx:attendance:view')")
    @GetMapping("/task/{taskId}")
    public AjaxResult getTask(@PathVariable Long taskId) {
        AttendanceTask t = attendanceService.getTask(taskId);
        return t == null ? AjaxResult.error("未找到签到任务") : AjaxResult.success(t);
    }

    @PreAuthorize("@ss.hasPermi('proj_myx:attendance:close')")
    @PostMapping("/task/{taskId}/close")
    public AjaxResult closeTask(@PathVariable Long taskId) {
        int rows = attendanceService.closeTask(taskId);
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    @PreAuthorize("@ss.hasPermi('proj_myx:attendance:stats')")
    @GetMapping("/task/{taskId}/records")
    public AjaxResult records(@PathVariable Long taskId) {
        List<Attendance> records = attendanceService.getAllByTask(taskId);
        return AjaxResult.success(records);
    }

    // 管理端：为指定 task 生成 QR token（可多次调用）
    @PreAuthorize("@ss.hasPermi('proj_myx:attendance:qr:create')")
    @PostMapping("/task/generateQr")
    public AjaxResult generateQrByBody(@RequestBody Map<String, Object> body) {
        if (body == null) return AjaxResult.error("请求体为空");
        Object taskIdObj = body.get("taskId");
        Object ttlObj = body.get("ttlMinutes");
        if (taskIdObj == null) return AjaxResult.error("taskId 不能为空");
        Long taskId = null;
        Integer ttl = null;
        try {
            taskId = Long.parseLong(taskIdObj.toString());
            if (ttlObj != null) ttl = Integer.parseInt(ttlObj.toString());
        } catch (NumberFormatException nfe) {
            return AjaxResult.error("taskId 或 ttlMinutes 格式不正确");
        }
        String username = getUsername();
        AttendanceQr qr = qrService.generateToken(taskId, ttl == null ? 10 : ttl, username);
        if (qr == null) return AjaxResult.error("生成二维码失败");
        Map<String, Object> data = new HashMap<>();
        data.put("token", qr.getToken());
        // Use a reliable QR image service and URL-encode the token
        String qrUrl;
        try {
            qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + URLEncoder.encode(qr.getToken(), "UTF-8");
        } catch (Exception e) {
            qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + qr.getToken();
        }
        data.put("qrUrl", qrUrl);
        data.put("expireTime", qr.getExpireTime());
        return AjaxResult.success(data);
    }

    // 学生端：扫码签到
    @PostMapping("/sign/qr")
    public AjaxResult signByQr(@RequestBody Map<String, Object> body) {
        String taskIdRaw = body.get("taskId") == null ? null : body.get("taskId").toString();
        String studentIdRaw = body.get("studentId") == null ? null : body.get("studentId").toString();
        String token = body.get("token") == null ? null : body.get("token").toString();
        if (!StringUtils.hasText(taskIdRaw) || !StringUtils.hasText(studentIdRaw) || !StringUtils.hasText(token)) return AjaxResult.error("参数不完整");
        Long taskId = null;
        Long studentId = null;
        try {
            taskId = Long.parseLong(taskIdRaw);
            studentId = Long.parseLong(studentIdRaw);
        } catch (NumberFormatException nfe) {
            return AjaxResult.error("taskId 或 studentId 参数类型无效");
        }
        int rows = attendanceService.signByQr(taskId, studentId, token);
        return rows > 0 ? AjaxResult.success("签到成功") : AjaxResult.error("签到失败，可能 token 无效、过期或已使用，或其他校验未通过");
    }

    // 学生端：位置签到（传纬度经度）
    @PostMapping("/sign/location")
    public AjaxResult signByLocation(@RequestBody Map<String, Object> body) {
        String taskIdRaw = body.get("taskId") == null ? null : body.get("taskId").toString();
        String studentIdRaw = body.get("studentId") == null ? null : body.get("studentId").toString();
        String latRaw = body.get("lat") == null ? null : body.get("lat").toString();
        String lngRaw = body.get("lng") == null ? null : body.get("lng").toString();
        if (!StringUtils.hasText(taskIdRaw) || !StringUtils.hasText(studentIdRaw) || !StringUtils.hasText(latRaw) || !StringUtils.hasText(lngRaw)) return AjaxResult.error("参数不完整");
        Long taskId = null;
        Long studentId = null;
        Double lat = null;
        Double lng = null;
        try {
            taskId = Long.parseLong(taskIdRaw);
            studentId = Long.parseLong(studentIdRaw);
            lat = Double.parseDouble(latRaw);
            lng = Double.parseDouble(lngRaw);
        } catch (NumberFormatException nfe) {
            return AjaxResult.error("参数类型无效，请检查 taskId/studentId/lat/lng 格式");
        }
        int rows = attendanceService.signByLocation(taskId, studentId, lat, lng);
        return rows > 0 ? AjaxResult.success("签到成功") : AjaxResult.error("签到失败，可能在范围外或任务不是位置签到");
    }

    @PreAuthorize("@ss.hasPermi('proj_myx:attendance:edit')")
    @PostMapping("/task/status")
    public AjaxResult updateStatus(@RequestBody Map<String, Object> body) {
        String taskIdRaw = body.get("taskId") == null ? null : body.get("taskId").toString();
        String studentIdRaw = body.get("studentId") == null ? null : body.get("studentId").toString();
        String statusRaw = body.get("status") == null ? null : body.get("status").toString();
        if (!StringUtils.hasText(taskIdRaw) || !StringUtils.hasText(studentIdRaw) || !StringUtils.hasText(statusRaw)) {
            return AjaxResult.error("参数不完整");
        }
        try {
            Long taskId = Long.parseLong(taskIdRaw);
            Long studentId = Long.parseLong(studentIdRaw);
            Integer status = Integer.parseInt(statusRaw);
            int rows = attendanceService.updateAttendanceStatus(taskId, studentId, status);
            return rows > 0 ? AjaxResult.success("更新成功") : AjaxResult.error("更新失败");
        } catch (NumberFormatException e) {
            return AjaxResult.error("参数格式不正确");
        }
    }
    // helper: get current username from BaseController context
    public String getUsername() {
        try {
            return super.getUsername();
        } catch (Exception e) {
            return "system";
        }
    }
}
