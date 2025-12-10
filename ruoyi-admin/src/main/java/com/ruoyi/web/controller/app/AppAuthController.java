package com.ruoyi.web.controller.app;

import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.AppLoginBody;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.framework.web.service.SysLoginService;
import com.ruoyi.framework.web.service.SysPermissionService;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.system.service.ISysUserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import com.ruoyi.framework.security.context.AuthenticationContextHolder;

/**
 * App登录验证
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/app/login")
public class AppAuthController
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysLoginService loginService;

    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private SysPermissionService permissionService;
    
    @Autowired
    private AuthenticationManager authenticationManager;

    @Value("${wx.miniapp.appid}")
    private String wxAppId;

    @Value("${wx.miniapp.secret}")
    private String wxSecret;

    /**
     * 微信一键登录检查
     */
    @PostMapping("/check")
    public AjaxResult check(@RequestBody AppLoginBody loginBody)
    {
        String code = loginBody.getCode();
        // 换取 openid
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + wxAppId + "&secret=" + wxSecret + "&js_code=" + code + "&grant_type=authorization_code";
        String res = HttpUtils.sendGet(url);
        JSONObject json = JSON.parseObject(res);
        String openId = json.getString("openid");
        
        if (openId == null)
        {
            return AjaxResult.error("获取OpenID失败: " + json.getString("errmsg"));
        }

        SysUser user = userService.selectUserByOpenId(openId);
        if (user != null)
        {
            // 用户存在，直接登录
            Set<String> permissions = permissionService.getMenuPermission(user);
            LoginUser loginUser = new LoginUser(user.getUserId(), user.getDeptId(), user, permissions);
            String token = tokenService.createToken(loginUser);
            return AjaxResult.success().put("token", token);
        }
        else
        {
            // 用户不存在，返回 202 和 openId
            AjaxResult ajax = AjaxResult.success();
            ajax.put("code", 202);
            ajax.put("msg", "用户未绑定");
            ajax.put("openId", openId);
            return ajax;
        }
    }

    /**
     * 绑定并登录
     */
    @PostMapping("/bind")
    public AjaxResult bind(@RequestBody AppLoginBody loginBody)
    {
        String username = loginBody.getUsername();
        String password = loginBody.getPassword();
        String openId = loginBody.getOpenId();
        
        // 1. 验证账号密码
        Authentication authentication = null;
        try
        {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            AuthenticationContextHolder.setContext(authenticationToken);
            authentication = authenticationManager.authenticate(authenticationToken);
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
        finally
        {
            AuthenticationContextHolder.clearContext();
        }
        
        // 2. 更新用户信息
        SysUser user = userService.selectUserByUserName(username);
        user.setOpenId(openId);
        user.setNickName(loginBody.getNickname());
        user.setAvatar(loginBody.getAvatar());
        userService.updateUser(user);
        
        // 3. 生成 Token
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        // 更新 loginUser 中的用户信息，以便 Token 中包含最新数据
        loginUser.getUser().setOpenId(openId);
        loginUser.getUser().setNickName(loginBody.getNickname());
        loginUser.getUser().setAvatar(loginBody.getAvatar());
        
        String token = tokenService.createToken(loginUser);
        
        AjaxResult ajax = AjaxResult.success();
        ajax.put("token", token);
        return ajax;
    }
}
