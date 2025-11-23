package com.ruoyi.proj_lw.service.impl;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.proj_lw.domain.ClassJoinApplication;
import com.ruoyi.proj_lw.domain.ClassSession;
import com.ruoyi.proj_lw.domain.ClassSessionStudent;
import com.ruoyi.proj_lw.domain.ClassStudentLw;
import com.ruoyi.proj_lw.mapper.ClassJoinApplicationMapper;
import com.ruoyi.proj_lw.mapper.ClassSessionMapper;
import com.ruoyi.proj_lw.mapper.ClassSessionStudentMapper;
import com.ruoyi.proj_lw.mapper.ClassStudentLwMapper;
import com.ruoyi.proj_lw.service.IClassJoinApplicationService;

/**
 * 课堂加入申请Service业务层处理
 */
@Service
public class ClassJoinApplicationServiceImpl implements IClassJoinApplicationService {

    private final ClassJoinApplicationMapper classJoinApplicationMapper;
    private final ClassSessionStudentMapper classSessionStudentMapper;
    private final ClassStudentLwMapper classStudentLwMapper;
    private final ClassSessionMapper classSessionMapper; // 添加这个

    @Autowired
    public ClassJoinApplicationServiceImpl(ClassJoinApplicationMapper classJoinApplicationMapper,
                                           ClassSessionStudentMapper classSessionStudentMapper,
                                           ClassStudentLwMapper classStudentLwMapper,
                                           ClassSessionMapper classSessionMapper) { // 添加这个参数
        this.classJoinApplicationMapper = classJoinApplicationMapper;
        this.classSessionStudentMapper = classSessionStudentMapper;
        this.classStudentLwMapper = classStudentLwMapper;
        this.classSessionMapper = classSessionMapper; // 添加这个
    }

    /**
     * 查询课堂加入申请
     */
    @Override
    public ClassJoinApplication selectClassJoinApplicationByApplicationId(Long applicationId) {
        return classJoinApplicationMapper.selectClassJoinApplicationByApplicationId(applicationId);
    }

    /**
     * 查询课堂加入申请列表
     */
    @Override
    public List<ClassJoinApplication> selectClassJoinApplicationList(ClassJoinApplication classJoinApplication) {
        // 这里需要根据实际情况实现
        return classJoinApplicationMapper.selectClassJoinApplicationList(classJoinApplication);
    }

    /**
     * 新增课堂加入申请
     */
    @Override
    public int insertClassJoinApplication(ClassJoinApplication classJoinApplication) {
        return classJoinApplicationMapper.insertClassJoinApplication(classJoinApplication);
    }

    /**
     * 修改课堂加入申请
     */
    @Override
    public int updateClassJoinApplication(ClassJoinApplication classJoinApplication) {
        return classJoinApplicationMapper.updateClassJoinApplication(classJoinApplication);
    }

    /**
     * 批量删除课堂加入申请
     */
    @Override
    public int deleteClassJoinApplicationByApplicationIds(Long[] applicationIds) {
        return classJoinApplicationMapper.deleteClassJoinApplicationByApplicationIds(applicationIds);
    }

    /**
     * 删除课堂加入申请信息
     */
    @Override
    public int deleteClassJoinApplicationByApplicationId(Long applicationId) {
        return classJoinApplicationMapper.deleteClassJoinApplicationByApplicationId(applicationId);
    }

    // 业务方法实现

    @Override
    @Transactional
    public int applyJoinClass(Long sessionId, Long userId) {
        // 根据用户ID查询学生信息
        ClassStudentLw student = classStudentLwMapper.selectClassStudentByUserId(userId);
        if (student == null) {
            throw new RuntimeException("学生信息不存在，请先完善个人信息");
        }

        Long studentId = student.getStudentId(); // 使用真实的学生ID

        // 检查是否已申请
        if (classJoinApplicationMapper.checkApplicationExists(sessionId, studentId) > 0) {
            throw new RuntimeException("您已申请加入该课堂，请勿重复申请");
        }

        // 检查是否已在课堂中
        if (classSessionStudentMapper.checkStudentInSession(sessionId, studentId) > 0) {
            throw new RuntimeException("您已在该课堂中，无需重复申请");
        }

        // 创建申请
        ClassJoinApplication application = new ClassJoinApplication();
        application.setSessionId(sessionId);
        application.setStudentId(studentId); // 使用真实的学生ID
        application.setStudentNo(student.getStudentNo());
        application.setStudentName(student.getStudentName());
        application.setStatus("0"); // 待审核
        application.setApplyTime(new Date());

        return classJoinApplicationMapper.insertClassJoinApplication(application);
    }

    @Override
    public List<ClassJoinApplication> getMyApplications(Long userId) {
        System.out.println("=== 查询我的申请 ===");
        System.out.println("用户ID: " + userId);

        // 根据用户ID查询学生信息
        ClassStudentLw student = classStudentLwMapper.selectClassStudentByUserId(userId);
        System.out.println("查询到的学生信息: " + student);

        if (student == null) {
            System.out.println("学生信息不存在，返回空列表");
            return new ArrayList<>();
        }

        Long studentId = student.getStudentId();
        System.out.println("对应的学生ID: " + studentId);

        List<ClassJoinApplication> applications = classJoinApplicationMapper.selectMyApplications(studentId);
        System.out.println("查询到的申请数量: " + (applications != null ? applications.size() : "null"));

        return applications != null ? applications : new ArrayList<>();
    }

    @Override
    public List<ClassJoinApplication> getPendingApplications(Long teacherId) {
        List<ClassJoinApplication> list = classJoinApplicationMapper.selectPendingApplications(teacherId);
        return list != null ? list : new ArrayList<>();
    }

    @Override
    @Transactional
    public int auditApplication(Long applicationId, String status, Long auditUserId, String remark) {
        ClassJoinApplication application = classJoinApplicationMapper.selectClassJoinApplicationByApplicationId(applicationId);
        if (application == null) {
            throw new RuntimeException("申请记录不存在");
        }

        // 更新申请状态
        int result = classJoinApplicationMapper.updateApplicationStatus(applicationId, status, auditUserId, remark);

        // 如果审核通过，将学生加入课堂
        if ("1".equals(status)) {
            // 检查是否已存在
            if (classSessionStudentMapper.checkStudentInSession(application.getSessionId(), application.getStudentId()) == 0) {
                ClassSessionStudent sessionStudent = new ClassSessionStudent();
                sessionStudent.setSessionId(application.getSessionId());
                sessionStudent.setStudentId(application.getStudentId());
                sessionStudent.setAssignedAt(new Date());
                classSessionStudentMapper.insertClassSessionStudent(sessionStudent);
            }
        }

        return result;
    }

    @Override
    @Transactional
    public int batchAuditApplications(List<Long> applicationIds, String status, Long auditUserId) {
        int result = classJoinApplicationMapper.batchUpdateApplicationStatus(applicationIds, status, auditUserId);

        // 批量处理通过的学生加入课堂
        if ("1".equals(status)) {
            for (Long applicationId : applicationIds) {
                ClassJoinApplication application = classJoinApplicationMapper.selectClassJoinApplicationByApplicationId(applicationId);
                if (application != null && classSessionStudentMapper.checkStudentInSession(
                        application.getSessionId(), application.getStudentId()) == 0) {

                    ClassSessionStudent sessionStudent = new ClassSessionStudent();
                    sessionStudent.setSessionId(application.getSessionId());
                    sessionStudent.setStudentId(application.getStudentId());
                    sessionStudent.setAssignedAt(new Date());
                    classSessionStudentMapper.insertClassSessionStudent(sessionStudent);
                }
            }
        }

        return result;
    }

    @Override
    public int cancelApplication(Long applicationId) {
        // 检查申请状态，只有待审核的申请可以取消
        ClassJoinApplication application = classJoinApplicationMapper.selectClassJoinApplicationByApplicationId(applicationId);
        if (application == null) {
            throw new RuntimeException("申请记录不存在");
        }
        if (!"0".equals(application.getStatus())) {
            throw new RuntimeException("只能取消待审核的申请");
        }
        return classJoinApplicationMapper.deleteClassJoinApplicationByApplicationId(applicationId);
    }

    @Override
    @Transactional
    public int quitClass(Long sessionId, Long userId) {
        // 根据用户ID查询学生信息
        ClassStudentLw student = classStudentLwMapper.selectClassStudentByUserId(userId);
        if (student == null) {
            throw new RuntimeException("学生信息不存在");
        }

        Long studentId = student.getStudentId();

        // 检查学生是否在该课堂中
        if (classSessionStudentMapper.checkStudentInSession(sessionId, studentId) == 0) {
            throw new RuntimeException("您不在该课堂中");
        }

        // 从课堂中删除学生
        int result = classSessionStudentMapper.deleteClassSessionStudent(sessionId, studentId);

        // 同时删除相关的申请记录
        classJoinApplicationMapper.deleteApplicationBySessionAndStudent(sessionId, studentId);

        return result;
    }

    @Override
    public List<ClassJoinApplication> getPendingApplicationsByTeacher(String teacherUsername) {
        System.out.println("根据教师用户名查询待审核申请: " + teacherUsername);

        // 查询该教师管理的所有课堂的待审核申请
        List<ClassJoinApplication> list = classJoinApplicationMapper.selectPendingApplicationsByTeacher(teacherUsername);
        return list != null ? list : new ArrayList<>();
    }

    @Override
    public boolean isTeacherOfSession(Long sessionId, String teacherUsername) {
        // 查询课堂信息

        ClassSession session = classSessionMapper.selectSessionById(sessionId);
        if (session == null) {
            return false;
        }
        // 检查课堂的教师名称是否匹配当前用户名
        return teacherUsername.equals(session.getTeacher());
    }
}