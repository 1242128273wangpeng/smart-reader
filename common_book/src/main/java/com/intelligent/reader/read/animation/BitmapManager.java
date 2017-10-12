package com.intelligent.reader.read.animation;

import com.intelligent.reader.app.BookApplication;

import net.lzbook.kit.constants.Constants;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class BitmapManager {

    public static final int CURRENT = 0;
    public static final int NEXT = 1;
    public static final int BG = 2;
    private static final int SIZE = 2;
    private static volatile BitmapManager mInstance = null;
    private final Bitmap[] myBitmaps = new Bitmap[SIZE];
    ArrayList<Bitmap> bitmaps = new ArrayList<>();
    private int myWidth;
    private int myHeight;
    private int mFootHeight;
    private int navigetionHeight;
    private int spaceHeight;

    private BitmapManager() {
        if (BookApplication.getDisplayMetrics() != null) {
            navigetionHeight = (int) (BookApplication.getDisplayMetrics().density * 50);
            spaceHeight = (int) (BookApplication.getDisplayMetrics().density * 10);

        } else {
            navigetionHeight = 120;
            spaceHeight = 30;
        }
    }

    public static BitmapManager getInstance() {
        if (mInstance == null) {
            synchronized (BitmapManager.class) {
                if (mInstance == null) {
                    mInstance = new BitmapManager();
                }
            }
        }

        return mInstance;
    }

    public synchronized void setSize(int width, int height) {
        int tempHeight = height;
        if (Constants.isSlideUp) {
            tempHeight += spaceHeight;
        }

        if (myWidth != width || myHeight != tempHeight) {
            clearBitmap();

            this.myWidth = width;
            this.myHeight = tempHeight;
        }
    }

    public synchronized Bitmap getBitmap() {
        Bitmap bitmap;
        try {
            bitmap = Bitmap.createBitmap(myWidth, myHeight, Bitmap.Config.RGB_565);
        } catch (OutOfMemoryError e) {
            System.gc();
            System.runFinalization();
            bitmap = Bitmap.createBitmap(myWidth, myHeight, Bitmap.Config.RGB_565);
        }
        bitmaps.add(bitmap);
        return bitmap;
    }

    public synchronized Bitmap getBitmap4444() {
        Bitmap bitmap;
        try {
            bitmap = Bitmap.createBitmap(myWidth, myHeight, Bitmap.Config.ARGB_4444);
        } catch (OutOfMemoryError e) {
            System.gc();
            System.runFinalization();
            bitmap = Bitmap.createBitmap(myWidth, myHeight, Bitmap.Config.ARGB_4444);
        }
        bitmaps.add(bitmap);
        return bitmap;
    }

    public synchronized Bitmap getBitmap(int which) {
        if (which >= SIZE) {
            throw new IllegalArgumentException();
        }
        if (which < 3) {
            if (myBitmaps[which] == null) {
                try {
                    myBitmaps[which] = Bitmap.createBitmap(myWidth, myHeight, Bitmap.Config.RGB_565);
                } catch (OutOfMemoryError e) {
                    System.gc();
                    System.gc();
                    System.gc();
                    System.runFinalization();
                    try {
                        myBitmaps[which] = Bitmap.createBitmap(myWidth, myHeight, Bitmap.Config.RGB_565);
                    } catch (OutOfMemoryError e2) {
                        System.gc();
                        System.gc();
                        System.gc();
                        System.runFinalization();
                        myBitmaps[which] = Bitmap.createBitmap(myWidth, myHeight, Bitmap.Config.RGB_565);
                    }

                }

            }
        } else {
            if (myBitmaps[which] == null) {
                try {
                    myBitmaps[which] = Bitmap.createBitmap(myWidth, mFootHeight, Bitmap.Config.RGB_565);
                } catch (OutOfMemoryError e) {
                    System.gc();
                    System.gc();
                    System.gc();
                    System.runFinalization();
                    try {
                        myBitmaps[which] = Bitmap.createBitmap(myWidth, mFootHeight, Bitmap.Config.RGB_565);
                    } catch (OutOfMemoryError e2) {
                        System.gc();
                        System.gc();
                        System.gc();
                        System.runFinalization();
                        myBitmaps[which] = Bitmap.createBitmap(myWidth, mFootHeight, Bitmap.Config.RGB_565);
                    }

                }

            }
        }


        return myBitmaps[which];
    }

    public void clearBitmap() {
        for (int i = 0; i < myBitmaps.length; i++) {
            if (myBitmaps[i] != null && !myBitmaps[i].isRecycled()) {
                myBitmaps[i].recycle();
            }
            myBitmaps[i] = null;
        }

        for (Bitmap bitmap :
                bitmaps) {
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }

        bitmaps.clear();
        System.gc();
        System.gc();
    }

}
