package com.ruoyi.proj_qhy.service.impl;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.proj_qhy.domain.Group;
import com.ruoyi.proj_qhy.domain.GroupMember;
import com.ruoyi.proj_qhy.domain.GroupMessage;
import com.ruoyi.proj_qhy.mapper.GroupMapper;
import com.ruoyi.proj_qhy.mapper.GroupMemberMapper;
import com.ruoyi.proj_qhy.mapper.GroupMessageMapper;
import com.ruoyi.proj_qhy.service.IGroupService;
import com.ruoyi.proj_qhy.domain.BbsArticle;
import com.ruoyi.proj_qhy.mapper.BbsArticleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 小组讨论服务实现
 */
@Service
public class GroupServiceImpl implements IGroupService {

    private static final Logger logger = LoggerFactory.getLogger(GroupServiceImpl.class);

    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private GroupMemberMapper groupMemberMapper;
    @Autowired
    private GroupMessageMapper groupMessageMapper;
    @Autowired
    private BbsArticleMapper bbsArticleMapper;

    /**
     * 获取我的小组列表 (主界面)
     * (包含已退出 '2' 和 正常 '0' 的小组)
     */
    @Override
    public List<Group> selectGroupList(Long userId) {
        // Mapper中的SQL已经处理了最新消息、发送人、未读数、成员状态
        return groupMapper.selectGroupListByUserId(userId);
    }

    /**
     * 创建小组
     * (系统消息使用昵称)
     */
    @Override
    @Transactional
    public Group createGroup(Group group, List<Long> memberUserIds) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long userId = loginUser.getUserId();
        String username = loginUser.getUsername();
        String nickName = loginUser.getUser().getNickName();

        // 1. 设置小组基本信息
        group.setOwnerUserId(userId);
        group.setCreateBy(username);
        // 生成唯一小组号
        String groupNumber = generateGroupNumber();
        while(groupMapper.checkGroupNumberUnique(groupNumber) > 0) {
            groupNumber = generateGroupNumber();
        }
        group.setGroupNumber(groupNumber);

        // 设置默认头像
        if (StringUtils.isEmpty(group.getAvatar())) {
            group.setAvatar("/profile/avatar/group_default.png");
        }

        // 2. 插入小组
        groupMapper.insertGroup(group);

        // 3. 插入成员 (必须包含创建者)
        if (!memberUserIds.contains(userId)) {
            memberUserIds.add(userId);
        }

        for (Long memberUserId : memberUserIds) {
            GroupMember member = new GroupMember();
            member.setGroupId(group.getGroupId());
            member.setUserId(memberUserId);
            member.setCreateBy(username);
            member.setLastReadMessageId(0L); // 初始已读为0
            groupMemberMapper.insertMember(member);
        }

        // 4. 发送一条系统消息 (使用昵称)
        GroupMessage systemMessage = new GroupMessage();
        systemMessage.setGroupId(group.getGroupId());
        systemMessage.setSenderUserId(userId);
        systemMessage.setMessageType("9"); // 9=系统
        systemMessage.setContent(nickName + " 创建了小组");
        systemMessage.setCreateBy(username);
        sendMessageInternal(systemMessage); // 内部发送

        return group;
    }

    /**
     * 获取小组详情 (聊天窗口用)
     * (包含当前用户的状态 '0' 或 '2')
     */
    @Override
    public Group getGroupDetails(Long groupId, Long userId) {
        // 1. 获取小组基本信息
        Group group = groupMapper.selectGroupById(groupId);
        if (group == null) {
            throw new ServiceException("小组不存在");
        }

        // 2. 获取 "我" 的成员详情 (无论状态如何)
        GroupMember myMember = groupMemberMapper.selectMemberAnyStatus(groupId, userId);
        if (myMember == null) {
            throw new ServiceException("您不是该小组成员");
        }

        // 3. 将 "我" 的状态 ('0' 或 '2') 附加到 group DTO
        group.setCurrentUserStatus(myMember.getStatus());

        // 4. 获取成员列表 (只获取 active 的 '0' 成员，用于弹窗)
        group.setMembers(groupMemberMapper.selectMembersByGroupId(groupId));
        return group;
    }

    /**
     * 修改小组信息 (仅名称)
     * (检查活跃状态)
     */
    @Override
    public int updateGroupInfo(Group group) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        // 检查活跃状态
        checkActiveAuth(group.getGroupId(), loginUser.getUserId());

        Group dbGroup = groupMapper.selectGroupById(group.getGroupId());

        // 只有创建者能修改
        if (!dbGroup.getOwnerUserId().equals(loginUser.getUserId())) {
            throw new ServiceException("无权修改");
        }

        return groupMapper.updateGroupName(group.getGroupId(), group.getGroupName(), loginUser.getUsername());
    }

    /**
     * (修正) 获取聊天记录
     * (如果已退出，只显示退出时间之前的消息)
     */
    @Override
    public List<GroupMessage> getChatMessages(Long groupId, Long userId) {
        // 1. 获取 "我" 的成员详情 (无论状态如何)
        GroupMember myMember = groupMemberMapper.selectMemberAnyStatus(groupId, userId);
        if (myMember == null) {
            throw new ServiceException("您不是该小组成员");
        }

        // 2. (新增) 检查是否已退出。如果已退出，获取退出时间
        Date exitTime = null;
        if ("2".equals(myMember.getStatus())) {
            exitTime = myMember.getUpdateTime(); // <-- 获取退出时间
        }

        // 3. (修改) 将退出时间传递给 Mapper
        List<GroupMessage> messages = groupMessageMapper.selectMessagesByGroupId(groupId, exitTime);

        // 4. (修正) 只有活跃成员 '0' 才更新已读
        // (您上次的问题“幽灵未读”就是因为这里没判断，现在必须判断)
        if ("0".equals(myMember.getStatus()) && !messages.isEmpty()) {
            Long latestMessageId = messages.get(0).getMessageId();
            groupMemberMapper.updateLastReadMessageId(groupId, userId, latestMessageId);
        }

        // 5. 返回时反转
        Collections.reverse(messages);
        return messages;
    }

    /**
     * 发送消息
     * (检查活跃状态)
     */
    @Override
    @Transactional
    public GroupMessage sendMessage(GroupMessage message, MultipartFile image) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long userId = loginUser.getUserId();

        // 1. 检查是否为活跃成员
        checkActiveAuth(message.getGroupId(), userId);

        message.setSenderUserId(userId);
        message.setCreateBy(loginUser.getUsername());

        // 2. 处理图片
        if (image != null && !image.isEmpty()) {
            String imageUrl = uploadImage(image);
            message.setContent(imageUrl);
            message.setMessageType("1"); // 1=图片
        } else {
            message.setMessageType("0"); // 0=文本
        }

        // 3. 内部发送
        return sendMessageInternal(message);
    }

    /**
     * (修正) 移除成员
     */
    @Override
    @Transactional
    public void removeMember(Long groupId, Long memberUserId) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        checkActiveAuth(groupId, loginUser.getUserId());
        Group dbGroup = groupMapper.selectGroupById(groupId);

        // ... (组长鉴权逻辑)
        if (!dbGroup.getOwnerUserId().equals(loginUser.getUserId())) {
            throw new ServiceException("只有组长才能移除成员");
        }
        if (dbGroup.getOwnerUserId().equals(memberUserId)) {
            throw new ServiceException("组长不能移除自己");
        }

        GroupMember memberToRemove = groupMemberMapper.selectMemberDetails(groupId, memberUserId);
        String memberName = (memberToRemove != null) ? memberToRemove.getNickName() : "成员(ID:" + memberUserId + ")";

        // 4. (修改) 移除成员 (传入 username 以便记录 update_by)
        groupMemberMapper.removeMember(groupId, memberUserId, loginUser.getUsername());

        // 5. 发送系统通知
        GroupMessage systemMessage = new GroupMessage();
        systemMessage.setGroupId(groupId);
        systemMessage.setSenderUserId(loginUser.getUserId());
        systemMessage.setMessageType("9");
        systemMessage.setContent("成员 \"" + memberName + "\" 已被组长移除");
        systemMessage.setCreateBy(loginUser.getUsername());
        sendMessageInternal(systemMessage);
    }

    /**
     * 解散小组
     */
    @Override
    @Transactional
    public void disbandGroup(Long groupId) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Group dbGroup = groupMapper.selectGroupById(groupId);

        // 1. 鉴权: 必须是组长
        if (!dbGroup.getOwnerUserId().equals(loginUser.getUserId())) {
            throw new ServiceException("只有组长才能解散小组");
        }

        // 2. 发送系统通知
        GroupMessage systemMessage = new GroupMessage();
        systemMessage.setGroupId(groupId);
        systemMessage.setSenderUserId(loginUser.getUserId());
        systemMessage.setMessageType("9"); // 9=系统
        systemMessage.setContent("组长已解散小组");
        systemMessage.setCreateBy(loginUser.getUsername());
        sendMessageInternal(systemMessage);

        // 3. 逻辑删除小组 (del_flag = '2')
        groupMapper.disbandGroup(groupId, loginUser.getUsername());

        // (成员和消息的查询都关联了 group.del_flag = '0', 所以解散后自动隐藏)
        // (注意: 已退出的成员列表依然会显示这个群，但点击会报错)
    }

    /**
     * (修正) 退出小组
     */
    @Override
    @Transactional
    public void exitGroup(Long groupId) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long userId = loginUser.getUserId();
        String nickName = loginUser.getUser().getNickName();
        String username = loginUser.getUsername();

        Group dbGroup = groupMapper.selectGroupById(groupId);
        if (dbGroup == null) throw new ServiceException("小组不存在");

        boolean isOwner = dbGroup.getOwnerUserId().equals(userId);

        // 1. (修改) 移除成员 (传入 username 以便记录 update_by)
        groupMemberMapper.removeMember(groupId, userId, username);

        // 2. 发送退出通知
        GroupMessage exitMessage = new GroupMessage();
        exitMessage.setGroupId(groupId);
        exitMessage.setSenderUserId(userId);
        exitMessage.setMessageType("9");
        exitMessage.setContent("成员 \"" + nickName + "\" 退出了小组");
        exitMessage.setCreateBy(username);
        sendMessageInternal(exitMessage);

        // ... (转让/解散逻辑保持不变)
        if (isOwner) {
            int activeMembers = groupMemberMapper.countActiveMembers(groupId);
            if (activeMembers == 0) {
                groupMapper.disbandGroup(groupId, username);
            } else {
                GroupMember newOwner = groupMemberMapper.findNextOwner(groupId, userId);
                if (newOwner != null) {
                    groupMapper.updateGroupOwner(groupId, newOwner.getUserId(), username);
                    GroupMessage transferMessage = new GroupMessage();
                    transferMessage.setGroupId(groupId);
                    transferMessage.setSenderUserId(userId);
                    transferMessage.setMessageType("9");
                    transferMessage.setContent("\"" + newOwner.getNickName() + "\" 成为新的组长");
                    transferMessage.setCreateBy(username);
                    sendMessageInternal(transferMessage);
                } else {
                    groupMapper.disbandGroup(groupId, username);
                }
            }
        }
    }

    /**
     * 撤回消息
     * (2分钟内)
     */
    @Override
    @Transactional
    public void recallMessage(Long messageId) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long userId = loginUser.getUserId();
        String nickName = loginUser.getUser().getNickName();

        GroupMessage message = groupMessageMapper.selectMessageById(messageId);
        if (message == null) throw new ServiceException("消息不存在");

        // 1. 鉴权: 必须是本人
        if (!message.getSenderUserId().equals(userId)) {
            throw new ServiceException("只能撤回自己的消息");
        }

        // 2. 鉴权: 检查时间 (2分钟内)
        long twoMinutesAgo = System.currentTimeMillis() - (2 * 60 * 1000);
        if (message.getCreateTime().getTime() < twoMinutesAgo) {
            throw new ServiceException("已超过2分钟，无法撤回");
        }

        // 3. 执行撤回 (更新消息为系统消息)
        String recallContent = "\"" + nickName + "\" 撤回了一条消息";
        // (调用修正后的 Mapper，不带 update_by)
        groupMessageMapper.recallMessage(messageId, recallContent);
    }

    /**
     * 搜索小组
     */
    @Override
    public List<Group> searchGroups(String query, String type, Long userId) {
        // Mapper SQL 已更新，会返回 isMember (且 status='0' 才算)
        if ("number".equals(type)) {
            return groupMapper.searchGroupByNumber(query, userId);
        } else { // name
            return groupMapper.searchGroupByName(query, userId);
        }
    }

    /**
     * 加入小组
     * (支持已退出 '2' 成员重新加入)
     */
    @Override
    @Transactional
    public GroupMember joinGroup(Long groupId, Long userId) {
        Group group = groupMapper.selectGroupById(groupId);
        if (group == null) {
            throw new ServiceException("小组不存在");
        }

        LoginUser loginUser = SecurityUtils.getLoginUser();
        String username = loginUser.getUsername();
        String nickName = loginUser.getUser().getNickName();

        // 检查成员 (无论状态)
        GroupMember member = groupMemberMapper.selectMemberAnyStatus(groupId, userId);

        // 获取最新消息ID，用于设置已读
        Long lastReadId = (group.getLatestMessageId() != null) ? group.getLatestMessageId() : 0L;

        if (member != null) {
            if ("0".equals(member.getStatus())) {
                // ...
            } else {
                // 2. 状态为 '2' (已退出)，重新激活
                groupMemberMapper.reviveMember(groupId, userId, lastReadId); // <-- 修正行
            }
        } else {
            // 3. 新成员，插入
            member = new GroupMember();
            member.setGroupId(groupId);
            member.setUserId(userId);
            member.setCreateBy(username);
            member.setLastReadMessageId(lastReadId);
            groupMemberMapper.insertMember(member);
        }

        // 4. 发送系统消息
        GroupMessage systemMessage = new GroupMessage();
        systemMessage.setGroupId(groupId);
        systemMessage.setSenderUserId(userId);
        systemMessage.setMessageType("9");
        systemMessage.setContent("\"" + nickName + "\" 加入了小组");
        systemMessage.setCreateBy(username);
        sendMessageInternal(systemMessage);

        return member;
    }

    /**
     * 更新小组头像
     */
    @Override
    @Transactional
    public String updateGroupAvatar(Long groupId, MultipartFile file) {
        LoginUser loginUser = SecurityUtils.getLoginUser();

        // 检查活跃状态
        checkActiveAuth(groupId, loginUser.getUserId());

        Group dbGroup = groupMapper.selectGroupById(groupId);

        // 1. 鉴权: 必须是组长
        if (!dbGroup.getOwnerUserId().equals(loginUser.getUserId())) {
            throw new ServiceException("只有组长才能修改头像");
        }

        // 2. 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }

        // 3. 上传图片
        String avatarUrl = uploadImage(file);

        // 4. 更新数据库
        groupMapper.updateGroupAvatar(groupId, avatarUrl, loginUser.getUsername());

        // 5. 返回新的URL，以便前端实时更新
        return avatarUrl;
    }


    // --- 内部辅助方法 ---

    /**
     * 内部发送消息, 并更新小组最新消息ID
     */
    private GroupMessage sendMessageInternal(GroupMessage message) {
        // 1. 插入消息
        groupMessageMapper.insertMessage(message);
        // 2. 更新小组的最新消息ID
        groupMapper.updateLatestMessageId(message.getGroupId(), message.getMessageId());
        return message;
    }

    /**
     * (新增) 检查用户是否为活跃成员 (status='0')
     */
    private void checkActiveAuth(Long groupId, Long userId) {
        if (groupMemberMapper.checkUserInGroup(groupId, userId) == 0) {
            throw new ServiceException("您已不是小组成员，无法操作");
        }
    }

    /**
     * 生成唯一小组号 (6位随机数)
     */
    private String generateGroupNumber() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }

    /**
     * 统一图片上传逻辑 (与论坛一致)
     */
    private String uploadImage(MultipartFile file) {
        try {
            String configuredProfile = RuoYiConfig.getProfile();
            String savePath = RuoYiConfig.getProfile();
            if (StringUtils.isEmpty(savePath)) {
                // (备用方案，如果配置为空)
                savePath = new File("").getAbsolutePath() + "/uploads/group";
            }

            File destDir = new File(savePath);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String newFileName = generateFileName(originalFilename);
            File destFile = new File(savePath + File.separator + newFileName);
            file.transferTo(destFile);

            return Constants.RESOURCE_PREFIX + "/" + newFileName;
        } catch (Exception e) {
            logger.error("图片上传失败:", e);
            throw new ServiceException("图片上传失败");
        }
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String originalFilename) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return "group_" + timeStamp + "_" + uuid + fileExtension;
    }
    /**
     * (新增) 分享文章到小组
     */
    @Override
    @Transactional
    public void shareArticle(Long articleId, List<Long> groupIds) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long userId = loginUser.getUserId();
        String username = loginUser.getUsername();
        String nickName = loginUser.getUser().getNickName();

        // 1. 获取文章信息
        BbsArticle article = bbsArticleMapper.selectBbsArticleById(articleId);
        if (article == null) {
            throw new ServiceException("文章不存在");
        }

        // 2. 构建 JSON 内容 (如我们之前讨论的)
        String content = String.format("{\"id\":%d, \"title\":\"%s\", \"digest\":\"%s\", \"cover\":\"%s\"}",
                article.getId(),
                escapeJson(article.getTitle()),
                escapeJson(article.getDigest()),
                escapeJson(article.getCover())
        );

        // 3. 循环发送到各个小组
        for (Long groupId : groupIds) {
            // 3.1 鉴权 (必须是活跃成员)
            checkActiveAuth(groupId, userId);

            // 3.2 发送系统消息
            //GroupMessage systemMessage = new GroupMessage();
            //systemMessage.setGroupId(groupId);
            //systemMessage.setSenderUserId(userId);
            //systemMessage.setMessageType("9"); // 9=系统
            //systemMessage.setContent("\"" + nickName + "\" 分享了一篇文章");
            //systemMessage.setCreateBy(username);
            //sendMessageInternal(systemMessage); // 发送 "xx分享了..."

            // 3.3 发送文章卡片消息
            GroupMessage articleMessage = new GroupMessage();
            articleMessage.setGroupId(groupId);
            articleMessage.setSenderUserId(userId);
            articleMessage.setMessageType("2"); // 2=文章卡片
            articleMessage.setContent(content); // 存入 JSON
            articleMessage.setCreateBy(username);
            sendMessageInternal(articleMessage); // 发送文章卡片
        }
    }

    /**
     * (新增) 辅助方法：转义 JSON 字符串
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}