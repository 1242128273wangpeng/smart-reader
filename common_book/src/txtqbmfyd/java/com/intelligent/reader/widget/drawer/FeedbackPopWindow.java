package com.intelligent.reader.widget.drawer;

import com.intelligent.reader.R;

import net.lzbook.kit.utils.StatServiceUtils;
import net.lzbook.kit.utils.ToastUtils;

import org.w3c.dom.Text;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lijun Lee
 * @desc 问题反馈窗口
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/3/1 11:28
 */

public class FeedbackPopWindow extends PopupWindow {

    private View mPopInflater;

    private FeedbackPopWindow(View inflate, int matchParent, int wrapContent, FeedbackPopWindow.Builder builder) {
        super(inflate, matchParent, wrapContent);
        mPopInflater = builder.popInflater;
    }

    public static FeedbackPopWindow.Builder newBuilder(Context context) {
        return new FeedbackPopWindow.Builder(context);
    }

    public void show() {
        showAtLocation(mPopInflater, Gravity.BOTTOM, 0, 0);
    }

    public static final class Builder {

        Context context;

        OnSubmitClickListener onSubmitClickListener;

        View popInflater;

        private Builder(Context context) {
            this.context = context;
        }

        public FeedbackPopWindow.Builder setOnSubmitClickListener(OnSubmitClickListener onSubmitClickListener) {
            this.onSubmitClickListener = onSubmitClickListener;
            return this;
        }

        public FeedbackPopWindow build() {
            View popupView = LayoutInflater.from(context).inflate(R.layout.pop_feedback_layout, null);
            this.popInflater = popupView;
            final FeedbackPopWindow popupWindow = new FeedbackPopWindow(popupView,
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, this);
            popupWindow.setFocusable(true);
            popupWindow.setTouchable(true);
            popupWindow.setOutsideTouchable(false);

            LinearLayout checkboxsParent = (LinearLayout) popInflater.findViewById(R.id.feedback_checkboxs_parent);
            final List<CheckBox> checkboxs = new ArrayList<>();
            final List<TextView> texts = new ArrayList<>();
            final List<RelativeLayout> relativeLayouts = new ArrayList<>();
            for (int i = 0; i < checkboxsParent.getChildCount(); i++) {
                RelativeLayout relativeLayout = (RelativeLayout) checkboxsParent.getChildAt(i);
                relativeLayout.setTag(i);
                relativeLayouts.add(relativeLayout);
                for (int j = 0; j < relativeLayout.getChildCount(); j++) {
                    View v = relativeLayout.getChildAt(j);
                    if (v instanceof CheckBox) {
                        checkboxs.add((CheckBox) v);
                    } else if (v instanceof TextView) {
                        texts.add((TextView) v);
                    }
                    v.setTag(i + 1);
                }
            }

            for (RelativeLayout relativeLayout : relativeLayouts) {
                relativeLayout.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                for (CheckBox checkBox : checkboxs) {
                                    checkBox.setChecked(false);
                                }
                                for (TextView textView : texts) {
                                    textView.setTextColor(context.getResources().getColor(R.color.reading_setting_text_color));
                                }
                                checkboxs.get((Integer) v.getTag()).setChecked(true);
                                texts.get((Integer) v.getTag()).setTextColor(context.getResources().getColor(R.color.dialog_recommend));
                            }
                        });
            }

            TextView submitButton = (TextView) popInflater.findViewById(R.id.feedback_submit);
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StatServiceUtils.statAppBtnClick(context.getApplicationContext(), StatServiceUtils.rb_click_feedback_submit);
                    int type = -1;
                    for (CheckBox checkBox : checkboxs) {
                        if (checkBox.isChecked()) {
                            type = (int) checkBox.getTag();
                        }
                    }
                    if (type == -1) {
                        ToastUtils.showToastNoRepeat("请选择错误类型");
                    } else {
                        if (onSubmitClickListener != null) {
                            onSubmitClickListener.onSubmit(type);
                        }
                        popupWindow.dismiss();
                    }
                }
            });

            TextView cancelButton = (TextView) popInflater.findViewById(R.id.feedback_cancel);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                }
            });
            return popupWindow;
        }
    }

    public interface OnSubmitClickListener {
        void onSubmit(int type);
    }
}
