package com.dingyue.downloadmanager

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import com.dingyue.downloadmanager.contract.BookHelperContract
import com.dingyue.downloadmanager.recl.DownloadManagerAdapter
import kotlinx.android.synthetic.zsmfqbxs.item_download_manager_task.view.*
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.utils.loge

class DownloadManagerTaskHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(book: Book, downloadManagerItemListener: DownloadManagerAdapter.DownloadManagerItemListener,
             contains: Boolean, remove: Boolean) = with(itemView) {

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

        val task = CacheManager.getBookTask(book)

        if (!(txt_book_name == null || TextUtils.isEmpty(book.name))) {
            txt_book_name.text = book.name
        }
        txt_task_progress.visibility = View.GONE
        if (txt_task_progress != null && task.progress > 0) {
            txt_task_progress.visibility = View.VISIBLE
            val progressStr = " :  " + task.progress + "%"
            txt_task_progress.text = progressStr
        }

        pgbar_task_progress.max = 100
        pgbar_task_progress.progress = task.progress

        val state = task.state

        pgbar_task_progress.progressDrawable = resources.getDrawable(R.drawable.download_manager_item_pgbar_second_bg)

        img_task_action.setImageResource(R.drawable.download_manager_item_download_icon)

        txt_task_state.setTextColor(Color.parseColor("#838181"))

        if (state == DownloadState.DOWNLOADING) {
            txt_task_state.text = context.getString(R.string.status_downloading)
            img_task_action.setImageResource(R.drawable.download_manager_item_downloading_icon)
            pgbar_task_progress.progressDrawable = resources.getDrawable(R.drawable.download_manager_item_pgbar_main_bg)
        } else if (state == DownloadState.WAITTING) {
            txt_task_state.text = context.getString(R.string.status_wait)
            img_task_action.setImageResource(R.drawable.download_manager_item_wait_icon)
        } else if (state == DownloadState.PAUSEED) {
            txt_task_state.text = context.getString(R.string.status_pause)
        } else if (state == DownloadState.NONE_NETWORK) {
            txt_task_state.text = context.getString(R.string.status_pause)
        } else if (state == null || state == DownloadState.NOSTART) {
            txt_task_state.text = context.getString(R.string.status_no_cache)
            pgbar_task_progress.progress = 0
        } else if (state == DownloadState.FINISH) {
            txt_task_state.text = context.getString(R.string.status_finish)
            img_task_action.setImageResource(R.drawable.download_manager_item_finish_icon)
            pgbar_task_progress.progress = 100
        } else if (state == DownloadState.WAITTING_WIFI) {
            txt_task_state.text = context.getString(R.string.status_wifi_require)
            txt_task_state.setTextColor(Color.parseColor("#2566C5"))
        } else {
            task.state = DownloadState.NOSTART
            txt_task_state.text = context.getString(R.string.status_no_cache)
            pgbar_task_progress.progress = 0
        }

        if (contains) {
            img_delete.setImageResource(R.drawable.download_manager_item_check_icon)
        } else {
            img_delete.setImageResource(R.drawable.download_manager_item_uncheck_icon)
        }

        if (remove) {
            img_task_action.isClickable = false
        } else {
            img_task_action.isClickable = true
            img_task_action.setOnClickListener{
                loge("download" + book.book_id)
                val status = CacheManager.getBookStatus(book)
                if (status == DownloadState.DOWNLOADING || status == DownloadState.WAITTING) {
                    CacheManager.stop(book.book_id)
                } else {
                    BookHelperContract.startDownBookTask(context, book, 0)
                }
                DownloadManagerLogger.uploadCacheManagerButtonClick(status, book.book_id, task.progress)
            }
        }
    }
}