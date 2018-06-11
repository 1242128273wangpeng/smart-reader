package com.ding.basic.bean;

import java.util.List;

/**
 * @author lijun Lee
 * @desc 搜索 API Result
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/12/5 11:54
 */

public class SearchResult {

    private List<HotWordBean> hotWords;

    private List<SearchOperations> operations;

    public List<HotWordBean> getHotWords() {
        return hotWords;
    }

    public void setHotWords(List<HotWordBean> hotWords) {
        this.hotWords = hotWords;
    }

    public List<SearchOperations> getOperations() {
        return operations;
    }

    public void setOperations(List<SearchOperations> operations) {
        this.operations = operations;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "hotWords=" + hotWords +
                ", operations=" + operations +
                '}';
    }
}
