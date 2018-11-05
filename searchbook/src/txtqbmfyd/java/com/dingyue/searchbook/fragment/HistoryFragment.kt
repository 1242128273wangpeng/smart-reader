package com.dingyue.searchbook.fragment

import android.view.View
import com.dingyue.searchbook.R
import com.dingyue.statistics.DyStatService
import kotlinx.android.synthetic.main.fragment_listview.*
import net.lzbook.kit.pointpage.EventPoint
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

    override fun addFooterView() {
        historyDeleteView = View.inflate(context, R.layout.item_history_delete_layout, null)
        historyDeleteView?.setOnClickListener {
            StatServiceUtils.statAppBtnClick(context, StatServiceUtils.b_search_click_his_clear)
            DyStatService.onEvent(EventPoint.SEARCH_BARCLEAR)
            showClearHistoryDialog()
        }
        listView.addFooterView(historyDeleteView)
    }

    override fun showHistoryRecord(historyList: ArrayList<String>) {
        super.showHistoryRecord(historyList)
        historyDeleteView?.visibility = if (historyList.size > 0) View.VISIBLE else View.GONE

    }

    /**
     * 清除搜索历史记录
     */
    private fun showClearHistoryDialog() {

        val dialog = ConfirmDialog(requireActivity())
        dialog.setTitle(requireActivity().getString(R.string.prompt))
        dialog.setContent(requireActivity().getString(R.string.determine_clear_serach_history))
        dialog.setOnConfirmListener {
            DyStatService.onEvent(EventPoint.SEARCH_HISTORYCLEAR, mapOf("type" to "1"))
            historyPresenter.removeHistoryRecord()
            historyAdapter?.notifyDataSetChanged()
            dialog.dismiss()
        }


        dialog.setOnCancelListener {
            DyStatService.onEvent(EventPoint.SEARCH_HISTORYCLEAR, mapOf("type" to "0"))
            dialog.dismiss()
        }

        dialog.show()

    }

}