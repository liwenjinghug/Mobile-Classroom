package com.ruoyi.proj_qhy.mapper;

import com.ruoyi.proj_qhy.domain.ForumComment;
import com.ruoyi.proj_qhy.domain.ForumNotice;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ForumCommentMapper {
    /**
     * 查询帖子的评论列表
     */
    @Select("""
        SELECT c.*, u.nick_name AS nickName, ru.nick_name AS replyToNickName
        FROM class_forum_comment c
        LEFT JOIN sys_user u ON c.user_id = u.user_id
        LEFT JOIN sys_user ru ON c.reply_to_user_id = ru.user_id
        WHERE c.post_id = #{postId} AND c.del_flag = '0'
        ORDER BY c.create_time ASC
    """)
    List<ForumComment> selectCommentListByPostId(@Param("postId") Long postId);

    /**
     * 新增评论
     */
    @Insert("""
        INSERT INTO class_forum_comment (post_id, user_id, parent_id, reply_to_user_id, content, del_flag, create_by, create_time)
        VALUES (#{postId}, #{userId}, #{parentId}, #{replyToUserId}, #{content}, '0', #{createBy}, NOW())
    """)
    @Options(useGeneratedKeys = true, keyProperty = "commentId")
    int insertComment(ForumComment comment);

    /**
     * 查询用户的评论通知
     */
    @Select("""
        SELECT c.comment_id AS noticeId, c.post_id, u.user_id AS operatorUserId, u.nick_name AS operatorNickName,
        u.avatar AS operatorAvatar, 2 AS noticeType, c.content AS commentContent, c.create_time
        FROM class_forum_comment c
        LEFT JOIN class_forum_post p ON c.post_id = p.post_id
        LEFT JOIN sys_user u ON c.user_id = u.user_id
        WHERE p.user_id = #{userId} AND c.user_id != #{userId} AND c.del_flag = '0'
        ORDER BY c.create_time DESC
    """)
    List<ForumNotice> selectCommentNoticeByUserId(@Param("userId") Long userId);
}