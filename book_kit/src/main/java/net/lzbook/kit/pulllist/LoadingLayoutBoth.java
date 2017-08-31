package net.lzbook.kit.pulllist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.lzbook.kit.R;

import java.text.SimpleDateFormat;
import java.util.Date;


public class LoadingLayoutBoth extends FrameLayout {

	static final int DEFAULT_ROTATION_ANIMATION_DURATION = 150;

	private  ImageView refreshImage;
	private  ProgressBar refreshProgress;
	private TextView refreshText;
	private TextView timeText = null;

	private String pullLabel;
	private String refreshingLabel;
	private String releaseLabel;

	private final Animation rotateAnimation, resetRotateAnimation;

	public LoadingLayoutBoth(Context context, final int mode, String releaseLabel, String pullLabel, String refreshingLabel) {
		super(context);
		getView(context,mode);

		final Interpolator interpolator = new LinearInterpolator();
		rotateAnimation = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
		        0.5f);
		rotateAnimation.setInterpolator(interpolator);
		rotateAnimation.setDuration(DEFAULT_ROTATION_ANIMATION_DURATION);
		rotateAnimation.setFillAfter(true);

		resetRotateAnimation = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 0.5f,
		        Animation.RELATIVE_TO_SELF, 0.5f);
		resetRotateAnimation.setInterpolator(interpolator);
		resetRotateAnimation.setDuration(DEFAULT_ROTATION_ANIMATION_DURATION);
		resetRotateAnimation.setFillAfter(true);

		this.releaseLabel = releaseLabel;
		this.pullLabel = pullLabel;
		this.refreshingLabel = refreshingLabel;

	}

	protected ViewGroup getView(Context context,int mode) {
		ViewGroup header = null;
		
		switch (mode) {
		case PullToRefreshBase.MODE_PULL_UP_TO_REFRESH:
			header = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_footer, this);
			refreshText = (TextView) header.findViewById(R.id.pull_to_refresh_text_list_footer);
			refreshImage = (ImageView) header.findViewById(R.id.pull_to_refresh_image_list_footer);
			refreshProgress = (ProgressBar) header.findViewById(R.id.pull_to_refresh_progress_list_footer);
			timeText = (TextView) header.findViewById(R.id.pull_to_refresh_time_list_footer);

			refreshImage.setImageResource(R.drawable.pulltorefresh_up_arrow);
			break;
		case PullToRefreshBase.MODE_PULL_DOWN_TO_REFRESH:
		default:
			header = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_header, this);
			refreshText = (TextView) header.findViewById(R.id.pull_to_refresh_text_list);
			refreshImage = (ImageView) header.findViewById(R.id.pull_to_refresh_image_list);
			refreshProgress = (ProgressBar) header.findViewById(R.id.pull_to_refresh_progress_list);
			timeText = (TextView) header.findViewById(R.id.pull_to_refresh_time_list);
			refreshImage.setImageResource(R.drawable.pulltorefresh_down_arrow);
			break;
		}
		return header;
	}

	public void reset(boolean isShowUpdateTime) {
		refreshText.setText(pullLabel);
		refreshImage.setVisibility(View.VISIBLE);
		refreshProgress.setVisibility(View.GONE);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		if(isShowUpdateTime){
			timeText.setText("最后更新："+sdf.format(new Date(System.currentTimeMillis())));
			timeText.setVisibility(View.VISIBLE);
		}else{
			timeText.setVisibility(View.GONE);
		}
	}

	public void releaseToRefresh() {
		refreshText.setText(releaseLabel);
		refreshImage.clearAnimation();
		refreshImage.startAnimation(rotateAnimation);
	}

	public void setPullLabel(String pullLabel) {
		this.pullLabel = pullLabel;
	}

	public void refreshing() {
		refreshText.setText(refreshingLabel);
		refreshImage.clearAnimation();
		refreshImage.setVisibility(View.GONE);
		refreshProgress.setVisibility(View.VISIBLE);
	}

	public void setRefreshingLabel(String refreshingLabel) {
		this.refreshingLabel = refreshingLabel;
	}

	public void setReleaseLabel(String releaseLabel) {
		this.releaseLabel = releaseLabel;
	}

	public void pullToRefresh() {
		refreshText.setText(pullLabel);
		refreshImage.clearAnimation();
		refreshImage.startAnimation(resetRotateAnimation);
	}

	public void setTextColor(int color) {
		refreshText.setTextColor(color);
	}

}
