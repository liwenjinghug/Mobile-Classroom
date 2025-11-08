package com.ruoyi.proj_cyq.manager.factory;

import java.util.TimerTask;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.LogUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.AddressUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.proj_cyq.domain.ClassLoginLog;
import com.ruoyi.proj_cyq.domain.ClassOperLog;
import com.ruoyi.proj_cyq.service.IClassLoginLogService;
import com.ruoyi.proj_cyq.service.IClassOperLogService;

import eu.bitwalker.useragentutils.UserAgent;

/**
 * 异步工厂（产生任务用）
 */
public class AsyncFactory {
    private static final Logger sys_user_logger = LoggerFactory.getLogger(AsyncFactory.class);

    /**
     * 记录登录信息
     *
     * @param username 用户名
     * @param status 状态
     * @param message 消息
     * @param args 列表
     * @return 任务task
     */
    public static TimerTask recordLoginInfo(final String username, final String status, final String message,
                                            final Object... args) {
        final UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        final String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
        return new TimerTask() {
            @Override
            public void run() {
                String address = AddressUtils.getRealAddressByIP(ip);
                StringBuilder s = new StringBuilder();
                s.append(LogUtils.getBlock(ip));
                s.append(address);
                s.append(LogUtils.getBlock(username));
                s.append(LogUtils.getBlock(status));
                s.append(LogUtils.getBlock(message));
                // 打印信息到日志
                sys_user_logger.info(s.toString(), args);
                // 获取客户端操作系统
                String os = userAgent.getOperatingSystem().getName();
                // 获取客户端浏览器
                String browser = userAgent.getBrowser().getName();
                // 封装对象
                ClassLoginLog loginLog = new ClassLoginLog();
                loginLog.setUserName(username);
                loginLog.setIpaddr(ip);
                loginLog.setLoginLocation(address);
                loginLog.setBrowser(browser);
                loginLog.setOs(os);
                loginLog.setMsg(message);
                loginLog.setLoginTime(new Date());
                // 日志状态 - 将字符串状态转换为整数
                if (StringUtils.equalsAny(status, Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER)) {
                    loginLog.setStatus(0); // 成功
                } else if (Constants.LOGIN_FAIL.equals(status)) {
                    loginLog.setStatus(1); // 失败
                } else {
                    loginLog.setStatus(0); // 默认成功
                }
                // 插入数据
                SpringUtils.getBean(IClassLoginLogService.class).insertClassLoginLog(loginLog);
            }
        };
    }

    /**
     * 操作日志记录
     *
     * @param operLog 操作日志信息
     * @return 任务task
     */
    public static TimerTask recordOper(final ClassOperLog operLog) {
        return new TimerTask() {
            @Override
            public void run() {
                // 远程查询操作地点
                operLog.setOperLocation(AddressUtils.getRealAddressByIP(operLog.getOperIp()));
                SpringUtils.getBean(IClassOperLogService.class).insertClassOperLog(operLog);
            }
        };
    }
}