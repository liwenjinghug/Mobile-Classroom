package com.ruoyi.proj_fz.service;

import com.ruoyi.proj_fz.domain.HomeworkStatisticsDTO;
import java.util.List;
import java.util.Map;

public interface IHomeworkStatisticsService {

    List<HomeworkStatisticsDTO> getHomeworkStatisticsList();

    HomeworkStatisticsDTO getHomeworkStatisticsById(Long homeworkId);

    List<Map<String, Object>> getScoreDistribution(Long homeworkId);

    List<Map<String, Object>> getSubmissionTrend(Long homeworkId);

    List<Map<String, Object>> getTeacherHomeworkOverview();

    Map<String, Object> getDashboardData();

    List<Map<String, Object>> getStudentSubmissionDetails(Long homeworkId);

    Map<String, Object> getDashboardOverview();

    // 调试方法
    List<Map<String, Object>> getAllHomeworkBasicInfo();
    List<Map<String, Object>> getHomeworkSubmissionStats();
}