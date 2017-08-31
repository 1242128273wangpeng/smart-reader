package com.intelligent.reader.read.animation;

import com.intelligent.reader.app.BookApplication;

import net.lzbook.kit.constants.Constants;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class BitmapManager {

    private static final int SIZE = 5;
    private final Bitmap[] myBitmaps = new Bitmap[SIZE];

    private int myWidth;
    private int myHeight;
    private int mFootHeight;

    public static final int CURRENT = 0;
    public static final int NEXT = 1;
    public static final int BG = 2;
    private int navigetionHeight;
    private int spaceHeight;

    public BitmapManager(int width, int height) {
        if (BookApplication.getDisplayMetrics() != null) {
            navigetionHeight = (int) (BookApplication.getDisplayMetrics().density * 50);
            spaceHeight = (int) (BookApplication.getDisplayMetrics().density * 10);

        } else {
            navigetionHeight = 120;
            spaceHeight = 30;
        }

        this.myWidth = width;
        this.myHeight = height;
        if (Constants.isSlideUp) {
            this.myHeight += spaceHeight;
        }

    }

    ArrayList<Bitmap> bitmaps = new ArrayList<>();

    public Bitmap getBitmap() {
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

    public Bitmap getBitmap(int which) {
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
                myBitmaps[i] = null;
            }
        }
        for (int i = 0; i < bitmaps.size(); i++) {
            Bitmap bitmap = bitmaps.get(i);
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null;
            }
        }
        bitmaps.clear();
        bitmaps = null;

        System.gc();
    }

}
