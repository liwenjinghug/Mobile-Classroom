package com.ruoyi.proj_qhy.mapper;

import com.ruoyi.proj_qhy.domain.GroupMessage;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Date;

@Mapper
public interface GroupMessageMapper {

    /**
     * (修正) 查询聊天记录 (倒序分页)
     * (新增) 如果传入 exitTime，则只查询该时间之前的消息
     */
    @Select("<script>" +
            "SELECT msg.*, u.nick_name AS senderNickName, u.avatar AS senderAvatar " +
            "FROM class_group_message msg " +
            "LEFT JOIN sys_user u ON msg.sender_user_id = u.user_id " +
            "WHERE msg.group_id = #{groupId} AND msg.del_flag = '0' " +
            // 如果 exitTime 不为 null，则只获取该时间点之前的消息
            "<if test='exitTime != null'>" +
            "  AND msg.create_time &lt;= #{exitTime} " + // &lt;= 是 <= (小于等于)
            "</if>" +
            "ORDER BY msg.create_time DESC " +
            "LIMIT 50" + // 仍然只取50条
            "</script>")
    List<GroupMessage> selectMessagesByGroupId(@Param("groupId") Long groupId, @Param("exitTime") Date exitTime);

    @Insert("INSERT INTO class_group_message(group_id, sender_user_id, message_type, content, create_by, create_time) " +
            "VALUES(#{groupId}, #{senderUserId}, #{messageType}, #{content}, #{createBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "messageId")
    int insertMessage(GroupMessage message);

    /**
     * 根据ID查询单条消息
     */
    @Select("SELECT * FROM class_group_message WHERE message_id = #{messageId} AND del_flag = '0'")
    GroupMessage selectMessageById(@Param("messageId") Long messageId);

    /**
     * 撤回消息
     * (将消息类型改为9-系统, 并更新内容)
     */
    @Update("UPDATE class_group_message SET content = #{recallContent}, message_type = '9' " +
            "WHERE message_id = #{messageId}")
    int recallMessage(@Param("messageId") Long messageId, @Param("recallContent") String recallContent);
}