package com.dingyue.downloadmanager

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ding.basic.bean.Book
import com.dingyue.downloadmanager.contract.BookHelperContract
import com.dingyue.downloadmanager.recl.DownloadManagerAdapter
import kotlinx.android.synthetic.txtqbmfyd.item_download_manager_task.view.*
import net.lzbook.kit.utils.download.CacheManager
import net.lzbook.kit.utils.download.DownloadState
import net.lzbook.kit.constants.ReplaceConstants

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

        if (!TextUtils.isEmpty(book.img_url) && book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
            Glide.with(itemView.context)
                    .load(book.img_url)
                    .placeholder(R.drawable.common_book_cover_default_icon)
                    .error(R.drawable.common_book_cover_default_icon)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(img_cover)
        } else {
            Glide.with(itemView.context)
                    .load(R.drawable.common_book_cover_default_icon)
                    .into(img_cover)
        }
        val task = CacheManager.getBookTask(book)

        if (!(txt_book_name == null || TextUtils.isEmpty(book.name))) {
            txt_book_name.text = book.name
        }

        txt_download_num.visibility = View.GONE

        if (txt_download_num != null && task.progress > 0) {
            txt_download_num.visibility = View.VISIBLE
            val progressStr = ": " + task.progress + "%"
            txt_download_num.text = progressStr
        }

        pgbar_download.max = 100
        pgbar_download.progress = task.progress

        val state = task.state
        pgbar_download.progressDrawable = resources.getDrawable(R.drawable.download_manager_item_pgbar_second_bg)
        img_download.setImageResource(R.drawable.download_manager_item_download_icon)

        if (state == DownloadState.DOWNLOADING) {
            txt_download_state.text = context.getString(R.string.status_downloading)
            img_download.setImageResource(R.drawable.download_manager_item_pause_icon)
            pgbar_download.progressDrawable = resources.getDrawable(R.drawable.download_manager_item_pgbar_main_bg)
        } else if (state == DownloadState.WAITTING) {
            txt_download_state.text = context.getString(R.string.status_wait)
            img_download.setImageResource(R.drawable.download_manager_item_wait_icon)
            txt_download_num.visibility = View.GONE
        } else if (state == DownloadState.PAUSEED) {
            txt_download_state.text = context.getString(R.string.status_pause)
        } else if (state == DownloadState.NONE_NETWORK) {
            txt_download_state.text = context.getString(R.string.status_pause)
        } else if (state == null || state == DownloadState.NOSTART) {
            txt_download_state.text = context.getString(R.string.status_no_cache)
            pgbar_download.progress = 0
        } else if (state == DownloadState.FINISH) {
            txt_download_state.text = context.getString(R.string.status_finish)
            img_download.setImageResource(R.drawable.download_manager_item_finish_icon)
            pgbar_download.progress = 100
            txt_download_num.visibility = View.GONE
            pgbar_download.progressDrawable = resources.getDrawable(R.drawable.download_manager_item_pgbar_third_bg)
        } else if (state == DownloadState.WAITTING_WIFI) {
            txt_download_state.text = context.getString(R.string.status_wifi_require)
            txt_download_num.visibility = View.GONE
        } else {
            task.state = DownloadState.NOSTART
            txt_download_state.text = context.getString(R.string.status_no_cache)
            pgbar_download.progress = 0
        }

        if (contains) {
            img_delete.setImageResource(R.drawable.download_manager_item_check_icon)
        } else {
            img_delete.setImageResource(R.drawable.download_manager_item_uncheck_icon)
        }

        if (remove) {
            img_download.isClickable = false
        } else {
            img_download.isClickable = true
            img_download.setOnClickListener {
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