package com.intelligent.reader.read.page;

import com.intelligent.reader.R;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.utils.StatServiceUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dodo
 */
public class AutoReadMenu extends LinearLayout implements OnClickListener {

    private TextView autoread_down;
    private TextView autoread_up;
    private TextView autoread_rate;
    private TextView autoread_label;
    private TextView autoread_stop;
    private Context mContext;
    private OnAutoMemuListener autoMemuListener;

    /**
     * 最小阅读速度
     */
    private final int AUTO_READ_MIN_SPEED = 1;

    /**
     * 最大阅读速度
     */
    private final int AUTO_READ_MAX_SPEED = 20;

    /**
     * 默认阅读速度
     */
    private final int AUTO_READ_DEFAULS_SPEED = 16;

    /**
     * 常量，用于在SP中保存阅读速度时使用
     */
    private final String AUTO_READ_SPEED_KEY = "ars";

    private SharedPreferences mAutoReadSp;

    private int mAutoReadSpeed;

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
        mAutoReadSp = PreferenceManager.getDefaultSharedPreferences(mContext);
        mAutoReadSpeed = mAutoReadSp.getInt(AUTO_READ_SPEED_KEY, AUTO_READ_DEFAULS_SPEED);
        initListener();
    }

    private void initListener() {
        autoread_up.setOnClickListener(this);
        autoread_down.setOnClickListener(this);
        autoread_stop.setOnClickListener(this);
    }

    public void setRateValue() {
        autoread_rate.setText(String.valueOf(mAutoReadSpeed));
    }

    public void setAutoReadSpeed(int speed) {

        if (speed == mAutoReadSpeed || speed < AUTO_READ_MIN_SPEED || speed > AUTO_READ_MAX_SPEED) {
            return;
        }

        mAutoReadSpeed = speed;
        SharedPreferences.Editor editor = mAutoReadSp.edit();
        editor.putInt(AUTO_READ_SPEED_KEY, mAutoReadSpeed);
        editor.apply();
    }

    public double autoReadFactor() {
        if (mAutoReadSpeed == AUTO_READ_DEFAULS_SPEED) {
            return 1;
        }

        double d = mAutoReadSpeed * 1.0 / AUTO_READ_DEFAULS_SPEED;
        if (mAutoReadSpeed < AUTO_READ_DEFAULS_SPEED) {
            return Math.sqrt(d);
        } else {
            return d * d;
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.autoread_up) {
            StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.rb_click_auto_read_speed_up);
            if (autoMemuListener != null) {
                autoMemuListener.setAutoSpeed(autoReadFactor());
                setAutoReadSpeed(mAutoReadSpeed + 1);
                setRateValue();
            }

        } else if (i == R.id.autoread_down) {
            StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.rb_click_auto_read_speed_down);
            if (autoMemuListener != null) {
                autoMemuListener.setAutoSpeed(autoReadFactor());
                setAutoReadSpeed(mAutoReadSpeed - 1);
                setRateValue();
            }

        } else if (i == R.id.autoread_stop) {
            Map<String, String> data = new HashMap<>();
            data.put("type", "2");
            StartLogClickUtil.upLoadEventLog(mContext, StartLogClickUtil.READPAGESET_PAGE, StartLogClickUtil.AUTOREAD, data);
            StatServiceUtils.statAppBtnClick(mContext, StatServiceUtils.rb_click_auto_read_cancel);
            if (autoMemuListener != null) {
                autoMemuListener.autoStop();
                setVisibility(GONE);
            }
        }
    }

    public void setOnAutoMemuListener(OnAutoMemuListener l) {
        this.autoMemuListener = l;
        if (autoMemuListener != null) {
            autoMemuListener.setAutoSpeed(autoReadFactor());
        }
    }

    public void recycleResource() {
        if (this.mContext != null) {
            this.mContext = null;
        }

    }

    public interface OnAutoMemuListener {

        void setAutoSpeed(double autoReadSpeed);

        void autoStop();
    }
}
