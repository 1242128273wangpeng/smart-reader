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
import kotlinx.android.synthetic.txtqbmfyd.fragment_listview.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.ui.widget.ConfirmDialog
import net.lzbook.kit.utils.StatServiceUtils

/**
 * Desc 历史记录
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019 22:05
 */
class HistoryFragment : Fragment(), IHistoryView, HistoryAdapter.OnHistoryItemClickListener {

    private var historyDeleteView: View? = null
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
        initHistoryDeleteView()
    }

    override fun showLoading() {

    }

    override fun hideLoading() {

    }


    override fun showHistoryRecord(historyList: ArrayList<String>) {

        historyDeleteView?.visibility = if (historyList.size > 0) View.VISIBLE else View.GONE
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

    //加载历史数据
    fun loadHistoryRecord() {
        historyPresenter.loadHistoryRecord()
    }

    override fun clearHistoryResult() {

    }

    /**
     * 清除搜索历史记录
     */
    private fun showClearHistoryDialog() {

        val dialog = ConfirmDialog(requireActivity())
        dialog.setTitle(requireActivity().getString(R.string.prompt))
        dialog.setContent(requireActivity().getString(R.string.determine_clear_serach_history))
        dialog.setOnConfirmListener({
            val data = java.util.HashMap<String, String>()
            data.put("type", "1")
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH, StartLogClickUtil.HISTORYCLEAR, data)
            historyPresenter.removeHistoryRecord()
            historyAdapter?.notifyDataSetChanged()
            dialog.dismiss()
        })


        dialog.setOnCancelListener({
            val data = java.util.HashMap<String, String>()
            data.put("type", "0")
            StartLogClickUtil.upLoadEventLog(activity, StartLogClickUtil.SEARCH_PAGE,
                    StartLogClickUtil.HISTORYCLEAR, data)
            dialog.dismiss()
        })

        dialog.show()

    }


    /**
     * 初始化删除历史记录方法
     */
    private fun initHistoryDeleteView() {
        historyDeleteView = View.inflate(context, R.layout.item_history_delete_layout, null)
        historyDeleteView?.setOnClickListener {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.b_search_click_his_clear)
            StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.SEARCH, StartLogClickUtil.BARCLEAR)
            showClearHistoryDialog()
        }
        listView.addFooterView(historyDeleteView)
    }

    override fun onDestroy() {
        super.onDestroy()
        historyPresenter.onDestroy()
    }


}