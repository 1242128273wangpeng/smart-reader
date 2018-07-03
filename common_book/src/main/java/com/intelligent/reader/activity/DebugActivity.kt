package com.intelligent.reader.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import com.ding.basic.repository.RequestRepositoryFactory
import com.dingyue.contract.util.SharedPreUtil
import com.dingyue.contract.util.showToastMessage
import com.intelligent.reader.R
import kotlinx.android.synthetic.main.activity_debug.*
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.book.view.SwitchButton
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.db.help.ChapterDaoHelper
import net.lzbook.kit.request.UrlUtils
import net.lzbook.kit.utils.LoadDataManager

/**
 * <pre>
 * Function：调试模式
 *
 * Created by JoannChen on 2018/4/19 0019 10:46
 * E-mail:yongzuo_chen@dingyuegroup.cn
 * </pre>
 */
class DebugActivity : Activity(), SwitchButton.OnCheckedChangeListener, View.OnClickListener {

    private val sharedPreUtil: SharedPreUtil = SharedPreUtil(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        initView()
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.img_back -> finish()
            R.id.tv_api -> {
                if (sharedPreUtil.getBoolean(SharedPreUtil.START_PARAMS)) {
                    this.showToastMessage("请先关闭动态参数")
                    return
                }
                val intent = Intent(this, DebugHostActivity::class.java)
                intent.putExtra("isApi", true)
                startActivity(intent)
            }
            R.id.tv_web -> {
                if (sharedPreUtil.getBoolean(SharedPreUtil.START_PARAMS)) {
                    this.showToastMessage("请先关闭动态参数")
                    return
                }
                val intent = Intent(this, DebugHostActivity::class.java)
                intent.putExtra("isApi", false)
                startActivity(intent)
            }
        }
    }

    override fun onCheckedChanged(v: SwitchButton, isChecked: Boolean) {
        when (v.id) {
            R.id.btn_debug_start_params -> {
                sharedPreUtil.putBoolean(SharedPreUtil.START_PARAMS, isChecked)
                startParams()
            }
            R.id.btn_debug_pre_show_ad -> {
                sharedPreUtil.putBoolean(SharedPreUtil.PRE_SHOW_AD, isChecked)
                preShowAd(isChecked)
            }
            R.id.btn_debug_reset_book_shelf -> {
//                SharedUtils.setBoolean(SharedUtils.RESET_BOOK_SHELF, isChecked)
                resetBookShelf(isChecked)
            }
            R.id.btn_debug_update_chapter -> {
//                SharedUtils.setBoolean(SharedUtils.UPDATE_CHAPTER, isChecked)
                updateChapter(isChecked)
            }
        }

    }


    private fun initView() {

        //启用动态参数、提前显示广告、重新获取默认书架、更新章节
        btn_debug_start_params.setOnCheckedChangeListener(this)
        btn_debug_pre_show_ad.setOnCheckedChangeListener(this)
        btn_debug_reset_book_shelf.setOnCheckedChangeListener(this)
        btn_debug_update_chapter.setOnCheckedChangeListener(this)

        btn_debug_start_params.isChecked = sharedPreUtil.getBoolean(SharedPreUtil.START_PARAMS, true)
        btn_debug_pre_show_ad.isChecked = sharedPreUtil.getBoolean(SharedPreUtil.PRE_SHOW_AD)
//        btn_debug_reset_book_shelf.isChecked = SharedUtils.getBoolean(SharedUtils.RESET_BOOK_SHELF)
//        btn_debug_update_chapter.isChecked = SharedUtils.getBoolean(SharedUtils.UPDATE_CHAPTER)


        iv_back.setOnClickListener(this)
        tv_api.setOnClickListener(this)
        tv_web.setOnClickListener(this)


    }

    /**
     * 启用动态参数
     */
    private fun startParams() {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        sp.edit().remove(Constants.NOVEL_HOST).apply()
        sp.edit().remove(Constants.WEBVIEW_HOST).apply()
        UrlUtils.dynamic()

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


    override fun onResume() {
        super.onResume()
        tv_api.text = ("api_host:【${UrlUtils.getBookNovelDeployHost()}】")
        tv_web.text = ("web_host:【${UrlUtils.getBookWebViewHost()}】")
        btn_debug_start_params.isChecked = sharedPreUtil.getBoolean(SharedPreUtil.START_PARAMS)
    }

}