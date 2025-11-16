package com.ruoyi.web.controller.proj_cyq;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Date;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// 【修复】导入你自定义的 @Log 注解
import com.ruoyi.proj_cyq.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.proj_cyq.service.IMessageService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.proj_cyq.domain.MessageExport;

@RestController
@RequestMapping("/proj_cyq/message")
public class MessageController extends BaseController {
    @Autowired
    private IMessageService messageService;

    // (查询列表和统计通常不记录操作日志，保持不变)
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

    @GetMapping("/stats")
    public AjaxResult getStats() {
        return success(messageService.getMessageStats(getUserId()));
    }

    // 此注解现在会正确指向 ClassLogAspect
    @Log(title = "消息中心", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response)
    {
        Long userId = getUserId();
        List<Map<String, Object>> list = messageService.getMessageList(userId);

        List<MessageExport> exportList = new ArrayList<>();
        for (Map<String, Object> map : list) {
            MessageExport item = new MessageExport();
            item.setType((String) map.get("type"));
            item.setTitle((String) map.get("title"));
            item.setContent((String) map.get("content"));
            item.setSender((String) map.get("sender"));
            item.setIsRead(String.valueOf(map.get("isRead")));
            Object sendTimeObj = map.get("sendTime");
            if (sendTimeObj instanceof Date) {
                item.setSendTime((Date) sendTimeObj);
            }
            exportList.add(item);
        }

        ExcelUtil<MessageExport> util = new ExcelUtil<MessageExport>(MessageExport.class);
        util.exportExcel(response, exportList, "消息中心数据");
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