package com.ruoyi.proj_lw.service.impl;

import com.ruoyi.proj_lw.domain.ClassStudentLw;
import com.ruoyi.proj_lw.mapper.ClassStudentLwMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.proj_lw.domain.ClassSession;
import com.ruoyi.proj_lw.domain.ClassSessionStudent;
import com.ruoyi.proj_lw.mapper.ClassSessionMapper;
import com.ruoyi.proj_lw.mapper.ClassSessionStudentMapper;
import com.ruoyi.proj_lw.service.IStudentClassService;

import java.util.List;
import java.util.ArrayList;

@Service
public class StudentClassServiceImpl implements IStudentClassService {

    @Autowired
    private ClassSessionStudentMapper classSessionStudentMapper;

    @Autowired
    private ClassSessionMapper classSessionMapper;

    @Autowired
    private ClassStudentLwMapper classStudentLwMapper;;

    @Override
    public List<ClassSessionStudent> getJoinedClasses(Long userId) {
        System.out.println("=== 查询我的课堂 ===");
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

        // 直接查询已加入的课堂详细信息
        List<ClassSessionStudent> result = classSessionStudentMapper.selectJoinedClassesWithDetail(studentId);
        System.out.println("查询到的课堂数量: " + (result != null ? result.size() : "null"));

        if (result != null) {
            for (ClassSessionStudent item : result) {
                System.out.println("课堂信息: ID=" + item.getSessionId() +
                        ", 名称=" + item.getClassName() +
                        ", 上课时间=" + item.getWeekDay() + " " + item.getStartTime() + "-" + item.getEndTime());
            }
        }

        return result != null ? result : new ArrayList<>();
    }

    @Override
    public List<ClassSession> getAvailableClasses(Long studentId) {
        // 获取学生已加入的课堂ID
        List<Long> joinedSessionIds = classSessionStudentMapper.selectJoinedSessionIds(studentId);

        // 获取所有课堂（不限制状态）
        ClassSession query = new ClassSession();
        List<ClassSession> allSessions = classSessionMapper.selectSessionList(query);
        List<ClassSession> availableSessions = new ArrayList<>();

        for (ClassSession session : allSessions) {
            // 只排除已加入的课堂，不限制状态
            if (!joinedSessionIds.contains(session.getSessionId())) {
                availableSessions.add(session);
            }
        }
        return availableSessions;
    }

    @Override
    public List<ClassSession> getTeachingClasses(Long teacherId) {
        // 根据教师ID查询管理的课堂
        ClassSession query = new ClassSession();
        query.setTeacherId(teacherId);
        return classSessionMapper.selectSessionList(query);
    }
}