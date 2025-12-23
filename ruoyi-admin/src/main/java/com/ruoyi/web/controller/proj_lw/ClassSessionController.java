package com.ruoyi.web.controller.proj_lw;

// [修改] 使用自定义 Log 注解
import com.ruoyi.proj_cyq.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.proj_lw.domain.ClassSession;
import com.ruoyi.proj_lw.service.IClassSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

@RestController
@RequestMapping("/proj_lw/session")
public class ClassSessionController extends BaseController {

    @Autowired
    private IClassSessionService sessionService;

    /**
     * 获取当前活跃的课堂（自动获取）
     */
    @GetMapping("/current/active")
    public AjaxResult getActiveSessions() {
        Long userId = getUserId();
        ClassSession query = new ClassSession();
        query.setTeacherId(userId);
        // 获取该老师的所有课程
        List<ClassSession> allSessions = sessionService.selectSessionList(query);
        List<ClassSession> activeList = new ArrayList<>();

        Date now = new Date();
        long bufferMillis = 15 * 60 * 1000; // 15 minutes buffer

        for (ClassSession session : allSessions) {
            if (session.getStartTime() != null && session.getEndTime() != null) {
                long start = session.getStartTime().getTime();
                long end = session.getEndTime().getTime();
                long current = now.getTime();

                // Active if: (Start - 15min) <= Now <= (End + 15min)
                if (current >= (start - bufferMillis) && current <= (end + bufferMillis)) {
                    activeList.add(session);
                }
            }
        }

        return AjaxResult.success(activeList);
    }

    /**
     * 查询课堂列表
     */
    @PreAuthorize("@ss.hasPermi('projlw:session:list')")
    @GetMapping("/list")
    public TableDataInfo list(ClassSession session) {
        startPage();
        List<ClassSession> list = sessionService.selectSessionList(session);
        return getDataTable(list);
    }

    /**
     * 根据课程ID获取课堂列表
     */
    @PreAuthorize("@ss.hasPermi('projlw:session:list')")
    @GetMapping("/byCourseId/{courseId}")
    public AjaxResult getSessionsByCourseId(@PathVariable Long courseId) {
        ClassSession session = new ClassSession();
        session.setCourseId(courseId);
        List<ClassSession> list = sessionService.selectSessionList(session);
        return AjaxResult.success(list);
    }

    /**
     * 获取课堂详细信息
     */
    @PreAuthorize("@ss.hasPermi('projlw:session:query')")
    @GetMapping(value = "/{sessionId}")
    public AjaxResult getInfo(@PathVariable("sessionId") Long sessionId) {
        return AjaxResult.success(sessionService.selectSessionById(sessionId));
    }

    /**
     * 新增课堂
     */
    @PreAuthorize("@ss.hasPermi('projlw:session:add')")
    @Log(title = "课堂管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ClassSession session) {
        // 验证courseId不为空
        if (session.getCourseId() == null) {
            return AjaxResult.error("课程ID不能为空");
        }
        session.setCreateBy(getUsername());
        return toAjax(sessionService.insertSession(session));
    }

    /**
     * 修改课堂
     */
    @PreAuthorize("@ss.hasPermi('projlw:session:edit')")
    @Log(title = "课堂管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ClassSession session) {
        // 验证courseId不为空
        if (session.getCourseId() == null) {
            return AjaxResult.error("课程ID不能为空");
        }
        session.setUpdateBy(getUsername());
        return toAjax(sessionService.updateSession(session));
    }

    /**
     * 删除课堂
     */
    @PreAuthorize("@ss.hasPermi('projlw:session:remove')")
    @Log(title = "课堂管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{sessionIds}")
    public AjaxResult remove(@PathVariable Long[] sessionIds) {
        return toAjax(sessionService.deleteSessionByIds(sessionIds));
    }

    /**
     * 进入课堂权限检查
     */
    @PreAuthorize("@ss.hasPermi('projlw:session:enter')")
    @GetMapping("/enter/{sessionId}")
    public AjaxResult checkEnterPermission(@PathVariable Long sessionId) {
        // 这里可以添加额外的进入课堂权限检查逻辑
        return AjaxResult.success("允许进入课堂");
    }
}