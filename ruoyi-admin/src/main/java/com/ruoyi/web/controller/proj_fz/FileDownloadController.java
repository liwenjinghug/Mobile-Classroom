package com.ruoyi.web.controller.proj_fz;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * 文件下载和上传Controller
 * 专门处理附件下载到服务器本地，以及保持原始文件名的上传
 *
 * @author proj_fz
 */
@RestController
@RequestMapping("/proj_fz/file")
public class FileDownloadController extends BaseController {

    /**
     * 上传文件并保持原始文件名（不添加时间戳）
     */
    @PostMapping("/uploadOriginal")
    public AjaxResult uploadOriginal(MultipartFile file, @RequestParam(value = "originalName", required = false) String originalName) {
        try {
            if (file == null || file.isEmpty()) {
                return AjaxResult.error("文件不能为空");
            }

            // 优先使用前端传递的原始文件名，如果没有则使用文件自带的名称
            String originalFilename = (originalName != null && !originalName.isEmpty())
                ? originalName
                : file.getOriginalFilename();

            if (originalFilename == null || originalFilename.isEmpty()) {
                return AjaxResult.error("文件名不能为空");
            }

            logger.info("上传文件，保持原始文件名: {} (前端传递: {}, 文件自带: {})",
                originalFilename, originalName, file.getOriginalFilename());

            // 构建保存路径：uploadPath/upload/
            String uploadPath = RuoYiConfig.getUploadPath();
            File uploadDir = new File(uploadPath, "upload");

            // 确保目录存在
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                logger.info("创建上传目录: {} - {}", uploadDir.getAbsolutePath(), created);
            }

            // 使用原始文件名保存（如果文件已存在则直接覆盖）
            File targetFile = new File(uploadDir, originalFilename);

            // 保存文件
            file.transferTo(targetFile);
            logger.info("文件保存成功: {}", targetFile.getAbsolutePath());

            // 构建返回数据
            java.util.HashMap<String, Object> fileInfo = new java.util.HashMap<>();
            fileInfo.put("originalFilename", originalFilename);  // 原始文件名
            fileInfo.put("fileName", "/upload/" + originalFilename);  // 相对路径
            fileInfo.put("url", "/upload/" + originalFilename);       // URL路径
            fileInfo.put("absolutePath", targetFile.getAbsolutePath());
            fileInfo.put("size", file.getSize());

            logger.info("上传结果 - 原始文件名: {}, URL: {}", originalFilename, "/upload/" + originalFilename);

            // 返回成功，数据在data字段中
            return AjaxResult.success("上传成功", fileInfo);

        } catch (Exception e) {
            logger.error("上传文件失败", e);
            return AjaxResult.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载附件到服务器uploadPath/upload目录（保持原始文件名）
     */
    @PostMapping("/download")
    public AjaxResult downloadToServer(@RequestBody DownloadRequest request) {
        try {
            String fileUrl = request.getFileUrl();
            String fileName = request.getFileName();

            if (fileUrl == null || fileUrl.isEmpty()) {
                return AjaxResult.error("文件URL不能为空");
            }

            // 如果没有提供文件名，从URL中提取并解码
            if (fileName == null || fileName.isEmpty()) {
                String[] urlParts = fileUrl.split("/");
                fileName = urlParts[urlParts.length - 1];
                // URL解码文件名
                try {
                    fileName = java.net.URLDecoder.decode(fileName, "UTF-8");
                } catch (Exception e) {
                    logger.warn("文件名URL解码失败: {}", fileName);
                }
            }

            logger.info("准备下载文件 - 原始URL: {}, 文件名: {}", fileUrl, fileName);

            // 对URL进行编码处理（支持中文）
            String fullUrl = fileUrl;
            if (!fileUrl.startsWith("http")) {
                fullUrl = "http://localhost:8080" + fileUrl;
            }

            // 对URL路径中的中文进行编码
            try {
                java.net.URI uri = new java.net.URI(fullUrl);
                // 重新构建URL，对路径部分进行编码
                String path = uri.getPath();
                String encodedPath = encodePath(path);
                fullUrl = uri.getScheme() + "://" + uri.getHost() +
                         (uri.getPort() != -1 ? ":" + uri.getPort() : "") + encodedPath;
                logger.info("编码后的URL: {}", fullUrl);
            } catch (Exception e) {
                logger.warn("URL编码失败，使用原始URL: {}", e.getMessage());
            }

            // 构建保存路径：uploadPath/upload/原始文件名
            String uploadPath = RuoYiConfig.getUploadPath();
            File uploadDir = new File(uploadPath, "upload");

            // 确保目录存在
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                logger.info("创建目录: {} - {}", uploadDir.getAbsolutePath(), created);
            }

            // 处理文件名重复：如果文件已存在，添加(1)(2)等序号
            File targetFile = new File(uploadDir, fileName);
            if (targetFile.exists()) {
                String nameWithoutExt = fileName;
                String ext = "";
                int lastDot = fileName.lastIndexOf('.');
                if (lastDot > 0) {
                    nameWithoutExt = fileName.substring(0, lastDot);
                    ext = fileName.substring(lastDot);
                }

                int counter = 1;
                while (targetFile.exists()) {
                    String newName = nameWithoutExt + "(" + counter + ")" + ext;
                    targetFile = new File(uploadDir, newName);
                    counter++;
                }
                fileName = targetFile.getName();
                logger.info("文件重名，使用新文件名: {}", fileName);
            }

            // 下载文件
            logger.info("开始下载 - URL: {}, 目标文件: {}", fullUrl, targetFile.getAbsolutePath());
            URL url = new URL(fullUrl);
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("Accept-Charset", "UTF-8");

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
            result.put("path", "uploadPath/upload/" + fileName);
            result.put("absolutePath", targetFile.getAbsolutePath());
            result.put("fileName", fileName);
            result.put("size", totalBytes);

            logger.info("返回结果: {}", result);
            return result;

        } catch (Exception e) {
            logger.error("下载文件失败", e);
            return AjaxResult.error("下载失败: " + e.getMessage());
        }
    }

    /**
     * 对URL路径进行编码，支持中文字符
     */
    private String encodePath(String path) {
        try {
            // 分割路径
            String[] parts = path.split("/");
            StringBuilder encoded = new StringBuilder();
            for (String part : parts) {
                if (!part.isEmpty()) {
                    // 对每个部分进行URL编码
                    encoded.append("/").append(java.net.URLEncoder.encode(part, "UTF-8").replace("+", "%20"));
                }
            }
            return encoded.length() > 0 ? encoded.toString() : "/";
        } catch (Exception e) {
            logger.error("路径编码失败", e);
            return path;
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

