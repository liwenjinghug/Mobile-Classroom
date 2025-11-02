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
            "message_status, message_read " +
            "FROM class_homework WHERE status = '0' ORDER BY create_time DESC")
    List<Map<String, Object>> selectAllHomework();

    @Select("SELECT h.homework_id, h.title as homework_name, h.content, " +
            "h.deadline, h.create_by as sender, h.create_time as send_time, " +
            "h.message_status, h.message_read " +
            "FROM class_homework h " +
            "JOIN student_course sc ON h.course_id = sc.course_id " +
            "WHERE sc.user_id = #{userId} AND h.status = '0' " +
            "ORDER BY h.create_time DESC")
    List<Map<String, Object>> selectHomeworkByUserId(@Param("userId") Long userId);

    // 新增：查询用于消息中心的作业（未删除的消息）
    @Select("SELECT homework_id, title as homework_name, content, " +
            "deadline, create_by as sender, create_time as send_time, " +
            "message_status, message_read " +
            "FROM class_homework " +
            "WHERE status = '0' AND message_status = '0' " +
            "ORDER BY create_time DESC")
    List<Map<String, Object>> selectHomeworkMessages();

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

    // 新增：获取作业未读消息数量
    @Select("SELECT COUNT(*) FROM class_homework " +
            "WHERE status = '0' AND message_status = '0' AND message_read = '0'")
    int selectUnreadHomeworkMessageCount();
}