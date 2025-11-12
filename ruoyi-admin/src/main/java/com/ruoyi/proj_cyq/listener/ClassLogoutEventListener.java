package com.ruoyi.proj_cyq.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.proj_cyq.manager.factory.LogoutLogFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * 登出事件监听器
 */
@Component
public class ClassLogoutEventListener implements ApplicationListener<LogoutSuccessEvent> {
    private static final Logger log = LoggerFactory.getLogger(ClassLogoutEventListener.class);

    @Override
    public void onApplicationEvent(LogoutSuccessEvent event) {
        try {
            log.info("=== Spring Security 登出事件触发 ===");

            String username = "未知用户";
            String ip = "未知";
            String userAgent = "未知";

            // 从事件中获取用户信息
            Object principal = event.getAuthentication().getPrincipal();
            log.info("Principal 类型: {}", principal.getClass().getName());

            if (principal instanceof com.ruoyi.common.core.domain.model.LoginUser) {
                com.ruoyi.common.core.domain.model.LoginUser loginUser = (com.ruoyi.common.core.domain.model.LoginUser) principal;
                if (StringUtils.isNotEmpty(loginUser.getUsername())) {
                    username = loginUser.getUsername();
                }
            } else if (principal instanceof String) {
                username = (String) principal;
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                org.springframework.security.core.userdetails.UserDetails userDetails =
                        (org.springframework.security.core.userdetails.UserDetails) principal;
                username = userDetails.getUsername();
            }

            // 获取请求信息
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    ip = com.ruoyi.common.utils.ip.IpUtils.getIpAddr(request);
                    userAgent = request.getHeader("User-Agent");
                    log.info("获取到退出请求信息 - IP: {}, UserAgent: {}", ip, userAgent);
                }
            } catch (Exception e) {
                log.warn("获取请求信息失败: {}", e.getMessage());
            }

            log.info("退出用户: {}, IP: {}", username, ip);

            // 同步记录退出日志
            LogoutLogFactory.recordLogoutInfo(username, ip, userAgent);

            log.info("=== 成功记录用户 {} 的退出日志到自定义表 ===", username);

        } catch (Exception e) {
            log.error("记录退出日志异常: {}", e.getMessage(), e);
        }
    }
}