package com.dy.reader.event

/**
 * Created by yuchao on 2018/4/30 0030.
 */
data class EventSetting(val type: Type, val obj: Any? = null) {
    enum class Type {
        OPEN_CATALOG, MENU_STATE_CHANGE,
        REFRESH_MODE, DISMISS_TOP_MENU, FULL_WINDOW_CHANGE,
        CHANGE_SCREEN_MODE,HIDE_AD,SHOW_AD
    }
}