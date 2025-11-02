package com.ruoyi.web.controller.proj_cyq;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.proj_cyq.service.IMessageService;

@RestController
@RequestMapping("/proj_cyq/message")
public class MessageController extends BaseController {
    @Autowired
    private IMessageService messageService;

    @GetMapping("/list")
    public AjaxResult list() {
        Long userId = getUserId();
        List<Map<String, Object>> messageList = messageService.getMessageList(userId);
        return success(messageList);
    }

    @GetMapping("/unreadCount")
    public AjaxResult getUnreadCount() {
        Long userId = getUserId();
        return success(messageService.getUnreadCount(userId));
    }

    @Log(title = "标记待办消息已读", businessType = BusinessType.UPDATE)
    @PutMapping("/read/todo/{todoId}")
    public AjaxResult markTodoAsRead(@PathVariable Long todoId) {
        return toAjax(messageService.markTodoAsRead(todoId));
    }

    @Log(title = "标记作业消息已读", businessType = BusinessType.UPDATE)
    @PutMapping("/read/homework/{homeworkId}")
    public AjaxResult markHomeworkAsRead(@PathVariable Long homeworkId) {
        return toAjax(messageService.markHomeworkAsRead(homeworkId));
    }

    @Log(title = "批量标记消息已读", businessType = BusinessType.UPDATE)
    @PutMapping("/read/all")
    public AjaxResult markAllAsRead() {
        Long userId = getUserId();
        return toAjax(messageService.markAllAsRead(userId));
    }

    @Log(title = "删除消息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{messageId}")
    public AjaxResult deleteMessage(@PathVariable String messageId) {
        return toAjax(messageService.deleteMessage(messageId));
    }
}