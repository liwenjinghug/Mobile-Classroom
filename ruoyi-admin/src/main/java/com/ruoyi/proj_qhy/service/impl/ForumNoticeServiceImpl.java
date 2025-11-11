package com.ruoyi.proj_qhy.service.impl;

import com.ruoyi.proj_qhy.domain.ForumNotice;
import com.ruoyi.proj_qhy.mapper.ForumCommentMapper;
import com.ruoyi.proj_qhy.mapper.ForumLikeMapper;
import com.ruoyi.proj_qhy.service.IForumNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ForumNoticeServiceImpl implements IForumNoticeService {

    @Autowired
    private ForumLikeMapper likeMapper;

    @Autowired
    private ForumCommentMapper commentMapper;

    @Override
    public List<ForumNotice> getUserNotices(Long userId) {
        // 查询点赞通知
        List<ForumNotice> likeNotices = likeMapper.selectLikeNoticeByUserId(userId);
        // 查询评论通知
        List<ForumNotice> commentNotices = commentMapper.selectCommentNoticeByUserId(userId);

        // 合并通知并按时间排序
        List<ForumNotice> allNotices = new ArrayList<>();
        allNotices.addAll(likeNotices);
        allNotices.addAll(commentNotices);
        allNotices.sort((n1, n2) -> n2.getCreateTime().compareTo(n1.getCreateTime()));

        return allNotices;
    }
}