package com.intelligent.reader.read.page;

import com.intelligent.reader.R;
import com.intelligent.reader.app.BookApplication;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
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

import java.util.HashMap;
import java.util.Map;

public class AutoReadMenu extends LinearLayout implements OnClickListener {

    private TextView autoread_down;
    private TextView autoread_up;
    private TextView autoread_rate;
    private TextView autoread_label;
    private TextView autoread_stop;
    private Context mContext;
    private OnAutoMemuListener autoMemuListener;

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
        LayoutInflater.from(mContext).inflate(R.layout.autoread_menu, this);
        autoread_up = (TextView) findViewById(R.id.autoread_up);
        autoread_down = (TextView) findViewById(R.id.autoread_down);
        autoread_rate = (TextView) findViewById(R.id.autoread_rate);
        autoread_stop = (TextView) findViewById(R.id.autoread_stop);
        autoread_label = (TextView) findViewById(R.id.autoread_label);
        initListener();
    }

    private void initListener() {
        autoread_up.setOnClickListener(this);
        autoread_down.setOnClickListener(this);
        autoread_stop.setOnClickListener(this);
    }

    public void setRateValue() {
        autoread_rate.setText(String.valueOf(BaseBookApplication.getGlobalContext().getReadStatus().autoReadSpeed()));
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.autoread_up) {
            StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.rb_click_auto_read_speed_up);
            if (autoMemuListener != null) {
                autoMemuListener.speedUp();
                setRateValue();
            }

        } else if (i == R.id.autoread_down) {
            StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.rb_click_auto_read_speed_down);
            if (autoMemuListener != null) {
                autoMemuListener.speedDown();
                setRateValue();
            }

        } else if (i == R.id.autoread_stop) {
            Map<String, String> data = new HashMap<>();
            data.put("type", "2");
            StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.AUTOREAD, data);
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

    }

    public interface OnAutoMemuListener {
        void speedUp();

        void speedDown();

        void autoStop();
    }
}
