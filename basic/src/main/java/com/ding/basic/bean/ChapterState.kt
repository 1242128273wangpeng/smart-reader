package com.ding.basic.bean

enum class ChapterState constructor(var state: String) {
    SOURCE_ERROR("源网站转换失败，请您更换来源或稍候重试！"),
    CONTENT_EMPTY("源网站内容为空，请您更换来源阅读！"),
    CONTENT_ERROR("源网站内容错误，请您更换来源阅读！"),
    CONTENT_NORMAL("内容正常")
}