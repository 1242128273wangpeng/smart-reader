package net.lzbook.kit.utils;

import net.lzbook.kit.R;
import net.lzbook.kit.book.adapter.RemoveModeAdapter;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.utils.popup.PopupBase;
import net.lzbook.kit.utils.popup.PopupBase.PopupWindowDeleteClickListener;
import net.lzbook.kit.utils.popup.PopupBookCollect;
import net.lzbook.kit.utils.popup.PopupBookCollect.PopupCollectClickListener;
import net.lzbook.kit.utils.popup.PopupDeleteCancle;
import net.lzbook.kit.utils.popup.PopupDeleteCancle.PopupCancleclickListener;
import net.lzbook.kit.utils.popup.PopupDownloadManager;
import net.lzbook.kit.utils.popup.PopupDownloadManager.PopupSelectALLClickListener;
import net.lzbook.kit.utils.popup.PopupFactory;
import net.lzbook.kit.utils.popup.PopupWindowInterface;
import net.lzbook.kit.utils.popup.PopupWindowManager.PopupWindowOnShowingListener;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;

import java.util.HashSet;
import java.util.List;

public class RemoveAdapterHelper implements PopupWindowDeleteClickListener, PopupWindowOnShowingListener, PopupCollectClickListener, PopupCancleclickListener, PopupSelectALLClickListener {
    private static final long DELAY_TIME = 500;
    public static final int popup_type_addBook = 2;
    public static final int popup_type_base = 0;
    public static final int popup_type_cancle = 3;
    public static final int popup_type_collect = 1;
    public static final int popup_type_download = 4;
    String TAG = "RemoveAdapterHelper";
    protected OnMenuAddBookClickListener addBookClickListener;
    protected View clickAllView;
    protected View clickCancleView;
    protected OnMenuCollectClickListener collectClickListener;
    protected OnMenuDeleteClickListener deleteClickListener;
    Handler handler = new Handler();
    boolean isAllChecked;
    protected ListView listview;
    protected OnMenuStateListener menuStateListener;
    protected PopupWindowInterface popupWindowManager;
    protected RemoveModeAdapter removeModeAdapter;
    protected OnMenuSelectAllListener selectAllListener;

    public interface OnMenuAddBookClickListener {
        void onMenuAddBook(HashSet<Integer> hashSet);
    }

    public interface OnMenuCollectClickListener {
        void onMenuColleck(View view, List<Book> list);
    }

    public interface OnMenuDeleteClickListener {
        void onMenuDelete(List<Book> list);
    }

    public interface OnMenuSelectAllListener {
        void onSelectAll(boolean z);
    }

    public interface OnMenuStateListener {
        void getAllCheckedState(boolean z);

        void getMenuShownState(boolean z);
    }

    public RemoveAdapterHelper(Context context, RemoveModeAdapter adapter, int type) {
        this.removeModeAdapter = adapter;
        setRemoveWindow(context, type);
    }

    public boolean isRemoveMode() {
        if (this.removeModeAdapter != null) {
            return this.removeModeAdapter.isRemoveMode();
        }
        return false;
    }

    public void setListView(ListView view) {
        this.listview = view;
    }

    public void setOnMenuDeleteListener(OnMenuDeleteClickListener click) {
        this.deleteClickListener = click;
    }

    public void setOnSelectAllListener(OnMenuSelectAllListener selectAllListener) {
        this.selectAllListener = selectAllListener;
    }

    public void setOnMenuStateListener(OnMenuStateListener shownListener) {
        this.menuStateListener = shownListener;
    }

    public void setCheckPosition(int position) {
        if (this.removeModeAdapter != null) {
            this.removeModeAdapter.setChecked(position);
            this.removeModeAdapter.notifyDataSetChanged();
            setSelectNum();
            if (this.removeModeAdapter.getCheckedSize() != BookDaoHelper.getInstance().getBooksCount()) {
                this.isAllChecked = false;
            } else if (this.removeModeAdapter.getCheckedSize() == BookDaoHelper.getInstance().getBooksCount()) {
                this.isAllChecked = true;
            } else {
                this.isAllChecked = false;
            }
            if (this.popupWindowManager instanceof PopupDownloadManager) {
                if (this.isAllChecked) {
                    ((PopupDownloadManager) this.popupWindowManager).hasSelectedAll = true;
                    ((PopupDownloadManager) this.popupWindowManager).btn_selectAll.setText("取消全选");
                } else {
                    ((PopupDownloadManager) this.popupWindowManager).hasSelectedAll = false;
                    ((PopupDownloadManager) this.popupWindowManager).btn_selectAll.setText("全选");
                }
            }
            if (this.menuStateListener != null) {
                this.menuStateListener.getAllCheckedState(this.isAllChecked);
            }
        }
    }

    public boolean dismissRemoveMenu() {
        if (this.popupWindowManager == null || !this.popupWindowManager.isShowing()) {
            return false;
        }
        ((PopupBase) this.popupWindowManager).dismissMenu();
        return true;
    }

    public void showRemoveMenu(View parent) {
        if (parent != null && this.popupWindowManager != null) {
            this.popupWindowManager.showPopupWindow(parent);
        }
    }

    public void selectAll(boolean checkedAll) {
        if (this.removeModeAdapter != null) {
            this.removeModeAdapter.setAllChecked(checkedAll);
            this.removeModeAdapter.notifyDataSetChanged();
            setSelectNum();
        }
        this.selectAllListener.onSelectAll(checkedAll);
    }

    private void setRemoveWindow(Context context, int type) {
        switch (type) {
            case 1:
                this.popupWindowManager = new PopupFactory().getPopupWindow(context, 4);
                if (this.popupWindowManager != null) {
                    this.popupWindowManager.initPopupWindow(R.layout.remove_menu_popup_collect, false);
                    ((PopupBookCollect) this.popupWindowManager).setPopupCollectClickListener(this);
                    break;
                }
                break;
            case 3:
                this.popupWindowManager = new PopupFactory().getPopupWindow(context, 5);
                if (this.popupWindowManager != null) {
                    this.popupWindowManager.initPopupWindow(R.layout.remove_menu_popup_collect, false);
                    ((PopupDeleteCancle) this.popupWindowManager).setPopupCancleClickListener(this);
                    break;
                }
                break;
            case 4:
                this.popupWindowManager = new PopupFactory().getPopupWindow(context, 1);
                if (this.popupWindowManager != null) {
                    this.popupWindowManager.initPopupWindow(R.layout.download_manager_bottom, false);
                    ((PopupDownloadManager) this.popupWindowManager).setPopupSelectALLClickListener(this);
                    break;
                }
                break;
        }
        ((PopupBase) this.popupWindowManager).setPopupWindowDeleteClickListener(this);
        ((PopupBase) this.popupWindowManager).setPopupWindowOnShowingListener(this);
    }

    @Override
    public void onShowing(final boolean isShowing) {
        if (this.removeModeAdapter != null) {
            this.removeModeAdapter.resetRemovedState();
            setSelectNum();
            this.isAllChecked = false;
        }
        if (this.menuStateListener != null) {
            this.menuStateListener.getAllCheckedState(this.isAllChecked);
        }
        if (isShowing) {
            if (this.menuStateListener != null) {
                this.menuStateListener.getMenuShownState(isShowing);
            }
            if (this.removeModeAdapter != null) {

                RemoveAdapterHelper.this.removeModeAdapter.setRemoveMode(isShowing);
                RemoveAdapterHelper.this.removeModeAdapter.setListPadding(RemoveAdapterHelper.this.listview, isShowing);
                RemoveAdapterHelper.this.removeModeAdapter.notifyDataSetChanged();

                return;
            }
            return;
        }
        if (this.menuStateListener != null) {
            RemoveAdapterHelper.this.menuStateListener.getMenuShownState(isShowing);

        }
        if (RemoveAdapterHelper.this.removeModeAdapter != null) {
            RemoveAdapterHelper.this.removeModeAdapter.setRemoveMode(isShowing);
            RemoveAdapterHelper.this.removeModeAdapter.setListPadding(RemoveAdapterHelper.this.listview, isShowing);
            RemoveAdapterHelper.this.removeModeAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void clickDeleteBtn() {
        if (this.deleteClickListener != null) {
            this.deleteClickListener.onMenuDelete(this.removeModeAdapter.remove_checked_states);
        }
    }

    @Override
    public void clickCollect(View baseView) {
        if (this.collectClickListener != null) {
            this.collectClickListener.onMenuColleck(baseView, this.removeModeAdapter.remove_checked_states);
        }
    }

    public void clickCancle(View collectView) {
        if (this.removeModeAdapter != null) {
            this.removeModeAdapter.setRemoveMode(false);
            this.removeModeAdapter.resetRemovedState();
            this.removeModeAdapter.notifyDataSetChanged();
            setSelectNum();
            this.isAllChecked = false;
        }
        if (this.menuStateListener != null) {
            this.menuStateListener.getAllCheckedState(this.isAllChecked);
        }
        dismissRemoveMenu();
    }

    private void setSelectNum() {
        if (this.removeModeAdapter != null && this.popupWindowManager != null) {
            int num = Math.min(this.removeModeAdapter.getCheckedSize(), BookDaoHelper.getInstance().getBooksCount());
            AppLog.d(this.TAG, "setSelectNum " + num);
            this.popupWindowManager.changeText(String.valueOf(num));
        }
    }
}
