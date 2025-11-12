package com.ruoyi.proj_qhy.service.impl;

import com.ruoyi.proj_qhy.domain.ForumLike;
import com.ruoyi.proj_qhy.mapper.ForumLikeMapper;
import com.ruoyi.proj_qhy.mapper.ForumPostMapper;
import com.ruoyi.proj_qhy.service.IForumLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ForumLikeServiceImpl implements IForumLikeService {

    @Autowired
    private ForumLikeMapper likeMapper;

    @Autowired
    private ForumPostMapper postMapper;

    @Override
    public List<ForumLike> getLikesByPostId(Long postId) {
        return likeMapper.selectLikeListByPostId(postId);
    }

    @Override
    @Transactional
    public boolean likePost(Long postId, Long userId, String username) {
        // 1. 先查询是否存在有效的点赞记录（包括未删除的）
        Integer isLiked = likeMapper.selectIsLiked(postId, userId);
        if (isLiked != null && isLiked > 0) {
            return false; // 已点赞，返回失败
        }

        // 2. 检查是否存在已删除的点赞记录（del_flag='2'），如果有则恢复而不是新增
        Integer deletedLikeCount = likeMapper.selectDeletedLikeCount(postId, userId);
        if (deletedLikeCount != null && deletedLikeCount > 0) {
            // 恢复已删除的点赞记录
            int recoverRows = likeMapper.recoverLike(postId, userId, username);
            if (recoverRows > 0) {
                postMapper.updateLikeCount(postId, 1);
                return true;
            }
            return false;
        }

        // 3. 新增点赞记录
        ForumLike like = new ForumLike();
        like.setPostId(postId);
        like.setUserId(userId);
        like.setCreateBy(username);
        int rows = likeMapper.insertLike(like);

        // 4. 更新帖子点赞数
        if (rows > 0) {
            postMapper.updateLikeCount(postId, 1);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean cancelLike(Long postId, Long userId, String username) {
        // 取消点赞
        int rows = likeMapper.cancelLike(postId, userId, username);
        if (rows > 0) {
            // 更新帖子点赞数
            postMapper.updateLikeCount(postId, -1);
            return true;
        }
        return false;
    }
}