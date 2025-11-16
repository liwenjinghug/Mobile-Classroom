package com.ruoyi.proj_cyq.service.impl;

import java.util.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.slf4j.Logger; // 【新增】
import org.slf4j.LoggerFactory; // 【新增】
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.proj_cyq.domain.Todo;
import com.ruoyi.proj_cyq.mapper.TodoMapper;
import com.ruoyi.proj_cyq.mapper.HomeworkMapper;
import com.ruoyi.proj_cyq.service.IMessageService;

@Service
public class MessageServiceImpl implements IMessageService {

    // 【新增】日志记录器
    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

    @Autowired
    private TodoMapper todoMapper;

    @Autowired
    private HomeworkMapper homeworkMapper;

    // ==========【 核心修改 1 】==========
    @Override
    public List<Map<String, Object>> getMessageList(Long userId) {
        List<Map<String, Object>> messageList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        System.out.println("=== 开始获取用户 " + userId + " 的消息列表 ===");

        // --- 1. 获取待办消息 (用 try-catch 包裹) ---
        try {
            List<Todo> todoMessages = todoMapper.selectTodoMessages(userId); //
            System.out.println("✅ 查询到的未完成待办消息数量: " + todoMessages.size());

            for (Todo todo : todoMessages) { //
                Map<String, Object> message = new HashMap<>();
                message.put("messageId", "todo_" + todo.getTodoId());
                message.put("type", "todo");
                message.put("sender", "系统提醒");
                message.put("isRead", todo.getMessageRead());
                message.put("todoId", todo.getTodoId());
                boolean isReminderTime = todo.getRemindTime() != null && todo.getRemindTime().before(new Date());
                if (isReminderTime) {
                    message.put("title", "待办事项【即将截止】");
                    String content = "您的待办事项 “" + todo.getTitle() + "” ";
                    if (todo.getEndTime() != null) {
                        content += "将于 " + sdf.format(todo.getEndTime()) + " 截止，请尽快处理！";
                    } else {
                        content += "即将截止，请尽快处理！";
                    }
                    message.put("content", content);
                    message.put("sendTime", todo.getRemindTime());
                } else {
                    message.put("title", "待办事项提醒");
                    message.put("content", "您有未完成的待办事项：" + todo.getTitle());
                    message.put("sendTime", todo.getCreateTime()); //
                }
                messageList.add(message);
            }
        } catch (Exception e) {
            log.error("❌ 获取待办消息失败，用户ID: {}", userId, e);
            System.err.println("❌ 获取待办消息失败: " + e.getMessage());
        }

        // --- 2. 获取作业消息 (用 try-catch 包裹) ---
        try {
            List<Map<String, Object>> homeworkMessages = getHomeworkMessages(userId); //
            System.out.println("📚 查询到的作业消息数量: " + homeworkMessages.size());
            messageList.addAll(homeworkMessages);
        } catch (Exception e) {
            log.error("❌ 获取作业消息失败，用户ID: {}", userId, e);
            System.err.println("❌ 获取作业消息失败: " + e.getMessage());
            // 即使作业消息失败，我们也不抛出异常，而是继续返回待办消息
        }


        // --- 3. 排序 (保持不变) ---
        messageList.sort((a, b) -> { //
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


    private Date convertToDate(Object timeObj) { //
        if (timeObj == null) { return null; }
        if (timeObj instanceof Date) { return (Date) timeObj; }
        else if (timeObj instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) timeObj;
            return Date.from(localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
        } else if (timeObj instanceof java.sql.Timestamp) {
            return new Date(((java.sql.Timestamp) timeObj).getTime());
        }
        return null;
    }


    private List<Map<String, Object>> getHomeworkMessages(Long userId) { //
        // 这个方法内部的 try-catch 仍然很危险，但我们已在 getMessageList 中将其隔离
        List<Map<String, Object>> messages = new ArrayList<>();
        try {
            List<Map<String, Object>> homeworkList = homeworkMapper.selectHomeworkByUserId(userId); //
            for (Map<String, Object> homework : homeworkList) {
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
            // 这个 catch 块 可能会再次抛出异常
            log.warn("获取用户作业失败 ({}). 尝试获取所有作业.", e.getMessage());
            List<Map<String, Object>> homeworkList = homeworkMapper.selectHomeworkMessages(); //
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

    // ==========【 核心修改 2 】==========
    @Override
    public Map<String, Object> getMessageStats(Long userId) {
        // 1. 获取消息列表
        List<Map<String, Object>> messageList = getMessageList(userId); //

        // 2. 统计变量
        int totalCount = 0;
        int unreadCount = 0;
        int todoCount = 0;
        int homeworkCount = 0;

        // 3. 【修改】健壮性检查
        if (messageList != null) {
            totalCount = messageList.size(); //
            for (Map<String, Object> msg : messageList) { //
                // 【新增】防止NPE
                if (msg == null) {
                    continue;
                }

                // 统计未读
                if ("0".equals(String.valueOf(msg.get("isRead")))) { //
                    unreadCount++;
                }
                // 统计类型
                if ("todo".equals(msg.get("type"))) { //
                    todoCount++;
                } else if ("homework".equals(msg.get("type"))) { //
                    homeworkCount++;
                }
            }
        }

        // 4. 组装按类型统计
        List<Map<String, Object>> typeStats = new ArrayList<>();
        Map<String, Object> todoStat = new HashMap<>();
        todoStat.put("name", "待办事项");
        todoStat.put("value", todoCount);
        typeStats.add(todoStat);

        Map<String, Object> homeworkStat = new HashMap<>();
        homeworkStat.put("name", "作业消息");
        homeworkStat.put("value", homeworkCount);
        typeStats.add(homeworkStat);


        // 5. 组装按已读/未读统计
        List<Map<String, Object>> readStats = new ArrayList<>();
        Map<String, Object> unreadStat = new HashMap<>();
        unreadStat.put("name", "未读");
        unreadStat.put("value", unreadCount);
        readStats.add(unreadStat);

        Map<String, Object> readStat = new HashMap<>();
        readStat.put("name", "已读");
        readStat.put("value", totalCount - unreadCount);
        readStats.add(readStat);

        // 6. 组装最终结果
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("unreadCount", unreadCount);
        result.put("typeStats", typeStats);
        result.put("readStats", readStats);

        return result;
    }


    // ... ( getUnreadCount, mark...AsRead, delete... 方法保持不变 ) ...

    @Override
    public int getUnreadCount(Long userId) {
        int todoUnreadCount = 0;
        int homeworkUnreadCount = 0;
        try {
            todoUnreadCount = todoMapper.selectUnreadMessageCount(userId); //
        } catch (Exception e) {
            log.error("获取待办未读数失败", e);
        }
        try {
            homeworkUnreadCount = homeworkMapper.selectUnreadHomeworkMessageCount();
        } catch (Exception e) {
            log.error("获取作业未读数失败", e);
        }
        System.out.println("📊 未读消息统计 - 待办: " + todoUnreadCount + ", 作业: " + homeworkUnreadCount);
        return todoUnreadCount + homeworkUnreadCount;
    }

    @Override
    public int markTodoAsRead(Long todoId) { //
        System.out.println("🔔 标记待办消息为已读，todoId: " + todoId);
        Todo todo = new Todo();
        todo.setTodoId(todoId);
        todo.setMessageRead("1");
        todo.setUpdateTime(new Date());
        todo.setUpdateBy(SecurityUtils.getUsername());
        int result = todoMapper.updateTodoMessageStatus(todo); //
        System.out.println("✅ 更新结果: " + result);
        return result;
    }

    @Override
    public int markHomeworkAsRead(Long homeworkId) { //
        System.out.println("🔔 标记作业消息为已读，homeworkId: " + homeworkId);
        int result = homeworkMapper.updateHomeworkMessageStatus(
                homeworkId, "0", "1", SecurityUtils.getUsername(), new Date()
        );
        System.out.println("✅ 更新结果: " + result);
        return result;
    }

    @Override
    public int markAllAsRead(Long userId) { //
        System.out.println("🔔 批量标记所有消息为已读，userId: " + userId);
        int successCount = 0;
        try {
            List<Todo> todoMessages = todoMapper.selectTodoMessages(userId); //
            for (Todo todo : todoMessages) {
                boolean isReAppeared = "1".equals(todo.getMessageStatus());
                boolean isUnread = "0".equals(todo.getMessageRead());
                if (isReAppeared || isUnread) {
                    int result = markTodoAsRead(todo.getTodoId());
                    if (result > 0) {
                        successCount++;
                    }
                }
            }
            List<Map<String, Object>> homeworkMessages = getHomeworkMessages(userId); //
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
    public int deleteMessage(String messageId) { //
        if (messageId.startsWith("todo_")) {
            Long todoId = Long.parseLong(messageId.replace("todo_", ""));
            return deleteTodoMessage(todoId);
        } else if (messageId.startsWith("homework_")) {
            Long homeworkId = Long.parseLong(messageId.replace("homework_", ""));
            return deleteHomeworkMessage(homeworkId);
        }
        return 0;
    }

    private int deleteTodoMessage(Long todoId) { //
        // 这是我们之前的修复的最终逻辑，保持不变
        Todo existingTodo = todoMapper.selectTodoById(todoId); //
        if (existingTodo == null) {
            return 0;
        }
        boolean isDueSoon = existingTodo.getRemindTime() != null &&
                existingTodo.getRemindTime().before(new Date());
        Todo todoUpdate = new Todo();
        todoUpdate.setTodoId(todoId);
        todoUpdate.setUpdateTime(new Date());
        todoUpdate.setUpdateBy(SecurityUtils.getUsername());
        if (isDueSoon) {
            todoUpdate.setMessageStatus("2"); // 永久忽略
            todoUpdate.setMessageRead("1");
        } else {
            todoUpdate.setMessageStatus("1"); // 等待重现
            todoUpdate.setMessageRead("0");
        }
        return todoMapper.updateTodoMessageStatus(todoUpdate); //
    }

    private int deleteHomeworkMessage(Long homeworkId) { //
        return homeworkMapper.updateHomeworkMessageStatus(
                homeworkId, "1", "1", SecurityUtils.getUsername(), new Date()
        );
    }
}