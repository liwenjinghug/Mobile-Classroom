// Java
package com.ruoyi.web.controller.proj_cyq;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import com.ruoyi.proj_cyq.service.IClassPasswordResetService;
import com.ruoyi.proj_cyq.domain.ClassPasswordReset;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

// 【新增】导入你的注解
import com.ruoyi.proj_cyq.annotation.Log;
import com.ruoyi.common.enums.BusinessType;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/proj_cyq/password") // 统一使用您模块的自定义路径
public class ClassPasswordResetController extends BaseController
{
    @Autowired
    private IClassPasswordResetService resetService;

    @Autowired
    private ISysUserService sysUserService; //

    @Autowired
    private JavaMailSender mailSender;

    // 从 application.yml 读取发件人邮箱
    @Value("${spring.mail.username}")
    private String mailFrom;

    // 您的前端页面地址 (ruoyi.frontendUrl:http://www.ylxteach.net:8080)
    @Value("${ruoyi.frontendUrl:http://www.ylxteach.net:8080}")
    private String frontendUrl;


    /**
     * 1. 请求重置密码（发送邮件）
     */
    // 【新增】日志
    @Log(title = "密码重置", businessType = BusinessType.OTHER)
    @PostMapping("/request-reset")
    public AjaxResult requestReset(@RequestBody Map<String, String> body)
    {
        String userName = body.get("userName");
        if (StringUtils.isEmpty(userName)) {
            return AjaxResult.error("用户名不能为空");
        }

        // 1. 查找用户
        SysUser user = sysUserService.selectUserByUserName(userName);

        // 2. 检查用户和邮箱
        if (user == null) {
            return AjaxResult.success("如果账号存在且绑定了邮箱，重置邮件将很快发送。");
        }
        if (StringUtils.isEmpty(user.getEmail())) {
            return AjaxResult.error("该用户未设置邮箱，无法通过邮件找回");
        }

        // 3. 创建 Token
        String token = resetService.createResetToken(user);

        // 4. 【修正】在异步块 *之前* 定义 message 变量
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(user.getEmail());
        message.setSubject("【在线课堂】密码重置");
        String url = frontendUrl + "/#/reset-password?token=" + token;
        message.setText("您好，" + user.getUserName() + "：\n\n您正在请求重置密码。\n请在30分钟内点击以下链接完成重置：\n" + url + "\n\n如果不是您本人操作，请忽略此邮件。");

        // (为了在 lambda 中安全使用，创建一个 'final' 副本)
        final SysUser finalUser = user;

        // 5. 【异步】发送邮件
        CompletableFuture.runAsync(() -> {
            try {
                mailSender.send(message);
                logger.info("密码重置邮件已异步发送至: {}", finalUser.getEmail());
            } catch (Exception e) {
                logger.error("异步发送密码重置邮件失败: {}", e.getMessage());
            }
        });

        // 6. 立即返回成功提示给前端
        return AjaxResult.success("重置邮件已发送（" + user.getEmail() + "），请在30分钟内查收。");
    }

    /**
     * 2. 验证 Token 是否有效
     */
    // (验证操作通常不记录日志)
    @GetMapping("/verify-token")
    public AjaxResult verify(@RequestParam String token)
    {
        ClassPasswordReset r = resetService.verifyToken(token);
        return r != null ? AjaxResult.success("Token有效") : AjaxResult.error("链接无效或已过期");
    }

    /**
     * 3. 提交新密码
     */
    // 【新增】日志
    @Log(title = "密码重置", businessType = BusinessType.UPDATE)
    @PostMapping("/reset-password")
    public AjaxResult resetPassword(@RequestBody Map<String, String> body)
    {
        String token = body.get("token");
        String newPassword = body.get("newPassword");

        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(newPassword)) {
            return AjaxResult.error("参数不完整");
        }

        // 1. 验证 Token
        ClassPasswordReset r = resetService.verifyToken(token);
        if (r == null) {
            return AjaxResult.error("链接无效或已过期，请重新请求");
        }

        // 2. 【核心】使用若依的工具加密密码
        String encryptedPwd = SecurityUtils.encryptPassword(newPassword); //

        // 3. 【核心】调用若依的服务更新密码
        int rows = sysUserService.resetUserPwd(r.getUserId(), encryptedPwd); //

        if (rows > 0) {
            // 4. 标记 Token 已使用
            resetService.markUsed(r.getId());
            return AjaxResult.success("密码重置成功，请使用新密码登录");
        } else {
            return AjaxResult.error("密码重置失败，请稍后再试");
        }
    }
}