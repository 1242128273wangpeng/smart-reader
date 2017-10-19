package com.intelligent.reader.activity;

import com.intelligent.reader.R;
import com.intelligent.reader.adapter.SourceAdapter;
import com.intelligent.reader.read.help.BookHelper;
import com.intelligent.reader.util.EventBookStore;

import net.lzbook.kit.book.component.service.DownloadService;
import net.lzbook.kit.book.view.LoadingPage;
import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.book.view.RecommendItemView;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.ReadStatus;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.data.bean.Source;
import net.lzbook.kit.data.bean.SourceItem;
import net.lzbook.kit.data.db.BookChapterDao;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.request.own.OtherRequestService;
import net.lzbook.kit.utils.BaseBookHelper;
import net.lzbook.kit.utils.StatisticManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

public class BookEndActivity extends BaseCacheableActivity implements View.OnClickListener {
    private static final String TAG = "BookEndActivity";
    private View iv_back_bookstore, iv_back, iv_title_right;
    private ImageView ad_view;
    private ImageView ad_view_logo;
    //    private ImageView item_ad_image ;
//    private TextView item_ad_title ;
//    private RatingBar item_ad_extension ;
//    private TextView item_ad_desc ;
    private TextView textView_endInfo;
    //    private ImageView item_ad_right_down ;
//    private RelativeLayout bookend_ad_layout;
    private Book book;
    private String bookName;
    private LoadingPage loadingPage;
    private TextView name_bookend;

    private StatisticManager statisticManager;

    private RequestItem requestItem;
    private String category;
    private String book_id;
    private MyDialog myDialog;
    private ReadStatus readStatus;

    private ArrayList<Source> sourceList = new ArrayList<>();
    private SourceAdapter sourceAdapter = null;
    private ListView sourceListView;

    private BookDaoHelper mBookDaoHelper;

    private String thememode;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    sourceListView.setVisibility(View.VISIBLE);
                    int count = ((SourceItem) msg.obj).sourceList.size();
                    if (count != 0) {
                        for (int i = 0; i < count; i++) {
                            if (i < 3) {
                                sourceList.add(((SourceItem) msg.obj).sourceList.get(i));
                            }
                        }
                        sourceListView.getLayoutParams().height = sourceList.size() * getResources().getDimensionPixelOffset(R
                                .dimen.dimen_view_height_70);
                        sourceAdapter.notifyDataSetChanged();
                    }
                    if (loadingPage != null) {
                        loadingPage.onSuccess();
                    }
                    break;
                case 0:
                    sourceListView.setVisibility(View.GONE);
                    if (loadingPage != null) {
                        loadingPage.onSuccess();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_over);
        initView();
        readStatus = new ReadStatus(getApplicationContext());
        initData();
        loadingPage = new LoadingPage(this, LoadingPage.setting_result);
        getBookSource();
        loadingPage.setReloadAction(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                getBookSource();
                return null;
            }
        });
    }

    private void initView() {
        sourceListView = (ListView) findViewById(R.id.sourcelist_bookend);
        iv_title_right = findViewById(R.id.iv_title_right);
        iv_back = findViewById(R.id.iv_back);
        iv_back_bookstore = findViewById(R.id.iv_back_bookstore);
        iv_title_right.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        iv_back_bookstore.setOnClickListener(this);
        ad_view = (ImageView) findViewById(R.id.ad_view);
        ad_view.setOnClickListener(this);
        ad_view_logo = (ImageView) findViewById(R.id.ad_view_logo);
        ad_view_logo.setVisibility(View.GONE);
        textView_endInfo = (TextView) findViewById(R.id.textView_endInfo);
        textView_endInfo.setText(Html.fromHtml(getResources().getString(R.string.book_end_info)));
        name_bookend = (TextView) findViewById(R.id.name_bookend);

//        item_ad_image = (ImageView) findViewById(R.id.item_ad_image);
//        item_ad_title = (TextView) findViewById(R.id.item_ad_title);
//        item_ad_extension = (RatingBar) findViewById(R.id.item_ad_extension);
//        item_ad_desc = (TextView)findViewById(R.id.item_ad_desc);
//        item_ad_right_down = (ImageView)findViewById(R.id.item_ad_right_down);
//        bookend_ad_layout =(RelativeLayout)findViewById(R.id.bookend_ad_layout);
//        bookend_ad_layout.setVisibility(View.GONE);
//        bookend_ad_layout.setOnClickListener(this);


    }

    private void initData() {
        mBookDaoHelper = BookDaoHelper.getInstance();
        if (getIntent() != null) {
            //readStatus = (ReadStatus) getIntent().getSerializableExtra("readStatus");
            bookName = getIntent().getStringExtra("bookName");
            book = (Book) getIntent().getSerializableExtra("book");
            requestItem = (RequestItem) getIntent().getSerializableExtra(Constants.REQUEST_ITEM);
            category = getIntent().getStringExtra("book_category");
            book_id = getIntent().getStringExtra("book_id");
            name_bookend.setText(bookName);
            readStatus.sequence = getIntent().getIntExtra("sequence", 0);
            readStatus.offset = getIntent().getIntExtra("offset", 0);
            thememode = getIntent().getStringExtra("thememode");
            if (requestItem != null) {
                readStatus.requestItem = requestItem;
            }
            readStatus.book = book;
            readStatus.book_id = book_id;

            sourceAdapter = new SourceAdapter(this, sourceList);

            sourceListView.setAdapter(sourceAdapter);
            sourceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Source source = sourceList.get(position);
                    if (mBookDaoHelper.isBookSubed(readStatus.book_id)) {
                        if (!source.book_source_id.equals(book.book_source_id)) {
                            //弹出切源提示
                            showChangeSourceNoticeDialog(source);
                            return;
                        }
                    }
                    intoCatalogActivity(source, false);
                    //openCategoryPage();
                }
            });
        }
        if (requestItem == null) {
            finish();
        }
    }

    private void showChangeSourceNoticeDialog(final Source source) {
        if (!isFinishing()) {
            dismissDialog();

            myDialog = new MyDialog(this, R.layout.publish_hint_dialog);
            myDialog.setCanceledOnTouchOutside(true);
            Button dialog_cancel = (Button) myDialog.findViewById(R.id.publish_stay);
            dialog_cancel.setText(R.string.cancel);
            Button dialog_confirm = (Button) myDialog.findViewById(R.id.publish_leave);
            dialog_confirm.setText(R.string.book_cover_confirm_change_source);
            TextView dialog_information = (TextView) myDialog.findViewById(R.id.publish_content);
            dialog_information.setText(R.string.book_cover_change_source_prompt);
            dialog_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissDialog();
                }
            });
            dialog_confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismissDialog();
                    intoCatalogActivity(source, true);
                }
            });

            myDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    myDialog.dismiss();
                }
            });
            if (!myDialog.isShowing()) {
                try {
                    myDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void intoCatalogActivity(Source source, boolean b) {
        if (readStatus != null && readStatus.getRequestItem() != null) {
            readStatus.firstChapterCurl = "";
            //dataFactory.currentChapter = null;

            RequestItem requestItem = new RequestItem();
            requestItem.book_id = source.book_id;
            requestItem.book_source_id = source.book_source_id;
            requestItem.host = source.host;
            requestItem.name = bookName;
            requestItem.author = readStatus.book.author;
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


            readStatus.setRequestItem(requestItem);
            //readStatus.requestConfig = BookApplication.getGlobalContext().getSourceConfig(requestItem.host);


            BookDaoHelper bookDaoHelper = BookDaoHelper.getInstance();
            if (bookDaoHelper.isBookSubed(source.book_id)) {
                Book iBook = bookDaoHelper.getBook(source.book_id, 0);
                iBook.book_source_id = requestItem.book_source_id;
                iBook.site = requestItem.host;
                iBook.parameter = requestItem.parameter;
                iBook.extra_parameter = requestItem.extra_parameter;
                iBook.last_updatetime_native = source.update_time;
                iBook.dex = source.dex;
                bookDaoHelper.updateBook(iBook);
                readStatus.book = iBook;
                if (b) {
                    BookChapterDao bookChapterDao = new BookChapterDao(BookEndActivity.this, source.book_id);
                    BookHelper.deleteAllChapterCache(source.book_id, 0, bookChapterDao.getCount());
                    bookChapterDao.deleteBookChapters(0);
                    DownloadService.clearTask(source.book_id);
                    BaseBookHelper.delDownIndex(this, source.book_id);
                }
            } else {
                Book iBook = readStatus.book;
                iBook.book_source_id = source.book_source_id;
                iBook.site = source.host;
                iBook.dex = source.dex;
                iBook.parameter = requestItem.parameter;
                iBook.extra_parameter = requestItem.extra_parameter;
                readStatus.book = iBook;
            }
            //dataFactory.chapterList.clear();
            openCategoryPage();
        }
    }

    private void dismissDialog() {
        if (myDialog != null && myDialog.isShowing()) {
            myDialog.dismiss();
        }
    }

    private void openCategoryPage() {
        //if (readStatus.book.book_type == 0) {
        Intent intent = new Intent(BookEndActivity.this, CataloguesActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("cover", readStatus.book);
        bundle.putString("book_id", readStatus.book_id);
        //AppLog.e(TAG, "OpenCategoryPage: " + readStatus.sequence);
        bundle.putInt("sequence", readStatus.sequence);
        bundle.putBoolean("fromCover", true);
        bundle.putBoolean("fromEnd", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //AppLog.e(TAG, "ReadingActivity: " + readStatus.getRequestItem().toString());
        bundle.putSerializable(Constants.REQUEST_ITEM, readStatus.getRequestItem());
        intent.putExtras(bundle);
        startActivity(intent);
        //}
    }

    private void getBookSource() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!requestItem.host.equals(Constants.QG_SOURCE) && !requestItem.host.equals(Constants.SG_SOURCE)) {
                        OtherRequestService.requestBookSourceChange(handler, 1, -144, book_id);
                    } else {
                        handler.sendEmptyMessage(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        if (v instanceof RecommendItemView) {
            Intent recommendIntent = new Intent();
            RecommendItemView item = (RecommendItemView) v;
            recommendIntent.putExtra("word", item.getTitle());
            recommendIntent.putExtra("search_type", "0");
            recommendIntent.putExtra("filter_type", "0");
            recommendIntent.putExtra("filter_word", "ALL");
            recommendIntent.putExtra("sort_type", "0");
            recommendIntent.setClass(this, SearchBookActivity.class);
            startActivity(recommendIntent);
            return;
        }

        switch (v.getId()) {
            //去书城
            case R.id.iv_back_bookstore:
                Intent storeIntent = new Intent();
                storeIntent.setClass(BookEndActivity.this, HomeActivity.class);
                try {
                    Bundle bundle = new Bundle();
                    bundle.putInt(EventBookStore.BOOKSTORE, EventBookStore.TYPE_TO_BOOKSTORE);
                    storeIntent.putExtras(bundle);
//                    if(!thememode.equals(mThemeHelper.getMode())){
//                        ATManager.exitClient();
//                    }
                    startActivity(storeIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finish();
                break;

            //去书架
            case R.id.iv_title_right:
                Intent shelfIntent = new Intent();
                shelfIntent.setClass(BookEndActivity.this, HomeActivity.class);
                try {
                    Bundle bundle = new Bundle();
                    bundle.putInt(EventBookStore.BOOKSTORE, EventBookStore.TYPE_TO_BOOKSHELF);
                    shelfIntent.putExtras(bundle);
//                    if(!thememode.equals(mThemeHelper.getMode())){
//                        ATManager.exitClient();
//                    }
                    startActivity(shelfIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finish();

                break;
            case R.id.iv_back:
                finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (loadingPage != null) {
            loadingPage = null;
        }
        try {
            setContentView(R.layout.empty);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }
}