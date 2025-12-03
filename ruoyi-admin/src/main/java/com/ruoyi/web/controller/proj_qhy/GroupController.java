package com.ruoyi.web.controller.proj_qhy;

// [修改] 使用自定义 Log 注解
import com.ruoyi.proj_cyq.annotation.Log;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.proj_qhy.domain.Group;
import com.ruoyi.proj_qhy.domain.GroupMessage;
import com.ruoyi.proj_qhy.service.IGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 小组讨论
 */
@RestController
@RequestMapping("/proj_qhy/group")
public class GroupController extends BaseController {

    @Autowired
    private IGroupService groupService;

    /**
     * 获取我的小组列表 (主界面)
     */
    @GetMapping("/list")
    public AjaxResult getGroupList() {
        Long userId = SecurityUtils.getUserId();
        return AjaxResult.success(groupService.selectGroupList(userId));
    }

    /**
     * 创建小组
     * 前端传递 { groupName: "xxx", memberUserIds: [1, 2, 3] }
     */
    @Log(title = "小组-创建", businessType = BusinessType.INSERT)
    @PostMapping("/create")
    public AjaxResult createGroup(@RequestBody Map<String, Object> params) {
        String groupName = (String) params.get("groupName");
        List<Long> memberUserIds = (List<Long>) params.get("memberUserIds");

        Group group = new Group();
        group.setGroupName(groupName);

        return AjaxResult.success(groupService.createGroup(group, memberUserIds));
    }

    /**
     * 获取小组详情 (用于聊天窗口)
     */
    @GetMapping("/{groupId}")
    public AjaxResult getGroupDetails(@PathVariable Long groupId) {
        Long userId = SecurityUtils.getUserId();
        return AjaxResult.success(groupService.getGroupDetails(groupId, userId));
    }

    /**
     * 修改小组信息 (名称/头像)
     */
    @Log(title = "小组-修改信息", businessType = BusinessType.UPDATE)
    @PutMapping("/update")
    public AjaxResult updateGroupInfo(@RequestBody Group group) {
        // 权限校验在Service层
        return toAjax(groupService.updateGroupInfo(group));
    }

    /**
     * (新增) 修改小组头像
     * 我们将头像文件命名为 'avatarfile'
     */
    @Log(title = "小组-修改头像", businessType = BusinessType.UPDATE)
    @PostMapping("/{groupId}/avatar")
    public AjaxResult updateGroupAvatar(@PathVariable Long groupId, @RequestParam("avatarfile") MultipartFile file) {
        // 权限和上传在Service层
        String newAvatarUrl = groupService.updateGroupAvatar(groupId, file);
        // 返回新的URL给前端
        return AjaxResult.success("上传成功", newAvatarUrl);
    }

    /**
     * 获取聊天记录 (非实时)
     */
    @GetMapping("/{groupId}/messages")
    public AjaxResult getChatMessages(@PathVariable Long groupId) {
        Long userId = SecurityUtils.getUserId();
        // Service层会更新已读
        return AjaxResult.success(groupService.getChatMessages(groupId, userId));
    }

    /**
     * 发送消息 (文本或图片)
     */
    @Log(title = "小组-发送消息", businessType = BusinessType.INSERT)
    @PostMapping("/{groupId}/send")
    public AjaxResult sendMessage(@PathVariable Long groupId,
                                  GroupMessage message,
                                  @RequestParam(value = "image", required = false) MultipartFile image) {
        message.setGroupId(groupId);
        // 权限校验和图片处理在Service层
        return AjaxResult.success(groupService.sendMessage(message, image));
    }

    /**
     * 移除成员 (组长权限)
     */
    @Log(title = "小组-移除成员", businessType = BusinessType.DELETE)
    @DeleteMapping("/{groupId}/member/{memberUserId}")
    public AjaxResult removeMember(@PathVariable Long groupId, @PathVariable Long memberUserId) {
        // 权限校验在Service层
        groupService.removeMember(groupId, memberUserId);
        return AjaxResult.success();
    }

    /**
     * 搜索小组
     * @param type (number 或 name)
     * @param query (小组号 或 名称)
     */
    @GetMapping("/search")
    public AjaxResult searchGroups(@RequestParam String type, @RequestParam String query) {
        Long userId = SecurityUtils.getUserId();
        return AjaxResult.success(groupService.searchGroups(query, type, userId));
    }

    /**
     * 加入小组
     */
    @Log(title = "小组-加入", businessType = BusinessType.INSERT)
    @PostMapping("/join/{groupId}")
    public AjaxResult joinGroup(@PathVariable Long groupId) {
        Long userId = SecurityUtils.getUserId();
        return AjaxResult.success(groupService.joinGroup(groupId, userId));
    }

    /**
     * (新增) 解散小组 (组长权限)
     */
    @Log(title = "小组-解散", businessType = BusinessType.DELETE)
    @DeleteMapping("/{groupId}/disband")
    public AjaxResult disbandGroup(@PathVariable Long groupId) {
        // 权限在Service层校验
        groupService.disbandGroup(groupId);
        return AjaxResult.success("小组已解散");
    }

    /**
     * (新增) 退出小组
     */
    @Log(title = "小组-退出", businessType = BusinessType.DELETE)
    @PostMapping("/{groupId}/exit")
    public AjaxResult exitGroup(@PathVariable Long groupId) {
        // 逻辑在Service层处理
        groupService.exitGroup(groupId);
        return AjaxResult.success("已退出小组");
    }

    /**
     * (新增) 撤回消息
     */
    @Log(title = "小组-撤回消息", businessType = BusinessType.UPDATE)
    @PostMapping("/message/recall/{messageId}")
    public AjaxResult recallMessage(@PathVariable Long messageId) {
        // 权限和时间校验在Service层
        groupService.recallMessage(messageId);
        return AjaxResult.success("撤回成功");
    }

    /**
     * (新增) 分享文章到小组
     */
    @Log(title = "小组-分享文章", businessType = BusinessType.INSERT)
    @PostMapping("/share/article")
    public AjaxResult shareArticle(@RequestBody Map<String, Object> payload) {
        // (注意: 确保 Spring Boot 可以正确反序列化 Long 和 List)
        Long articleId = Long.parseLong(payload.get("articleId").toString());

        @SuppressWarnings("unchecked")
        List<Long> groupIds = ((List<?>) payload.get("groupIds")).stream()
                .map(id -> Long.parseLong(id.toString()))
                .collect(Collectors.toList());

        groupService.shareArticle(articleId, groupIds);
        return AjaxResult.success("分享成功");
    }
}