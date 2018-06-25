package com.dingyue.downloadmanager

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ding.basic.bean.Book
import com.dingyue.downloadmanager.recl.DownloadManagerAdapter

/**
 * Function：已缓存/未缓存
 *
 * Created by JoannChen on 2018/6/21 0021 18:22
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
class DownloadFragment : Fragment() {



//    private var downloadBooks: ArrayList<Book> = ArrayList()
//
//    private val downloadManagerAdapter: DownloadManagerAdapter by lazy {
//        DownloadManagerAdapter(this, this, downloadBooks)
//    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frag_download, container, false)
    }



}