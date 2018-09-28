package com.ding.basic.bean

import android.text.TextUtils
import java.io.Serializable

/**
 * Desc 真假书籍book_id切换
 * Author crazylei
 * Mail crazylei911228@gmail.com
 * Date 2018/8/15 18:04
 */
class FakeBook : Serializable {

    var from: String = ""

    var to: String = ""

    fun checkValueValid(): Boolean {
        return !TextUtils.isEmpty(this.from) && !TextUtils.isEmpty(this.to)
    }
}