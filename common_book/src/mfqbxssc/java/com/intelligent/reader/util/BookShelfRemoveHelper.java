package com.intelligent.reader.util;

import com.intelligent.reader.R;
import com.intelligent.reader.adapter.BookDetailAdapter;
import com.intelligent.reader.adapter.BookShelfReAdapter;
import com.intelligent.reader.event.ChangeHomeSelectEvent;

import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout;
import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.popup.PopupWindowInterface;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * BookShelfRemoveHelper
 */
public class BookShelfRemoveHelper implements View.OnClickListener {

    private final static long DELAY_TIME = 500;
    public PopupWindow popupWindowDetail;
    protected PopupWindowInterface popupWindowManager;
    protected OnMenuStateListener menuStateListener;
    protected OnMenuDeleteClickListener deleteClickListener;
    protected OnMenuSelectAllListener selectAllListener;
    protected BookShelfReAdapter bookShelfReAdapter;
    protected SuperSwipeRefreshLayout Layout;
    String TAG = "BookShelfRemoveHelper";
    Handler handler = new Handler();
    private Activity mContext;
    private PopupWindow popupWindow;
    private Button delete_btn;
    private Button selectAll_btn;
    private Button btnDetail;
    private View showView;

    private ViewPager viewPager;
    private BookDetailAdapter adapter;
    private TextView tvNum;
    private TextView tvCurrentNum;
    private TextView tvTotalNum;


    private List<Book> mlist = new ArrayList<>();

    public BookShelfRemoveHelper(Activity context, BookShelfReAdapter adapter) {//显示指定菜单类别
        mContext = context;
        bookShelfReAdapter = adapter;
        setRemoveWindow(context);

    }

    public boolean isRemoveMode() {//判定当前菜单状态是否为删除模式
        if (bookShelfReAdapter != null) {
            return bookShelfReAdapter.isRemoveMode();
        }
        return false;
    }

    public void setLayout(SuperSwipeRefreshLayout layout) {//指定使用菜单的ListView
        this.Layout = layout;
    }

    ////指定回调对象
    public void setOnMenuDeleteListener(OnMenuDeleteClickListener click) {//指定删除回调对象
        deleteClickListener = click;
    }

    public void setOnSelectAllListener(OnMenuSelectAllListener selectAllListener) {
        this.selectAllListener = selectAllListener;
    }

    public void setOnMenuStateListener(OnMenuStateListener shownListener) {//指定菜单状态监听回调对象
        menuStateListener = shownListener;
    }

    public void setCheckPosition(int position) {//点击选中position,已自动判定全选或非全选状态
        if (bookShelfReAdapter != null) {
            bookShelfReAdapter.setChecked(position);
            bookShelfReAdapter.notifyItemRangeChanged(position, 1);
            setDeleteNum();

            if (menuStateListener != null) {
                menuStateListener.getAllCheckedState(isAllChecked());
            }

        }
    }

    public boolean isAllChecked() {
        return bookShelfReAdapter.getCheckedSize() == bookShelfReAdapter.getItemCount();
    }


    /**
     * use to dissmiss the remove window
     */
    public boolean dismissRemoveMenu() {//显示关闭菜单
        if (popupWindow != null && popupWindow.isShowing()) {
            onShowing(false);
            popupWindow.dismiss();
            return true;
        }
        return false;
    }


    /**
     * use to show the remove window
     *
     * @param parent the view which the menu belong with
     */
    public void showRemoveMenu(View parent) {//显示菜单
        showView = parent;
        if (popupWindow != null && popupWindow.isShowing()) {
            onShowing(false);
            popupWindow.dismiss();
        } else if (parent != null && popupWindow != null) {
            onShowing(true);
            popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
        }
    }

    public void showRemoveMenu2(View parent, int position) {//显示菜单
        showView = parent;
        if (popupWindow != null && popupWindow.isShowing()) {
            onShowing(false);
            popupWindow.dismiss();
        } else if (parent != null && popupWindow != null) {
            onShowing(true);
            bookShelfReAdapter.setChecked(position);
            setDeleteNum();
            popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
        }
    }

    public void selectAll(boolean checkedAll) {
        if (bookShelfReAdapter != null) {
            bookShelfReAdapter.setAllChecked(checkedAll);
            bookShelfReAdapter.notifyDataSetChanged();
            setDeleteNum();
        }
        if (selectAllListener != null) {
            selectAllListener.onSelectAll();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.btn_right:
                if (deleteClickListener != null) {
                    deleteClickListener.onMenuDelete(bookShelfReAdapter.remove_checked_states);
                }
                break;
            case R.id.btn_left:
                selectAll(isAllChecked() ? false : true);
                break;
            case R.id.btn_detail:
                mlist.clear();

                if (bookShelfReAdapter.remove_checked_states != null && bookShelfReAdapter.remove_checked_states.size() != 0) {
                    List<Integer> list = new ArrayList(bookShelfReAdapter.remove_checked_states);
                    Collections.sort(list);
                    for (int i = 0; i < list.size(); i++) {
                        mlist.add(bookShelfReAdapter.book_list.get(Integer.parseInt(list.get(i) + "")));
                    }
                }
                if (mlist.size() > 0) {
                    showBookDetailPop(showView);
                } else {
                    Toast.makeText(mContext, "请先选择书籍", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    public void showBookDetailPop(View view) {
        //自定义PopupWindow的布局
        View contentView = LayoutInflater.from(mContext).inflate(R.layout.popwindow_book_detail, null);
        popupWindowDetail = new PopupWindow(contentView);
        popupWindowDetail.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindowDetail.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindowDetail.setFocusable(true);
        popupWindowDetail.setBackgroundDrawable(new ColorDrawable(0x60000000));   //为PopupWindow设置半透明背景.
        popupWindowDetail.setOutsideTouchable(true);
        popupWindowDetail.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                AppLog.e("kkk", "kkk");
                setBackgroundAlpha(1.0f);
            }
        });


        viewPager = (ViewPager) contentView.findViewById(R.id.viewPager);
        tvNum = (TextView) contentView.findViewById(R.id.tv_num);
        tvCurrentNum = (TextView) contentView.findViewById(R.id.current_num);
        tvTotalNum = (TextView) contentView.findViewById(R.id.total_num);

        tvCurrentNum.setText("1");
        if (mlist.size() != 0) {
            tvTotalNum.setText(mlist.size() + "");
        }

        viewPager.setAdapter(new BookDetailAdapter(mlist, mContext));
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tvCurrentNum.setText(position + 1 + "");
                tvTotalNum.setText(mlist.size() + "");
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        //设置PopupWindow进入和退出动画
        popupWindowDetail.setAnimationStyle(R.style.remove_menu_anim_style);
        //设置PopupWindow显示的位置
        popupWindowDetail.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        setBackgroundAlpha(0.6f);
    }

    public void setBackgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = mContext.getWindow()
                .getAttributes();
        lp.alpha = bgAlpha;

        mContext.getWindow().setAttributes(lp);
    }

    private void setRemoveWindow(Context context) {//实例化菜单对象

        View baseView = LayoutInflater.from(context).inflate(R.layout.book_shelf_bottom, null);
        popupWindow = new PopupWindow(baseView, WindowManager.LayoutParams.MATCH_PARENT, (int) context.getResources().getDimension(R.dimen.dimen_view_height_default));
        popupWindow.setAnimationStyle(R.style.remove_menu_anim_style);
        delete_btn = (Button) baseView.findViewById(R.id.btn_right);
        selectAll_btn = (Button) baseView.findViewById(R.id.btn_left);
        btnDetail = (Button) baseView.findViewById(R.id.btn_detail);
        LinearLayout layout = (LinearLayout) baseView.findViewById(R.id.remove_delete_layout);

        layout.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MENU) {
                    onShowing(false);
                    if (popupWindow != null) {
                        popupWindow.dismiss();
                    }
                    return true;
                }
                return false;
            }
        });

        layout.setFocusable(true);
        layout.setFocusableInTouchMode(true);
        layout.requestFocus();

        delete_btn.setOnClickListener(this);
        selectAll_btn.setOnClickListener(this);
        btnDetail.setOnClickListener(this);
    }

    public void onShowing(final boolean isShowing) {//菜单打开及关闭状态
        if (bookShelfReAdapter != null) {
            bookShelfReAdapter.setRemoveMode(isShowing);
            bookShelfReAdapter.resetRemovedState();
            setDeleteNum();
        }
        if (menuStateListener != null) {
            menuStateListener.getAllCheckedState(isAllChecked());
        }
        if (isShowing) {
            if (menuStateListener != null) {
                menuStateListener.getMenuShownState(isShowing);
            }
            if (bookShelfReAdapter != null) {

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        menuStateListener.doHideAd();
                        bookShelfReAdapter.setListPadding(Layout, isShowing);
                        bookShelfReAdapter.notifyDataSetChanged();
                    }
                }, DELAY_TIME);

            }
        } else {
            if (menuStateListener != null) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        menuStateListener.getMenuShownState(isShowing);
                    }
                }, 300);
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (bookShelfReAdapter != null) {
                        bookShelfReAdapter.setRemoveMode(isShowing);
                        bookShelfReAdapter.setListPadding(Layout, isShowing);
                        bookShelfReAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    private void setDeleteNum() {

        if (bookShelfReAdapter != null && delete_btn != null && selectAll_btn != null) {
            int num = bookShelfReAdapter.getCheckedSize();
            EventBus.getDefault().post(new ChangeHomeSelectEvent(isAllChecked()));

            if (num == 0) {

                delete_btn.setText("删除");
                int textColor = 0;
                Resources.Theme theme = mContext.getTheme();
                textColor = R.color.bookshelf_last_chapter_text_color_1;
                delete_btn.setTextColor(mContext.getResources().getColor(textColor));
            } else {
                delete_btn.setText("删除 (" + num + ")");
                int textColor = 0;

                textColor = R.color.setting_login_font_color;
                delete_btn.setTextColor(mContext.getResources().getColor(textColor));

            }
        }
    }

    // ====================================================
    // callback
    // ===================================================
    public interface OnMenuStateListener {//菜单状态监听

        void getMenuShownState(boolean isShown);//菜单开启状态

        void getAllCheckedState(boolean isAll);//菜单全选状态

        void doHideAd();//编辑状态删除广告数据

    }


    public interface OnMenuSelectAllListener {
        void onSelectAll();
    }

    public interface OnMenuDeleteClickListener {//菜单删除按钮监听

        /**
         * delete button
         *
         * @param checked_state position which the list checkbox is selected
         */
        public void onMenuDelete(HashSet<Integer> checked_state);//删除按钮回调，得到被选中的position
    }


}