package com.intelligent.reader.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.intelligent.reader.R

/**
 * Function：搜索书籍Fragment
 *
 * Created by JoannChen on 2018/6/20 0020 14:57
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
class SearchBookFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.frag_search_book, container, false)
}