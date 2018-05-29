package com.dy.reader.event

data class EventLoading(val type: Type, val retry: (() -> Unit)? = null){
    enum class Type{
        START, SUCCESS, RETRY, PROGRESS_CHANGE
    }
}