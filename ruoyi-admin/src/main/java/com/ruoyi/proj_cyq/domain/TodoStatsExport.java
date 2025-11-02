package com.ruoyi.proj_cyq.domain;

import com.ruoyi.common.annotation.Excel;

public class TodoStatsExport {

    @Excel(name = "分类")
    private String category;

    @Excel(name = "项目")
    private String name;

    @Excel(name = "数量")
    private Integer value;

    @Excel(name = "占比")
    private String percentage;

    public TodoStatsExport() {
    }

    public TodoStatsExport(String category, String name, Integer value, String percentage) {
        this.category = category;
        this.name = name;
        this.value = value;
        this.percentage = percentage;
    }

    // getter 和 setter 方法
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }
}