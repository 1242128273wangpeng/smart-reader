package com.intelligent.reader.read.help;

import com.dingyueads.sdk.Bean.Novel;
import com.dingyueads.sdk.NativeInit;
import com.dingyueads.sdk.manager.ADStatisticManager;
import com.intelligent.reader.activity.ReadingActivity;
import com.intelligent.reader.read.page.PageInterface;

import net.lzbook.kit.R;
import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.book.view.LoadingPage;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.NovelLineBean;
import net.lzbook.kit.data.bean.ReadStatus;
import net.lzbook.kit.data.bean.Source;
import net.lzbook.kit.data.bean.SourceItem;
import net.lzbook.kit.data.db.BookChapterDao;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.request.DataCache;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.OpenUDID;
import net.lzbook.kit.utils.StatisticManager;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class IReadDataFactory {

    public LoadingPage loadingPage;
    public ArrayList<Chapter> chapterList;
    public boolean toChapterStart;
    public Chapter nextChapter;
    public Chapter preChapter;
    public Chapter currentChapter;
    public ReadHandler mHandler = new ReadHandler(this);
    protected Context mContext;
    protected PageInterface pageView;
    protected ReadStatus readStatus;
    protected int tempCurrentPage;
    protected int tempPageCount;
    protected int tempSequence;
    protected String tempChapterName;
    protected ArrayList<NovelLineBean> tempChapterNameList;
    protected Chapter tempNextChapter;
    protected Chapter tempPreviousChapter;
    protected Chapter tempCurrentChapter;
    protected int tempOffset;
    //	protected ArrayList<String> tempLineList;
    protected ArrayList<ArrayList<NovelLineBean>> tempLineList;
    protected NovelHelper myNovelHelper;
    protected WeakReference<ReadingActivity> actNovelReference;
    protected ReadDataListener dataListener;
    protected int g_what;
    protected int g_sequence;
    protected ReadingActivity readingActivity;
    protected ArrayList<Chapter> readedChapter;
    private BookDaoHelper bookDaoHelper;
    private StatisticManager statisticManager;

    public void clean() {
        if (mHandler != null) {
            mHandler.canHandleMessage = false;
        }
        if (chapterList != null) {
            chapterList.clear();
        }

        if (tempChapterNameList != null) {
            tempChapterNameList.clear();
        }

        if (tempLineList != null) {
            tempLineList.clear();
        }

        if (readedChapter != null) {
            readedChapter.clear();
        }

        if (readStatus != null) {
            readStatus = null;
        }

        if (myNovelHelper != null) {
            myNovelHelper.clear();
        }

        if (pageView != null) {
            pageView = null;
        }

        readingActivity = null;
    }

    public IReadDataFactory(Context context, ReadingActivity readingActivity, ReadStatus readStatus, NovelHelper
            novelHelper) {
        this.mContext = context;
        this.readingActivity = readingActivity;
        this.readStatus = readStatus;
        this.myNovelHelper = novelHelper;
        this.actNovelReference = new WeakReference<>(readingActivity);

        readedChapter = new ArrayList<Chapter>();
    }

    public void setPageView(PageInterface pageView) {
        this.pageView = pageView;
    }

    public void setReadDataListener(ReadDataListener listener) {
        this.dataListener = listener;
    }

    public void sendMessage(int message) {
        mHandler.obtainMessage(message).sendToTarget();
    }

    public void saveData() {
        tempCurrentPage = readStatus.currentPage;
        tempPageCount = readStatus.pageCount;
        tempSequence = readStatus.sequence;
        tempOffset = readStatus.offset;
        tempChapterName = readStatus.chapterName;
        tempChapterNameList = readStatus.chapterNameList;

        tempNextChapter = nextChapter;
        tempCurrentChapter = currentChapter;
        tempPreviousChapter = preChapter;


        tempLineList = readStatus.mLineList;
    }

    public void restore() {
        readStatus.currentPage = tempCurrentPage;
        readStatus.pageCount = tempPageCount;
        readStatus.sequence = tempSequence;
        readStatus.offset = tempOffset;
        readStatus.chapterName = tempChapterName;
        readStatus.chapterNameList = tempChapterNameList;

        nextChapter = tempNextChapter;
        currentChapter = tempCurrentChapter;
        preChapter = tempPreviousChapter;

        readStatus.mLineList = tempLineList;
    }

    public void setScreenSize(int screenWidth, int screenHeight) {
        readStatus.screenWidth = screenWidth;
        readStatus.screenHeight = screenHeight;
    }

    /**
     * 加载失败时打点
     * <p/>
     * loadingPage
     */
    public void loadingError(LoadingPage loadingPage) {
        if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
            return;
        }
        loadingPage.setErrorAction(new Runnable() {

            @Override
            public void run() {
                mHandler.sendEmptyMessage(ReadingActivity.ERROR);

            }
        });
    }

    public LoadingPage getCustomLoadingPage() {
        String curl = "";
        if (readStatus.sequence == -1) {
            curl = readStatus.firstChapterCurl;
        } else if (currentChapter != null && !TextUtils.isEmpty(currentChapter.curl)) {
            //if (readStatus.book.dex == 1 && !TextUtils.isEmpty(currentChapter.curl)) {
            curl = currentChapter.curl;
            /*} else if (readStatus.book.dex == 0 && !TextUtils.isEmpty(currentChapter.curl1)) {
                curl = currentChapter.curl1;
            }*/
        }
        if (loadingPage == null) {
            loadingPage = new LoadingPage(readingActivity, true, curl, LoadingPage.setting_result);
        }
        loadingPage.setCustomBackgroud();
        return loadingPage;
    }

    private void loadCurrentChapter(Message msg) {
        currentChapter = (Chapter) msg.obj;
        if (readStatus != null && currentChapter != null && currentChapter.sequence != -1) {
            readStatus.sequence = currentChapter.sequence;
        }
        initBookCallBack();
    }

    private void loadNextChapter(Message msg) {
        nextChapter = (Chapter) msg.obj;
        nextChapterCallBack(true);
    }

    private void loadPreChapter(Message msg) {
        preChapter = (Chapter) msg.obj;
        preChapterCallBack(true);
    }

    private void loadSearchChapter(Message msg) {
        ArrayList<Source> sourcesList = ((SourceItem) msg.obj).sourceList;
        ReadingActivity readingActivity = actNovelReference.get();
        if (readingActivity != null) {
            readingActivity.searchChapterCallBack(sourcesList);
        }
    }

    private void loadChangeSource(Message msg) {
        ReadingActivity readingActivity = actNovelReference.get();
        if (readingActivity != null) {
            readingActivity.changeSourceCallBack();
        }

    }

    private void loadJumpChapter(Message msg) {
        currentChapter = (Chapter) msg.obj;
        ReadingActivity readingActivity = actNovelReference.get();
        if (readingActivity != null) {
            readingActivity.jumpChapterCallBack();
        }

        Constants.readedCount++;
    }

    private void loadError(Message msg) {
        if (msg.obj != null && dataListener != null) {
            String error = msg.obj.toString();
            String chapter_name = "";
            if (chapterList != null && msg.arg1 - 1 >= 0 && msg.arg1 - 1 < chapterList.size()) {
                Chapter chapter = chapterList.get(msg.arg1 - 1);
                if (chapter != null) {
                    chapter_name = chapter.chapter_name;
                }
            }
            if (bookDaoHelper == null) {
                bookDaoHelper = BookDaoHelper.getInstance();
            }
        }
    }

    private void loadNeedLogin(Message msg) {
    }

    /**
     * 打开书籍取得书签章节内容后的处理
     */
    private void initBookCallBack() {
        Constants.readedCount++;
        //		dealAdShow();
        if (chapterList == null) {
            return;
        }
        // 章节数
        if (readStatus != null) {
            readStatus.chapterCount = chapterList.size();
            if (chapterList != null && !chapterList.isEmpty()) {
                Chapter firstChapter = chapterList.get(0);
                if (firstChapter != null) {
                    //if (readStatus.book.dex == 1) {
                    readStatus.firstChapterCurl = firstChapter.curl;
                    /*} else if (readStatus.book.dex == 0) {
                        readStatus.firstChapterCurl = firstChapter.curl1;
                    }*/
                }
            }
        }

        // 初始化章节内容
        if (myNovelHelper != null) {
            myNovelHelper.getChapterContent(readingActivity, currentChapter, readStatus.book, false);
        }
        // 刷新页面
        if (dataListener != null) {
            dataListener.freshPage();
        }
        if (pageView != null) {
            pageView.drawCurrentPage();
            pageView.drawNextPage();
            pageView.getChapter(true);
        }
        if (dataListener != null) {
            dataListener.initBookStateDeal();
        }
        //第一次看书 显示章节间广告
//        loadNativeAd();
    }

    public void dealManualDialogShow() {
        AppLog.d("IReadDataFactory", "Constants.manualReadedCount " + Constants.manualReadedCount);
        if (Constants.manualReadedCount != 0) {
            if (Constants.manualReadedCount == Constants.manualTip) {
                AppLog.d("IReadDataFactory", "显示自动阅读提醒");
                if (myNovelHelper != null) {
                    myNovelHelper.showHintAutoReadDialog();
                }
            }
        }
    }

    /**
     * 翻页到下一章的处理
     */
    protected void nextChapterCallBack(boolean drawCurrent) {
        if (readStatus.sequence != -1) {
            statistics();
        }

        Constants.readedCount++;
        preChapter = currentChapter;
        currentChapter = nextChapter;
        nextChapter = null;
        readStatus.sequence++;
        readStatus.offset = 0;
        myNovelHelper.isShown = false;

        myNovelHelper.getChapterContent(readingActivity, currentChapter, readStatus.book, false);
        readStatus.currentPage = 1;
        pageView.drawNextPage();
        if (drawCurrent) {
            pageView.drawCurrentPage();

        }
        pageView.getNextChapter();
        if (dataListener != null) {
            dataListener.downLoadNovelMore();
        }
        if (dataListener != null) {
            dataListener.freshPage();
            dataListener.changeChapter();
        }
        readStatus.isLoading = false;
        // 加载章节间大图相关内容
        loadNativeAd();
    }

    public void statistics() {
        if (statisticManager == null) {
            statisticManager = StatisticManager.getStatisticManager();
        }

        if (readStatus != null && readStatus.novel_basePageView != null) {
            //翻到下一章时处理上一章时处理当前章节广告
            Novel novel = transformation();

            if (readStatus.currentAdInfo_image != null) {
                if (Constants.IS_LANDSCAPE) {
                    statisticManager.schedulingRequest(readingActivity, readStatus.novel_basePageView, readStatus.currentAdInfo_image,
                            novel, StatisticManager.TYPE_END, NativeInit.ad_position[9]);
                } else {
                    statisticManager.schedulingRequest(readingActivity, readStatus.novel_basePageView, readStatus.currentAdInfo_image,
                            novel, StatisticManager.TYPE_END, NativeInit.ad_position[2]);
                }
            }

//            if (readStatus.currentAdInfo_in_chapter != null) {
//                statisticManager.schedulingRequest(readStatus.novel_basePageView, readStatus.currentAdInfo_in_chapter,
//                        novel, StatisticManager.TYPE_END, NativeInit.ad_position[7]);
//            }
//            readStatus.recycleResourceNew();
            //翻到下一章时清除5-2广告容器储存的广告信息
            readStatus.recycleResourceNew();

            //elk pv统计
            ADStatisticManager.getADStatisticManager().onPvStatistics(OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext()), novel, Constants.dy_ad_old_request_switch);
        }
    }

    public Novel transformation() {
        Novel novel = new Novel();
        if (readStatus != null && readStatus.book != null) {
            novel.novelId = readStatus.book.book_id;
            novel.author = readStatus.book.author;
            novel.label = readStatus.book.category;
            novel.adBookName = readStatus.book.name;
            novel.book_source_id = readStatus.book.book_source_id;
        }
        if (currentChapter != null) {
            novel.chapterId = String.valueOf(currentChapter.sort);
            novel.adChapterId = currentChapter.chapter_id;
        } else if (tempCurrentChapter != null) {
            novel.chapterId = String.valueOf(tempCurrentChapter.sort);
            novel.adChapterId = tempCurrentChapter.chapter_id;
        }

        if (readStatus != null && readStatus.book != null && Constants.QG_SOURCE.equals(readStatus.book.site)) {
            novel.channelCode = "A001";
//            novel.ad_QG_bookCategory = readStatus.book.category;
//            novel.ad_QG_bookFenpin = "";
        } else {
            novel.channelCode = "A002";
//            novel.ad_YQ_bookLabel = readStatus.book.category;
        }
        return novel;
    }

    private void loadNativeAd() {
        pageView.loadNatvieAd();
    }

    /**
     * 翻页到上一章的处理
     */
    protected void preChapterCallBack(boolean drawCurrent) {
        readStatus.shouldShowInMobiAdView = false;
        if (readStatus != null) {
            readStatus.recycleResourceNew();
        }

        Constants.readedCount++;
        nextChapter = currentChapter;
        currentChapter = preChapter;
        preChapter = null;
        readStatus.sequence--;
        readStatus.offset = 0;
        myNovelHelper.isShown = false;
        myNovelHelper.getChapterContent(readingActivity, currentChapter, readStatus.book, false);
        if (toChapterStart) {
            readStatus.currentPage = 1;
        } else {
            readStatus.currentPage = readStatus.pageCount;
        }
        toChapterStart = false;
        pageView.drawNextPage();
        if (drawCurrent) {
            pageView.drawCurrentPage();
        }
        pageView.getPreChapter();
        if (dataListener != null) {
            dataListener.freshPage();
            dataListener.changeChapter();
        }
        readStatus.isLoading = false;

        if (readStatus.currentPage == readStatus.pageCount) {
        }
    }

    public void freshPage() {
        if (dataListener != null) {
            dataListener.freshPage();
        }
    }

    public boolean next() {

        saveData();
        boolean isPrepared = false;

        if (readStatus.currentPage < readStatus.pageCount) {
            readStatus.currentPage++;
            if (dataListener != null) {
                dataListener.freshPage();
            }
            isPrepared = true;
            if (readStatus.currentPage == readStatus.pageCount) {

            }
        } else {

            sendPVData();

            if (readStatus.sequence == readStatus.chapterCount - 1) {
                if (readStatus.book.book_type != 0 && dataListener != null) {
                    dataListener.showToast(R.string.last_chapter_tip);
                }
                if (readStatus.book.book_type == 0) {
                    getNextChapter();
                }

                return false;
            }
            nextChapter = null;
            isPrepared = getNextChapter() != null;
            if (isPrepared || readStatus.book.book_type != 0) {
                nextChapterCallBack(false);
                if (currentChapter.status != Chapter.Status.CONTENT_NORMAL) {
                    isPrepared = false;
                }
            }
        }

        return isPrepared;
    }

    private void sendPVData() {
        Constants.endReadTime = System.currentTimeMillis() / 1000L;
        HashMap<String, String> params = new HashMap<>();
        params.put("book_id", readStatus.book_id);
        String book_source_id;
        if (Constants.QG_SOURCE.equals(readStatus.book.site)) {
            book_source_id = readStatus.book.book_id;
        } else {
            book_source_id = readStatus.book.book_source_id;
        }

        params.put("book_source_id", book_source_id);
        if (currentChapter != null) {
            params.put("chapter_id", currentChapter.chapter_id);
        }
        String channelCode;

        if (Constants.QG_SOURCE.equals(readStatus.book.site)) {
            channelCode = "1";
        } else {
            channelCode = "2";
        }
        params.put("channel_code", channelCode);
        params.put("chapter_read", "1");
        params.put("chapter_pages", String.valueOf(readStatus.pageCount));
        params.put("start_time", String.valueOf(Constants.startReadTime));
        params.put("end_time", String.valueOf(Constants.endReadTime));
        if (statisticManager == null) {
            statisticManager = StatisticManager.getStatisticManager();
        }
        statisticManager.sendReadPvData(params);
    }

    public boolean nextByAutoRead() {

        saveData();
        boolean isPrepared = false;

        if (readStatus.currentPage < readStatus.pageCount) {
            readStatus.currentPage++;
            if (dataListener != null) {
                dataListener.freshPage();
            }
            isPrepared = true;
            if (readStatus.currentPage == readStatus.pageCount) {
            }
        } else {
            nextChapter = null;
            if (readStatus.sequence < readStatus.chapterCount - 1) {
                if (BookHelper.isChapterExist(readStatus.sequence + 1, readStatus.book_id)) {
                    nextChapter = getChapterByAuto(ReadingActivity.MSG_LOAD_NEXT_CHAPTER, readStatus.sequence
                            + 1);
                }
            }
            isPrepared = nextChapter == null ? false : true;
            if (isPrepared) {
                nextChapterCallBack(false);
                if (currentChapter.status != Chapter.Status.CONTENT_NORMAL) {
                    isPrepared = false;
                }
            }
        }

        return isPrepared;
    }

    public boolean previous() {
        saveData();
        boolean isPrepared = false;
        if (readStatus.currentPage > 1) {
            readStatus.currentPage--;
            if (dataListener != null) {
                dataListener.freshPage();
            }
            isPrepared = true;
        } else {
            if (readStatus.sequence == -1) {
                if (dataListener != null) {
                    dataListener.showToast(R.string.is_first_chapter);
                }
                return false;
            }

            preChapter = null;
            isPrepared = getPreviousChapter() != null;
            if (isPrepared || readStatus.book.book_type != 0) {
                preChapterCallBack(false);
            }
        }
        return isPrepared;
    }

    protected Chapter getChapterByAuto(int what, int sequence) {
        if (chapterList == null || chapterList.isEmpty()) {
            chapterList = new BookChapterDao(mContext, readStatus.book_id).queryBookChapter();
            return null;
        }
        if (sequence < 0) {
            sequence = 0;
        } else if (sequence >= chapterList.size()) {
            sequence = chapterList.size() - 1;
        }
        Chapter chapter = chapterList.get(sequence);
        try {
            String content = DataCache.getChapterFromCache(chapter.sequence, chapter.book_id);
            if (!TextUtils.isEmpty(content) && !("null".equals(content) || "isChapterExists".equals(content))) {
                chapter.content = content;
                chapter.isSuccess = true;
            } else {
                chapter = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return chapter;
    }

    public abstract void getChapterByLoading(final int what, int sequence);

    public abstract Chapter getNextChapter();

    public abstract Chapter getPreviousChapter();

    protected abstract Chapter getChapter(int what, int sequence);

    public void recycleResource() {

        if (this.mContext != null) {
            this.mContext = null;
        }

        if (this.readingActivity != null) {
            this.readingActivity = null;
        }

        if (this.readStatus != null) {
            this.readStatus = null;
        }

        if (this.pageView != null) {
            this.pageView = null;
        }

        if (this.chapterList != null) {
            this.chapterList.clear();
            this.chapterList = null;
        }

        if (this.tempLineList != null) {
            this.tempLineList.clear();
            this.tempLineList = null;
        }

        if (this.nextChapter != null) {
            this.nextChapter = null;
        }

        if (this.tempNextChapter != null) {
            this.tempNextChapter = null;
        }

        if (this.preChapter != null) {
            this.preChapter = null;
        }

        if (this.tempPreviousChapter != null) {
            this.tempPreviousChapter = null;
        }

        if (this.currentChapter != null) {
            this.currentChapter = null;
        }

        if (this.tempCurrentChapter != null) {
            this.tempCurrentChapter = null;
        }
    }

    public interface ReadDataListener {
        void freshPage();

        void gotoOver();

        void showToast(int str);

        void downLoadNovelMore();

        void initBookStateDeal();

        void changeChapter();

        void showChangeNetDialog();
    }

    public static class ReadHandler extends Handler {
        public boolean canHandleMessage = true;
        private WeakReference<IReadDataFactory> reference;

        ReadHandler(IReadDataFactory instance) {
            reference = new WeakReference<IReadDataFactory>(instance);
        }

        @Override
        public void handleMessage(Message msg) {
            IReadDataFactory dataFactory = reference.get();
            if (!canHandleMessage || dataFactory == null) {
                return;
            }
            if (dataFactory.loadingPage != null) {
                dataFactory.loadingPage.onSuccess();
            }
            switch (msg.what) {
                case ReadingActivity.MSG_LOAD_CUR_CHAPTER:
                    dataFactory.loadCurrentChapter(msg);
                    break;
                case ReadingActivity.MSG_LOAD_NEXT_CHAPTER:
                    dataFactory.loadNextChapter(msg);
                    break;
                case ReadingActivity.MSG_LOAD_PRE_CHAPTER:
                    dataFactory.loadPreChapter(msg);
                    break;
                case ReadingActivity.MSG_SEARCH_CHAPTER:
                    dataFactory.loadSearchChapter(msg);
                    break;
                case ReadingActivity.MSG_CHANGE_SOURCE:
                    dataFactory.loadChangeSource(msg);
                    break;
                case ReadingActivity.MSG_JUMP_CHAPTER:
                    dataFactory.loadJumpChapter(msg);
                    break;
                case ReadingActivity.ERROR:
                    dataFactory.loadError(msg);
                    break;
                case ReadingActivity.NEED_LOGIN:
                    dataFactory.loadNeedLogin(msg);
                    break;
                default:

                    break;
            }
        }
    }
}
