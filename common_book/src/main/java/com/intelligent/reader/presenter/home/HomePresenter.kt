package com.intelligent.reader.presenter.home

import android.content.pm.PackageManager
import android.preference.PreferenceManager
import com.intelligent.reader.app.BookApplication
import com.intelligent.reader.presenter.IPresenter
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.book.download.CacheManager
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.ReadConfig
import net.lzbook.kit.utils.AppUtils
import net.lzbook.kit.utils.StatServiceUtils

/**
 * Desc HomeFragment - presenter
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/2/28 0028 11:12
 */
class HomePresenter(override var view: HomeView?, var packageManager: PackageManager) : IPresenter<HomeView> {

    /***
     * 初始化参数
     * **/
    fun initParameters() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(BookApplication.getGlobalContext())

        //初始化阅读页背景
        if (sharedPreferences.getInt("content_mode", 51) < 50) {
            Constants.MODE = 51
            ReadConfig.MODE = 51
            sharedPreferences.edit().putInt("content_mode", Constants.MODE).apply()
            sharedPreferences.edit().putInt("current_light_mode", Constants.MODE).apply()
        } else {
            Constants.MODE = sharedPreferences.getInt("content_mode", 51)
            ReadConfig.MODE = sharedPreferences.getInt("content_mode", 51)
        }

        val firstTime = sharedPreferences.getLong(Constants.TODAY_FIRST_OPEN_APP, 0)
        val currentTime = System.currentTimeMillis()

        //判断用户是否是当日首次打开应用
        val result = AppUtils.isToday(firstTime, currentTime)

        if (result) {
            Constants.is_user_today_first = false
        } else {
            //用户首次打开，记录当前时间
            Constants.is_user_today_first = true
            sharedPreferences.edit().putLong(Constants.TODAY_FIRST_OPEN_APP, currentTime).apply()
            sharedPreferences.edit().putBoolean(Constants.IS_UPLOAD, false).apply()
            updateApplicationList()
        }

        Constants.upload_userinformation = sharedPreferences.getBoolean(Constants.IS_UPLOAD, false)
    }


    /***
     * 初始化下载服务
     * **/
    fun initDownloadService() {
        CacheManager.checkService()
    }

    fun uploadHeadSearchLog(bottomType: Int) {
        val context = BaseBookApplication.getGlobalContext()
        when (bottomType) {
            2 -> StartLogClickUtil.upLoadEventLog(context,
                    StartLogClickUtil.RECOMMEND_PAGE, StartLogClickUtil.QG_TJY_SEARCH)
            3 -> StartLogClickUtil.upLoadEventLog(context,
                    StartLogClickUtil.TOP_PAGE, StartLogClickUtil.QG_BDY_SEARCH)
            4 -> StartLogClickUtil.upLoadEventLog(context,
                    StartLogClickUtil.CLASS_PAGE, StartLogClickUtil.QG_FL_SEARCH)
            else -> StartLogClickUtil.upLoadEventLog(context,
                    StartLogClickUtil.SHELF_PAGE, StartLogClickUtil.SEARCH)
        }
        net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(context,
                net.lzbook.kit.utils.StatServiceUtils.bs_click_search_btn)
    }


    fun uploadBookshelfSelectedLog() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.BOOKSHELF)
    }

    fun uploadRecommendSelectedLog() {
        val context = BaseBookApplication.getGlobalContext()
        net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(context,
                net.lzbook.kit.utils.StatServiceUtils.bs_click_recommend_menu)
        StartLogClickUtil.upLoadEventLog(context,
                StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.RECOMMEND)
    }

    fun uploadRankingSelectedLog() {
        val context = BaseBookApplication.getGlobalContext()
        net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(context,
                net.lzbook.kit.utils.StatServiceUtils.bs_click_rank_menu)
        StartLogClickUtil.upLoadEventLog(context,
                StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.TOP)
    }

    fun uploadCategorySelectedLog() {
        val context = BaseBookApplication.getGlobalContext()
        net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(context,
                net.lzbook.kit.utils.StatServiceUtils.bs_click_category_menu)
        StartLogClickUtil.upLoadEventLog(context,
                StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.CLASS)
    }

    fun uploadRankingEntryLog() {
        val context = BaseBookApplication.getGlobalContext()
        val data = HashMap<String, String>()
        data["pk"] = "主页"
        StartLogClickUtil.upLoadEventLog(context,
                StartLogClickUtil.TOP_PAGE, StartLogClickUtil.ENTRYPAGE, data)
    }

    fun uploadCategoryEntryLog() {
        val context = BaseBookApplication.getGlobalContext()
        val data = HashMap<String, String>()
        data["pk"] = "主页"
        StartLogClickUtil.upLoadEventLog(context,
                StartLogClickUtil.CLASS_PAGE, StartLogClickUtil.ENTRYPAGE, data)
    }

    fun uploadEditorBackLog() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.SHELFEDIT_PAGE, StartLogClickUtil.CANCLE1)
    }

    fun uploadEditorCancelLog() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.SHELFEDIT_PAGE, StartLogClickUtil.CANCLE1)
    }

    fun uploadCurModeLog(isNightMode: Boolean) {
        val value = if (isNightMode) {
            StatServiceUtils.me_set_cli_day_shift
        } else {
            StatServiceUtils.me_set_cli_night_shift
        }
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(), value)
    }

    fun uploadModeChangeLog() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.NIGHTMODE)
    }

    fun uploadPushSettingClickLog() {
        val context = BaseBookApplication.getGlobalContext()
        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.MORESET)
        StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_more)
    }

    fun uploadFeedbackClickLog() {
        val context = BaseBookApplication.getGlobalContext()
        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.HELP)
        StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_help)
    }

    fun uploadMarkClickLog() {
        val context = BaseBookApplication.getGlobalContext()
        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.COMMENT)
        StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_help)
    }

    fun uploadDisclaimerClickLog() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.PROCTCOL)
    }

    fun uploadCheckUpdateLog() {
        val context = BaseBookApplication.getGlobalContext()
        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.VERSION)
        StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_ver)
    }

    fun uploadClearCacheClickLog() {
        val context = BaseBookApplication.getGlobalContext()
        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.CACHECLEAR)
        StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_cli_clear_cache)
    }

    fun uploadEditorSelectAllLog(isAllSelected: Boolean) {
        val data = HashMap<String, String>()
        data.put("type", if (isAllSelected) "2" else "1")
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.SHELFEDIT_PAGE, StartLogClickUtil.SELECTALL1, data)
    }


    fun uploadAutoCacheLog(isChecked: Boolean) {
        val data = HashMap<String, String>()
        data["type"] = if (isChecked) "1" else "0"
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PEASONAL_PAGE, StartLogClickUtil.WIFI_AUTOCACHE, data)
    }







    /***
     * 上传用户应用列表
     * **/
    private fun updateApplicationList() {
        Observable.create(ObservableOnSubscribe<String> { emitter ->
            emitter.onNext(AppUtils.scanLocalInstallAppList(packageManager))
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = { message ->
                    StartLogClickUtil.upLoadApps(message)
                })
    }
}