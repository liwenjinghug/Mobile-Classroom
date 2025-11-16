package com.ruoyi.proj_cyq.domain;

import com.ruoyi.common.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * 消息中心导出实体
 * @author ruoyi
 */
public class MessageExport {

    // @Excel 注解的 readConverterExp 用于转换 0/1 为中文
    @Excel(name = "消息类型", readConverterExp = "todo=待办事项,homework=作业消息")
    private String type;

    @Excel(name = "消息标题")
    private String title;

    @Excel(name = "消息内容")
    private String content;

    @Excel(name = "发送方")
    private String sender;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "发送时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date sendTime;

    @Excel(name = "读取状态", readConverterExp = "0=未读,1=已读")
    private String isRead;

    // --- Getter 和 Setter ---

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public String getIsRead() {
        return isRead;
    }

    public void setIsRead(String isRead) {
        this.isRead = isRead;
    }
}