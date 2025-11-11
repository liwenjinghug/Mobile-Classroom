package com.ruoyi.proj_qhy.service;

import com.ruoyi.proj_qhy.domain.ForumComment;
import java.util.List;

public interface IForumCommentService {
    List<ForumComment> getCommentsByPostId(Long postId);
    boolean addComment(ForumComment comment);
}