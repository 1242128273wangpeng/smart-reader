package com.dy.reader.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.dingyue.contract.util.CommonUtil;
import com.dy.reader.R;

import net.lzbook.kit.book.view.NightShadowView;
import net.lzbook.kit.utils.StatServiceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lijun Lee
 * @desc 问题反馈窗口
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/3/1 11:28
 */

public class FeedbackPopWindow {

//    private View mPopInflater;

    private Dialog mDialog;

    private FeedbackPopWindow(FeedbackPopWindow.Builder builder) {
//        super(inflate, matchParent, wrapContent);
//        mPopInflater = builder.popInflater;
        mDialog = builder.dialog;
    }

    public static FeedbackPopWindow.Builder newBuilder(Context context) {
        return new FeedbackPopWindow.Builder(context);
    }

    public void show() {
        mDialog.show();
    }

    public static final class Builder {

        Context context;

        OnSubmitClickListener onSubmitClickListener;

        View popInflater;

        Dialog dialog;

        private Builder(Context context) {
            this.context = context;
        }

        public FeedbackPopWindow.Builder setOnSubmitClickListener(OnSubmitClickListener onSubmitClickListener) {
            this.onSubmitClickListener = onSubmitClickListener;
            return this;
        }

        public FeedbackPopWindow build() {
            final View popupView = LayoutInflater.from(context).inflate(R.layout.pop_feedback_layout, null);
            this.popInflater = popupView;

            final Dialog dialog = new Dialog(context,R.style.update_dialog);
            dialog.setContentView(popInflater);
            Window window = dialog.getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.BOTTOM;
            window.setAttributes(params);
            dialog.setCanceledOnTouchOutside(true);
            this.dialog = dialog;

            final NightShadowView nightShadowView = (NightShadowView) popInflater.findViewById(R.id.nightShadowView);
            final FrameLayout container = (FrameLayout) popInflater.findViewById(R.id.container);
            container.post(new Runnable() {
                @Override
                public void run() {
                    nightShadowView.getLayoutParams().height = container.getHeight();
                    popupView.requestLayout();
                }
            });
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
                        CommonUtil.showToastMessage("请选择错误类型");
                    } else {
                        if (onSubmitClickListener != null) {
                            onSubmitClickListener.onSubmit(type);
                        }
                        dialog.dismiss();
                    }
                }
            });

            TextView cancelButton = (TextView) popInflater.findViewById(R.id.feedback_cancel);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            FeedbackPopWindow popupWindow = new FeedbackPopWindow(this);
            return popupWindow;
        }
    }

    public interface OnSubmitClickListener {
        void onSubmit(int type);
    }
}
