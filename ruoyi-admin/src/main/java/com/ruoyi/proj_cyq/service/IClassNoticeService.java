// src/main/java/com/ruoyi/proj_cyq/service/IClassNoticeService.java
package com.ruoyi.proj_cyq.service;

import com.ruoyi.proj_cyq.domain.ClassNotice;
import java.util.List;

public interface IClassNoticeService {

    /**
     * 查询公告列表
     */
    List<ClassNotice> selectClassNoticeList(ClassNotice notice);

    /**
     * 根据ID查询公告
     */
    ClassNotice selectClassNoticeById(Long noticeId);

    /**
     * 新增公告
     */
    int insertClassNotice(ClassNotice notice);

    /**
     * 修改公告
     */
    int updateClassNotice(ClassNotice notice);

    /**
     * 删除公告
     */
    int deleteClassNoticeById(Long noticeId);

    /**
     * 批量删除公告
     */
    int deleteClassNoticeByIds(Long[] noticeIds);
}