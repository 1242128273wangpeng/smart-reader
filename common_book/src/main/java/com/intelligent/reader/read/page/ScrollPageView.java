package com.intelligent.reader.read.page;

import com.dingyueads.sdk.Bean.Novel;
import com.intelligent.reader.R;
import com.intelligent.reader.activity.ReadingActivity;
import com.intelligent.reader.read.animation.BitmapManager;
import com.intelligent.reader.read.help.CallBack;
import com.intelligent.reader.read.help.DrawTextHelper;
import com.intelligent.reader.read.help.IReadDataFactory;
import com.intelligent.reader.read.help.NovelHelper;
import com.intelligent.reader.util.DisplayUtils;

import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.bean.ReadStatus;
import net.lzbook.kit.statistic.alilog.Log;
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ScrollPageView extends LinearLayout implements PageInterface {
    private static final int NO_START = 0;
    private static final int PRE_START = 1;
    private static final int NEXt_START = 2;
    private final MHandler handler = new MHandler(this);
    public Chapter tempChapter;
    boolean nextResult = false;
    ArrayList<String> chapterNameList = new ArrayList<String>();
    private FListView page_list;
    private ArrayList<ArrayList<String>> chapterContent;
    private ArrayList<ArrayList<String>> preChaperConent;
    private ArrayList<ArrayList<String>> currentChaperConent;
    private ArrayList<ArrayList<String>> nextChaperContent;
    private Chapter preChapter;
    private Chapter curChapter;
    private Chapter nextChapter;
    private int preSize;
    private int currentSize;
    private int nextSize;
    private NovelHelper novelHelper;
    private ReadStatus readStatus;
    private DrawTextHelper drawTextHelper;
    private IReadDataFactory dataFactory;
    private ScrollPageAdapter adapter;
    private boolean loadingData = false;
    private int width, height;
    private FrameLayout.LayoutParams lp;
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
    private RelativeLayout novel_bottom;
    private BitmapManager manager;
    private long startTouchTime;
    private int startTouchX;
    private int startTouchY;
    private boolean isNeedToNextPage = false;
    private BatteryView novel_content_battery_view;
    private RelativeLayout scroll_page;

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

        manager = new BitmapManager(readStatus.screenWidth, readStatus.screenHeight);
        // if (Constants.isSlideUp) {
        // resetCanvas(w, h);
        // }
        if (callBack != null && (Math.abs(oldh - h) > AppUtils.dip2px(mContext, 26))) {
            callBack.onResize();
//            if (android.os.Build.VERSION.SDK_INT < 11 && Constants.isFullWindowRead) {
//                height = readStatus.screenHeight - AppUtils.dip2px(mContext, 20);
//            } else {
//                height = readStatus.screenHeight - AppUtils.dip2px(mContext, 40);
//            }
//
//            lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, height);
            setBackground();
            if (chapterContent != null) {
                getChapter(true);
            }
        }

    }

    @Override
    public void init(Activity activity, ReadStatus readStatus, NovelHelper novelHelper) {
        this.mActivity = activity;
        View view = LayoutInflater.from(mContext).inflate(R.layout.scroll_page, this);
        scroll_page = (RelativeLayout) view.findViewById(R.id.scroll_page);
        page_list = (FListView) view.findViewById(R.id.page_list);
        novel_time = (TextView) view.findViewById(R.id.novel_time);
        novel_chapter = (TextView) view.findViewById(R.id.novel_chapter);
        novel_title = (TextView) view.findViewById(R.id.novel_title);
        novel_page = (TextView) view.findViewById(R.id.novel_page);
        novel_bottom = (RelativeLayout) view.findViewById(R.id.novel_bottom);
        novel_content_battery_view = (BatteryView) view.findViewById(R.id.novel_content_battery_view);

        this.novelHelper = novelHelper;
        this.readStatus = readStatus;

        width = readStatus.screenWidth;
        height = readStatus.screenHeight - DisplayUtils.dp2px(getResources(), 30) * 2;

//        dataFactory.setScreenSize(readStatus.screenWidth, height);

        chapterContent = new ArrayList<>();

        drawTextHelper = new DrawTextHelper(getResources(), this, mActivity);

        adapter = new ScrollPageAdapter();
        page_list.setAdapter(adapter);
        lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, height);

        page_list.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                ScrollPageView.this.totalItemCount = totalItemCount;
                ScrollPageView.this.firstVisibleItem = firstVisibleItem;
                //
                int lastVisible = page_list.getLastVisiblePosition();
//				Log.e("onScroll", "getLastVisiblePosition:" + lastVisible);
                if (ScrollPageView.this.lastVisible != lastVisible) {

                    ScrollPageView.this.lastVisible = lastVisible;
//					Log.e("onScroll", "getLastVisiblePosition:" + lastVisible);
//					Log.e("onScroll", "totalItemCount:" + totalItemCount);
//					AppLog.e("onScroll", "firstVisibleItem:"+firstVisibleItem);
                    ScrollPageView.this.readStatus.currentPage = getCurrentPage(lastVisible + 1);
//					AppLog.e("onScroll", "readStatus.currentPage:" + ScrollPageView.this.readStatus.currentPage);
//					AppLog.e("onScroll", "readStatus.sequence:" + ScrollPageView.this.readStatus.sequence);
//					if (ScrollPageView.this.readStatus.currentPage == 1 && adapter != null) {
//						adapter.notifyDataSetChanged();
//					}
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
//			AppLog.e("onScroll", "sequence:"+readStatus.sequence);
                if (!loadingData && lastY - event.getY() > 20 && totalItemCount == lastVisible + 2
                        && readStatus.currentPage == readStatus.pageCount) {
                    loadingData = true;
//				AppLog.e("onScroll", "next:");
                    isNeedToNextPage = false;
                    boolean result = dataFactory.next();
                    if (!result) {
                        handler.sendEmptyMessageDelayed(0, 1000);
                    }

                } else if (!loadingData && lastY - event.getY() < -20 &&
                        firstVisibleItem == 0 && readStatus.currentPage <= 1) {
                    loadingData = true;
                    if (currentChaperConent != null && currentChaperConent.size() > 0) {
                        ArrayList<String> arrayList = currentChaperConent.get(0);
                        if (arrayList != null && arrayList.size() > 0 &&
                                arrayList.get(0).indexOf("txtzsydsq_homepage") > -1) {
                            loadingData = false;
                            if (page_list.getChildCount() > 1 && page_list.getChildAt(1) != null &&
                                    Math.abs(page_list.getChildAt(1).getTop() - height) < 10) {
                                Toast.makeText(mContext, R.string.is_first_chapter, Toast.LENGTH_SHORT).show();
                            }
                            return false;
                        } else {

                        }
                    }
                    if (preChaperConent != null && preChaperConent.size() > 0) {
                        ArrayList<String> arrayList = preChaperConent.get(0);
                        if (arrayList != null && arrayList.size() > 0 &&
                                arrayList.get(0).indexOf("txtzsydsq_homepage") > -1) {
                            loadingData = false;
                            if (page_list.getChildCount() > 1 && page_list.getChildAt(1) != null &&
                                    Math.abs(page_list.getChildAt(1).getTop() - height) < 10) {
                                Toast.makeText(mContext, R.string.is_first_chapter, Toast.LENGTH_SHORT).show();
                            }
                            return false;
                        } else {

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
//		Log.e("getCurrentPage", "position:"+position);
//		Log.e("getCurrentPage", "preSize:"+preSize);
//		Log.e("getCurrentPage", "currentSize:"+currentSize);
//		Log.e("getCurrentPage", "nextSize:"+nextSize);
//		ArrayList<String> chapterNameList = null;
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

    private int getCurrentPage(int position) {
        String chapter_name = "";
//		Log.e("getCurrentPage", "position:"+position);
//		Log.e("getCurrentPage", "preSize:"+preSize);
//		Log.e("getCurrentPage", "currentSize:"+currentSize);
//		Log.e("getCurrentPage", "nextSize:"+nextSize);
//		ArrayList<String> chapterNameList = null;
        if (preSize == 0) {
            if (position <= currentSize) {
                readStatus.pageCount = currentSize;
                if (curChapter != null) {
                    tempChapter = curChapter;
                    readStatus.sequence = curChapter.sequence;
                    chapter_name = curChapter.chapter_name;
//					chapterNameList = curChapter.chapterNameList;
                }

            } else if (position <= currentSize + nextSize) {
                position = position - currentSize;
                readStatus.pageCount = nextSize;
                if (nextChapter != null) {
                    tempChapter = nextChapter;
                    readStatus.sequence = nextChapter.sequence;
                    chapter_name = nextChapter.chapter_name;
//					chapterNameList = nextChapter.chapterNameList;
                }
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

            }
        }

        if (readStatus.sequence == -1) {
            novel_content_battery_view.setVisibility(GONE);
            novel_time.setVisibility(GONE);
            novel_chapter.setVisibility(GONE);
            novel_page.setVisibility(GONE);
        } else {
            novel_content_battery_view.setVisibility(VISIBLE);
            novel_time.setVisibility(VISIBLE);
            novel_chapter.setVisibility(VISIBLE);
            novel_page.setVisibility(VISIBLE);
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

    @Override
    public void setReadFactory(IReadDataFactory factory) {
        this.dataFactory = factory;

    }

    @Override
    public void setFirstPage(boolean firstPage) {

    }

    @Override
    public void getChapter(boolean needSavePage) {
        if (readStatus.mLineList == null) {
            return;
        }
        // totalChapter.put(readStatus.sequence, readStatus.mLineList);
        chapterContent.clear();
        preChapter = null;
        nextChapter = null;
        currentChaperConent = null;
        nextChaperContent = null;
        preChaperConent = null;

        curChapter = dataFactory.currentChapter;
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
        Log.e("getNextChapter", "getPreChapter()");
        boolean canRemove = false;
        ArrayList<ArrayList<String>> temp = nextChaperContent;
        if (preChaperConent != null) {
            nextChaperContent = currentChaperConent;
            currentChaperConent = preChaperConent;

            nextChapter = curChapter;
            curChapter = preChapter;

            canRemove = true;
        }
        preChapter = dataFactory.currentChapter;
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
        Log.e("getNextChapter", "getNextChapter()");
        ArrayList<ArrayList<String>> temp = preChaperConent;
        boolean canRemove = false;
        if (nextChaperContent != null) {
            preChaperConent = currentChaperConent;
            currentChaperConent = nextChaperContent;

            preChapter = curChapter;
            curChapter = nextChapter;

            canRemove = true;
        }
        nextChapter = dataFactory.currentChapter;
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
        novel_content_battery_view.setBattery(percent);
    }

    @Override
    public void drawNextPage() {

    }

    @Override
    public void drawCurrentPage() {

    }

    @Override
    public void setTextColor(int color) {
        drawTextHelper.setTextColor(color);
    }

    @Override
    public void changeBatteryBg(int res) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBackground() {
        drawTextHelper.resetBackBitmap();
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
            drawTextHelper.clear();
        }

        if (this.mActivity != null) {
            this.mActivity = null;
        }

        if (this.mContext != null) {
            this.mContext = null;
        }
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
        if (Constants.MODE == 51) {// 牛皮纸
//            scroll_page.setBackgroundResource(R.drawable.read_page_bg_default);
//            novel_title.setBackgroundResource(R.drawable.read_page_bg_default_patch);
//            novel_bottom.setBackgroundResource(R.drawable.read_page_bg_default_patch);
//            page_list.setFootViewBackground(R.drawable.read_page_bg_default_patch);
            setBackgroundResource(R.drawable.read_page_bg_default);
        } else {
            // 通过新的画布，将矩形画新的bitmap上去
            int color_int = R.color.reading_backdrop_first;
            if (Constants.MODE == 52) {// day
                color_int = R.color.reading_backdrop_second;
            } else if (Constants.MODE == 53) {// eye
                color_int = R.color.reading_backdrop_third;
            } else if (Constants.MODE == 54) {// powersave
                color_int = R.color.reading_backdrop_fourth;
            } else if (Constants.MODE == 55) {// color -4
                color_int = R.color.reading_backdrop_fifth;
            } else if (Constants.MODE == 56) {// color -5
                color_int = R.color.reading_backdrop_sixth;
            } else if (Constants.MODE == 61) {//night3
                color_int = R.color.reading_backdrop_night;
            }

//            novel_title.setBackgroundColor(getResources().getColor(color_int));
//            novel_bottom.setBackgroundColor(getResources().getColor(color_int));
//            page_list.setFootViewBackgroundColor(getResources().getColor(color_int));

            setBackgroundColor(getResources().getColor(color_int));
        }
    }

    private void drawHeadFootText() {
        int color_int = R.color.reading_text_color_first;
        if (Constants.MODE == 51) {// night1
            color_int = R.color.reading_text_color_first;
        } else if (Constants.MODE == 52) {// day
            color_int = R.color.reading_text_color_second;
        } else if (Constants.MODE == 53) {// eye
            color_int = R.color.reading_text_color_third;
        } else if (Constants.MODE == 54) {// powersave
            color_int = R.color.reading_text_color_fourth;
        } else if (Constants.MODE == 55) {// color -4
            color_int = R.color.reading_text_color_fifth;
        } else if (Constants.MODE == 56) {// color -5
            color_int = R.color.reading_text_color_sixth;
        } else if (Constants.MODE == 61) {// night2
            color_int = R.color.reading_text_color_night;
        }

        novel_time.setTextColor(getResources().getColor(color_int));
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
    public void loadNatvieAd() {
        drawTextHelper.loadNatvieAd();
    }

    @Override
    public Novel getCurrentNovel() {
        if (dataFactory != null) {
            return dataFactory.transformation();
        }
        return null;
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
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHodler hodler = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.page_item, null);
                hodler = new ViewHodler();
                hodler.page = (Page) convertView.findViewById(R.id.page_item);
                convertView.setTag(hodler);
                Bitmap mCurPageBitmap = manager.getBitmap8888();
                Canvas mCurrentCanvas = new Canvas(mCurPageBitmap);

                hodler.page.setTag(R.id.tag_bitmap, mCurPageBitmap);
                hodler.page.setTag(R.id.tag_canvas, mCurrentCanvas);
//				Log.e("getView", "mCurrentCanvas");
            } else {
                hodler = (ViewHodler) convertView.getTag();
            }
//			Log.e("getView", "position:" + position);
            // readStatus.currentPage = getCurrentPage(lastVisible + 1);
            getCurrentSequence(position + 1);
//			Log.e("getView", "position:" + position);
//			if (chapterNameList.size() > 0) {
//				Log.e("getView", "chapterNameList:" + chapterNameList.get(0));
//			}

            Bitmap mCurPageBitmap = (Bitmap) hodler.page.getTag(R.id.tag_bitmap);
            Canvas mCurrentCanvas = (Canvas) hodler.page.getTag(R.id.tag_canvas);
            float pageHeight = drawTextHelper.drawText(mCurrentCanvas, chapterContent.get(position), chapterNameList);
            android.util.Log.e("ScrollView", "pageHeight: " + pageHeight);
            if (position != 0) {
                lp.height = (int) pageHeight;
            }
            hodler.page.setLayoutParams(lp);
            hodler.page.drawPage(mCurPageBitmap);

            return convertView;
        }

        class ViewHodler {
            Page page;
        }

    }
}
