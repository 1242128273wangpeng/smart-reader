package com.intelligent.reader.read.page;

import com.intelligent.reader.R;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.data.bean.ReadStatus;
import net.lzbook.kit.utils.StatServiceUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AutoReadMenu extends LinearLayout implements OnClickListener {

    private TextView autoread_down;
    private TextView autoread_up;
    private TextView autoread_rate;
    private TextView autoread_label;
    private TextView autoread_stop;
    private ReadStatus readStatus;
    private Context mContext;
    private OnAutoMemuListener autoMemuListener;
    private View view;

    public AutoReadMenu(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public AutoReadMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public AutoReadMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    private void init() {

        view = LayoutInflater.from(mContext).inflate(R.layout.autoread_menu, null);
        autoread_up = (TextView) view.findViewById(R.id.autoread_up);
        autoread_down = (TextView) view.findViewById(R.id.autoread_down);
        autoread_rate = (TextView) view.findViewById(R.id.autoread_rate);
        autoread_stop = (TextView) view.findViewById(R.id.autoread_stop);
        autoread_label = (TextView) view.findViewById(R.id.autoread_label);

        readStatus = BaseBookApplication.getGlobalContext().getReadStatus();
        if (readStatus != null) {
            setRateValue(readStatus.autoReadSpeed());
        }
        initListener();

        addView(view);
    }

    private void initListener() {
        autoread_up.setOnClickListener(this);
        autoread_down.setOnClickListener(this);
        autoread_stop.setOnClickListener(this);
    }

    private void setRateValue(int value) {
        autoread_rate.setText(String.valueOf(value));
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.autoread_up) {
            StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.rb_click_auto_read_speed_up);
            if (autoMemuListener != null) {
                autoMemuListener.speedUp();
                setRateValue(readStatus.autoReadSpeed());
            }

        } else if (i == R.id.autoread_down) {
            StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.rb_click_auto_read_speed_down);
            if (autoMemuListener != null) {
                autoMemuListener.speedDown();
                setRateValue(readStatus.autoReadSpeed());
            }

        } else if (i == R.id.autoread_stop) {
            StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.rb_click_auto_read_cancel);
            if (autoMemuListener != null) {
                autoMemuListener.autoStop();
            }

        } else {
        }
    }

    public void setOnAutoMemuListener(OnAutoMemuListener l) {
        this.autoMemuListener = l;
    }

    public void recycleResource() {
        if (this.mContext != null) {
            this.mContext = null;
        }

        if (this.readStatus != null) {
            this.readStatus = null;
        }
    }

    public interface OnAutoMemuListener {
        void speedUp();

        void speedDown();

        void autoStop();
    }
}
