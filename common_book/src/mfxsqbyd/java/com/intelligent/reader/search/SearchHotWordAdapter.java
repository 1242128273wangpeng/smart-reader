package com.intelligent.reader.search;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ding.basic.bean.HotWordBean;
import com.intelligent.reader.R;


import java.util.List;

/**
 * Function：搜索热词子条目
 *
 * Created by JoannChen on 2018/5/30 0030 17:09
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
public class SearchHotWordAdapter extends BaseAdapter {
    private Activity mContext;
    private List<HotWordBean> data;

    SearchHotWordAdapter(Activity context, List<HotWordBean> data) {
        this.mContext = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        if (data != null && data.size() != 0) {
            if (data.size() >= 5) {
                return 6;
            } else {
                return data.size();
            }
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = mContext.getLayoutInflater();
            convertView = inflater.inflate(R.layout.search_item_hotword, parent, false);
            holder = new ViewHolder();
            holder.tvHotWord = (TextView) convertView.findViewById(R.id.tv_hot_word);
            holder.iv_type = (ImageView) convertView.findViewById(R.id.iv_type);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        HotWordBean dataBean = data.get(position);
        holder.tvHotWord.setText(dataBean.getKeyword());
        if (!TextUtils.isEmpty(dataBean.getSuperscript())) {
            holder.iv_type.setVisibility(View.VISIBLE);
            switch (dataBean.getSuperscript()) {
                case "热":
                    holder.iv_type.setImageResource(R.mipmap.search_icon_hot);
                    break;
                case "荐":
                    holder.iv_type.setImageResource(R.mipmap.search_icon_tuijian);
                    break;
                case "新":
                    holder.iv_type.setImageResource(R.mipmap.search_icon_new);
                    break;
            }
        } else {
            holder.iv_type.setVisibility(View.GONE);
        }
/*
//        动态改变字体颜色
        if (!TextUtils.isEmpty(dataBean.getColor())) {
            holder.tvHotWord.setTextColor(Color.parseColor(dataBean.getColor()));
            holder.tvHotWord.setBackgroundResource(R.drawable.book_search_theme_bg);
        }*/

        return convertView;
    }

    private class ViewHolder {
        TextView tvHotWord;
        ImageView iv_type;
    }


    public void setData(List<HotWordBean> data) {
        this.data = data;
    }

}
