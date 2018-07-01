package com.intelligent.reader.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ding.basic.bean.HotWordBean;
import com.ding.basic.bean.SearchHotBean;
import com.intelligent.reader.R;



import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2017\9\4 0004.
 */

public class SearchHotWordAdapter extends BaseAdapter {
    private Activity mContext;
    private int layoutResourceId;
    private Random random;
    private int oldType = -1;
    private List<HotWordBean> datas;

    public SearchHotWordAdapter(Activity context, List<HotWordBean> datas) {
        this.mContext = context;
        this.datas = datas;
        random = new Random();
    }

    @Override
    public int getCount() {
        if (datas != null && datas.size() != 0) {
            if (datas.size() >= 6) {
                return 6;
            } else {
                return datas.size();
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
            convertView = inflater.inflate(R.layout.item_search_hotword, parent, false);
            holder = new ViewHolder();
            holder.txt_hotword = (TextView) convertView.findViewById(R.id.txt_hotword);
            holder.img_hot_rank = (ImageView) convertView.findViewById(R.id.img_hot_rank);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        HotWordBean dataBean = datas.get(position);
        holder.txt_hotword.setText(dataBean.getKeyword());
        setHotShowType(holder.img_hot_rank, position);
        return convertView;


    }

    private class ViewHolder {
        TextView txt_hotword;
        ImageView img_hot_rank;
    }


    public void setDatas(List<HotWordBean> datas) {
        this.datas = datas;
    }


    private void setHotShowType(ImageView bubbleIv, int position) {
        switch (position) {
            case 0:
                bubbleIv.setImageResource(R.drawable.search_img_hot_1);
                break;
            case 1:
                bubbleIv.setImageResource(R.drawable.search_img_hot_2);
                break;
            case 2:
                bubbleIv.setImageResource(R.drawable.search_img_hot_3);
                break;
            case 3:
                bubbleIv.setImageResource(R.drawable.search_img_hot_4);
                break;
            case 4:
                bubbleIv.setImageResource(R.drawable.search_img_hot_5);
                break;
            case 5:
                bubbleIv.setImageResource(R.drawable.search_img_hot_6);
                break;

        }
    }
}