package net.lzbook.kit.utils.logger

import net.lzbook.kit.base.BaseBookApplication
import net.lzbook.kit.appender_loghub.StartLogClickUtil
import net.lzbook.kit.utils.StatServiceUtils

/**
 * Desc 设置页面打点
 * Author crazylei
 * Mail crazylei951002@gmail.com
 * Date 2018/5/12 17:32
 */
object PersonalLogger {

    fun uploadPersonalCurrentMode(isNightMode: Boolean) {
        val value = if (isNightMode) {
            StatServiceUtils.me_set_cli_day_shift
        } else {
            StatServiceUtils.me_set_cli_night_shift
        }
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(), value)
    }

    fun uploadPersonalNightModeChange() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_PERSONAL, StartLogClickUtil.ACTION_PERSONAL_NIGHT_MODE)
    }

    fun uploadPersonalPushSetting() {
        val context = BaseBookApplication.getGlobalContext()

        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.PAGE_PERSONAL,
                StartLogClickUtil.ACTION_PERSONAL_MORE_SET)

        StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_more)
    }

    fun uploadPersonalFeedback() {
        val context = BaseBookApplication.getGlobalContext()

        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.PAGE_PERSONAL,
                StartLogClickUtil.ACTION_PERSONAL_HELP)

        StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_help)
    }

    fun uploadPersonalMark() {
        val context = BaseBookApplication.getGlobalContext()

        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.PAGE_PERSONAL,
                StartLogClickUtil.ACTION_PERSONAL_COMMENT)

        StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_help)
    }

    fun uploadPersonalDisclaimer() {
        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_PERSONAL, StartLogClickUtil.ACTION_PERSONAL_PROCTCOL)
    }

    fun uploadPersonalCheckUpdate() {
        val context = BaseBookApplication.getGlobalContext()

        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.PAGE_PERSONAL,
                StartLogClickUtil.ACTION_PERSONAL_VERSION)

        StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_click_ver)
    }

    fun uploadPersonalClearCache() {
        val context = BaseBookApplication.getGlobalContext()

        StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.PAGE_PERSONAL,
                StartLogClickUtil.ACTION_PERSONAL_CACHE_CLEAR)

        StatServiceUtils.statAppBtnClick(context, StatServiceUtils.me_set_cli_clear_cache)
    }


    fun uploadPersonalAutoCache(isChecked: Boolean) {
        val data = HashMap<String, String>()
        data["type"] = if (isChecked) "1" else "0"

        StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                StartLogClickUtil.PAGE_PERSONAL, StartLogClickUtil.ACTION_PERSONAL_WIFI_AUTO_CACHE, data)
    }
}