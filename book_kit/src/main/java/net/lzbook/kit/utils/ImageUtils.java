package net.lzbook.kit.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by q on 2015/12/14.
 */
public class ImageUtils {
	/**
	 * 矩形图片 圆角
	 * @param bitmap
	 * @param pixels
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
		Bitmap output;
		try {
			output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			System.gc();
			System.gc();
			return null;
		}
		if(output == null) return null;
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		float roundPx = pixels;
		if (pixels == 0) {
			roundPx = bitmap.getWidth() / 2;
		}
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		if (bitmap == null || bitmap.isRecycled()) {
			return null;
		}
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}
}
