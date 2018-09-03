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

import net.lzbook.kit.utils.AppUtils;

import java.util.List;

import com.ding.basic.bean.SearchCommonBeanYouHua;
import com.intelligent.reader.R;
import com.intelligent.reader.search.SearchConstant;

/**
 * Created by Administrator on 2016/12/16 0016.
 */
public class SearchSuggestAdapter extends BaseAdapter {


    private Context mContext;
    private List<SearchSuggest> mData;
    private String editInput="";
    String mColorTag;
    //item的类型
    public static final int ITEM_VIEW_TYPE_DATA = 0;
    public static final int ITEM_VIEW_MARGEN_ONE = 1;
    public static final int ITEM_VIEW_MARGEN_TWO = 2;
    private static final int ITEM_VIEW_TYPE_COUNT = 3;

    public SearchSuggestAdapter(Context context, List<SearchSuggest> mData, String editInput) {
        this.mContext = context;
        this.mData = mData;
        this.editInput = editInput;
        int color = mContext.getResources().getColor(R.color.dialog_recommend);
        mColorTag = String.format("<font color='%s'>", AppUtils.colorHoHex(color));
    }

    @Override
    public int getViewTypeCount() {
        return ITEM_VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).type;
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

                ViewHolder hodler = null;
                if (convertView == null) {
                    try {
                        LayoutInflater inflater = LayoutInflater.from(mContext);
                        convertView = inflater.inflate(R.layout.item_search_suggest, parent, false);
                    } catch (InflateException e) {
                        e.printStackTrace();
                    }
                    if (convertView != null) {
                        hodler = new ViewHolder();
                        hodler.iv_type = (ImageView) convertView.findViewById(R.id.iv_type);
                        hodler.tv_2 = (TextView) convertView.findViewById(R.id.tv_search_item_2);
                        convertView.setTag(hodler);
                    }
                } else {
                    hodler = (ViewHolder) convertView.getTag();
                }
                SearchCommonBeanYouHua bean = mData.get(position).commonBean;
                String type = bean.getWordtype();
                if (hodler != null) {
                    if (SearchConstant.AUTHOR_TYPE.equals(type)) {
                        hodler.iv_type.setImageResource(R.drawable.search_writer);

                    } else if (SearchConstant.LABEL_TYPE.equals(type)) {
                        hodler.iv_type.setImageResource(R.drawable.search_label);

                    } else if (SearchConstant.NAME_TYPE.equals(type)) {
                        //如果不是以上三种的话，说明返回的数据为书籍名，则通过url加载后台返回的图片URL地址（加上非空判断）
                        hodler.iv_type.setImageResource(R.drawable.search_book);
                    }

                    String content = bean.getSuggest();
                    String finalInput = "";

                    if (editInput != null) {
                        finalInput = AppUtils.deleteAllIllegalChar(editInput);
                    }

                    content = content.replaceAll(finalInput, mColorTag + finalInput + "</font>");
                    hodler.tv_2.setText(Html.fromHtml(content));
                }
                break;
            //item中间的gap显示
            case ITEM_VIEW_MARGEN_ONE:

                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.item_search_suggest_one, parent, false);
                }
                break;
            case ITEM_VIEW_MARGEN_TWO:

                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.item_search_suggest_two, parent, false);
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
        TextView tv_2;
        ImageView iv_type;
    }
    public void setEditInput(String editInput) {
        this.editInput = editInput;
    }

}
