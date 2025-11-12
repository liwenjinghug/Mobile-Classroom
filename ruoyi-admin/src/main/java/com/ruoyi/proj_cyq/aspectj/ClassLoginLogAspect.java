package com.ruoyi.proj_cyq.aspectj;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.proj_cyq.common.Constants;

import javax.servlet.http.HttpServletRequest;

/**
 * 登录日志切面处理
 */
@Aspect
@Component
public class ClassLoginLogAspect {
    private static final Logger log = LoggerFactory.getLogger(ClassLoginLogAspect.class);

    /**
     * 登录方法切入点 - 拦截登录控制器
     */
    @Pointcut("execution(* com.ruoyi.web.controller.system.SysLoginController.login(..))")
    public void loginPointCut() {
    }

    /**
     * 登录成功处理
     */
    @AfterReturning(pointcut = "loginPointCut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        try {
            log.info("登录成功切面触发，开始记录登录日志");

            // 获取请求对象
            HttpServletRequest request = getRequest();
            if (request == null) {
                log.warn("无法获取HttpServletRequest，跳过记录登录日志");
                return;
            }

            // 从请求参数中获取用户名
            String username = extractUsernameFromArgs(joinPoint.getArgs());

            if (StringUtils.isNotEmpty(username)) {
                // 获取客户端信息
                String ip = getClientIp(request);
                String browser = getBrowser(request);
                String os = getOs(request);
                String location = getLocation(ip);

                log.info("客户端信息 - IP: {}, 浏览器: {}, 操作系统: {}, 地点: {}", ip, browser, os, location);

                // 直接构建登录日志对象并保存
                saveLoginLogDirectly(username, Constants.LOGIN_SUCCESS, "登录成功", ip, location, browser, os);
                log.info("成功记录用户 {} 的登录成功日志", username);
            } else {
                log.warn("无法获取用户名，跳过记录登录日志");
            }
        } catch (Exception e) {
            log.error("记录登录成功日志异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 登录异常处理
     */
    @AfterThrowing(pointcut = "loginPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        try {
            log.info("登录失败切面触发，开始记录登录失败日志");

            // 获取请求对象
            HttpServletRequest request = getRequest();
            if (request == null) {
                log.warn("无法获取HttpServletRequest，跳过记录登录失败日志");
                return;
            }

            // 获取用户名
            String username = extractUsernameFromArgs(joinPoint.getArgs());

            if (StringUtils.isNotEmpty(username)) {
                String errorMsg = getLoginErrorMessage(e);

                // 获取客户端信息
                String ip = getClientIp(request);
                String browser = getBrowser(request);
                String os = getOs(request);
                String location = getLocation(ip);

                log.info("客户端信息 - IP: {}, 浏览器: {}, 操作系统: {}, 地点: {}", ip, browser, os, location);

                // 直接构建登录日志对象并保存
                saveLoginLogDirectly(username, Constants.LOGIN_FAIL, errorMsg, ip, location, browser, os);
                log.info("成功记录用户 {} 的登录失败日志: {}", username, errorMsg);
            } else {
                log.warn("无法获取用户名，跳过记录登录失败日志");
            }
        } catch (Exception ex) {
            log.error("记录登录失败日志异常: {}", ex.getMessage(), ex);
        }
    }

    /**
     * 直接保存登录日志到数据库
     */
    private void saveLoginLogDirectly(String username, String status, String message,
                                      String ip, String location, String browser, String os) {
        try {
            // 创建定时任务来保存日志
            AsyncManager.me().execute(new java.util.TimerTask() {
                @Override
                public void run() {
                    try {
                        // 构建登录日志对象
                        com.ruoyi.proj_cyq.domain.ClassLoginLog loginLog = new com.ruoyi.proj_cyq.domain.ClassLoginLog();
                        loginLog.setUserName(username);
                        loginLog.setStatus(Integer.parseInt(status));
                        loginLog.setMsg(message);
                        loginLog.setIpaddr(ip);
                        loginLog.setLoginLocation(location);
                        loginLog.setBrowser(browser);
                        loginLog.setOs(os);
                        loginLog.setLoginTime(new java.util.Date());

                        // 直接调用Service保存
                        com.ruoyi.common.utils.spring.SpringUtils.getBean(
                                com.ruoyi.proj_cyq.service.IClassLoginLogService.class
                        ).insertClassLoginLog(loginLog);

                        log.info("直接保存登录日志成功: 用户={}, 状态={}, IP={}", username, status, ip);
                    } catch (Exception e) {
                        log.error("直接保存登录日志异常: {}", e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            log.error("创建登录日志保存任务异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取HttpServletRequest对象
     */
    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes)
                    RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest();
            }
        } catch (Exception e) {
            log.warn("获取HttpServletRequest失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        try {
            return IpUtils.getIpAddr(request);
        } catch (Exception e) {
            log.warn("获取客户端IP失败: {}", e.getMessage());
            return "未知";
        }
    }

    /**
     * 获取浏览器信息 - 使用简单的字符串匹配
     */
    private String getBrowser(HttpServletRequest request) {
        try {
            String userAgent = request.getHeader("User-Agent");
            if (StringUtils.isEmpty(userAgent)) {
                return "未知";
            }

            String ua = userAgent.toLowerCase();

            // 浏览器检测
            if (ua.contains("msie") || ua.contains("trident")) {
                return "Internet Explorer";
            } else if (ua.contains("edg")) {
                return "Microsoft Edge";
            } else if (ua.contains("chrome")) {
                return "Chrome";
            } else if (ua.contains("firefox")) {
                return "Firefox";
            } else if (ua.contains("safari") && !ua.contains("chrome")) {
                return "Safari";
            } else if (ua.contains("opera")) {
                return "Opera";
            } else {
                return "其他浏览器";
            }
        } catch (Exception e) {
            log.warn("解析浏览器信息失败: {}", e.getMessage());
            return "未知";
        }
    }

    /**
     * 获取操作系统信息 - 使用简单的字符串匹配
     */
    private String getOs(HttpServletRequest request) {
        try {
            String userAgent = request.getHeader("User-Agent");
            if (StringUtils.isEmpty(userAgent)) {
                return "未知";
            }

            String ua = userAgent.toLowerCase();

            // 操作系统检测
            if (ua.contains("windows")) {
                if (ua.contains("windows nt 10.0")) {
                    return "Windows 10";
                } else if (ua.contains("windows nt 6.3")) {
                    return "Windows 8.1";
                } else if (ua.contains("windows nt 6.2")) {
                    return "Windows 8";
                } else if (ua.contains("windows nt 6.1")) {
                    return "Windows 7";
                } else if (ua.contains("windows nt 6.0")) {
                    return "Windows Vista";
                } else if (ua.contains("windows nt 5.1")) {
                    return "Windows XP";
                } else {
                    return "Windows";
                }
            } else if (ua.contains("mac")) {
                return "Mac OS X";
            } else if (ua.contains("linux")) {
                return "Linux";
            } else if (ua.contains("android")) {
                return "Android";
            } else if (ua.contains("iphone") || ua.contains("ipad")) {
                return "iOS";
            } else {
                return "其他操作系统";
            }
        } catch (Exception e) {
            log.warn("解析操作系统信息失败: {}", e.getMessage());
            return "未知";
        }
    }

    /**
     * 获取登录地点（根据IP地址）
     */
    private String getLocation(String ip) {
        try {
            if (StringUtils.isEmpty(ip) || "未知".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "127.0.0.1".equals(ip)) {
                return "内网IP";
            }

            // 判断是否为内网IP
            if (isInternalIp(ip)) {
                return "内网IP";
            } else {
                // 对于外网IP，暂时返回"未知"
                return "未知";
            }

        } catch (Exception e) {
            log.warn("获取登录地点失败: {}", e.getMessage());
            return "未知";
        }
    }

    /**
     * 判断是否为内网IP
     */
    private boolean isInternalIp(String ip) {
        if (StringUtils.isEmpty(ip)) {
            return false;
        }

        // 内网IP段
        return ip.startsWith("10.") ||
                ip.startsWith("192.168.") ||
                ip.startsWith("172.16.") ||
                ip.startsWith("172.17.") ||
                ip.startsWith("172.18.") ||
                ip.startsWith("172.19.") ||
                ip.startsWith("172.20.") ||
                ip.startsWith("172.21.") ||
                ip.startsWith("172.22.") ||
                ip.startsWith("172.23.") ||
                ip.startsWith("172.24.") ||
                ip.startsWith("172.25.") ||
                ip.startsWith("172.26.") ||
                ip.startsWith("172.27.") ||
                ip.startsWith("172.28.") ||
                ip.startsWith("172.29.") ||
                ip.startsWith("172.30.") ||
                ip.startsWith("172.31.") ||
                "127.0.0.1".equals(ip) ||
                "0:0:0:0:0:0:0:1".equals(ip);
    }

    /**
     * 从方法参数中提取用户名
     */
    private String extractUsernameFromArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }

        try {
            // 若依框架的登录方法通常接收LoginBody对象
            for (Object arg : args) {
                if (arg != null) {
                    // 使用反射获取username字段
                    Class<?> argClass = arg.getClass();
                    try {
                        java.lang.reflect.Field usernameField = argClass.getDeclaredField("username");
                        usernameField.setAccessible(true);
                        Object usernameValue = usernameField.get(arg);
                        if (usernameValue instanceof String) {
                            return (String) usernameValue;
                        }
                    } catch (NoSuchFieldException e) {
                        // 如果没有username字段，尝试其他方式
                        log.debug("参数中没有username字段，尝试其他方式获取用户名");
                    }

                    // 尝试调用getUsername方法
                    try {
                        java.lang.reflect.Method getUsernameMethod = argClass.getMethod("getUsername");
                        Object usernameValue = getUsernameMethod.invoke(arg);
                        if (usernameValue instanceof String) {
                            return (String) usernameValue;
                        }
                    } catch (Exception e) {
                        log.debug("无法通过getUsername方法获取用户名: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("提取用户名异常: {}", e.getMessage());
        }

        return "";
    }

    /**
     * 获取登录错误消息
     */
    private String getLoginErrorMessage(Exception e) {
        if (e == null) {
            return "登录失败";
        }

        String errorMsg = e.getMessage();
        if (StringUtils.isEmpty(errorMsg)) {
            return "登录失败";
        }

        // 根据异常类型返回具体的错误信息
        try {
            String exceptionClassName = e.getClass().getSimpleName();

            if (e instanceof com.ruoyi.common.exception.user.CaptchaException) {
                return "验证码错误";
            } else if (e instanceof com.ruoyi.common.exception.user.UserPasswordNotMatchException) {
                return "用户不存在/密码错误";
            } else if (e instanceof com.ruoyi.common.exception.user.UserNotExistsException) {
                return "用户不存在";
            } else if (exceptionClassName.equals("UserPasswordRetryLimitExceedException")) {
                return "密码错误次数超限";
            } else if (exceptionClassName.equals("UserBlockedException") ||
                    exceptionClassName.equals("BlockedException")) {
                return "用户已锁定";
            } else if (exceptionClassName.equals("UserDeleteException")) {
                return "用户已删除";
            } else if (e instanceof com.ruoyi.common.exception.ServiceException) {
                if (errorMsg.contains("用户不存在") || errorMsg.contains("密码错误")) {
                    return "用户不存在/密码错误";
                } else if (errorMsg.contains("验证码")) {
                    return "验证码错误";
                } else if (errorMsg.contains("锁定") || errorMsg.contains("禁用")) {
                    return "用户已锁定";
                } else if (errorMsg.contains("删除")) {
                    return "用户已删除";
                } else if (errorMsg.contains("次数") || errorMsg.contains("重试")) {
                    return "密码错误次数超限";
                }
            }
        } catch (Exception ex) {
            log.warn("解析异常类型时出错: {}", ex.getMessage());
        }

        if (errorMsg.contains("验证码")) {
            return "验证码错误";
        } else if (errorMsg.contains("密码") || errorMsg.contains("用户不存在")) {
            return "用户不存在/密码错误";
        } else if (errorMsg.contains("锁定") || errorMsg.contains("禁用")) {
            return "用户已锁定";
        } else if (errorMsg.contains("删除")) {
            return "用户已删除";
        } else if (errorMsg.contains("次数") || errorMsg.contains("重试")) {
            return "密码错误次数超限";
        }

        return StringUtils.substring(errorMsg, 0, 100);
    }
}