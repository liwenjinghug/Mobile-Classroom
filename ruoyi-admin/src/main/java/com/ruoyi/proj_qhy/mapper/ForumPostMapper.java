package com.ruoyi.proj_qhy.mapper;

import com.ruoyi.proj_qhy.domain.ForumPost;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ForumPostMapper {
    /**
     * 查询帖子列表（关联用户昵称、头像和当前用户点赞状态）
     */
    @Select("SELECT p.*, u.nick_name AS nickName, u.avatar, " +
            "EXISTS(SELECT 1 FROM class_forum_like l WHERE l.post_id = p.post_id AND l.user_id = #{userId} AND l.del_flag = '0') AS isLiked " +
            "FROM class_forum_post p " +
            "LEFT JOIN sys_user u ON p.user_id = u.user_id " +
            "WHERE p.del_flag = '0' AND p.status = '0' " +
            "ORDER BY p.create_time DESC")
    List<ForumPost> selectPostList(@Param("userId") Long userId);

    /**
     * 根据帖子ID查询详情（用于修改、鉴权）
     */
    @Select("SELECT * FROM class_forum_post WHERE post_id = #{postId} AND del_flag = '0'")
    ForumPost selectPostByIdSimple(Long postId);

    /**
     * 新增帖子
     */
    @Insert("INSERT INTO class_forum_post (user_id, content, image_urls, like_count, comment_count, status, del_flag, create_by, create_time) " +
            "VALUES (#{userId}, #{content}, #{imageUrls}, 0, 0, '0', '0', #{createBy}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "postId")
    int insertPost(ForumPost post);

    /**
     * 更新帖子
     */
    @Update("UPDATE class_forum_post SET " +
            "content = #{content}, " +
            "image_urls = #{imageUrls}, " +
            "update_by = #{updateBy}, " +
            "update_time = NOW() " +
            "WHERE post_id = #{postId}")
    int updatePost(ForumPost post);

    /**
     * 逻辑删除帖子
     */
    @Update("UPDATE class_forum_post SET del_flag = '2', update_by = #{updateBy}, update_time = NOW() " +
            "WHERE post_id = #{postId}")
    int deletePostById(ForumPost post);


    /**
     * 更新帖子点赞数
     */
    @Update("UPDATE class_forum_post SET like_count = like_count + #{num} WHERE post_id = #{postId}")
    int updateLikeCount(@Param("postId") Long postId, @Param("num") Integer num);

    /**
     * 更新帖子评论数
     */
    @Update("UPDATE class_forum_post SET comment_count = comment_count + #{num} WHERE post_id = #{postId}")
    int updateCommentCount(@Param("postId") Long postId, @Param("num") Integer num);
}