package com.ruoyi.proj_cyq.aspectj;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
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
// 添加IpUtils导入
import com.ruoyi.common.utils.ip.IpUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 退出登录日志切面处理 - 基于Token删除
 */
@Aspect
@Component
public class ClassLogoutLogAspect {
    private static final Logger log = LoggerFactory.getLogger(ClassLogoutLogAspect.class);

    /**
     * 拦截TokenService的delLoginUser方法
     */
    @Pointcut("execution(* com.ruoyi.framework.web.service.TokenService.delLoginUser(..))")
    public void tokenDeletePointCut() {
    }

    /**
     * 在Token删除前后记录退出日志
     */
    @Around("tokenDeletePointCut()")
    public Object doAroundTokenDelete(ProceedingJoinPoint joinPoint) throws Throwable {
        String username = "未知用户";
        String ip = "未知";
        String userAgent = "未知";

        try {
            log.info("检测到Token删除操作，准备记录退出日志");

            // 方法1：从方法参数中获取LoginUser（最可靠）
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                for (Object arg : args) {
                    if (arg instanceof String) {
                        // 如果是token字符串，尝试获取用户信息
                        String token = (String) arg;
                        username = getUsernameByToken(token);
                        if (!"未知用户".equals(username)) {
                            break;
                        }
                    } else if (arg instanceof LoginUser) {
                        LoginUser loginUser = (LoginUser) arg;
                        if (loginUser != null && StringUtils.isNotEmpty(loginUser.getUsername())) {
                            username = loginUser.getUsername();
                            log.info("从LoginUser参数获取到用户名: {}", username);
                            break;
                        }
                    }
                }
            }

            // 方法2：从SecurityUtils获取（在Token删除前）
            if ("未知用户".equals(username)) {
                try {
                    LoginUser loginUser = SecurityUtils.getLoginUser();
                    if (loginUser != null && StringUtils.isNotEmpty(loginUser.getUsername())) {
                        username = loginUser.getUsername();
                        log.info("通过SecurityUtils获取到退出用户: {}", username);
                    }
                } catch (Exception e) {
                    log.warn("SecurityUtils获取用户信息失败: {}", e.getMessage());
                }
            }

            // 获取请求信息
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    // 使用导入的IpUtils
                    ip = IpUtils.getIpAddr(request);
                    userAgent = request.getHeader("User-Agent");
                    log.info("退出切面获取到请求信息 - IP: {}, UserAgent: {}", ip, userAgent);

                    // 方法3：从Session获取（备用方案）
                    if ("未知用户".equals(username)) {
                        Object sessionUser = request.getSession().getAttribute("username");
                        if (sessionUser != null) {
                            username = sessionUser.toString();
                            log.info("从Session获取到用户名: {}", username);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("获取请求信息失败: {}", e.getMessage());
            }

        } catch (Exception e) {
            log.error("退出切面准备阶段异常: {}", e.getMessage(), e);
        }

        // 执行原方法（删除Token）
        Object result = joinPoint.proceed();

        try {
            // 记录退出日志
            if (!"未知用户".equals(username)) {
                LogoutLogFactory.recordLogoutInfo(username, ip, userAgent);
                log.info("成功记录用户 {} 的退出日志", username);
            } else {
                log.warn("无法获取用户名，跳过记录退出日志");
            }
        } catch (Exception e) {
            log.error("记录退出日志异常: {}", e.getMessage(), e);
        }

        return result;
    }

    /**
     * 根据Token获取用户名
     */
    private String getUsernameByToken(String token) {
        try {
            if (StringUtils.isEmpty(token)) {
                return "未知用户";
            }

            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 这里可以添加从Redis查询用户名的逻辑
            // 暂时返回未知用户，你可以根据实际情况实现
            log.debug("Token: {}", token.substring(0, Math.min(token.length(), 10)) + "...");
            return "未知用户";

        } catch (Exception e) {
            log.warn("根据Token获取用户名失败: {}", e.getMessage());
            return "未知用户";
        }
    }
}