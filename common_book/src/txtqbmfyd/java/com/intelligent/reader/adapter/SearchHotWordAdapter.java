package com.intelligent.reader.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.intelligent.reader.R;


import net.lzbook.kit.data.search.SearchHotBean;

import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2017\9\4 0004.
 */

public class SearchHotWordAdapter extends BaseAdapter {
    private List<SearchHotBean.DataBean> hotData;
    private Activity mContext;
    private int layoutResourceId;
    private Random random;
    private int oldType = -1;
    private List<SearchHotBean.DataBean> datas;

    public SearchHotWordAdapter(Activity context,List<SearchHotBean.DataBean> datas) {
        this.mContext = context;
        this.datas = datas;
        random = new Random();
    }

    @Override
    public int getCount() {
        if(datas!=null&&datas.size()!=0){
            if (datas.size() >= 9) {
                return 9;
            }else{
                return datas.size();
            }
        }else{
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
            holder.tvHotWord = (TextView) convertView.findViewById(R.id.tv_hotword);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        SearchHotBean.DataBean dataBean = datas.get(position);
        holder.tvHotWord.setText(dataBean.getWord());
        setHotShowType(holder.tvHotWord);
        return convertView;


    }

    private class ViewHolder {
        TextView tvHotWord;
    }


    public void setDatas(List<SearchHotBean.DataBean> datas) {
        this.datas = datas;
    }


    private void setHotShowType(TextView textView) {
        int currType = random.nextInt(7);
        while (oldType == currType){
            currType = random.nextInt(7);
        }
        oldType = currType;
        int csl;
        switch (currType){
            case 0:
            case 1:
            case 2:
            case 3:
                textView.setBackgroundResource(R.drawable.search_hot_word_bg_1);
                csl =  mContext.getResources().getColor(R.color.search_hot_word_text_color_1);
                textView.setTextColor(csl);
                break;
            case 4:
                textView.setBackgroundResource(R.drawable.search_hot_word_bg_2);
                csl =  mContext.getResources().getColor(R.color.search_hot_word_text_color_2);
                textView.setTextColor(csl);
                break;
            case 5:
                textView.setBackgroundResource(R.drawable.search_hot_word_bg_3);
                csl =  mContext.getResources().getColor(R.color.search_hot_word_text_color_3);
                textView.setTextColor(csl);
                break;
            case 6:
                textView.setBackgroundResource(R.drawable.search_hot_word_bg_3);
                csl =  mContext.getResources().getColor(R.color.search_hot_word_text_color_4);
                textView.setTextColor(csl);
                break;
        }
    }
}
