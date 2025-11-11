package com.ruoyi.web.controller.proj_qhy;

import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/proj_qhy/file")
public class FileController {

    // 图片保存目录（直接写死绝对路径，确保正确）
    private static final String SAVE_DIR = "D:\\code\\java\\Mobile-Classroom\\ruoyi-ui\\src\\assets\\xxsq_images\\";

    @PostMapping("/uploadLocal")
    public AjaxResult uploadLocalImage(@RequestBody Map<String, String> params) {
        try {
            String base64 = params.get("base64");
            String fileName = params.get("fileName");

            // 校验参数
            if (base64 == null || fileName == null) {
                return AjaxResult.error("缺少参数");
            }

            // 处理base64（去掉前缀）
            if (base64.contains(",")) {
                base64 = base64.split(",")[1];
            }
            byte[] imageBytes = Base64.getDecoder().decode(base64);

            // 确保保存目录存在
            File dir = new File(SAVE_DIR);
            if (!dir.exists()) {
                dir.mkdirs(); // 创建目录
            }

            // 保存图片到指定目录
            String filePath = SAVE_DIR + fileName;
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(imageBytes);
            fos.close();

            // 返回文件名（只返回文件名，不含路径）
            return AjaxResult.success(fileName);

        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error("保存失败：" + e.getMessage());
        }
    }
}