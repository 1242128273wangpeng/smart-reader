package com.ding.basic.bean

import java.io.Serializable

/**
 * Created on 2018/3/19.
 * Created by crazylei.
 */
class CoverRecommend: Serializable {

    var booksZN: ArrayList<Book>? = null
    var booksQG: ArrayList<Book>? = null
    var booksFree: ArrayList<Book>? = null

}