package com.dingyue.bookshelf

import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.utils.StatServiceUtils
import java.util.HashMap

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/11 10:33
 */
object BookShelfLogger {

    fun uploadShelfEditCancelLog() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.SHELFEDIT_PAGE, StartLogClickUtil.CANCLE1)
    }


    fun uploadEditorSelectAllLog(all: Boolean) {
        val data = HashMap<String, String>()
        data["type"] = if (all) "2" else "1"
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.SHELFEDIT_PAGE, StartLogClickUtil.SELECTALL1, data)
    }




    /***
     * 书架排序日志
     * **/
    fun uploadSortingLog(type: Int) {
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(), StatServiceUtils.me_set_cli_shelf_rak_time)
    }
}