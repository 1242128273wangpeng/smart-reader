package com.intelligent.reader.presenter.read

import com.intelligent.reader.activity.ReadingActivity
import net.lzbook.kit.data.bean.ReadViewEnums
import com.intelligent.reader.reader.ReaderViewModel
import net.lzbook.kit.app.BaseBookApplication
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Chapter
import net.lzbook.kit.data.bean.RequestItem
import net.lzbook.kit.data.db.BookChapterDao

/**
 * 拉取章节 Presenter
 * 1、拉取章节判断是否展示LoadPage
 * 2、判断是否更改状态
 * 3、检查章节状态
 * Created by wt on 2017/12/18.
 */
class ReadPresenter(act: ReadingActivity) : BaseReadPresenter(act) {
    /**
     * 开始加载章
     */
    fun onLoadChapter(msg: Int, sequence: Int, showLoadPage: Boolean, pageIndex: ReadViewEnums.PageIndex) {
        if (mReaderViewModel?.chapterList == null || mReaderViewModel?.chapterList!!.isEmpty()) {
            mReaderViewModel?.chapterList = BookChapterDao(BaseBookApplication.getGlobalContext(), readStatus?.book_id).queryBookChapter()
        }
        val tempSequence = if (sequence < -1) {
             -1
        } else if (mReaderViewModel?.chapterList != null && mReaderViewModel?.chapterList?.size?:0 > 0 && sequence + 1 > mReaderViewModel?.chapterList?.size?:0) {
            mReaderViewModel?.chapterList?.size?:0 - 1
        } else{
            sequence
        }
        val requestItem = mReaderViewModel?.readStatus?.getRequestItem() ?: return
        if (showLoadPage) {
            getCustomLoadingPage()
            loadingPage?.loading({
                isLoadingCatalog(msg, requestItem, tempSequence, pageIndex)
                null
            })
        }else {
            isLoadingCatalog(msg, requestItem, tempSequence, pageIndex)
        }
    }

    /**
     *  先拉取目录
     */
    private fun isLoadingCatalog(msg: Int, requestItem: RequestItem, tempSequence: Int, pageIndex: ReadViewEnums.PageIndex) {
        if (mReaderViewModel?.chapterList == null || mReaderViewModel?.chapterList?.isEmpty()!!) {//拉目录
            mReaderViewModel?.setBookChapterViewCallback(object : ReaderViewModel.BookChapterViewCallback {
                override fun onChapterList(chapters: List<Chapter>) {//拉目录成功
                    if (readReference?.get() != null && !readReference?.get()?.isFinishing!!) {
                        val chapterList = chapters as ArrayList<Chapter>
                        sendChapter(msg, requestItem, tempSequence, chapterList, pageIndex)//加载章节
                    }
                }
                override fun onFail(msg: String) {//拉目录失败
                    if (loadingPage != null) {
                        loadingPage!!.onError()
                    }
                }
            })
            mReaderViewModel?.getChapterList(requestItem)
        } else {
            sendChapter(msg, requestItem, tempSequence, mReaderViewModel?.chapterList, pageIndex)//加载章节
        }
    }

    /**
     * 统一处理加载章节请求后逻辑
     * @param what Msg
     * @param chapter 章节
     * @param pageIndex 状态 //Next、Previous 改变 ReadState 状态  Cur不改变 ReadState 状态
     */
    override fun obtainWhat(what: Int, chapter: Chapter,pageIndex: ReadViewEnums.PageIndex) {
        when(pageIndex){
            ReadViewEnums.PageIndex.next -> {
                changeReadStatus(ReadViewEnums.MsgType.MSG_LOAD_NEXT_CHAPTER)
            }
            ReadViewEnums.PageIndex.previous -> {
                changeReadStatus(ReadViewEnums.MsgType.MSG_LOAD_PRE_CHAPTER)
            }
            else -> {

            }
        }
        when (what) {//设置数据
            ReadViewEnums.MsgType.MSG_LOAD_CUR_CHAPTER.Msg ->{
                mReaderViewModel?.currentChapter = chapter
                if (mReaderViewModel?.readStatus != null && mReaderViewModel?.currentChapter != null && mReaderViewModel?.currentChapter?.sequence != -1) {
                    mReaderViewModel?.readStatus?.sequence = mReaderViewModel?.currentChapter?.sequence
                }
            }
            ReadViewEnums.MsgType.MSG_LOAD_PRE_CHAPTER.Msg ->{
                mReaderViewModel?.preChapter = chapter
            }
            ReadViewEnums.MsgType.MSG_LOAD_NEXT_CHAPTER.Msg ->{
                mReaderViewModel?.nextChapter = chapter
            }
        }
        //成功回调 检查加载章节的状态
        val chapterIsNormal = myNovelHelper?.getChapterContent(readReference?.get(),chapter, readStatus!!.book)
        val chapterList = if (chapterIsNormal == true){
            myNovelHelper?.initTextContent2(chapter.content)
        }else {
            myNovelHelper?.initTextContent2("")
        }
        view?.loadChapterSuccess(what,chapter,chapterList!!)
        loadingPage?.onSuccess()
        Constants.startReadTime = System.currentTimeMillis() / 1000L
    }

    /**
     * 改变ReadStatus状态
     * @param 'MSG_LOAD_NEXT_CHAPTER' 翻下页 'MSG_LOAD_PRE_CHAPTER' 翻上页 'MSG_LOAD_JUMP_CHAPTER'跳章
     */
    private fun changeReadStatus(type: ReadViewEnums.MsgType){
        Constants.readedCount++
        mReaderViewModel?.readStatus?.offset = 0
        myNovelHelper?.isShown = false
        when(type){
            ReadViewEnums.MsgType.MSG_LOAD_NEXT_CHAPTER ->{
                mReaderViewModel?.preChapter = mReaderViewModel?.currentChapter
                mReaderViewModel?.currentChapter = mReaderViewModel?.nextChapter
                mReaderViewModel?.nextChapter = null
                mReaderViewModel?.readStatus!!.sequence++
                mReaderViewModel?.readStatus?.currentPage = 1
            }
            ReadViewEnums.MsgType.MSG_LOAD_PRE_CHAPTER->{
                mReaderViewModel?.nextChapter = mReaderViewModel?.currentChapter
                mReaderViewModel?.currentChapter = mReaderViewModel?.preChapter
                mReaderViewModel?.preChapter = null
                mReaderViewModel?.readStatus!!.sequence--
                if (mReaderViewModel?.toChapterStart != false) {
                    mReaderViewModel?.readStatus?.currentPage = 1
                } else {
                    mReaderViewModel?.readStatus?.currentPage = mReaderViewModel?.readStatus?.pageCount
                }
                mReaderViewModel?.toChapterStart = false
            }
            ReadViewEnums.MsgType.MSG_LOAD_JUMP_CHAPTER->{
                mReaderViewModel?.nextChapter = null
                mReaderViewModel?.nextChapter = null
                mReaderViewModel?.preChapter = null
                readStatus?.sequence = readStatus!!.novel_progress
                readStatus?.currentPage = 1
            }
            else -> {
            }
        }
    }


}