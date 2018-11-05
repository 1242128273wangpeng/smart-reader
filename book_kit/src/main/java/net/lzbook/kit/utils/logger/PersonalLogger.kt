package net.lzbook.kit.utils.logger

import com.dingyue.statistics.DyStatService
import net.lzbook.kit.app.base.BaseBookApplication
import net.lzbook.kit.pointpage.EventPoint
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
        DyStatService.onEvent(EventPoint.PERSONAL_NIGHTMODE)
    }

    fun uploadPersonalPushSetting() {
        DyStatService.onEvent(EventPoint.PERSONAL_MORESET)
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(), StatServiceUtils.me_set_click_more)
    }

    fun uploadPersonalFeedback() {
        DyStatService.onEvent(EventPoint.PERSONAL_HELP)
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(), StatServiceUtils.me_set_click_help)
    }

    fun uploadPersonalMark() {
        DyStatService.onEvent(EventPoint.PERSONAL_COMMENT)
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(), StatServiceUtils.me_set_click_help)
    }

    fun uploadPersonalDisclaimer() {
        DyStatService.onEvent(EventPoint.PERSONAL_PROCTCOL)
    }

    fun uploadPersonalCheckUpdate() {
        DyStatService.onEvent(EventPoint.PERSONAL_VERSION)
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(), StatServiceUtils.me_set_click_ver)
    }

    fun uploadPersonalClearCache() {
        DyStatService.onEvent(EventPoint.PERSONAL_CACHECLEAR)
        StatServiceUtils.statAppBtnClick(BaseBookApplication.getGlobalContext(), StatServiceUtils.me_set_cli_clear_cache)
    }

    fun uploadPersonalAutoCache(isChecked: Boolean) {
        DyStatService.onEvent(EventPoint.PERSONAL_WIFI_AUTOCACHE, mapOf("type" to if (isChecked) "1" else "0"))
    }

    fun uploadPersonalADPage() {
        DyStatService.onEvent(EventPoint.PERSONAL_ADPAGE)
    }

    fun uploadPersonalWebCollect() {
        DyStatService.onEvent(EventPoint.PERSONAL_WEBCOLLECT)
    }
}