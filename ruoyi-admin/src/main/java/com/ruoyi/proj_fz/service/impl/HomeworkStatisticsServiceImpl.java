package com.ruoyi.proj_fz.service.impl;

import com.ruoyi.proj_fz.domain.HomeworkStatisticsDTO;
import com.ruoyi.proj_fz.mapper.HomeworkStatisticsMapper;
import com.ruoyi.proj_fz.service.IHomeworkStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HomeworkStatisticsServiceImpl implements IHomeworkStatisticsService {

    @Autowired
    private HomeworkStatisticsMapper statisticsMapper;

    @Override
    public List<HomeworkStatisticsDTO> getHomeworkStatisticsList() {
        List<HomeworkStatisticsDTO> statisticsList = statisticsMapper.selectHomeworkStatisticsList();

        // 为每个作业添加成绩分布数据
        for (HomeworkStatisticsDTO statistics : statisticsList) {
            List<Map<String, Object>> scoreDistribution = statisticsMapper.selectScoreDistribution(statistics.getHomeworkId());
            Map<String, Integer> distributionMap = new HashMap<>();
            for (Map<String, Object> dist : scoreDistribution) {
                distributionMap.put((String) dist.get("scoreRange"), ((Long) dist.get("count")).intValue());
            }
            statistics.setScoreDistribution(distributionMap);
        }

        return statisticsList;
    }

    @Override
    public HomeworkStatisticsDTO getHomeworkStatisticsById(Long homeworkId) {
        HomeworkStatisticsDTO statistics = statisticsMapper.selectHomeworkStatisticsById(homeworkId);
        if (statistics != null) {
            // 添加成绩分布
            List<Map<String, Object>> scoreDistribution = statisticsMapper.selectScoreDistribution(homeworkId);
            Map<String, Integer> distributionMap = new HashMap<>();
            for (Map<String, Object> dist : scoreDistribution) {
                distributionMap.put((String) dist.get("scoreRange"), ((Long) dist.get("count")).intValue());
            }
            statistics.setScoreDistribution(distributionMap);

            // 添加提交趋势
            List<Map<String, Object>> trendData = statisticsMapper.selectSubmissionTrend(homeworkId);
            statistics.setChartData(trendData);
        }
        return statistics;
    }

    @Override
    public List<Map<String, Object>> getScoreDistribution(Long homeworkId) {
        return statisticsMapper.selectScoreDistribution(homeworkId);
    }

    @Override
    public List<Map<String, Object>> getSubmissionTrend(Long homeworkId) {
        return statisticsMapper.selectSubmissionTrend(homeworkId);
    }

    @Override
    public List<Map<String, Object>> getTeacherHomeworkOverview() {
        return statisticsMapper.selectTeacherHomeworkOverview();
    }

    @Override
    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();

        // 获取概览数据
        Map<String, Object> overview = statisticsMapper.selectDashboardOverview();
        dashboardData.put("overview", overview);

        // 获取课程概览
        List<Map<String, Object>> courseOverview = statisticsMapper.selectTeacherHomeworkOverview();
        dashboardData.put("courseOverview", courseOverview);

        // 获取最近的作业统计
        List<HomeworkStatisticsDTO> recentHomework = statisticsMapper.selectHomeworkStatisticsList();
        dashboardData.put("recentHomework", recentHomework.size() > 5 ? recentHomework.subList(0, 5) : recentHomework);

        return dashboardData;
    }

    @Override
    public List<Map<String, Object>> getStudentSubmissionDetails(Long homeworkId) {
        return statisticsMapper.selectStudentSubmissionDetails(homeworkId);
    }

    @Override
    public Map<String, Object> getDashboardOverview() {
        return statisticsMapper.selectDashboardOverview();
    }

    @Override
    public List<Map<String, Object>> getAllHomeworkBasicInfo() {
        return statisticsMapper.selectAllHomeworkBasicInfo();
    }

    @Override
    public List<Map<String, Object>> getHomeworkSubmissionStats() {
        return statisticsMapper.selectHomeworkSubmissionStats();
    }
}