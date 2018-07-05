package com.intelligent.reader.adapter;

import android.app.Activity;
import android.graphics.Color;
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
 * Created by JoannChen on 2018/6/14 0013 21:08
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
public class SearchHotWordAdapter extends BaseAdapter {

    private Activity mContext;
    private List<HotWordBean> list;

    public SearchHotWordAdapter(Activity context, List<HotWordBean> list) {
        this.mContext = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        if (list != null && list.size() != 0) {
            if (list.size() >= 5) {
                return 6;
            } else {
                return list.size();
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
            holder.tvHotWord = convertView.findViewById(R.id.tv_hotword);
            holder.iv_type = convertView.findViewById(R.id.iv_type);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        HotWordBean dataBean = list.get(position);
        holder.tvHotWord.setText(dataBean.getKeyword());
        if (!TextUtils.isEmpty(dataBean.getSuperscript())) {
            holder.iv_type.setVisibility(View.VISIBLE);
            switch (dataBean.getSuperscript()) {
                case "热":
                    holder.iv_type.setImageResource(R.drawable.icon_search_hot);
                    break;
                case "荐":
                    holder.iv_type.setImageResource(R.drawable.icon_search_recommend);
                    break;
                case "新":
                    holder.iv_type.setImageResource(R.drawable.icon_search_new);
                    break;
            }
        } else {
            holder.iv_type.setVisibility(View.GONE);
        }
//        if (!TextUtils.isEmpty(dataBean.getColor())) {
//            holder.tvHotWord.setTextColor(Color.parseColor(dataBean.getColor()));
//            holder.tvHotWord.setBackgroundResource(R.drawable.booksearch_theme_color_bg);
//        }

        return convertView;
    }

    private class ViewHolder {
        TextView tvHotWord;
        ImageView iv_type;
    }


    public void setList(List<HotWordBean> list) {
        this.list = list;
    }

}
