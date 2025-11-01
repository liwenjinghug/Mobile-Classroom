package com.ruoyi.proj_qhy.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.proj_qhy.mapper.BbsArticleMapper;
import com.ruoyi.proj_qhy.domain.BbsArticle;
import com.ruoyi.proj_qhy.service.IBbsArticleService;

/**
 * 文章管理Service业务层处理
 */
@Service
public class BbsArticleServiceImpl implements IBbsArticleService {
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


}