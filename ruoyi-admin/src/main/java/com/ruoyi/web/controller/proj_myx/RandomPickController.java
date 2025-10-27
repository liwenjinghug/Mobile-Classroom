package com.ruoyi.web.controller.proj_myx;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.proj_myx.domain.Attendance;
import com.ruoyi.proj_myx.domain.RandomPickRecord;
import com.ruoyi.proj_myx.service.IRandomPickService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/proj_myx/random")
public class RandomPickController extends BaseController {

    @Autowired
    private IRandomPickService randomPickService;

    @PreAuthorize("@ss.hasPermi('proj_myx:random:list')")
    @GetMapping("/eligible")
    public AjaxResult eligible(@RequestParam Long sessionId) {
        if (sessionId == null) {
            return AjaxResult.error("sessionId 不可为空");
        }
        List<Attendance> list = randomPickService.getEligibleStudents(sessionId);
        return AjaxResult.success(list);
    }

    @PreAuthorize("@ss.hasPermi('proj_myx:random:pick')")
    @Log(title = "随机抽人", businessType = com.ruoyi.common.enums.BusinessType.OTHER)
    @PostMapping("/pick/random")
    public AjaxResult pickRandom(@RequestParam Long sessionId, @RequestParam(required = false) Long teacherId) {
        if (sessionId == null) {
            return AjaxResult.error("sessionId 不可为空");
        }
        RandomPickRecord record = randomPickService.pickRandomAndSave(sessionId, teacherId);
        if (record == null) {
            return AjaxResult.error("没有可抽取的学生");
        }
        return AjaxResult.success(record);
    }

    @PreAuthorize("@ss.hasPermi('proj_myx:random:add')")
    @Log(title = "保存抽取记录", businessType = com.ruoyi.common.enums.BusinessType.INSERT)
    @PostMapping("/pick/save")
    public AjaxResult savePick(@RequestBody RandomPickRecord record) {
        if (record == null || record.getStudentId() == null || record.getSessionId() == null) {
            return AjaxResult.error("参数不完整");
        }
        int rows = randomPickService.savePick(record);
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    @PreAuthorize("@ss.hasPermi('proj_myx:random:history')")
    @GetMapping("/history")
    public AjaxResult history(@RequestParam Long sessionId) {
        if (sessionId == null) {
            return AjaxResult.error("sessionId 不可为空");
        }
        List<RandomPickRecord> list = randomPickService.getHistory(sessionId);
        return AjaxResult.success(list);
    }
}
