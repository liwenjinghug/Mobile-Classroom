package com.ruoyi.proj_qhy.service;

import com.ruoyi.proj_qhy.domain.ForumLike;
import java.util.List;

public interface IForumLikeService {
    List<ForumLike> getLikesByPostId(Long postId);
    boolean likePost(Long postId, Long userId, String username);
    boolean cancelLike(Long postId, Long userId, String username);
}