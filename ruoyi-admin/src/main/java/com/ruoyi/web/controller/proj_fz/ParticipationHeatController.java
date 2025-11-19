package com.ruoyi.web.controller.proj_fz;

import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.proj_fz.service.IParticipationHeatService;

@RestController
@RequestMapping("/proj_fz/participationHeat")
public class ParticipationHeatController extends BaseController
{
    @Autowired
    private IParticipationHeatService participationHeatService;

    @PreAuthorize("@ss.hasPermi('proj_fz:participationHeat:list')")
    @GetMapping("/list")
    public AjaxResult list()
    {
        List<Map<String, Object>> heatData = participationHeatService.selectParticipationHeatData();
        return success(heatData);
    }

    @PreAuthorize("@ss.hasPermi('proj_fz:participationHeat:list')")
    @GetMapping("/stats")
    public AjaxResult stats()
    {
        Map<String, Object> stats = participationHeatService.selectParticipationStats();
        return success(stats);
    }
}