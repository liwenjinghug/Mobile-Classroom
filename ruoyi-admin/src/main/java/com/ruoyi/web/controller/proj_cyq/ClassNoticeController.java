package com.ruoyi.web.controller.proj_cyq;

import com.ruoyi.proj_cyq.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.proj_cyq.domain.ClassNotice;
import com.ruoyi.proj_cyq.service.IClassNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.io.IOException;
import java.net.URLEncoder;

@RestController
@RequestMapping("/proj_cyq/notice")
public class ClassNoticeController extends BaseController {

    @Autowired
    private IClassNoticeService classNoticeService;

    // ... (保留 list, export, getInfo, add, edit, remove 等方法) ...
    @PreAuthorize("@ss.hasPermi('proj_cyq:notice:list')")
    @GetMapping("/list")
    public TableDataInfo list(ClassNotice notice) {
        startPage();
        List<ClassNotice> list = classNoticeService.selectClassNoticeList(notice);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('proj_cyq:notice:export')")
    @Log(title = "通告管理", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        List<ClassNotice> list = classNoticeService.selectClassNoticeList(new ClassNotice());
        ExcelUtil<ClassNotice> util = new ExcelUtil<>(ClassNotice.class);
        util.exportExcel(response, list, "通告管理数据");
    }

    @PreAuthorize("@ss.hasPermi('proj_cyq:notice:query')")
    @GetMapping(value = "/{noticeId}")
    public AjaxResult getInfo(@PathVariable("noticeId") Long noticeId) {
        return AjaxResult.success(classNoticeService.selectClassNoticeById(noticeId));
    }

    @PreAuthorize("@ss.hasPermi('proj_cyq:notice:add')")
    @Log(title = "通告管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ClassNotice notice) {
        return toAjax(classNoticeService.insertClassNotice(notice));
    }

    @PreAuthorize("@ss.hasPermi('proj_cyq:notice:edit')")
    @Log(title = "通告管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ClassNotice notice) {
        return toAjax(classNoticeService.updateClassNotice(notice));
    }

    @PreAuthorize("@ss.hasPermi('proj_cyq:notice:remove')")
    @Log(title = "通告管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public AjaxResult remove(@PathVariable Long[] noticeIds) {
        return toAjax(classNoticeService.deleteClassNoticeByIds(noticeIds));
    }

    @GetMapping("/stats")
    public AjaxResult getStats() {
        return success(classNoticeService.getNoticeStats());
    }

    // ========== 【修改】导出为 Word ==========
    @PreAuthorize("@ss.hasPermi('proj_cyq:notice:export')")
    @Log(title = "通告管理", businessType = BusinessType.EXPORT)
    @GetMapping("/exportWord/{noticeId}")
    public void exportWord(@PathVariable Long noticeId, HttpServletResponse response) throws IOException {
        // 1. 设置响应头为 Word (docx)
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setCharacterEncoding("utf-8");
        String filename = "通告详情_" + noticeId + ".docx";
        response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));

        // 2. 调用 Service 生成 Word
        classNoticeService.exportNoticeWord(noticeId, response.getOutputStream());
    }
}