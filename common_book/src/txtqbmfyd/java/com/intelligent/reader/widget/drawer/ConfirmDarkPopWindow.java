package com.intelligent.reader.widget.drawer;

import com.intelligent.reader.R;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * @author lijun Lee
 * @desc 确认窗口 (黑)
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/2/28 15:37
 */

public class ConfirmDarkPopWindow extends PopupWindow {

    private View mPopInflater;

    private ConfirmDarkPopWindow(View inflate, int matchParent, int wrapContent, ConfirmDarkPopWindow.Builder builder) {
        super(inflate, matchParent, wrapContent);
        mPopInflater = builder.popInflater;
    }

    public static Builder newBuilder(Context context) {
        return new Builder(context);
    }

    public void show() {
        showAtLocation(mPopInflater, Gravity.BOTTOM, 0, 0);
    }

    public static final class Builder {

        Context context;

        View popInflater;

        String title;

        String content;

        String confirmButtonName;

        String cancelButtonName;

        OnConfirmListener onConfirmListener;

        private Builder(Context context) {
            this.context = context;
        }

        public ConfirmDarkPopWindow.Builder setOnConfirmListener(OnConfirmListener onConfirmListener) {
            this.onConfirmListener = onConfirmListener;
            return this;
        }

        public ConfirmDarkPopWindow.Builder title(String title) {
            this.title = title;
            return this;
        }

        public ConfirmDarkPopWindow.Builder content(String content) {
            this.content = content;
            return this;
        }

        public ConfirmDarkPopWindow.Builder confirmButtonName(String confirmButtonName) {
            this.confirmButtonName = confirmButtonName;
            return this;
        }

        public ConfirmDarkPopWindow.Builder cancelButtonName(String cancelButtonName) {
            this.cancelButtonName = cancelButtonName;
            return this;
        }

        public ConfirmDarkPopWindow build() {
            View popupView = LayoutInflater.from(context).inflate(R.layout.pop_confirm_dark_layout, null);
            this.popInflater = popupView;
            final ConfirmDarkPopWindow popupWindow = new ConfirmDarkPopWindow(popupView,
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, this);
            popupWindow.setFocusable(true);
            popupWindow.setTouchable(true);
            popupWindow.setOutsideTouchable(false);

            TextView titleTv = (TextView) popInflater.findViewById(R.id.dialog_title);
            if (!TextUtils.isEmpty(title)) {
                titleTv.setText(title);
            }

            TextView contentTv = (TextView) popInflater.findViewById(R.id.publish_content);
            if (!TextUtils.isEmpty(content)) {
                contentTv.setText(content);
            }

            Button okBt = (Button) popInflater.findViewById(R.id.okBt);
            if (!TextUtils.isEmpty(confirmButtonName)) {
                okBt.setText(confirmButtonName);
            }

            Button cancelBt = (Button) popInflater.findViewById(R.id.cancelBt);
            if (!TextUtils.isEmpty(cancelButtonName)) {
                cancelBt.setText(cancelButtonName);
            }

            okBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onConfirmListener != null) {
                        onConfirmListener.onConfirm(v);
                    }
                    popupWindow.dismiss();
                }
            });

            cancelBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onConfirmListener != null) {
                        onConfirmListener.onCancel(v);
                    }
                    popupWindow.dismiss();
                }
            });

            return popupWindow;
        }
    }

    public interface OnConfirmListener {
        void onConfirm(View view);

        void onCancel(View view);
    }
}
