package com.dingyue.searchbook.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dingyue.searchbook.R
import com.dingyue.searchbook.adapter.HistoryAdapter
import com.dingyue.searchbook.interfaces.OnKeyWordListener
import com.dingyue.searchbook.presenter.HistoryPresenter
import com.dingyue.searchbook.view.IHistoryView
import com.dingyue.statistics.DyStatService
import kotlinx.android.synthetic.main.fragment_listview.*
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.ui.widget.LoadingPage
import net.lzbook.kit.utils.StatServiceUtils


/**
 * Desc：
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/10/19 0019 10:17
 */
abstract class BaseHistoryFragment : Fragment(), IHistoryView, HistoryAdapter.OnHistoryItemClickListener {

    private var loadingPage: LoadingPage? = null

    var onKeyWordListener: OnKeyWordListener? = null

    var historyAdapter: HistoryAdapter? = null

    val historyPresenter: HistoryPresenter by lazy {
        HistoryPresenter(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_listview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        historyPresenter.onCreate()
        addHeaderView()
        addFooterView()
    }

    override fun showLoading() {
        loadingPage = LoadingPage(requireActivity(), search_result_main, LoadingPage.setting_result)
    }

    override fun hideLoading() {
        loadingPage?.onSuccessGone()
    }

    override fun showHistoryRecord(historyList: ArrayList<String>) {
        historyAdapter = HistoryAdapter(requireContext(), historyList, this)
        listView.adapter = historyAdapter
    }

    override fun onHistoryItemClickListener(position: Int, historyList: List<String>?) {

        StatServiceUtils.statAppBtnClick(context,
                StatServiceUtils.b_search_click_his_word)

        if (historyList != null && !historyList.isEmpty() &&
                position > -1 && position < historyList.size) {

            val history = historyList[position]

            onKeyWordListener?.onKeyWord(history)

            val data = HashMap<String, String>()
            data["keyword"] = history
            data["rank"] = position.toString()
            DyStatService.onEvent(EventPoint.SEARCH_BARLIST, data)
        }
    }


    //加载历史数据
    fun loadHistoryRecord() {
        historyPresenter.loadHistoryRecord()
    }

    /**
     * 添加headerView
     */
    open fun addHeaderView() {

    }

    /**
     * 添加footerView
     */
    open fun addFooterView() {

    }


    override fun clearHistoryResult() {

    }

    override fun onDestroy() {
        super.onDestroy()
        historyPresenter.onDestroy()
    }


}