package com.ruoyi.proj_qhy.mapper;

import com.ruoyi.proj_qhy.domain.Group;
import com.ruoyi.proj_qhy.domain.GroupMessage;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface GroupMapper {

    /**
     * 查询我的小组列表 (主界面)
     * 1. 查出我所在的小组
     * 2. 关联查询最新一条消息
     * 3. 关联查询最新消息的发送者
     * 4. 关联查询我的未读数量
     */
    /**
     * (修正) 查询我的小组列表 (主界面)
     * - 解决了退出后 "幽灵未读" 和 "消息预览" 的问题
     */
    @Select("<script>" +
            "SELECT g.*, " +
            "m.status AS memberStatus, " +
            "m.last_read_message_id AS lastReadMessageId, " +

            // 1. 修正未读计数: 如果 status='2' (已退出), 未读数强制为 0
            "CASE " +
            "  WHEN m.status = '2' THEN 0 " +
            "  ELSE (SELECT COUNT(*) FROM class_group_message unread_msg " +
            "        WHERE unread_msg.group_id = g.group_id AND unread_msg.message_id > m.last_read_message_id) " +
            "END AS unreadCount, " +

            "msg.content AS `latestMessage.content`, " +
            "msg.create_time AS `latestMessage.createTime`, " +
            "msg.message_type AS `latestMessage.messageType`, " +
            "sender.nick_name AS `latestMessage.senderNickName` " +

            "FROM class_group g " +
            "JOIN class_group_member m ON g.group_id = m.group_id " +

            // 2. 修正最新消息JOIN:
            "LEFT JOIN class_group_message msg ON msg.message_id = ( " +
            "    CASE " +
            //   如果已退出, 动态查找 "我退出那一刻" 的最后一条消息
            "      WHEN m.status = '2' THEN " +
            "        (SELECT m_exit.message_id FROM class_group_message m_exit " +
            "         WHERE m_exit.group_id = g.group_id AND m_exit.create_time &lt;= m.update_time " + // &lt;= 是 <=
            "         ORDER BY m_exit.create_time DESC LIMIT 1) " +
            //   如果还活跃, 正常使用小组的最新消息
            "      ELSE g.latest_message_id " +
            "    END " +
            ") " +

            "LEFT JOIN sys_user sender ON msg.sender_user_id = sender.user_id " +
            "WHERE m.user_id = #{userId} AND g.del_flag = '0' AND m.del_flag = '0' " +
            "ORDER BY msg.create_time DESC" +
            "</script>")
    List<Group> selectGroupListByUserId(Long userId);

    /**
     * 查询小组详情 (用于聊天窗口)
     */
    @Select("SELECT g.*, owner.nick_name AS ownerNickName " +
            "FROM class_group g " +
            "LEFT JOIN sys_user owner ON g.owner_user_id = owner.user_id " +
            "WHERE g.group_id = #{groupId} AND g.del_flag = '0'")
    Group selectGroupById(Long groupId);

    @Insert("INSERT INTO class_group(group_name, owner_user_id, group_number, avatar, qr_code, create_by, create_time) " +
            "VALUES(#{groupName}, #{ownerUserId}, #{groupNumber}, #{avatar}, #{qrCode}, #{createBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "groupId")
    int insertGroup(Group group);

    @Update("UPDATE class_group SET group_name = #{groupName}, update_by = #{updateBy}, update_time = NOW() " +
            "WHERE group_id = #{groupId}")
    int updateGroupName(@Param("groupId") Long groupId, @Param("groupName") String groupName, @Param("updateBy") String updateBy);

    @Update("UPDATE class_group SET avatar = #{avatar}, update_by = #{updateBy}, update_time = NOW() " +
            "WHERE group_id = #{groupId}")
    int updateGroupAvatar(@Param("groupId") Long groupId, @Param("avatar") String avatar, @Param("updateBy") String updateBy);

    @Update("UPDATE class_group SET latest_message_id = #{messageId}, update_time = NOW() " +
            "WHERE group_id = #{groupId}")
    int updateLatestMessageId(@Param("groupId") Long groupId, @Param("messageId") Long messageId);

    @Select("SELECT COUNT(*) FROM class_group WHERE group_number = #{groupNumber} AND del_flag = '0'")
    int checkGroupNumberUnique(String groupNumber);

    /**
     * 搜索小组 (按小组号)
     * (修改点: 移除 'NOT IN' 过滤, 添加 'EXISTS' 作为 isMember 字段)
     */
    @Select("SELECT g.*, " +
            "EXISTS(SELECT 1 FROM class_group_member m WHERE m.group_id = g.group_id AND m.user_id = #{userId} AND m.status = '0') AS isMember " +
            "FROM class_group g " +
            "WHERE g.group_number = #{groupNumber} AND g.del_flag = '0'")
    List<Group> searchGroupByNumber(@Param("groupNumber") String groupNumber, @Param("userId") Long userId);

    /**
     * 搜索小组 (按名称)
     * (修改点: 移除 'NOT IN' 过滤, 添加 'EXISTS' 作为 isMember 字段)
     */
    @Select("SELECT g.*, " +
            "EXISTS(SELECT 1 FROM class_group_member m WHERE m.group_id = g.group_id AND m.user_id = #{userId} AND m.status = '0') AS isMember " +
            "FROM class_group g " +
            "WHERE g.group_name LIKE CONCAT('%', #{groupName}, '%') AND g.del_flag = '0'")
    List<Group> searchGroupByName(@Param("groupName") String groupName, @Param("userId") Long userId);
    /**
     *解散小组 (逻辑删除)
     */
    @Update("UPDATE class_group SET del_flag = '2', update_by = #{updateBy}, update_time = NOW() " +
            "WHERE group_id = #{groupId}")
    int disbandGroup(@Param("groupId") Long groupId, @Param("updateBy") String updateBy);

    /**
     * 转移组长
     */
    @Update("UPDATE class_group SET owner_user_id = #{newOwnerUserId}, update_by = #{updateBy}, update_time = NOW() " +
            "WHERE group_id = #{groupId}")
    int updateGroupOwner(@Param("groupId") Long groupId, @Param("newOwnerUserId") Long newOwnerUserId, @Param("updateBy") String updateBy);
}