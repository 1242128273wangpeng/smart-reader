package com.intelligent.reader.read.page;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.intelligent.reader.read.util.ReadQueryUtil;

import net.lzbook.kit.data.bean.ReadConfig;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class BatteryView extends ImageView {

    static Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    ;

    Bitmap mBitmap;

    int mBitmapWidth = 0;
    int mBitmapHeight = 0;
    private int left;

    private static int top;

    private static float percent;

    private static float right;
    private static float bottom;

    private static boolean isChecked = false;

    private Resources resources;

    public BatteryView(Context context) {
        super(context);

        resources = context.getResources();
        getRect();
    }

    public BatteryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        resources = context.getResources();
        getRect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int color_int = ReadQueryUtil.INSTANCE.getColor(resources);
        mPaint.setColor(color_int);
        canvas.drawRect(left + 1, getPaddingTop() + top + 1, (right - (left + 1)) * percent + (left + 1),
                getPaddingTop() + bottom, mPaint);
    }

    private void getRect() {
        if (!isChecked) {
            isChecked = true;
            mBitmap = ((BitmapDrawable) getDrawable()).getBitmap();
            mBitmapWidth = mBitmap.getWidth();
            mBitmapHeight = mBitmap.getHeight();
            for (int i = 0; i < mBitmapWidth; i++) {
                int color = mBitmap.getPixel(i, mBitmapHeight / 2);
                if (color == 0) {
                    this.left = i;
                    break;
                }
            }
            for (int i = mBitmapWidth - 1; i >= 0; i--) {
                int color = mBitmap.getPixel(i, mBitmapHeight / 2);
                if (color == 0) {
                    this.right = i;
                    break;
                }
            }
            for (int i = 0; i < mBitmapHeight; i++) {
                int color = mBitmap.getPixel(mBitmapWidth / 2, i);
                if (color == 0) {
                    this.top = i;
                    break;
                }
            }
            for (int i = mBitmapHeight - 1; i >= 0; i--) {
                int color = mBitmap.getPixel(mBitmapWidth / 2, i);
                if (color == 0) {
                    this.bottom = i;
                    break;
                }
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        post(new Runnable() {
            @Override
            public void run() {
                if (mBatInfoReceiver == null) {
                    mBatInfoReceiver = new BatteryReceiver();
                    try {
                        getContext().registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                mBatInfoReceiver.listeners.add(BatteryView.this);
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        post(new Runnable() {
            @Override
            public void run() {
                if (mBatInfoReceiver != null && !mBatInfoReceiver.listeners.isEmpty()) {
                    mBatInfoReceiver.listeners.remove(BatteryView.this);
                    if (mBatInfoReceiver.listeners.isEmpty()) {
                        try {
                            getContext().unregisterReceiver(mBatInfoReceiver);
                            mBatInfoReceiver = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        });
    }

    /**
     * 接受电量改变广播
     */
    private static BatteryReceiver mBatInfoReceiver = null;

    static class BatteryReceiver extends BroadcastReceiver {
        public HashSet<BatteryView> listeners = new HashSet<>();

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == Intent.ACTION_BATTERY_CHANGED) {
                float level = intent.getIntExtra("level", 0);
                float scale = intent.getIntExtra("scale", 100);
                percent = level / scale;
                for (BatteryView batteryView : listeners) {
                    batteryView.postInvalidate();
//                    batteryView.destroyDrawingCache();
                }
            }
        }
    }
}