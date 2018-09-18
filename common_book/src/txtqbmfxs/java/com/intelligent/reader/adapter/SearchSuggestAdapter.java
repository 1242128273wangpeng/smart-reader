package com.intelligent.reader.adapter;

import android.content.Context;
import android.text.Html;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ding.basic.bean.SearchCommonBeanYouHua;
import com.intelligent.reader.R;

import net.lzbook.kit.utils.AppUtils;

import java.util.List;

/**
 * 自动补全
 * Created by Administrator on 2016/12/16 0016.
 */
public class SearchSuggestAdapter extends BaseAdapter {

    private Context mContext;
    private List<Object> mData;
    private String editInput = "";
    private String mColorTag;
    //item的类型
    private static final int ITEM_VIEW_TYPE_DATA = 0;
    private static final int ITEM_VIEW_TYPE_GAP = 1;
    private static final int ITEM_VIEW_TYPE_COUNT = 2;

    public SearchSuggestAdapter(Context context, List<Object> mData, String editInput) {
        this.mContext = context;
        this.mData = mData;
        this.editInput = editInput;
        int color = mContext.getResources().getColor(R.color.colorPrimary);
        mColorTag = String.format("<font color='%s'>", AppUtils.colorHoHex(color));
    }

    @Override
    public int getViewTypeCount() {
        return ITEM_VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position) instanceof SearchCommonBeanYouHua ? ITEM_VIEW_TYPE_DATA
                : ITEM_VIEW_TYPE_GAP;
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
        //判断view的type，通过type判断item是显示数据还是隔断
        switch (getItemViewType(position)) {
            //数据填充item
            case ITEM_VIEW_TYPE_DATA:

                ViewHolder holder = null;
                if (convertView == null) {
                    try {
                        LayoutInflater inflater = LayoutInflater.from(mContext);
                        convertView = inflater.inflate(R.layout.item_search_suggest, parent, false);
                    } catch (InflateException e) {
                        e.printStackTrace();
                    }
                    if (convertView != null) {
                        holder = new ViewHolder();
                        holder.iv_icon = convertView.findViewById(R.id.iv_icon);
                        holder.tv_search_item = convertView.findViewById(R.id.tv_search_item_2);
                        convertView.setTag(holder);
                    }
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                SearchCommonBeanYouHua bean = (SearchCommonBeanYouHua) mData.get(position);
                String type = bean.getWordtype();
                if (holder != null) {
                    switch (type) {
                        case "author":
                            holder.iv_icon.setImageResource(R.drawable.search_personal_yh);
                            break;
                        case "label":
                            holder.iv_icon.setImageResource(R.drawable.search_label_icon);
                            break;
                        case "name":
                            holder.iv_icon.setImageResource(R.drawable.search_book_icon);
                            break;
                    }

                    String content = bean.getSuggest();
                    String finalInput = "";

                    if (editInput != null) {
                        finalInput = AppUtils.deleteAllIllegalChar(editInput);
                    }

                    content = content.replaceAll(finalInput, mColorTag + finalInput + "</font>");
                    holder.tv_search_item.setText(Html.fromHtml(content));
                }
                break;
            //item中间的gap显示
            case ITEM_VIEW_TYPE_GAP:
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(
                            R.layout.item_search_history_gap, parent, false);
                }
                break;
            default:
                break;
        }

        return convertView;
    }

    public void clear() {
        if (mData != null) {
            mData.clear();
        }
    }

    private static class ViewHolder {
        ImageView iv_icon;
        TextView tv_search_item;
    }

    public void setEditInput(String editInput) {
        this.editInput = editInput;
    }

}
