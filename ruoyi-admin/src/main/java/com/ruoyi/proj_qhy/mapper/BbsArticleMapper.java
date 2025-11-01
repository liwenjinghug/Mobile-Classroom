package com.ruoyi.proj_qhy.mapper;

import java.util.List;
import org.apache.ibatis.annotations.*;
import com.ruoyi.proj_qhy.domain.BbsArticle;

/**
 * 文章管理Mapper接口
 */
@Mapper
public interface BbsArticleMapper {

    /**
     * 查询文章管理
     */
    @Select("SELECT id, title, digest, content, cover, article_type as articleType, status, view_count as viewCount, comment_count as commentCount, like_count as likeCount, hate_count as hateCount, bookmark_count as bookmarkCount, author, user_id as userId, create_by as createBy, create_time as createTime, update_by as updateBy, update_time as updateTime, remark FROM class_article WHERE id = #{id}")
    BbsArticle selectBbsArticleById(Long id);

    /**
     * 查询文章管理列表
     */
    @Select({
            "<script>",
            "SELECT ",
            "id, ",
            "title, ",
            "digest, ",
            "content, ",
            "cover, ",
            "article_type as articleType, ",  // 添加别名
            "status, ",
            "view_count as viewCount, ",      // 添加别名
            "comment_count as commentCount, ", // 添加别名
            "like_count as likeCount, ",      // 添加别名
            "hate_count as hateCount, ",      // 添加别名
            "bookmark_count as bookmarkCount, ", // 添加别名
            "author, ",
            "user_id as userId, ",            // 添加别名
            "create_by as createBy, ",        // 添加别名
            "create_time as createTime, ",    // 添加别名
            "update_by as updateBy, ",        // 添加别名
            "update_time as updateTime, ",    // 添加别名
            "remark ",
            "FROM class_article",
            "<where>",
            "<if test='title != null and title != \"\"'> AND title LIKE CONCAT('%', #{title}, '%')</if>",
            "<if test='articleType != null and articleType != \"\"'> AND article_type = #{articleType}</if>",
            "<if test='status != null and status != \"\"'> AND status = #{status}</if>",
            "<if test='author != null and author != \"\"'> AND author LIKE CONCAT('%', #{author}, '%')</if>",
            "<if test='userId != null'> AND user_id = #{userId}</if>",
            "</where>",
            "ORDER BY create_time DESC",
            "</script>"
    })
    List<BbsArticle> selectBbsArticleList(BbsArticle bbsArticle);

    /**
     * 新增文章管理
     */
    @Insert({
            "INSERT INTO class_article (",
            "title, digest, content, cover, article_type, status, view_count, comment_count, like_count, hate_count, bookmark_count, author, user_id, create_by, remark, create_time",
            ") VALUES (",
            "#{title}, #{digest}, #{content}, #{cover}, #{articleType}, #{status}, #{viewCount}, #{commentCount}, #{likeCount}, #{hateCount}, #{bookmarkCount}, #{author}, #{userId}, #{createBy}, #{remark}, sysdate()",
            ")"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertBbsArticle(BbsArticle bbsArticle);

    /**
     * 修改文章管理
     */
    @Update({
            "<script>",
            "UPDATE class_article",
            "<set>",
            "<if test='title != null and title != \"\"'>title = #{title},</if>",
            "<if test='digest != null'>digest = #{digest},</if>",
            "<if test='content != null'>content = #{content},</if>",
            "<if test='cover != null'>cover = #{cover},</if>",
            "<if test='articleType != null'>article_type = #{articleType},</if>",
            "<if test='status != null'>status = #{status},</if>",
            "<if test='viewCount != null'>view_count = #{viewCount},</if>",
            "<if test='commentCount != null'>comment_count = #{commentCount},</if>",
            "<if test='likeCount != null'>like_count = #{likeCount},</if>",
            "<if test='hateCount != null'>hate_count = #{hateCount},</if>",
            "<if test='bookmarkCount != null'>bookmark_count = #{bookmarkCount},</if>",
            "<if test='author != null'>author = #{author},</if>",
            "<if test='userId != null'>user_id = #{userId},</if>",
            "<if test='updateBy != null'>update_by = #{updateBy},</if>",
            "<if test='remark != null'>remark = #{remark},</if>",
            "update_time = sysdate()",
            "</set>",
            "WHERE id = #{id}",
            "</script>"
    })
    int updateBbsArticle(BbsArticle bbsArticle);

    /**
     * 删除文章管理
     */
    @Delete("DELETE FROM class_article WHERE id = #{id}")
    int deleteBbsArticleById(Long id);

    /**
     * 批量删除文章管理
     */
    @Delete({
            "<script>",
            "DELETE FROM class_article WHERE id IN",
            "<foreach collection='array' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int deleteBbsArticleByIds(Long[] ids);

    /**
     * 增加阅读数
     */
    @Update("UPDATE class_article SET view_count = view_count + 1 WHERE id = #{id}")
    int increaseViewCount(Long id);


    /**
     * 点赞文章
     */
    @Update("UPDATE class_article SET like_count = like_count + 1 WHERE id = #{id}")
    int likeArticle(Long id);

    /**
     * 点踩文章
     */
    @Update("UPDATE class_article SET hate_count = hate_count + 1 WHERE id = #{id}")
    int hateArticle(Long id);

    /**
     * 查询热门文章
     */
    @Select({
            "<script>",
            "SELECT id, title, digest, content, cover, article_type, status, view_count, comment_count, like_count, hate_count, bookmark_count, author, user_id, create_by, create_time, update_by, update_time, remark FROM class_article",
            "<where>",
            "<if test='status != null and status != \"\"'> AND status = #{status}</if>",
            "</where>",
            "ORDER BY view_count DESC, like_count DESC LIMIT 10",
            "</script>"
    })
    List<BbsArticle> selectHotArticleList(BbsArticle bbsArticle);
}