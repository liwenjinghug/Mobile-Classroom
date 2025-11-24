package com.ruoyi.web.controller.proj_fz;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    /**
     * 根据筛选条件获取参与热力数据
     */
    @PreAuthorize("@ss.hasPermi('proj_fz:participationHeat:list')")
    @PostMapping("/listByFilter")
    public AjaxResult listByFilter(@RequestBody Map<String, Object> params)
    {
        try {
            List<Map<String, Object>> heatData = participationHeatService.selectParticipationHeatDataByFilter(params);
            return success(heatData);
        } catch (Exception e) {
            logger.error("根据筛选条件查询参与热力数据失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取参与度分布统计
     */
    @PreAuthorize("@ss.hasPermi('proj_fz:participationHeat:list')")
    @PostMapping("/distribution")
    public AjaxResult getParticipationDistribution(@RequestBody Map<String, Object> params) {
        try {
            Map<String, Object> distribution = participationHeatService.selectParticipationDistribution(params);
            return success(distribution);
        } catch (Exception e) {
            logger.error("获取参与度分布失败", e);
            return error("获取参与度分布失败: " + e.getMessage());
        }
    }

    /**
     * 导出参与热力数据
     */
    @PreAuthorize("@ss.hasPermi('proj_fz:participationHeat:export')")
    @PostMapping("/export")
    public void export(@RequestBody Map<String, Object> params, HttpServletResponse response)
    {
        try {
            participationHeatService.exportParticipationHeatData(params, response);
        } catch (Exception e) {
            logger.error("导出参与热力数据失败", e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    /**
     * 获取筛选条件数据
     */
    @PreAuthorize("@ss.hasPermi('proj_fz:participationHeat:list')")
    @GetMapping("/filterOptions")
    public AjaxResult getFilterOptions()
    {
        try {
            Map<String, Object> options = participationHeatService.getFilterOptions();
            return success(options);
        } catch (Exception e) {
            logger.error("获取筛选条件失败", e);
            return error("获取筛选条件失败: " + e.getMessage());
        }
    }
}