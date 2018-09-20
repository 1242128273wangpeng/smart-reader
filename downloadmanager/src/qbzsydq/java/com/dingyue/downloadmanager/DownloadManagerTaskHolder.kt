package com.dingyue.downloadmanager

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import com.ding.basic.bean.Book
import com.dingyue.downloadmanager.contract.BookHelperContract
import com.dingyue.downloadmanager.recl.DownloadManagerAdapter
import kotlinx.android.synthetic.qbzsydq.item_download_manager_task.view.*
import net.lzbook.kit.utils.download.CacheManager
import net.lzbook.kit.utils.download.DownloadState


import java.text.MessageFormat

class DownloadManagerTaskHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(book: Book, downloadManagerItemListener: DownloadManagerAdapter.DownloadManagerItemListener, contains: Boolean, remove: Boolean) = with(itemView) {

        if (remove) {
            rl_content.translationX = 140F
            rl_delete.visibility = View.VISIBLE
        } else {
            rl_content.translationX = 0F
            rl_delete.visibility = View.GONE
        }

        fl_content.setOnClickListener {
            downloadManagerItemListener.clickedDownloadItem(book, adapterPosition)
        }

        fl_content.setOnLongClickListener {
            downloadManagerItemListener.longClickedDownloadItem()
        }

        val bookTask = CacheManager.getBookTask(book)

        if (!(txt_book_name == null || TextUtils.isEmpty(book.name))) {
            txt_book_name.text = book.name
        }

        txt_task_progress.visibility = View.GONE

        if (txt_task_progress != null && bookTask.progress > 0) {
            txt_task_progress.visibility = View.VISIBLE
            txt_task_progress.text = MessageFormat.format(" {0}%", bookTask.progress)
        }

        pgbar_task_progress.max = 100
        pgbar_task_progress.progress = bookTask.progress

        val state = bookTask.state
        pgbar_task_progress.progressDrawable = resources.getDrawable(R.drawable.download_manager_item_pgbar_second_bg)

        img_download_action.setImageResource(R.drawable.download_manager_item_download_icon)
        txt_task_state.setTextColor(Color.parseColor("#ACACAC"))

        if (state == DownloadState.DOWNLOADING) {
            txt_task_state.text = context.getString(R.string.status_downloading)
            img_download_action.setImageResource(R.drawable.download_manager_item_downloading_icon)
            pgbar_task_progress.progressDrawable = resources.getDrawable(R.drawable.download_manager_item_pgbar_main_bg)
        } else if (state == DownloadState.WAITTING) {
            txt_task_state.text = context.getString(R.string.status_wait)
            img_download_action.setImageResource(R.drawable.download_manager_item_wait_icon)
            txt_task_progress.visibility = View.GONE
        } else if (state == DownloadState.PAUSEED) {
            txt_task_state.text = context.getString(R.string.status_pause)
        } else if (state == DownloadState.NONE_NETWORK) {
            txt_task_state.text = context.getString(R.string.status_pause)
        } else if (state == null || state == DownloadState.NOSTART) {
            txt_task_state.text = context.getString(R.string.status_no_cache)
            pgbar_task_progress.progress = 0
        } else if (state == DownloadState.FINISH) {
            txt_task_state.text = context.getString(R.string.status_finish)
            img_download_action.setImageResource(R.drawable.download_manager_item_finish_icon)
            pgbar_task_progress.progress = 100
            txt_task_progress.visibility = View.GONE
        } else if (state == DownloadState.WAITTING_WIFI) {
            txt_task_state.text = context.getString(R.string.status_wifi_require)
            txt_task_state.setTextColor(Color.parseColor("#882F46"))
            txt_task_progress.visibility = View.GONE
        } else {
            bookTask.state = DownloadState.NOSTART
            txt_task_state.text = context.getString(R.string.status_no_cache)
            pgbar_task_progress.progress = 0
        }
        if (contains) {
            img_delete.setImageResource(R.drawable.download_manager_item_checked_icon)
        } else {
            img_delete.setImageResource(R.drawable.download_manager_item_check_icon)
        }

        if (remove) {
            img_download_action.isClickable = false
        } else {
            img_download_action.isClickable = true
            img_download_action.setOnClickListener{
                val status = CacheManager.getBookStatus(book)
                if (status == DownloadState.DOWNLOADING || status == DownloadState.WAITTING) {
                    CacheManager.stop(book.book_id)
                } else {
                    BookHelperContract.startDownBookTask(context, book, 0)
                }
                DownloadManagerLogger.uploadCacheManagerButtonClick(status, book.book_id, bookTask.progress)
            }
        }
    }
}