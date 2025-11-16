package com.ruoyi.proj_cyq.service;

import java.util.List;
import java.util.Map;

public interface IMessageService {
    /**
     * 获取用户的所有消息
     */
    List<Map<String, Object>> getMessageList(Long userId);

    /**
     * 获取未读消息数量
     */
    int getUnreadCount(Long userId);

    /**
     * 标记待办消息为已读
     */
    int markTodoAsRead(Long todoId);

    /**
     * 标记作业消息为已读
     */
    int markHomeworkAsRead(Long homeworkId);

    /**
     * 删除消息
     */
    int deleteMessage(String messageId);

    /**
     * 批量标记消息为已读
     */
    int markAllAsRead(Long userId);

    /**
     * 【新增】获取消息统计
     * (参考 ITodoService)
     */
    public Map<String, Object> getMessageStats(Long userId);
}