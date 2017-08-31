package net.lzbook.kit.utils.popup;



import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

public abstract class PopupWindowManager implements PopupWindowInterface{

	protected Context mContext;
	protected PopupWindow popupWindow;
	protected PopupWindowOnShowingListener onShowingListener;
	
	public PopupWindowManager(Context context) {
		this.mContext = context;
	}

	@Override
	public void initPopupWindow(int layout_id, boolean isAllowTouchOutside) {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View baseView = inflater.inflate(layout_id, null);
		
		initView(baseView);
		
		if (isAllowTouchOutside&&popupWindow!=null) {
			popupWindow.setFocusable(true);
			popupWindow.setOutsideTouchable(true);
			popupWindow.setBackgroundDrawable(new ColorDrawable());
		}
		
	}
	
	@Override
	public void dismissPop()
	{
		if(popupWindow!=null){
			popupWindow.dismiss();
		}
	}
	
	@Override
	public boolean isShowing() {
		return popupWindow!=null && popupWindow.isShowing();
	}

	protected abstract void initView(View baseView);

	public void setPopupWindowOnShowingListener(PopupWindowOnShowingListener l) {
		this.onShowingListener = l;
	}

	public interface PopupWindowOnShowingListener {
		void onShowing(boolean isShowing);
	}
}
