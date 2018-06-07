package com.ding.basic.bean;

import com.google.gson.annotations.Expose;

/**
 * @author lijun Lee
 * @desc 搜索热词
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/12/4 14:18
 */

public class HotWordBean {

    @Expose
    public static final String NEW_TAG = "新";
    @Expose
    public static final String HOT_TAG = "热";
    @Expose
    public static final String RECOMMEND_TAG = "荐";

    private int keywordType;
    private String keyword;
    private String superscript;
    private String color;
    private int sort;

    public int getKeywordType() {
        return keywordType;
    }

    public void setKeywordType(int keywordType) {
        this.keywordType = keywordType;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getSuperscript() {
        return superscript;
    }

    public void setSuperscript(String superscript) {
        this.superscript = superscript;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    @Override
    public String toString() {
        return "HotWordBean{" +
                "keywordType=" + keywordType +
                ", keyword='" + keyword + '\'' +
                ", superscript='" + superscript + '\'' +
                ", color='" + color + '\'' +
                ", sort=" + sort +
                '}';
    }
}
