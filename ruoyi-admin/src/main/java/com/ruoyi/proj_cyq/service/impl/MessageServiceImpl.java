package com.ruoyi.proj_cyq.service.impl;

import java.util.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.proj_cyq.domain.Todo;
import com.ruoyi.proj_cyq.mapper.TodoMapper;
import com.ruoyi.proj_cyq.mapper.HomeworkMapper;
import com.ruoyi.proj_lwj.mapper.ClassExamMapper;
import com.ruoyi.proj_lwj.domain.ClassExam;
import com.ruoyi.proj_cyq.service.IMessageService;

@Service
public class MessageServiceImpl implements IMessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

    @Autowired
    private TodoMapper todoMapper;
    @Autowired
    private HomeworkMapper homeworkMapper;
    @Autowired
    private ClassExamMapper classExamMapper;

    @Override
    public List<Map<String, Object>> getMessageList(Long userId) {
        List<Map<String, Object>> messageList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        // 1. 待办
        try {
            List<Todo> todoMessages = todoMapper.selectTodoMessages(userId);
            for (Todo todo : todoMessages) {
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
                        content += "将于 " + sdf.format(todo.getEndTime()) + " 截止！";
                    }
                    message.put("content", content);
                    message.put("sendTime", todo.getRemindTime());
                } else {
                    message.put("title", "待办事项提醒");
                    message.put("content", "您有未完成的待办事项：" + todo.getTitle());
                    message.put("sendTime", todo.getCreateTime());
                }
                messageList.add(message);
            }
        } catch (Exception e) {}

        // 2. 作业
        try {
            List<Map<String, Object>> homeworkMessages = getHomeworkMessages(userId);
            messageList.addAll(homeworkMessages);
        } catch (Exception e) {}

        // 3. 考试
        try {
            boolean isAdmin = SecurityUtils.getLoginUser().getUser().isAdmin();
            List<ClassExam> exams = classExamMapper.selectExamMessages(userId, isAdmin);
            for (ClassExam exam : exams) {
                Map<String, Object> message = new HashMap<>();
                message.put("messageId", "exam_" + exam.getId());
                message.put("type", "exam");
                message.put("title", "考试通知");
                message.put("sender", "考试系统");
                message.put("examId", exam.getId());
                message.put("examName", exam.getExamName());
                message.put("startTime", exam.getStartTime());
                message.put("endTime", exam.getEndTime());
                message.put("duration", exam.getExamDuration());
                String content = "新考试发布：“" + exam.getExamName() + "”，时长 " + exam.getExamDuration() + " 分钟。";
                message.put("content", content);
                message.put("sendTime", exam.getCreateTime());
                message.put("isRead", StringUtils.isNotEmpty(exam.getMessageRead()) ? exam.getMessageRead() : "0");
                messageList.add(message);
            }
        } catch (Exception e) {}

        messageList.sort((a, b) -> {
            Date dateA = convertToDate(a.get("sendTime"));
            Date dateB = convertToDate(b.get("sendTime"));
            if (dateA != null && dateB != null) return dateB.compareTo(dateA);
            return 0;
        });
        return messageList;
    }

    private Date convertToDate(Object timeObj) {
        if (timeObj == null) return null;
        if (timeObj instanceof Date) return (Date) timeObj;
        if (timeObj instanceof LocalDateTime) return Date.from(((LocalDateTime)timeObj).atZone(java.time.ZoneId.systemDefault()).toInstant());
        if (timeObj instanceof java.sql.Timestamp) return new Date(((java.sql.Timestamp) timeObj).getTime());
        return null;
    }

    private List<Map<String, Object>> getHomeworkMessages(Long userId) {
        List<Map<String, Object>> messages = new ArrayList<>();
        try {
            List<Map<String, Object>> homeworkList = homeworkMapper.selectHomeworkByUserId(userId);
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
            try {
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
            } catch (Exception ex) {}
        }
        return messages;
    }

    @Override
    public Map<String, Object> getMessageStats(Long userId) {
        List<Map<String, Object>> messageList = getMessageList(userId);
        int totalCount = 0;
        int unreadCount = 0;
        int todoCount = 0;
        int homeworkCount = 0;
        int examCount = 0;

        if (messageList != null) {
            totalCount = messageList.size();
            for (Map<String, Object> msg : messageList) {
                if (msg == null) continue;
                if ("0".equals(String.valueOf(msg.get("isRead")))) unreadCount++;
                String type = (String) msg.get("type");
                if ("todo".equals(type)) todoCount++;
                else if ("homework".equals(type)) homeworkCount++;
                else if ("exam".equals(type)) examCount++;
            }
        }

        // 兼容 Java 8 的 Map 写法
        List<Map<String, Object>> typeStats = new ArrayList<>();
        Map<String, Object> s1 = new HashMap<>(); s1.put("name", "待办事项"); s1.put("value", todoCount); typeStats.add(s1);
        Map<String, Object> s2 = new HashMap<>(); s2.put("name", "作业消息"); s2.put("value", homeworkCount); typeStats.add(s2);
        Map<String, Object> s3 = new HashMap<>(); s3.put("name", "考试通知"); s3.put("value", examCount); typeStats.add(s3);

        List<Map<String, Object>> readStats = new ArrayList<>();
        Map<String, Object> r1 = new HashMap<>(); r1.put("name", "未读"); r1.put("value", unreadCount); readStats.add(r1);
        Map<String, Object> r2 = new HashMap<>(); r2.put("name", "已读"); r2.put("value", totalCount - unreadCount); readStats.add(r2);

        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("unreadCount", unreadCount);
        result.put("typeStats", typeStats);
        result.put("readStats", readStats);
        return result;
    }

    @Override
    public int getUnreadCount(Long userId) {
        int count = 0;
        try { count += todoMapper.selectUnreadMessageCount(userId); } catch (Exception e) {}
        try { count += homeworkMapper.selectUnreadHomeworkMessageCount(); } catch (Exception e) {}
        try {
            boolean isAdmin = SecurityUtils.getLoginUser().getUser().isAdmin();
            count += classExamMapper.selectUnreadExamCount(userId, isAdmin);
        } catch (Exception e) {}
        return count;
    }

    @Override
    public int markTodoAsRead(Long todoId) {
        Todo todo = new Todo(); todo.setTodoId(todoId); todo.setMessageRead("1"); todo.setUpdateTime(new Date()); todo.setUpdateBy(SecurityUtils.getUsername());
        return todoMapper.updateTodoMessageStatus(todo);
    }

    @Override
    public int markHomeworkAsRead(Long homeworkId) {
        return homeworkMapper.updateHomeworkMessageStatus(homeworkId, "0", "1", SecurityUtils.getUsername(), new Date());
    }

    // ========== 【修复】标记考试为已读 ==========
    @Override
    public int markExamAsRead(Long examId) {
        Long userId = SecurityUtils.getUserId();
        try {
            // 1. 获取学生ID
            Long studentId = classExamMapper.selectStudentIdByUserId(userId);

            // 【关键修复】如果不是学生（例如管理员），直接返回成功(1)，不报错
            if (studentId == null) {
                return 1;
            }

            // 2. 检查是否已有记录
            Long participantId = classExamMapper.selectParticipantId(examId, studentId);

            if (participantId != null) {
                // 3A. 有记录 -> 更新
                return classExamMapper.updateExamReadStatus(participantId);
            } else {
                // 3B. 无记录 -> 插入一条“已读但未开始”的记录
                Map<String, Object> student = classExamMapper.selectStudentById(studentId);
                if (student != null) {
                    String studentNo = (String) student.get("student_no");
                    String studentName = (String) student.get("student_name");
                    return classExamMapper.insertReadRecord(examId, studentId, studentNo, studentName);
                }
            }
        } catch (Exception e) {
            log.error("标记考试已读失败", e);
            return 0; // 异常返回失败
        }
        return 0;
    }

    @Override
    public int markAllAsRead(Long userId) {
        int successCount = 0;
        try {
            List<Todo> todos = todoMapper.selectTodoMessages(userId);
            for(Todo t : todos) if("0".equals(t.getMessageRead())) { markTodoAsRead(t.getTodoId()); successCount++; }

            List<Map<String, Object>> hws = getHomeworkMessages(userId);
            for(Map<String, Object> h : hws) {
                if("0".equals(String.valueOf(h.get("isRead")))) {
                    Object hwId = h.get("homeworkId");
                    if (hwId != null) { markHomeworkAsRead(Long.parseLong(hwId.toString())); successCount++; }
                }
            }

            boolean isAdmin = SecurityUtils.getLoginUser().getUser().isAdmin();
            if (!isAdmin) {
                List<ClassExam> exams = classExamMapper.selectExamMessages(userId, false);
                for (ClassExam e : exams) {
                    if ("0".equals(e.getMessageRead())) {
                        markExamAsRead(e.getId());
                        successCount++;
                    }
                }
            }
        } catch (Exception e) {}
        return successCount;
    }

    @Override
    public int deleteMessage(String messageId) {
        if (messageId.startsWith("todo_")) {
            return deleteTodoMessage(Long.parseLong(messageId.replace("todo_", "")));
        } else if (messageId.startsWith("homework_")) {
            return deleteHomeworkMessage(Long.parseLong(messageId.replace("homework_", "")));
        } else if (messageId.startsWith("exam_")) {
            return 1;
        }
        return 0;
    }

    private int deleteTodoMessage(Long todoId) {
        Todo todo = new Todo(); todo.setTodoId(todoId); todo.setMessageStatus("1"); todo.setMessageRead("0"); todo.setUpdateTime(new Date()); todo.setUpdateBy(SecurityUtils.getUsername());
        return todoMapper.updateTodoMessageStatus(todo);
    }
    private int deleteHomeworkMessage(Long homeworkId) {
        return homeworkMapper.updateHomeworkMessageStatus(homeworkId, "1", "1", SecurityUtils.getUsername(), new Date());
    }
}