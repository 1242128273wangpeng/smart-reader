package com.dingyue.downloadmanager

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dingyue.downloadmanager.contract.BookHelperContract
import com.dingyue.downloadmanager.recl.DownloadManagerAdapter
import kotlinx.android.synthetic.qbmfxsydq.item_download_manager_task.view.*
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.data.bean.Book
import net.lzbook.kit.utils.loge
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

        if (!(txt_book_name == null || TextUtils.isEmpty(book.name))) {
            txt_book_name.text = book.name
        }

        if (img_cover != null) {
            if (!TextUtils.isEmpty(book.img_url) && book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
                Glide.with(context)
                        .load(book.img_url)
                        .placeholder(R.drawable.icon_book_cover_default)
                        .error(R.drawable.icon_book_cover_default)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(img_cover)
            } else {
                Glide.with(context)
                        .load(R.drawable.icon_book_cover_default)
                        .into(img_cover)
            }
        }

        txt_task_progress.visibility = View.GONE

        val bookTask = CacheManager.getBookTask(book)

        if (txt_task_progress != null && bookTask.progress > 0) {
            txt_task_progress.visibility = View.VISIBLE
            txt_task_progress.text = MessageFormat.format(" : {0}%", bookTask.progress)
        }

        pgbar_task_progress.max = 100
        pgbar_task_progress.progress = bookTask.progress

        val downloadState = bookTask.state

        txt_task_state.setTextColor(Color.parseColor("#838181"))
        img_download_action.setImageResource(R.drawable.download_manager_item_download_icon)
        pgbar_task_progress.progressDrawable = resources.getDrawable(R.drawable.download_manager_item_pgbar_second_bg)

        if (downloadState == DownloadState.DOWNLOADING) {
            txt_task_state.text = context.getString(R.string.status_downloading)
            img_download_action.setImageResource(R.drawable.download_manager_item_downloading_icon)
            pgbar_task_progress.progressDrawable = resources.getDrawable(R.drawable.download_manager_item_pgbar_main_bg)
        } else if (downloadState == DownloadState.WAITTING) {
            txt_task_state.text = context.getString(R.string.status_wait)
            img_download_action.setImageResource(R.drawable.download_manager_item_wait_icon)
        } else if (downloadState == DownloadState.PAUSEED) {
            txt_task_state.text = context.getString(R.string.status_pause)
        } else if (downloadState == DownloadState.NONE_NETWORK) {
            txt_task_state.text = context.getString(R.string.status_pause)
        } else if (downloadState == null || downloadState == DownloadState.NOSTART) {
            txt_task_state.text = context.getString(R.string.status_no_cache)
            pgbar_task_progress.progress = 0
        } else if (downloadState == DownloadState.FINISH) {
            txt_task_state.text = context.getString(R.string.status_no_cache)
            img_download_action.setImageResource(R.drawable.download_manager_item_finish_icon)
            pgbar_task_progress.progress = 100
        } else if (downloadState == DownloadState.WAITTING_WIFI) {
            txt_task_state.text = context.getString(R.string.status_wifi_require)
            txt_task_state.setTextColor(Color.parseColor("#2566C5"))
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
                loge("download" + book.book_id)
                val status = CacheManager.getBookStatus(book)
                if (status == DownloadState.DOWNLOADING || status == DownloadState.WAITTING) {
                    CacheManager.stop(book.book_id)
                } else {
                    BookHelperContract.startDownBookTask(context, book, 0)
                }
                DownloadManagerLogger.uploadItemClickLog(status, book.book_id, bookTask.progress)
            }
        }
    }
}