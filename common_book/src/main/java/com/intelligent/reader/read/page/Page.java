package com.intelligent.reader.read.page;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class Page extends View{

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

}
