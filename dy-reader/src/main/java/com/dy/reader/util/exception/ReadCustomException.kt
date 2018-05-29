package com.dy.reader.util.exception

/**
 * Created by wt on 2018/1/16.
 */
class ReadCustomException{
    open class PageIndexException(msg: String) : Exception(msg)
    open class PageOffsetException(msg: String) : Exception(msg)
    open class PageContentException(msg: String) : Exception(msg)
}
