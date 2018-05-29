package com.dingyue.bookshelf.contract

import net.lzbook.kit.data.UpdateCallBack
import net.lzbook.kit.data.bean.BookUpdateTaskData
import net.lzbook.kit.utils.BaseBookHelper
import java.util.ArrayList

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/8 09:50
 */
object BookHelperContract {

    fun loadBookUpdateTaskData(list: ArrayList<Book>, updateCallBack: UpdateCallBack): BookUpdateTaskData {
        return BaseBookHelper.getBookUpdateTaskData(list, updateCallBack)
    }

}