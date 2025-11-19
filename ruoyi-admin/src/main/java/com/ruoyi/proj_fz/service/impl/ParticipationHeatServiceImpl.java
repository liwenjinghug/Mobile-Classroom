package com.ruoyi.proj_fz.service.impl;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.proj_fz.mapper.ParticipationHeatMapper;
import com.ruoyi.proj_fz.service.IParticipationHeatService;

@Service
public class ParticipationHeatServiceImpl implements IParticipationHeatService
{
    @Autowired
    private ParticipationHeatMapper participationHeatMapper;

    @Override
    public List<Map<String, Object>> selectParticipationHeatData()
    {
        List<Map<String, Object>> heatData = participationHeatMapper.selectParticipationHeatData();

        for (Map<String, Object> data : heatData) {
            int attendanceScore = ((Number) data.get("attendance_rate")).intValue();
            int homeworkScore = ((Number) data.get("homework_rate")).intValue();
            int forumScore = ((Number) data.get("forum_score")).intValue();

            int totalScore = (int) (attendanceScore * 0.4 + homeworkScore * 0.3 + forumScore * 0.3);
            data.put("participation_score", totalScore);

            String level;
            if (totalScore >= 90) level = "极高";
            else if (totalScore >= 80) level = "高";
            else if (totalScore >= 70) level = "中等";
            else if (totalScore >= 60) level = "一般";
            else level = "低";
            data.put("participation_level", level);
        }

        return heatData;
    }

    @Override
    public Map<String, Object> selectParticipationStats()
    {
        return participationHeatMapper.selectParticipationStats();
    }
}