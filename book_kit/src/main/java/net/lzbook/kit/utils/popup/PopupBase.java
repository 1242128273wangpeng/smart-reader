package net.lzbook.kit.utils.popup;

import net.lzbook.kit.R;

import android.content.Context;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;


/**
 * 产生书架底部popupwindow的子类
 */

public class PopupBase extends PopupWindowManager implements View.OnClickListener {

    protected PopupWindowDeleteClickListener clickListener;

    protected Button delete_btn;

    public PopupBase(Context context) {
        super(context);
    }

    @Override
    public void showPopupWindow(View parent) {
        if (popupWindow != null && popupWindow.isShowing()) {
            if (onShowingListener != null) {
                onShowingListener.onShowing(false);
            }
            popupWindow.dismiss();
        } else {
            if (onShowingListener != null) {
                onShowingListener.onShowing(true);
            }
            if (popupWindow != null && parent != null && !popupWindow.isShowing()) {

                popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
            }
        }
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
        popupWindow = new PopupWindow(baseView, LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(R.dimen.dimen_view_height_50));
        popupWindow.setAnimationStyle(R.style.remove_menu_anim_style);
        delete_btn = (Button) baseView.findViewById(R.id.bookrack_popup_delete);
        LinearLayout layout = (LinearLayout) baseView.findViewById(R.id.remove_menu_base);

        layout.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MENU) {
                    if (onShowingListener != null) {
                        onShowingListener.onShowing(false);
                    }
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

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.bookrack_popup_delete) {
            if (clickListener != null) {
                clickListener.clickDeleteBtn();
            }

        } else {
        }
    }

    public void setPopupWindowDeleteClickListener(PopupWindowDeleteClickListener l) {
        this.clickListener = l;
    }

    @Override
    public void changeText(String text) {
        if (text != null) {
            if ("0".equals(text)) {
                delete_btn.setText("删除");
//                ColorStateList csl =  mContext.getResources().getColorStateList(R.color.color_gray_babfc1);
//                delete_btn.setTextColor(csl);
//                delete_btn.setBackgroundResource(R.drawable.remove_bottom_delete_unclick);
            } else {
                delete_btn.setText("删除 (" + text + ")");
//                ColorStateList draw = mContext.getResources().getColorStateList(R.color.selector_text_default);
//                delete_btn.setTextColor(draw);
//                delete_btn.setBackgroundResource(R.drawable.remove_bottom_delete);
            }
        }
    }

    public void dismissMenu() {
        if (onShowingListener != null) {
            onShowingListener.onShowing(false);
        }
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
    }

    public interface PopupWindowDeleteClickListener {

        void clickDeleteBtn();
    }

}
