package com.intelligent.reader.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.ding.basic.Config
import com.ding.basic.repository.RequestRepositoryFactory
import com.ding.basic.request.ContentAPI
import com.ding.basic.request.MicroAPI
import com.ding.basic.request.RequestAPI
import com.dingyue.contract.router.RouterConfig
import com.dingyue.contract.util.SharedPreUtil
import com.dingyue.contract.util.showToastMessage
import com.intelligent.reader.R
import iyouqu.theme.BaseCacheableActivity
import kotlinx.android.synthetic.main.activity_debug.*
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.book.view.SwitchButton
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.db.help.ChapterDaoHelper
import net.lzbook.kit.utils.LoadDataManager

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

    private val sp = BaseBookApplication.getGlobalContext().getSharedPreferences(Constants.SHAREDPREFERENCES_KEY, 0)
    private val editor = sp.edit()

    //保存禁用动态参数前的host,用来还原动态参数
    private val sharePreUtil = SharedPreUtil(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        initView()
    }

    override fun onResume() {
        super.onResume()

        tv_api.text = ("${resources.getString(R.string.debug_api_host)}【${sp.getString(Constants.NOVEL_HOST, "")}】")
        tv_web.text = ("${resources.getString(R.string.debug_web_host)}【${sp.getString(Constants.WEBVIEW_HOST, "")}】")
        tv_micro.text = ("${resources.getString(R.string.debug_micro_host)}【${sp.getString(Constants.UNION_HOST, "")}】")
        tv_micro_content.text = ("${resources.getString(R.string.debug_micro_content_host)}【${sp.getString(Constants.CONTENT_HOST, "")}】")

        btn_debug_start_params.isChecked = sp.getBoolean(Constants.START_PARAMS, true)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_back -> finish()
            R.id.tv_api -> {
                intentHostList(Constants.NOVEL_HOST)
            }
            R.id.tv_web -> {
                intentHostList(Constants.WEBVIEW_HOST)
            }
            R.id.tv_micro -> {
                intentHostList(Constants.UNION_HOST)
            }
            R.id.tv_micro_content -> {
                intentHostList(Constants.CONTENT_HOST)
            }

        }
    }

    override fun onCheckedChanged(v: SwitchButton, isChecked: Boolean) {
        when (v.id) {
            R.id.btn_debug_start_params -> {
                editor.putBoolean(Constants.START_PARAMS, isChecked).apply()
                startParams()
            }
            R.id.btn_debug_pre_show_ad -> {
                editor.putBoolean(Constants.PRE_SHOW_AD, isChecked).apply()
                preShowAd(isChecked)
            }
            R.id.btn_debug_reset_book_shelf -> {
                resetBookShelf(isChecked)
            }
            R.id.btn_debug_update_chapter -> {
                updateChapter(isChecked)
            }
            R.id.btn_debug_show_toast -> {
                editor.putBoolean(Constants.SHOW_TOAST_LOG, isChecked).apply()
            }
        }

    }


    private fun initView() {

        //启用动态参数
        btn_debug_start_params.setOnCheckedChangeListener(this)
        btn_debug_start_params.isChecked = sp.getBoolean(Constants.START_PARAMS, true)

        // 提前显示广告
        btn_debug_pre_show_ad.setOnCheckedChangeListener(this)
        btn_debug_pre_show_ad.isChecked = sp.getBoolean(Constants.PRE_SHOW_AD, false)

        // 重新获取默认书架
        btn_debug_reset_book_shelf.setOnCheckedChangeListener(this)
        // 更新章节
        btn_debug_update_chapter.setOnCheckedChangeListener(this)

        // 打点Toast
        btn_debug_show_toast.setOnCheckedChangeListener(this)
        btn_debug_show_toast.isChecked = sp.getBoolean(Constants.SHOW_TOAST_LOG, false)

        iv_back.setOnClickListener(this)

        tv_api.setOnClickListener(this)
        tv_web.setOnClickListener(this)
        tv_micro.setOnClickListener(this)
        tv_micro_content.setOnClickListener(this)


    }


    /**
     * 启用动态参数
     */
    private fun startParams() {
        if (sp.getBoolean(Constants.START_PARAMS, true)) {

            //还原动态参数
            editor.putString(Constants.NOVEL_HOST, sharePreUtil.getString(Constants.NOVEL_PRE_HOST))
            editor.putString(Constants.WEBVIEW_HOST, sharePreUtil.getString(Constants.WEBVIEW_PRE_HOST))
            editor.putString(Constants.UNION_HOST, sharePreUtil.getString(Constants.UNION_PRE_HOST))
            editor.putString(Constants.CONTENT_HOST, sharePreUtil.getString(Constants.CONTENT_PRE_HOST))
            editor.apply()

            Config.insertRequestAPIHost(sharePreUtil.getString(Constants.NOVEL_PRE_HOST))
            Config.insertWebViewHost(sharePreUtil.getString(Constants.WEBVIEW_PRE_HOST))
            Config.insertMicroAPIHost(sharePreUtil.getString(Constants.UNION_PRE_HOST))
            Config.insertContentAPIHost(sharePreUtil.getString(Constants.CONTENT_PRE_HOST))

            ContentAPI.initMicroService()
            MicroAPI.initMicroService()
            RequestAPI.initializeDataRequestService()

        } else { //禁用动态参数
            // 保留动态参数
            sharePreUtil.putString(Constants.NOVEL_PRE_HOST, sp.getString(Constants.NOVEL_HOST, ""))
            sharePreUtil.putString(Constants.WEBVIEW_PRE_HOST, sp.getString(Constants.WEBVIEW_HOST, ""))
            sharePreUtil.putString(Constants.UNION_PRE_HOST, sp.getString(Constants.UNION_HOST, ""))
            sharePreUtil.putString(Constants.CONTENT_PRE_HOST, sp.getString(Constants.CONTENT_HOST, ""))
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
            loadDataManager.addDefaultBooks()
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
                            val dao = ChapterDaoHelper.loadChapterDataProviderHelper(context = this, book_id = it.book_id)
                            val lastChapter = dao.queryLastChapter()
                            if (lastChapter != null) it.chapters_update_index = lastChapter.sequence + 2

                        }
                        it.chapters_update_index -= 1//青果更新标识
                    }

                    // 查询并删除最后一条章节
                    val dao = ChapterDaoHelper.loadChapterDataProviderHelper(context = this, book_id = it.book_id)
                    val chapters = dao.queryAllChapters()

                    dao.deleteChapters(chapters.size - 1)
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
        if (sp.getBoolean(Constants.START_PARAMS, true)) {
            this.showToastMessage("请先关闭动态参数")
            return
        }
        val intent = Intent(this, DebugHostActivity::class.java)
        intent.putExtra("type", type)
        startActivity(intent)
    }


}