package com.ruoyi.proj_cyq.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.*;
import com.ruoyi.proj_cyq.domain.Homework;
import java.util.Date;

@Mapper
public interface HomeworkMapper {

    @Select("SELECT homework_id, title as homework_name, content, " +
            "deadline, create_by as sender, create_time as send_time, " +
            "message_status, message_read, course_id, session_id " +  // 添加 course_id 和 session_id
            "FROM class_homework WHERE status = '0' ORDER BY create_time DESC")
    List<Map<String, Object>> selectAllHomework();

    @Select("SELECT h.homework_id, h.title as homework_name, h.content, " +
            "h.deadline, h.create_by as sender, h.create_time as send_time, " +
            "h.message_status, h.message_read, h.course_id, h.session_id " +  // 添加 course_id 和 session_id
            "FROM class_homework h " +
            "JOIN student_course sc ON h.course_id = sc.course_id " +  // 这里已经使用 course_id 关联，是正确的
            "WHERE sc.user_id = #{userId} AND h.status = '0' " +
            "ORDER BY h.create_time DESC")
    List<Map<String, Object>> selectHomeworkByUserId(@Param("userId") Long userId);

    // 修改：查询用于消息中心的作业（未删除的消息）
    @Select("SELECT homework_id, title as homework_name, content, " +
            "deadline, create_by as sender, create_time as send_time, " +
            "message_status, message_read, course_id, session_id " +  // 添加 course_id 和 session_id
            "FROM class_homework " +
            "WHERE status = '0' AND (message_status = '0' OR message_status IS NULL) " +  // 修改这里
            "ORDER BY create_time DESC")
    List<Map<String, Object>> selectHomeworkMessages();

    // 新增：根据课程ID查询作业
    @Select("SELECT homework_id, title as homework_name, content, " +
            "deadline, create_by as sender, create_time as send_time, " +
            "message_status, message_read, course_id, session_id " +
            "FROM class_homework " +
            "WHERE course_id = #{courseId} AND status = '0' " +
            "ORDER BY create_time DESC")
    List<Map<String, Object>> selectHomeworkByCourseId(@Param("courseId") Long courseId);

    // 新增：根据课堂ID查询作业
    @Select("SELECT homework_id, title as homework_name, content, " +
            "deadline, create_by as sender, create_time as send_time, " +
            "message_status, message_read, course_id, session_id " +
            "FROM class_homework " +
            "WHERE session_id = #{sessionId} AND status = '0' " +
            "ORDER BY create_time DESC")
    List<Map<String, Object>> selectHomeworkBySessionId(@Param("sessionId") Long sessionId);

    // 新增：更新作业消息状态
    @Update("UPDATE class_homework SET " +
            "message_status = #{messageStatus}, message_read = #{messageRead}, " +
            "update_by = #{updateBy}, update_time = #{updateTime} " +
            "WHERE homework_id = #{homeworkId}")
    int updateHomeworkMessageStatus(@Param("homeworkId") Long homeworkId,
                                    @Param("messageStatus") String messageStatus,
                                    @Param("messageRead") String messageRead,
                                    @Param("updateBy") String updateBy,
                                    @Param("updateTime") Date updateTime);

    // 修改：获取作业未读消息数量
    @Select("SELECT COUNT(*) FROM class_homework " +
            "WHERE status = '0' AND (message_status = '0' OR message_status IS NULL) AND (message_read = '0' OR message_read IS NULL)")  // 修改这里
    int selectUnreadHomeworkMessageCount();
}