package com.ruoyi.proj_fz.service;

import java.util.List;
import java.util.Map;

public interface IParticipationHeatService
{
    List<Map<String, Object>> selectParticipationHeatData();
    Map<String, Object> selectParticipationStats();
}