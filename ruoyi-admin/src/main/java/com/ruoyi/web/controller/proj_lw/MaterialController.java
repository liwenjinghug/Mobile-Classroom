package com.ruoyi.web.controller.proj_lw;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
// [修改] 使用自定义 Log 注解
import com.ruoyi.proj_cyq.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.proj_lw.domain.Material;
import com.ruoyi.proj_lw.service.IMaterialService;
/**
 * 资料Controller
 */
@RestController
@RequestMapping("/proj_lw/material")
public class MaterialController extends BaseController
{
    @Autowired
    private IMaterialService materialService;

    /**
     * 查询资料列表
     */
    @PreAuthorize("@ss.hasPermi('projlw:material:list')")
    @GetMapping("/list")
    public TableDataInfo list(Material material)
    {
        startPage();

        // 判断是否是学生角色
        boolean isStudent = SecurityUtils.getLoginUser().getUser().getRoles().stream()
                .anyMatch(role -> "student".equals(role.getRoleKey()));

        // 如果是学生，只能查看已推送的资料
        if (isStudent) {
            material.setPushStatus("1"); // 只查询已推送的资料
        }

        List<Material> list = materialService.selectMaterialList(material);
        return getDataTable(list);
    }

    /**
     * 获取资料详细信息
     */
    @PreAuthorize("@ss.hasPermi('projlw:material:query')")
    @GetMapping(value = "/{materialId}")
    public AjaxResult getInfo(@PathVariable("materialId") Long materialId)
    {
        return AjaxResult.success(materialService.selectMaterialByMaterialId(materialId));
    }

    /**
     * 新增资料
     */
    @PreAuthorize("@ss.hasPermi('projlw:material:add')")
    @Log(title = "资料管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Material material)
    {
        return toAjax(materialService.insertMaterial(material));
    }

    /**
     * 修改资料
     */
    @PreAuthorize("@ss.hasPermi('projlw:material:edit')")
    @Log(title = "资料管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Material material)
    {
        return toAjax(materialService.updateMaterial(material));
    }

    /**
     * 删除资料
     */
    @PreAuthorize("@ss.hasPermi('projlw:material:remove')")
    @Log(title = "资料管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{materialId}")
    public AjaxResult remove(@PathVariable Long materialId)
    {
        return toAjax(materialService.deleteMaterialByMaterialId(materialId));
    }

    /**
     * 推送资料
     */
    @PreAuthorize("@ss.hasPermi('projlw:material:push')")
    @Log(title = "资料推送", businessType = BusinessType.UPDATE)
    @PostMapping("/push/{materialId}")
    public AjaxResult pushMaterial(@PathVariable Long materialId)
    {
        return materialService.pushMaterialToStudents(materialId);
    }

    /**
     * 上传资料文件 - 修复重复路径版本
     */
    @PreAuthorize("@ss.hasPermi('projlw:material:upload')")
    @Log(title = "资料上传", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    public AjaxResult uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("=== 文件上传开始 ===");

            if (file.isEmpty()) {
                return AjaxResult.error("上传文件不能为空");
            }

            String uploadPath = RuoYiConfig.getUploadPath();
            System.out.println("上传根路径: " + uploadPath);

            // 生成日期目录结构
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            String datePath = sdf.format(new Date());

            // 创建日期目录
            File dateDir = new File(uploadPath + File.separator + datePath);
            if (!dateDir.exists()) {
                dateDir.mkdirs();
                System.out.println("创建日期目录: " + dateDir.getAbsolutePath());
            }

            // 生成唯一文件名
            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String baseName = originalFileName.substring(0, originalFileName.lastIndexOf("."));
            Random random = new Random();
            String newFileName = baseName +
                    "_" + System.currentTimeMillis() +
                    "A" + String.format("%03d", random.nextInt(1000)) +
                    fileExtension;

            // 保存文件到日期目录
            File destFile = new File(dateDir, newFileName);
            file.transferTo(destFile);

            // 保存相对路径：只保存日期路径，不包含 upload/
            // 因为 uploadPath 已经是 D:/ruoyi/uploadPath/upload 了
            String relativePath = datePath + "/" + newFileName;
            System.out.println("保存的相对路径: " + relativePath);
            System.out.println("文件实际位置: " + destFile.getAbsolutePath());
            System.out.println("文件存在: " + destFile.exists());
            System.out.println("文件大小: " + destFile.length());

            AjaxResult ajax = AjaxResult.success("上传成功");
            ajax.put("fileName", newFileName);
            ajax.put("url", relativePath);  // 这里只返回日期路径，不包含 upload/
            return ajax;

        } catch (Exception e) {
            System.out.println("上传失败: " + e.getMessage());
            e.printStackTrace();
            return AjaxResult.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 下载资料文件 - 修复版本（不需要权限控制）
     */
    @GetMapping("/download/{materialId}")
    public void downloadFile(@PathVariable("materialId") Long materialId, HttpServletResponse response) {
        try {
            System.out.println("=== 下载请求 ===");
            System.out.println("materialId: " + materialId);

            Material material = materialService.selectMaterialByMaterialId(materialId);

            if (material == null) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.getWriter().write("资料不存在");
                return;
            }

            System.out.println("资料名称: " + material.getMaterialName());
            System.out.println("数据库文件路径: " + material.getFilePath());

            String filePathInDb = material.getFilePath();
            if (filePathInDb == null) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.getWriter().write("文件路径为空");
                return;
            }

            // 构建完整路径
            String uploadPath = RuoYiConfig.getUploadPath(); // 这个已经是 D:/.../uploadPath/upload
            String fullPath = uploadPath + File.separator + filePathInDb;

            System.out.println("上传路径: " + uploadPath);
            System.out.println("完整文件路径: " + fullPath);

            File file = new File(fullPath);
            System.out.println("文件是否存在: " + file.exists());

            if (!file.exists()) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                response.getWriter().write("文件不存在: " + fullPath);
                return;
            }

            // 设置响应头
            String encodedFileName = java.net.URLEncoder.encode(material.getMaterialName(), "UTF-8")
                    .replaceAll("\\+", "%20");

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
            response.setHeader("Content-Length", String.valueOf(file.length()));

            // 写入文件流
            Files.copy(file.toPath(), response.getOutputStream());
            response.getOutputStream().flush();

            System.out.println("下载完成");

        } catch (Exception e) {
            System.out.println("下载异常: " + e.getMessage());
            e.printStackTrace();
            try {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                response.getWriter().write("下载失败：" + e.getMessage());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}