package com.ruoyi.framework.config;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.File;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.framework.interceptor.RepeatSubmitInterceptor;

/**
 * 通用配置
 * 
 * @author ruoyi
 */
@Configuration
public class ResourcesConfig implements WebMvcConfigurer
{
    @Autowired
    private RepeatSubmitInterceptor repeatSubmitInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        /** 本地文件上传路径（解析相对路径为绝对路径，保证多开发者环境下访问不出现 404） */
        String configuredProfile = RuoYiConfig.getProfile();
        String resourceLocation;
        if (configuredProfile != null && !configuredProfile.trim().isEmpty()) {
            File cfg = new File(configuredProfile);
            try {
                if (cfg.isAbsolute()) {
                    String abs = cfg.getAbsolutePath();
                    if (!abs.endsWith(File.separator)) abs = abs + File.separator;
                    resourceLocation = "file:" + abs;
                } else {
                    // 相对路径：尝试从当前工作目录向上查找，或基于 user.dir 构建路径
                    File dir = new File(""
                            ).getAbsoluteFile();
                    File resolved = null;
                    while (dir != null) {
                        File candidate = new File(dir, configuredProfile);
                        if (candidate.exists()) {
                            resolved = candidate;
                            break;
                        }
                        if ("Mobile-Classroom".equals(dir.getName())) {
                            File candidate2 = new File(dir, configuredProfile);
                            resolved = candidate2;
                            break;
                        }
                        dir = dir.getParentFile();
                    }
                    if (resolved == null) {
                        resolved = new File(System.getProperty("user.dir"), configuredProfile);
                    }
                    String abs = resolved.getAbsolutePath();
                    if (!abs.endsWith(File.separator)) abs = abs + File.separator;
                    resourceLocation = "file:" + abs;
                }
            } catch (Exception e) {
                // 回退到 user.dir 下的默认目录
                String abs = System.getProperty("user.dir") + File.separator + configuredProfile;
                if (!abs.endsWith(File.separator)) abs = abs + File.separator;
                resourceLocation = "file:" + abs;
            }
        } else {
            // 未配置 profile 时，使用默认的 repo 相对路径
            String abs = System.getProperty("user.dir") + File.separator + "ruoyi-ui" + File.separator + "src" + File.separator + "assets" + File.separator + "xxsq_images" + File.separator;
            resourceLocation = "file:" + abs;
        }

        registry.addResourceHandler(Constants.RESOURCE_PREFIX + "/**")
                .addResourceLocations(resourceLocation);

        /** swagger配置 */
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                        .setCacheControl(CacheControl.maxAge(5, TimeUnit.HOURS).cachePublic());
    }

    /**
     * 自定义拦截规则
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");
    }

    /**
     * 跨域配置
     */
    @Bean
    public CorsFilter corsFilter()
    {
        CorsConfiguration config = new CorsConfiguration();
        // 设置访问源地址
        config.addAllowedOriginPattern("*");
        // 设置访问源请求头
        config.addAllowedHeader("*");
        // 设置访问源请求方法
        config.addAllowedMethod("*");
        // 有效期 1800秒
        config.setMaxAge(1800L);
        // 添加映射路径，拦截一切请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        // 返回新的CorsFilter
        return new CorsFilter(source);
    }
}