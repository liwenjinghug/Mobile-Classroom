package com.ruoyi.proj_qhy.service;

import com.ruoyi.proj_qhy.domain.Group;
import com.ruoyi.proj_qhy.domain.GroupMessage;
import com.ruoyi.proj_qhy.domain.GroupMember;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface IGroupService {

    /**
     * 获取我的小组列表 (主界面)
     */
    List<Group> selectGroupList(Long userId);

    /**
     * 创建小组
     */
    Group createGroup(Group group, List<Long> memberUserIds);

    /**
     * 获取小组详情 (聊天窗口用)
     */
    Group getGroupDetails(Long groupId, Long userId);

    /**
     * 修改小组信息 (名称)
     */
    int updateGroupInfo(Group group);

    /**
     * (新增) 更新小组头像
     */
    String updateGroupAvatar(Long groupId, MultipartFile file);

    /**
     * 获取聊天记录
     */
    List<GroupMessage> getChatMessages(Long groupId, Long userId);

    /**
     * 发送消息
     */
    GroupMessage sendMessage(GroupMessage message, MultipartFile image);

    /**
     * 移除成员
     */
    void removeMember(Long groupId, Long memberUserId);

    /**
     * 搜索小组
     */
    List<Group> searchGroups(String query, String type, Long userId);

    /**
     * 加入小组
     */
    GroupMember joinGroup(Long groupId, Long userId);

    /**
     * 解散小组
     */
    void disbandGroup(Long groupId);

    /**
     * 退出小组
     */
    void exitGroup(Long groupId);

    /**
     * 撤回消息
     */
    void recallMessage(Long messageId);

    /**
     * (新增) 分享文章到小组
     */
    void shareArticle(Long articleId, List<Long> groupIds);
}