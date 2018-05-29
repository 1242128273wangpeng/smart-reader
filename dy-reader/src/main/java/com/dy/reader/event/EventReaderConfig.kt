package com.dy.reader.event

import com.dy.reader.setting.ReaderSettings

data class EventReaderConfig(val type: ReaderSettings.ConfigType, val obj: Any? = null)