package com.intelligent.reader.widget.drawer;

import com.intelligent.reader.R;
import com.intelligent.reader.adapter.SourceAdapter;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.data.bean.Source;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lijun Lee
 * @desc 确认窗口
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/2/28 15:37
 */

public class ConfirmPopWindow extends PopupWindow {

    private View mPopInflater;

    private ConfirmPopWindow(View inflate, int matchParent, int wrapContent, ConfirmPopWindow.Builder builder) {
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

        public ConfirmPopWindow.Builder setOnConfirmListener(OnConfirmListener onConfirmListener) {
            this.onConfirmListener = onConfirmListener;
            return this;
        }

        public ConfirmPopWindow.Builder title(String title) {
            this.title = title;
            return this;
        }

        public ConfirmPopWindow.Builder content(String content) {
            this.content = content;
            return this;
        }

        public ConfirmPopWindow.Builder confirmButtonName(String confirmButtonName) {
            this.confirmButtonName = confirmButtonName;
            return this;
        }

        public ConfirmPopWindow.Builder cancelButtonName(String cancelButtonName) {
            this.cancelButtonName = cancelButtonName;
            return this;
        }

        public ConfirmPopWindow build() {
            View popupView = LayoutInflater.from(context).inflate(R.layout.pop_confirm_layout, null);
            this.popInflater = popupView;
            final ConfirmPopWindow popupWindow = new ConfirmPopWindow(popupView,
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
