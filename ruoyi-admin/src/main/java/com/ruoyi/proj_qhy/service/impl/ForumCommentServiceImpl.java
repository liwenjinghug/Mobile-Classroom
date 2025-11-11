package com.ruoyi.proj_qhy.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.proj_qhy.domain.ForumComment;
import com.ruoyi.proj_qhy.mapper.ForumCommentMapper;
import com.ruoyi.proj_qhy.mapper.ForumPostMapper;
import com.ruoyi.proj_qhy.service.IForumCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ForumCommentServiceImpl implements IForumCommentService {

    @Autowired
    private ForumCommentMapper commentMapper;

    @Autowired
    private ForumPostMapper postMapper;

    @Override
    public List<ForumComment> getCommentsByPostId(Long postId) {
        return commentMapper.selectCommentListByPostId(postId);
    }

    @Override
    @Transactional
    public boolean addComment(ForumComment comment) {
        // 设置评论者信息
        Long userId = SecurityUtils.getUserId();
        String username = SecurityUtils.getUsername();
        comment.setUserId(userId);
        comment.setCreateBy(username);

        // 保存评论
        int rows = commentMapper.insertComment(comment);
        if (rows > 0) {
            // 更新帖子评论数
            postMapper.updateCommentCount(comment.getPostId(), 1);
            return true;
        }
        return false;
    }
}