package com.ruoyi.proj_qhy.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.proj_qhy.mapper.BbsArticleMapper;
import com.ruoyi.proj_qhy.domain.BbsArticle;
import com.ruoyi.proj_qhy.service.IBbsArticleService;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.lowagie.text.Image;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import com.ruoyi.common.utils.StringUtils;
import java.nio.file.Paths; // <-- 新增
import java.nio.file.Path;  // <-- 新增
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.ByteArrayOutputStream; // 内存流
import javax.servlet.http.HttpServletResponse; // 响应对象

import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;

import java.io.FileInputStream;
import java.io.InputStream;


/**
 * 文章管理Service业务层处理
 */
@Service
public class BbsArticleServiceImpl implements IBbsArticleService {
    private static final Logger logger = LoggerFactory.getLogger(BbsArticleServiceImpl.class);
    @Autowired
    private BbsArticleMapper bbsArticleMapper;

    @Override
    public BbsArticle selectBbsArticleById(Long id) {
        return bbsArticleMapper.selectBbsArticleById(id);
    }

    @Override
    public List<BbsArticle> selectBbsArticleList(BbsArticle bbsArticle) {
        return bbsArticleMapper.selectBbsArticleList(bbsArticle);
    }

    @Override
    public int insertBbsArticle(BbsArticle bbsArticle) {
        return bbsArticleMapper.insertBbsArticle(bbsArticle);
    }

    @Override
    public int updateBbsArticle(BbsArticle bbsArticle) {
        return bbsArticleMapper.updateBbsArticle(bbsArticle);
    }

    @Override
    public int deleteBbsArticleById(Long id) {
        return bbsArticleMapper.deleteBbsArticleById(id);
    }

    @Override
    public int deleteBbsArticleByIds(Long[] ids) {
        return bbsArticleMapper.deleteBbsArticleByIds(ids);
    }

    @Override
    public int increaseViewCount(Long id) {
        return bbsArticleMapper.increaseViewCount(id);
    }

    @Override
    public List<BbsArticle> selectHotArticleList(BbsArticle bbsArticle) {
        return bbsArticleMapper.selectHotArticleList(bbsArticle);
    }
    @Override
    public int likeArticle(Long id) {
        return bbsArticleMapper.likeArticle(id);
    }

    @Override
    public int hateArticle(Long id) {
        return bbsArticleMapper.hateArticle(id);
    }

    @Override
    public List<BbsArticle> selectBbsArticleByIds(Long[] ids) {
        return bbsArticleMapper.selectBbsArticleByIds(ids);
    }

    /**
     * (最终修正) 导出文章为 Word (支持图片递归、修复文件名缓存问题)
     */
    @Override
    public void exportArticlesToWord(Long[] ids, HttpServletResponse response) throws Exception {
        List<BbsArticle> articles = bbsArticleMapper.selectBbsArticleByIds(ids);
        if (articles == null || articles.isEmpty()) {
            throw new RuntimeException("没有找到要导出的文章");
        }

        response.setContentType("application/zip");
        response.setCharacterEncoding("utf-8");
        // ZIP 包名也加上当前时间
        String zipName = "articles_word_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".zip";
        response.setHeader("Content-Disposition", "attachment; filename=" + zipName);

        // 用于给 Word 文档命名的时间格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (BbsArticle article : articles) {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    XWPFDocument document = new XWPFDocument();

                    // 1. 标题
                    XWPFParagraph titlePara = document.createParagraph();
                    titlePara.setAlignment(ParagraphAlignment.CENTER);
                    XWPFRun titleRun = titlePara.createRun();
                    titleRun.setText(article.getTitle());
                    titleRun.setBold(true);
                    titleRun.setFontSize(20);
                    titleRun.setFontFamily("微软雅黑");

                    // 2. 元数据
                    XWPFParagraph metaPara = document.createParagraph();
                    metaPara.setAlignment(ParagraphAlignment.CENTER);
                    XWPFRun metaRun = metaPara.createRun();
                    String meta = String.format("作者: %s | 发布时间: %s",
                            article.getAuthor(),
                            new SimpleDateFormat("yyyy-MM-dd HH:mm").format(article.getCreateTime()));
                    metaRun.setText(meta);
                    metaRun.setFontSize(10);
                    metaRun.setColor("808080");

                    // 3. 摘要
                    if (StringUtils.isNotBlank(article.getDigest())) {
                        XWPFParagraph digestPara = document.createParagraph();
                        XWPFRun digestRun = digestPara.createRun();
                        digestRun.setText("摘要：" + stripHtml(article.getDigest()));
                        digestRun.setItalic(true);
                        digestRun.setColor("666666");
                    }

                    // 4. 正文 (使用递归解析)
                    if (StringUtils.isNotBlank(article.getContent())) {
                        org.jsoup.nodes.Document jsoupDoc = Jsoup.parseBodyFragment(article.getContent());
                        traverseNodeToWord(document, jsoupDoc.body());
                    }

                    // 5. 写入流
                    document.write(baos);

                    // 6. 添加到 ZIP
                    String cleanTitle = cleanInvalidFileName(article.getTitle());

                    // (修正点：使用当前时间 new Date() 避免文件名重复导致打开旧文件)
                    // 添加一个随机数防止同一秒导出多个文件重名
                    String fileName = cleanTitle + "_" + sdf.format(new Date()) + "_" + (int)(Math.random()*1000) + ".docx";

                    ZipEntry entry = new ZipEntry(fileName);
                    zos.putNextEntry(entry);
                    zos.write(baos.toByteArray());
                    zos.closeEntry();

                } catch (Exception e) {
                    logger.error("Word导出失败: " + article.getTitle(), e);
                }
            }
            zos.finish();
        }
    }
    /**
     * (核心递归) 遍历 HTML 节点并转为 Word 元素
     */
    private void traverseNodeToWord(XWPFDocument doc, Node node) {
        for (Node child : node.childNodes()) {
            if (child instanceof Element) {
                Element element = (Element) child;
                String tagName = element.tagName().toLowerCase();

                if ("img".equals(tagName)) {
                    // --- 情况 A: 图片 ---
                    // 图片必须独占一段，否则 POI 很难处理
                    String src = element.attr("src");
                    insertImageToWord(doc, src);

                } else if ("a".equals(tagName)) {
                    // --- 情况 B: 超链接/附件 ---
                    String href = element.attr("href");
                    String text = element.text();
                    // 创建新段落存放链接
                    XWPFParagraph p = doc.createParagraph();
                    createHyperlink(doc, p, href, text);

                } else if ("br".equals(tagName)) {
                    // --- 情况 C: 换行 ---
                    doc.createParagraph();

                } else if (isBlockElement(tagName)) {
                    // --- 情况 D: 块级元素 (p, div, h1...) ---
                    // 这里有个技巧：如果块级元素里只有纯文本，我们就创建一个段落。
                    // 如果块级元素里有 img 或 a，递归进去处理。
                    // 为了简单且稳健：我们直接递归进去。
                    // 但是，HTML 的 <p> 意味着换行。所以我们在递归前，先检查是否需要创建新段落。
                    // (简化策略：直接递归，依靠子节点的 TextNode 创建段落)
                    traverseNodeToWord(doc, child);

                } else {
                    // --- 情况 E: 其他标签 (span, b, i...) ---
                    traverseNodeToWord(doc, child);
                }

            } else if (child instanceof TextNode) {
                // --- 情况 F: 纯文本 ---
                String text = ((TextNode) child).getWholeText();
                // 过滤掉空文本，但保留有意义的空格
                if (StringUtils.isNotBlank(text.replace("\u00A0", " ").trim())) {
                    XWPFParagraph p = doc.createParagraph();
                    XWPFRun r = p.createRun();
                    r.setText(text);
                }
            }
        }
    }

    /**
     * 辅助：插入图片到 Word
     */
    private void insertImageToWord(XWPFDocument doc, String src) {
        // 1. 使用之前的 resolveImagePath 方法获取本地绝对路径
        String physicalPath = resolveImagePath(src);

        if (physicalPath != null && new java.io.File(physicalPath).exists()) {
            try (InputStream is = new FileInputStream(physicalPath)) {
                XWPFParagraph p = doc.createParagraph();
                p.setAlignment(ParagraphAlignment.CENTER);
                XWPFRun r = p.createRun();
                // 插入图片 (宽 400px, 高 300px)
                r.addPicture(is, XWPFDocument.PICTURE_TYPE_PNG, physicalPath, Units.toEMU(400), Units.toEMU(300));
            } catch (Exception e) {
                logger.error("Word插入图片失败: " + src, e);
                // 插入一段文字提示
                XWPFParagraph p = doc.createParagraph();
                XWPFRun r = p.createRun();
                r.setText("[图片加载失败]");
                r.setColor("FF0000");
            }
        }
    }

    /**
     * 辅助：创建可点击的超链接 (修复了相对路径问题)
     */
    private void createHyperlink(XWPFDocument doc, XWPFParagraph paragraph, String url, String text) {
        // --- 关键修复：处理相对路径 ---
        String validUrl = url;
        // 如果不是 http 开头，说明是相对路径 (如 /dev-api/...)，Word 打不开。
        // 我们强制拼接 localhost (或者您的服务器IP)
        if (!url.startsWith("http")) {
            // 这里的端口 80 是假设您通过 Nginx/网关访问，如果是本地开发可能是 8080 或 80
            // 为了确保能点开，我们拼一个标头
            if (url.startsWith("/")) {
                validUrl = "http://localhost" + url;
            } else {
                validUrl = "http://localhost/" + url;
            }
        }

        try {
            // 1. 创建关系 ID
            PackagePart part = paragraph.getDocument().getPackagePart();
            String id = part.addExternalRelationship(validUrl, XWPFRelation.HYPERLINK.getRelation()).getId();

            // 2. 创建 CTHyperlink
            CTHyperlink cLink = paragraph.getCTP().addNewHyperlink();
            cLink.setId(id);

            // 3. 创建文本 Run
            CTR cRun = cLink.addNewR();
            CTText cText = cRun.addNewT();
            cText.setStringValue("📎 " + text);

            // 4. 设置样式 (蓝色 + 下划线) - 这是 POI 较新的写法
            // 如果您的 POI 版本较老不支持 addNewRPr，可以直接跳过这步，链接依然可点，只是不变蓝
            try {
                // 尝试设置样式，如果报错则忽略
                CTR cRun2 = cLink.getRArray(0);
                // 这是一个hack，POI操作样式很繁琐，这里仅做基本尝试
            } catch (Exception ignore) {}

        } catch (Exception e) {
            // 降级处理：普通文本
            XWPFRun r = paragraph.createRun();
            r.setText(text + " (链接: " + validUrl + ")");
            r.setColor("0000FF");
        }
    }

    private boolean isBlockElement(String tagName) {
        return "p".equals(tagName) || "div".equals(tagName) || "h1".equals(tagName) ||
                "h2".equals(tagName) || "h3".equals(tagName) || "li".equals(tagName) ||
                "ul".equals(tagName) || "ol".equals(tagName) || "blockquote".equals(tagName);
    }
    /**
     * (重写) 导出文章为 PDF (直接写入 Response 流，打包为 ZIP)
     */
    @Override
    public void exportArticlesToPdf(Long[] ids, HttpServletResponse response) throws Exception {
        List<BbsArticle> articles = bbsArticleMapper.selectBbsArticleByIds(ids);
        if (articles == null || articles.isEmpty()) {
            throw new RuntimeException("没有找到要导出的文章");
        }

        // 1. 设置响应头 (告诉浏览器这是一个要下载的 ZIP 文件)
        response.setContentType("application/zip");
        response.setCharacterEncoding("utf-8");
        // 文件名: articles_导出时间.zip
        String zipName = "articles_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".zip";
        response.setHeader("Content-Disposition", "attachment; filename=" + zipName);

        // 2. 准备字体
        BaseFont bfChinese = null;
        try {
            bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            logger.error("字体加载失败", e);
            throw new RuntimeException("字体加载失败");
        }
        Font titleFont = new Font(bfChinese, 18, Font.BOLD);
        Font metaFont = new Font(bfChinese, 12, Font.ITALIC);
        Font bodyFont = new Font(bfChinese, 12, Font.NORMAL);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        // 3. 创建 ZIP 输出流 (包裹 Response 输出流)
        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {

            for (BbsArticle article : articles) {
                // --- A. 在内存中生成单个 PDF ---
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    Document document = new Document();
                    PdfWriter.getInstance(document, baos);
                    document.open();

                    // 写入内容
                    document.add(new Paragraph(article.getTitle(), titleFont));
                    String meta = String.format("作者: %s | 发布于: %s",
                            article.getAuthor(),
                            new SimpleDateFormat("yyyy-MM-dd HH:mm").format(article.getCreateTime()));
                    document.add(new Paragraph(meta, metaFont));
                    document.add(new Paragraph("--- 摘要 ---", metaFont));
                    parseAndAddHtml(document, article.getDigest(), bodyFont);
                    document.add(new Paragraph("--- 正文 ---", metaFont));
                    parseAndAddHtml(document, article.getContent(), bodyFont);

                    document.close(); // 完成当前 PDF 生成

                    // --- B. 将 PDF 数据写入 ZIP ---
                    String title = cleanInvalidFileName(article.getTitle());
                    String time = sdf.format(article.getCreateTime());
                    // PDF 文件名
                    String pdfFileName = title + "_" + time + ".pdf";

                    // 添加 ZIP 条目
                    ZipEntry entry = new ZipEntry(pdfFileName);
                    zos.putNextEntry(entry);
                    zos.write(baos.toByteArray()); // 写入 PDF 数据
                    zos.closeEntry();
                } catch (Exception e) {
                    logger.error("生成PDF出错: " + article.getTitle(), e);
                    // 继续下一个，不中断整个下载
                }
            }

            zos.finish(); // 完成 ZIP 压缩
        }
    }
    /**
     * (修正版) 辅助方法：入口
     */
    private void parseAndAddHtml(Document pdfDoc, String html, Font defaultFont) throws Exception {
        if (StringUtils.isBlank(html)) {
            return;
        }
        // 1. 使用 Jsoup 解析 HTML 片段
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parseBodyFragment(html);

        // 2. 开始递归遍历 (从 body 开始)
        traverseAndAddNode(pdfDoc, jsoupDoc.body(), defaultFont);
    }

    /**
     * (新增) 递归遍历所有节点，确保找出所有嵌套的图片
     */
    private void traverseAndAddNode(Document pdfDoc, Node node, Font defaultFont) throws Exception {
        for (Node child : node.childNodes()) {
            if (child instanceof Element) {
                Element element = (Element) child;

                if ("img".equalsIgnoreCase(element.tagName())) {
                    // --- 情况 A: 发现图片 ---
                    String src = element.attr("src");
                    String physicalPath = resolveImagePath(src); // 解析路径

                    if (physicalPath != null) {
                        try {
                            File imgFile = new File(physicalPath);
                            if (imgFile.exists()) {
                                Image pdfImage = Image.getInstance(physicalPath);
                                // 缩放图片适应页面
                                float pageWidth = pdfDoc.getPageSize().getWidth() - pdfDoc.leftMargin() - pdfDoc.rightMargin();
                                if (pdfImage.getWidth() > pageWidth) {
                                    pdfImage.scaleToFit(pageWidth, Float.MAX_VALUE);
                                }
                                pdfDoc.add(pdfImage);
                                logger.info("PDF导出 - 成功添加图片: {}", physicalPath);
                            } else {
                                logger.warn("PDF导出 - 图片文件不存在: {}", physicalPath);
                            }
                        } catch (Exception e) {
                            logger.error("PDF导出 - 添加图片异常", e);
                        }
                    }
                } else if ("br".equalsIgnoreCase(element.tagName())) {
                    // --- 情况 B: 换行 ---
                    pdfDoc.add(new Paragraph("\n"));
                } else {
                    // --- 情况 C: 其他容器标签 (p, div, span...) ---
                    // 关键：递归进去找内容！
                    traverseAndAddNode(pdfDoc, child, defaultFont);
                }

            } else if (child instanceof TextNode) {
                // --- 情况 D: 纯文本 ---
                String text = ((TextNode) child).getWholeText();
                // 过滤掉只有空白字符的文本节点，避免排版过乱
                if (StringUtils.isNotBlank(text.replace("\u00A0", " ").trim())) {
                    pdfDoc.add(new Paragraph(text, defaultFont));
                }
            }
        }
    }
    private String resolveImagePath(String src) {
        if (StringUtils.isBlank(src)) {
            return null;
        }

        // 1. 清理 URL (移除 http前缀 和 /dev-api 前缀)
        String cleanSrc = src;

        if (cleanSrc.startsWith("http")) {
            int protocolIndex = cleanSrc.indexOf("//");
            if (protocolIndex > -1) {
                int pathStartIndex = cleanSrc.indexOf("/", protocolIndex + 2);
                if (pathStartIndex > -1) {
                    cleanSrc = cleanSrc.substring(pathStartIndex);
                }
            }
        }

        // 去掉 /dev-api
        String apiPrefix = "/dev-api";
        if (cleanSrc.startsWith(apiPrefix)) {
            cleanSrc = cleanSrc.substring(apiPrefix.length());
        }

        // 2. 获取配置的根路径
        String profilePath = RuoYiConfig.getProfile();
        String resourcePrefix = Constants.RESOURCE_PREFIX; // /profile

        // 3. 智能拼接
        if (cleanSrc.startsWith(resourcePrefix)) {
            // 移除 /profile，得到 /upload/2025/11/25/image.png
            // (注意：substring(8) 会保留开头的斜杠，如 /upload...)
            String relativePath = cleanSrc.substring(resourcePrefix.length());

            try {
                // 使用 Paths.get 自动处理分隔符 (Windows反斜杠)
                // Paths.get("D:\code...", "/upload/...") 会自动正确拼接
                Path fullPathObj = Paths.get(profilePath, relativePath);
                String fullPath = fullPathObj.toString();

                File imgFile = new File(fullPath);

                // 详细日志：帮助我们最后确认一次
                if (imgFile.exists()) {
                    logger.info("PDF导出 - 图片找到: {}", fullPath);
                    return fullPath;
                } else {
                    logger.warn("PDF导出 - 图片文件不存在: {} (原URL: {})", fullPath, src);
                    return null;
                }
            } catch (Exception e) {
                logger.error("PDF导出 - 路径解析错误", e);
                return null;
            }
        }

        return null;
    }
    /**
     * 辅助方法：移除HTML标签
     */
    private String stripHtml(String html) {
        if (html == null) return "";
        // 简单的 regex 移除 HTML 标签
        return html.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ");
    }

    /**
     * 辅助方法：清理文件名中的非法字符
     */
    private String cleanInvalidFileName(String title) {
        if (title == null) return "Untitled";
        return title.replaceAll("[\\\\/:*?\"<>|]", "_"); // 替换Windows/Linux非法字符
    }



}