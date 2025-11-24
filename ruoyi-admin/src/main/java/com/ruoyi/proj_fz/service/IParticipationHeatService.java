package com.ruoyi.proj_fz.service;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

public interface IParticipationHeatService
{
    List<Map<String, Object>> selectParticipationHeatData();
    Map<String, Object> selectParticipationStats();

    // 新增方法
    List<Map<String, Object>> selectParticipationHeatDataByFilter(Map<String, Object> params);
    void exportParticipationHeatData(Map<String, Object> params, HttpServletResponse response);
    Map<String, Object> getFilterOptions();

    // 添加参与度分布统计方法
    Map<String, Object> selectParticipationDistribution(Map<String, Object> params);
}