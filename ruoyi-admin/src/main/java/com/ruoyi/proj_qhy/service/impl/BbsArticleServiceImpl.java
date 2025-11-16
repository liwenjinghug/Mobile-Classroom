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
     * (修正) 导出文章为 PDF
     * (使用 Jsoup 解析 HTML，支持图片)
     */
    @Override
    public List<String> exportArticlesToPdf(Long[] ids) throws Exception {
        // 1. 查询文章数据
        List<BbsArticle> articles = bbsArticleMapper.selectBbsArticleByIds(ids);
        if (articles == null || articles.isEmpty()) {
            throw new RuntimeException("没有找到要导出的文章");
        }

        // 2. 准备保存路径
        String savePath = RuoYiConfig.getProfile();
        File saveDir = new File(savePath);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        // 3. 准备中文字体
        BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        Font titleFont = new Font(bfChinese, 18, Font.BOLD);
        Font metaFont = new Font(bfChinese, 12, Font.ITALIC);
        Font bodyFont = new Font(bfChinese, 12, Font.NORMAL);

        List<String> generatedFiles = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        // 4. 循环生成 PDF
        for (BbsArticle article : articles) {
            String title = cleanInvalidFileName(article.getTitle());
            String time = sdf.format(article.getCreateTime() != null ? article.getCreateTime() : new Date());
            String fileName = title + time + ".pdf";
            String filePath = savePath + File.separator + fileName;

            Document document = new Document();
            FileOutputStream fos = null;

            try {
                fos = new FileOutputStream(filePath);
                PdfWriter.getInstance(document, fos);
                document.open(); // 打开

                // --- 写入内容 ---

                // 标题
                document.add(new Paragraph(article.getTitle(), titleFont));

                // 元数据
                String meta = String.format("作者: %s | 类型: %s | 发布于: %s",
                        article.getAuthor(), article.getArticleType(),
                        new SimpleDateFormat("yyyy-MM-dd HH:mm").format(article.getCreateTime()));
                document.add(new Paragraph(meta, metaFont));

                // 摘要 (使用 Jsoup 解析)
                document.add(new Paragraph("--- 摘要 ---", metaFont));
                parseAndAddHtml(document, article.getDigest(), bodyFont); // <-- (新方法)

                // 正文 (使用 Jsoup 解析)
                document.add(new Paragraph("--- 正文 ---", metaFont));
                parseAndAddHtml(document, article.getContent(), bodyFont); // <-- (新方法)

            } catch (Exception e) {
                logger.error("生成PDF文件 " + fileName + " 时出错", e);
                throw new RuntimeException("生成PDF " + fileName + " 失败", e);
            } finally {
                // (关键) 在 finally 中关闭 document
                if (document.isOpen()) {
                    document.close();
                }
                // (fos 会在 document.close() 时自动关闭，但保险起见)
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }

            // 返回可访问的 URL
            generatedFiles.add(Constants.RESOURCE_PREFIX + "/" + fileName);
        }

        return generatedFiles;
    }
    /**
     * (新增) 辅助方法：使用 Jsoup 解析 HTML 并添加到 PDF
     * @param pdfDoc PDF 文档对象
     * @param html HTML 内容
     * @param defaultFont 默认字体
     */
    private void parseAndAddHtml(Document pdfDoc, String html, Font defaultFont) throws Exception {
        if (StringUtils.isBlank(html)) {
            return;
        }

        // 1. 使用 Jsoup 解析 HTML 片段
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parseBodyFragment(html);

        // 2. 遍历 body 下的所有子节点
        for (org.jsoup.nodes.Node node : jsoupDoc.body().childNodes()) {

            if (node instanceof TextNode) {
                // A. 如果是纯文本节点
                String text = ((TextNode) node).getWholeText();
                if (StringUtils.isNotBlank(text)) {
                    pdfDoc.add(new Paragraph(text, defaultFont));
                }
            } else if (node instanceof Element) {
                // B. 如果是 HTML 元素
                Element element = (Element) node;

                if (element.tagName().equals("img")) {
                    // (重点!) 如果是图片
                    String src = element.attr("src");
                    String physicalPath = resolveImagePath(src); // 解析为物理路径

                    if (physicalPath != null) {
                        try {
                            File imgFile = new File(physicalPath);
                            if (imgFile.exists()) {
                                // 将图片添加到 PDF
                                Image pdfImage = Image.getInstance(physicalPath);
                                // 缩放图片以适应页面宽度
                                float pageWidth = pdfDoc.getPageSize().getWidth() - pdfDoc.leftMargin() - pdfDoc.rightMargin();
                                if (pdfImage.getWidth() > pageWidth) {
                                    pdfImage.scaleToFit(pageWidth, Float.MAX_VALUE);
                                }
                                pdfDoc.add(pdfImage);
                            } else {
                                logger.warn("PDF导出：找不到图片文件 " + physicalPath);
                                pdfDoc.add(new Paragraph("[图片加载失败: " + src + "]", defaultFont));
                            }
                        } catch (Exception e) {
                            logger.warn("PDF导出：加载图片 " + physicalPath + " 出错", e);
                            pdfDoc.add(new Paragraph("[图片加载失败: " + src + "]", defaultFont));
                        }
                    }
                } else {
                    // C. 如果是其他元素 (p, div, h1, ...)
                    // (简化处理：只获取它们的纯文本)
                    String text = element.text();
                    if (StringUtils.isNotBlank(text)) {
                        pdfDoc.add(new Paragraph(text, defaultFont));
                    }
                }
            }
        }
    }

    /**
     * (新增) 辅助方法：将前端的图片 URL 解析为服务器物理路径
     * @param src (例如 /profile/upload/2025/11/16/image.png)

     */
    private String resolveImagePath(String src) {
        if (StringUtils.isBlank(src)) {
            return null;
        }

        // 获取若依的全局上传路径
        String profilePath = RuoYiConfig.getProfile();
        // 获取资源前缀 (例如: /profile)
        String resourcePrefix = Constants.RESOURCE_PREFIX;

        if (src.startsWith(resourcePrefix)) {
            // 1. 移除 /profile
            String relativePath = src.substring(resourcePrefix.length());

            // 2. 拼接
            // (profilePath: 
            // (注意: .replace('/', File.separator) 确保跨平台)
            return profilePath + relativePath.replace('/', File.separatorChar);
        }

        // (如果图片是外部 http/https 链接，暂不支持下载)
        if (src.startsWith("http")) {
            logger.warn("PDF导出：跳过外部图片 " + src);
            return null;
        }

        // (如果 src 是其他格式，尝试直接拼接)
        return profilePath + src.replace('/', File.separatorChar);
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