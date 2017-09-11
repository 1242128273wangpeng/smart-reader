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

import com.intelligent.reader.R;

import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.search.SearchCommonBean;

import java.util.List;

/**
 * Created by Administrator on 2016/12/16 0016.
 */
public class SearchSuggestAdapter extends BaseAdapter {

    private Context mContext;
    private List<SearchCommonBean> mData;
    private String editInput;

    public SearchSuggestAdapter(Context context, List<SearchCommonBean> mData, String editInput) {
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
        SearchCommonBean bean = mData.get(position);
        String type = bean.getWordtype();
        if(type.equals("author")){
            hodler.iv_type.setImageResource(R.drawable.search_author);

        }else if(type.equals("label")){
            hodler.iv_type.setImageResource(R.drawable.search_biaoqian);
        }else if(type.equals("fenlei")){
            hodler.iv_type.setImageResource(R.drawable.search_fenlei);
        }else{
            hodler.iv_type.setImageResource(R.drawable.search_transparent);
        }
        String content = bean.getSuggest();

        content = content.replaceAll(editInput, "<font color='#87af4c'>" + editInput + "</font>");

        hodler.tv_2.setText(Html.fromHtml(content));
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
