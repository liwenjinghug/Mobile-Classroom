package com.ruoyi.proj_lw.service;

import java.util.List;
import com.ruoyi.proj_lw.domain.ClassJoinApplication;

/**
 * 课堂加入申请Service接口
 */
public interface IClassJoinApplicationService {
    /**
     * 查询课堂加入申请
     */
    public ClassJoinApplication selectClassJoinApplicationByApplicationId(Long applicationId);

    /**
     * 查询课堂加入申请列表
     */
    public List<ClassJoinApplication> selectClassJoinApplicationList(ClassJoinApplication classJoinApplication);

    /**
     * 新增课堂加入申请
     */
    public int insertClassJoinApplication(ClassJoinApplication classJoinApplication);

    /**
     * 修改课堂加入申请
     */
    public int updateClassJoinApplication(ClassJoinApplication classJoinApplication);

    /**
     * 批量删除课堂加入申请
     */
    public int deleteClassJoinApplicationByApplicationIds(Long[] applicationIds);

    /**
     * 删除课堂加入申请信息
     */
    public int deleteClassJoinApplicationByApplicationId(Long applicationId);

    // 业务方法

    /**
     * 学生申请加入课堂
     */
    int applyJoinClass(Long sessionId, Long userId);

    /**
     * 学生查看我的申请列表
     */
    List<ClassJoinApplication> getMyApplications(Long userId);

    /**
     * 教师查看待审核申请
     */
    List<ClassJoinApplication> getPendingApplications(Long teacherId);

    /**
     * 根据教师用户名获取待审核申请
     */
    List<ClassJoinApplication> getPendingApplicationsByTeacher(String teacherUsername);

    /**
     * 审核申请
     */
    int auditApplication(Long applicationId, String status, Long auditUserId, String remark);

    /**
     * 批量审核
     */
    int batchAuditApplications(List<Long> applicationIds, String status, Long auditUserId);

    /**
     * 取消申请
     */
    int cancelApplication(Long applicationId);

    /**
     * 退出课堂
     */
    int quitClass(Long sessionId, Long userId);

    /**
     * 检查用户是否是某个课堂的教师
     */
    boolean isTeacherOfSession(Long sessionId, String teacherUsername);
}