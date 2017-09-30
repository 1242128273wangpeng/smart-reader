package com.intelligent.reader.activity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.intelligent.reader.R;
import com.intelligent.reader.adapter.CoverSourceAdapter;
import com.intelligent.reader.read.help.BookHelper;
import com.intelligent.reader.receiver.DownBookClickReceiver;
import com.quduquxie.bean.BookMode;
import com.quduquxie.network.DataService;
import com.quduquxie.network.RequestManager;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.component.service.DownloadService;
import net.lzbook.kit.book.download.DownloadState;
import net.lzbook.kit.book.view.ExpandableTextView;
import net.lzbook.kit.book.view.LoadingPage;
import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.book.view.RecommendItemView;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.CoverPage;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.data.db.BookChapterDao;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.request.RequestExecutor;
import net.lzbook.kit.request.RequestFactory;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.BaseBookHelper;
import net.lzbook.kit.utils.BookCoverUtil;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.OpenUDID;
import net.lzbook.kit.utils.StatServiceUtils;
import net.lzbook.kit.utils.Tools;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class CoverPageActivity extends BaseCacheableActivity implements OnClickListener, BookCoverUtil
        .OnDownloadState, BookCoverUtil.OnDownLoadService, DownloadService.OnDownloadListener {

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
    UIHandler uiHandler = new UIHandler(this);
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
    private ExpandableTextView book_cover_description;
    private RelativeLayout book_cover_catalog_view;
    private RelativeLayout book_cover_catalog_view_nobg;
    //private BookCover bookCover;
    private BookDaoHelper bookDaoHelper;
    private BookCoverUtil bookCoverUtil;
    private LoadingPage loadingPage;
    private RequestFactory requestFactory;

    private List<CoverPage.SourcesBean> bookSourceList = new ArrayList<>();

    private RequestItem requestItem;

    private MyDialog coverSourceDialog;
    private MyDialog readingSourceDialog;

    private CoverPage.SourcesBean currentSource;
    private MyDialog confirm_change_source_dialog;
    private DownloadService downloadService;


    private CoverPage.BookVoBean bookVo;
    private ServiceConnection sc = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadService = ((DownloadService.MyBinder) service).getService();
            BaseBookApplication.setDownloadService(downloadService);
            downloadService.setUiContext(getApplicationContext());
            downloadService.setOnDownloadListener(CoverPageActivity.this);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatServiceUtils.statAppBtnClick(this, StatServiceUtils.cover_into);
        setContentView(R.layout.act_book_cover);
        requestFactory = new RequestFactory();
        downloadService = BaseBookApplication.getDownloadService();
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

        book_cover_description = (ExpandableTextView) findViewById(R.id.book_cover_description);
        book_cover_catalog_view = (RelativeLayout) findViewById(R.id.book_cover_catalog_view);

        book_cover_catalog_view_nobg = (RelativeLayout) findViewById(R.id.book_cover_catalog_view_nobg);

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

        if (downloadService == null) {
            reStartDownloadService(BaseBookApplication.getGlobalContext().getApplicationContext());
            downloadService = BaseBookApplication.getDownloadService();
        } else {
            downloadService.setUiContext(getApplicationContext());
            downloadService.setOnDownloadListener(this);
        }

    }

    private void reStartDownloadService(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, DownloadService.class);
        context.startService(intent);
        context.bindService(intent, sc, BIND_AUTO_CREATE);
    }

    protected void initData(Intent intent) {

        if (intent != null) {

            if (intent.hasExtra(Constants.REQUEST_ITEM)) {
                requestItem = (RequestItem) intent.getSerializableExtra(Constants.REQUEST_ITEM);
            }
        }

        //      AppLog.e(TAG, "RequestItem: " + requestItem.toString());

        if (bookDaoHelper == null) {
            bookDaoHelper = BookDaoHelper.getInstance();
        }

        if (BaseBookApplication.getDownloadService() == null) {
            BookHelper.reStartDownloadService();
        }
        if (bookCoverUtil == null) {
            bookCoverUtil = new BookCoverUtil(CoverPageActivity.this, this);
        }
        bookCoverUtil.registReceiver();
        bookCoverUtil.setOnDownloadState(this);
        bookCoverUtil.setOnDownLoadService(this);

        loadCoverInfo();
    }

    protected void loadCoverInfo() {

        if (loadingPage != null) {
            loadingPage.onSuccess();
        }

        loadingPage = new LoadingPage(this, (ViewGroup) findViewById(R.id.book_cover_main),
                LoadingPage.setting_result);


        if (requestItem != null) {
            AppLog.e("loadCoverInfo", "requestItem.host-->" + requestItem.host);
            if (Constants.QG_SOURCE.equals(requestItem.host)) {//青果
                requestItem.channel_code = 1;
                RequestManager.init(getApplicationContext());
                String udid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext());
                DataService.getBookInfo(this, requestItem.book_id, uiHandler, RequestExecutor.REQUEST_COVER_QG_SUCCESS, RequestExecutor
                        .REQUEST_COVER_QG_ERROR, udid);
            } else {//自有
                if (Constants.SG_SOURCE.equals(requestItem.host)) {
                    uiHandler.sendEmptyMessage(RequestExecutor.REQUEST_COVER_ERROR);
                } else {
                    requestFactory.requestExecutor(requestItem).requestBookCover(uiHandler, requestItem);
                }
                requestItem.channel_code = 2;
            }
        }

        if (loadingPage != null) {
            loadingPage.setReloadAction(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    if (requestItem != null) {
                        if (Constants.QG_SOURCE.equals(requestItem.host)) {//青果
                            RequestManager.init(getApplicationContext());
                            String udid = OpenUDID.getOpenUDIDInContext(BaseBookApplication.getGlobalContext());
                            DataService.getBookInfo(CoverPageActivity.this, requestItem.book_id, uiHandler, RequestExecutor
                                    .REQUEST_COVER_QG_SUCCESS, RequestExecutor.REQUEST_COVER_QG_ERROR, udid);
                        } else {//自有
                            if (Constants.SG_SOURCE.equals(requestItem.host)) {
                                uiHandler.sendEmptyMessage(RequestExecutor.REQUEST_COVER_ERROR);
                            } else {
                                requestFactory.requestExecutor(requestItem).requestBookCover(uiHandler, requestItem);
                            }
                        }
                        /*requestFactory.requestExecutor(requestItem).requestBookCover(uiHandler,
                                requestItem);*/
                    }
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
        if (bookDaoHelper == null || requestItem == null)
            return;

        if (bookDaoHelper.isBookSubed(requestItem.book_id)) {
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
        Book book = null;
        if (bookCoverUtil != null) {
            book = bookCoverUtil.getCoverBook(bookDaoHelper, bookVo);
        }
        if (book != null && book_cover_download != null) {
            if (BookHelper.getDownloadState(CoverPageActivity.this, book) == DownloadState.FINISH) {
                book_cover_download.setText(R.string.download_status_complete);
            } else if (BookHelper.getDownloadState(CoverPageActivity.this, book) == DownloadState
                    .LOCKED) {
                book_cover_download.setText(R.string.download_status_complete);
            } else if (BookHelper.getDownloadState(CoverPageActivity.this, book) == DownloadState
                    .NOSTART) {
                book_cover_download.setText(R.string.download_status_total);
            } else {
                book_cover_download.setText(R.string.download_status_underway);
            }
        }
    }

    @Override
    public void notificationCallBack(Notification preNTF, String book_id) {
        PendingIntent pending = null;
        Intent intent = null;
        if (!book_id.equals(-1 + "")) {
            intent = new Intent(this, DownBookClickReceiver.class);
            intent.setAction(DownBookClickReceiver.action);
            intent.putExtra("book_id", book_id);
            pending = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            intent = new Intent(this, DownloadManagerActivity.class);
            pending = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        preNTF.contentIntent = pending;
    }

    private void handCategoryError() {
        if (loadingPage != null) {
            loadingPage.onSuccess();
        }
        Toast.makeText(CoverPageActivity.this, "请求失败", Toast.LENGTH_SHORT).show();
    }

    private void handCategoryOk() {
        if (loadingPage != null) {
            loadingPage.onSuccess();
        }
    }

    private void handleOK(Object objects, boolean isQG) {

        //bookCover = ((CoverPage) objects).bookCover;
        if (isQG) {//如果是青果的数据，就先进行一次类型转换
            bookVo = parseToBookVoBean((BookMode) objects);
            book_cover_source_form.setText("青果阅读");
        } else {
            bookVo = ((CoverPage) objects).bookVo;

            if (bookVo != null) {


                if (requestItem != null && !TextUtils.isEmpty(requestItem.book_id)) {
                    bookVo.book_id = requestItem.book_id;
                }

                if (requestItem != null && !TextUtils.isEmpty(requestItem.book_source_id)) {
                    bookVo.book_source_id = requestItem.book_source_id;
                }

                if (requestItem != null && !TextUtils.isEmpty(requestItem.host)) {
                    bookVo.host = requestItem.host;
                }
                if (requestItem != null) {
                    bookVo.dex = requestItem.dex;
                }

            /*if (bookVo != null && !TextUtils.isEmpty(bookVo.book_id)) {
                requestItem.book_id = bookVo.book_id;
            }

            if (bookVo != null && !TextUtils.isEmpty(bookVo.book_source_id)) {
                requestItem.book_source_id = bookVo.book_source_id;
            }

            if (bookVo != null && !TextUtils.isEmpty(bookVo.host)) {
                requestItem.host = bookVo.host;
            }
            if (bookVo != null) {
                requestItem.dex = bookVo.dex;
            }*/
                List<CoverPage.SourcesBean> sources = ((CoverPage) objects).sources;
                if (bookSourceList == null) {
                    bookSourceList = new ArrayList<>();
                }
                bookSourceList.clear();

                if (sources != null) {
                    bookSourceList.addAll(sources);
                    for (int i = 0; i < bookSourceList.size(); i++) {
                        CoverPage.SourcesBean source = bookSourceList.get(i);
                        if (requestItem.book_source_id.equals(source.book_source_id)) {
                            currentSource = source;
                        }
                    }

                    if (book_cover_source_form != null) {
                        if (currentSource != null && !TextUtils.isEmpty(currentSource.host)) {
                            book_cover_source_form.setText(currentSource.host);
                        } else {
                            if (bookSourceList != null && bookSourceList.size() > 0) {
                                book_cover_source_form.setText(bookSourceList.get(0).host);
                                currentSource = bookSourceList.get(0);
                            }
                        }
                    }

                }
            }
        }

        if (bookVo != null && bookCoverUtil != null) {
            bookCoverUtil.saveHistory(bookVo);
        }

        upDateUI();

        if (loadingPage != null) {
            loadingPage.onSuccess();
        }
        changeDownloadButtonStatus();
//        initGuide();
    }

    private CoverPage.BookVoBean parseToBookVoBean(BookMode bookMode) {
        //防止青果后端书籍出错导致的封面页崩溃问题
        if (bookMode.model == null) {
            handleError();
            return null;
        }
        CoverPage.BookVoBean bookVoBean = new CoverPage.BookVoBean();
        bookVoBean.book_source_id = Constants.QG_SOURCE;
        bookVoBean.host = Constants.QG_SOURCE;
        bookVoBean.book_id = bookMode.model.id_book;
        bookVoBean.name = bookMode.model.name;
        bookVoBean.author = bookMode.model.penname;
        if (!TextUtils.isEmpty(bookMode.model.attribute_book)) {
            bookVoBean.status = bookMode.model.attribute_book.equals("serialize") ? 1 : 2;
        }
        bookVoBean.last_chapter_name = bookMode.model.id_last_chapter_name;
        bookVoBean.serial_number = bookMode.model.id_last_chapter_serial_number;//总章数
        bookVoBean.img_url = bookMode.model.image_book;
        bookVoBean.labels = bookMode.model.category;
        bookVoBean.desc = bookMode.model.description;
        bookVoBean.update_time = bookMode.model.id_last_chapter_create_time;

        return bookVoBean;
    }

    private void handleError() {
        if (loadingPage != null) {
            loadingPage.onError();
        }
        Toast.makeText(CoverPageActivity.this, "请求失败", Toast.LENGTH_SHORT).show();
    }

    private void upDateUI() {
        book_cover_content.smoothScrollTo(0, 0);
        if (bookVo != null) {

            if (book_cover_image != null && !TextUtils.isEmpty(bookVo.img_url) && !bookVo
                    .img_url.equals(ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL)) {
                Glide.with(getApplicationContext()).load(bookVo.img_url).placeholder(net.lzbook.kit.R.drawable.icon_book_cover_default).error((net.lzbook.kit.R.drawable.icon_book_cover_default)).diskCacheStrategy(DiskCacheStrategy.ALL).into(book_cover_image);
            } else {
                Glide.with(getApplicationContext()).load(net.lzbook.kit.R.drawable.icon_book_cover_default).into(book_cover_image);
            }

            if (book_cover_title != null && !TextUtils.isEmpty(bookVo.name)) {
                book_cover_title.setText(bookVo.name);
            }

            if (book_cover_author != null && !TextUtils.isEmpty(bookVo.author)) {
                book_cover_author.setText(bookVo.author);
            }

            if (book_cover_category != null && !TextUtils.isEmpty(bookVo.labels)) {
                book_cover_category.setText(bookVo.labels);
            }

            if (book_cover_category2 != null && !TextUtils.isEmpty(bookVo.labels)) {
                book_cover_category2.setText(bookVo.labels);
                if (!mThemeHelper.isNight()) {
                    book_cover_category2.setBackgroundResource(R.drawable.book_cover_label_bg);
                    GradientDrawable background = (GradientDrawable) book_cover_category2.getBackground();
                    background.setColor(getResources().getColor(R.color.color_white_ffffff));
                    book_cover_category2.setTextColor(AppUtils.getRandomColor());
                } else {
                    book_cover_category2.setTextColor(AppUtils.getRandomColor());
                }
            }

            if (1 == bookVo.status) {
                if (book_cover_category2.getVisibility() != View.VISIBLE) {
                    book_cover_status.setText("—" + getString(R.string.book_cover_state_writing));
                } else {
                    book_cover_status.setText(getString(R.string.book_cover_state_writing));
                    if (!mThemeHelper.isNight()) {
                        book_cover_status.setBackgroundResource(R.drawable.book_cover_label_bg);
                        GradientDrawable background = (GradientDrawable) book_cover_status.getBackground();
                        background.setColor(getResources().getColor(R.color.color_white_ffffff));
                        book_cover_status.setTextColor(getResources().getColor(R.color.color_red_ff2d2d));
                    } else {
                        book_cover_status.setTextColor(getResources().getColor(R.color.color_red_ff5656));
                    }
                }
            } else {
                if (book_cover_category2.getVisibility() != View.VISIBLE) {
                    book_cover_status.setText("—" + getString(R.string.book_cover_state_written));
                } else {
                    book_cover_status.setText(getString(R.string.book_cover_state_written));
                    if (!mThemeHelper.isNight()) {
                        book_cover_status.setBackgroundResource(R.drawable.book_cover_label_bg);
                        GradientDrawable background = (GradientDrawable) book_cover_status.getBackground();
                        background.setColor(getResources().getColor(R.color.color_white_ffffff));
                        book_cover_status.setTextColor(getResources().getColor(R.color.color_brown_e9cfae));
                    } else {
                        book_cover_status.setTextColor(getResources().getColor(R.color.color_brown_e2bd8d));
                    }

                }
            }

            if (book_cover_update_time != null) {
                book_cover_update_time.setText(Tools.compareTime(AppUtils.formatter, bookVo
                        .update_time));
            }

            if (book_cover_last_chapter != null && bookVo != null && !TextUtils.isEmpty
                    (bookVo.last_chapter_name)) {
                book_cover_last_chapter.setText(bookVo.last_chapter_name);
            }

            if (bookVo.desc != null && !TextUtils.isEmpty(bookVo.desc)) {
                book_cover_description.setText(bookVo.desc);
            } else {
                book_cover_description.setText(getResources().getString(R.string
                        .book_cover_no_description));
            }
        } else {
            showToastShort(R.string.book_cover_no_resource);
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
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.BACK, data);
                finish();
                break;

            case R.id.book_cover_source_view:
                //书籍详情页换源点击
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_ch_source);
                showCoverSourceDialog();

                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.SOURCECHANGE);
                break;

            case R.id.book_cover_bookshelf:
                if (bookDaoHelper == null || bookCoverUtil == null) {
                    return;
                }
                if (!bookDaoHelper.isBookSubed(requestItem.book_id)) {
                    Book insertBook = bookCoverUtil.getCoverBook(bookDaoHelper, bookVo);
                    if (currentSource != null) {
                        insertBook.last_updatetime_native = currentSource.update_time;
                    }
                    if (bookVo.host.equals(Constants.QG_SOURCE)) {
                        insertBook.last_updatetime_native = bookVo.update_time;
                    }
                    insertBook.last_updateSucessTime = System.currentTimeMillis();
                    boolean succeed = bookDaoHelper.insertBook(insertBook);
                    if (succeed && book_cover_bookshelf != null) {
                        //添加书架打点
                        StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_book_add);
                        book_cover_bookshelf.setText(R.string.book_cover_remove_bookshelf);
                        setRemoveBtn();
                        showToastShort(R.string.succeed_add);
                        Map<String, String> data1 = new HashMap<>();
                        data1.put("type", "1");
                        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.SHELFEDIT, data1);
                    }

                } else {
                    if (bookDaoHelper != null) {
                        bookDaoHelper.deleteBook(requestItem.host, requestItem.book_id);
                    }
                    BookHelper.delDownIndex(this, requestItem.book_id);
                    if (book_cover_bookshelf != null) {
                        book_cover_bookshelf.setText(R.string.book_cover_add_bookshelf);
                        setAddShelfBtn();
                    }
                    //移除书架的打点
                    StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_book_remove);
                    showToastShort(getString(R.string.succeed_remove));
                    Map<String, String> data2 = new HashMap<>();
                    data2.put("type", "2");
                    StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.SHELFEDIT, data2);
                    DownloadService downloadService = BaseBookApplication.getDownloadService();
                    if (downloadService != null) {
                        downloadService.cancelTask(requestItem.book_id);
                    }
                    changeDownloadButtonStatus();
                }
                break;

            case R.id.book_cover_reading:
                //转码阅读点击的统计
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_trans_read);
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.TRANSCODEREAD);
                if (bookSourceList != null && bookSourceList.size() == 0) {
                    if (Constants.QG_SOURCE.equals(requestItem.host)) {
                        showReadingSourceDialog();
                        requestItem.fromType = 3;// 打点统计 当前页面来源，所有可能来源的映射唯一字符串。书架(0)/目录页(1)/上一页翻页(2)/书籍封面(3)
                    } else {
                        Toast.makeText(getApplicationContext(), "当前书籍不能阅读，先去看看其他书吧", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showReadingSourceDialog();
                }
                break;

            case R.id.book_cover_download:
                Book book = bookCoverUtil.getCoverBook(bookDaoHelper, bookVo);
                DownloadState downloadState = BookHelper.getDownloadState(CoverPageActivity.this, book);
                if (downloadState != DownloadState.FINISH && downloadState != DownloadState.WAITTING && downloadState != DownloadState.DOWNLOADING) {
                    Toast.makeText(this, "马上开始为你缓存。。。", Toast.LENGTH_SHORT).show();
                }

                //全本缓存的点击统计
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_all_load);
                Map<String, String> data3 = new HashMap<>();
                data3.put("bookId", requestItem.book_id);
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.CASHEALL, data3);
                if (Constants.QG_SOURCE.equals(requestItem.host)) {
                    if (bookDaoHelper == null) {
                        bookDaoHelper = BookDaoHelper.getInstance();
                    }
                    if (bookDaoHelper != null && bookCoverUtil != null) {
                        if (!bookDaoHelper.isBookSubed(requestItem.book_id)) {
                            Book insertBook = bookCoverUtil.getCoverBook(bookDaoHelper, bookVo);

                            insertBook.last_updatetime_native = bookVo.update_time;

                            boolean succeed = bookDaoHelper.insertBook(insertBook);
                            if (succeed && book_cover_bookshelf != null) {
                                book_cover_bookshelf.setText(R.string.book_cover_remove_bookshelf);
                                setRemoveBtn();
                                showToastShort(getString(R.string.succeed_add));
                                bookCoverUtil.startDownLoad(bookDaoHelper, bookVo);
                            }
                        } else {
                            bookCoverUtil.startDownLoad(bookDaoHelper, bookVo);
                        }
                    }
                } else {
                    if (bookSourceList != null && bookSourceList.size() == 0) {
                        Toast.makeText(getApplicationContext(), "当前书籍不能缓存，先去看看其他书吧", Toast.LENGTH_SHORT).show();
                    } else {
                        if (bookDaoHelper == null) {
                            bookDaoHelper = BookDaoHelper.getInstance();
                        }
                        if (bookDaoHelper != null && bookCoverUtil != null) {
                            if (!bookDaoHelper.isBookSubed(requestItem.book_id)) {
                                Book insertBook = bookCoverUtil.getCoverBook(bookDaoHelper, bookVo);

                                if (currentSource != null) {
                                    insertBook.last_updatetime_native = currentSource.update_time;
                                }

                                boolean succeed = bookDaoHelper.insertBook(insertBook);
                                if (succeed && book_cover_bookshelf != null) {
                                    book_cover_bookshelf.setText(R.string.book_cover_remove_bookshelf);
                                    setRemoveBtn();
                                    showToastShort(getString(R.string.succeed_add));
                                    bookCoverUtil.startDownLoad(bookDaoHelper, bookVo);
                                }
                            } else {
                                bookCoverUtil.startDownLoad(bookDaoHelper, bookVo);
                            }
                        }
                    }
                }
                break;
            case R.id.book_cover_catalog_view_nobg:
            case R.id.book_cover_catalog_view:
                //书籍详情页查看目录点击
                StatServiceUtils.statAppBtnClick(this, StatServiceUtils.b_details_click_to_catalogue);
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.CATALOG);
                if (Constants.QG_SOURCE.equals(requestItem.host)) {
                    goToCataloguesAct(intent, 0, false);
                } else {
                    if (bookSourceList != null && bookSourceList.size() == 0) {
                        Toast.makeText(getApplicationContext(), "当前书籍不能阅读，先去看看其他书吧", Toast.LENGTH_SHORT).show();
                    } else {
                        goToCataloguesAct(intent, 0, false);
                    }
                }
                break;
            case R.id.book_cover_chapter_view:
            case R.id.book_cover_last_chapter:
                if (Constants.QG_SOURCE.equals(requestItem.host)) {
                    goToCataloguesAct(intent, bookVo.serial_number - 1, true);
                } else {
                    if (bookSourceList != null && bookSourceList.size() == 0) {
                        Toast.makeText(getApplicationContext(), "当前书籍不能阅读，先去看看其他书吧", Toast.LENGTH_SHORT).show();
                    } else {
                        goToCataloguesAct(intent, bookVo.serial_number - 1, true);
                    }
                }
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.LATESTCHAPTER);
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
            bundle.putSerializable("cover", bookCoverUtil.getCoverBook(bookDaoHelper,
                    bookVo));
            bundle.putInt("sequence", locationSequence);
            bundle.putBoolean("fromCover", true);
            bundle.putBoolean("is_last_chapter", isLastChapter);
            bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
            intent.setClass(CoverPageActivity.this, CataloguesActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    private void showCoverSourceDialog() {
        if (Constants.QG_SOURCE.equals(requestItem.host) || bookSourceList.size() == 1) {//青果
            Toast.makeText(this, "该小说暂无其他来源！", Toast.LENGTH_LONG).show();
            return;
        }
        if (coverSourceDialog == null) {
            coverSourceDialog = new MyDialog(CoverPageActivity.this, R.layout
                    .dialog_read_source, Gravity.CENTER);
            coverSourceDialog.setCanceledOnTouchOutside(true);
            ListView sourceView = (ListView) coverSourceDialog.findViewById(R.id
                    .change_source_list);
            TextView dialog_top_title = (TextView) coverSourceDialog.findViewById(R.id.dialog_top_title);
            dialog_top_title.setText(R.string.change_source);
            RelativeLayout change_source_statement = (RelativeLayout) coverSourceDialog
                    .findViewById(R.id.change_source_statement);
            change_source_statement.setVisibility(View.GONE);


            final CoverSourceAdapter bookSourceAdapter = new CoverSourceAdapter(this,
                    bookSourceList);

            sourceView.setAdapter(bookSourceAdapter);

            if (bookSourceList.size() > 4) {
                sourceView.getLayoutParams().height = getResources().getDimensionPixelOffset(R
                        .dimen.dimen_view_height_240);
            }


            sourceView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    CoverPage.SourcesBean source = (CoverPage.SourcesBean) bookSourceAdapter.getItem(position);
                    AppLog.e(TAG, "监听开始执行");
                    if (source != null) {
                        if (requestItem != null && !TextUtils.isEmpty(source.book_source_id)) {
                            requestItem.book_id = source.book_id;
                            requestItem.book_source_id = source.book_source_id;
                            requestItem.host = source.host;
                            requestItem.dex = source.dex;
                            Iterator<Map.Entry<String, String>> iterator = source.source.entrySet().iterator();
                            ArrayList<String> list = new ArrayList<>();
                            while (iterator.hasNext()) {
                                Map.Entry<String, String> entry = iterator.next();
                                String value = entry.getValue();
                                list.add(value);
                            }
                            if (list.size() > 0) {
                                requestItem.parameter = list.get(0);
                            }
                            if (list.size() > 1) {
                                requestItem.extra_parameter = list.get(1);
                            }
                        }

                        currentSource = source;

                        loadCoverInfo();

                        if (book_cover_source_form != null) {
                            book_cover_source_form.setText(source.host);
                        }

                        //                        changeBookInformation(source);

                        coverSourceDialog.dismiss();
                    }
                }
            });
        }

        TextView change_source_original_web = (TextView) coverSourceDialog.findViewById(R.id
                .change_source_original_web);
        change_source_original_web.setText(R.string.cancel);
        TextView change_source_continue = (TextView) coverSourceDialog.findViewById(R.id
                .change_source_continue);

        change_source_original_web.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                coverSourceDialog.dismiss();
            }
        });
        change_source_continue.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if (bookSourceList != null && bookSourceList.size() == 0) {
                    Toast.makeText(getApplicationContext(), "当前书籍不能阅读，先去看看其他书吧", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                if (bookDaoHelper == null || bookCoverUtil == null || bookVo == null) {
                    return;
                }
                Book book = bookCoverUtil.getCoverBook(bookDaoHelper, bookVo);

                if (bookDaoHelper.isBookSubed(requestItem.book_id) && book != null && book
                        .sequence != -2) {
                    bundle.putInt("sequence", book.sequence);
                    bundle.putInt("offset", book.offset);
                } else {
                    bundle.putInt("sequence", -1);
                }
                if (book != null) {
                    bundle.putSerializable("book", book);
                }
                if (requestItem != null) {
                    bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
                }
                if (book != null && requestItem != null) {
                    AppLog.e(TAG, "GotoReading: " + book.site + " : " + requestItem.host);
                }
                intent.setClass(CoverPageActivity.this, ReadingActivity.class);
                intent.putExtras(bundle);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                coverSourceDialog.dismiss();
            }
        });

        if (!coverSourceDialog.isShowing()) {
            try {
                coverSourceDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showReadingSourceDialog() {
        if (readingSourceDialog == null) {
            readingSourceDialog = new MyDialog(CoverPageActivity.this, R.layout
                    .dialog_read_source, Gravity.CENTER);
            readingSourceDialog.setCanceledOnTouchOutside(true);
            TextView change_source_head = (TextView) readingSourceDialog.findViewById(R.id.dialog_top_title);
            change_source_head.setText("转码");
            TextView change_source_disclaimer_jump = (TextView) readingSourceDialog.findViewById
                    (R.id.change_source_disclaimer_jump);
            TextView change_source_original_web = (TextView) readingSourceDialog.findViewById(R
                    .id.change_source_original_web);
            change_source_original_web.setText(R.string.cancel);
            TextView change_source_continue = (TextView) readingSourceDialog.findViewById(R.id
                    .change_source_continue);

            if (change_source_disclaimer_jump != null) {
                change_source_disclaimer_jump.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(CoverPageActivity.this, DisclaimerActivity
                                .class);
                        try {
                            intent.putExtra("isFromReadingPage", true);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        readingSourceDialog.dismiss();
                    }
                });
            }
            change_source_original_web.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    readingSourceDialog.dismiss();
                }
            });
            change_source_continue.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    requestItem.fromType = 3;// 打点统计 当前页面来源，所有可能来源的映射唯一字符串。书架(0)/目录页(1)/上一页翻页(2)/书籍封面(3)
                    if (bookDaoHelper.isBookSubed(bookVo.book_id)) {
                        Book book = bookDaoHelper.getBook(bookVo.book_id, 0);
                        if (Constants.QG_SOURCE.equals(requestItem.host)) {
                            readingCustomaryBook(null, false);
                        } else {
                            if (currentSource.book_source_id.equals(book.book_source_id)) {

                                //直接进入阅读
                                readingCustomaryBook(currentSource, true);
                                readingSourceDialog.dismiss();
                            } else {
                                //弹出切源提示
                                readingSourceDialog.dismiss();
                                showChangeSourceNoticeDialog(currentSource);
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
    private void readingCustomaryBook(CoverPage.SourcesBean source, boolean isCurrentSource) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        if (bookDaoHelper == null || bookCoverUtil == null || bookVo == null) {
            return;
        }
        Book book;

        if (source == null) {//说明是青果源的书
            book = bookDaoHelper.getBook(requestItem.book_id, 0);

        } else {
            book = bookDaoHelper.getBook(source.book_id, 0);
        }

        if (book != null && book.sequence != -2) {
            bundle.putInt("sequence", book.sequence);
            bundle.putInt("offset", book.offset);
        } else {
            bundle.putInt("sequence", -1);
        }
        if (book != null) {
            if (isCurrentSource) {
                book.last_updatetime_native = source.update_time;
            }
            if (Constants.QG_SOURCE.equals(bookVo.host)) {
                book.last_updatetime_native = bookVo.update_time;
            }
            bundle.putSerializable("book", book);
        }
        if (requestItem != null) {
            bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
        }
        if (book != null && requestItem != null) {
            AppLog.e(TAG, "GotoReading: " + book.site + " : " + requestItem.host);
        }
        intent.setClass(CoverPageActivity.this, ReadingActivity.class);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void continueReading() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        if (bookDaoHelper == null || bookCoverUtil == null || bookVo == null) {
            return;
        }
        Book book = bookCoverUtil.getCoverBook(bookDaoHelper, bookVo);
        if (bookDaoHelper.isBookSubed(requestItem.book_id) && book != null && book.sequence != -2) {
            bundle.putInt("sequence", book.sequence);
            bundle.putInt("offset", book.offset);
        } else {
            bundle.putInt("sequence", -1);
        }
        if (book != null) {
            if (Constants.QG_SOURCE.equals(bookVo.host)) {
                book.last_updatetime_native = bookVo.update_time;
            } else {
                if (currentSource != null) {
                    book.last_updatetime_native = currentSource.update_time;
                }
            }
            bundle.putSerializable("book", book);
        }
        if (requestItem != null) {
            bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
        }
        AppLog.e(TAG, "GotoReading: " + book.site + " : " + requestItem.host);
        intent.setClass(CoverPageActivity.this, ReadingActivity.class);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void intoReadingActivity(CoverPage.SourcesBean source) {
        //进入阅读页逻辑
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        if (bookDaoHelper == null || bookCoverUtil == null || bookVo == null) {
            return;
        }
        Book book = bookCoverUtil.getCoverBook(bookDaoHelper, bookVo);
        if (bookDaoHelper.isBookSubed(requestItem.book_id) && book != null && book.sequence != -2) {
            bundle.putInt("sequence", book.sequence);
            AppLog.e(TAG, "offset : " + book.offset);
        } else {
            bundle.putInt("sequence", -1);
        }


        if (requestItem != null) {
            requestItem.book_id = source.book_id;
            requestItem.book_source_id = source.book_source_id;
            requestItem.host = source.host;
            requestItem.dex = source.dex;
            Iterator<Map.Entry<String, String>> iterator = source.source.entrySet().iterator();
            ArrayList<String> list = new ArrayList<>();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                String value = entry.getValue();
                list.add(value);
            }
            if (list.size() > 0) {
                requestItem.parameter = list.get(0);
            }
            if (list.size() > 1) {
                requestItem.extra_parameter = list.get(1);
            }

            bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
        }

        if (book != null) {
            book = changeBookInformation(source, book);
            bundle.putSerializable("book", book);
        }
        AppLog.e(TAG, "GotoReading: " + book.site + " : " + requestItem.host);
        intent.setClass(CoverPageActivity.this, ReadingActivity.class);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
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
                    readingCustomaryBook(source, false);
                }
            });
            dialog_confirm.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismissDialog();
                    intoReadingActivity(source);
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
    public void onBackPressed() {
        Map<String, String> data = new HashMap<>();
        data.put("type", "2");
        StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.SYSTEM_PAGE, StartLogClickUtil.BACK, data);
        finish();
    }

    @Override
    protected void onDestroy() {

        try {
            setContentView(R.layout.empty);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        try {
            if (requestItem != null && !bookDaoHelper.isBookSubed(requestItem.book_id)) {
                deleteDatabase("book_chapter_" + requestItem.book_id);
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        if (bookCoverUtil != null) {
            bookCoverUtil.unRegistReceiver();
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

    private Book changeBookInformation(CoverPage.SourcesBean source, Book book) {
        BookDaoHelper bookDaoHelper = BookDaoHelper.getInstance();
        book.book_source_id = source.book_source_id;
        book.site = source.host;
        book.last_updatetime_native = source.update_time;
        book.dex = source.dex;
        //        if ("b.easou.com".equals(source.host)) {
        //            book.parameter = source.source.get(Constants.SOURCE_GID);
        //        } else if ("k.sogou.com".equals(source.host)) {
        //            book.parameter = source.source.get(Constants.SOURCE_MD);
        //            book.extra_parameter = source.source.get(Constants.SOURCE_ID);
        //        }
        Iterator<Map.Entry<String, String>> iterator = source.source.entrySet().iterator();
        ArrayList<String> list = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String value = entry.getValue();
            list.add(value);
        }
        if (list.size() > 0) {
            book.parameter = list.get(0);
        }
        if (list.size() > 1) {
            book.extra_parameter = list.get(1);
        }

        if (bookDaoHelper.isBookSubed(source.book_id)) {
            bookDaoHelper.updateBook(book);
        }

        BookChapterDao bookChapterDao = new BookChapterDao(CoverPageActivity.this, source.book_id);

        BookHelper.deleteAllChapterCache(source.book_id, 0, bookChapterDao.getCount());
        bookChapterDao.deleteBookChapters(0);
        DownloadService.clearTask(source.book_id);
        BaseBookHelper.delDownIndex(this, source.book_id);
        return book;
    }

    private static class UIHandler extends Handler {

        private WeakReference<CoverPageActivity> weakReference;

        UIHandler(CoverPageActivity coverPageActivity) {
            weakReference = new WeakReference<>(coverPageActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            CoverPageActivity coverPageActivity = weakReference.get();
            if (coverPageActivity == null) {
                return;
            }
            switch (msg.what) {
                case RequestExecutor.REQUEST_COVER_SUCCESS:
                    coverPageActivity.handleOK(msg.obj, false);
                    break;
                case RequestExecutor.REQUEST_COVER_ERROR:
                    coverPageActivity.handleError();
                    break;
                case RequestExecutor.REQUEST_COVER_QG_SUCCESS:
                    coverPageActivity.handleOK(msg.obj, true);
                    break;
                case RequestExecutor.REQUEST_COVER_QG_ERROR:
                    coverPageActivity.handleError();
                    break;
                case GET_CATEGORY_OK:
                    coverPageActivity.handCategoryOk();
                    break;
                case GET_CATEGORY_ERROR:
                    coverPageActivity.handCategoryError();
                    break;
                default:
                    break;
            }
        }
    }
}
