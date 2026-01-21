package com.ruoyi.framework.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.RegisterBody;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.user.CaptchaException;
import com.ruoyi.common.exception.user.CaptchaExpireException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysUserService;
// 【关键】引入 JDBC 工具
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Date;

@Component
public class SysRegisterService
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private RedisCache redisCache;

    // 【关键】注入 JdbcTemplate
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 注册
     */
    public String register(RegisterBody registerBody)
    {
        // 1. 添加显眼的控制台日志，确认代码生效
        System.out.println("====== [DEBUG] 正在执行注册逻辑 (新代码已生效) ======");

        String msg = "", username = registerBody.getUsername(), password = registerBody.getPassword();
        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);

        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled)
        {
            validateCaptcha(username, registerBody.getCode(), registerBody.getUuid());
        }

        if (StringUtils.isEmpty(username))
        {
            msg = "用户名不能为空";
        }
        else if (StringUtils.isEmpty(password))
        {
            msg = "用户密码不能为空";
        }
        else if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            msg = "账户长度必须在2到20个字符之间";
        }
        else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            msg = "密码长度必须在5到20个字符之间";
        }
        else if (!userService.checkUserNameUnique(sysUser))
        {
            msg = "保存用户'" + username + "'失败，注册账号已存在";
        }
        else
        {
            if (StringUtils.isEmpty(registerBody.getNickName())) {
                sysUser.setNickName(username);
            } else {
                sysUser.setNickName(registerBody.getNickName());
            }

            sysUser.setPwdUpdateDate(DateUtils.getNowDate());
            sysUser.setPassword(SecurityUtils.encryptPassword(password));

            // 设置角色 (默认为学生 101)
            Long roleId = registerBody.getRoleId();
            if (roleId == null) {
                roleId = 101L;
            }
            sysUser.setRoleIds(new Long[]{roleId});

            // 2. 插入系统用户表 (sys_user)
            boolean regFlag = userService.registerUser(sysUser);

            if (!regFlag)
            {
                msg = "注册失败,请联系系统管理人员";
                System.out.println("====== [DEBUG] sys_user 插入失败 ======");
            }
            else
            {
                System.out.println("====== [DEBUG] sys_user 插入成功，ID: " + sysUser.getUserId());

                // 3. 【防回滚核心】插入学生表 (class_student)
                if (roleId == 101L) {
                    try {
                        System.out.println("====== [DEBUG] 正在尝试插入 class_student ======");

                        String sql = "INSERT INTO class_student " +
                                "(user_id, student_no, student_name, gender, status, create_time) " +
                                "VALUES (?, ?, ?, ?, ?, ?)";

                        // 准备参数
                        Long userId = sysUser.getUserId();
                        String studentNo = "S" + System.currentTimeMillis();
                        String studentName = sysUser.getNickName();
                        String gender = "M";
                        Integer status = 1;
                        Date createTime = new Date();

                        int rows = jdbcTemplate.update(sql, userId, studentNo, studentName, gender, status, createTime);
                        System.out.println("====== [DEBUG] class_student 插入成功，行数: " + rows + " ======");

                    } catch (Exception e) {
                        // 【重点】这里捕获了所有错误，只打印，不抛出！
                        // 这样即使 SQL 写错了，sys_user 表的数据也绝对保留下来。
                        System.err.println("====== [ERROR] 学生表插入报错 (不影响账号注册) ======");
                        e.printStackTrace();
                    }
                }

                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.REGISTER, MessageUtils.message("user.register.success")));
            }
        }
        return msg;
    }

    public void validateCaptcha(String username, String code, String uuid)
    {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null)
        {
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha))
        {
            throw new CaptchaException();
        }
    }
}