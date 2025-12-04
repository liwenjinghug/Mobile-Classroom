package com.ruoyi.web.controller.proj_myx;

// [修改] 使用自定义 Log 注解
import com.ruoyi.proj_cyq.annotation.Log;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.proj_myx.domain.Vote;
import com.ruoyi.proj_myx.service.IVoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/proj_myx/vote")
public class VoteController extends BaseController {

    @Autowired
    private IVoteService voteService;

    @PreAuthorize("@ss.hasPermi('proj_myx:vote:list')")
    @GetMapping("/list")
    public AjaxResult list(@RequestParam Long sessionId) {
        return AjaxResult.success(voteService.getVotesBySession(sessionId));
    }

    @PreAuthorize("@ss.hasPermi('proj_myx:vote:add')")
    @Log(title = "投票管理", businessType = BusinessType.INSERT)
    @PostMapping("/create")
    public AjaxResult create(@RequestBody Vote vote) {
        if (vote.getSessionId() == null || vote.getTitle() == null) {
            return AjaxResult.error("参数不完整");
        }

        // Initial status logic based on time
        Date now = new Date();
        if (vote.getEndTime() != null && now.after(vote.getEndTime())) {
            vote.setStatus("2");
        } else if (vote.getStartTime() != null && now.before(vote.getStartTime())) {
            vote.setStatus("0");
        } else {
            vote.setStatus("1");
        }

        vote.setCreateBy(getUsername());
        return AjaxResult.success(voteService.createVote(vote));
    }

    @PreAuthorize("@ss.hasPermi('proj_myx:vote:edit')")
    @Log(title = "投票管理", businessType = BusinessType.UPDATE)
    @PostMapping("/{voteId}/start")
    public AjaxResult start(@PathVariable Long voteId) {
        return toAjax(voteService.startVote(voteId));
    }

    @PreAuthorize("@ss.hasPermi('proj_myx:vote:edit')")
    @Log(title = "投票管理", businessType = BusinessType.UPDATE)
    @PostMapping("/{voteId}/close")
    public AjaxResult close(@PathVariable Long voteId) {
        return toAjax(voteService.closeVote(voteId));
    }

    @PreAuthorize("@ss.hasPermi('proj_myx:vote:remove')")
    @Log(title = "投票管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{voteId}")
    public AjaxResult remove(@PathVariable Long voteId) {
        return toAjax(voteService.deleteVote(voteId));
    }

    @PreAuthorize("@ss.hasPermi('proj_myx:vote:query')")
    @GetMapping("/{voteId}")
    public AjaxResult getInfo(@PathVariable Long voteId) {
        return AjaxResult.success(voteService.getVote(voteId));
    }

    @PreAuthorize("@ss.hasPermi('proj_myx:vote:query')")
    @GetMapping("/{voteId}/stats")
    public AjaxResult stats(@PathVariable Long voteId) {
        return AjaxResult.success(voteService.getVoteStats(voteId));
    }

    // Student submit
    @Log(title = "参与投票", businessType = BusinessType.INSERT)
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody Map<String, Object> body) {
        Long voteId = Long.valueOf(body.get("voteId").toString());
        Long studentId = Long.valueOf(body.get("studentId").toString());
        String studentName = (String) body.get("studentName");
        List<Integer> optIdsInt = (List<Integer>) body.get("optionIds");

        if (optIdsInt == null || optIdsInt.isEmpty()) return AjaxResult.error("请选择选项");

        List<Long> optionIds = new java.util.ArrayList<>();
        for(Integer id : optIdsInt) optionIds.add(Long.valueOf(id));

        try {
            voteService.submitVote(voteId, studentId, studentName, optionIds);
            return AjaxResult.success();
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @GetMapping("/{voteId}/check")
    public AjaxResult checkVoted(@PathVariable Long voteId, @RequestParam Long studentId) {
        return AjaxResult.success(voteService.hasUserVoted(voteId, studentId));
    }
}