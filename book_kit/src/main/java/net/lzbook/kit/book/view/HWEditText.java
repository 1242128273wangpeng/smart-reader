package net.lzbook.kit.book.view;

import net.lzbook.kit.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * Created by Administrator on 2017/3/20 0020.
 * for 修改华为手机的EditText光标颜色
 */
public class HWEditText extends EditText {

    public HWEditText(Context context) {
        this(context, null);
    }

    public HWEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        modifyCursorDrawable(context, attrs);
    }

    public HWEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        modifyCursorDrawable(context, attrs);
    }

    private void modifyCursorDrawable(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HWEditText);
        int drawable = a.getResourceId(R.styleable.HWEditText_HWEditText_textCursorDrawable, 0);
        if (drawable != 0) {
            try {

                Field setCursor = TextView.class.getDeclaredField("mCursorDrawableRes");
                setCursor.setAccessible(true);
                setCursor.set(this, drawable);

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        a.recycle();
    }
}
