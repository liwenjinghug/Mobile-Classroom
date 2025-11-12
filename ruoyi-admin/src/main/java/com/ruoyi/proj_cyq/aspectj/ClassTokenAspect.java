package com.ruoyi.proj_cyq.aspectj;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.proj_cyq.manager.factory.LogoutLogFactory;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.proj_cyq.service.IClassLoginLogService;
import com.ruoyi.proj_cyq.domain.ClassLoginLog;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Token操作切面 - 退出日志记录
 */
@Aspect
@Component
public class ClassTokenAspect {
    private static final Logger log = LoggerFactory.getLogger(ClassTokenAspect.class);

    /**
     * 拦截TokenService的delLoginUser方法
     */
    @Pointcut("execution(* com.ruoyi.framework.web.service.TokenService.delLoginUser(..))")
    public void tokenDeletePointCut() {
    }

    /**
     * 在Token删除前记录退出日志 - 使用@Before确保在Token删除前执行
     */
    @Before("tokenDeletePointCut()")
    public void doBeforeTokenDelete(JoinPoint joinPoint) {
        try {
            log.info("=== ClassTokenAspect 开始执行 ===");

            String username = "未知用户";
            String ip = "未知";
            String userAgent = "未知";

            // 先获取请求信息（IP地址）
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    ip = IpUtils.getIpAddr(request);
                    userAgent = request.getHeader("User-Agent");
                    log.info("获取到请求信息 - IP: {}, UserAgent: {}", ip, userAgent);
                }
            } catch (Exception e) {
                log.warn("获取请求信息失败: {}", e.getMessage());
            }

            // 方法一：从class_login_log表查询最近登录用户（最可靠）
            if (!"未知".equals(ip)) {
                username = getUsernameFromLoginLog(ip);
                if (!"未知用户".equals(username)) {
                    log.info("通过数据库查询获取到退出用户: {}", username);
                }
            }

            // 方法二：从SecurityUtils获取（备用）
            if ("未知用户".equals(username)) {
                try {
                    LoginUser loginUser = SecurityUtils.getLoginUser();
                    if (loginUser != null && StringUtils.isNotEmpty(loginUser.getUsername())) {
                        username = loginUser.getUsername();
                        log.info("通过SecurityUtils获取到退出用户: {}", username);
                    } else {
                        log.warn("SecurityUtils返回的LoginUser为空");
                    }
                } catch (Exception e) {
                    log.warn("SecurityUtils获取用户信息失败: {}", e.getMessage());
                }
            }

            // 方法三：从方法参数中获取Token尝试解析
            if ("未知用户".equals(username)) {
                Object[] args = joinPoint.getArgs();
                if (args != null && args.length > 0) {
                    for (Object arg : args) {
                        if (arg instanceof String) {
                            String token = (String) arg;
                            if (StringUtils.isNotEmpty(token)) {
                                log.info("从方法参数获取到Token，尝试解析");
                                String tokenUsername = extractUsernameFromToken(token);
                                if (!"未知用户".equals(tokenUsername)) {
                                    username = tokenUsername;
                                    log.info("从Token解析获取到用户名: {}", username);
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            // 记录退出日志
            if (!"未知用户".equals(username)) {
                LogoutLogFactory.recordLogoutInfo(username, ip, userAgent);
                log.info("=== ClassTokenAspect 成功记录用户 {} 的退出日志 ===", username);
            } else {
                log.warn("ClassTokenAspect 无法获取用户名，记录未知用户退出日志");
                LogoutLogFactory.recordLogoutInfo("未知用户", ip, userAgent);
            }

        } catch (Exception e) {
            log.error("Token删除切面异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 从登录日志表查询最近登录的用户名
     */
    private String getUsernameFromLoginLog(String ip) {
        try {
            log.info("尝试从class_login_log表查询IP {} 的最近登录用户", ip);

            IClassLoginLogService loginLogService = SpringUtils.getBean(IClassLoginLogService.class);

            // 由于Mapper是注解方式，我们需要手动实现时间范围查询
            // 先获取所有记录，然后在内存中过滤
            List<ClassLoginLog> allLogs = loginLogService.selectClassLoginLogList(new ClassLoginLog());

            if (allLogs != null && !allLogs.isEmpty()) {
                // 过滤：同一IP、成功状态、最近10分钟内
                Date tenMinutesAgo = new Date(System.currentTimeMillis() - 10 * 60 * 1000);

                List<ClassLoginLog> filteredLogs = allLogs.stream()
                        .filter(log -> ip.equals(log.getIpaddr()))
                        .filter(log -> log.getStatus() != null && log.getStatus() == 0)
                        .filter(log -> log.getLoginTime() != null && log.getLoginTime().after(tenMinutesAgo))
                        .filter(log -> StringUtils.isNotEmpty(log.getUserName()) && !"未知用户".equals(log.getUserName()))
                        .sorted((a, b) -> b.getLoginTime().compareTo(a.getLoginTime()))
                        .collect(Collectors.toList());

                if (!filteredLogs.isEmpty()) {
                    String foundUsername = filteredLogs.get(0).getUserName();
                    log.info("从class_login_log表找到最近登录用户: {}", foundUsername);
                    return foundUsername;
                }
            }

            log.warn("class_login_log表中未找到IP {} 的最近登录记录", ip);
            return "未知用户";

        } catch (Exception e) {
            log.warn("从class_login_log表查询用户失败: {}", e.getMessage());
            return "未知用户";
        }
    }

    /**
     * 从Token中提取用户名 - 简化版本
     */
    private String extractUsernameFromToken(String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            log.info("Token信息（前10位）: {}", token.substring(0, Math.min(token.length(), 10)) + "...");

            // 暂时返回未知用户，先确保功能可用
            return "未知用户";

        } catch (Exception e) {
            log.warn("从Token解析用户名失败: {}", e.getMessage());
            return "未知用户";
        }
    }
}