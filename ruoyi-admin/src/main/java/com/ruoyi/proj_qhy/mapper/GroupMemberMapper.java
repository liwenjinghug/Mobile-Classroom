package com.ruoyi.proj_qhy.mapper;

import com.ruoyi.proj_qhy.domain.GroupMember;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface GroupMemberMapper {

    @Insert("INSERT INTO class_group_member(group_id, user_id, status, last_read_message_id, create_by, create_time) " +
            "VALUES(#{groupId}, #{userId}, '0', #{lastReadMessageId}, #{createBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "memberId")
    int insertMember(GroupMember member);

    /**
     * 查询小组成员列表 (带用户信息)
     */
    @Select("SELECT m.*, u.nick_name AS nickName, u.avatar " +
            "FROM class_group_member m " +
            "JOIN sys_user u ON m.user_id = u.user_id " +
            "WHERE m.group_id = #{groupId} AND m.status = '0' AND m.del_flag = '0'")
    List<GroupMember> selectMembersByGroupId(Long groupId);

    /**
     * (修正) 移除成员 (更新 status 和 update_time)
     */
    @Update("UPDATE class_group_member SET status = '2', update_by = #{updateBy}, update_time = NOW() " +
            "WHERE group_id = #{groupId} AND user_id = #{userId}")
    int removeMember(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("updateBy") String updateBy);

    /**
     * 检查用户是否在组内 (且状态正常)
     */
    @Select("SELECT COUNT(*) FROM class_group_member " +
            "WHERE group_id = #{groupId} AND user_id = #{userId} AND status = '0' AND del_flag = '0'")
    int checkUserInGroup(@Param("groupId") Long groupId, @Param("userId") Long userId);

    /**
     * 更新用户的最后已读消息ID
     */
    @Update("UPDATE class_group_member SET last_read_message_id = #{messageId} " +
            "WHERE group_id = #{groupId} AND user_id = #{userId}")
    int updateLastReadMessageId(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("messageId") Long messageId);

    /**
     * 查询单个成员详情 (带用户信息)
     */
    @Select("SELECT m.*, u.nick_name AS nickName, u.avatar " +
            "FROM class_group_member m " +
            "JOIN sys_user u ON m.user_id = u.user_id " +
            "WHERE m.group_id = #{groupId} AND m.user_id = #{userId} AND m.del_flag = '0'")
    GroupMember selectMemberDetails(@Param("groupId") Long groupId, @Param("userId") Long userId);

    /**
     *查询小组的活跃成员数量
     */
    @Select("SELECT COUNT(*) FROM class_group_member " +
            "WHERE group_id = #{groupId} AND status = '0' AND del_flag = '0'")
    int countActiveMembers(@Param("groupId") Long groupId);

    /**
     * 查找下一任组长 (按加入时间排序，即 member_id 最小)
     * 返回包含用户昵称的成员对象
     */
    @Select("SELECT m.*, u.nick_name as nickName FROM class_group_member m " +
            "JOIN sys_user u ON m.user_id = u.user_id " +
            "WHERE m.group_id = #{groupId} AND m.user_id != #{oldOwnerId} AND m.status = '0' AND m.del_flag = '0' " +
            "ORDER BY m.member_id ASC LIMIT 1")
    GroupMember findNextOwner(@Param("groupId") Long groupId, @Param("oldOwnerId") Long oldOwnerId);

    /**
     * (新增) 查询成员 (无论状态如何)
     * (这就是解决您问题的关键方法!)
     */
    @Select("SELECT * FROM class_group_member " +
            "WHERE group_id = #{groupId} AND user_id = #{userId} AND del_flag = '0'")
    GroupMember selectMemberAnyStatus(@Param("groupId") Long groupId, @Param("userId") Long userId);

    /**
     * (新增/修正) 重新激活已退出的成员
     */
    @Update("UPDATE class_group_member SET status = '0', last_read_message_id = #{lastReadMessageId} " +
            "WHERE group_id = #{groupId} AND user_id = #{userId}")
    int reviveMember(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("lastReadMessageId") Long lastReadMessageId);
}