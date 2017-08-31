package net.lzbook.kit.utils.popup;

import android.view.View;

public interface PopupWindowInterface {

	/**
	 * layout_id
	 * isAllowTouchOutside 是否允许点击外界popupwindow消失
     */
	void initPopupWindow(int layout_id, boolean isAllowTouchOutside);

	void showPopupWindow(View parent);
	
	boolean isShowing();
	
	void dismissPop();
	
	void changeText(String text);

}
