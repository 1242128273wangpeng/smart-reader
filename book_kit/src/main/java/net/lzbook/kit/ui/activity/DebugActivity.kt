package net.lzbook.kit.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.ding.basic.RequestRepositoryFactory
import com.ding.basic.config.ParameterConfig
import com.ding.basic.net.Config
import com.ding.basic.net.api.ContentAPI
import com.ding.basic.net.api.MicroAPI
import com.ding.basic.net.api.RequestAPI
import com.ding.basic.util.ReplaceConstants
import com.ding.basic.util.sp.SPKey
import com.ding.basic.util.sp.SPUtils
import com.dingyue.statistics.DyStatService
import com.orhanobut.logger.Logger
import com.umeng.message.MessageSharedPrefs
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_debug.*
import net.lzbook.kit.R
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.ui.activity.base.FrameActivity
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.OpenUDID
import net.lzbook.kit.utils.book.LoadDataManager
import net.lzbook.kit.utils.router.RouterConfig
import net.lzbook.kit.utils.toast.ToastUtil

/**
 * Function：调试模式
 *
 * Created by JoannChen on 2018/4/19 0019 10:46
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
@Route(path = RouterConfig.DEBUG_ACTIVITY)
class DebugActivity : FrameActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)
        initializeView()
    }

    override fun onResume() {
        super.onResume()

        txt_debug_request_api_result.text = SPUtils.getOnlineConfigSharedString(SPKey.NOVEL_HOST, ReplaceConstants.getReplaceConstants().BOOK_NOVEL_DEPLOY_HOST)

        txt_debug_micro_api_result.text = SPUtils.loadPrivateSharedString(SPKey.MICRO_AUTH_HOST, ReplaceConstants.getReplaceConstants().MICRO_API_HOST)

        txt_debug_content_api_result.text = SPUtils.loadPrivateSharedString(SPKey.CONTENT_AUTH_HOST, ReplaceConstants.getReplaceConstants().CONTENT_API_HOST)

        txt_debug_web_host_result.text = SPUtils.getOnlineConfigSharedString(SPKey.WEBVIEW_HOST, ReplaceConstants.getReplaceConstants().BOOK_WEBVIEW_HOST)

        txt_debug_user_tag_result.text = SPUtils.getOnlineConfigSharedString(SPKey.USER_TAG_HOST, Config.loadUserTagHost())

        txt_debug_h5_host_result.text = Config.webViewBaseHost

        txt_debug_city_code.text = ParameterConfig.cityCode

        txt_debug_user_id.text = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext())


        if (AppUtils.hasUPush()) {
            rl_debug_device_token.visibility = View.VISIBLE
            txt_debug_device_token.visibility = View.VISIBLE
            txt_debug_device_token.text = MessageSharedPrefs.getInstance(BaseBookApplication.getGlobalContext()).deviceToken
        } else {
            rl_debug_device_token.visibility = View.GONE
            txt_debug_device_token.visibility = View.GONE
        }
    }

    private fun initializeView() {

        rgroup_debug_h5_state.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbtn_debug_h5_debug -> Config.webDeploy = "bug"
                R.id.rbtn_debug_h5_regress -> Config.webDeploy = "uat"
                R.id.rbtn_debug_h5_official -> Config.webDeploy = "off"
            }

            requestWebViewParameter()
        }

        sbtn_debug_dynamic_check.isChecked = SPUtils.getOnlineConfigSharedBoolean(SPKey.DEBUG_DYNAMICA_STATE, true)
        
        sbtn_debug_show_ad.isChecked = SPUtils.getOnlineConfigSharedBoolean(SPKey.PRE_SHOW_AD, false)

        sbtn_debug_show_toast.isChecked = DyStatService.eventToastOpen

        sbtn_debug_shield_book.isChecked = SPUtils.getOnlineConfigSharedBoolean(SPKey.SHIELD_BOOK, true)


        //启用动态参数
        sbtn_debug_dynamic_check.setOnCheckedChangeListener { _, isChecked ->
            SPUtils.putOnlineConfigSharedBoolean(SPKey.DEBUG_DYNAMICA_STATE, isChecked)
            startParams()
        }

        // 提前显示广告
        sbtn_debug_show_ad.setOnCheckedChangeListener { _, isChecked ->
            SPUtils.putOnlineConfigSharedBoolean(SPKey.PRE_SHOW_AD, isChecked)
            SPUtils.putDefaultSharedInt(SPKey.USER_NEW_INDEX, if (isChecked) 2 else 1)
            preShowAd(isChecked)
        }

        // 重新获取默认书架
        btn_debug_reset_shelf.setOnClickListener {
            resetBookShelf()
        }

        // 更新章节
        btn_debug_update_chapter.setOnClickListener {
            updateChapter()
        }

        sbtn_debug_show_toast.setOnCheckedChangeListener { _, isChecked ->
            DyStatService.eventToastOpen = isChecked
        }

        sbtn_debug_shield_book.setOnCheckedChangeListener { _, isChecked ->
            SPUtils.putOnlineConfigSharedBoolean(SPKey.SHIELD_BOOK, isChecked)
        }

        img_debug_back.setOnClickListener {
            finish()
        }

        txt_debug_request_api_result.setOnClickListener {
            intentHostList(SPKey.NOVEL_HOST)
        }

        txt_debug_micro_api_result.setOnClickListener {
            intentHostList(SPKey.MICRO_AUTH_HOST)
        }

        txt_debug_content_api_result.setOnClickListener {
            intentHostList(SPKey.CONTENT_AUTH_HOST)
        }

        txt_debug_web_host_result.setOnClickListener {
            intentHostList(SPKey.WEBVIEW_HOST)
        }

        txt_debug_user_tag_result.setOnClickListener {
            intentHostList(SPKey.USER_TAG_HOST)
        }

        btn_debug_udid_copy.setOnClickListener {
            if (!TextUtils.isEmpty(txt_debug_user_id.text.toString())) {
                AppUtils.copyText(txt_debug_user_id.text.toString(), this)
                ToastUtil.showToastMessage(R.string.debug_copy_success)
            }
        }

        btn_debug_device_token_copy.setOnClickListener {
            if (!TextUtils.isEmpty(txt_debug_device_token.text.toString())) {
                AppUtils.copyText(txt_debug_device_token.text.toString(), this)
                ToastUtil.showToastMessage(R.string.debug_copy_success)
            }
        }
    }


    /**
     * 启用动态参数
     */
    private fun startParams() {
        if (SPUtils.loadPrivateSharedBoolean(SPKey.DEBUG_DYNAMICA_STATE, true)) {
            //还原动态参数
            SPUtils.putOnlineConfigSharedString(SPKey.NOVEL_HOST, SPUtils.getDefaultSharedString(SPKey.NOVEL_PRE_HOST))
            SPUtils.putOnlineConfigSharedString(SPKey.WEBVIEW_HOST, SPUtils.getDefaultSharedString(SPKey.WEBVIEW_PRE_HOST))

            SPUtils.insertPrivateSharedString(SPKey.MICRO_AUTH_HOST, SPUtils.loadPrivateSharedString(SPKey.UNION_PRE_HOST))
            SPUtils.insertPrivateSharedString(SPKey.CONTENT_AUTH_HOST, SPUtils.loadPrivateSharedString(SPKey.CONTENT_PRE_HOST))

            SPUtils.putOnlineConfigSharedString(SPKey.USER_TAG_HOST, SPUtils.getDefaultSharedString(SPKey.USER_TAG_PRE_HOST))


            Config.insertRequestAPIHost(SPUtils.getDefaultSharedString(SPKey.NOVEL_PRE_HOST))
            Config.insertWebViewHost(SPUtils.getDefaultSharedString(SPKey.WEBVIEW_PRE_HOST))

            MicroAPI.microHost = SPUtils.loadPrivateSharedString(SPKey.UNION_PRE_HOST)
            ContentAPI.contentHost = SPUtils.loadPrivateSharedString(SPKey.CONTENT_PRE_HOST)

            MicroAPI.initMicroService()
            ContentAPI.initContentService()
            RequestAPI.initializeDataRequestService()

        } else {
            SPUtils.putDefaultSharedString(SPKey.NOVEL_PRE_HOST, SPUtils.getOnlineConfigSharedString(SPKey.NOVEL_HOST, ""))
            SPUtils.putDefaultSharedString(SPKey.WEBVIEW_PRE_HOST, SPUtils.getOnlineConfigSharedString(SPKey.WEBVIEW_HOST, ""))
            SPUtils.putDefaultSharedString(SPKey.USER_TAG_PRE_HOST, SPUtils.getOnlineConfigSharedString(SPKey.USER_TAG_HOST, ""))

            SPUtils.insertPrivateSharedString(SPKey.UNION_PRE_HOST, SPUtils.loadPrivateSharedString(SPKey.MICRO_AUTH_HOST))
            SPUtils.insertPrivateSharedString(SPKey.CONTENT_PRE_HOST, SPUtils.loadPrivateSharedString(SPKey.CONTENT_AUTH_HOST))
        }
    }

    /**
     * 提前展示广告（无需两天后）
     */
    private fun preShowAd(isChecked: Boolean) {

        Constants.isHideAD = !isChecked
    }

    /**
     * 跳转不同的界面
     */
    private fun intentHostList(type: String) {
        if (SPUtils.getOnlineConfigSharedBoolean(SPKey.DEBUG_DYNAMICA_STATE, true)) {
            ToastUtil.showToastMessage("请先关闭动态参数")
            return
        }

        val intent = Intent(this@DebugActivity, DebugHostActivity::class.java)
        intent.putExtra("type", type)
        startActivity(intent)
    }


    private fun requestWebViewParameter() {
        insertDisposable(RequestRepositoryFactory.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestWebViewConfig()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ it ->
                    if (it != null && it.checkResultAvailable()) {
                        Config.webViewBaseHost = it.data ?: ""
                    }
                }, {
                    Logger.e("Error: " + it.toString())
                })
        )
    }


    /***
     * 重置书架书籍
     * **/
    private fun resetBookShelf() {
        val loadDataManager = LoadDataManager(this)
        // 首次安装新用户添加默认书籍
        loadDataManager.addDefaultBooks(ParameterConfig.GENDER_TYPE)
    }

    /***
     * 模拟书籍更新：增量更新
     * **/
    private fun updateChapter() {

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

                factory.deleteChapters(it.book_id, chapters.size - 1)
            }

            //更新书的当前章节数
            factory.updateBook(it)

        }
    }
}