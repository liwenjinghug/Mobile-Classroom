package com.ruoyi.proj_qhy.service;

import com.ruoyi.proj_qhy.domain.ForumPost;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IForumPostService {
    List<ForumPost> selectPostList(Long userId);
    boolean publishPost(ForumPost post, MultipartFile[] files);
    List<ForumPost> refreshPostList(Long userId);
}