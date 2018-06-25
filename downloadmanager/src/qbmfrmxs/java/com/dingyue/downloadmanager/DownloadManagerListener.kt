package com.dingyue.downloadmanager

/**
 * Desc 请描述这个文件
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/6/20 17:37
 */
interface DownloadManagerListener {

    fun navigationBookStore()

    fun changeRemoveViewState(show: Boolean)

    fun changeSelectAllContent(content: String)
}