package com.intelligent.reader.util;

import com.intelligent.reader.R;
import com.intelligent.reader.adapter.BookShelfReAdapter;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout;
import net.lzbook.kit.utils.popup.PopupWindowInterface;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * BookShelfRemoveHelper
 */
public class BookShelfRemoveHelper implements View.OnClickListener {

    private final static long DELAY_TIME = 500;
    protected PopupWindowInterface popupWindowManager;
    protected OnMenuStateListener menuStateListener;
    protected OnMenuDeleteClickListener deleteClickListener;
    protected OnMenuSelectAllListener selectAllListener;
    protected BookShelfReAdapter bookShelfReAdapter;
    protected SuperSwipeRefreshLayout Layout;
    String TAG = "BookShelfRemoveHelper";
    Handler handler = new Handler();
    private Context mContext;
    private PopupWindow popupWindow;
    private Button delete_btn;
    private Button selectAll_btn;

    public BookShelfRemoveHelper(Context context, BookShelfReAdapter adapter) {//显示指定菜单类别
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

    private boolean isAllChecked() {
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
        if (popupWindow != null && popupWindow.isShowing()) {
            onShowing(false);
            popupWindow.dismiss();
        } else if (parent != null && popupWindow != null) {
            onShowing(true);
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
                Map<String, String> data = new HashMap<>();
                data.put("type", isAllChecked() ? "2" : "1");
                StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.SHELFEDIT_PAGE, StartLogClickUtil.SELECTALL1, data);
                selectAll(isAllChecked() ? false : true);
                break;
        }
    }

    private void setRemoveWindow(Context context) {//实例化菜单对象

        View baseView = LayoutInflater.from(context).inflate(R.layout.download_manager_bottom, null);
        popupWindow = new PopupWindow(baseView, WindowManager.LayoutParams.MATCH_PARENT, (int) context.getResources().getDimension(R.dimen.dimen_view_height_default));
        popupWindow.setAnimationStyle(R.style.remove_menu_anim_style);
        delete_btn = (Button) baseView.findViewById(R.id.btn_right);
        selectAll_btn = (Button) baseView.findViewById(R.id.btn_left);
        ViewGroup layout = (ViewGroup) baseView.findViewById(R.id.remove_delete_layout);

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
            selectAll_btn.setText(isAllChecked() ? "取消全选" : "全选");
            if (num == 0) {
                delete_btn.setText("删除");
                int textCsl = mContext.getResources().getColor(R.color.color_gray_babfc1);
                delete_btn.setTextColor(textCsl);
                delete_btn.setBackgroundResource(com.intelligent.reader.R.drawable.bookshelf_delete_submit_default_bg);
            } else {
                delete_btn.setText("删除 (" + num + ")");
                int draw = mContext.getResources().getColor(com.intelligent.reader.R.color.bookshelf_delete_submit_text_color);
                delete_btn.setTextColor(draw);
                delete_btn.setBackgroundResource(com.intelligent.reader.R.drawable.bookshelf_delete_submit_bg);
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