package com.ruoyi.web.controller.proj_fz;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * 文件下载Controller
 * 专门处理附件下载到服务器本地
 *
 * @author proj_fz
 */
@RestController
@RequestMapping("/proj_fz/file")
public class FileDownloadController extends BaseController {

    /**
     * 下载附件到服务器uploadPath/upload目录
     */
    @PostMapping("/download")
    public AjaxResult downloadToServer(@RequestBody DownloadRequest request) {
        try {
            String fileUrl = request.getFileUrl();
            String fileName = request.getFileName();

            if (fileUrl == null || fileUrl.isEmpty()) {
                return AjaxResult.error("文件URL不能为空");
            }

            // 如果是相对路径，转换为完整URL
            String fullUrl = fileUrl;
            if (!fileUrl.startsWith("http")) {
                // 获取服务器地址
                String serverUrl = "http://localhost:8080";
                fullUrl = serverUrl + fileUrl;
            }

            logger.info("开始下载文件: {} -> {}", fullUrl, fileName);

            // 构建保存路径：uploadPath/upload/年/月/日/文件名
            String uploadPath = RuoYiConfig.getUploadPath();
            java.time.LocalDate now = java.time.LocalDate.now();
            String datePath = String.format("%d/%02d/%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
            File dateDir = new File(uploadPath, datePath);

            // 确保目录存在
            if (!dateDir.exists()) {
                boolean created = dateDir.mkdirs();
                logger.info("创建目录: {} - {}", dateDir.getAbsolutePath(), created);
            }

            // 生成唯一文件名（避免重名）
            String uniqueFileName = fileName;
            if (fileName.contains(".")) {
                String nameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
                String ext = fileName.substring(fileName.lastIndexOf('.'));
                uniqueFileName = nameWithoutExt + "_" + System.currentTimeMillis() + ext;
            } else {
                uniqueFileName = fileName + "_" + System.currentTimeMillis();
            }

            File targetFile = new File(dateDir, uniqueFileName);

            // 下载文件
            logger.info("目标文件: {}", targetFile.getAbsolutePath());
            URL url = new URL(fullUrl);
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);

            long totalBytes = 0;
            try (InputStream in = conn.getInputStream();
                 FileOutputStream out = new FileOutputStream(targetFile)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }
            }

            logger.info("文件下载成功: {} ({} bytes)", targetFile.getAbsolutePath(), totalBytes);

            // 返回包含完整路径信息的结果
            AjaxResult result = AjaxResult.success("文件下载成功");
            result.put("path", "uploadPath/upload/" + datePath + "/" + uniqueFileName);
            result.put("absolutePath", targetFile.getAbsolutePath());
            result.put("fileName", uniqueFileName);
            result.put("size", totalBytes);
            result.put("relativePath", datePath + "/" + uniqueFileName);

            logger.info("返回结果: {}", result);
            return result;

        } catch (Exception e) {
            logger.error("下载文件失败", e);
            return AjaxResult.error("下载失败: " + e.getMessage());
        }
    }

    /**
     * 下载请求参数
     */
    public static class DownloadRequest {
        private String fileUrl;
        private String fileName;

        public String getFileUrl() {
            return fileUrl;
        }

        public void setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
    }
}

