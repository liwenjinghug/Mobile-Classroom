// ruoyi-admin/src/main/java/com/ruoyi/web/controller/proj_qhy/ForumImageController.java
package com.ruoyi.web.controller.proj_qhy;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// [修改] 使用自定义 Log 注解
import com.ruoyi.proj_cyq.annotation.Log;
import com.ruoyi.common.enums.BusinessType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 论坛图片上传Controller
 */
@RestController
@RequestMapping("/proj_qhy/forum/image")
public class ForumImageController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ForumImageController.class);

    /**
     * 上传图片到本地assets目录
     */
    @Log(title = "论坛图片上传", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    public AjaxResult uploadImage(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return AjaxResult.error("上传文件不能为空");
            }

            // 验证文件名和类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                return AjaxResult.error("文件名不合法");
            }
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
            if (!isImageFile(fileExtension)) {
                return AjaxResult.error("只支持上传图片文件");
            }

            // 验证文件大小 (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return AjaxResult.error("图片大小不能超过5MB");
            }

            // 生成唯一文件名
            String newFileName = generateFileName(originalFilename);

            // 优先使用 ruoyi.profile（由 RuoYiConfig.getProfile() 提供），若为空则按之前方式保存到 ruoyi-ui 目录
            String configuredProfile = RuoYiConfig.getProfile();
            String savePath = null;
            if (configuredProfile != null && !configuredProfile.trim().isEmpty()) {
                savePath = resolveProfilePath(configuredProfile);
            }
            if (savePath == null) {
                // fallback to project-relative path
                String projectRootPath = getProjectRootPath();
                savePath = projectRootPath + "ruoyi-ui/src/assets/xxsq_images/";
            }

            // 确保目录存在
            File destDir = new File(savePath);
            if (!destDir.exists()) {
                boolean ok = destDir.mkdirs();
                if (!ok) {
                    logger.warn("创建目录失败: {}", savePath);
                }
            }

            // 保存文件
            File destFile = new File(savePath + newFileName);
            file.transferTo(destFile);

            // 构建返回的URL路径 - 使用后端资源访问前缀 (Constants.RESOURCE_PREFIX)
            // ResourcesConfig 已将 Constants.RESOURCE_PREFIX/** 映射到 file: + RuoYiConfig.getProfile()
            String imageUrl = Constants.RESOURCE_PREFIX + "/" + newFileName;

            Map<String, Object> result = new HashMap<>();
            result.put("url", imageUrl);
            result.put("fileName", newFileName);
            result.put("originalName", originalFilename);

            return AjaxResult.success("上传成功", result);

        } catch (IOException e) {
            logger.error("图片上传失败:", e);
            return AjaxResult.error("图片上传失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("系统错误:", e);
            return AjaxResult.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 批量上传图片
     */
    @Log(title = "论坛图片批量上传", businessType = BusinessType.INSERT)
    @PostMapping("/uploadMultiple")
    public AjaxResult uploadMultipleImages(MultipartFile[] files) {
        try {
            if (files == null || files.length == 0) {
                return AjaxResult.error("请选择要上传的图片");
            }

            Map<String, Object> result = new HashMap<>();
            String[] successUrls = new String[files.length];
            int successCount = 0;

            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;

                try {
                    // 生成唯一文件名
                    String originalFilename = file.getOriginalFilename();
                    if (originalFilename == null || !originalFilename.contains(".")) {
                        logger.warn("跳过无效文件名的文件");
                        continue;
                    }
                    String newFileName = generateFileName(originalFilename);

                    // 优先使用 ruoyi.profile（由 RuoYiConfig.getProfile() 提供），若为空则按之前方式保存到 ruoyi-ui 目录
                    String configuredProfile = RuoYiConfig.getProfile();
                    String savePath = null;
                    if (configuredProfile != null && !configuredProfile.trim().isEmpty()) {
                        savePath = resolveProfilePath(configuredProfile);
                    }
                    if (savePath == null) {
                        // fallback to project-relative path
                        String projectRootPath = getProjectRootPath();
                        savePath = projectRootPath + "ruoyi-ui/src/assets/xxsq_images/";
                    }

                    // 确保目录存在
                    File destDir = new File(savePath);
                    if (!destDir.exists()) {
                        boolean ok = destDir.mkdirs();
                        if (!ok) {
                            logger.warn("创建目录失败: {}", savePath);
                        }
                    }

                    // 保存文件
                    File destFile = new File(savePath + newFileName);
                    file.transferTo(destFile);

                    // 构建返回的URL路径
                    String imageUrl = Constants.RESOURCE_PREFIX + "/" + newFileName;
                    successUrls[successCount] = imageUrl;
                    successCount++;

                } catch (Exception e) {
                    logger.error("文件上传失败: " + file.getOriginalFilename(), e);
                }
            }

            if (successCount == 0) {
                return AjaxResult.error("所有图片上传失败");
            }

            // 只返回成功上传的URL
            String[] finalUrls = new String[successCount];
            System.arraycopy(successUrls, 0, finalUrls, 0, successCount);

            result.put("urls", finalUrls);
            result.put("successCount", successCount);
            result.put("totalCount", files.length);

            return AjaxResult.success("上传完成", result);

        } catch (Exception e) {
            logger.error("批量上传失败:", e);
            return AjaxResult.error("批量上传失败: " + e.getMessage());
        }
    }

    /**
     * 验证是否为图片文件
     */
    private boolean isImageFile(String fileExtension) {
        String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};
        for (String ext : allowedExtensions) {
            if (ext.equals(fileExtension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String originalFilename) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return timeStamp + "_" + uuid + fileExtension;
    }

    /**
     * 获取项目根路径
     */
    private String getProjectRootPath() {
        // 尝试多种方式获取项目根路径
        String rootPath;

        try {
            // 方式1: 通过classpath获取
            rootPath = new File("").getAbsolutePath();

            // 如果是在开发环境中，可能需要调整路径
            if (rootPath.contains("ruoyi-admin")) {
                // 在admin模块中，需要返回到项目根目录
                rootPath = rootPath.substring(0, rootPath.lastIndexOf("ruoyi-admin")) + "Mobile-Classroom/";
            } else if (rootPath.contains("Mobile-Classroom")) {
                // 已经在项目根目录
                rootPath = rootPath + "/";
            } else {
                // 其他情况，使用相对路径
                rootPath = System.getProperty("user.dir") + "/";
            }

            // 确保路径正确
            File rootDir = new File(rootPath);
            if (!rootDir.exists()) {
                // 如果路径不存在，使用用户目录
                rootPath = System.getProperty("user.home") + "/Mobile-Classroom/";
            }

        } catch (Exception e) {
            // 如果所有方法都失败，使用用户目录
            rootPath = System.getProperty("user.home") + "/Mobile-Classroom/";
        }

        return rootPath;
    }

    /**
     * Resolve a configured profile path. If it's absolute, return as-is (ensure trailing separator).
     * If it's relative, try to resolve it by searching ancestor directories for an existing target,
     * or by locating a 'Mobile-Classroom' project root. Falls back to user.home/Mobile-Classroom/<configuredProfile>.
     */
    private String resolveProfilePath(String configuredProfile) {
        if (configuredProfile == null) return null;
        configuredProfile = configuredProfile.trim();
        File cfgFile = new File(configuredProfile);
        if (cfgFile.isAbsolute()) {
            String p = cfgFile.getAbsolutePath();
            if (!p.endsWith(File.separator)) p = p + File.separator;
            return p;
        }

        // It's a relative path. Walk up from current working dir to try to find a match.
        File dir = new File("").getAbsoluteFile();
        while (dir != null) {
            File candidate = new File(dir, configuredProfile);
            if (candidate.exists()) {
                String p = candidate.getAbsolutePath();
                if (!p.endsWith(File.separator)) p = p + File.separator;
                return p;
            }
            // if we find Mobile-Classroom ancestor, try resolving relative to it
            if ("Mobile-Classroom".equals(dir.getName())) {
                File candidate2 = new File(dir, configuredProfile);
                String p2 = candidate2.getAbsolutePath();
                if (!p2.endsWith(File.separator)) p2 = p2 + File.separator;
                return p2;
            }
            dir = dir.getParentFile();
        }

        // fallback: construct under user home
        String fallback = System.getProperty("user.home") + File.separator + "Mobile-Classroom" + File.separator + configuredProfile;
        File f = new File(fallback);
        String abs = f.getAbsolutePath();
        if (!abs.endsWith(File.separator)) abs = abs + File.separator;
        return abs;
    }
}