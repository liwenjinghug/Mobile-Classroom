package com.ruoyi.proj_qhy.service.impl;

import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.proj_qhy.domain.ForumPost;
import com.ruoyi.proj_qhy.mapper.ForumLikeMapper;
import com.ruoyi.proj_qhy.mapper.ForumPostMapper;
import com.ruoyi.proj_qhy.service.IForumPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ForumPostServiceImpl implements IForumPostService {

    private static final Logger logger = LoggerFactory.getLogger(ForumPostServiceImpl.class);

    @Autowired
    private ForumPostMapper postMapper;

    @Autowired
    private ForumLikeMapper likeMapper;

    // 使用 ruoyi.profile 配置的上传目录（由 RuoYiConfig 注入），ResourcesConfig 已把 Constants.RESOURCE_PREFIX 映射到该目录
    // 不再硬编码到 ruoyi-ui 源码目录
    // 保存时会使用 RuoYiConfig.getProfile()；对外返回的路径使用 Constants.RESOURCE_PREFIX + "/<fileName>"
    @Override
    public List<ForumPost> selectPostList(Long userId) {
        List<ForumPost> posts = postMapper.selectPostList();
        // 补充当前用户点赞状态
        for (ForumPost post : posts) {
            Integer isLiked = likeMapper.selectIsLiked(post.getPostId(), userId);
            post.setIsLiked(isLiked != null && isLiked > 0);
        }
        return posts;
    }

    @Override
    public boolean publishPost(ForumPost post, MultipartFile[] files) {
        try {
            // 设置发布者信息
            Long userId = SecurityUtils.getUserId();
            String username = SecurityUtils.getUsername();
            post.setUserId(userId);
            post.setCreateBy(username);

            // 处理图片上传
            if (files != null && files.length > 0) {
                List<String> imageUrls = new ArrayList<>();
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        // 生成唯一文件名
                        String originalFilename = file.getOriginalFilename();
                        if (!isValidFilename(originalFilename)) {
                            continue;
                        }
                        int dotIdx = originalFilename.lastIndexOf('.');
                        String ext = originalFilename.substring(dotIdx);
                        String fileName = UUID.randomUUID().toString() + ext;

                        // 保存图片到配置的目录（RuoYiConfig.getProfile()），如果配置为空则使用后台模块相对路径
                        String profilePath = RuoYiConfig.getProfile();
                        String saveDir = null;
                        if (profilePath != null && !profilePath.trim().isEmpty()) {
                            saveDir = resolveProfilePath(profilePath);
                        }
                        if (saveDir == null) {
                            // fallback: put into ruoyi-ui assets folder relative to project root
                            saveDir = "D:\\code\\java\\Mobile-Classroom\\ruoyi-ui\\src\\assets\\xxsq_images\\";
                        }

                        File saveFile = new File(saveDir + fileName);
                        if (!saveFile.getParentFile().exists()) {
                            boolean ok = saveFile.getParentFile().mkdirs();
                            if (!ok) {
                                logger.warn("创建图片保存目录失败: {}", saveFile.getParentFile().getAbsolutePath());
                            }
                        }
                        file.transferTo(saveFile);

                        // 记录可被前端通过后端访问的 URL（ResourcesConfig 把 Constants.RESOURCE_PREFIX/** 映射到 profile）
                        imageUrls.add(Constants.RESOURCE_PREFIX + "/" + fileName);
                    }
                }
                if (!imageUrls.isEmpty()) {
                    post.setImageUrls(StringUtils.join(imageUrls, ","));
                }
            }

            // 保存帖子
            return postMapper.insertPost(post) > 0;
        } catch (IOException e) {
            logger.error("保存帖子图片失败", e);
            return false;
        }
    }

    @Override
    public List<ForumPost> refreshPostList(Long userId) {
        // 刷新逻辑同查询列表
        return selectPostList(userId);
    }

    /**
     * 简单校验文件名是否有效（非空且包含扩展名）
     */
    private boolean isValidFilename(String name) {
        return name != null && name.indexOf('.') > 0;
    }

    private String resolveProfilePath(String configuredProfile) {
        if (configuredProfile == null) return null;
        configuredProfile = configuredProfile.trim();
        File cfgFile = new File(configuredProfile);
        if (cfgFile.isAbsolute()) {
            String p = cfgFile.getAbsolutePath();
            if (!p.endsWith(File.separator)) p = p + File.separator;
            return p;
        }
        File dir = new File("").getAbsoluteFile();
        while (dir != null) {
            File candidate = new File(dir, configuredProfile);
            if (candidate.exists()) {
                String p = candidate.getAbsolutePath();
                if (!p.endsWith(File.separator)) p = p + File.separator;
                return p;
            }
            if ("Mobile-Classroom".equals(dir.getName())) {
                File candidate2 = new File(dir, configuredProfile);
                String p2 = candidate2.getAbsolutePath();
                if (!p2.endsWith(File.separator)) p2 = p2 + File.separator;
                return p2;
            }
            dir = dir.getParentFile();
        }
        String fallback = System.getProperty("user.home") + File.separator + "Mobile-Classroom" + File.separator + configuredProfile;
        File f = new File(fallback);
        String abs = f.getAbsolutePath();
        if (!abs.endsWith(File.separator)) abs = abs + File.separator;
        return abs;
    }
}