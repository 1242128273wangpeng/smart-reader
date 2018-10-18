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
import kotlinx.android.synthetic.main.fragment_listview.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.utils.StatServiceUtils

/**
 * Desc 历史记录
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019 22:05
 */
class HistoryFragment : Fragment(), IHistoryView, HistoryAdapter.OnHistoryItemClickListener {

    private var historyAdapter: HistoryAdapter? = null

    var onKeyWordListener: OnKeyWordListener? = null

    private val historyPresenter: HistoryPresenter by lazy {
        HistoryPresenter(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_listview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        historyPresenter.onCreate()
    }

    override fun showLoading() {

    }

    override fun hideLoading() {

    }


    override fun showHistoryRecord(historyList: ArrayList<String>) {
        historyAdapter = HistoryAdapter(requireContext(), historyList, this@HistoryFragment)
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
            data.put("keyword", history)
            data.put("rank", position.toString() + "")
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE, StartLogClickUtil.BARLIST, data)

        }
    }

    override fun onHistoryIndex(index: Int) {
        historyPresenter.removeHistoryRecord(index)
        historyAdapter?.notifyDataSetChanged()
    }

    //加载历史数据
    fun loadHistoryRecord() {
        historyPresenter.loadHistoryRecord()
    }

    override fun clearHistoryResult() {

    }

    override fun onDestroy() {
        super.onDestroy()
        historyPresenter.onDestroy()
    }


}