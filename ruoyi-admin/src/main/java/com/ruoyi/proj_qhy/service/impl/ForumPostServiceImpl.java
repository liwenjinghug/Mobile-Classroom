package com.ruoyi.proj_qhy.service.impl;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.proj_qhy.domain.ForumPost;
import com.ruoyi.proj_qhy.mapper.ForumCommentMapper;
import com.ruoyi.proj_qhy.mapper.ForumLikeMapper;
import com.ruoyi.proj_qhy.mapper.ForumPostMapper;
import com.ruoyi.proj_qhy.service.IForumPostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 论坛帖子服务实现
 */
@Service
public class ForumPostServiceImpl implements IForumPostService {

    private static final Logger logger = LoggerFactory.getLogger(ForumPostServiceImpl.class);

    @Autowired
    private ForumPostMapper postMapper;

    @Autowired
    private ForumLikeMapper likeMapper;

    @Autowired
    private ForumCommentMapper commentMapper; // 确保已创建 ForumCommentMapper

    @Override
    public List<ForumPost> selectPostList(Long userId) {
        // 假设 postMapper.selectPostList 已按要求修改，通过 JOIN 一次性查询
        return postMapper.selectPostList(userId);
    }

    @Override
    public List<ForumPost> refreshPostList(Long userId) {
        // 刷新和查询列表是同一逻辑
        return postMapper.selectPostList(userId);
    }

    @Override
    @Transactional
    public boolean publishPost(ForumPost post, MultipartFile[] files) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long userId = loginUser.getUserId();
        String username = loginUser.getUsername();

        // 1. 处理图片上传
        String imageUrls = uploadFiles(files);

        // 2. 设置帖子属性
        post.setUserId(userId);
        post.setCreateBy(username);
        post.setImageUrls(imageUrls);

        // 3. 插入数据库
        int rows = postMapper.insertPost(post);
        return rows > 0;
    }

    @Override
    @Transactional
    public boolean updatePost(ForumPost post, MultipartFile[] files) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long currentUserId = loginUser.getUserId();
        String username = loginUser.getUsername();

        // 1. 鉴权：检查帖子是否存在且属于当前用户
        ForumPost existingPost = postMapper.selectPostByIdSimple(post.getPostId());
        if (existingPost == null) {
            throw new ServiceException("帖子不存在");
        }
        if (!existingPost.getUserId().equals(currentUserId)) {
            throw new ServiceException("无权修改此帖子");
        }

        // 2. 处理图片
        // 2.1 上传新图片
        String newImageUrls = uploadFiles(files);

        // 2.2 合并新旧图片
        // post.getImageUrls() 此时应包含前端传来的、需要保留的旧图片URL
        List<String> finalImageUrls = new ArrayList<>();
        if (StringUtils.isNotEmpty(post.getImageUrls())) {
            finalImageUrls.addAll(Arrays.asList(post.getImageUrls().split(",")));
        }
        if (StringUtils.isNotEmpty(newImageUrls)) {
            finalImageUrls.addAll(Arrays.asList(newImageUrls.split(",")));
        }

        // 3. 设置更新属性
        post.setImageUrls(String.join(",", finalImageUrls));
        post.setUpdateBy(username);

        // 4. 更新数据库
        int rows = postMapper.updatePost(post);
        return rows > 0;
    }

    @Override
    @Transactional
    public boolean deletePost(Long postId) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long currentUserId = loginUser.getUserId();
        String username = loginUser.getUsername();

        // 1. 鉴权
        ForumPost existingPost = postMapper.selectPostByIdSimple(postId);
        if (existingPost == null) {
            // 帖子可能已被删除，也算成功
            return true;
        }
        if (!existingPost.getUserId().equals(currentUserId)) {
            throw new ServiceException("无权删除此帖子");
        }

        // 2. 准备更新信息
        existingPost.setUpdateBy(username);

        // 3. 逻辑删除帖子
        int postRows = postMapper.deletePostById(existingPost);

        // 4. 逻辑删除关联的点赞
        likeMapper.deleteLikesByPostId(postId, username);

        // 5. 逻辑删除关联的评论
        commentMapper.deleteCommentsByPostId(postId, username);

        return postRows > 0;
    }


    /**
     * 统一文件上传逻辑
     * (这个逻辑比您现有的更健壮，它依赖于若依的配置 ruoyi.profile)
     */
    private String uploadFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return "";
        }

        List<String> successUrls = new ArrayList<>();
        // 获取若依配置的全局上传路径
        String configuredProfile = RuoYiConfig.getProfile();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            try {
                // 1. 生成文件名
                String originalFilename = file.getOriginalFilename();
                String newFileName = generateFileName(originalFilename);

                // 2. 确定保存路径
                String savePath = configuredProfile;
                if (StringUtils.isEmpty(savePath)) {
                    // 备用方案（不推荐，但作为兜底）
                    savePath = new File("").getAbsolutePath() + "/uploads/forum";
                }

                // 确保目录存在
                File destDir = new File(savePath);
                if (!destDir.exists()) {
                    destDir.mkdirs();
                }

                // 3. 保存文件
                File destFile = new File(savePath + File.separator + newFileName);
                file.transferTo(destFile);

                // 4. 构建返回的URL
                // Constants.RESOURCE_PREFIX 是 /profile
                String imageUrl = Constants.RESOURCE_PREFIX + "/" + newFileName;
                successUrls.add(imageUrl);

            } catch (Exception e) {
                logger.error("文件上传失败: " + file.getOriginalFilename(), e);
            }
        }

        return successUrls.stream().collect(Collectors.joining(","));
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String originalFilename) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return "forum_" + timeStamp + "_" + uuid + fileExtension;
    }
}