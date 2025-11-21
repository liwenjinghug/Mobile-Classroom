package com.ruoyi.proj_myx.domain;

import java.io.Serializable;

/**
 * 投票选项实体
 */
public class VoteOption implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long optionId;
    private Long voteId;
    private String label;
    private String content;
    private Integer sortOrder;
    
    private Integer count; // 统计票数
    
    private java.util.List<String> voterNames; // 投票人姓名列表（非匿名时返回）

    public Long getOptionId() { return optionId; }
    public void setOptionId(Long optionId) { this.optionId = optionId; }

    public Long getVoteId() { return voteId; }
    public void setVoteId(Long voteId) { this.voteId = voteId; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }

    public java.util.List<String> getVoterNames() { return voterNames; }
    public void setVoterNames(java.util.List<String> voterNames) { this.voterNames = voterNames; }
}
