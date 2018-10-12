package com.dingyue.searchbook.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dingyue.searchbook.R
import com.dingyue.searchbook.interfaces.OnKeyWordListener
import com.dingyue.searchbook.interfaces.OnResultListener
import kotlinx.android.synthetic.qbzsydq.fragment_hot_history.*


/**
 * Desc：热词和历史记录
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/10/11 0011 22:44
 */
class HotAndHisFragment : Fragment() {

    // 历史记录回调
    var onKeyWordListener: OnKeyWordListener? = null

    // 热词回调
    var onResultListener: OnResultListener<String>? = null

    private val hotWordFragment: HotWordFragment by lazy {
        HotWordFragment()
    }

    private val historyFragment: HistoryFragment by lazy {
        HistoryFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_hot_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager.beginTransaction().add(R.id.search_hot_layout, hotWordFragment).commit()
        childFragmentManager.beginTransaction().add(R.id.search_his_layout, historyFragment).commit()

        historyFragment.onKeyWordListener = onKeyWordListener
        hotWordFragment.onResultListener = onResultListener

        search_his_layout.postDelayed({
            historyFragment.loadHistoryRecord()
        }, 100)

    }

    fun loadHistoryRecord() {
        historyFragment.loadHistoryRecord()
    }
}