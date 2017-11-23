package com.intelligent.reader.presenter.downloadmanager

import net.lzbook.kit.data.bean.Book

/**
 * Created by qiantao on 2017/11/22 0022
 */
interface DownloadManagerView {

    fun onDownloadBookQuery(bookList: ArrayList<Book>, hasDeleted: Boolean)

    fun onDownloadDelete(isDeleteOfShelf: Boolean)
}