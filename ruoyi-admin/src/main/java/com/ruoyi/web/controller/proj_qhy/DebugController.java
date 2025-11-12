package com.ruoyi.web.controller.proj_qhy;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 调试控制器：用于查看 ruoyi.profile 配置值与解析结果
 */
@RestController
@RequestMapping("/debug")
public class DebugController {

    @GetMapping("/profile")
    public AjaxResult profile() {
        String configured = RuoYiConfig.getProfile();
        Map<String, Object> data = new HashMap<>();
        data.put("configuredProfile", configured);

        String resolved = null;
        if (configured != null && !configured.trim().isEmpty()) {
            File cfg = new File(configured);
            if (cfg.isAbsolute()) {
                resolved = cfg.getAbsolutePath();
            } else {
                // 尝试解析相对路径
                File dir = new File("").getAbsoluteFile();
                File found = null;
                while (dir != null) {
                    File candidate = new File(dir, configured);
                    if (candidate.exists()) { found = candidate; break; }
                    if ("Mobile-Classroom".equals(dir.getName())) {
                        found = new File(dir, configured); break;
                    }
                    dir = dir.getParentFile();
                }
                if (found == null) found = new File(System.getProperty("user.dir"), configured);
                resolved = found.getAbsolutePath();
            }
        } else {
            resolved = new File(System.getProperty("user.dir"), "ruoyi-ui/src/assets/xxsq_images").getAbsolutePath();
        }
        data.put("resolvedAbsolutePath", resolved);
        data.put("resourcePrefix", Constants.RESOURCE_PREFIX);

        return AjaxResult.success(data);
    }
}

