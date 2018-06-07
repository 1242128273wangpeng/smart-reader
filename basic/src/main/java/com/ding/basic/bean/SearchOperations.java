package com.ding.basic.bean;

/**
 * @author lijun Lee
 * @desc 搜索运营模块
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/12/5 11:55
 */

public class SearchOperations {

    private String imgUrl;
    private String title;
    private int type;
    private String bookId;
    private int vip;
    private int bookType;
    private String webviewUrl;
    private int sort;

    private String bookSourceId;
    private String bookName;
    private String authorName;
    private String host;
    private String parameter;
    private String extraParameter;
    private String updateType;

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public int getVip() {
        return vip;
    }

    public void setVip(int vip) {
        this.vip = vip;
    }

    public int getBookType() {
        return bookType;
    }

    public void setBookType(int bookType) {
        this.bookType = bookType;
    }

    public String getWebviewUrl() {
        return webviewUrl;
    }

    public void setWebviewUrl(String webviewUrl) {
        this.webviewUrl = webviewUrl;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getBookSourceId() {
        return bookSourceId;
    }

    public void setBookSourceId(String bookSourceId) {
        this.bookSourceId = bookSourceId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getExtraParameter() {
        return extraParameter;
    }

    public void setExtraParameter(String extraParameter) {
        this.extraParameter = extraParameter;
    }

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    @Override
    public String toString() {
        return "SearchOperations{" +
                "imgUrl='" + imgUrl + '\'' +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", bookId='" + bookId + '\'' +
                ", vip=" + vip +
                ", bookType=" + bookType +
                ", webviewUrl='" + webviewUrl + '\'' +
                ", sort=" + sort +
                ", bookSourceId='" + bookSourceId + '\'' +
                ", bookName='" + bookName + '\'' +
                ", authorName='" + authorName + '\'' +
                ", host='" + host + '\'' +
                ", parameter='" + parameter + '\'' +
                ", extraParameter='" + extraParameter + '\'' +
                ", updateType='" + updateType + '\'' +
                '}';
    }
}
