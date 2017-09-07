package net.lzbook.kit.utils;

import net.lzbook.kit.R;
import net.lzbook.kit.book.adapter.RemoveModeAdapter;
import net.lzbook.kit.utils.popup.PopupBase;
import net.lzbook.kit.utils.popup.PopupBookCollect;
import net.lzbook.kit.utils.popup.PopupDeleteCancle;
import net.lzbook.kit.utils.popup.PopupDownloadManager;
import net.lzbook.kit.utils.popup.PopupFactory;
import net.lzbook.kit.utils.popup.PopupWindowInterface;
import net.lzbook.kit.utils.popup.PopupWindowManager;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;

import java.util.HashSet;

/**
 * RemoveMode Helper
 */
public class RemoveAdapterHelper implements PopupBase.PopupWindowDeleteClickListener, PopupWindowManager.PopupWindowOnShowingListener, PopupBookCollect.PopupCollectClickListener, PopupDeleteCancle.PopupCancleclickListener, PopupDownloadManager.PopupSelectALLClickListener {
    public static final int popup_type_base = 0;
    public static final int popup_type_collect = popup_type_base + 1;
    public static final int popup_type_addBook = popup_type_collect + 1;
    public static final int popup_type_cancle = popup_type_addBook + 1;
    public static final int popup_type_download = popup_type_cancle + 1;
    private final static long DELAY_TIME = 500;
    // ====================================
    // fields
    // =====================================
    protected PopupWindowInterface popupWindowManager;
    protected OnMenuStateListener menuStateListener;
    protected OnMenuDeleteClickListener deleteClickListener;
    protected OnMenuSelectAllListener selectAllListener;
    protected OnMenuAddBookClickListener addBookClickListener;
    protected OnMenuCollectClickListener collectClickListener;
    protected RemoveModeAdapter removeModeAdapter;
    protected ListView listview;
    protected View clickAllView, clickCancleView;
    String TAG = "RemoveAdapterHelper";
    boolean isAllChecked;
    Handler handler = new Handler();

    public RemoveAdapterHelper(Context context, RemoveModeAdapter adapter, int type) {//显示指定菜单类别

        removeModeAdapter = adapter;
        setRemoveWindow(context, type);
    }
    // =========================================
    // state
    // =======================================

    public boolean isRemoveMode() {//判定当前菜单状态是否为删除模式
        if (removeModeAdapter != null) {
            return removeModeAdapter.isRemoveMode();
        }
        return false;
    }

    public void setListView(ListView view) {//指定使用菜单的ListView
        this.listview = view;
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
        if (removeModeAdapter != null) {
            removeModeAdapter.setChecked(position);
            removeModeAdapter.notifyDataSetChanged();
            setSelectNum();
            if (isAllChecked) {//全选状态中，选中任意position,取消全选状态
                isAllChecked = false;
            } else {//非全选状态中，如果已经全部选中完成，则进入全选状态
                //FIXME
                if (removeModeAdapter.getCheckedSize() == removeModeAdapter.getCount()) {
                    isAllChecked = true;
                } else {
                    isAllChecked = false;
                }
            }
            //改变全选按钮的显示
            if (popupWindowManager instanceof PopupDownloadManager) {
                if (isAllChecked) {
                    ((PopupDownloadManager) popupWindowManager).hasSelectedAll = true;
                    ((PopupDownloadManager) popupWindowManager).btn_selectAll.setText("取消全选");
                } else {
                    ((PopupDownloadManager) popupWindowManager).hasSelectedAll = false;
                    ((PopupDownloadManager) popupWindowManager).btn_selectAll.setText("全选");
                }
            }

            if (menuStateListener != null) {
                menuStateListener.getAllCheckedState(isAllChecked);
            }

        }
    }


    /**
     * use to dissmiss the remove window
     */
    public boolean dismissRemoveMenu() {//显示关闭菜单
        if (popupWindowManager != null && popupWindowManager.isShowing()) {
//            popupWindowManager.dismissPop();
            ((PopupBase) popupWindowManager).dismissMenu();
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
        if (parent != null && popupWindowManager != null) {
            popupWindowManager.showPopupWindow(parent);
        }
    }

    @Override
    public void selectAll(boolean checkedAll) {
        if (removeModeAdapter != null) {
            removeModeAdapter.setAllChecked(checkedAll);
            removeModeAdapter.notifyDataSetChanged();
            setSelectNum();
        }
        selectAllListener.onSelectAll();
    }

    // =================================================================
    // menu method
    // do not touch
    // =================================================================
    private void setRemoveWindow(Context context, int type) {//实例化对应类别的菜单对象
        switch (type) {
            case popup_type_collect:
                popupWindowManager = new PopupFactory().getPopupWindow(context, PopupFactory.BOOKCOLLECT_BOTTOM);
                if (popupWindowManager != null) {

                    popupWindowManager.initPopupWindow(R.layout.remove_menu_popup_collect, false);

                    ((PopupBookCollect) popupWindowManager).setPopupCollectClickListener(this);
                }
                break;
            case popup_type_cancle:
                popupWindowManager = new PopupFactory().getPopupWindow(context, PopupFactory.DELETE_CANCLE);
                if (popupWindowManager != null) {
                    popupWindowManager.initPopupWindow(R.layout.remove_menu_popup_collect, false);
//                    popupWindowManager.initPopupWindow(R.layout.remove_menu_popup_cancle, false);
                    ((PopupDeleteCancle) popupWindowManager).setPopupCancleClickListener(this);
                }
                break;
            case popup_type_download:
                popupWindowManager = new PopupFactory().getPopupWindow(context, PopupFactory.DOWNLOAD_BOTTOM);
                if (popupWindowManager != null) {
                    popupWindowManager.initPopupWindow(R.layout.download_manager_bottom, false);
                    ((PopupDownloadManager) popupWindowManager).setPopupSelectALLClickListener(this);
                }
                break;
        }
        ((PopupBase) popupWindowManager).setPopupWindowDeleteClickListener(this);
        ((PopupBase) popupWindowManager).setPopupWindowOnShowingListener(this);

    }

    @Override
    public void onShowing(final boolean isShowing) {//菜单打开及关闭状态
        if (removeModeAdapter != null) {
            removeModeAdapter.resetRemovedState();
            setSelectNum();
            isAllChecked = false;
        }
        if (menuStateListener != null) {
            menuStateListener.getAllCheckedState(isAllChecked);
        }
        if (isShowing) {
            if (menuStateListener != null) {
                menuStateListener.getMenuShownState(isShowing);
            }
            if (removeModeAdapter != null) {

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        removeModeAdapter.setRemoveMode(isShowing);
                        removeModeAdapter.setListPadding(listview, isShowing);
                        removeModeAdapter.notifyDataSetChanged();
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
                    if (removeModeAdapter != null) {
                        removeModeAdapter.setRemoveMode(isShowing);
                        removeModeAdapter.setListPadding(listview, isShowing);
                        removeModeAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    @Override
    public void clickDeleteBtn() {//删除
        if (deleteClickListener != null) {
            deleteClickListener.onMenuDelete(removeModeAdapter.remove_checked_states);
        }
    }

    @Override
    public void clickCollect(View baseView) {//收藏  取消
        if (collectClickListener != null)

            collectClickListener.onMenuColleck(baseView, removeModeAdapter.remove_checked_states);
    }

    @Override
    public void clickCancle(View collectView) {
        if (removeModeAdapter != null) {
            removeModeAdapter.setRemoveMode(false);
            removeModeAdapter.resetRemovedState();
            removeModeAdapter.notifyDataSetChanged();
            setSelectNum();
            isAllChecked = false;
        }
        if (menuStateListener != null) {
            menuStateListener.getAllCheckedState(isAllChecked);
        }
        dismissRemoveMenu();
    }

    private void setSelectNum() {//改变菜单显示所选定总数
        if (removeModeAdapter != null && popupWindowManager != null) {
            int num = removeModeAdapter.getCheckedSize();
            AppLog.d(TAG, "setSelectNum " + num);
            popupWindowManager.changeText(String.valueOf(num));
        }
    }

    // ====================================================
    // callback
    // ===================================================
    public interface OnMenuStateListener {//菜单状态监听

        public void getMenuShownState(boolean isShown);//菜单开启状态

        public void getAllCheckedState(boolean isAll);//菜单全选状态

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

    public interface OnMenuAddBookClickListener {//菜单加入书架监听

        public void onMenuAddBook(HashSet<Integer> checked_state);//加入数据按钮回调，得到被选中的position
    }

    public interface OnMenuCollectClickListener {//菜单收藏监听

        public void onMenuColleck(View baseView, HashSet<Integer> checked_state);//加入收藏回调
    }

}