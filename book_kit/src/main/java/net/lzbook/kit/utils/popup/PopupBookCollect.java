package net.lzbook.kit.utils.popup;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import net.lzbook.kit.R;


/**
 * 书架用弹窗
 */
public class PopupBookCollect extends PopupBase implements View.OnClickListener {
	protected Button btn_collect;
	protected PopupCollectClickListener collectClickListener;
	View baseView;

	public PopupBookCollect(Context context) {
		super(context);
	}

	@Override
	public void showPopupWindow(View parent) {
		super.showPopupWindow(parent);
	}

	@Override
	public void dismissPop() {
		super.dismissPop();
		if (onShowingListener != null) {
			onShowingListener.onShowing(false);
		}
	}

	@Override
	protected void initView(View baseView) {
		//        super.initView(baseView);
		this.baseView = baseView;
		popupWindow = new PopupWindow(baseView, WindowManager.LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(R.dimen.dimen_view_height_50));
		popupWindow.setAnimationStyle(R.style.remove_menu_anim_style);
		if (baseView != null) {

			LinearLayout layout = (LinearLayout) baseView.findViewById(R.id.remove_delete_layout);

			delete_btn = (Button) baseView.findViewById(R.id.btn_right);
			btn_collect = (Button) baseView.findViewById(R.id.btn_left);
			if (delete_btn != null)
				delete_btn.setOnClickListener(this);
			if (btn_collect != null)
				btn_collect.setOnClickListener(this);
			if (layout == null)
				return;
			layout.setOnKeyListener(new View.OnKeyListener() {

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_MENU) {
						if (onShowingListener != null) {
							onShowingListener.onShowing(false);
						}
						if (popupWindow != null) {

							popupWindow.dismiss();
						}
						return true;
					}
					return false;
				}
			});

			layout.setFocusable(true);
			layout.setFocusableInTouchMode(true);
			layout.requestFocus();
		}


	}

	@Override
	public void onClick(View v) {
		int i = v.getId();
		if (i == R.id.btn_right) {
			if (clickListener != null) {
				clickListener.clickDeleteBtn();
			}

		} else if (i == R.id.btn_left) {
			if (collectClickListener != null) {
				collectClickListener.clickCollect(btn_collect);
			}


		} else {
		}
	}


	public interface PopupCollectClickListener {
		void clickCollect(View collectView);
	}

	public void setPopupCollectClickListener(PopupCollectClickListener l) {
		this.collectClickListener = l;
	}

	@Override
	public void changeText(String text) {
		//        super.ChangeText(text);
	}
}
