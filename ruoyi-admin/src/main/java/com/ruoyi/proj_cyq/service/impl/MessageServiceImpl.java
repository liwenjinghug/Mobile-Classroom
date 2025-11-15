package com.ruoyi.proj_cyq.service.impl;

import java.util.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.text.SimpleDateFormat; // 引入 SimpleDateFormat
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
    private TodoMapper todoMapper; //

    @Autowired
    private HomeworkMapper homeworkMapper;

    @Override
    public List<Map<String, Object>> getMessageList(Long userId) {
        List<Map<String, Object>> messageList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        System.out.println("=== 开始获取用户 " + userId + " 的消息列表 ===");

        // 1. 获取待办消息 (查询 保持不变)
        List<Todo> todoMessages = todoMapper.selectTodoMessages(userId); //
        System.out.println("✅ 查询到的未完成待办消息数量: " + todoMessages.size());

        for (Todo todo : todoMessages) { //
            System.out.println("📝 待办消息 - ID: " + todo.getTodoId() +
                    ", 标题: " + todo.getTitle() +
                    ", 消息状态: " + todo.getMessageStatus() +
                    ", 消息已读: " + todo.getMessageRead());

            Map<String, Object> message = new HashMap<>();
            message.put("messageId", "todo_" + todo.getTodoId());
            message.put("type", "todo");
            message.put("sender", "系统提醒");

            // 【已修复】: 忠实反映数据库状态
            message.put("isRead", todo.getMessageRead());

            message.put("todoId", todo.getTodoId());

            boolean isReminderTime = todo.getRemindTime() != null && todo.getRemindTime().before(new Date());

            if (isReminderTime) {
                // *** 提醒逻辑 (保持不变) ***
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
                // *** 常规逻辑 (保持不变) ***
                message.put("title", "待办事项提醒");
                message.put("content", "您有未完成的待办事项：" + todo.getTitle());
                message.put("sendTime", todo.getCreateTime()); //
            }

            messageList.add(message);
        }

        // 2. 获取作业消息
        List<Map<String, Object>> homeworkMessages = getHomeworkMessages(userId);
        System.out.println("📚 查询到的作业消息数量: " + homeworkMessages.size());
        messageList.addAll(homeworkMessages);

        // 3. 排序
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


    private Date convertToDate(Object timeObj) { //
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


    private List<Map<String, Object>> getHomeworkMessages(Long userId) { //
        List<Map<String, Object>> messages = new ArrayList<>();
        try {
            List<Map<String, Object>> homeworkList = homeworkMapper.selectHomeworkByUserId(userId);
            for (Map<String, Object> homework : homeworkList) {
                if ("0".equals(homework.get("message_status"))) {
                    Map<String, Object> message = new HashMap<>();
                    // ... (省略内部代码, 保持不变) ...
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
            List<Map<String, Object>> homeworkList = homeworkMapper.selectHomeworkMessages();
            for (Map<String, Object> homework : homeworkList) {
                Map<String, Object> message = new HashMap<>();
                // ... (省略内部代码, 保持不变) ...
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
        int todoUnreadCount = todoMapper.selectUnreadMessageCount(userId); //
        int homeworkUnreadCount = homeworkMapper.selectUnreadHomeworkMessageCount();
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

    // ==========【 核心修改 】==========
    // 删除待办消息（软删除）
    private int deleteTodoMessage(Long todoId) { //

        // 1. 获取待办事项的当前状态
        Todo existingTodo = todoMapper.selectTodoById(todoId); //
        if (existingTodo == null) {
            return 0;
        }

        // 2. 检查它是否已经是“即将截止”状态
        boolean isDueSoon = existingTodo.getRemindTime() != null &&
                existingTodo.getRemindTime().before(new Date());

        // 3. 准备更新对象
        Todo todoUpdate = new Todo();
        todoUpdate.setTodoId(todoId);
        todoUpdate.setUpdateTime(new Date());
        todoUpdate.setUpdateBy(SecurityUtils.getUsername());

        // 4. 应用智能逻辑
        if (isDueSoon) {
            // 用户删除的是“即将截止”的消息，设为状态'2' (永久忽略)
            todoUpdate.setMessageStatus("2");
            todoUpdate.setMessageRead("1");   // 标记为已读
        } else {
            // 用户删除的是“普通”消息，设为状态'1' (等待重现)
            todoUpdate.setMessageStatus("1"); //
            todoUpdate.setMessageRead("0");   // 设为未读，以便重现时提醒
        }

        // 5. 执行更新
        return todoMapper.updateTodoMessageStatus(todoUpdate); //
    }

    // 删除作业消息（软删除）
    private int deleteHomeworkMessage(Long homeworkId) { //
        return homeworkMapper.updateHomeworkMessageStatus(
                homeworkId,
                "1", // message_status 设为已删除
                "1", // message_read 设为已读
                SecurityUtils.getUsername(),
                new Date()
        );
    }
}