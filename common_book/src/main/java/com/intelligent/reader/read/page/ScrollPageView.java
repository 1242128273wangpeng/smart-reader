package com.intelligent.reader.read.page;

import com.intelligent.reader.R;
import com.intelligent.reader.activity.ReadingActivity;
import com.intelligent.reader.read.animation.BitmapManager;
import com.intelligent.reader.read.help.CallBack;
import com.intelligent.reader.read.help.DrawTextHelper;
import com.intelligent.reader.read.help.NovelHelper;
import com.intelligent.reader.reader.ReaderViewModel;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.NovelLineBean;
import net.lzbook.kit.data.bean.ReadConfig;
import net.lzbook.kit.data.bean.ReadStatus;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ScrollPageView extends LinearLayout implements PageInterface, View.OnClickListener {
    private final MHandler handler = new MHandler(this);
    public Chapter tempChapter;
    boolean nextResult = false;
    ArrayList<NovelLineBean> chapterNameList = new ArrayList<NovelLineBean>();
    private FListView page_list;
    private ArrayList<ArrayList<NovelLineBean>> chapterContent;
    private ArrayList<ArrayList<NovelLineBean>> preChaperConent;
    private ArrayList<ArrayList<NovelLineBean>> currentChaperConent;
    private ArrayList<ArrayList<NovelLineBean>> nextChaperContent;
    private Chapter preChapter;
    private Chapter curChapter;
    private Chapter nextChapter;
    private int preSize;
    private int currentSize;
    private int nextSize;
    private NovelHelper novelHelper;
    private ReadStatus readStatus;
    private DrawTextHelper drawTextHelper;
    private ReaderViewModel dataFactory;
    private ScrollPageAdapter adapter;
    private boolean loadingData = false;
    private int width, height;
    private float lastY;
    private int totalItemCount;
    private int lastVisible = -1;
    private int firstVisibleItem = -1;
    private CallBack callBack;
    private Context mContext;
    private Activity mActivity;
    private TextView novel_time;
    private TextView novel_chapter;
    private TextView novel_page;
    private TextView novel_title;
    private BitmapManager manager;
    private long startTouchTime;
    private int startTouchX;
    private int startTouchY;
    private boolean isNeedToNextPage = false;
    private BatteryView novel_content_battery_view;

    private TextView mOriginTv;
    private TextView mTransCodingTv;

    private OnOperationClickListener mOnOperationClickListener;
    private long endTime;//阅读结束时间
    private int count = 0;//用于记录第一次进来时次数（用户画像打点）
    private int markPosition;//标记是否是向下滑动
    private boolean isFirstCome = true;//用于记录当前是否是第一次进来（用户画像打点）

    public ScrollPageView(Context context) {
        super(context);
        this.mContext = context;
    }

    public ScrollPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = readStatus.screenWidth = w;
        height = readStatus.screenHeight = h;


        if (callBack != null && (Math.abs(oldh - h) > AppUtils.dip2px(mContext, 26))) {
            if (android.os.Build.VERSION.SDK_INT < 11 && Constants.isFullWindowRead) {
                height = readStatus.screenHeight - AppUtils.dip2px(mContext, 20);
            } else {
                height = readStatus.screenHeight - AppUtils.dip2px(mContext, 40);
            }
            callBack.onResize();
            setBackground();
            if (chapterContent != null) {
                getChapter(true);
            }
        }
    }

    @Override
    public void init(Activity activity, ReadStatus readStatus, NovelHelper novelHelper) {
        this.mActivity = activity;
//        View view = LayoutInflater.from(mContext).inflate(R.layout.scroll_page, this);
//        page_list = (FListView) view.findViewById(R.id.page_list);
//        novel_time = (TextView) view.findViewById(R.id.novel_time);
//        novel_chapter = (TextView) view.findViewById(R.id.novel_chapter);
//        novel_title = (TextView) view.findViewById(R.id.novel_title);
//        novel_page = (TextView) view.findViewById(R.id.novel_page);
//        novel_content_battery_view = (BatteryView) view.findViewById(R.id.novel_content_battery_view);
        mOriginTv = (TextView) findViewById(R.id.origin_tv);
        mTransCodingTv = (TextView) findViewById(R.id.trans_coding_tv);
        mOriginTv.setOnClickListener(this);
        mTransCodingTv.setOnClickListener(this);

        this.novelHelper = novelHelper;
        this.readStatus = readStatus;

        chapterContent = new ArrayList<>();

        drawTextHelper = new DrawTextHelper(getResources());
        readStatus.startReadTime = System.currentTimeMillis();
        count = 0;
        isFirstCome = true;
        manager = BitmapManager.getInstance();
        manager.setSize(readStatus.screenWidth, readStatus.screenHeight);
        adapter = new ScrollPageAdapter();
        page_list.setAdapter(adapter);


        page_list.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                ScrollPageView.this.totalItemCount = totalItemCount;
                ScrollPageView.this.firstVisibleItem = firstVisibleItem;
                int lastVisible = page_list.getLastVisiblePosition();
                if (ScrollPageView.this.lastVisible != lastVisible) {
                    ScrollPageView.this.lastVisible = lastVisible;
                    ScrollPageView.this.readStatus.currentPage = getCurrentPage(lastVisible);
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (readStatus.isMenuShow) {
            callBack.onShowMenu(false);
            return false;
        }
        int tmpX = (int) event.getX();
        int tmpY = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTouchTime = System.currentTimeMillis();
                lastY = event.getY();
                startTouchX = tmpX;
                startTouchY = tmpY;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.e("Scroll", "TouchEvent: lastY : " + lastY);
                Log.e("Scroll", "TouchEvent: event.getY() : " + event.getY());
                Log.e("Scroll", "TouchEvent: totalItemCount : " + totalItemCount);
                Log.e("Scroll", "TouchEvent: lastVisible : " + (lastVisible + 1));
                Log.e("Scroll", "TouchEvent: readStatus.currentPage : " + readStatus.currentPage);
                Log.e("Scroll", "TouchEvent: readStatus.pageCount : " + readStatus.pageCount);
                if (!loadingData && lastY - event.getY() > 20 && totalItemCount == lastVisible + 1
                        && readStatus.currentPage == readStatus.pageCount) {
                    loadingData = true;
                    isNeedToNextPage = false;
                    boolean result = dataFactory.next();
                    if (!result) {
                        handler.sendEmptyMessageDelayed(0, 1000);
                    }
                } else if (!loadingData && lastY - event.getY() < -20 &&
                        firstVisibleItem == 0 && readStatus.currentPage <= 1) {
                    loadingData = true;

                    if (currentChaperConent != null && currentChaperConent.size() > 0) {
                        ArrayList<NovelLineBean> arrayList = currentChaperConent.get(0);
                        if (arrayList != null && arrayList.size() > 0 &&
                                arrayList.get(0).getLineContent().indexOf("txtzsydsq_homepage") > -1) {
                            loadingData = false;
                            if (page_list.getChildCount() > 1 && page_list.getChildAt(1) != null &&
                                    Math.abs(page_list.getChildAt(1).getTop() - height) < 10) {
                                Toast.makeText(mContext, R.string.is_first_chapter, Toast.LENGTH_SHORT).show();
                            }
                            return false;
                        }
                    }
                    if (preChaperConent != null && preChaperConent.size() > 0) {
                        ArrayList<NovelLineBean> arrayList = preChaperConent.get(0);
                        if (arrayList != null && arrayList.size() > 0 &&
                                arrayList.get(0).getLineContent().indexOf("txtzsydsq_homepage") > -1) {
                            loadingData = false;
                            if (page_list.getChildCount() > 1 && page_list.getChildAt(1) != null &&
                                    Math.abs(page_list.getChildAt(1).getTop() - height) < 10) {
                                Toast.makeText(mContext, R.string.is_first_chapter, Toast.LENGTH_SHORT).show();
                            }
                            return false;
                        }
                    }
                    boolean result = dataFactory.previous();
                    if (!result) {
                        handler.sendEmptyMessageDelayed(0, 1000);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                long touchTime = System.currentTimeMillis() - startTouchTime;
                int distance = (int) Math.sqrt(Math.pow(startTouchX - tmpX, 2) + Math.pow(startTouchY - tmpY, 2));
                if (touchTime < 100 && distance < 30 || distance < 10) {
                    onClick(event);
                }
                startTouchTime = 0;
                break;
            case MotionEvent.ACTION_CANCEL:
                startTouchTime = 0;
                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(event);
    }

    private void onClick(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        int h4 = height / 4;
        int w3 = width / 3;
        if (x <= w3) {

        } else if (x >= width - w3 || (y >= height - h4 && x >= w3)) {

        } else {
            if (callBack != null) {
                callBack.onShowMenu(true);
            }
        }
    }

    public void changeLoadState() {
        loadingData = false;
    }

    private int getCurrentSequence(int position) {
        if (preSize == 0) {
            if (position <= currentSize) {
                if (curChapter != null) {
                    chapterNameList = curChapter.chapterNameList;
                }

            } else if (position <= currentSize + nextSize) {
                if (nextChapter != null) {
                    chapterNameList = nextChapter.chapterNameList;
                }
            }
        } else if (nextSize == 0) {
            if (position <= preSize) {
                if (preChapter != null) {
                    chapterNameList = preChapter.chapterNameList;
                }

            } else if (position <= currentSize + preSize) {
                if (curChapter != null) {
                    chapterNameList = curChapter.chapterNameList;
                }
            }
        } else {
            if (position <= preSize) {
                if (preChapter != null) {
                    chapterNameList = preChapter.chapterNameList;
                }

            } else if (position <= currentSize + preSize) {
                if (curChapter != null) {
                    chapterNameList = curChapter.chapterNameList;
                }
            } else if (position <= nextSize + preSize + currentSize) {
                position = position - currentSize - preSize;
                if (nextChapter != null) {
                    chapterNameList = nextChapter.chapterNameList;
                }

            }
        }

        return position;
    }


    private int getCurrentPage(int position) {
        String chapter_name = "";
        if (preSize == 0) {
            if (position <= currentSize) {
                readStatus.pageCount = currentSize;
                if (curChapter != null) {
                    tempChapter = curChapter;
                    readStatus.sequence = curChapter.sequence;
                    chapter_name = curChapter.chapter_name;
//					chapterNameList = curChapter.chapterNameList;
                }
                if (count > 2) {
                    AppLog.e("count", count + "===" + readStatus.pageCount + "===" + readStatus.sequence + "===" + position);
                    endTime = System.currentTimeMillis();
                    addLog(endTime, position, readStatus.pageCount, readStatus.sequence);
                }

                count++;

            } else if (position <= currentSize + nextSize) {
                position = position - currentSize;
                readStatus.pageCount = nextSize;
                if (nextChapter != null) {
                    tempChapter = nextChapter;
                    readStatus.sequence = nextChapter.sequence;
                    chapter_name = nextChapter.chapter_name;
//					chapterNameList = nextChapter.chapterNameList;
                }
                endTime = System.currentTimeMillis();
                addLog(endTime, position, readStatus.pageCount, readStatus.sequence);
                count++;
            }
        } else if (nextSize == 0) {
            if (position <= preSize) {
                readStatus.pageCount = preSize;
                if (preChapter != null) {
                    tempChapter = preChapter;
                    readStatus.sequence = preChapter.sequence;
                    chapter_name = preChapter.chapter_name;
//					chapterNameList = preChapter.chapterNameList;
                }

            } else if (position <= currentSize + preSize) {
                position = position - preSize;
                readStatus.pageCount = currentSize;
                if (curChapter != null) {
                    tempChapter = curChapter;
                    chapter_name = curChapter.chapter_name;
                    readStatus.sequence = curChapter.sequence;
//					chapterNameList = curChapter.chapterNameList;
                }
            }
        } else {
            if (position <= preSize) {
                readStatus.pageCount = preSize;
                if (preChapter != null) {
                    tempChapter = preChapter;
                    chapter_name = preChapter.chapter_name;
                    readStatus.sequence = preChapter.sequence;
//					chapterNameList = preChapter.chapterNameList;
                }

            } else if (position <= currentSize + preSize) {
                position = position - preSize;
                readStatus.pageCount = currentSize;
                if (curChapter != null) {
                    tempChapter = curChapter;
                    chapter_name = curChapter.chapter_name;
                    readStatus.sequence = curChapter.sequence;
//					chapterNameList = curChapter.chapterNameList;
                }
            } else if (position <= nextSize + preSize + currentSize) {
                position = position - currentSize - preSize;
                readStatus.pageCount = nextSize;
                if (nextChapter != null) {
                    tempChapter = nextChapter;
                    chapter_name = nextChapter.chapter_name;
                    readStatus.sequence = nextChapter.sequence;
//					chapterNameList = nextChapter.chapterNameList;
                }
                endTime = System.currentTimeMillis();
                addLog(endTime, position, readStatus.pageCount, readStatus.sequence);

            }
        }

        if (readStatus.sequence == -1) {
            novel_content_battery_view.setVisibility(GONE);
            novel_time.setVisibility(GONE);
            novel_chapter.setVisibility(GONE);
            novel_page.setVisibility(GONE);
            mOriginTv.setVisibility(GONE);
            mTransCodingTv.setVisibility(GONE);
        } else {
            novel_content_battery_view.setVisibility(VISIBLE);
            novel_time.setVisibility(VISIBLE);
            novel_chapter.setVisibility(VISIBLE);
            novel_page.setVisibility(VISIBLE);
            mOriginTv.setVisibility(VISIBLE);
            mTransCodingTv.setVisibility(VISIBLE);
        }

        readStatus.chapterName = chapter_name;
//		if (chapterNameList != null) {
//			readStatus.chapterNameList = chapterNameList;

        if (mContext instanceof ReadingActivity) {
            ((ReadingActivity) mContext).freshPage();
        }
//		}
        /**
         * 上下模式中小说左上角标题超过字数做缩略处理
         */
        if (!TextUtils.isEmpty(chapter_name)) {
            chapter_name = chapter_name.replace("\n", "");
            if (chapter_name.length() > 18) {
                chapter_name = chapter_name.substring(0, 18) + "...";
            }
        }


        novel_title.setText(chapter_name);
        novel_chapter.setText((readStatus.sequence + 1) + "/" + readStatus.chapterCount + "章");
        novel_page.setText("本章第" + position + "/" + readStatus.pageCount);
        // lastResult = position;
        novelHelper.getPageContentScroll();
        dataFactory.freshPage();
        return position;
    }

    /**
     * 用户画像打点
     *
     * @param endTime   阅读结束时间
     * @param position  当前第几页
     * @param pagecount 当前章的总页数
     * @param sequence  当前第几张
     */
    public void addLog(long endTime, int position, int pagecount, int sequence) {
        //判断章节的最后一页
        if (sequence > readStatus.lastSequenceRemark && !isFirstCome && readStatus.requestItem != null) {
            //按照此顺序传值 当前的book_id，阅读章节，书籍源，章节总页数，当前阅读页，当前页总字数，当前页面来自，开始阅读时间,结束时间,阅读时间,是否有阅读中间退出行为,书籍来源1为青果，2为智能
            StartLogClickUtil.upLoadReadContent(readStatus.book_id, readStatus.lastChapterId + "", readStatus.source_ids, readStatus.lastPageCount + "",
                    readStatus.lastCurrentPageRemark + "", readStatus.currentPageConentLength + "", readStatus.requestItem.fromType + "",
                    readStatus.startReadTime + "", endTime + "", endTime - readStatus.startReadTime + "", "false", readStatus.requestItem.channel_code + "");

        } else {
            if (readStatus.requestItem != null && dataFactory != null && dataFactory.getCurrentChapter() != null && markPosition < position) {
                //按照此顺序传值 当前的book_id，阅读章节，书籍源，章节总页数，当前阅读页，当前页总字数，当前页面来自，开始阅读时间,结束时间,阅读时间,是否有阅读中间退出行为,书籍来源1为青果，2为智能
                StartLogClickUtil.upLoadReadContent(readStatus.book_id, dataFactory.getCurrentChapter().chapter_id + "", readStatus.source_ids, readStatus.pageCount + "",
                        position - 1 + "", readStatus.currentPageConentLength + "", readStatus.requestItem.fromType + "",
                        readStatus.startReadTime + "", endTime + "", endTime - readStatus.startReadTime + "", "false", readStatus.requestItem.channel_code + "");
                readStatus.lastChapterId = dataFactory.getCurrentChapter().chapter_id;
                readStatus.requestItem.fromType = 2;
            }
        }

        readStatus.startReadTime = endTime;

        readStatus.lastSequenceRemark = sequence;
        readStatus.lastCurrentPageRemark = position;
        readStatus.lastPageCount = pagecount;
        markPosition = position;
        isFirstCome = false;
    }


    private void getChapterSize() {

        if (preChaperConent != null) {
            preSize = preChaperConent.size();
        } else {
            preSize = 0;
        }
        if (currentChaperConent != null) {
            currentSize = currentChaperConent.size();
        }
        if (nextChaperContent != null) {
            nextSize = nextChaperContent.size();
        }
    }

    @Override
    public void setViewModel(ReaderViewModel mReaderViewModel) {
        this.dataFactory = mReaderViewModel;
    }

    @Override
    public void setFirstPage(boolean firstPage) {

    }

    @Override
    public void getChapter(boolean needSavePage) {
        if (readStatus.mLineList == null) {
            return;
        }
        chapterContent.clear();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        preChapter = null;
        nextChapter = null;
        currentChaperConent = null;
        nextChaperContent = null;
        preChaperConent = null;

        if (dataFactory != null) {
            curChapter = dataFactory.getCurrentChapter();
        }
        if (curChapter == null)
            return;

        curChapter.chapterNameList = readStatus.chapterNameList;
        currentChaperConent = readStatus.mLineList;
        chapterContent.addAll(currentChaperConent);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        loadingData = false;
        preSize = 0;
        currentSize = 0;
        nextSize = 0;
        getChapterSize();
        int offset = readStatus.offset;
        if (!needSavePage) {
            readStatus.currentPage = 1;
        }

        getCurrentPage(readStatus.currentPage);
        readStatus.offset = offset;
        final int position = readStatus.currentPage - 1;
        if (readStatus.currentPage > 1) {
            post(new Runnable() {

                @Override
                public void run() {
                    page_list.setSelection(position);
                    readStatus.currentPage = position + 1;
                }
            });

        } else if (readStatus.currentPage == 1) {
            post(new Runnable() {

                @Override
                public void run() {
                    page_list.setSelection(0);
                    readStatus.currentPage = 1;
                }
            });
        }
        if (readStatus.sequence == -1) {
            dataFactory.next();
        }
    }

    @Override
    public void getPreChapter() {
        boolean canRemove = false;
        ArrayList<ArrayList<NovelLineBean>> temp = nextChaperContent;
        if (preChaperConent != null) {
            nextChaperContent = currentChaperConent;
            currentChaperConent = preChaperConent;

            nextChapter = curChapter;
            curChapter = preChapter;

            canRemove = true;
        }
        preChapter = dataFactory.getCurrentChapter();
        preChapter.chapterNameList = readStatus.chapterNameList;
        preChaperConent = readStatus.mLineList;
        getChapterSize();
        chapterContent.addAll(0, readStatus.mLineList);
        if (temp != null && canRemove) {
//			chapterContent.removeAll(temp);
            chapterContent.clear();
            chapterContent.addAll(preChaperConent);
            chapterContent.addAll(currentChaperConent);
            chapterContent.addAll(nextChaperContent);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        int position = preChaperConent.size();
        // position = position;
        if (isNeedToNextPage) {
            page_list.setSelection(position - 1);
        } else {
            page_list.setSelection(position);
        }
        loadingData = false;

        if (readStatus.offset == 0) {
            readStatus.offset = 1;
        }
    }

    @Override
    public void getNextChapter() {
        ArrayList<ArrayList<NovelLineBean>> temp = preChaperConent;
        boolean canRemove = false;
        if (nextChaperContent != null) {
            preChaperConent = currentChaperConent;
            currentChaperConent = nextChaperContent;

            preChapter = curChapter;
            curChapter = nextChapter;

            canRemove = true;
        }
        nextChapter = dataFactory.getCurrentChapter();
        if (nextChapter == null) {
            return;
        }
        nextChapter.chapterNameList = readStatus.chapterNameList;
        nextChaperContent = readStatus.mLineList;
        int position = nextChaperContent.size();
        getChapterSize();
//		if (readStatus.sequence == 0) {
//			preSize = 1;
//		}
        chapterContent.addAll(readStatus.mLineList);
        if (temp != null && canRemove) {
//			chapterContent.removeAll(temp);
            chapterContent.clear();
            chapterContent.addAll(preChaperConent);
            chapterContent.addAll(currentChaperConent);
            chapterContent.addAll(readStatus.mLineList);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        if (isNeedToNextPage) {
            position = chapterContent.size() - position;
        } else {
            position = chapterContent.size() - position - 1;
        }

        page_list.setSelection(position);
        loadingData = false;

        if (readStatus.offset == 0) {//修正手动书签
            readStatus.offset = 1;
        }
    }


    @Override
    public void freshTime(CharSequence time) {
        novel_time.setText(time);
    }

    @Override
    public void freshBattery(float percent) {
//        novel_content_battery_view.setBattery(percent);
    }

    @Override
    public void drawNextPage() {

    }

    @Override
    public void drawCurrentPage() {

    }

    @Override
    public void setTextColor(int color) {
//        drawTextHelper.setTextColor(color);
    }

    @Override
    public void changeBatteryBg(int res) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBackground() {
//        drawTextHelper.resetBackBitmap();
        drawBackground();
        drawHeadFootText();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setPageBackColor(int color) {

    }

    @Override
    public void refreshCurrentPage() {

    }

    @Override
    public void tryTurnPrePage() {

    }

    @Override
    public void onAnimationFinish() {

    }

    @Override
    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public void clear() {

        if (manager != null) {
            manager.clearBitmap();
        }

        if (drawTextHelper != null) {
//            drawTextHelper.clear();
        }
        if (adapter != null) {
            adapter = null;
        }

        setBackgroundResource(0);
    }

    @Override
    public boolean isAutoReadMode() {
        return false;
    }

    @Override
    public void startAutoRead() {

    }

    @Override
    public void exitAutoRead() {

    }

    @Override
    public void exitAutoReadNoCancel() {

    }

    @Override
    public void tryResumeAutoRead() {

    }

    @Override
    public void resumeAutoRead() {

    }

    @Override
    public void pauseAutoRead() {

    }

    @Override
    public void setisAutoMenuShowing(boolean isShowing) {

    }

    private void drawBackground() {
        if (ReadConfig.INSTANCE.getMODE() == 51) {// 牛皮纸
            setBackgroundResource(R.drawable.read_page_bg_default);
        } else {
            // 通过新的画布，将矩形画新的bitmap上去
            int color_int = R.color.reading_backdrop_first;
            if (ReadConfig.INSTANCE.getMODE() == 52) {// day
                color_int = R.color.reading_backdrop_second;
            } else if (ReadConfig.INSTANCE.getMODE() == 53) {// eye
                color_int = R.color.reading_backdrop_third;
            } else if (ReadConfig.INSTANCE.getMODE() == 54) {// powersave
                color_int = R.color.reading_backdrop_fourth;
            } else if (ReadConfig.INSTANCE.getMODE() == 55) {// color -4
                color_int = R.color.reading_backdrop_fifth;
            } else if (ReadConfig.INSTANCE.getMODE() == 56) {// color -5
                color_int = R.color.reading_backdrop_sixth;
            } else if (ReadConfig.INSTANCE.getMODE() == 61) {//night3
                color_int = R.color.reading_backdrop_night;
            }
            setBackgroundColor(getResources().getColor(color_int));
        }
    }

    private void drawHeadFootText() {
        int color_int = R.color.reading_operation_text_color_first;
        if (ReadConfig.INSTANCE.getMODE() == 51) {// night1
            color_int = R.color.reading_operation_text_color_first;
        } else if (ReadConfig.INSTANCE.getMODE() == 52) {// day
            color_int = R.color.reading_operation_text_color_second;
        } else if (ReadConfig.INSTANCE.getMODE() == 53) {// eye
            color_int = R.color.reading_operation_text_color_third;
        } else if (ReadConfig.INSTANCE.getMODE() == 54) {// powersave
            color_int = R.color.reading_operation_text_color_fourth;
        } else if (ReadConfig.INSTANCE.getMODE() == 55) {// color -4
            color_int = R.color.reading_operation_text_color_fifth;
        } else if (ReadConfig.INSTANCE.getMODE() == 56) {// color -5
            color_int = R.color.reading_operation_text_color_sixth;
        } else if (ReadConfig.INSTANCE.getMODE() == 61) {// night2
            color_int = R.color.reading_operation_text_color_night;
        }

        novel_time.setTextColor(getResources().getColor(color_int));
        mOriginTv.setTextColor(getResources().getColor(color_int));
        mTransCodingTv.setTextColor(getResources().getColor(color_int));
        novel_page.setTextColor(getResources().getColor(color_int));
        novel_chapter.setTextColor(getResources().getColor(color_int));
        novel_title.setTextColor(getResources().getColor(color_int));
    }

    @Override
    public boolean setKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {

            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                int position = 0;
                if (android.os.Build.VERSION.SDK_INT < 11) {
                    position = lastVisible - 1;
                } else {
                    position = lastVisible - 2;
                }

                page_list.setSelection(position);
                if (!loadingData && lastVisible == 1 && readStatus.currentPage == 1) {
                    loadingData = true;
                    isNeedToNextPage = true;
                    boolean result = dataFactory.previous();
                    if (!result) {
                        handler.sendEmptyMessageDelayed(0, 1000);
                    }
                }
            }
            return true;
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {

            } else if (event.getAction() == KeyEvent.ACTION_UP) {
//				AppLog.e("onScroll", "lastVisible0:"+lastVisible);
//				if (lastVisible + 2== totalItemCount) {
//					lastVisible++;
//				}
                int position = 0;
                if (android.os.Build.VERSION.SDK_INT < 11) {
                    position = lastVisible + 1;
                } else {
                    position = lastVisible;
                }
                page_list.setSelection(position);
                if (!loadingData && (totalItemCount == lastVisible + 1 ||
                        totalItemCount == lastVisible + 2) && readStatus.currentPage == readStatus.pageCount) {
                    loadingData = true;
                    isNeedToNextPage = true;
                    boolean result = dataFactory.next();
                    if (!result) {
                        handler.sendEmptyMessageDelayed(0, 1000);
                    } else {
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void setOnOperationClickListener(OnOperationClickListener onOperationClickListener) {
        mOnOperationClickListener = onOperationClickListener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.origin_tv:
                if (mOnOperationClickListener != null) {
                    mOnOperationClickListener.onOriginClick();
                }
                break;
            case R.id.trans_coding_tv:
                if (mOnOperationClickListener != null) {
                    mOnOperationClickListener.onTransCodingClick();
                }
                break;
        }
    }

    static class MHandler extends Handler {

        private WeakReference<ScrollPageView> reference;

        public MHandler(ScrollPageView r) {
            reference = new WeakReference<ScrollPageView>(r);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ScrollPageView pageView = reference.get();
            if (pageView == null) {
                return;
            }
            switch (msg.what) {
                case 0:
                    pageView.changeLoadState();
                    break;

                default:
                    break;
            }
        }

    }

    class ScrollPageAdapter extends BaseAdapter {

        LayoutInflater inflater;

        public ScrollPageAdapter() {
            inflater = LayoutInflater.from(getContext());
        }

        @Override
        public int getCount() {
            return chapterContent.size();
        }

        @Override
        public Object getItem(int position) {
            return chapterContent.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (chapterContent != null && chapterContent.size() > 0 && chapterContent.get(position).get(0).getLineContent().startsWith(NovelHelper.empty_page_ad)) {
                return 1;
            }
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHodler hodler = null;
            int type = getItemViewType(position);
            if (convertView == null) {
                switch (type) {
                    case 0:
                        convertView = inflater.inflate(R.layout.page_item, parent, false);
                        hodler = new ViewHodler();
                        hodler.page = (Page) convertView.findViewById(R.id.page_item);
                        Bitmap mCurPageBitmap = manager.getBitmap4444();
                        Canvas mCurrentCanvas = new Canvas(mCurPageBitmap);
                        hodler.page.setTag(R.id.tag_bitmap, mCurPageBitmap);
                        hodler.page.setTag(R.id.tag_canvas, mCurrentCanvas);
                        convertView.setTag(R.id.Tag_BOOK, hodler);
                        break;
                }
            } else {
                switch (type) {
                    case 0:
                        hodler = (ViewHodler) convertView.getTag(R.id.Tag_BOOK);
                        break;
                }
            }
            getCurrentSequence(position + 1);
//            switch (type) {
//                case 0:
//                    Bitmap mCurPageBitmap = (Bitmap) hodler.page.getTag(R.id.tag_bitmap);
//                    Canvas mCurrentCanvas = (Canvas) hodler.page.getTag(R.id.tag_canvas);
//                    float pageHeight = drawTextHelper.drawText(mCurrentCanvas, chapterContent.get(position), chapterNameList);
//                    if (pageHeight != 0.0f) {
//                        hodler.page.getLayoutParams().height = (int) pageHeight;
//                    } else {
//                        hodler.page.getLayoutParams().height = readStatus.screenHeight;
//                    }
//                    hodler.page.drawPage(mCurPageBitmap);
//                    break;
//            }
            return convertView;
        }

        class ViewHodler {
            Page page;
        }

    }
}
