package com.ruoyi.web.controller.proj_fz;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.proj_lw.domain.ClassSession;
import com.ruoyi.proj_lw.service.IClassSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 课堂Controller - 无权限限制版本
 * 专门给小程序学生端使用
 *
 * @author proj_fz
 */
@RestController
@RequestMapping("/proj_fz/class")
public class ClassController extends BaseController {

    @Autowired
    private IClassSessionService sessionService;

    /**
     * 获取课堂详情（学生端）
     */
    @GetMapping("/{sessionId}")
    public AjaxResult getClassDetail(@PathVariable Long sessionId) {
        ClassSession session = sessionService.selectSessionById(sessionId);
        if (session == null) {
            return AjaxResult.error("课堂不存在");
        }
        return AjaxResult.success(session);
    }
}

