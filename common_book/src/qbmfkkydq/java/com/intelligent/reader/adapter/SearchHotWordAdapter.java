package com.intelligent.reader.adapter;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ding.basic.bean.HotWordBean;
import com.intelligent.reader.R;


import net.lzbook.kit.data.search.SearchHotBean;

import java.util.List;
import java.util.Random;

/**
 * 热词实体类
 * Created by Administrator on 2017\9\4 0004.
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
            if (list.size() >= 9) {
                return 9;
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
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        HotWordBean dataBean = list.get(position);
        holder.tvHotWord.setText(dataBean.getKeyword());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            holder.tvHotWord.setBackground(getHotWordBgColor(hotWordBgColor[position]));
        }

        return convertView;


    }

    public void setData(List<HotWordBean> list) {
        this.list = list;
    }


    /**
     * 边框填充和文字颜色
     */
    private int[] hotWordBgColor =
            {R.color.search_hotWord_bg1, R.color.search_hotWord_bg2, R.color.search_hotWord_bg3,
                    R.color.search_hotWord_bg4, R.color.search_hotWord_bg5,
                    R.color.search_hotWord_bg6};

    /**
     * 设置边框，背景，圆角
     */
    private GradientDrawable getHotWordBgColor(int solidColor) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setCornerRadius(2); //圆角
        gd.setColor(ContextCompat.getColor(mContext, solidColor));//填充色
        return gd;
    }

    private class ViewHolder {
        TextView tvHotWord;
    }
}
