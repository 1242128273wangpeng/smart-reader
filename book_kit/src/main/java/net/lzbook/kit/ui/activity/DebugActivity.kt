package net.lzbook.kit.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.net.Config
import com.ding.basic.net.api.ContentAPI
import com.ding.basic.net.api.MicroAPI
import com.ding.basic.net.api.RequestAPI
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.umeng.message.MessageSharedPrefs
import kotlinx.android.synthetic.main.activity_debug.*
import net.lzbook.kit.R
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.ui.activity.base.BaseCacheableActivity
import net.lzbook.kit.ui.widget.SwitchButton
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.OpenUDID
import net.lzbook.kit.utils.book.LoadDataManager
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.toast.ToastUtil

/**
 * <pre>
 * Function：调试模式
 *
 * Created by JoannChen on 2018/4/19 0019 10:46
 * E-mail:yongzuo_chen@dingyuegroup.cn
 * </pre>
 */
@Route(path = RouterConfig.DEBUG_ACTIVITY)
class DebugActivity : BaseCacheableActivity(), SwitchButton.OnCheckedChangeListener, View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        initView()
    }

    override fun onResume() {
        super.onResume()

        tv_api.text = ("${resources.getString(R.string.debug_api_host)}【${SPUtils.getOnlineConfigSharedString(SPKey.NOVEL_HOST, "")}】")
        tv_web.text = ("${resources.getString(R.string.debug_web_host)}【${SPUtils.getOnlineConfigSharedString(SPKey.WEBVIEW_HOST, "")}】")
        tv_micro.text = ("${resources.getString(R.string.debug_micro_host)}【${SPUtils.getOnlineConfigSharedString(SPKey.UNION_HOST, "")}】")
        tv_micro_content.text = ("${resources.getString(R.string.debug_micro_content_host)}【${SPUtils.getOnlineConfigSharedString(SPKey.CONTENT_HOST, "")}】")

        btn_debug_start_params.isChecked = SPUtils.getOnlineConfigSharedBoolean(SPKey.START_PARAMS, true)
        txt_udid.text = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext())
        if (AppUtils.hasUPush()) {
            txt_device.text = MessageSharedPrefs.getInstance(BaseBookApplication.getGlobalContext()).deviceToken
        } else {
            txt_device.visibility = View.GONE
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_back -> finish()
            R.id.tv_api -> {
                intentHostList(SPKey.NOVEL_HOST)
            }
            R.id.tv_web -> {
                intentHostList(SPKey.WEBVIEW_HOST)
            }
            R.id.tv_micro -> {
                intentHostList(SPKey.UNION_HOST)
            }
            R.id.tv_micro_content -> {
                intentHostList(SPKey.CONTENT_HOST)
            }
            R.id.btn_debug_device_copy -> {
                if (!TextUtils.isEmpty(txt_device.text.toString())) {
                    AppUtils.copyText(txt_device.text.toString(), this)
                    ToastUtil.showToastMessage(R.string.debug_copy_success)
                }

            }
            R.id.btn_debug_udid_copy -> {
                if (!TextUtils.isEmpty(txt_udid.text.toString())) {
                    AppUtils.copyText(txt_udid.text.toString(), this)
                    ToastUtil.showToastMessage(R.string.debug_copy_success)
                }
            }

        }
    }

    override fun onCheckedChanged(v: SwitchButton, isChecked: Boolean) {
        when (v.id) {
            R.id.btn_debug_start_params -> {
                SPUtils.putOnlineConfigSharedBoolean(SPKey.START_PARAMS, isChecked)
                startParams()
            }
            R.id.btn_debug_pre_show_ad -> {
                SPUtils.putOnlineConfigSharedBoolean(SPKey.PRE_SHOW_AD, isChecked)
                SPUtils.putDefaultSharedInt(SPKey.USER_NEW_INDEX, if (isChecked) 2 else 1)
                preShowAd(isChecked)
            }
            R.id.btn_debug_reset_book_shelf -> {
                resetBookShelf(isChecked)
            }
            R.id.btn_debug_update_chapter -> {
                updateChapter(isChecked)
            }
            R.id.btn_debug_show_toast -> {
                SPUtils.putOnlineConfigSharedBoolean(SPKey.SHOW_TOAST_LOG, isChecked)
            }
        }

    }


    private fun initView() {

        //启用动态参数
        btn_debug_start_params.setOnCheckedChangeListener(this)
        btn_debug_start_params.isChecked = SPUtils.getOnlineConfigSharedBoolean(SPKey.START_PARAMS, true)

        // 提前显示广告
        btn_debug_pre_show_ad.setOnCheckedChangeListener(this)
        btn_debug_pre_show_ad.isChecked = SPUtils.getOnlineConfigSharedBoolean(SPKey.PRE_SHOW_AD, false)

        // 重新获取默认书架
        btn_debug_reset_book_shelf.setOnCheckedChangeListener(this)
        // 更新章节
        btn_debug_update_chapter.setOnCheckedChangeListener(this)

        // 打点Toast
        btn_debug_show_toast.setOnCheckedChangeListener(this)
        btn_debug_show_toast.isChecked = SPUtils.getOnlineConfigSharedBoolean(SPKey.SHOW_TOAST_LOG, false)
        var backId=AppUtils.getDrawableByName(this,"icon_back_left")
        if(backId!=-1) {
            try{
               iv_back.setImageResource(backId)
            }catch (e:Throwable){
                e.printStackTrace()
            }
        }
        iv_back.setOnClickListener(this)

        tv_api.setOnClickListener(this)
        tv_web.setOnClickListener(this)
        tv_micro.setOnClickListener(this)
        tv_micro_content.setOnClickListener(this)
        btn_debug_device_copy.setOnClickListener(this)
        btn_debug_udid_copy.setOnClickListener(this)


    }


    /**
     * 启用动态参数
     */
    private fun startParams() {
        if (SPUtils.getOnlineConfigSharedBoolean(SPKey.START_PARAMS, true)) {

            //还原动态参数
            SPUtils.putOnlineConfigSharedString(SPKey.NOVEL_HOST, SPUtils.getDefaultSharedString(SPKey.NOVEL_PRE_HOST))
            SPUtils.putOnlineConfigSharedString(SPKey.WEBVIEW_HOST, SPUtils.getDefaultSharedString(SPKey.WEBVIEW_PRE_HOST))
            SPUtils.putOnlineConfigSharedString(SPKey.UNION_HOST, SPUtils.getDefaultSharedString(SPKey.UNION_PRE_HOST))
            SPUtils.putOnlineConfigSharedString(SPKey.CONTENT_HOST, SPUtils.getDefaultSharedString(SPKey.CONTENT_PRE_HOST))

            Config.insertRequestAPIHost(SPUtils.getDefaultSharedString(SPKey.NOVEL_PRE_HOST))
            Config.insertWebViewHost(SPUtils.getDefaultSharedString(SPKey.WEBVIEW_PRE_HOST))
            MicroAPI.microHost = (SPUtils.getDefaultSharedString(SPKey.UNION_PRE_HOST))
            ContentAPI.contentHost = (SPUtils.getDefaultSharedString(SPKey.CONTENT_PRE_HOST))

            MicroAPI.initMicroService()
            ContentAPI.initContentService()
            RequestAPI.initializeDataRequestService()

        } else { //禁用动态参数
            // 保留动态参数
            SPUtils.putDefaultSharedString(SPKey.NOVEL_PRE_HOST, SPUtils.getOnlineConfigSharedString(SPKey.NOVEL_HOST, ""))
            SPUtils.putDefaultSharedString(SPKey.WEBVIEW_PRE_HOST, SPUtils.getOnlineConfigSharedString(SPKey.WEBVIEW_HOST, ""))
            SPUtils.putDefaultSharedString(SPKey.UNION_PRE_HOST, SPUtils.getOnlineConfigSharedString(SPKey.UNION_HOST, ""))
            SPUtils.putDefaultSharedString(SPKey.CONTENT_PRE_HOST, SPUtils.getOnlineConfigSharedString(SPKey.CONTENT_HOST, ""))
        }


    }

    /**
     * 提前展示广告（无需两天后）
     */
    private fun preShowAd(isChecked: Boolean) {

        Constants.isHideAD = !isChecked
    }

    /**
     * 重置获取默认书架
     */
    private fun resetBookShelf(isChecked: Boolean) {
        if (isChecked) {
            val loadDataManager = LoadDataManager(this)
            // 首次安装新用户添加默认书籍
            loadDataManager.addDefaultBooks(Constants.SGENDER)
        }

    }

    /**
     * 更新章节
     *
     * 清除目录表的最后一条
     * 目的是要模拟书籍更新
     * 删除最后一条的时候还要对应的更新book表
     */
    private fun updateChapter(isChecked: Boolean) {

        if (isChecked) {

            val factory = RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext())

            val booksList = factory.loadBooks()

            booksList?.forEach {
                it.update_status = 1
                if (it.chapter_count > 1) {
                    it.chapter_count -= 1
                    if (Constants.QG_SOURCE == it.book_type) {
                        if (it.chapters_update_index <= 0) {
                            val lastChapter = factory.queryLastChapter(it.book_id)
                            if (lastChapter != null) it.chapters_update_index = lastChapter.sequence + 2

                        }
                        it.chapters_update_index -= 1//青果更新标识
                    }

                    // 查询并删除最后一条章节
                    val chapters = factory.queryAllChapters(it.book_id)

                    factory.deleteChapters(it.book_id,chapters.size - 1)
                }

                //更新书的当前章节数
                factory.updateBook(it)

            }
        }
    }


    /**
     * 跳转不同的界面
     */
    private fun intentHostList(type: String) {
        if (SPUtils.getOnlineConfigSharedBoolean(SPKey.START_PARAMS, true)) {
            ToastUtil.showToastMessage("请先关闭动态参数")
            return
        }
        val intent = Intent(this, DebugHostActivity::class.java)
        intent.putExtra("type", type)
        startActivity(intent)
    }


}