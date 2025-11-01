package com.ruoyi.proj_fz.service;

import com.ruoyi.proj_fz.domain.HomeworkStatisticsDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface IHomeworkStatisticsService {

    List<HomeworkStatisticsDTO> getHomeworkStatisticsList();

    List<HomeworkStatisticsDTO> getHomeworkStatisticsListByFilter(Map<String, Object> params);

    HomeworkStatisticsDTO getHomeworkStatisticsById(Long homeworkId);

    List<Map<String, Object>> getScoreDistribution(Long homeworkId);

    List<Map<String, Object>> getSubmissionTrend(Long homeworkId);

    List<Map<String, Object>> getTeacherHomeworkOverview();

    List<Map<String, Object>> getStudentSubmissionDetails(Long homeworkId);

    Map<String, Object> getDashboardOverview();

    List<Map<String, Object>> getAllHomeworkBasicInfo();
    List<Map<String, Object>> getHomeworkSubmissionStats();

    List<Map<String, Object>> getCourseList();
    List<Map<String, Object>> getSessionList();

    List<Map<String, Object>> getSessionHomeworkOverview();

    void exportHomeworkData(Map<String, Object> params, HttpServletResponse response);
}