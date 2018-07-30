package com.intelligent.reader.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.intelligent.reader.R;

import net.lzbook.kit.utils.AppUtils;

import java.util.List;

/**
 * @author lijun Lee
 * @desc 底部弹出框
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/3/28 17:17
 */

public class BottomDialog {

    private BottomDialog.Builder builder;

    private Context mContext;

    private Dialog mDialog;

    private BottomDialog() {
    }

    private BottomDialog(BottomDialog.Builder builder) {
        this.builder = builder;
        this.mContext = builder.context;
        this.mDialog = builder.dialog;
    }


    public static Builder newBuilder(Context context) {
        return new Builder(context);
    }

    public void show() {
        mDialog.show();
    }

    public void dismiss() {
        mDialog.dismiss();
    }

    public static final class Builder {

        Context context;

        OnMenuClickListener onMenuClickListener;

        Dialog dialog;

        List<MenuItem> menuItemList;

        LinearLayout menuLl;

        private Builder(Context context) {
            this.context = context;
        }

        public BottomDialog.Builder setCancelable(boolean cancel) {
            dialog.setCancelable(cancel);
            return this;
        }

        public BottomDialog.Builder setCanceledOnTouchOutside(boolean cancel) {
            dialog.setCanceledOnTouchOutside(cancel);
            return this;
        }

        public BottomDialog.Builder addMenu(List<MenuItem> menuItemList) {
            this.menuItemList = menuItemList;
            return this;
        }

        public BottomDialog.Builder setOnMenuClickListener(OnMenuClickListener onMenuClickListener) {
            this.onMenuClickListener = onMenuClickListener;
            return this;
        }

        public BottomDialog build() {
            View bottomLayout = LayoutInflater.from(context).inflate(R.layout.dialog_bottom_layout, null);
            dialog = new Dialog(context, R.style.update_dialog);
            TextView cancelTv = (TextView) bottomLayout.findViewById(R.id.cancel_tv);
            LinearLayout menuLl = (LinearLayout) bottomLayout.findViewById(R.id.menu_ll);
            cancelTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            generateMenu(menuItemList, menuLl, onMenuClickListener);

            dialog.setContentView(bottomLayout);
            Window window = dialog.getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.BOTTOM;
            window.setAttributes(params);
            return new BottomDialog(this);
        }

        private void generateMenu(List<MenuItem> menuItemList, LinearLayout menuLl, final OnMenuClickListener onMenuClickListener) {
            if (menuItemList == null) {
                return;
            }

            this.menuLl = menuLl;

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            for (int i = 0; i < menuItemList.size(); i++) {
                TextView menuTv = new TextView(context);
                View divider = new View(context);
                divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AppUtils.dip2px(context, 0.5f)));
                divider.setBackgroundResource(R.color.dialog_item_divider_color);
                menuTv.setLayoutParams(layoutParams);
                menuTv.setText(menuItemList.get(i).getTitle());
                menuTv.setTextColor(menuItemList.get(i).getColor());
                menuTv.setGravity(Gravity.CENTER);
                menuTv.setBackgroundResource(R.drawable.bottom_dialog_btn_bg_selector);
                menuTv.setPadding(0, AppUtils.dip2px(context, 15), 0, AppUtils.dip2px(context, 15));
                menuTv.setTextSize(16);
                menuTv.setId(i + 1);
                menuTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onMenuClickListener != null) {
                            onMenuClickListener.onMenuClick(v.getId());
                        }
                    }
                });
                menuLl.addView(menuTv);
                menuLl.addView(divider);
            }
        }

    }

    public void changeItemTextColor(int color, int position) {
        TextView txt = (TextView) builder.menuLl.findViewById(position + 1);
        txt.setTextColor(color);
    }

    public interface OnMenuClickListener {
        void onMenuClick(int position);
    }

}
