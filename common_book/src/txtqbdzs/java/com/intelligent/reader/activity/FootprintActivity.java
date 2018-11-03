package com.intelligent.reader.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ding.basic.RequestRepositoryFactory;
import com.ding.basic.bean.HistoryInfo;
import com.dingyue.statistics.DyStatService;
import com.intelligent.reader.R;

import net.lzbook.kit.app.base.BaseBookApplication;
import net.lzbook.kit.bean.EventBookStore;
import net.lzbook.kit.pointpage.EventPoint;
import net.lzbook.kit.ui.activity.base.FrameActivity;
import net.lzbook.kit.ui.adapter.HisAdapter;
import net.lzbook.kit.ui.adapter.LoadMoreAdapterWrapper;
import net.lzbook.kit.ui.adapter.base.BaseAdapter;
import net.lzbook.kit.ui.widget.EmptyRecyclerView;
import net.lzbook.kit.ui.widget.MyDialog;
import net.lzbook.kit.utils.AbsRecyclerViewHolder;
import net.lzbook.kit.utils.StatServiceUtils;
import net.lzbook.kit.utils.logger.AppLog;
import net.lzbook.kit.utils.user.UserManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FootprintActivity extends FrameActivity implements AbsRecyclerViewHolder.ShelfItemClickListener, AbsRecyclerViewHolder.ShelfItemLongClickListener, LoadMoreAdapterWrapper.OnLoad, View.OnClickListener, EmptyRecyclerView.OnItemChangeListener {

    private static final String TAG = FootprintActivity.class.getSimpleName();
    private EmptyRecyclerView mRecyclerView;
    private BaseAdapter mLoadMoreAdapter;
    private List<HistoryInfo> mDataSet;
    private HisAdapter mHisAdapter;
    private ImageView mBack;
    private TextView mClearDataTV;
    private TextView mEmptyFind;
    private View mNotLoginView;
    private View mEmptyView;
    private TextView mLoginTV;
    private TextView mLoginInfo;
    private TextView mTypeInfoTV;
    private boolean currLoginState;
    private RequestRepositoryFactory requestRepositoryFactory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatServiceUtils.statAppBtnClick(this, StatServiceUtils.his_into);
        setContentView(R.layout.activity_footprint);
        currLoginState = !UserManager.INSTANCE.isUserLogin();
        requestRepositoryFactory = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(
                BaseBookApplication.getGlobalContext());
        initView();
        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mLoginTV.setClickable(true);
        checkInit(UserManager.INSTANCE.isUserLogin());
    }

    private void checkInit(boolean isLogin) {
        if (isLogin == currLoginState) {
            return;
        }

        if (isLogin) {
            initData();
        } else {
            initNotLoginData();
        }
        currLoginState = isLogin;
    }

    private void initView() {
        mRecyclerView = (EmptyRecyclerView) findViewById(R.id.recycler_footprint);
        mBack = (ImageView) findViewById(R.id.book_history_back);
        mClearDataTV = (TextView) findViewById(R.id.book_history_clear);

        mEmptyView = findViewById(R.id.footprint_empty);
        mEmptyFind = (TextView) findViewById(R.id.footprint_empty_find);

        mNotLoginView = findViewById(R.id.footprint_not_login);
        mNotLoginView.setVisibility(View.VISIBLE);
        mNotLoginView.setClickable(true);
        mLoginTV = (TextView) findViewById(R.id.footprint_to_login);
        mLoginInfo = (TextView) findViewById(R.id.footprint_login_hint);
        mTypeInfoTV = (TextView) findViewById(R.id.footprint_type_tv);
    }

    private void initNotLoginData() {
        if (mRecyclerView != null) {
            if (mRecyclerView.getVisibility() == View.VISIBLE) {
                mRecyclerView.setVisibility(View.GONE);
            }
        }

        if (mEmptyView != null) {
            if (mEmptyView.getVisibility() == View.VISIBLE) {
                mEmptyView.setVisibility(View.GONE);
            }
        }

        initClearBtnState(false);

        if (mNotLoginView != null) {
            mNotLoginView.setVisibility(View.VISIBLE);
            mNotLoginView.setClickable(true);
        }

        int dataCount = 0;
        try {
            dataCount = (int) requestRepositoryFactory.getHistoryCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String typeInfo = null;
        if (mTypeInfoTV != null) {
            typeInfo = (String) mTypeInfoTV.getText();
        }

        if ("TypeTwo".equals(typeInfo)) {
            TextView loginInfoTwo = (TextView) findViewById(R.id.footprint_login_hint2);
            if (dataCount > 0) {
                mLoginInfo.setText("您最近浏览了" + dataCount + "本书");
                loginInfoTwo.setVisibility(View.VISIBLE);
                loginInfoTwo.setText("请登录后查看");
            } else {
                mLoginInfo.setText("登录后可查看浏览过的书");
                loginInfoTwo.setVisibility(View.GONE);
            }
        } else {
            if (dataCount > 0) {
                mLoginInfo.setText("您最近浏览了" + dataCount + "本书, 请登录后查看");
            } else {
                mLoginInfo.setText("登录后可查看浏览过的书");
            }
        }
    }

    private void initData() {
        if (mNotLoginView != null) {
            if (mNotLoginView.getVisibility() == View.VISIBLE) {
                mNotLoginView.setVisibility(View.GONE);
            }
        }

        try {
            mDataSet = requestRepositoryFactory.queryHistoryPaging(0L, LoadMoreAdapterWrapper.PAGE_SIZE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mDataSet == null || mDataSet.isEmpty()) {
            initClearBtnState(false);
        } else {
            initClearBtnState(true);
        }

        // 创建被装饰者
        mHisAdapter = new HisAdapter(this, this, this);
        mHisAdapter.appendData(mDataSet);
        // 创建装饰者
        mLoadMoreAdapter = new LoadMoreAdapterWrapper(mHisAdapter, this);
        mRecyclerView.setEmptyView(mEmptyView);
        mRecyclerView.setAdapter(mLoadMoreAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    private void initListener() {
        if (mBack != null) {
            mBack.setOnClickListener(this);
        }

        if (mClearDataTV != null) {
            mClearDataTV.setOnClickListener(this);
        }

        if (mEmptyFind != null) {
            mEmptyFind.setOnClickListener(this);
        }

        if (mRecyclerView != null) {
            mRecyclerView.setOnItemChangeListener(this);
        }

        if (mLoginTV != null) {
            mLoginTV.setOnClickListener(this);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        HistoryInfo info = null;
        if (mHisAdapter != null) {
            List<HistoryInfo> dataSet = mHisAdapter.getDataSet();
            if (dataSet != null && dataSet.size() > position) {
                info = dataSet.get(position);
            }
        }

        if (info != null) {
            Intent intent = new Intent();
            intent.setClass(this, CoverPageActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("author", info.getAuthor());
            bundle.putString("book_id", info.getBook_id());
            bundle.putString("book_source_id", info.getBook_source_id());
            intent.putExtras(bundle);
            startActivity(intent);
            StatServiceUtils.statAppBtnClick(this.getApplicationContext(), StatServiceUtils.cover_into_his);
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }

    @Override
    public void load(final int pagePosition, final int pageSize, final LoadMoreAdapterWrapper.ILoadCallback callback) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                AppLog.d(TAG, "pagePosition = " + pagePosition);
                List<HistoryInfo> dataSet = null;
                try {
                    dataSet = requestRepositoryFactory.queryHistoryPaging((long)pagePosition, (long) pageSize);
                }catch (Exception e){
                    e.printStackTrace();
                }

                if (dataSet == null || dataSet.isEmpty() || dataSet.size() < pageSize) {
                    callback.onFailure();
                }else{
                    mHisAdapter.appendData(dataSet);
                    callback.onSuccess();
                }
            }
        }, 200);
    }


    @Override
    public void onClick(View v) {
        if (v == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.book_history_back:
                Map<String, String> data = new HashMap<>();
                data.put("type", "1");
                DyStatService.onEvent(EventPoint.PERHISTORY_BACK, data);
                finish();
                break;
            case R.id.book_history_clear:
                showDialog();
                break;
            case R.id.footprint_to_login:
                mLoginTV.setClickable(false);
                Intent intent = new Intent(this, LoginActivity.class);
                startActivityForResult(intent, 1);
                DyStatService.onEvent(EventPoint.PERSONAL_HISTORYLOGIN);
                break;
            case R.id.footprint_empty_find:
                Intent storeIntent = new Intent();
                storeIntent.setClass(this, HomeActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(EventBookStore.BOOKSTORE, EventBookStore.TYPE_TO_BOOKSTORE);
                storeIntent.putExtras(bundle);
                startActivity(storeIntent);
                break;
            default:
                break;
        }
    }

    private void initClearBtnState(boolean isEnable) {
        if (mClearDataTV == null || isEnable == mClearDataTV.isEnabled()) {
            return;
        }

        int clearTvColor = 0;

        if (isEnable) {
            clearTvColor = R.color.footprint_title_clear_color;
        } else {
            clearTvColor = R.color.footprint_title_clear_unusable_color;
        }

        mClearDataTV.setTextColor(getResources().getColorStateList(clearTvColor));
        mClearDataTV.setEnabled(isEnable);

    }

    @Override
    public void onItemChange(int itemCount) {
        if (itemCount > 0) {
            initClearBtnState(true);
        } else {
            initClearBtnState(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode) {
            checkInit(UserManager.INSTANCE.isUserLogin());
        }
    }

    private void showDialog() {
        if (!this.isFinishing()) {
            final MyDialog myDialog = new MyDialog(this, R.layout.publish_hint_dialog);
            myDialog.setCanceledOnTouchOutside(true);
            TextView dialog_title = (TextView) myDialog.findViewById(R.id.dialog_title);
            dialog_title.setText(R.string.prompt);
            TextView dialog_content = (TextView) myDialog.findViewById(R.id.publish_content);
            dialog_content.setText(R.string.determine_clear_footprint_history);
            TextView dialog_comfire = (TextView) myDialog.findViewById(R.id.publish_leave);

            dialog_comfire.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mHisAdapter != null && mLoadMoreAdapter != null && requestRepositoryFactory
                            != null) {
                        mHisAdapter.updateData(null);
                        mLoadMoreAdapter.notifyDataSetChanged();
                        requestRepositoryFactory.deleteAllHistory();
                    }
                    myDialog.dismiss();
                }
            });
            TextView dialog_cancle = (TextView) myDialog.findViewById(R.id.publish_stay);
            dialog_cancle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myDialog.dismiss();
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
}
