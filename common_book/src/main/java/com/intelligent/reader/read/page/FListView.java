package com.intelligent.reader.read.page;


import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
public class FListView extends ListView{

	private View footView, headView;
	
	public FListView(Context context) {
		super(context);
		
		initView(context);
	}

	
	public FListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	
	private void initView(Context context){
		LayoutInflater inflater = LayoutInflater.from(context);
		footView = inflater.inflate(com.intelligent.reader.R.layout.footview, null);
//		headView = inflater.inflate(R.layout.footview, null);
//		addHeaderView(headView);
//		super.setOnScrollListener(this);

	}
	
	@Override
	public void setAdapter(ListAdapter adapter) {
		// make sure XListViewFooter is the last footer view, and only add once.
		addFooterView(footView);
		super.setAdapter(adapter);
	}

	public void setFootViewBackground(@DrawableRes int id){
        footView.findViewById(com.intelligent.reader.R.id.foot).setBackgroundResource(id);

	}
    public void setFootViewBackgroundColor(@ColorInt int id){
        footView.findViewById(com.intelligent.reader.R.id.foot).setBackgroundColor(id);
    }

//	@Override
//	public void onScrollStateChanged(AbsListView view, int scrollState) {
////		if (mScrollListener != null) {
////			mScrollListener.onScrollStateChanged(view, scrollState);
////		}
//	}
//
//	@Override
//	public void onScroll(AbsListView view, int firstVisibleItem,
//			int visibleItemCount, int totalItemCount) {
//		Log.e("onScroll", "totalItemCount:"+totalItemCount);
//		Log.e("onScroll", "getLastVisiblePosition:"+getLastVisiblePosition());
//		// send to user's listener
////		mTotalItemCount = totalItemCount;
////		if (mScrollListener != null) {
////			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,
////					totalItemCount);
////		}
//	}
}
