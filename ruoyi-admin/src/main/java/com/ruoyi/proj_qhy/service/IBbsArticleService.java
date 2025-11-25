package com.ruoyi.proj_qhy.service;

import java.util.List;
import com.ruoyi.proj_qhy.domain.BbsArticle;

import javax.servlet.http.HttpServletResponse;

/**
 * 文章管理Service接口
 */
public interface IBbsArticleService {
    /**
     * 查询文章管理
     */
    public BbsArticle selectBbsArticleById(Long id);

    /**
     * 查询文章管理列表
     */
    public List<BbsArticle> selectBbsArticleList(BbsArticle bbsArticle);

    /**
     * 新增文章管理
     */
    public int insertBbsArticle(BbsArticle bbsArticle);

    /**
     * 修改文章管理
     */
    public int updateBbsArticle(BbsArticle bbsArticle);

    /**
     * 删除文章管理信息
     */
    public int deleteBbsArticleById(Long id);

    /**
     * 批量删除文章管理
     */
    public int deleteBbsArticleByIds(Long[] ids);

    /**
     * 增加阅读数
     */
    public int increaseViewCount(Long id);

    public int likeArticle(Long id);
    public int hateArticle(Long id);

    /**
     * 查询热门文章列表
     */
    public List<BbsArticle> selectHotArticleList(BbsArticle bbsArticle);

    /**
     * (新增) 批量查询文章管理
     */
    public List<BbsArticle> selectBbsArticleByIds(Long[] ids);

    /**
     * (新增) 导出文章为 PDF
     */
    void exportArticlesToPdf(Long[] ids, HttpServletResponse response) throws Exception;
}