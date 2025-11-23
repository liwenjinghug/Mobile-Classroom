package com.ruoyi.proj_lw.service.impl;

import com.ruoyi.proj_lw.domain.ClassSessionStudent;
import com.ruoyi.proj_lw.domain.ClassStudentLw;
import com.ruoyi.proj_lw.mapper.ClassSessionStudentMapper;
import com.ruoyi.proj_lw.mapper.ClassStudentLwMapper;
import com.ruoyi.proj_lw.service.IClassManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * 课堂管理Service业务层处理
 */
@Service
public class ClassManagementServiceImpl implements IClassManagementService {

    @Autowired
    private ClassSessionStudentMapper classSessionStudentMapper;

    @Autowired
    private ClassStudentLwMapper classStudentLwMapper;

    @Override
    public List<ClassSessionStudent> getClassStudents(Long sessionId, ClassStudentLw query) {
        try {
            System.out.println("Service: 获取课堂 " + sessionId + " 的学生列表");

            List<ClassSessionStudent> list = classSessionStudentMapper.selectClassSessionStudentList(sessionId);

            // 如果有查询条件，进行过滤
            if (list != null && query != null && query.getStudentName() != null && !query.getStudentName().isEmpty()) {
                List<ClassSessionStudent> filteredList = new ArrayList<>();
                for (ClassSessionStudent student : list) {
                    if (student.getStudentName() != null &&
                            student.getStudentName().toLowerCase().contains(query.getStudentName().toLowerCase())) {
                        filteredList.add(student);
                    }
                }
                return filteredList;
            }

            return list != null ? list : new ArrayList<>();

        } catch (Exception e) {
            System.err.println("Service: 获取课堂学生列表失败: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<ClassStudentLw> searchAllStudents(String keyword, Long sessionId) {
        try {
            System.out.println("=== Service: 搜索学生 ===");
            System.out.println("关键词: " + keyword + ", 课堂ID: " + sessionId);

            List<ClassStudentLw> list;

            if (keyword == null || keyword.trim().isEmpty()) {
                // 搜索所有学生
                list = classStudentLwMapper.selectClassStudentList(new ClassStudentLw());
                System.out.println("搜索所有学生，数量: " + (list != null ? list.size() : "null"));
            } else {
                // 搜索学号或姓名包含关键词的学生
                list = classStudentLwMapper.searchStudentsByKeyword(keyword.trim());
                System.out.println("关键词搜索，数量: " + (list != null ? list.size() : "null"));
            }

            // 过滤掉已经在当前课堂中的学生
            if (list != null && sessionId != null) {
                System.out.println("开始过滤，sessionId: " + sessionId);
                List<ClassStudentLw> filteredList = new ArrayList<>();

                for (ClassStudentLw student : list) {
                    // 检查学生是否已在课堂中
                    int inSession = classSessionStudentMapper.checkStudentInSession(sessionId, student.getStudentId());
                    System.out.println("学生 " + student.getStudentName() + " (ID:" + student.getStudentId() +
                            ") 在课堂中: " + (inSession > 0 ? "是" : "否"));

                    if (inSession == 0) {
                        filteredList.add(student);
                    }
                }
                System.out.println("过滤前数量: " + list.size() + ", 过滤后数量: " + filteredList.size());
                return filteredList;
            } else {
                System.out.println("跳过过滤 - list: " + list + ", sessionId: " + sessionId);
            }

            return list != null ? list : new ArrayList<>();

        } catch (Exception e) {
            System.err.println("Service: 搜索学生失败: " + e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public int addStudentsToClass(Long sessionId, List<Long> studentIds) {
        try {
            System.out.println("Service: 添加 " + studentIds.size() + " 名学生到课堂 " + sessionId);

            int successCount = 0;

            for (Long studentId : studentIds) {
                // 检查学生是否已在课堂中
                if (classSessionStudentMapper.checkStudentInSession(sessionId, studentId) == 0) {
                    // 学生不在课堂中，添加
                    ClassSessionStudent sessionStudent = new ClassSessionStudent();
                    sessionStudent.setSessionId(sessionId);
                    sessionStudent.setStudentId(studentId);
                    sessionStudent.setAssignedAt(new Date());

                    int result = classSessionStudentMapper.insertClassSessionStudent(sessionStudent);
                    if (result > 0) {
                        successCount++;
                        System.out.println("成功添加学生 " + studentId + " 到课堂 " + sessionId);
                    }
                } else {
                    System.out.println("学生 " + studentId + " 已在课堂 " + sessionId + " 中，跳过");
                }
            }

            System.out.println("总共成功添加 " + successCount + " 名学生");
            return successCount;

        } catch (Exception e) {
            System.err.println("Service: 添加学生到课堂失败: " + e.getMessage());
            throw new RuntimeException("添加学生失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public int removeStudentFromClass(Long sessionId, Long studentId) {
        try {
            System.out.println("Service: 从课堂 " + sessionId + " 移除学生 " + studentId);

            // 检查学生是否在课堂中
            if (classSessionStudentMapper.checkStudentInSession(sessionId, studentId) == 0) {
                throw new RuntimeException("学生不在该课堂中");
            }

            int result = classSessionStudentMapper.deleteClassSessionStudent(sessionId, studentId);
            System.out.println("移除学生结果: " + result);

            return result;

        } catch (Exception e) {
            System.err.println("Service: 移除学生失败: " + e.getMessage());
            throw new RuntimeException("移除学生失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public int batchRemoveStudents(Long sessionId, List<Long> studentIds) {
        try {
            System.out.println("Service: 从课堂 " + sessionId + " 批量移除 " + studentIds.size() + " 名学生");

            int successCount = 0;

            for (Long studentId : studentIds) {
                // 检查学生是否在课堂中
                if (classSessionStudentMapper.checkStudentInSession(sessionId, studentId) > 0) {
                    int result = classSessionStudentMapper.deleteClassSessionStudent(sessionId, studentId);
                    if (result > 0) {
                        successCount++;
                        System.out.println("成功移除学生 " + studentId);
                    }
                } else {
                    System.out.println("学生 " + studentId + " 不在课堂中，跳过");
                }
            }

            System.out.println("总共成功移除 " + successCount + " 名学生");
            return successCount;

        } catch (Exception e) {
            System.err.println("Service: 批量移除学生失败: " + e.getMessage());
            throw new RuntimeException("批量移除学生失败: " + e.getMessage());
        }
    }
}