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
import kotlinx.android.synthetic.txtqbmfxs.item_download_manager_task.view.*
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.book.download.DownloadState
import net.lzbook.kit.constants.ReplaceConstants
import net.lzbook.kit.utils.loge

class DownloadManagerTaskHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(book: Book, downloadManagerItemListener: DownloadManagerAdapter.DownloadManagerItemListener, contains: Boolean, remove: Boolean) = with(itemView) {

        if (remove) {
            rl_content.translationX = 140F
            rl_check.visibility = View.VISIBLE
        } else {
            rl_content.translationX = 0F
            rl_check.visibility = View.GONE
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
        if (img_book != null) {

            if (!TextUtils.isEmpty(book.img_url) && book.img_url != ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL) {
                Glide.with(context)
                        .load(book.img_url)
                        .placeholder(R.drawable.common_book_cover_default_icon)
                        .error(R.drawable.common_book_cover_default_icon)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(img_book)
            } else {
                Glide.with(context)
                        .load(R.drawable.common_book_cover_default_icon)
                        .into(img_book)
            }
        }
        txt_download_progress.visibility = View.GONE
        if (txt_download_progress != null && task.progress > 0) {
            txt_download_progress.visibility = View.VISIBLE
            val progressStr = " : " + task.progress + "%"
            txt_download_progress.text = progressStr
        }
        txt_download_pgbar.max = 100
        txt_download_pgbar.progress = task.progress
        val state = task.state
        txt_download_pgbar.progressDrawable = resources.getDrawable(R.drawable.download_manager_item_pgbar_normal_bg)
        img_download.setImageResource(R.drawable.download_manager_item_download_icon)
        txt_download_state.setTextColor(Color.parseColor("#838181"))
        if (state == DownloadState.DOWNLOADING) {
            txt_download_state.text = context.getString(R.string.status_downloading)
            img_download.setImageResource(R.drawable.download_manager_item_downloading_icon)
            txt_download_pgbar.progressDrawable = resources.getDrawable(R.drawable.download_manager_item_pgbar_normal_bg)
        } else if (state == DownloadState.WAITTING) {
            txt_download_state.text = context.getString(R.string.status_wait)
            img_download.setImageResource(R.drawable.download_manager_item_wait_icon)
        } else if (state == DownloadState.PAUSEED) {
            txt_download_state.text = context.getString(R.string.status_pause)
        } else if (state == DownloadState.NONE_NETWORK) {
            txt_download_state.text = context.getString(R.string.status_pause)
        } else if (state == null || state == DownloadState.NOSTART) {
            txt_download_state.text = context.getString(R.string.status_no_cache)
            txt_download_pgbar.progress = 0
        } else if (state == DownloadState.FINISH) {
            txt_download_state.text = context.getString(R.string.status_finish)
            img_download.setImageResource(R.drawable.download_manager_item_finish_icon)
            txt_download_pgbar.progress = 100
            txt_download_pgbar.progressDrawable = resources.getDrawable(R.drawable.download_manager_item_pgbar_finish_bg)
        } else if (state == DownloadState.WAITTING_WIFI) {
            txt_download_state.text = context.getString(R.string.status_wifi_require)
            txt_download_state.setTextColor(Color.parseColor("#2566C5"))
        } else {
            task.state = DownloadState.NOSTART
            txt_download_state.text = context.getString(R.string.status_no_cache)
            txt_download_pgbar.progress = 0
        }
        if (contains) {
            img_check.setImageResource(R.drawable.download_manager_item_check_icon)
        } else {
            img_check.setImageResource(R.drawable.download_manager_item_uncheck_icon)
        }

        if (remove) {
            img_download.isClickable = false
        } else {
            img_download.isClickable = true
            img_download.setOnClickListener{
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