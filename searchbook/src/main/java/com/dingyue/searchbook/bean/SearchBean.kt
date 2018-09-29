package com.dingyue.searchbook.bean


/**
 * Desc：跳转搜索页携带的参数
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/9/28 0028 14:38
 */
class SearchBean {

    var word: String = ""
    var searchType = "0"
    var filterType = "0"
    var filterWord = "ALL"
    var sortType = "0"
    var fromClass: String = "" // 判断是从哪个页面进入搜索页

}