// src/main/java/com/ruoyi/web/controller/proj_cyq/ClassNoticeController.java
package com.ruoyi.web.controller.proj_cyq;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.proj_cyq.domain.ClassNotice;
import com.ruoyi.proj_cyq.service.IClassNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/proj_cyq/notice")
public class ClassNoticeController extends BaseController {

    @Autowired
    private IClassNoticeService classNoticeService;

    /**
     * 查询公告列表
     */
    @PreAuthorize("@ss.hasPermi('proj_cyq:notice:list')")
    @GetMapping("/list")
    public TableDataInfo list(ClassNotice notice) {
        startPage();
        List<ClassNotice> list = classNoticeService.selectClassNoticeList(notice);
        return getDataTable(list);
    }

    /**
     * 导出公告列表 - 修改为GET请求，不接收参数
     */
    @PreAuthorize("@ss.hasPermi('proj_cyq:notice:export')")
    @Log(title = "课堂公告", businessType = BusinessType.EXPORT)
    @GetMapping("/export")  // 改为GET请求
    public void export(HttpServletResponse response) {
        List<ClassNotice> list = classNoticeService.selectClassNoticeList(new ClassNotice());
        ExcelUtil<ClassNotice> util = new ExcelUtil<>(ClassNotice.class);
        util.exportExcel(response, list, "课堂公告数据");
    }

    /**
     * 获取公告详细信息
     */
    @PreAuthorize("@ss.hasPermi('proj_cyq:notice:query')")
    @GetMapping(value = "/{noticeId}")
    public AjaxResult getInfo(@PathVariable("noticeId") Long noticeId) {
        return AjaxResult.success(classNoticeService.selectClassNoticeById(noticeId));
    }

    /**
     * 新增公告
     */
    @PreAuthorize("@ss.hasPermi('proj_cyq:notice:add')")
    @Log(title = "课堂公告", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ClassNotice notice) {
        return toAjax(classNoticeService.insertClassNotice(notice));
    }

    /**
     * 修改公告
     */
    @PreAuthorize("@ss.hasPermi('proj_cyq:notice:edit')")
    @Log(title = "课堂公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ClassNotice notice) {
        return toAjax(classNoticeService.updateClassNotice(notice));
    }

    /**
     * 删除公告
     */
    @PreAuthorize("@ss.hasPermi('proj_cyq:notice:remove')")
    @Log(title = "课堂公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public AjaxResult remove(@PathVariable Long[] noticeIds) {
        return toAjax(classNoticeService.deleteClassNoticeByIds(noticeIds));
    }
}