package com.ruoyi.proj_qhy.service;

import com.ruoyi.proj_qhy.domain.ForumNotice;
import java.util.List;

public interface IForumNoticeService {
    List<ForumNotice> getUserNotices(Long userId);
}