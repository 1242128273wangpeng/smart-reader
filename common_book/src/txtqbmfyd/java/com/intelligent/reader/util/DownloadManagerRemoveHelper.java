package com.intelligent.reader.util;

import android.content.Context;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.intelligent.reader.R;
import com.intelligent.reader.adapter.DownloadManagerAdapter;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout;
import net.lzbook.kit.utils.ExtensionsKt;
import net.lzbook.kit.utils.popup.PopupWindowInterface;

import java.util.ArrayList;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class DownloadManagerRemoveHelper {

    protected PopupWindowInterface popupWindowManager;
    protected OnMenuStateListener menuStateListener;
    protected OnMenuDeleteClickListener deleteClickListener;
    private DownloadManagerAdapter downloadManagerAdapter;
    protected SuperSwipeRefreshLayout Layout;
    String TAG = "BookShelfRemoveHelper";
    private Context mContext;
    private PopupWindow popupWindow;
    private Button btnDelete;
    private Button btnCancel;
    private ListView listView;
    private int distanceY;

    public DownloadManagerRemoveHelper(Context context, DownloadManagerAdapter adapter,
                                       ListView listView) {//显示指定菜单类别
        mContext = context;
        downloadManagerAdapter = adapter;
        this.listView = listView;
        setRemoveWindow(context);
        distanceY = (int) mContext.getResources().getDimension(R.dimen.bottom_tab_height);
    }

    public boolean isRemoveMode() {//判定当前菜单状态是否为删除模式
        if (downloadManagerAdapter != null) {
            return downloadManagerAdapter.isRemoveMode();
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

    public void setOnMenuStateListener(OnMenuStateListener shownListener) {//指定菜单状态监听回调对象
        menuStateListener = shownListener;
    }

    public void setCheckPosition(int position) {//点击选中position,已自动判定全选或非全选状态
        if (downloadManagerAdapter != null) {
            downloadManagerAdapter.setChecked(position);
            downloadManagerAdapter.notifyDataSetChanged();
            setDeleteNum();

            if (menuStateListener != null) {
                menuStateListener.onAllCheckedStateChanged(isAllChecked());
            }

        }
    }

    public boolean isAllChecked() {
        return downloadManagerAdapter.getCheckedSize() == downloadManagerAdapter.getBookCount();
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
        if (downloadManagerAdapter != null) {
            downloadManagerAdapter.setAllChecked(checkedAll);
            downloadManagerAdapter.notifyDataSetChanged();
            setDeleteNum();
        }
        if (menuStateListener != null) {
            menuStateListener.onAllCheckedStateChanged(checkedAll);
        }
    }

    private void setRemoveWindow(Context context) {//实例化菜单对象

        View baseView = LayoutInflater.from(context).inflate(R.layout.popup_download_manager_bottom_editor, null);
        popupWindow = new PopupWindow(baseView, WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setAnimationStyle(R.style.remove_menu_anim_style);
        btnDelete = (Button) baseView.findViewById(R.id.btn_delete);
        btnCancel = (Button) baseView.findViewById(R.id.btn_cancel);
        ViewGroup layout = (ViewGroup) baseView.findViewById(R.id.rl_remove);

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

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteClickListener != null) {
                    deleteClickListener.onMenuDelete(downloadManagerAdapter.remove_checked_states);
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartLogClickUtil.upLoadEventLog(BaseBookApplication.getGlobalContext(),
                        StartLogClickUtil.SHELFEDIT_PAGE, StartLogClickUtil.CANCLE1);
                dismissRemoveMenu();
            }
        });
    }

    public void onShowing(final boolean isShowing) {//菜单打开及关闭状态
        if (downloadManagerAdapter != null) {
            downloadManagerAdapter.setRemoveMode(isShowing);
            setListPadding(listView, isShowing);
            downloadManagerAdapter.resetRemovedState();
            setDeleteNum();
        }
        if (menuStateListener != null) {
            menuStateListener.onAllCheckedStateChanged(isAllChecked());
        }
        if (isShowing) {
            if (menuStateListener != null) {
                menuStateListener.onMenuStateChanged(true);
            }
        } else {
            ExtensionsKt.uiThread(mContext, new Function1<Object, Unit>() {
                @Override
                public Unit invoke(Object o) {
                    if (menuStateListener != null) {
                        menuStateListener.onMenuStateChanged(false);
                    }
                    if (downloadManagerAdapter != null) {
                        downloadManagerAdapter.setRemoveMode(false);
                        downloadManagerAdapter.notifyDataSetChanged();
                    }
                    return null;
                }
            });
        }
    }

    private void setListPadding(ListView listView, boolean isShowing) {
        if (isShowing) {
            int height = distanceY;
            listView.setPadding(0, 0, 0, height);
        } else {
            listView.setPadding(0, 0, 0, 0);
        }
    }

    private void setDeleteNum() {
        if (downloadManagerAdapter != null && btnDelete != null && btnCancel != null) {
            int num = downloadManagerAdapter.getCheckedSize();
            if (num == 0) {
                btnDelete.setText(mContext.getString(R.string.delete));
                btnDelete.setEnabled(false);
            } else {
                String text = mContext.getString(R.string.delete) + "(" + num + ")";
                btnDelete.setText(text);
                btnDelete.setEnabled(true);
            }
        }
    }

    public interface OnMenuStateListener {

        void onMenuStateChanged(boolean isShown);//菜单开启状态

        void onAllCheckedStateChanged(boolean isAllChecked);//菜单全选状态

    }

    public interface OnMenuDeleteClickListener {//菜单删除按钮监听

        void onMenuDelete(ArrayList<Book> books);//删除按钮回调，得到被选中的position
    }

}
