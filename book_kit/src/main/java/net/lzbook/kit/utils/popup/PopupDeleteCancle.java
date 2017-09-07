package net.lzbook.kit.utils.popup;

import net.lzbook.kit.R;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;


/**
 * 书架用弹窗
 */
public class PopupDeleteCancle extends PopupBase implements View.OnClickListener {
    protected Button btn_cancle;
    protected PopupCancleclickListener cancleclickListener;
    View baseView;

    public PopupDeleteCancle(Context context) {
        super(context);
    }

    @Override
    public void showPopupWindow(View parent) {
        super.showPopupWindow(parent);
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
//        super.initView(baseView);
        this.baseView = baseView;
        popupWindow = new PopupWindow(baseView, WindowManager.LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(R.dimen.dimen_view_height_default));
        popupWindow.setAnimationStyle(R.style.remove_menu_anim_style);
        LinearLayout layout = (LinearLayout) baseView.findViewById(R.id.remove_delete_layout);

        delete_btn = (Button) baseView.findViewById(R.id.btn_left);
        btn_cancle = (Button) baseView.findViewById(R.id.btn_right);

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
        btn_cancle.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_left) {
            if (clickListener != null) {
                clickListener.clickDeleteBtn();
            }

        } else if (i == R.id.btn_right) {
            if (cancleclickListener != null) {
                cancleclickListener.clickCancle(baseView);
            }

        } else {
        }
    }

    public void setPopupCancleClickListener(PopupCancleclickListener l) {
        this.cancleclickListener = l;
    }

    // 显示删除个数
    @Override
    public void changeText(String text) {
        super.changeText(text);
    }

    public interface PopupCancleclickListener {
        void clickCancle(View collectView);
    }
}
