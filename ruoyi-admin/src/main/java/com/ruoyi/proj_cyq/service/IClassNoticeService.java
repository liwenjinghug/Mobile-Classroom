package com.ruoyi.proj_cyq.service;

import com.ruoyi.proj_cyq.domain.ClassNotice;
import java.util.List;
import java.util.Map;
import java.io.OutputStream;

public interface IClassNoticeService {

    List<ClassNotice> selectClassNoticeList(ClassNotice notice);

    ClassNotice selectClassNoticeById(Long noticeId);

    int insertClassNotice(ClassNotice notice);

    int updateClassNotice(ClassNotice notice);

    int deleteClassNoticeById(Long noticeId);

    int deleteClassNoticeByIds(Long[] noticeIds);

    Map<String, Object> getNoticeStats();

    /**
     * 【修改】导出通告为 Word
     */
    void exportNoticeWord(Long noticeId, OutputStream outputStream);
}