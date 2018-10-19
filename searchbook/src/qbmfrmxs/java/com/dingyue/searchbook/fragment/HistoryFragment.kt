package com.dingyue.searchbook.fragment

import android.view.Gravity
import android.view.View
import com.dingyue.searchbook.R
import kotlinx.android.synthetic.main.fragment_listview.*
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.ui.widget.ConfirmDialog
import net.lzbook.kit.utils.StatServiceUtils

/**
 * Desc 历史记录
 * Author JoannChen
 * Mail yongzuo_chen@dingyuegroup.cn
 * Date 2018/9/19 0019 22:05
 */
class HistoryFragment : BaseHistoryFragment() {

    private var historyDeleteView: View? = null

    override fun addHeaderView() {
        historyDeleteView = View.inflate(context, R.layout.item_history_delete_layout, null)
        historyDeleteView?.setOnClickListener {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.b_search_click_his_clear)
            StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.SEARCH, StartLogClickUtil.BARCLEAR)
            showClearHistoryDialog()
        }
        listView.addHeaderView(historyDeleteView)
    }

    override fun showHistoryRecord(historyList: ArrayList<String>) {
        super.showHistoryRecord(historyList)
        historyDeleteView?.visibility = if (historyList.size > 0) View.VISIBLE else View.GONE
    }

    override fun onHistoryIndex(index: Int) {
        historyPresenter.removeHistoryRecord(index)
        historyAdapter?.notifyDataSetChanged()
    }


    /**
     * 清除搜索历史记录
     */
    private fun showClearHistoryDialog() {

        val dialog = ConfirmDialog(requireActivity(), Gravity.CENTER)
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

}