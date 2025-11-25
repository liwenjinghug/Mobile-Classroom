package com.ruoyi.proj_cyq.service.impl;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.proj_cyq.domain.ClassNotice;
import com.ruoyi.proj_cyq.mapper.ClassNoticeMapper;
import com.ruoyi.proj_cyq.service.IClassNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Jsoup HTML解析
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

// Apache POI Word处理
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.util.Units;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class ClassNoticeServiceImpl implements IClassNoticeService {

    @Autowired
    private ClassNoticeMapper classNoticeMapper;

    // ... (保留原有的 select, insert, update, delete, stats 逻辑，不做变动) ...
    @Override
    public List<ClassNotice> selectClassNoticeList(ClassNotice notice) { return classNoticeMapper.selectClassNoticeList(notice); }
    @Override
    public ClassNotice selectClassNoticeById(Long noticeId) { return classNoticeMapper.selectClassNoticeById(noticeId); }
    @Override
    public int insertClassNotice(ClassNotice notice) {
        notice.setCreateBy(SecurityUtils.getUsername());
        return classNoticeMapper.insertClassNotice(notice);
    }
    @Override
    public int updateClassNotice(ClassNotice notice) {
        notice.setUpdateBy(SecurityUtils.getUsername());
        return classNoticeMapper.updateClassNotice(notice);
    }
    @Override
    public int deleteClassNoticeById(Long noticeId) {
        ClassNotice notice = new ClassNotice();
        notice.setNoticeId(noticeId);
        notice.setUpdateBy(SecurityUtils.getUsername());
        return classNoticeMapper.deleteClassNoticeById(notice);
    }
    @Override
    public int deleteClassNoticeByIds(Long[] noticeIds) {
        return classNoticeMapper.deleteClassNoticeByIds(noticeIds, SecurityUtils.getUsername());
    }
    @Override
    public Map<String, Object> getNoticeStats() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> creatorStats = classNoticeMapper.selectNoticeStatsByCreator();
        List<Map<String, Object>> dateStats = classNoticeMapper.selectNoticeStatsByDate();
        int totalCount = 0;
        for (Map<String, Object> item : creatorStats) {
            totalCount += Integer.parseInt(item.get("value").toString());
        }
        result.put("totalCount", totalCount);
        result.put("creatorStats", creatorStats);
        result.put("dateStats", dateStats);
        return result;
    }

    // ========== 【核心修改】Word 导出逻辑 (修复图片路径问题) ==========
    @Override
    public void exportNoticeWord(Long noticeId, OutputStream outputStream) {
        // 1. 获取通告
        ClassNotice notice = classNoticeMapper.selectClassNoticeById(noticeId);
        if (notice == null) {
            throw new RuntimeException("通告不存在");
        }

        // 2. 创建 Word 文档
        try (XWPFDocument document = new XWPFDocument()) {

            // --- 标题 ---
            XWPFParagraph titlePara = document.createParagraph();
            titlePara.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText(notice.getTitle());
            titleRun.setBold(true);
            titleRun.setFontSize(20);
            // 尝试设置中文字体，如果系统没有"黑体"可能会回退到默认
            titleRun.setFontFamily("SimHei");
            titleRun.addBreak();

            // --- 内容解析 (HTML -> Word) ---
            String htmlContent = notice.getContent();
            if (StringUtils.isNotEmpty(htmlContent)) {
                System.out.println(">>> 开始解析通告内容HTML...");
                Document doc = Jsoup.parse(htmlContent);
                Elements children = doc.body().children();

                if (children.isEmpty()) {
                    // 如果没有子标签，可能是一段纯文本
                    createParagraph(document, doc.body().text());
                } else {
                    for (Element element : children) {
                        processElementToWord(document, element);
                    }
                }
            }

            // --- 写入输出流 ---
            document.write(outputStream);
            System.out.println(">>> Word文档写入完成");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Word导出失败: " + e.getMessage());
        }
    }

    // 处理单个 HTML 元素到 Word 段落
    private void processElementToWord(XWPFDocument doc, Element element) {
        // 1. 图片处理
        if (element.tagName().equals("img") || !element.select("img").isEmpty()) {
            XWPFParagraph p = doc.createParagraph();
            p.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun r = p.createRun();

            // 提取所有图片
            Elements imgs = element.tagName().equals("img") ? new Elements(element) : element.select("img");

            for (Element img : imgs) {
                String src = img.attr("src");
                System.out.println(">>> 发现图片标签，src: " + src);

                // 【关键修复】智能匹配 /profile 路径
                // 无论 src 是 "/dev-api/profile/..." 还是 "http://localhost/profile/..."
                // 我们只取 "/profile" 及其后面的部分
                int profileIndex = src.indexOf("/profile");

                if (profileIndex != -1) {
                    // 截取有效路径：/profile/upload/2023/11/25/xxx.jpg
                    String cleanPath = src.substring(profileIndex);

                    // 拼接本地物理路径：D:/ruoyi/uploadPath + /upload/2023/11/25/xxx.jpg
                    // RuoYiConfig.getProfile() 返回的是 D:/ruoyi/uploadPath (不带 /profile 后缀)
                    // 所以我们要把 cleanPath 中的 "/profile" 替换掉
                    String localPath = RuoYiConfig.getProfile() + cleanPath.replace("/profile", "");

                    System.out.println(">>> 解析出的本地物理路径: " + localPath);

                    File imgFile = new File(localPath);
                    if (imgFile.exists()) {
                        try (InputStream is = new FileInputStream(imgFile)) {
                            // 简单的图片类型判断
                            int format = XWPFDocument.PICTURE_TYPE_JPEG;
                            if (localPath.toLowerCase().endsWith(".png")) {
                                format = XWPFDocument.PICTURE_TYPE_PNG;
                            } else if (localPath.toLowerCase().endsWith(".gif")) {
                                format = XWPFDocument.PICTURE_TYPE_GIF;
                            } else if (localPath.toLowerCase().endsWith(".bmp")) {
                                format = XWPFDocument.PICTURE_TYPE_BMP;
                            }

                            // 添加图片 (宽度设为 450px，高度自适应)
                            r.addPicture(is, format, imgFile.getName(), Units.toEMU(400), Units.toEMU(300));
                            System.out.println(">>> 图片嵌入成功");
                            r.addBreak();
                        } catch (Exception e) {
                            System.err.println(">>> 图片嵌入失败: " + e.getMessage());
                            r.setText("[图片插入失败: " + imgFile.getName() + "]");
                        }
                    } else {
                        System.err.println(">>> 错误：本地文件不存在! " + localPath);
                        r.setText("[图片文件丢失]");
                    }
                } else {
                    System.out.println(">>> 跳过非本地图片 (不包含 /profile)");
                }
            }

            // 如果段落里还有文字，也写进去
            String text = element.text();
            if (StringUtils.isNotEmpty(text)) {
                XWPFParagraph textP = doc.createParagraph();
                textP.createRun().setText("    " + text);
            }
        }
        // 2. 文本处理
        else {
            String text = element.text();
            if (StringUtils.isNotEmpty(text)) {
                createParagraph(doc, "    " + text); // 首行缩进模拟
            }
        }
    }

    // 辅助方法：创建纯文本段落
    private void createParagraph(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(text);
        // 尝试使用中文字体
        r.setFontFamily("SimSun"); // 宋体
        r.setFontSize(12);
    }
}