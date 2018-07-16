package com.dingyue.downloadmanager

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ding.basic.bean.Book
import com.dingyue.downloadmanager.contract.BookHelperContract
import com.dingyue.downloadmanager.recl.DownloadManagerAdapter
import kotlinx.android.synthetic.txtqbdzs.item_download_manager_task.view.*
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.constants.ReplaceConstants
import java.text.MessageFormat

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

        if (!(txt_book_name == null || TextUtils.isEmpty(book.name))) {
            txt_book_name.text = book.name
        }

        if (img_book_cover != null) {
            if (!TextUtils.isEmpty(book.img_url) && book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
                Glide.with(context)
                        .load(book.img_url)
                        .placeholder(R.drawable.common_book_cover_default_icon)
                        .error(R.drawable.common_book_cover_default_icon)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(img_book_cover)
            } else {
                Glide.with(context)
                        .load(R.drawable.common_book_cover_default_icon)
                        .into(img_book_cover)
            }
        }

        pgbar_task_progress.max = 100


        val bookTask = CacheManager.getBookTask(book)

        pgbar_task_progress.progress = bookTask.progress

        val state = bookTask.state

        pgbar_task_progress.progressDrawable = resources.getDrawable(R.drawable.download_manager_item_pgbar_second_bg)
        txt_task_state.setTextColor(Color.parseColor("#838181"))

        if (state == DownloadState.DOWNLOADING) {
            txt_task_state.text = context.getString(R.string.status_downloading)
            if (txt_task_progress != null && bookTask.progress >= 0) {
                txt_task_progress.text = MessageFormat.format("{0}%", bookTask.progress)
            }
            pgbar_task_progress.progressDrawable = resources.getDrawable(R.drawable.download_manager_item_pgbar_main_bg)
        } else if (state == DownloadState.WAITTING) {
            txt_task_state.text = context.getString(R.string.status_wait)
            txt_task_progress.text = context.getString(R.string.status_wait_txt)
        } else if (state == DownloadState.PAUSEED) {
            txt_task_state.text = context.getString(R.string.status_pause)
            if (txt_task_progress != null && bookTask.progress >= 0) {
                txt_task_progress.text = MessageFormat.format("暂停: {0}%", bookTask.progress)
            }
        } else if (state == DownloadState.NONE_NETWORK) {
            txt_task_state.text = context.getString(R.string.status_pause)
            if (txt_task_progress != null && bookTask.progress >= 0) {
                txt_task_progress.text = MessageFormat.format("暂停: {0}%", bookTask.progress)
            }
        } else if (state == null || state == DownloadState.NOSTART) {
            pgbar_task_progress.progress = 0
            txt_task_state.text = context.getString(R.string.status_no_cache_txt)
            txt_task_progress.text = context.getString(R.string.status_no_cache)
        } else if (state == DownloadState.FINISH) {
            pgbar_task_progress.progress = 100
            txt_task_state.text = context.getString(R.string.status_finish)
            txt_task_progress.text = context.getString(R.string.status_finish_txt)
        } else if (state == DownloadState.WAITTING_WIFI) {
            txt_task_state.text = context.getString(R.string.status_wifi_require)
            if (txt_task_progress != null && bookTask.progress >= 0) {
                txt_task_progress.text = MessageFormat.format("暂停: {0}%", bookTask.progress)
            }
            txt_task_state.setTextColor(Color.parseColor("#2596C5"))
        } else {
            bookTask.state = DownloadState.NOSTART
            pgbar_task_progress.progress = 0
            txt_task_state.text = context.getString(R.string.status_no_cache_txt)
            txt_task_progress.text = context.getString(R.string.status_no_cache)
        }
        if (contains) {
            img_delete.setImageResource(R.drawable.download_manager_item_check_icon)
        } else {
            img_delete.setImageResource(R.drawable.download_manager_item_uncheck_icon)
        }

        if (remove) {
            img_download_action.isClickable = false
        } else {
            img_download_action.isClickable = true
            img_download_action.setOnClickListener {
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