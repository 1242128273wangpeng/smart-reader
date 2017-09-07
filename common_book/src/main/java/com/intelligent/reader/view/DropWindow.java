package com.intelligent.reader.view;

import com.intelligent.reader.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;


/**
 * 向下弹窗
 */
public class DropWindow extends PopupWindow {
    private static DropWindow window = null;
    private final float width_roate = 0.45f;
    private final float height_roate = 0.6f;
    private final int bg_color = R.color.transparent_color;
    Context context;
    private View contentView;

    private DropWindow(Context context, int menuViewID) {
        super(context);
        this.context = context;
        initSelectPopUpMenu(menuViewID);
    }

    public DropWindow(Context context, View menu) {
//       super( context, null,R.style.labels_sort_pop_style);
        super(context);
        if (menu != null)// 外部传参菜单对象
        {
            contentView = menu;
        }
        this.context = context;
        initSelectPopUpMenu(-1);
    }

    /**
     * 实例化菜单对象，显示该View
     */
    public static DropWindow DroupMenu(Context context, View menu) {
        window = DropWindow.getInst(context, -1, menu);
        return window;
    }

    /**
     * 实例化菜单对象，显示该View
     */
    public static DropWindow DroupMenu(Context context, int menuid) {
        window = DropWindow.getInst(context, menuid, null);
        return window;
    }

    private static DropWindow getInst(Context c, int menuViewID, View view) {
        DropWindow inst = null;
        synchronized (DropWindow.class) {
            if (inst == null) {
                if (menuViewID != -1 && view == null) {

                    inst = new DropWindow(c, menuViewID);
                } else {
                    inst = new DropWindow(c, view);
                }
            }
        }
        return inst;
    }

    public void showPopupWindow(View root) {
        if (root != null && window != null && contentView.getParent() != null) {
            ((ViewGroup) contentView.getParent()).removeAllViews();
        }
        if (!this.isShowing()) {
            window.showAsDropDown(root);
        }
    }

    public void showPopup(View parent, int x, int y) {
        if (parent != null && window != null && contentView.getParent() != null) {
            ((ViewGroup) contentView.getParent()).removeAllViews();
        }
        if (!this.isShowing())
            window.showAtLocation(parent, Gravity.BOTTOM | Gravity.RIGHT, x, y); /*弹出显示   在指定的位置(parent)  相对于 x / y 轴的坐标 */
    }

    public void dismissPopMenu() {
        if (window != null && window.isShowing()) {
            window.dismiss();
        }
    }

    private void initSelectPopUpMenu(int menuViewID) {
        LayoutInflater mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    /*设置显示menu布局   view子VIEW*/
        if (menuViewID != -1) {
            contentView = mLayoutInflater.inflate(menuViewID, null);
        }
        this.setContentView(contentView);/*设置显示menu布局   view子VIEW*/

        this.setPopMenuLayout(); /*窗口大小*/
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        this.setOutsideTouchable(true); /*设置触摸外面时消失*/
        this.setFocusable(true);
        this.setTouchable(true);/*设置点击menu以外其他地方以及返回键退出*/
        contentView.setFocusableInTouchMode(true);
//        this.setAnimationStyle(R.style.remove_menu_anim_style);
//        ColorDrawable dw = new ColorDrawable(context.getResources().getColor(bg_color));
        this.setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(bg_color)));/*设置背景显示*/

        this.update();

        contentView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if ((keyCode == KeyEvent.KEYCODE_MENU) && (isShowing())) {
                    dismiss();// 这里写明模拟menu的PopupWindow退出就行
                    return true;
                }
                return false;
            }
        });

    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")

    private void setPopMenuLayout() {
        int width = 0;
        int height = 0;
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);

        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        if (Build.VERSION.SDK_INT > 13) {
            display.getSize(point);
            width = point.x;
            height = point.y;
        } else {
            width = display.getWidth();
            height = display.getHeight();
        }
        this.setWidth((int) (width * width_roate));
        this.setHeight((int) (height * height_roate));

    }

    @SuppressLint("NewApi")
    public void setPopMenuLayout(float width_roate, float height_roate) {
        int width = 0;
        int height = 0;
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);

        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        if (Build.VERSION.SDK_INT > 13) {
            display.getSize(point);
            width = point.x;
            height = point.y;
        } else {
            width = display.getWidth();
            height = display.getHeight();
        }
        if (width_roate != 0) {
            this.setWidth((int) (width * width_roate));
        } else {
            this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        if (height_roate != 0) {

            this.setHeight((int) (height * height_roate));
        } else {
            this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        }

    }
}
