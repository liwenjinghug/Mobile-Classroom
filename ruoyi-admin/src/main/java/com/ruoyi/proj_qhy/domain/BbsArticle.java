package com.ruoyi.proj_qhy.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 文章管理对象 class_article
 */
public class BbsArticle extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 文章ID */
    private Long id;

    /** 文章标题 */
    @Excel(name = "文章标题")
    private String title;

    /** 文章摘要 */
    @Excel(name = "文章摘要")
    private String digest;

    /** 文章内容 */
    private String content;

    /** 封面图片 */
    @Excel(name = "封面图片")
    private String cover;

    /** 文章分类 */
    @Excel(name = "文章分类")
    private String articleType;

    /** 状态：editting-编辑中,published-已发布,draft-草稿 */
    @Excel(name = "状态")
    private String status;

    /** 阅读数 */
    @Excel(name = "阅读数")
    private Long viewCount;

    /** 评论数 */
    @Excel(name = "评论数")
    private Long commentCount;

    /** 点赞数 */
    @Excel(name = "点赞数")
    private Long likeCount;

    /** 点踩数 */
    @Excel(name = "点踩数")
    private Long hateCount;

    /** 收藏数 */
    @Excel(name = "收藏数")
    private Long bookmarkCount;

    /** 作者 */
    @Excel(name = "作者")
    private String author;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    // getter/setter 方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDigest() { return digest; }
    public void setDigest(String digest) { this.digest = digest; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }

    public String getArticleType() { return articleType; }
    public void setArticleType(String articleType) { this.articleType = articleType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }

    public Long getCommentCount() { return commentCount; }
    public void setCommentCount(Long commentCount) { this.commentCount = commentCount; }

    public Long getLikeCount() { return likeCount; }
    public void setLikeCount(Long likeCount) { this.likeCount = likeCount; }

    public Long getHateCount() { return hateCount; }
    public void setHateCount(Long hateCount) { this.hateCount = hateCount; }

    public Long getBookmarkCount() { return bookmarkCount; }
    public void setBookmarkCount(Long bookmarkCount) { this.bookmarkCount = bookmarkCount; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("title", getTitle())
                .append("digest", getDigest())
                .append("content", getContent())
                .append("cover", getCover())
                .append("articleType", getArticleType())
                .append("status", getStatus())
                .append("viewCount", getViewCount())
                .append("commentCount", getCommentCount())
                .append("likeCount", getLikeCount())
                .append("hateCount", getHateCount())
                .append("bookmarkCount", getBookmarkCount())
                .append("author", getAuthor())
                .append("userId", getUserId())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}