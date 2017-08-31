package com.intelligent.reader.read.help;

import com.intelligent.reader.activity.ReadingActivity;

import net.lzbook.kit.R;
import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.ReadStatus;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.data.db.BookChapterDao;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.net.volley.request.VolleyDataService;
import net.lzbook.kit.request.DataCache;
import net.lzbook.kit.request.RequestFactory;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.NetWorkUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class ReadDataFactory extends IReadDataFactory {
    private BookChapterDao mBookChapterDao;
    private BookDaoHelper mBookDaoHelper;

    private RequestFactory requestFactory;

    public ReadDataFactory(Context context, ReadingActivity readingActivity, ReadStatus readStatus, NovelHelper novelHelper) {
        super(context, readingActivity, readStatus, novelHelper);
        requestFactory = new RequestFactory();
        getCustomLoadingPage();
    }

    /**
     * 获取章节内容
     */
    @Override
    public void getChapterByLoading(final int what, int sequence) {

        if (sequence < -1) {
            sequence = -1;
        } else if (chapterList != null && chapterList.size() > 0 && sequence + 1 > chapterList.size()) {
            sequence = chapterList.size() - 1;
        }
        final int temp_sequence = sequence;

        if (mBookDaoHelper == null) {
            mBookDaoHelper = BookDaoHelper.getInstance(mContext);
        }
        if (mBookChapterDao == null) {
            mBookChapterDao = new BookChapterDao(mContext, readStatus.book_id);
        }

        loadingPage = getCustomLoadingPage();

        loadingPage.loading(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final RequestItem requestItem = readStatus.getRequestItem();
                if (requestItem != null && !Constants.SG_SOURCE.equals(requestItem.host)) {
                    if (chapterList == null || chapterList.isEmpty()) {
                        requestFactory.requestExecutor(requestItem).requestChapterList(mContext, requestItem, new VolleyDataService.DataServiceCallBack() {
                            @Override
                            public void onSuccess(final Object result) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!readingActivity.isFinishing()) {
                                            ArrayList<Chapter> chapterList = (ArrayList<Chapter>) result;
                                            sendChapter(what, requestItem, temp_sequence, chapterList);
                                        }
                                    }
                                }).start();

                            }

                            @Override
                            public void onError(Exception error) {
                                mHandler.obtainMessage(ReadingActivity.ERROR).sendToTarget();
                                if (loadingPage != null) {
                                    loadingPage.onError();
                                }
                            }

                        });
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                sendChapter(what, requestItem, temp_sequence, chapterList);
                            }
                        }).start();
                    }
                }

                return null;
            }
        });
        loadingError(loadingPage);
    }

    public void sendChapter(int what, RequestItem requestItem, int temp_sequence, ArrayList<Chapter> chapterList) {
        try {
            if (chapterList == null) {
                return;
            } else {
                ReadDataFactory.this.chapterList = chapterList;
                readStatus.book.extra_parameter = requestItem.extra_parameter;
            }
            readStatus.chapterCount = chapterList.size();

            if (temp_sequence == -1) {
                Chapter result = new Chapter();
                result.chapter_name = "";
                result.content = "";
                mHandler.obtainMessage(what, result).sendToTarget();
                setLoadingCurl(500);
                return;
            }

            AppLog.e("ReadDataFactory", "ReadDataFactory: " + temp_sequence + " : " + readStatus.book_id);
            Chapter result = chapterList.get(temp_sequence);
            if (Constants.QG_SOURCE.equals(readStatus.requestItem.host)) {
                if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
                    result = requestFactory.requestExecutor(requestItem).requestSingleChapter(readStatus.book.dex, mBookDaoHelper,
                            mBookChapterDao, result);
                    Constants.startReadTime = System.currentTimeMillis() / 1000L;
                } else {
                    if (com.quduquxie.network.DataCache.isChapterExists(result.chapter_id, result.book_id)) {
                        result.content = com.quduquxie.network.DataCache.getChapterFromCache(result.chapter_id, result.book_id);
                        result.isSuccess = true;
                        Constants.startReadTime = System.currentTimeMillis() / 1000L;
                    } else {
                        //提示网络不给力
                    }
                }
            } else {
                if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE || BookHelper.isChapterExist(temp_sequence, readStatus.book_id)) {
                    result = requestFactory.requestExecutor(requestItem).requestSingleChapter(readStatus.book.dex, mBookDaoHelper,
                            mBookChapterDao, result);
                    if (result == null) {
                        result = requestFactory.requestExecutor(requestItem).requestSingleChapter(readStatus.book.dex, mBookDaoHelper,
                                mBookChapterDao, result);
                    }
                    Constants.startReadTime = System.currentTimeMillis() / 1000L;
                }
            }

            mHandler.obtainMessage(what, result).sendToTarget();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * loading 页面显示原网页地址
     */
    private void setLoadingCurl(int time) throws InterruptedException {
        if (chapterList != null && !chapterList.isEmpty()) {
            Chapter firstChapter = chapterList.get(0);
            if (firstChapter != null) {
                //if (readStatus.book.dex == 0) {
                readStatus.firstChapterCurl = firstChapter.curl;
                /*} else if (readStatus.book.dex == 1) {
                    readStatus.firstChapterCurl = firstChapter.curl1;
                }*/

                loadingPage.setNovelSource(readStatus.firstChapterCurl);
                Thread.sleep(time);
            }
        }
    }

    @Override
    public Chapter getNextChapter() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(BaseBookApplication.getGlobalContext());
        long last_read = sharedPreferences.getLong(Constants.LAST_READ, 0);
        long currentTime = System.currentTimeMillis();
        long noNetRead = sharedPreferences.getLong(Constants.NONET_READ, 0);
        int nonet_readhour = sharedPreferences.getInt(Constants.NONET_READTIME, 1);
        sharedPreferences.edit().putLong(Constants.LAST_READ, currentTime).apply();

        if(readStatus.requestItem == null || readStatus.requestItem.host == null || chapterList == null){
            return null;
        }

        if (nextChapter != null) {
            if (readStatus.sequence == readStatus.chapterCount - 1) {
                if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
                    readStatus.isLoading = true;
                    if (dataListener != null) {
                        dataListener.gotoOver();
                        statistics();
                    }
                } else if (dataListener != null) {
                    dataListener.showToast(R.string.err_no_net);
                }
            }
        } else if (readStatus.sequence < readStatus.chapterCount - 1) {
            if (Constants.QG_SOURCE.equals(readStatus.requestItem.host)) {
                Chapter tempChapter = chapterList.get(readStatus.sequence + 1);
                if (com.quduquxie.network.DataCache.isChapterExists(tempChapter.chapter_id, tempChapter.book_id)) {
                    if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE && Constants.isNoNetRead == 1) {
                        double noNetRead_hour = (noNetRead / 1000) / (60 * 60);
                        if (noNetRead_hour >= nonet_readhour) {
                            dataListener.showChangeNetDialog();
                        } else {
                            tempChapter.content = com.quduquxie.network.DataCache.getChapterFromCache(tempChapter.chapter_id, tempChapter.book_id);
                            tempChapter.isSuccess = true;
                            nextChapter = tempChapter;
                            noNetRead += currentTime - last_read;
                        }
                        sharedPreferences.edit().putLong(Constants.NONET_READ, noNetRead).apply();
                    } else {
                        tempChapter.content = com.quduquxie.network.DataCache.getChapterFromCache(tempChapter.chapter_id, tempChapter.book_id);
                        tempChapter.isSuccess = true;
                        nextChapter = tempChapter;
                    }
                } else {
                    if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
                        readStatus.isLoading = true;
                        getChapterByLoading(ReadingActivity.MSG_LOAD_NEXT_CHAPTER, readStatus.sequence + 1);
                    } else if (dataListener != null) {//提示网络不给力
                        dataListener.showToast(R.string.err_no_net);
                    }
                }
            } else {
                if (BookHelper.isChapterExist(readStatus.sequence + 1, readStatus.book_id)) {
                    Chapter chapter = getChapter(ReadingActivity.MSG_LOAD_NEXT_CHAPTER, readStatus.sequence + 1);
                    if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
                        if (chapter != null) {
                            if (!TextUtils.isEmpty(chapter.content)) {
                                if ((chapter.content.length()) <= Constants.CONTENT_ERROR_COUNT) {
                                    readStatus.isLoading = true;
                                    getChapterByLoading(ReadingActivity.MSG_LOAD_NEXT_CHAPTER, readStatus.sequence + 1);
                                } else {
                                    nextChapter = chapter;
                                }
                            } else {
                                nextChapter = chapter;
                            }
                        } else {
                            nextChapter = chapter;
                        }
                    } else {
                        if (Constants.isNoNetRead == 1) {
                            double noNetRead_hour = (noNetRead / 1000) / (60 * 60);
                            if (noNetRead_hour >= nonet_readhour) {
                                dataListener.showChangeNetDialog();
                            } else {
                                nextChapter = chapter;
                                noNetRead += currentTime - last_read;
                            }
                            sharedPreferences.edit().putLong(Constants.NONET_READ, noNetRead).apply();
                        } else {
                            nextChapter = chapter;
                        }
                    }
                } else {
                    if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
                        readStatus.isLoading = true;
                        getChapterByLoading(ReadingActivity.MSG_LOAD_NEXT_CHAPTER, readStatus.sequence + 1);
                    } else if (dataListener != null) {
                        dataListener.showToast(R.string.err_no_net);
                    }
                }
            }
        } else {
            if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
                readStatus.isLoading = true;
                if (dataListener != null) {
                    dataListener.gotoOver();
                    statistics();
                }
            } else if (dataListener != null) {
                dataListener.showToast(R.string.err_no_net);
            }
        }
        Constants.startReadTime = System.currentTimeMillis() / 1000L;
        return nextChapter;
    }

    @Override
    public Chapter getPreviousChapter() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(BaseBookApplication.getGlobalContext());
        long last_read = sharedPreferences.getLong(Constants.LAST_READ, 0);
        long currentTime = System.currentTimeMillis();
        long noNetRead = sharedPreferences.getLong(Constants.NONET_READ, 0);
        int nonet_readhour = sharedPreferences.getInt(Constants.NONET_READTIME, 1);
        sharedPreferences.edit().putLong(Constants.LAST_READ, currentTime).apply();

        if (readStatus.sequence == 0) {
            preChapter = new Chapter();
            preChapter.content = "";
            preChapter.chapter_name = "";
            preChapter.isSuccess = true;

        } else if (preChapter != null) {

        } else if (readStatus.sequence > 0) {
            if (Constants.QG_SOURCE.equals(readStatus.requestItem.host) && chapterList != null && chapterList.size() > (readStatus.sequence - 1)) {
                Chapter tempChapter = chapterList.get(readStatus.sequence - 1);
                if (com.quduquxie.network.DataCache.isChapterExists(tempChapter.chapter_id, tempChapter.book_id)) {
                    if (NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE && Constants.isNoNetRead == 1) {
                        double noNetRead_hour = (noNetRead / 1000) / (60 * 60);
                        if (noNetRead_hour >= nonet_readhour) {
                            dataListener.showChangeNetDialog();
                        } else {
                            tempChapter.content = com.quduquxie.network.DataCache.getChapterFromCache(tempChapter.chapter_id, tempChapter.book_id);
                            tempChapter.isSuccess = true;
                            preChapter = tempChapter;
                            noNetRead += currentTime - last_read;
                        }
                        sharedPreferences.edit().putLong(Constants.NONET_READ, noNetRead).apply();
                    } else {
                        tempChapter.content = com.quduquxie.network.DataCache.getChapterFromCache(tempChapter.chapter_id, tempChapter.book_id);
                        tempChapter.isSuccess = true;
                        preChapter = tempChapter;
                    }
                } else {
                    if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
                        readStatus.isLoading = true;
                        getChapterByLoading(ReadingActivity.MSG_LOAD_PRE_CHAPTER, readStatus.sequence - 1);
                    } else if (dataListener != null) {//提示网络不给力
                        dataListener.showToast(R.string.err_no_net);
                    }
                }
            } else {
                if (BookHelper.isChapterExist(readStatus.sequence - 1, readStatus.book_id)) {
                    Chapter chapter = getChapter(ReadingActivity.MSG_LOAD_PRE_CHAPTER, readStatus.sequence - 1);
                    if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
                        if (chapter != null) {
                            if (!TextUtils.isEmpty(chapter.content)) {
                                if ((chapter.content.length()) <= Constants.CONTENT_ERROR_COUNT) {
                                    readStatus.isLoading = true;
                                    getChapterByLoading(ReadingActivity.MSG_LOAD_PRE_CHAPTER, readStatus.sequence - 1);
                                } else {
                                    preChapter = chapter;
                                }
                            } else {
                                preChapter = chapter;
                            }
                        } else {
                            preChapter = chapter;
                        }
                    } else {
                        if (Constants.isNoNetRead == 1) {
                            double noNetRead_hour = (noNetRead / 1000) / (60 * 60);
                            if (noNetRead_hour >= nonet_readhour) {
                                dataListener.showChangeNetDialog();
                            } else {
                                preChapter = chapter;
                                noNetRead += currentTime - last_read;
                            }
                            sharedPreferences.edit().putLong(Constants.NONET_READ, noNetRead).apply();
                        } else {
                            preChapter = chapter;
                        }

                    }
                } else {
                    if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
                        readStatus.isLoading = true;
                        getChapterByLoading(ReadingActivity.MSG_LOAD_PRE_CHAPTER, readStatus.sequence - 1);
                    } else {
                        if (dataListener != null) {
                            dataListener.showToast(R.string.err_no_net);
                        }
                    }
                }
            }
        } else {
            // 第一页
            if (dataListener != null) {
                dataListener.showToast(R.string.is_first_chapter);
            }
        }
        Constants.startReadTime = System.currentTimeMillis() / 1000L;
        return preChapter;
    }

    @Override
    protected Chapter getChapter(int what, int sequence) {
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
                getChapterByLoading(what, sequence);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return chapter;
    }


}
