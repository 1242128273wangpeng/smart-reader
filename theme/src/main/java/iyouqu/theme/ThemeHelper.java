package iyouqu.theme;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * 主题助手：负责从SP中存取主题偏好.
 */
public class ThemeHelper {
    private final static String MODE = "theme_mode";
    private final static String MODE_DEFAULT = "theme_default";
    private SharedPreferences mSharedPreferences;

    public ThemeHelper(Context context) {
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * 保存主题模式；在设置非夜间模式的时候会把选中的主题存储到默认主题SP中
     *
     * @return boolean
     */
    public boolean setMode(ThemeMode mode) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(MODE, mode.getName());
        if (!ThemeMode.NIGHT.getName().equals(mode.getName())) {
            editor.putInt(MODE_DEFAULT, mode.getCode());
        }
        return editor.commit();
    }


    /**
     * 是否主题一
     */
    public boolean isTheme1() {
        String mode = mSharedPreferences.getString(MODE, ThemeMode.THEME1.getName());
        if (!ThemeMode.NIGHT.getName().equals(mode)) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 是否主题四
     */
    public boolean isNight() {
        String mode = mSharedPreferences.getString(MODE, ThemeMode.THEME1.getName());
        if (ThemeMode.NIGHT.getName().equals(mode)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取当前主题
     *
     * @return String
     */
    public String getMode() {
        return mSharedPreferences.getString(MODE, ThemeMode.THEME1.getName());
    }

    /**
     * 获取上次用户选取的主题
     *
     * @return String
     */
    public int getModeDefault() {
        return mSharedPreferences.getInt(MODE_DEFAULT, ThemeMode.THEME1.getCode());
    }


    /**
     * 设置Textview左上右下的图片
     */
    public void setTextviewDrawable(@NonNull Activity act, TypedValue left, TypedValue top, TypedValue right, TypedValue bottom, TextView tv) {
        tv.setCompoundDrawables(setDrawableBounds(act, left), setDrawableBounds(act, top), setDrawableBounds(act, right), setDrawableBounds(act, bottom));
    }

    private Bitmap getCacheBitmapFromView(View view) {
        final boolean drawingCacheEnabled = true;
        view.setDrawingCacheEnabled(drawingCacheEnabled);
        view.buildDrawingCache(drawingCacheEnabled);
        final Bitmap drawingCache = view.getDrawingCache();
        Bitmap bitmap;
        if (drawingCache != null) {
            bitmap = Bitmap.createBitmap(drawingCache);
            view.setDrawingCacheEnabled(false);
        } else {
            bitmap = null;
        }
        return bitmap;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void showAnimation(Activity act) {
        final View decorView = act.getWindow().getDecorView();
        Bitmap cacheBitmap = getCacheBitmapFromView(decorView);
        if (decorView instanceof ViewGroup && cacheBitmap != null) {
            final View view = new View(act);
            view.setBackgroundDrawable(new BitmapDrawable(act.getResources(), cacheBitmap));
            ViewGroup.LayoutParams layoutParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            ((ViewGroup) decorView).addView(view, layoutParam);
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
            objectAnimator.setDuration(250);
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ((ViewGroup) decorView).removeView(view);
                }
            });
            objectAnimator.start();
        }
    }

    private Drawable setDrawableBounds(Activity act, TypedValue typedValue) {
        Drawable pic = null;
        if (typedValue != null) {
            pic = act.getResources().getDrawable(typedValue.resourceId);
            pic.setBounds(0, 0, pic.getMinimumWidth(), pic.getMinimumHeight());
        }
        return pic;
    }
}
