package com.intelligent.reader.adapter;

import com.intelligent.reader.R;

import android.content.Context;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by yuchao on 2017/08/05 0016.
 */
public class SearchHisAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<String> mData;
    private SearchClearCallBack mSearchClearCallBack;

    public SearchHisAdapter(Context context, ArrayList<String> mData) {
        this.mContext = context;
        this.mData = mData;
    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData == null ? null : mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder hodler = null;
        if (convertView == null) {
            try {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(R.layout.lv_search_his_item, parent, false);
            } catch (InflateException e) {
                e.printStackTrace();
            }
            if (convertView != null) {
                hodler = new ViewHolder();
                hodler.tv_content = (TextView) convertView.findViewById(R.id.tv_search_his_content);
                hodler.iv_clear = (ImageView) convertView.findViewById(R.id.iv_search_his_clear);
                convertView.setTag(hodler);
            }
        } else {
            hodler = (ViewHolder) convertView.getTag();
        }
        String content = mData.get(position);
        hodler.tv_content.setText(content);
        hodler.iv_clear.setTag(content);
        hodler.iv_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSearchClearCallBack != null) {
                    String conte = null;
                    if (v != null) {
                        conte = (String) (v.getTag());
                    }
                    mSearchClearCallBack.onSearchClear(conte);
                }
            }
        });
        return convertView;
    }

    public void clear() {
        if (mData != null) {
            mData.clear();
        }
    }

    public void setSearchClearCallBack(SearchClearCallBack searchClearCallBack) {
        mSearchClearCallBack = searchClearCallBack;
    }

    public interface SearchClearCallBack {
        void onSearchClear(String content);
    }

    private static class ViewHolder {
        TextView tv_content;
        ImageView iv_clear;
    }

}
