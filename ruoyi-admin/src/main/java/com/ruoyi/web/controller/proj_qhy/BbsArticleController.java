package com.ruoyi.web.controller.proj_qhy;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.proj_qhy.domain.BbsArticle;
import com.ruoyi.proj_qhy.service.IBbsArticleService;

/**
 * 文章管理Controller
 */
@RestController
@RequestMapping("/proj_qhy/article")
public class BbsArticleController extends BaseController {
    @Autowired
    private IBbsArticleService bbsArticleService;

    /**
     * 查询文章管理列表
     */
    //@PreAuthorize("@ss.hasPermi('proj_qhy:article:list')")
    @GetMapping("/list")
    public TableDataInfo list(BbsArticle bbsArticle) {
        startPage();
        List<BbsArticle> list = bbsArticleService.selectBbsArticleList(bbsArticle);
        return getDataTable(list);
    }

    /**
     * 导出文章管理列表
     */
    @PreAuthorize("@ss.hasPermi('proj_qhy:article:export')")
    @Log(title = "文章管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BbsArticle bbsArticle) {
        List<BbsArticle> list = bbsArticleService.selectBbsArticleList(bbsArticle);
        ExcelUtil<BbsArticle> util = new ExcelUtil<BbsArticle>(BbsArticle.class);
        util.exportExcel(response, list, "文章管理数据");
    }

    /**
     * 获取文章管理详细信息
     */
    //@PreAuthorize("@ss.hasPermi('proj_qhy:article:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(bbsArticleService.selectBbsArticleById(id));
    }

    /**
     * 新增文章管理
     */
    @PreAuthorize("@ss.hasPermi('proj_qhy:article:add')")
    @Log(title = "文章管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BbsArticle bbsArticle) {
        return toAjax(bbsArticleService.insertBbsArticle(bbsArticle));
    }

    /**
     * 修改文章管理
     */
    @PreAuthorize("@ss.hasPermi('proj_qhy:article:edit')")
    @Log(title = "文章管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BbsArticle bbsArticle) {
        return toAjax(bbsArticleService.updateBbsArticle(bbsArticle));
    }

    /**
     * 删除文章管理
     */
    @PreAuthorize("@ss.hasPermi('proj_qhy:article:remove')")
    @Log(title = "文章管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(bbsArticleService.deleteBbsArticleByIds(ids));
    }

    /**
     * 获取热门文章
     */
    @GetMapping("/hot")
    public AjaxResult hotArticles(BbsArticle bbsArticle) {
        List<BbsArticle> list = bbsArticleService.selectHotArticleList(bbsArticle);
        return success(list);
    }

    /**
     * 增加阅读数
     */
    @PostMapping("/view/{id}")
    public AjaxResult increaseViewCount(@PathVariable Long id) {
        return toAjax(bbsArticleService.increaseViewCount(id));
    }
    /**
     * 点赞文章
     */
    @PostMapping("/like/{id}")
    public AjaxResult likeArticle(@PathVariable Long id) {
        return toAjax(bbsArticleService.likeArticle(id));
    }

    /**
     * 点踩文章
     */
    @PostMapping("/hate/{id}")
    public AjaxResult hateArticle(@PathVariable Long id) {
        return toAjax(bbsArticleService.hateArticle(id));
    }

    /**
     * (新增) 批量导出文章为PDF
     */
    //@PreAuthorize("@ss.hasPermi('proj_qhy:article:export')") // 复用导出权限
    @Log(title = "文章导出PDF", businessType = BusinessType.EXPORT)
    @PostMapping("/export-pdf")
    public AjaxResult exportPdf(@RequestBody Long[] ids) {
        try {
            List<String> fileUrls = bbsArticleService.exportArticlesToPdf(ids);
            // 返回文件路径列表，前端可以引导下载
            return AjaxResult.success("批量导出PDF成功", fileUrls);
        } catch (Exception e) {
            logger.error("导出PDF失败", e);
            return AjaxResult.error("导出PDF失败: " + e.getMessage());
        }
    }
}