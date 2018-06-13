package com.dy.reader.page;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.dy.reader.helper.AppHelper;
import com.dy.reader.setting.ReaderStatus;
import com.dycm_adsdk.PlatformSDK;
import com.dycm_adsdk.view.NativeView;

import java.util.HashMap;
import java.util.Map;


/**
 * wt改
 * 1、增加广告缓存
 * 2、缓存加载时机：翻页子线程拉取
 * Created by Xian on 2018/1/30.
 */

public class PageAdContainer extends FrameLayout {

    boolean isDown = true;
    float DownX = 0;
    float DownY = 0;
    float moveX = 0;
    float moveY = 0;
    public FrameLayout frameLayout;

    public PageAdContainer(@NonNull Context context) {
        this(context, null);
    }

    public PageAdContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageAdContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            DownX = event.getX();//float DownX
            DownY = event.getY();//float DownY
            moveX = 0;
            moveY = 0;
            return super.dispatchTouchEvent(event);
        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            moveX += Math.abs(event.getX() - DownX);//X轴距离
            moveY += Math.abs(event.getY() - DownY);//y轴距离
            DownX = event.getX();
            DownY = event.getY();
            if ((moveX > 20 || moveY > 20)) {
                if (isDown) {
                    isDown = false;
                    event.setAction(MotionEvent.ACTION_DOWN);
                }
                this.setVisibility(GONE);
                return frameLayout.dispatchTouchEvent(event);
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            if (!isDown) {
                isDown = true;
                return frameLayout.dispatchTouchEvent(event);
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            View glview = frameLayout.getChildAt(0);
            if (glview != null && glview instanceof GLReaderView) {
                if (event.getX() < AppHelper.INSTANCE.getScreenWidth() / 2) {
                    ((GLReaderView) glview).onClickLife();
                } else {
                    ((GLReaderView) glview).onClickRight();
                }
            }
        }
        return true;
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        super.addView(child, params);

        Map<String,String > map = new HashMap<>();
        map.put("book_id", ReaderStatus.INSTANCE.getBook().getBook_id());
        map.put("book_source_id",ReaderStatus.INSTANCE.getBook().getBook_source_id());
        map.put("chapter_id", ReaderStatus.INSTANCE.getChapterId());

        if ("api.qingoo.cn".equalsIgnoreCase(ReaderStatus.INSTANCE.getBook().getHost())
                || "open.qingoo.cn".equalsIgnoreCase(ReaderStatus.INSTANCE.getBook().getHost())) {
            map.put("channel_code", "A001");
        } else {
            map.put("channel_code", "A002");
        }

        PlatformSDK.config().setExpandInfo(map);

        try{
            if(child instanceof NativeView){
                PlatformSDK.config().ExposureToPlugin((NativeView) child);
            }else{
                if(((ViewGroup)child).getChildAt(0)!=null){
                    if(((ViewGroup)child).getChildAt(0) instanceof NativeView){
                        PlatformSDK.config().ExposureToPlugin((NativeView)((ViewGroup)child).getChildAt(0));
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
