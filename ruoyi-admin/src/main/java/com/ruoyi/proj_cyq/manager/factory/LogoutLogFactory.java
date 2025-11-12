package com.ruoyi.proj_cyq.manager.factory;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.proj_cyq.domain.ClassLoginLog;
import com.ruoyi.proj_cyq.service.IClassLoginLogService;

/**
 * 退出日志记录工厂
 */
public class LogoutLogFactory {
    private static final Logger log = LoggerFactory.getLogger(LogoutLogFactory.class);

    /**
     * 同步记录退出日志 - 避免异步上下文问题
     */
    public static void recordLogoutInfo(String username, String ip, String userAgent) {
        try {
            log.info("开始同步记录退出日志 - 用户: {}, IP: {}", username, ip);

            // 解析浏览器和操作系统
            String browser = "Unknown";
            String os = "Unknown";
            String location = "未知";

            try {
                if (StringUtils.isNotEmpty(userAgent)) {
                    eu.bitwalker.useragentutils.UserAgent ua = eu.bitwalker.useragentutils.UserAgent.parseUserAgentString(userAgent);
                    browser = ua.getBrowser().getName();
                    os = ua.getOperatingSystem().getName();
                }

                // 获取IP地址位置
                if (StringUtils.isNotEmpty(ip) && !"未知".equals(ip)) {
                    location = com.ruoyi.common.utils.ip.AddressUtils.getRealAddressByIP(ip);
                }
            } catch (Exception e) {
                log.warn("解析用户代理信息失败: {}", e.getMessage());
            }

            // 封装登录日志对象
            ClassLoginLog loginLog = new ClassLoginLog();
            loginLog.setUserName(username);
            loginLog.setIpaddr(ip);
            loginLog.setLoginLocation(location);
            loginLog.setBrowser(browser);
            loginLog.setOs(os);
            loginLog.setLoginTime(new Date());
            loginLog.setStatus(0); // 退出成功状态为0
            loginLog.setMsg("退出成功");

            // 同步插入数据
            IClassLoginLogService loginLogService = SpringUtils.getBean(IClassLoginLogService.class);
            int result = loginLogService.insertClassLoginLog(loginLog);

            log.info("退出日志同步插入结果: {}, 用户: {}", result, username);

        } catch (Exception e) {
            log.error("同步记录退出日志异常: {}", e.getMessage(), e);
        }
    }
}