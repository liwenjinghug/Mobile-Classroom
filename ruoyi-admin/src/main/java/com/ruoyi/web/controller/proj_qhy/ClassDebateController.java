package com.ruoyi.web.controller.proj_qhy;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.proj_qhy.domain.ClassDebate;
import com.ruoyi.proj_qhy.domain.ClassDebateMsg;
import com.ruoyi.proj_qhy.service.IClassDebateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/proj_qhy/debate")
public class ClassDebateController extends BaseController {

    @Autowired
    private IClassDebateService classDebateService;

    @PreAuthorize("@ss.hasPermi('proj_qhy:debate:list')")
    @GetMapping("/list")
    public TableDataInfo list(ClassDebate classDebate) {
        startPage();
        List<ClassDebate> list = classDebateService.selectClassDebateList(classDebate);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('proj_qhy:debate:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(classDebateService.selectClassDebateById(id));
    }

    @PreAuthorize("@ss.hasPermi('proj_qhy:debate:add')")
    @Log(title = "辩论管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ClassDebate classDebate) {
        return toAjax(classDebateService.insertClassDebate(classDebate));
    }

    @PreAuthorize("@ss.hasPermi('proj_qhy:debate:edit')")
    @Log(title = "辩论管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ClassDebate classDebate) {
        return toAjax(classDebateService.updateClassDebate(classDebate));
    }

    @PreAuthorize("@ss.hasPermi('proj_qhy:debate:remove')")
    @Log(title = "辩论管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(classDebateService.deleteClassDebateByIds(ids));
    }

    // --- 业务接口 ---

    @PostMapping("/join")
    public AjaxResult join(@RequestBody Map<String, String> body) {
        Long debateId = Long.parseLong(body.get("debateId"));
        String code = body.get("inviteCode");
        String role = body.get("role");
        return AjaxResult.success(classDebateService.joinDebate(debateId, code, role));
    }

    @GetMapping("/room/{id}")
    public AjaxResult getRoom(@PathVariable("id") Long id) {
        return AjaxResult.success(classDebateService.getRoomInfo(id));
    }

    @PostMapping("/msg")
    public AjaxResult sendMsg(@RequestBody ClassDebateMsg msg) {
        return toAjax(classDebateService.sendMsg(msg));
    }

    @GetMapping("/msg/list/{debateId}")
    public AjaxResult getMsgList(@PathVariable("debateId") Long debateId) {
        return AjaxResult.success(classDebateService.getMsgList(debateId));
    }

    @PostMapping("/vote")
    public AjaxResult vote(@RequestBody Map<String, Object> body) {
        Long debateId = Long.parseLong(body.get("debateId").toString());
        String side = body.get("side").toString();
        return toAjax(classDebateService.vote(debateId, side));
    }

    @PutMapping("/start/{id}")
    public AjaxResult start(@PathVariable("id") Long id) {
        return toAjax(classDebateService.startDebate(id));
    }

    @PutMapping("/stop/{id}")
    public AjaxResult stop(@PathVariable("id") Long id) {
        return toAjax(classDebateService.stopDebate(id));
    }
}