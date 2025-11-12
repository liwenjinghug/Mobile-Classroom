package com.ruoyi.proj_cyq.manager.factory;

import java.util.TimerTask;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ruoyi.common.utils.LogUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.AddressUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.proj_cyq.domain.ClassLoginLog;
import com.ruoyi.proj_cyq.service.IClassLoginLogService;
import com.ruoyi.proj_cyq.common.Constants;

import eu.bitwalker.useragentutils.UserAgent;

import javax.servlet.http.HttpServletRequest;

/**
 * 异步工厂（产生任务用）
 */
public class AsyncFactory {
    private static final Logger sys_user_logger = LoggerFactory.getLogger(AsyncFactory.class);

    /**
     * 记录登录信息 - 只处理登录相关
     */
    public static TimerTask recordLoginInfo(final String username, final String status, final String message,
                                            final Object... args) {
        return new TimerTask() {
            @Override
            public void run() {
                try {
                    // 获取用户代理和IP
                    String ip = "未知";
                    String userAgentString = "未知";
                    String address = "未知";
                    String browser = "Unknown";
                    String os = "Unknown";

                    try {
                        // 方式1: 使用 RequestContextHolder 获取请求信息
                        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                        if (attributes != null) {
                            HttpServletRequest request = attributes.getRequest();
                            ip = IpUtils.getIpAddr(request);
                            userAgentString = request.getHeader("User-Agent");
                            address = AddressUtils.getRealAddressByIP(ip);
                        } else {
                            // 方式2: 使用 ServletUtils 获取请求信息
                            if (ServletUtils.getRequest() != null) {
                                ip = IpUtils.getIpAddr(ServletUtils.getRequest());
                                userAgentString = ServletUtils.getRequest().getHeader("User-Agent");
                                address = AddressUtils.getRealAddressByIP(ip);
                            }
                        }

                        // 解析用户代理
                        if (StringUtils.isNotEmpty(userAgentString) && !"未知".equals(userAgentString)) {
                            final UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);
                            os = userAgent.getOperatingSystem().getName();
                            browser = userAgent.getBrowser().getName();
                        }
                    } catch (Exception e) {
                        sys_user_logger.warn("获取请求信息失败: {}", e.getMessage());
                    }

                    // 日志输出
                    StringBuilder s = new StringBuilder();
                    s.append(LogUtils.getBlock(ip));
                    s.append(address);
                    s.append(LogUtils.getBlock(username));
                    s.append(LogUtils.getBlock(status));
                    s.append(LogUtils.getBlock(message));
                    sys_user_logger.info(s.toString(), args);

                    // 封装登录日志对象
                    ClassLoginLog loginLog = new ClassLoginLog();
                    loginLog.setUserName(username);
                    loginLog.setIpaddr(ip);
                    loginLog.setLoginLocation(address);
                    loginLog.setBrowser(browser);
                    loginLog.setOs(os);
                    loginLog.setLoginTime(new Date());

                    // 状态处理逻辑 - 只处理登录相关
                    if (Constants.LOGIN_SUCCESS.equals(status)) {
                        loginLog.setStatus(0); // 登录成功
                        loginLog.setMsg(message);
                    } else if (Constants.LOGIN_FAIL.equals(status)) {
                        loginLog.setStatus(1); // 登录失败
                        loginLog.setMsg(message);
                    } else {
                        loginLog.setStatus(0); // 默认成功
                        loginLog.setMsg(message);
                    }

                    // 调试日志
                    sys_user_logger.info("记录登录日志 - 用户: {}, 状态: {}, 消息: {}, 最终状态值: {}",
                            username, status, message, loginLog.getStatus());

                    // 插入数据
                    IClassLoginLogService loginLogService = SpringUtils.getBean(IClassLoginLogService.class);
                    int result = loginLogService.insertClassLoginLog(loginLog);
                    sys_user_logger.info("登录日志插入结果: {}, ID: {}", result, loginLog.getLoginId());

                } catch (Exception e) {
                    sys_user_logger.error("记录登录日志异常: {}", e.getMessage(), e);
                }
            }
        };
    }
}