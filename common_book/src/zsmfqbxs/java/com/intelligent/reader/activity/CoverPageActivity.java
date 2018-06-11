package com.intelligent.reader.activity;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ding.basic.bean.Book;
import com.ding.basic.repository.RequestRepositoryFactory;
import com.dingyue.contract.router.RouterConfig;
import com.dingyue.contract.router.RouterUtil;
import com.dingyue.contract.util.CommonUtil;
import com.intelligent.reader.R;
import com.intelligent.reader.cover.BookCoverViewModel;
import com.intelligent.reader.view.ExpandableTextView2;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.download.CacheManager;
import net.lzbook.kit.book.download.DownloadState;
import net.lzbook.kit.book.view.LoadingPage;
import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.book.view.RecommendItemView;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.data.bean.CoverPage;
import net.lzbook.kit.data.db.help.ChapterDaoHelper;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.BaseBookHelper;
import net.lzbook.kit.utils.BookCoverUtil;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.StatServiceUtils;
import net.lzbook.kit.utils.Tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import iyouqu.theme.BaseCacheableActivity;

@Route(path = RouterConfig.COVER_PAGE_ACTIVITY)
public class CoverPageActivity extends BaseCacheableActivity implements OnClickListener,
        BookCoverUtil.OnDownloadState, BookCoverUtil.OnDownLoadService,
        BookCoverViewModel.BookCoverViewCallback {

    final static int DOWNLOADING = 0x10;
    final static int NO_DOWNLOAD = DOWNLOADING + 1;
    final static int DOWNLOAD_FINISH = NO_DOWNLOAD + 1;
    final static int DATA_OK = DOWNLOAD_FINISH + 1;
    final static int DATA_ERROR = DATA_OK + 1;
    public final static int MESSAGE_SHOW_TIEBA = DATA_ERROR + 1;
    final static int COLLECT_DATA_OK = MESSAGE_SHOW_TIEBA + 1;
    final static int COLLECT_DATA_ERROR = COLLECT_DATA_OK + 1;
    final static int UN_COLLECT_DATA_OK = COLLECT_DATA_ERROR + 1;
    final static int UN_COLLECT_DATA_ERROR = UN_COLLECT_DATA_OK + 1;
    final static int HAD_COLLECT_DATA_OK = UN_COLLECT_DATA_ERROR + 1;
    final static int HAD_COLLECT_DATA_ERROR = HAD_COLLECT_DATA_OK + 1;
    final static int GET_CATEGORY_OK = HAD_COLLECT_DATA_ERROR + 1;
    final static int GET_CATEGORY_ERROR = GET_CATEGORY_OK + 1;
    //private RelativeLayout book_cover_reading_view;
    private ImageView book_cover_back;
    private ScrollView book_cover_content;
    private ImageView book_cover_image;
    private TextView book_cover_title;
    private TextView book_cover_author;
    private TextView book_cover_category;
    private TextView book_cover_category2;
    private TextView book_cover_status;
    private TextView book_cover_update_time;
    private LinearLayout book_cover_source_view;
    private TextView book_cover_source_form;
    private ViewGroup book_cover_chapter_view;
    private TextView book_cover_last_chapter;
    //private RelativeLayout book_cover_bookshelf_view;
    private TextView book_cover_bookshelf;
    private TextView book_cover_reading;
    //private RelativeLayout book_cover_download_view;
    private TextView book_cover_download;
    private int mBackground = 0;
    private int mTextColor = 0;
    private ExpandableTextView2 book_cover_description;
    private RelativeLayout book_cover_catalog_view;
    private RelativeLayout book_cover_catalog_view_nobg;
    //private BookCover bookCover;
    private BookCoverUtil bookCoverUtil;
    private LoadingPage loadingPage;

    private List<CoverPage.SourcesBean> bookSourceList = new ArrayList<>();


    private MyDialog coverSourceDialog;
    private MyDialog readingSourceDialog;

    private MyDialog confirm_change_source_dialog;


    private Book bookVo;
    private String bookId = null;
    private String bookSourceId = null;
    private String bookChapterId = "";
    private BookCoverViewModel mBookCoverViewModel;

    public static void launcher(Context context, final String host, final String book_id,
            final String book_source_id, final String name, final String author, final
    String parameter, final String extra_parameter) {

        Intent intent = new Intent();
        intent.setClass(context, CoverPageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("book_id", book_id);
        bundle.putString("book_source_id", book_source_id);


        try {
            intent.putExtras(bundle);
            context.startActivity(intent);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatServiceUtils.statAppBtnClick(this, StatServiceUtils.cover_into);
        setContentView(R.layout.act_book_cover);

        initView();
        initData(getIntent());
        initListener();

    }

    protected void initView() {
        book_cover_back = (ImageView) findViewById(R.id.book_cover_back);
        book_cover_content = (ScrollView) findViewById(R.id.book_cover_content);
        book_cover_image = (ImageView) findViewById(R.id.book_cover_image);
        book_cover_title = (TextView) findViewById(R.id.book_cover_title);
        book_cover_author = (TextView) findViewById(R.id.book_cover_author);
        book_cover_category = (TextView) findViewById(R.id.book_cover_category);
        book_cover_category2 = (TextView) findViewById(R.id.book_cover_category2);
        book_cover_status = (TextView) findViewById(R.id.book_cover_status);
        book_cover_update_time = (TextView) findViewById(R.id.book_cover_update_time);

        book_cover_source_view = (LinearLayout) findViewById(R.id.book_cover_source_view);
        book_cover_source_form = (TextView) findViewById(R.id.book_cover_source_form);

        book_cover_chapter_view = (ViewGroup) findViewById(R.id.book_cover_chapter_view);
        book_cover_last_chapter = (TextView) findViewById(R.id.book_cover_last_chapter);

        //book_cover_bookshelf_view = (RelativeLayout) findViewById(R.id.book_cover_bookshelf_view);
        book_cover_bookshelf = (TextView) findViewById(R.id.book_cover_bookshelf);

        // book_cover_reading_view = (RelativeLayout) findViewById(R.id.book_cover_reading_view);
        book_cover_reading = (TextView) findViewById(R.id.book_cover_reading);
        //book_cover_download_view = (RelativeLayout) findViewById(R.id.book_cover_download_view);
        book_cover_download = (TextView) findViewById(R.id.book_cover_download);

        book_cover_description = (ExpandableTextView2) findViewById(R.id.book_cover_description);
        book_cover_catalog_view = (RelativeLayout) findViewById(R.id.book_cover_catalog_view);

        book_cover_catalog_view_nobg = (RelativeLayout) findViewById(
                R.id.book_cover_catalog_view_nobg);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        initData(intent);
    }

    protected void initListener() {
        if (book_cover_back != null) {
            book_cover_back.setOnClickListener(this);
        }

        if (book_cover_author != null) {
            book_cover_author.setOnClickListener(this);
        }

        if (book_cover_source_view != null) {
            book_cover_source_view.setOnClickListener(this);
        }

        if (book_cover_chapter_view != null) {
            book_cover_chapter_view.setOnClickListener(this);
        }

        if (book_cover_last_chapter != null) {
            book_cover_last_chapter.setOnClickListener(this);
//            setDebounceClickListener(book_cover_last_chapter, this, 1000);
        }

        if (book_cover_bookshelf != null) {
            book_cover_bookshelf.setOnClickListener(this);
        }

        if (book_cover_reading != null) {
            book_cover_reading.setOnClickListener(this);
        }

        if (book_cover_download != null) {
            book_cover_download.setOnClickListener(this);
        }

        if (book_cover_catalog_view != null) {
            book_cover_catalog_view.setOnClickListener(this);
        }

        if (book_cover_catalog_view_nobg != null) {
            book_cover_catalog_view_nobg.setOnClickListener(this);
        }

    }


    protected void initData(Intent intent) {

        if (mBookCoverViewModel == null) {
            mBookCoverViewModel = new BookCoverViewModel();
            mBookCoverViewModel.setBookCoverViewCallback(this);
        }
        if (intent != null) {
            if (intent.hasExtra("book_id")) {
                bookId = intent.getStringExtra("book_id");
            }

            if (intent.hasExtra("book_source_id")) {
                bookSourceId = intent.getStringExtra("book_source_id");
            }
            if (intent.hasExtra("book_chapter_id")) {
                bookChapterId = intent.getStringExtra("book_chapter_id");
            }
        }


//        if (intent != null) {
//
//            if (intent.hasExtra(Constants.REQUEST_ITEM)) {
//                requestItem = (RequestItem) intent.getSerializableExtra(Constants.REQUEST_ITEM);
//            }
//        }
//
//        //      AppLog.e(TAG, "RequestItem: " + requestItem.toString());
//
//        if (bookDaoHelper == null) {
//            bookDaoHelper = BookDaoHelper.getInstance();
//        }

        if (bookCoverUtil == null) {
            bookCoverUtil = new BookCoverUtil(CoverPageActivity.this, this);
        }
        bookCoverUtil.registReceiver();
        bookCoverUtil.setOnDownloadState(this);
        bookCoverUtil.setOnDownLoadService(this);
        if (!TextUtils.isEmpty(bookId) && (!TextUtils.isEmpty(bookSourceId) || !TextUtils.isEmpty(
                bookChapterId))) {
            loadCoverInfo();
        }

    }

    protected void loadCoverInfo() {

        if (loadingPage != null) {
            loadingPage.onSuccess();
        }

        loadingPage = new LoadingPage(this, (ViewGroup) findViewById(R.id.book_cover_main),
                LoadingPage.setting_result);


        mBookCoverViewModel.requestBookDetail(bookId, bookSourceId, bookChapterId);

        if (loadingPage != null) {
            loadingPage.setReloadAction(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    mBookCoverViewModel.requestBookDetail(bookId, bookSourceId, bookChapterId);
                    return null;
                }
            });
        }
    }


    private void setRemoveBtn() {
        mBackground = R.drawable.cover_bottom_btn_remove_bg;
        mTextColor = R.color.cover_bottom_btn_remove_text_color;
        book_cover_bookshelf.setTextColor(getResources().getColor(mTextColor));
        if (book_cover_category2.getVisibility() != View.VISIBLE) {
            book_cover_bookshelf.setBackgroundResource(mBackground);
        }
    }

    private void setAddShelfBtn() {
        mBackground = R.drawable.cover_bottom_btn_add_bg;
        mTextColor = R.color.cover_bottom_btn_add_text_color;
        book_cover_bookshelf.setTextColor(getResources().getColor(mTextColor));
        if (book_cover_category2.getVisibility() != View.VISIBLE) {
            book_cover_bookshelf.setBackgroundResource(mBackground);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bookId == null || TextUtils.isEmpty(bookId)) {
            return;
        }


        changeDownloadButtonStatus();
        Book book = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).checkBookSubscribe(bookId);

        if (book != null) {

            if (book_cover_bookshelf != null) {
                book_cover_bookshelf.setText(R.string.book_cover_remove_bookshelf);
                setRemoveBtn();
            }
        } else {
            if (book_cover_bookshelf != null) {
                setAddShelfBtn();
            }
        }

    }

    private void changeDownloadButtonStatus() {
        if (book_cover_download == null) {
            return;
        }

        if (bookVo != null && book_cover_download != null) {
            Boolean isSub = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                    BaseBookApplication.getGlobalContext()).checkBookSubscribe(bookId) != null;

            if(isSub){
                DownloadState status = CacheManager.INSTANCE.getBookStatus(bookVo);
                if (status == DownloadState.FINISH) {
                    book_cover_download.setText(R.string.download_status_complete);
                } else if (status == DownloadState
                        .WAITTING || status == DownloadState.DOWNLOADING) {
                    book_cover_download.setText(R.string.download_status_underway);
                } else {
                    book_cover_download.setText(R.string.download_status_total);
                }
            }else{
                book_cover_download.setText(R.string.download_status_total);
            }

        }
    }


    @Override
    public void onTaskStatusChange() {
        super.onTaskStatusChange();
        changeDownloadButtonStatus();
    }


    private void upDateUI() {
        book_cover_content.smoothScrollTo(0, 0);
        if (bookVo != null) {

            if (book_cover_image != null && !TextUtils.isEmpty(bookVo.getImg_url()) && !bookVo
                    .getImg_url().equals(
                            ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL)) {
                Glide.with(getApplicationContext()).load(bookVo.getImg_url()).placeholder(
                        net.lzbook.kit.R.drawable.icon_book_cover_default).error(
                        (net.lzbook.kit.R.drawable.icon_book_cover_default)).diskCacheStrategy(
                        DiskCacheStrategy.ALL).into(book_cover_image);
            } else {
                Glide.with(getApplicationContext()).load(
                        net.lzbook.kit.R.drawable.icon_book_cover_default).into(book_cover_image);
            }

            if (book_cover_title != null && !TextUtils.isEmpty(bookVo.getName())) {
                book_cover_title.setText(bookVo.getName());
            }

            if (book_cover_author != null && !TextUtils.isEmpty(bookVo.getAuthor())) {
                book_cover_author.setText(bookVo.getAuthor());
            }

            if (book_cover_category != null && !TextUtils.isEmpty(bookVo.getLabel())) {
                book_cover_category.setText(bookVo.getLabel());
            }

            if (book_cover_category2 != null && !TextUtils.isEmpty(bookVo.getLabel())) {
                book_cover_category2.setText(bookVo.getLabel());
                if (!mThemeHelper.isNight()) {
                    book_cover_category2.setBackgroundResource(R.drawable.book_cover_label_bg);
                    GradientDrawable background =
                            (GradientDrawable) book_cover_category2.getBackground();
                    background.setColor(getResources().getColor(R.color.color_white_ffffff));
                    book_cover_category2.setTextColor(AppUtils.getRandomColor());
                } else {
                    book_cover_category2.setTextColor(AppUtils.getRandomColor());
                }
            }

            if (!"FINISH".equals(bookVo.getStatus())) {
                if (book_cover_category2.getVisibility() != View.VISIBLE) {
                    book_cover_status.setText("—" + getString(R.string.book_cover_state_writing));
                } else {
                    book_cover_status.setText(getString(R.string.book_cover_state_writing));
                    if (!mThemeHelper.isNight()) {
                        book_cover_status.setBackgroundResource(R.drawable.book_cover_label_bg);
                        GradientDrawable background =
                                (GradientDrawable) book_cover_status.getBackground();
                        background.setColor(getResources().getColor(R.color.color_white_ffffff));
                        book_cover_status.setTextColor(
                                getResources().getColor(R.color.color_red_ff2d2d));
                    } else {
                        book_cover_status.setTextColor(
                                getResources().getColor(R.color.color_red_ff5656));
                    }
                }
            } else {
                if (book_cover_category2.getVisibility() != View.VISIBLE) {
                    book_cover_status.setText("—" + getString(R.string.book_cover_state_written));
                } else {
                    book_cover_status.setText(getString(R.string.book_cover_state_written));
                    if (!mThemeHelper.isNight()) {
                        book_cover_status.setBackgroundResource(R.drawable.book_cover_label_bg);
                        GradientDrawable background =
                                (GradientDrawable) book_cover_status.getBackground();
                        background.setColor(getResources().getColor(R.color.color_white_ffffff));
                        book_cover_status.setTextColor(
                                getResources().getColor(R.color.color_brown_e9cfae));
                    } else {
                        book_cover_status.setTextColor(
                                getResources().getColor(R.color.color_brown_e2bd8d));
                    }

                }
            }

            if (book_cover_update_time != null && bookVo.getLast_chapter() != null) {
                book_cover_update_time.setText(Tools.compareTime(AppUtils.formatter, bookVo
                        .getLast_chapter().getUpdate_time()));
            }

            if (book_cover_last_chapter != null && bookVo != null
                    && bookVo.getLast_chapter() != null && !TextUtils.isEmpty
                    (bookVo.getLast_chapter().getName())) {
                book_cover_last_chapter.setText(bookVo.getLast_chapter().getName());
            }

            if (bookVo.getDesc() != null && !TextUtils.isEmpty(bookVo.getDesc())) {
                book_cover_description.setText(bookVo.getDesc());
            } else {
                book_cover_description.setText(getResources().getString(R.string
                        .book_cover_no_description));
            }

            if ("qg".equals(bookVo.getBook_type())) {
                book_cover_source_form.setText("青果阅读");
            } else {
                book_cover_source_form.setText(bookVo.getHost());
            }

            book_cover_source_form.setCompoundDrawables(null, null, null, null);

        } else {
            CommonUtil.showToastMessage(R.string.book_cover_no_resource);
            if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
                finish();
            }
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();

        if (view instanceof RecommendItemView) {
            RecommendItemView item = (RecommendItemView) view;
            intent.putExtra("word", item.getTitle());
            intent.putExtra("search_type", "0");
            intent.putExtra("filter_type", "0");
            intent.putExtra("filter_word", "ALL");
            intent.putExtra("sort_type", "0");
            intent.setClass(this, SearchBookActivity.class);
            startActivity(intent);
            return;
        }

        switch (view.getId()) {
            case R.id.book_cover_back:
                Map<String, String> data = new HashMap<>();
                data.put("type", "1");
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE,
                        StartLogClickUtil.BACK, data);
                finish();
//                SearchBookActivity.isSatyHistory = true;
                break;

//            case R.id.book_cover_source_view:
//                //书籍详情页换源点击
//                StatServiceUtils.statAppBtnClick(this, StatServiceUtils
// .b_details_click_ch_source);
//                showCoverSourceDialog();
//
//                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE,
// StartLogClickUtil.SOURCECHANGE);
//                break;

            case R.id.book_cover_bookshelf:
                if (bookVo == null || bookCoverUtil == null) {
                    return;
                }
                final Book book = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                        BaseBookApplication.getGlobalContext()).checkBookSubscribe(
                        bookVo.getBook_id());

                if (book == null) {
                    Long succeed = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                            BaseBookApplication.getGlobalContext()).insertBook(bookVo);
                    if (succeed > 0 && book_cover_bookshelf != null) {
                        //添加书架打点
                        StatServiceUtils.statAppBtnClick(this,
                                StatServiceUtils.b_details_click_book_add);
                        book_cover_bookshelf.setText(R.string.book_cover_remove_bookshelf);
                        setRemoveBtn();
                        CommonUtil.showToastMessage(R.string.succeed_add);
                        Map<String, String> data1 = new HashMap<>();
                        data1.put("type", "1");
                        data1.put("bookid", bookVo.getBook_id());
                        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE,
                                StartLogClickUtil.SHELFEDIT, data1);

                    } else {
                        CommonUtil.showToastMessage("加入书架失败！");
                    }

                } else {

                    if (book_cover_bookshelf != null) {
                        book_cover_bookshelf.setText(R.string.book_cover_add_bookshelf);
                        setAddShelfBtn();
                    }
                    //移除书架的打点
                    StatServiceUtils.statAppBtnClick(this,
                            StatServiceUtils.b_details_click_book_remove);
                    CommonUtil.showToastMessage(getString(R.string.succeed_remove));
                    Map<String, String> data2 = new HashMap<>();
                    data2.put("type", "2");
                    data2.put("bookid", bookVo.getBook_id());
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE,
                            StartLogClickUtil.SHELFEDIT, data2);
                    changeDownloadButtonStatus();

                    book_cover_bookshelf.setClickable(false);
                    final MyDialog cleanDialog = new MyDialog(this, R.layout.dialog_download_clean);
                    cleanDialog.setCanceledOnTouchOutside(false);
                    cleanDialog.setCancelable(false);
                    ((TextView) cleanDialog.findViewById(R.id.dialog_msg)).setText(
                            R.string.tip_cleaning_cache);
                    cleanDialog.show();

                    Observable.create(new ObservableOnSubscribe<Boolean>() {
                        @Override
                        public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                            CacheManager.INSTANCE.remove(bookVo.getBook_id());

                            RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                                    BaseBookApplication.getGlobalContext()).deleteBook(
                                    bookVo.getBook_id());

                            BaseBookHelper.removeChapterCacheFile(bookVo);


                            e.onNext(true);
                            e.onComplete();
                        }
                    }).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<Boolean>() {
                                @Override
                                public void accept(Boolean aBoolean) throws Exception {
                                    cleanDialog.dismiss();
                                    book_cover_bookshelf.setClickable(true);

                                    changeDownloadButtonStatus();
                                }
                            });
                }
                break;

            case R.id.book_cover_reading:
                //转码阅读点击的统计
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_trans_read);
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE,
                        StartLogClickUtil.TRANSCODEREAD);
                showReadingSourceDialog();
                break;

            case R.id.book_cover_download:

                if (bookVo == null) {
                    return;
                }

                DownloadState downloadState = CacheManager.INSTANCE.getBookStatus(bookVo);
                if (downloadState != DownloadState.FINISH && downloadState != DownloadState.WAITTING
                        && downloadState != DownloadState.DOWNLOADING) {
                    Toast.makeText(this, "马上开始为你缓存...", Toast.LENGTH_SHORT).show();
                }

                Book book1 = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                        BaseBookApplication.getGlobalContext()).checkBookSubscribe(
                        bookVo.getBook_id());

                if (book1 != null) {
                    BaseBookHelper.startDownBookTask(this, bookVo, 0);
                } else {
                    long result = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                            BaseBookApplication.getGlobalContext()).insertBook(bookVo);

                    if (result > 0 && book_cover_bookshelf != null) {
                        //添加书架打点
                        StatServiceUtils.statAppBtnClick(this,
                                StatServiceUtils.b_details_click_book_add);
                        book_cover_bookshelf.setText(R.string.book_cover_remove_bookshelf);
                        setRemoveBtn();
                        CommonUtil.showToastMessage(R.string.succeed_add);
                        BaseBookHelper.startDownBookTask(this, bookVo, 0);
                    }
                }
                changeDownloadButtonStatus();
                break;
            case R.id.book_cover_catalog_view_nobg:
            case R.id.book_cover_catalog_view:
                //书籍详情页查看目录点击
                StatServiceUtils.statAppBtnClick(this,
                        StatServiceUtils.b_details_click_to_catalogue);
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE,
                        StartLogClickUtil.CATALOG);
                goToCataloguesAct(intent, 0, false);
                break;
            case R.id.book_cover_chapter_view:
            case R.id.book_cover_last_chapter:
                if (bookVo == null) {
                    return;
                }
                goToCataloguesAct(intent, bookVo.getLast_chapter().getSerial_number() - 1, true);
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE,
                        StartLogClickUtil.LATESTCHAPTER);
                break;
            default:
                break;
        }
    }

    /**
     * 点击查看目录或者最新章节后的跳转操作
     *
     * @param locationSequence 将要定位到的章节序号
     */
    private void goToCataloguesAct(Intent intent, int locationSequence, boolean isLastChapter) {
        if (bookVo != null && bookCoverUtil != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("cover", bookVo);
            bundle.putInt("sequence", locationSequence);
            bundle.putBoolean("fromCover", true);
            bundle.putBoolean("is_last_chapter", isLastChapter);
            intent.setClass(CoverPageActivity.this, CataloguesActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }


    private void showReadingSourceDialog() {
        if (readingSourceDialog == null) {
            readingSourceDialog = new MyDialog(CoverPageActivity.this, R.layout
                    .dialog_read_source, Gravity.CENTER);
            readingSourceDialog.setCanceledOnTouchOutside(true);
            TextView change_source_head = (TextView) readingSourceDialog.findViewById(
                    R.id.dialog_top_title);
            change_source_head.setText("转码");
            TextView change_source_original_web = (TextView) readingSourceDialog.findViewById(R
                    .id.change_source_original_web);
            change_source_original_web.setText(R.string.cancel);
            TextView change_source_continue = (TextView) readingSourceDialog.findViewById(R.id
                    .change_source_continue);

            change_source_original_web.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    readingSourceDialog.dismiss();
                }
            });
            change_source_continue.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Book book = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                            BaseBookApplication.getGlobalContext()).checkBookSubscribe(bookId);

                    if (book != null) {
                        if ("qg".equals(book.getBook_type())) {
                            readingCustomaryBook();
                        } else {
                            if (bookVo.getBook_source_id().equals(book.getBook_source_id())) {

                                //直接进入阅读
                                readingCustomaryBook();
                                readingSourceDialog.dismiss();
                            } else {
                                //弹出切源提示
                                readingSourceDialog.dismiss();
//                                showChangeSourceNoticeDialog(currentSource);

                                intoReadingActivity();
                            }
                        }
                    } else {
                        continueReading();
                    }
                    if (readingSourceDialog.isShowing()) {
                        readingSourceDialog.dismiss();
                    }
                }
            });
        }

        if (!readingSourceDialog.isShowing()) {
            try {
                readingSourceDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //阅读书架上的书籍
    private void readingCustomaryBook() {

        if (bookVo == null) {
            return;
        }
        Bundle bundle = new Bundle();
        int flags = Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK;
        Book book = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).loadBook(bookId);


        if (book != null) {
            if (bookVo != null && bookVo.getLast_chapter() != null
                    && book.getLast_chapter() != null) {
                book.getLast_chapter().setUpdate_time(bookVo.getLast_chapter().getUpdate_time());
            }

            if (book != null && book.getSequence() != -2) {
                bundle.putInt("sequence", book.getSequence());
                bundle.putInt("offset", book.getOffset());
            } else {
                bundle.putInt("sequence", -1);
                bundle.putInt("offset", 0);
            }
            bundle.putSerializable("book", book);
        } else {
            bundle.putSerializable("book", bookVo);
        }
        RouterUtil.INSTANCE.navigation(this, RouterConfig.READER_ACTIVITY, bundle, flags);
    }

    private void continueReading() {
        if (bookVo == null) {
            return;
        }
        Bundle bundle = new Bundle();
        int flags = Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK;
        Book book = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).loadBook(bookId);


        if (book != null) {
            if (bookVo != null && bookVo.getLast_chapter() != null
                    && book.getLast_chapter() != null) {
                book.getLast_chapter().setUpdate_time(bookVo.getLast_chapter().getUpdate_time());
            }

            if (book != null && book.getSequence() != -2) {
                bundle.putInt("sequence", book.getSequence());
                bundle.putInt("offset", book.getOffset());
            } else {
                bundle.putInt("sequence", -1);
                bundle.putInt("offset", 0);
            }
            bundle.putSerializable("book", book);
        } else {
            bundle.putSerializable("book", bookVo);
        }
        RouterUtil.INSTANCE.navigation(this, RouterConfig.READER_ACTIVITY, bundle, flags);

    }

    private void intoReadingActivity() {

        if (bookVo == null) {
            return;
        }
        //进入阅读页逻辑
        Bundle bundle = new Bundle();
        int flags = Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK;


        Book book = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext()).checkBookSubscribe(bookId);

        if (book != null) {
            if (bookVo != null && bookVo.getLast_chapter() != null
                    && book.getLast_chapter() != null) {
                book.getLast_chapter().setUpdate_time(bookVo.getLast_chapter().getUpdate_time());
            }

            if (book != null && book.getSequence() != -2) {
                bundle.putInt("sequence", book.getSequence());
                bundle.putInt("offset", book.getOffset());
            } else {
                bundle.putInt("sequence", -1);
                bundle.putInt("offset", 0);
            }
            changeBookInformation();
            bundle.putSerializable("book", book);
        } else {
            bundle.putSerializable("book", bookVo);
        }
        RouterUtil.INSTANCE.navigation(this, RouterConfig.READER_ACTIVITY, bundle, flags);
    }


    private void showChangeSourceNoticeDialog(final CoverPage.SourcesBean source) {
        if (!isFinishing()) {
            confirm_change_source_dialog = new MyDialog(this, R.layout.publish_hint_dialog);
            confirm_change_source_dialog.setCanceledOnTouchOutside(true);
            Button dialog_cancel = (Button) confirm_change_source_dialog.findViewById(R.id
                    .publish_stay);
            dialog_cancel.setText(R.string.book_cover_continue_read_cache);
            Button dialog_confirm = (Button) confirm_change_source_dialog.findViewById(R.id
                    .publish_leave);
            dialog_confirm.setText(R.string.book_cover_confirm_change_source);
            TextView dialog_information = (TextView) confirm_change_source_dialog.findViewById(R
                    .id.publish_content);
            dialog_information.setText(R.string.book_cover_change_source_prompt);
            dialog_cancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissDialog();
                    readingCustomaryBook();
                }
            });
            dialog_confirm.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismissDialog();
                    intoReadingActivity();
                }
            });

            confirm_change_source_dialog.setOnCancelListener(new DialogInterface.OnCancelListener
                    () {
                @Override
                public void onCancel(DialogInterface dialog) {
                    confirm_change_source_dialog.dismiss();
                }
            });
            if (!confirm_change_source_dialog.isShowing()) {
                try {
                    confirm_change_source_dialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void dismissDialog() {
        if (confirm_change_source_dialog != null && confirm_change_source_dialog.isShowing()) {
            confirm_change_source_dialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {

        try {
            setContentView(R.layout.empty);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        if (bookCoverUtil != null) {
            bookCoverUtil.unRegisterReceiver();
            bookCoverUtil = null;
        }


        super.onDestroy();
    }

    @Override
    public void changeState() {
        changeDownloadButtonStatus();
    }

    @Override
    public void downLoadService() {
        changeDownloadButtonStatus();
    }

    private void changeBookInformation() {
        if (bookVo != null) {
            Boolean result = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                    BaseBookApplication.getGlobalContext()).updateBook(bookVo);

            if (result) {

                if (bookVo.getBook_id() != null && !TextUtils.isEmpty(bookVo.getBook_id())) {
                    ChapterDaoHelper chapterDaoHelp =
                            ChapterDaoHelper.Companion.loadChapterDataProviderHelper(
                                    BaseBookApplication.getGlobalContext(), bookVo.getBook_id());
                    chapterDaoHelp.deleteAllChapters();
                }
            }
        }
    }


    @Override
    public void requestCoverDetailFail(String message) {
        if (loadingPage != null) {
            loadingPage.onError();
        }
    }

    @Override
    public void requestCoverDetailSuccess(Book book) {
        this.bookVo = book;
        if (bookVo != null && bookCoverUtil != null) {
            bookCoverUtil.saveHistory(bookVo);
        }
        upDateUI();
        if (loadingPage != null) {
            loadingPage.onSuccess();
        }
        changeDownloadButtonStatus();
    }
}
