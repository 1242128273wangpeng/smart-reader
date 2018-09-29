package com.ding.basic.bean;

import java.io.Serializable;

/**
 * SearchCommonBeanYouHua
 * Created by Administrator on 2017\9\5 0005
 */

public class SearchCommonBeanYouHua implements Serializable {

    private String suggest;
    private String wordtype;
    private String pv;
    private String image_url;

    //以下字段为书名所需字段
    private String host;
    private String book_id;
    private String book_source_id;
    private String name;
    private String author;
    private String parameter;
    private String extra_parameter;
    private String bookType;
    private int isAuthor;

    /**
     * 本地字段：0书籍 1分割线
     */
    public int viewType = 0;

    public int getIsAuthor() {
        return isAuthor;
    }

    public void setIsAuthor(int isAuthor) {
        this.isAuthor = isAuthor;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getSuggest() {
        return suggest;
    }

    public void setSuggest(String suggest) {
        this.suggest = suggest;
    }

    public String getWordtype() {
        return wordtype;
    }

    public void setWordtype(String wordtype) {
        this.wordtype = wordtype;
    }

    public String getPv() {
        return pv;
    }

    public void setPv(String pv) {
        this.pv = pv;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getBook_id() {
        return book_id;
    }

    public void setBook_id(String book_id) {
        this.book_id = book_id;
    }

    public String getBook_source_id() {
        return book_source_id;
    }

    public void setBook_source_id(String book_source_id) {
        this.book_source_id = book_source_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getExtra_parameter() {
        return extra_parameter;
    }

    public void setExtra_parameter(String extra_parameter) {
        this.extra_parameter = extra_parameter;
    }

    public String getBookType() {
        return bookType;
    }

    public void setBookType(String bookType) {
        this.bookType = bookType;
    }

    @Override
    public String toString() {
        return "SearchCommonBean{" +
                "suggest='" + suggest + '\'' +
                ", wordtype='" + wordtype + '\'' +
                ", pv='" + pv + '\'' +
                ", image_url='" + image_url + '\'' +
                '}';
    }
}
