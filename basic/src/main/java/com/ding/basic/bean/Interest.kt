package com.ding.basic.bean

/**
 * Desc 兴趣实体类
 * Author jiaxing_sun
 * Mail jiaxing_sun@dingyuegroup.cn
 * Date 2018/10/12 14:20
 */
data class Interest(var name: String) {
    var type = 0 // 兴趣类型（等级，一级分类、二级分类...）
    var selected = false
}