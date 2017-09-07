package com.intelligent.reader.adapter;

import com.intelligent.reader.R;

import net.lzbook.kit.utils.AppLog;
import net.lzbook.kit.utils.AppUtils;

import android.content.Context;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/12/16 0016.
 */
public class SearchSuggestAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<String> mData;
    private String editInput;

    public SearchSuggestAdapter(Context context, ArrayList<String> mData, String editInput) {
        this.mContext = context;
        this.mData = mData;
        this.editInput = editInput;
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
                convertView = inflater.inflate(R.layout.lv_searchbook_item, parent, false);
            } catch (InflateException e) {
                e.printStackTrace();
            }
            if (convertView != null) {
                hodler = new ViewHolder();
                hodler.tv_1 = (TextView) convertView.findViewById(R.id.tv_search_item_1);
                hodler.tv_2 = (TextView) convertView.findViewById(R.id.tv_search_item_2);
                convertView.setTag(hodler);
            }
        } else {
            hodler = (ViewHolder) convertView.getTag();
        }
        String content = mData.get(position);
        if (editInput != null) {
            editInput = AppUtils.deleteAllIllegalChar(editInput);
            AppLog.i("getView", "editInput = " + editInput);
            hodler.tv_1.setText(editInput);
            hodler.tv_2.setText(content.replaceFirst(editInput, ""));
        } else {
            hodler.tv_1.setText("");
            hodler.tv_2.setText(content);
        }
        return convertView;
    }

    public void clear() {
        if (mData != null) {
            mData.clear();
        }
    }

    public void setEditInput(String editInput) {
        this.editInput = editInput;
    }

    private static class ViewHolder {
        TextView tv_1;
        TextView tv_2;
    }

}
