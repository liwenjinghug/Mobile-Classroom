package com.ruoyi.proj_cyq.service.impl;

import java.util.*;
import java.time.LocalDateTime;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.proj_cyq.domain.Todo;
import com.ruoyi.proj_cyq.mapper.TodoMapper;
import com.ruoyi.proj_cyq.mapper.HomeworkMapper;
import com.ruoyi.proj_cyq.service.IMessageService;

@Service
public class MessageServiceImpl implements IMessageService {

    @Autowired
    private TodoMapper todoMapper;

    @Autowired
    private HomeworkMapper homeworkMapper;

    @Override
    public List<Map<String, Object>> getMessageList(Long userId) {
        List<Map<String, Object>> messageList = new ArrayList<>();

        System.out.println("=== 开始获取用户 " + userId + " 的消息列表 ===");

        // 1. 获取待办消息（只获取未完成且未删除的消息）
        List<Todo> todoMessages = todoMapper.selectTodoMessages(userId);
        System.out.println("✅ 查询到的未完成待办消息数量: " + todoMessages.size());

        for (Todo todo : todoMessages) {
            System.out.println("📝 待办消息 - ID: " + todo.getTodoId() +
                    ", 标题: " + todo.getTitle() +
                    ", 状态: " + todo.getStatus() +
                    ", 消息状态: " + todo.getMessageStatus() +
                    ", 消息已读: " + todo.getMessageRead());

            // 创建消息对象
            Map<String, Object> message = new HashMap<>();
            message.put("messageId", "todo_" + todo.getTodoId());
            message.put("type", "todo");
            message.put("title", "待办事项提醒");
            message.put("content", "您有未完成的待办事项：" + todo.getTitle());
            message.put("sender", "系统提醒");
            message.put("sendTime", todo.getCreateTime());
            message.put("isRead", todo.getMessageRead());
            message.put("todoId", todo.getTodoId());
            messageList.add(message);

            System.out.println("✅ 已添加待办消息到消息列表: " + todo.getTitle());
        }

        // 2. 获取作业消息（只获取未删除的消息）
        List<Map<String, Object>> homeworkMessages = getHomeworkMessages(userId);
        System.out.println("📚 查询到的作业消息数量: " + homeworkMessages.size());
        messageList.addAll(homeworkMessages);

        // 按时间倒序排序
        messageList.sort((a, b) -> {
            Object timeA = a.get("sendTime");
            Object timeB = b.get("sendTime");

            Date dateA = convertToDate(timeA);
            Date dateB = convertToDate(timeB);

            if (dateA != null && dateB != null) {
                return dateB.compareTo(dateA);
            }
            return 0;
        });

        System.out.println("🎯 最终消息总数: " + messageList.size());
        System.out.println("=== 消息列表获取完成 ===\n");
        return messageList;
    }

    /**
     * 将不同类型的时间对象转换为 Date
     */
    private Date convertToDate(Object timeObj) {
        if (timeObj == null) {
            return null;
        }

        if (timeObj instanceof Date) {
            return (Date) timeObj;
        } else if (timeObj instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) timeObj;
            return Date.from(localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
        } else if (timeObj instanceof java.sql.Timestamp) {
            return new Date(((java.sql.Timestamp) timeObj).getTime());
        }

        return null;
    }

    /**
     * 获取作业相关消息（只获取未删除的消息）
     */
    private List<Map<String, Object>> getHomeworkMessages(Long userId) {
        List<Map<String, Object>> messages = new ArrayList<>();

        try {
            // 先尝试根据用户ID查询相关作业
            List<Map<String, Object>> homeworkList = homeworkMapper.selectHomeworkByUserId(userId);
            for (Map<String, Object> homework : homeworkList) {
                // 检查消息状态，只显示未删除的消息
                if ("0".equals(homework.get("message_status"))) {
                    Map<String, Object> message = new HashMap<>();
                    message.put("messageId", "homework_" + homework.get("homework_id"));
                    message.put("type", "homework");
                    message.put("title", "新作业发布");
                    message.put("content", homework.get("content"));
                    message.put("sender", homework.get("sender"));
                    message.put("sendTime", convertToDate(homework.get("send_time")));
                    message.put("deadline", convertToDate(homework.get("deadline")));
                    message.put("homeworkName", homework.get("homework_name"));
                    message.put("isRead", homework.get("message_read") != null ? homework.get("message_read") : "0");
                    message.put("homeworkId", homework.get("homework_id"));
                    messages.add(message);
                }
            }
        } catch (Exception e) {
            // 如果student_course表不存在，使用简化版本
            List<Map<String, Object>> homeworkList = homeworkMapper.selectHomeworkMessages();
            for (Map<String, Object> homework : homeworkList) {
                Map<String, Object> message = new HashMap<>();
                message.put("messageId", "homework_" + homework.get("homework_id"));
                message.put("type", "homework");
                message.put("title", "新作业发布");
                message.put("content", homework.get("content"));
                message.put("sender", homework.get("sender"));
                message.put("sendTime", convertToDate(homework.get("send_time")));
                message.put("deadline", convertToDate(homework.get("deadline")));
                message.put("homeworkName", homework.get("homework_name"));
                message.put("isRead", homework.get("message_read") != null ? homework.get("message_read") : "0");
                message.put("homeworkId", homework.get("homework_id"));
                messages.add(message);
            }
        }

        return messages;
    }

    @Override
    public int getUnreadCount(Long userId) {
        int todoUnreadCount = todoMapper.selectUnreadMessageCount(userId);
        int homeworkUnreadCount = homeworkMapper.selectUnreadHomeworkMessageCount();
        System.out.println("📊 未读消息统计 - 待办: " + todoUnreadCount + ", 作业: " + homeworkUnreadCount);
        return todoUnreadCount + homeworkUnreadCount;
    }

    @Override
    public int markTodoAsRead(Long todoId) {
        System.out.println("🔔 标记待办消息为已读，todoId: " + todoId);
        Todo todo = new Todo();
        todo.setTodoId(todoId);
        todo.setMessageRead("1");
        todo.setUpdateTime(new Date());
        todo.setUpdateBy(SecurityUtils.getUsername());
        int result = todoMapper.updateTodoMessageStatus(todo);
        System.out.println("✅ 更新结果: " + result);
        return result;
    }

    @Override
    public int markHomeworkAsRead(Long homeworkId) {
        System.out.println("🔔 标记作业消息为已读，homeworkId: " + homeworkId);
        int result = homeworkMapper.updateHomeworkMessageStatus(
                homeworkId,
                "0", // message_status 不变
                "1", // message_read 设为已读
                SecurityUtils.getUsername(),
                new Date()
        );
        System.out.println("✅ 更新结果: " + result);
        return result;
    }

    @Override
    public int markAllAsRead(Long userId) {
        System.out.println("🔔 批量标记所有消息为已读，userId: " + userId);
        int successCount = 0;

        try {
            // 标记所有待办消息为已读
            List<Todo> todoMessages = todoMapper.selectTodoMessages(userId);
            for (Todo todo : todoMessages) {
                if ("0".equals(todo.getMessageRead())) {
                    int result = markTodoAsRead(todo.getTodoId());
                    if (result > 0) {
                        successCount++;
                    }
                }
            }

            // 标记所有作业消息为已读
            List<Map<String, Object>> homeworkMessages = getHomeworkMessages(userId);
            for (Map<String, Object> homework : homeworkMessages) {
                if ("0".equals(homework.get("isRead"))) {
                    Long homeworkId = Long.parseLong(homework.get("homework_id").toString());
                    int result = markHomeworkAsRead(homeworkId);
                    if (result > 0) {
                        successCount++;
                    }
                }
            }

            System.out.println("✅ 批量标记完成，成功数量: " + successCount);
        } catch (Exception e) {
            System.err.println("❌ 批量标记已读失败: " + e.getMessage());
            e.printStackTrace();
        }

        return successCount;
    }

    @Override
    public int deleteMessage(String messageId) {
        if (messageId.startsWith("todo_")) {
            Long todoId = Long.parseLong(messageId.replace("todo_", ""));
            return deleteTodoMessage(todoId);
        } else if (messageId.startsWith("homework_")) {
            Long homeworkId = Long.parseLong(messageId.replace("homework_", ""));
            return deleteHomeworkMessage(homeworkId);
        }
        return 0;
    }

    // 删除待办消息（软删除）
    private int deleteTodoMessage(Long todoId) {
        Todo todo = new Todo();
        todo.setTodoId(todoId);
        todo.setMessageStatus("1"); // 标记为消息已删除
        todo.setUpdateTime(new Date());
        todo.setUpdateBy(SecurityUtils.getUsername());
        return todoMapper.updateTodoMessageStatus(todo);
    }

    // 删除作业消息（软删除）
    private int deleteHomeworkMessage(Long homeworkId) {
        return homeworkMapper.updateHomeworkMessageStatus(
                homeworkId,
                "1", // message_status 设为已删除
                "1", // message_read 设为已读
                SecurityUtils.getUsername(),
                new Date()
        );
    }
}