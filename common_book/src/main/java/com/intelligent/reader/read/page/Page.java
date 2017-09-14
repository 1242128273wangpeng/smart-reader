package com.intelligent.reader.read.page;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.intelligent.reader.read.help.DrawTextHelper;

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
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mCurPageBitmap, 0, 0, paint);
    }

    public void drawContent(DrawTextHelper drawTextHelper, Canvas pageCanvas, Bitmap pageBitmap, List<String> pageLines, ArrayList<String> chapterNameList) {
        Log.e("Page","content: " + pageLines.toString());
//        Log.e("Page","chapterName: " + chapterNameList.toString());
        drawTextHelper.drawText(pageCanvas, pageLines, chapterNameList);
        drawPage(pageBitmap);
    }

}
