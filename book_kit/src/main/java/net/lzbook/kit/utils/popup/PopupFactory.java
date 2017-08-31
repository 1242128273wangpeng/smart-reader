package net.lzbook.kit.utils.popup;

import android.content.Context;

/**
 * 生产popupwindow的工厂类
 */
public class PopupFactory {

    public static final int BASE_BOTTOM = 0;
    public static final int DOWNLOAD_BOTTOM = 1;
    public static final int LIFT_BOTTOM = 2;
    public static final int ADDBOOK_BOTTOM = 3;
    public static final int BOOKCOLLECT_BOTTOM = 4;
    public static final int DELETE_CANCLE = 5;

    public PopupWindowInterface getPopupWindow(Context context, int type) {
        switch (type) {
            case BASE_BOTTOM:

                return new PopupBase(context);
            case DOWNLOAD_BOTTOM:
                return new PopupDownloadManager(context);
//            case ADDBOOK_BOTTOM:
//
//                return new PopupAddBook(context);
            case BOOKCOLLECT_BOTTOM:

                return new PopupBookCollect(context);
            case DELETE_CANCLE:
                return new PopupDeleteCancle(context);
            default:
                return null;
        }
    }
}
