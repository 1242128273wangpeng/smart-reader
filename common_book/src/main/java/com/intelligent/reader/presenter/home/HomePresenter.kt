package com.intelligent.reader.presenter.home

import com.intelligent.reader.presenter.IPresenter
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.utils.StatServiceUtils

/**
 * Desc HomeFragment - presenter
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/2/28 0028 11:12
 */
class HomePresenter(override var view: HomeView?) : IPresenter<HomeView> {

    private val tag = "HomePresenter"

    fun uploadHeadSettingLog() {
        val context = BaseBookApplication.getGlobalContext()
        StartLogClickUtil.upLoadEventLog(context,
                StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.PERSONAL)
        net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(context,
                net.lzbook.kit.utils.StatServiceUtils.bs_click_mine_menu)
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
                    StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.SEARCH)
        }
        net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(context,
                net.lzbook.kit.utils.StatServiceUtils.bs_click_search_btn)
    }

    fun uploadDownloadManagerLog() {
        val context = BaseBookApplication.getGlobalContext()
        net.lzbook.kit.utils.StatServiceUtils.statAppBtnClick(context,
                net.lzbook.kit.utils.StatServiceUtils.bs_click_download_btn)
        StartLogClickUtil.upLoadEventLog(context,
                StartLogClickUtil.MAIN_PAGE, StartLogClickUtil.CACHEMANAGE)
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
}