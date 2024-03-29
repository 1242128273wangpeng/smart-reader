package com.dy.reader.view

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.ding.basic.bean.FontData
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.dingyue.statistics.DyStatService
import com.dy.reader.R
import com.dy.reader.adapter.FontAdapter
import com.dy.reader.helper.DrawTextHelper
import com.dy.reader.service.FontDownLoadService
import com.dy.reader.setting.ReaderSettings
import com.dy.reader.util.TypefaceUtil
import kotlinx.android.synthetic.txtqbmfxs.reader_option_font_layout.view.*
import net.lzbook.kit.pointpage.EventPoint
import net.lzbook.kit.ui.widget.base.BasePopup
import net.lzbook.kit.utils.loge
import net.lzbook.kit.utils.toast.ToastUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * Function：字体下载弹框
 *
 * Created by JoannChen on 2018/9/10 0010 22:02
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
class FontPopupWindow(context: Context, layout: Int = R.layout.reader_option_font_layout,
                      width: Int = WindowManager.LayoutParams.MATCH_PARENT,
                      height: Int = WindowManager.LayoutParams.WRAP_CONTENT)
    : BasePopup(context, layout, width, height) {

    private val fontList = arrayListOf<FontData>()
    private val fontAdapter = FontAdapter(fontList)
    private val fontDownLoadService = FontDownLoadService()

    var fontProgressMap: HashMap<String, Int>? = null
        get() {
            if (field == null) {
                field = HashMap()
            }
            return field
        }

    init {

        fontList.add(FontData(FontDownLoadService.FONT_DEFAULT, null, "", 100))
        fontList.add(FontData(FontDownLoadService.FONT_SIYUAN_SONG, R.drawable.font_2, "11.3M"))
        fontList.add(FontData(FontDownLoadService.FONT_ZHUSHITI, R.drawable.font_4, "4.3M"))
        fontList.add(FontData(FontDownLoadService.FONT_SIYUAN_HEI, R.drawable.font_5, "16.6M"))

        contentView.recyclerView.adapter = fontAdapter
        val itemAnimator = contentView.recyclerView.itemAnimator
        if (itemAnimator is SimpleItemAnimator) {
            itemAnimator.supportsChangeAnimations = false //解决 notifyItemChanged 闪烁
        }

        fontAdapter.onItemClickListener = { data, position ->
            if (data.progress == 100) {
                if (data.name != FontDownLoadService.FONT_DEFAULT
                        && !FontDownLoadService.isFontExists(data.name)) {
                    data.progress = -1
                } else {
                    val typeface = TypefaceUtil.getTypefaceCode(data.name)
                    ReaderSettings.instance.fontTypeface = typeface


                    TypefaceUtil.loadTypeface(typeface)?.let {
                        DrawTextHelper.setTypeFace(it)
                    }
                    SPUtils.putDefaultSharedString(SPKey.READER_TYPE_FACE, data.name)

                    uploadUseFontLog(typeface)
                }
                fontAdapter.notifyDataSetChanged()
            } else if (data.progress == -1) {
                if (fontProgressMap?.containsKey(data.name) == false) {
                    fontDownLoadService.start(context, data.name, position)
                    uploadDownloadFontLog(data.name)
                }
            }
        }
    }

    private fun uploadUseFontLog(typeface: Int) {
        DyStatService.onEvent(EventPoint.READPAGESET_FONTSETTING,
                mapOf(Pair("type", TypefaceUtil.loadTypefaceTag(typeface))))
    }

    private fun uploadDownloadFontLog(name: String) {
        val typeface = TypefaceUtil.getTypefaceCode(name)
        DyStatService.onEvent(EventPoint.READPAGESET_FONTDOWNLOAD,
                mapOf(Pair("type", TypefaceUtil.loadTypefaceTag(typeface))))
    }

    fun show(parent: View) {
        EventBus.getDefault().register(this)
        if (ReaderSettings.instance.isLandscape) {
            contentView.recyclerView.layoutManager = GridLayoutManager(context, 2)
        } else {
            contentView.recyclerView.layoutManager = LinearLayoutManager(context)
        }

        fontList.forEachIndexed { index, fontData ->
            if (index == 0) return@forEachIndexed
            val name = fontData.name
            when {
                FontDownLoadService.isFontExists(name) -> fontData.progress = 100
                fontProgressMap?.containsKey(name) == true -> fontData.progress = fontProgressMap?.get(name) ?: -1
                else -> fontData.progress = -1
            }
        }

        fontAdapter.notifyDataSetChanged()

        showAtLocation(parent, Gravity.BOTTOM)
    }

    override fun dismiss() {
        super.dismiss()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEvent(event: FontDownLoadService.Event) {
        if (fontList.isEmpty() || event.fontPosition >= fontList.size) return

        when (event.status) {
            FontDownLoadService.STATUS_DOWNLOADING -> {
                fontProgressMap?.put(event.fontName, event.progress)

                val font = fontList[event.fontPosition]
                if (event.progress == 100) return
                loge("popup progress: ${event.progress} + position:${event.fontPosition}")
                font.progress = event.progress
                fontAdapter.notifyItemChanged(event.fontPosition)
            }
            FontDownLoadService.STATUS_FINISH -> {
                fontProgressMap?.remove(event.fontName)

                val font = fontList[event.fontPosition]
                loge("popup progress: ${event.progress}")
                font.progress = 100
                fontAdapter.notifyItemChanged(event.fontPosition)
            }
            FontDownLoadService.STATUS_ERROR -> {
                fontProgressMap?.remove(event.fontName)

                val font = fontList[event.fontPosition]
                loge("popup progress: ${event.progress}")
                font.progress = -1
                fontAdapter.notifyItemChanged(event.fontPosition)
                ToastUtil.showToastMessage("下载字体失败")
            }
        }
    }

}