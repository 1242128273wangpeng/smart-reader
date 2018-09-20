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
import com.ding.basic.bean.SearchHotBean;
import com.intelligent.reader.R;

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
    private List<HotWordBean> datas;

    public SearchHotWordAdapter(Activity context,List<HotWordBean> datas) {
        this.mContext = context;
        this.datas = datas;
        random = new Random();
    }

    @Override
    public int getCount() {
        if(datas!=null&&datas.size()!=0){
            if (datas.size() >= 5) {
                return 6;
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
            holder.iv_type = (ImageView) convertView.findViewById(R.id.iv_type);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        HotWordBean dataBean = datas.get(position);
        holder.tvHotWord.setText(dataBean.getKeyword());
        if(!TextUtils.isEmpty(dataBean.getSuperscript())){
            holder.iv_type.setVisibility(View.VISIBLE);
            if(dataBean.getSuperscript().equals("热")){
                holder.iv_type.setImageResource(R.drawable.icon_hot_re);
            }else if(dataBean.getSuperscript().equals("荐")){
                holder.iv_type.setImageResource(R.drawable.icon_hot_jian);
            }else if(dataBean.getSuperscript().equals("新")){
                holder.iv_type.setImageResource(R.drawable.icon_hot_xin);
            }
        }else{
            holder.iv_type.setVisibility(View.GONE);
        }
        if(!TextUtils.isEmpty(dataBean.getColor())){
            holder.tvHotWord.setTextColor(Color.parseColor(dataBean.getColor()));
        }
        return convertView;


    }

    private class ViewHolder {
        TextView tvHotWord;
        ImageView iv_type;
    }


    public void setDatas(List<HotWordBean> datas) {
        this.datas = datas;
    }


}
