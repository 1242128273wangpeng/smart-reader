package com.intelligent.reader.read.page;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.intelligent.reader.read.help.DrawTextHelper;

import net.lzbook.kit.data.bean.NovelLineBean;

import java.util.ArrayList;
import java.util.List;

public class Page extends View {

    private Bitmap mCurPageBitmap;
    private Paint paint;

    public Page(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
    }

    public void drawPage(Bitmap mCurPageBitmap) {
        this.mCurPageBitmap = mCurPageBitmap;
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mCurPageBitmap, new Rect(0, 0, getWidth(), getHeight()), new Rect(0, 0, getWidth(), getHeight()), paint);
    }

    public void drawContent(DrawTextHelper drawTextHelper, Canvas pageCanvas, Bitmap pageBitmap, List<NovelLineBean> pageLines, ArrayList<NovelLineBean> chapterNameList) {
        drawTextHelper.drawText(pageCanvas, pageLines, chapterNameList);
        drawPage(pageBitmap);
    }

}
