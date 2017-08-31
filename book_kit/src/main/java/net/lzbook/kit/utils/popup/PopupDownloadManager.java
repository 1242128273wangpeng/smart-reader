package net.lzbook.kit.utils.popup;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import net.lzbook.kit.R;

/**
 * Created by Administrator on 2016/12/15 0015.
 */
public class PopupDownloadManager extends PopupBase implements View.OnClickListener {

    public Button btn_selectAll;
    protected PopupSelectALLClickListener selectAlllickListener;
    View baseView;
    public boolean hasSelectedAll;

    public PopupDownloadManager(Context context) {
        super(context);
        hasSelectedAll = false;
    }

    @Override
    public void showPopupWindow(View parent) {
        super.showPopupWindow(parent);
        hasSelectedAll = false;
        btn_selectAll.setText("全选");
    }

    @Override
    public void dismissPop() {
        super.dismissPop();
        if (onShowingListener != null) {
            onShowingListener.onShowing(false);
        }
    }

    @Override
    protected void initView(View baseView) {
        this.baseView = baseView;
        popupWindow = new PopupWindow(baseView, WindowManager.LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(R.dimen.dimen_view_height_default));
        popupWindow.setAnimationStyle(R.style.remove_menu_anim_style);
//        LinearLayout layout = (LinearLayout) baseView.findViewById(R.id.remove_delete_layout);

        btn_selectAll= (Button) baseView.findViewById(R.id.btn_left);
        delete_btn = (Button) baseView.findViewById(R.id.btn_right);

//        layout.setOnKeyListener(new View.OnKeyListener() {
//
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (keyCode == KeyEvent.KEYCODE_MENU) {
//                    if (onShowingListener != null) {
//                        onShowingListener.onShowing(false);
//                    }
//                    if (popupWindow != null) {
//
//                        popupWindow.dismiss();
//                    }
//                    return true;
//                }
//                return false;
//            }
//        });
//
//        layout.setFocusable(true);
//        layout.setFocusableInTouchMode(true);
//        layout.requestFocus();

        delete_btn.setOnClickListener(this);
        btn_selectAll.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_left) {
            if (selectAlllickListener != null) {
                if (hasSelectedAll) {
                    btn_selectAll.setText("全选");
                    selectAlllickListener.selectAll(false);
                    hasSelectedAll = false;
                }else {
                    btn_selectAll.setText("取消全选");
                    selectAlllickListener.selectAll(true);
                    hasSelectedAll = true;
                }
            }

        } else if (i == R.id.btn_right) {
            if (clickListener != null) {
                clickListener.clickDeleteBtn();
            }
        }

    }

    public interface PopupSelectALLClickListener {
        void selectAll(boolean checkedAll);
    }

    public void setPopupSelectALLClickListener(PopupSelectALLClickListener l) {
        this.selectAlllickListener = l;
    }

    @Override
    public void changeText(String num) {
        super.changeText(num);
    }

}
