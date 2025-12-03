package com.ruoyi.proj_qhy.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.proj_qhy.domain.BbsArticle;
import com.ruoyi.proj_qhy.mapper.BbsArticleMapper;
import com.ruoyi.proj_qhy.service.IBbsArticleService;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

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
        BbsArticle article = bbsArticleMapper.selectBbsArticleById(id);
        if (article != null && StringUtils.isNotBlank(article.getContent())) {
            try {
                org.jsoup.nodes.Document doc = Jsoup.parseBodyFragment(article.getContent());
                // 修复：前端点击图片跳转问题 (剥离 a 标签)
                for (Element a : doc.select("a:has(img)")) {
                    a.replaceWith(a.select("img").first());
                }
                article.setContent(doc.body().html());
            } catch (Exception e) {
                logger.error("解析文章HTML失败", e);
            }
        }
        return article;
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

    // =================================== PDF 导出逻辑 ========================================
    @Override
    public void exportArticlesToPdf(Long[] ids, HttpServletResponse response) throws Exception {
        List<BbsArticle> articles = bbsArticleMapper.selectBbsArticleByIds(ids);
        if (articles == null || articles.isEmpty()) {
            throw new RuntimeException("没有找到要导出的文章");
        }

        response.setContentType("application/zip");
        response.setCharacterEncoding("utf-8");
        String zipName = "articles_pdf_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".zip";
        response.setHeader("Content-Disposition", "attachment; filename=" + zipName);

        BaseFont bfChinese = null;
        try {
            bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            logger.error("字体加载失败", e);
            throw new RuntimeException("PDF字体加载失败");
        }
        Font titleFont = new Font(bfChinese, 18, Font.BOLD);
        Font metaFont = new Font(bfChinese, 12, Font.ITALIC);
        Font bodyFont = new Font(bfChinese, 12, Font.NORMAL);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (BbsArticle article : articles) {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    Document document = new Document();
                    PdfWriter.getInstance(document, baos);
                    document.open();

                    document.add(new Paragraph(article.getTitle(), titleFont));
                    String meta = String.format("作者: %s | 类型: %s | 发布于: %s",
                            article.getAuthor(), article.getArticleType(),
                            new SimpleDateFormat("yyyy-MM-dd HH:mm").format(article.getCreateTime()));
                    document.add(new Paragraph(meta, metaFont));
                    document.add(new Paragraph("--- 摘要 ---", metaFont));
                    parseAndAddHtmlPdf(document, article.getDigest(), bodyFont);
                    document.add(new Paragraph("--- 正文 ---", metaFont));
                    parseAndAddHtmlPdf(document, article.getContent(), bodyFont);

                    document.close();

                    String title = cleanInvalidFileName(article.getTitle());
                    String fileName = title + "_" + sdf.format(new Date()) + "_" + (int) (Math.random() * 1000) + ".pdf";

                    ZipEntry entry = new ZipEntry(fileName);
                    zos.putNextEntry(entry);
                    zos.write(baos.toByteArray());
                    zos.closeEntry();

                } catch (Exception e) {
                    logger.error("生成PDF文件出错: " + article.getTitle(), e);
                }
            }
            zos.finish();
        }
    }

    private void parseAndAddHtmlPdf(Document pdfDoc, String html, Font defaultFont) throws Exception {
        if (StringUtils.isBlank(html)) return;
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parseBodyFragment(html);
        traverseAndAddNodePdf(pdfDoc, jsoupDoc.body(), defaultFont);
    }

    private void traverseAndAddNodePdf(Document pdfDoc, Node node, Font defaultFont) throws Exception {
        for (Node child : node.childNodes()) {
            if (child instanceof Element) {
                Element element = (Element) child;
                if ("img".equalsIgnoreCase(element.tagName())) {
                    String src = element.attr("src");
                    String physicalPath = resolveImagePath(src);
                    if (physicalPath != null && new File(physicalPath).exists()) {
                        try {
                            Image pdfImage = Image.getInstance(physicalPath);
                            float pageWidth = pdfDoc.getPageSize().getWidth() - pdfDoc.leftMargin() - pdfDoc.rightMargin();
                            if (pdfImage.getWidth() > pageWidth) {
                                pdfImage.scaleToFit(pageWidth, Float.MAX_VALUE);
                            }
                            pdfDoc.add(pdfImage);
                        } catch (Exception e) {
                            logger.error("PDF添加图片异常", e);
                        }
                    }
                } else if ("br".equalsIgnoreCase(element.tagName())) {
                    pdfDoc.add(new Paragraph("\n"));
                } else {
                    traverseAndAddNodePdf(pdfDoc, child, defaultFont);
                }
            } else if (child instanceof TextNode) {
                String text = ((TextNode) child).getWholeText();
                if (StringUtils.isNotBlank(text.replace("\u00A0", " ").trim())) {
                    pdfDoc.add(new Paragraph(text, defaultFont));
                }
            }
        }
    }

    // =================================== WORD 导出逻辑 =======================================

    private static class StyleContext {
        boolean bold = false;
        boolean italic = false;
        boolean underline = false;
        String color = null;       // Hex color
        String backgroundColor = null; // Hex color
        String fontFamily = null;
        int fontSize = 12;
        boolean isCodeBlock = false;

        public StyleContext copy() {
            StyleContext n = new StyleContext();
            n.bold = this.bold;
            n.italic = this.italic;
            n.underline = this.underline;
            n.color = this.color;
            n.backgroundColor = this.backgroundColor;
            n.fontFamily = this.fontFamily;
            n.fontSize = this.fontSize;
            n.isCodeBlock = this.isCodeBlock;
            return n;
        }
    }

    @Override
    public void exportArticlesToWord(Long[] ids, HttpServletResponse response) throws Exception {
        List<BbsArticle> articles = bbsArticleMapper.selectBbsArticleByIds(ids);
        if (articles == null || articles.isEmpty()) {
            throw new RuntimeException("没有找到要导出的文章");
        }

        response.setContentType("application/zip");
        response.setCharacterEncoding("utf-8");
        String zipName = "articles_word_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".zip";
        response.setHeader("Content-Disposition", "attachment; filename=" + zipName);
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
                    titleRun.setFontSize(22);
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
                        digestPara.setBorderLeft(Borders.SINGLE);
                        digestPara.setIndentationLeft(300);
                        XWPFRun digestRun = digestPara.createRun();
                        digestRun.setText("摘要：" + stripHtml(article.getDigest()));
                        digestRun.setItalic(true);
                        digestRun.setColor("666666");
                        digestRun.setFontSize(11);
                    }

                    // 4. 正文
                    if (StringUtils.isNotBlank(article.getContent())) {
                        org.jsoup.nodes.Document jsoupDoc = Jsoup.parseBodyFragment(article.getContent());
                        traverseHtmlNodes(document, jsoupDoc.body());
                    }

                    document.write(baos);

                    String cleanTitle = cleanInvalidFileName(article.getTitle());
                    String fileName = cleanTitle + "_" + sdf.format(new Date()) + "_" + (int) (Math.random() * 1000) + ".docx";
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
     * (主递归方法) 遍历 HTML 节点
     */
    private void traverseHtmlNodes(XWPFDocument doc, Node node) {
        XWPFParagraph currentParagraph = null;

        for (Node child : node.childNodes()) {
            if (child instanceof Element) {
                Element element = (Element) child;
                String tagName = element.tagName().toLowerCase();

                // --- 新增：处理 Quill 代码块容器 (修复换行问题关键点) ---
                if (element.hasClass("ql-code-block-container")) {
                    currentParagraph = null;
                    // 容器本身不是代码行，而是包裹着代码行，直接遍历其子元素
                    for (Element codeLine : element.children()) {
                        processCodeBlock(doc, codeLine);
                    }
                    continue;
                }

                // --- A. 块级元素 ---
                // 处理列表
                if ("ul".equals(tagName) || "ol".equals(tagName)) {
                    currentParagraph = null;
                    processList(doc, element, "ol".equals(tagName));
                    continue;
                }
                // 处理图片 (独占一行)
                if ("img".equals(tagName)) {
                    currentParagraph = null;
                    XWPFParagraph p = doc.createParagraph();
                    p.setAlignment(ParagraphAlignment.CENTER);
                    insertImageToWord(doc, p, element.attr("src"));
                    continue;
                }
                // 处理代码块 (Quill: .ql-code-block，如果未使用container包裹的备用逻辑)
                if (element.hasClass("ql-code-block") || "pre".equals(tagName)) {
                    currentParagraph = null;
                    processCodeBlock(doc, element);
                    continue;
                }
                // 处理其他块级元素
                if (isBlockElement(tagName)) {
                    currentParagraph = null;
                    processBlockNode(doc, element);
                    continue;
                }

                // --- B. 行内元素 (br, a, span...) ---
                if (currentParagraph == null) {
                    currentParagraph = doc.createParagraph();
                }

                if ("br".equals(tagName)) {
                    currentParagraph.createRun().addBreak();
                } else if ("a".equals(tagName)) {
                    if (!element.select("img").isEmpty()) {
                        // 链接里包含图片 -> 视为图片处理
                        currentParagraph = null;
                        traverseHtmlNodes(doc, element);
                    } else {
                        processInlineNode(doc, currentParagraph, element, new StyleContext());
                    }
                } else {
                    // 其他行内元素
                    processInlineNode(doc, currentParagraph, element, new StyleContext());
                }

            } else if (child instanceof TextNode) {
                // --- C. 纯文本 ---
                String text = ((TextNode) child).getWholeText();
                // 忽略块级元素间的纯空白，但不忽略段落内的空格
                if (currentParagraph == null && StringUtils.isBlank(text.replace("\u00A0", " ").trim())) {
                    continue;
                }

                if (currentParagraph == null) {
                    currentParagraph = doc.createParagraph();
                }

                XWPFRun run = currentParagraph.createRun();
                run.setText(text);
            }
        }
    }

    /**
     * 处理普通段落
     */
    private void processBlockNode(XWPFDocument doc, Element block) {
        XWPFParagraph p = doc.createParagraph();
        StyleContext context = new StyleContext();
        String tagName = block.tagName().toLowerCase();

        // 对齐
        if (block.hasClass("ql-align-center")) p.setAlignment(ParagraphAlignment.CENTER);
        else if (block.hasClass("ql-align-right")) p.setAlignment(ParagraphAlignment.RIGHT);
        else if (block.hasClass("ql-align-justify")) p.setAlignment(ParagraphAlignment.BOTH);

        // 标题
        if (tagName.startsWith("h")) {
            context.bold = true;
            if ("h1".equals(tagName)) context.fontSize = 24;
            else if ("h2".equals(tagName)) context.fontSize = 18;
            else if ("h3".equals(tagName)) context.fontSize = 16;
            else context.fontSize = 14;
        }

        // 引用
        if ("blockquote".equals(tagName)) {
            p.setBorderLeft(Borders.SINGLE);
            p.setIndentationLeft(300);
            context.color = "666666";
            context.italic = true;
        }

        processInlineNode(doc, p, block, context);
    }

    /**
     * (智能代码块) 自动合并连续的代码行，设置背景色
     */
    /**
     * (智能代码块) 自动合并连续的代码行，设置背景色
     */
    private void processCodeBlock(XWPFDocument doc, Element block) {
        XWPFParagraph p = doc.createParagraph();

        // 1. 设置灰色背景 (F6F8FA)
        if (!p.getCTP().isSetPPr()) p.getCTP().addNewPPr();
        CTPPr ppr = p.getCTP().getPPr();
        if (!ppr.isSetShd()) ppr.addNewShd();
        CTShd shd = ppr.getShd();
        shd.setVal(STShd.CLEAR);
        shd.setColor("auto");
        shd.setFill("F6F8FA");

        // 2. 智能边框：判断上下兄弟节点
        Element prev = block.previousElementSibling();
        Element next = block.nextElementSibling();
        boolean isPrevCode = prev != null && (prev.hasClass("ql-code-block") || "pre".equals(prev.tagName()));
        boolean isNextCode = next != null && (next.hasClass("ql-code-block") || "pre".equals(next.tagName()));

        // 第一行才加顶边框，最后一行才加底边框，实现“合并”效果
        if (!isPrevCode) p.setBorderTop(Borders.SINGLE);
        if (!isNextCode) p.setBorderBottom(Borders.SINGLE);
        p.setBorderLeft(Borders.SINGLE);
        p.setBorderRight(Borders.SINGLE);

        // 3. 紧凑排列，无段落间距
        p.setSpacingBefore(0);
        p.setSpacingAfter(0);

        // 【删除】下面这行引发了 Crash，且非必须，直接注释或删除即可
        // p.setWordWrapped(false);

        StyleContext context = new StyleContext();
        context.fontFamily = "Consolas"; // 等宽字体
        context.fontSize = 9;
        context.isCodeBlock = true;

        // 直接写入内容
        XWPFRun run = p.createRun();
        run.setText(block.text());
        applyStyle(run, context);
    }

    /**
     * (修复) 列表处理，手动添加序号
     */
    private void processList(XWPFDocument doc, Element listElement, boolean isOrdered) {
        int index = 1;
        for (Element li : listElement.children()) {
            if (!"li".equals(li.tagName().toLowerCase())) continue;

            XWPFParagraph p = doc.createParagraph();
            p.setIndentationLeft(300);
            p.setIndentationHanging(300); // 悬挂缩进

            XWPFRun markerRun = p.createRun();
            if (isOrdered) {
                markerRun.setText(index++ + ". ");
            } else {
                markerRun.setText("• ");
            }
            markerRun.setBold(true);

            processInlineNode(doc, p, li, new StyleContext());
        }
    }

    /**
     * 递归行内元素
     */
    /**
     * 递归行内元素 (已修复：增加 font-weight 样式解析，解决部分加粗不显示问题)
     */
    private void processInlineNode(XWPFDocument doc, XWPFParagraph p, Node node, StyleContext currentStyle) {
        for (Node child : node.childNodes()) {
            StyleContext nextStyle = currentStyle.copy();

            if (child instanceof Element) {
                Element el = (Element) child;
                String tag = el.tagName().toLowerCase();

                // 1. 标签解析
                if ("strong".equals(tag) || "b".equals(tag)) nextStyle.bold = true;
                if ("em".equals(tag) || "i".equals(tag)) nextStyle.italic = true;
                if ("u".equals(tag)) nextStyle.underline = true;

                // 2. Quill 类名解析
                if (el.hasClass("ql-size-small")) nextStyle.fontSize = 10;
                else if (el.hasClass("ql-size-large")) nextStyle.fontSize = 16;
                else if (el.hasClass("ql-size-huge")) nextStyle.fontSize = 24;

                if (el.hasClass("ql-font-SimSun")) nextStyle.fontFamily = "SimSun";
                else if (el.hasClass("ql-font-SimHei")) nextStyle.fontFamily = "SimHei";
                else if (el.hasClass("ql-font-Microsoft-YaHei")) nextStyle.fontFamily = "Microsoft YaHei";
                else if (el.hasClass("ql-font-KaiTi")) nextStyle.fontFamily = "KaiTi";
                else if (el.hasClass("ql-font-FangSong")) nextStyle.fontFamily = "FangSong";

                // 3. CSS Style 属性解析 (新增 font-weight 处理)
                String styleAttr = el.attr("style");
                if (StringUtils.isNotEmpty(styleAttr)) {
                    String[] styles = styleAttr.split(";");
                    for (String s : styles) {
                        String[] kv = s.split(":");
                        if (kv.length < 2) continue;
                        String k = kv[0].trim().toLowerCase();
                        String v = kv[1].trim();

                        if ("color".equals(k)) {
                            if (v.startsWith("#")) nextStyle.color = v.substring(1);
                            else if (v.startsWith("rgb")) nextStyle.color = rgbToHex(v);
                        } else if ("background-color".equals(k)) {
                            if (v.startsWith("#")) nextStyle.backgroundColor = v.substring(1);
                            else if (v.startsWith("rgb")) nextStyle.backgroundColor = rgbToHex(v);
                        } else if ("font-weight".equals(k)) {
                            // --- 新增：处理 CSS 加粗 ---
                            if ("bold".equalsIgnoreCase(v) || "bolder".equalsIgnoreCase(v)) {
                                nextStyle.bold = true;
                            } else {
                                // 处理数字粗细 (如 700, 800, 900)
                                try {
                                    if (Integer.parseInt(v) >= 700) nextStyle.bold = true;
                                } catch (NumberFormatException ignored) {}
                            }
                        } else if ("font-size".equals(k)) {
                            // (可选) 额外支持 style="font-size: 18px"
                            if (v.endsWith("px")) {
                                try {
                                    int px = Integer.parseInt(v.replace("px", "").trim());
                                    // Word 中 1px 约等于 0.75pt，这里简单近似转换或直接按需要处理
                                    if (px > 0) nextStyle.fontSize = px;
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                }

                if ("a".equals(tag)) {
                    if (!el.select("img").isEmpty()) {
                        for (Element img : el.select("img")) {
                            insertImageToWord(doc, p, img.attr("src"));
                        }
                    } else {
                        String href = el.attr("href");
                        String text = el.text();
                        if (StringUtils.isNotEmpty(text)) {
                            createHyperlink(p, href, text, nextStyle);
                        }
                    }
                    continue;
                } else if ("br".equals(tag)) {
                    p.createRun().addBreak();
                    continue;
                } else if ("img".equals(tag)) {
                    insertImageToWord(doc, p, el.attr("src"));
                    continue;
                }

                processInlineNode(doc, p, child, nextStyle);

            } else if (child instanceof TextNode) {
                String text = ((TextNode) child).getWholeText();
                if (!nextStyle.isCodeBlock) {
                    text = text.replace("\u00A0", " ");
                }

                if (StringUtils.isNotEmpty(text)) {
                    XWPFRun run = p.createRun();
                    run.setText(text);
                    applyStyle(run, nextStyle);
                }
            }
        }
    }

    private void applyStyle(XWPFRun run, StyleContext style) {
        if (style.bold) run.setBold(true);
        if (style.italic) run.setItalic(true);
        if (style.underline) run.setUnderline(UnderlinePatterns.SINGLE);
        if (style.color != null) run.setColor(style.color);
        if (style.fontSize != 12) run.setFontSize(style.fontSize);

        // (修复) 修正字体设置语法
        if (style.fontFamily != null) {
            run.setFontFamily(style.fontFamily);
            if ("SimSun".equals(style.fontFamily)) run.setFontFamily("宋体", XWPFRun.FontCharRange.eastAsia);
            else if ("SimHei".equals(style.fontFamily)) run.setFontFamily("黑体", XWPFRun.FontCharRange.eastAsia);
            else if ("Microsoft YaHei".equals(style.fontFamily))
                run.setFontFamily("微软雅黑", XWPFRun.FontCharRange.eastAsia);
            else if ("KaiTi".equals(style.fontFamily)) run.setFontFamily("楷体", XWPFRun.FontCharRange.eastAsia);
            else if ("FangSong".equals(style.fontFamily)) run.setFontFamily("仿宋", XWPFRun.FontCharRange.eastAsia);
        }

        // (新增) 背景色设置 (Shading)
        if (style.backgroundColor != null) {
            CTRPr rpr = run.getCTR().isSetRPr() ? run.getCTR().getRPr() : run.getCTR().addNewRPr();
            CTShd shd = rpr.isSetShd() ? rpr.getShd() : rpr.addNewShd();
            shd.setVal(STShd.CLEAR);
            shd.setColor("auto");
            shd.setFill(style.backgroundColor);
        }
    }

    // --- 修复：计算图片纵横比 ---
    private void insertImageToWord(XWPFDocument doc, XWPFParagraph p, String src) {
        String physicalPath = resolveImagePath(src);
        if (physicalPath != null && new File(physicalPath).exists()) {
            try (InputStream is = new FileInputStream(physicalPath)) {

                // 1. 读取原始图片尺寸
                BufferedImage bi = ImageIO.read(new File(physicalPath));
                int width = bi.getWidth();
                int height = bi.getHeight();

                // 2. 设定最大宽度 (Word A4 页面默认边距下，可用宽度约为 450pt)
                // POI Unit: EMU (1 inch = 914400 EMU, 1 pt = 12700 EMU)
                // 1 px ≈ 9525 EMU (at 96 DPI)
                int maxContentWidthEMU = 450 * 12700; // 约 450pt

                int originalWidthEMU = width * 9525;
                int originalHeightEMU = height * 9525;

                int finalWidth, finalHeight;

                if (originalWidthEMU > maxContentWidthEMU) {
                    // 如果图片太宽，缩放到最大宽度
                    double ratio = (double) height / width;
                    finalWidth = maxContentWidthEMU;
                    finalHeight = (int) (maxContentWidthEMU * ratio);
                } else {
                    // 否则保持原大小
                    finalWidth = originalWidthEMU;
                    finalHeight = originalHeightEMU;
                }

                // 3. 插入图片
                XWPFRun r = p.createRun();
                int format = getPictureType(physicalPath);
                // 使用计算后的 EMU 尺寸
                r.addPicture(is, format, physicalPath, finalWidth, finalHeight);

            } catch (Exception e) {
                logger.error("Word插入图片失败: " + src, e);
                XWPFRun r = p.createRun();
                r.setText("[图片插入失败]");
                r.setColor("FF0000");
            }
        }
    }

    private void createHyperlink(XWPFParagraph paragraph, String url, String text, StyleContext style) {
        String validUrl = url;

        // 1. 【关键修复】去除前端代理前缀 /dev-api
        // 只有去掉了这个，后端的 Security 配置 (/profile/**) 才能正确匹配并放行
        String apiPrefix = "/dev-api";
        if (validUrl.startsWith(apiPrefix)) {
            validUrl = validUrl.substring(apiPrefix.length());
        }

        // 2. 补全完整 URL
        if (!validUrl.startsWith("http")) {
            if (!validUrl.startsWith("/")) {
                validUrl = "/" + validUrl;
            }
            // 注意：这里默认是 localhost:8080
            // 如果你部署到服务器，建议将 "http://localhost:8080" 提取到配置文件或用 RuoYiConfig 获取
            validUrl = "http://localhost:8080" + validUrl;
        }

        try {
            String rId = paragraph.getDocument().getPackagePart().addExternalRelationship(
                    validUrl, XWPFRelation.HYPERLINK.getRelation()
            ).getId();
            CTHyperlink cLink = paragraph.getCTP().addNewHyperlink();
            cLink.setId(rId);
            CTR cRun = cLink.addNewR();
            CTText cText = cRun.addNewT();
            cText.setStringValue("📎 " + text);

            if (!cRun.isSetRPr()) cRun.addNewRPr();
            CTColor color = cRun.getRPr().isSetColor() ? cRun.getRPr().getColor() : cRun.getRPr().addNewColor();
            color.setVal("0000FF");
            CTUnderline underline = cRun.getRPr().isSetU() ? cRun.getRPr().getU() : cRun.getRPr().addNewU();
            underline.setVal(STUnderline.SINGLE);

        } catch (Exception e) {
            XWPFRun r = paragraph.createRun();
            r.setText(text + " (链接: " + validUrl + ")");
            r.setColor("0000FF");
        }
    }

    // (新增) 颜色转换
    private String rgbToHex(String rgb) {
        if (StringUtils.isEmpty(rgb) || !rgb.startsWith("rgb")) return null;
        try {
            String[] c = rgb.substring(rgb.indexOf("(") + 1, rgb.indexOf(")")).split(",");
            if (c.length >= 3) {
                return String.format("%02X%02X%02X", Integer.parseInt(c[0].trim()),
                        Integer.parseInt(c[1].trim()), Integer.parseInt(c[2].trim()));
            }
        } catch (Exception e) {
        }
        return null;
    }

    private int getPictureType(String filename) {
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        if ("jpg".equals(ext) || "jpeg".equals(ext)) return XWPFDocument.PICTURE_TYPE_JPEG;
        if ("png".equals(ext)) return XWPFDocument.PICTURE_TYPE_PNG;
        if ("gif".equals(ext)) return XWPFDocument.PICTURE_TYPE_GIF;
        if ("bmp".equals(ext)) return XWPFDocument.PICTURE_TYPE_BMP;
        return XWPFDocument.PICTURE_TYPE_PICT;
    }

    private String resolveImagePath(String src) {
        if (StringUtils.isBlank(src)) return null;
        String cleanSrc;
        try {
            cleanSrc = URLDecoder.decode(src, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            cleanSrc = src;
        }
        if (cleanSrc.startsWith("http")) {
            int idx = cleanSrc.indexOf("//");
            if (idx > -1) {
                int pIdx = cleanSrc.indexOf("/", idx + 2);
                if (pIdx > -1) cleanSrc = cleanSrc.substring(pIdx);
            }
        }
        String apiPrefix = "/dev-api";
        if (cleanSrc.startsWith(apiPrefix)) cleanSrc = cleanSrc.substring(apiPrefix.length());
        String profilePath = RuoYiConfig.getProfile();
        String resourcePrefix = Constants.RESOURCE_PREFIX;
        if (cleanSrc.startsWith(resourcePrefix)) {
            String relativePath = cleanSrc.substring(resourcePrefix.length());
            try {
                Path fullPathObj = Paths.get(profilePath, relativePath);
                File imgFile = fullPathObj.toFile();
                if (imgFile.exists()) return fullPathObj.toString();
            } catch (Exception e) {
                logger.error("路径解析错误", e);
            }
        }
        return null;
    }

    private String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ");
    }

    private String cleanInvalidFileName(String title) {
        if (title == null) return "Untitled";
        return title.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private boolean isBlockElement(String tagName) {
        return "p".equals(tagName) || "div".equals(tagName) || "h1".equals(tagName) ||
                "h2".equals(tagName) || "h3".equals(tagName) || "li".equals(tagName) ||
                "ul".equals(tagName) || "ol".equals(tagName) || "blockquote".equals(tagName);
    }
}