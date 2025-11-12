package com.ruoyi.proj_qhy.mapper;

import com.ruoyi.proj_qhy.domain.ForumLike;
import com.ruoyi.proj_qhy.domain.ForumNotice;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ForumLikeMapper {
    /**
     * 查询帖子的点赞列表（含用户昵称）
     */
    @Select("SELECT l.*, u.nick_name AS nickName " +
            "FROM class_forum_like l " +
            "LEFT JOIN sys_user u ON l.user_id = u.user_id " +
            "WHERE l.post_id = #{postId} AND l.del_flag = '0' " +
            "ORDER BY l.create_time ASC")
    List<ForumLike> selectLikeListByPostId(@Param("postId") Long postId);

    /**
     * 查询用户是否已点赞帖子（有效的点赞）
     */
    @Select("SELECT COUNT(1) FROM class_forum_like WHERE post_id = #{postId} AND user_id = #{userId} AND del_flag = '0'")
    Integer selectIsLiked(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 查询用户是否有已删除的点赞记录
     */
    @Select("SELECT COUNT(1) FROM class_forum_like WHERE post_id = #{postId} AND user_id = #{userId} AND del_flag = '2'")
    Integer selectDeletedLikeCount(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 恢复已删除的点赞记录
     */
    @Update("UPDATE class_forum_like " +
            "SET del_flag = '0', update_by = #{updateBy}, update_time = NOW() " +
            "WHERE post_id = #{postId} AND user_id = #{userId} AND del_flag = '2'")
    int recoverLike(@Param("postId") Long postId, @Param("userId") Long userId, @Param("updateBy") String updateBy);

    /**
     * 新增点赞
     */
    @Insert("INSERT INTO class_forum_like (post_id, user_id, del_flag, create_by, create_time) " +
            "VALUES (#{postId}, #{userId}, '0', #{createBy}, NOW())")
    int insertLike(ForumLike like);

    /**
     * 取消点赞（逻辑删除）
     */
    @Update("UPDATE class_forum_like SET del_flag = '2', update_by = #{updateBy}, update_time = NOW() " +
            "WHERE post_id = #{postId} AND user_id = #{userId} AND del_flag = '0'")
    int cancelLike(@Param("postId") Long postId, @Param("userId") Long userId, @Param("updateBy") String updateBy);

    /**
     * 查询用户的点赞通知
     */
    @Select("SELECT l.like_id AS noticeId, l.post_id, u.user_id AS operatorUserId, u.nick_name AS operatorNickName, " +
            "u.avatar AS operatorAvatar, 1 AS noticeType, l.create_time " +
            "FROM class_forum_like l " +
            "LEFT JOIN class_forum_post p ON l.post_id = p.post_id " +
            "LEFT JOIN sys_user u ON l.user_id = u.user_id " +
            "WHERE p.user_id = #{userId} AND l.user_id != #{userId} AND l.del_flag = '0' " +
            "ORDER BY l.create_time DESC")
    List<ForumNotice> selectLikeNoticeByUserId(@Param("userId") Long userId);
}