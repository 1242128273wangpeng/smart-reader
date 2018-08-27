package com.intelligent.reader.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.util.TypedValue;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ding.basic.bean.SearchCommonBeanYouHua;
import com.intelligent.reader.R;

import net.lzbook.kit.utils.AppUtils;

import java.util.List;

/**
 * Created by Administrator on 2016/12/16 0016.
 */
public class SearchSuggestAdapter extends BaseAdapter {

    private Context mContext;
    private List<Object> mData;
    private String editInput = "";
    String mColorTag;
    //item的类型
    private static final int ITEM_VIEW_TYPE_DATA = 0;
    private static final int ITEM_VIEW_TYPE_GAP = 1;
    private static final int ITEM_VIEW_TYPE_COUNT = 2;

    public SearchSuggestAdapter(Context context, List<Object> mData, String editInput) {
        this.mContext = context;
        this.mData = mData;
        this.editInput = editInput;
        TypedValue typeColor = new TypedValue();
        Resources.Theme theme = mContext.getTheme();
//        theme.resolveAttribute(R.attr.dialog_recommend, typeColor, true);
        int color = mContext.getResources().getColor(R.color.primary);
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
                        hodler.img_book_cover = (ImageView) convertView.findViewById(
                                R.id.img_book_cover);
                        hodler.img_type = (ImageView) convertView.findViewById(R.id.img_type);
                        hodler.txt_name = (TextView) convertView.findViewById(R.id.txt_name);
                        hodler.img_shadow = (ImageView) convertView.findViewById(R.id.img_shadow);
                        convertView.setTag(hodler);
                    }
                } else {
                    hodler = (ViewHolder) convertView.getTag();
                }
                SearchCommonBeanYouHua bean = (SearchCommonBeanYouHua) mData.get(position);
                String type = bean.getWordtype();
                if (hodler != null) {
                    if (type.equals("author")) {
                        hodler.img_shadow.setVisibility(View.GONE);
                        hodler.img_book_cover.setImageResource(R.drawable.search_author_icon);
                        hodler.img_type.setImageResource(R.drawable.search_writer);

                    } else if (type.equals("label")) {
                        hodler.img_shadow.setVisibility(View.GONE);
                        hodler.img_book_cover.setImageResource(R.drawable.search_label_icon);
                        hodler.img_type.setImageResource(R.drawable.search_label);

                    } else if (type.equals("name")) {
                        hodler.img_shadow.setVisibility(View.VISIBLE);
                        //如果不是以上三种的话，说明返回的数据为书籍名，则通过url加载后台返回的图片URL地址（加上非空判断）
                        if (bean != null) {
                            Glide.with(mContext).load(bean.getImage_url()).placeholder(
                                    R.drawable.bookshelf_book_cover_default).error(
                                    (R.drawable.bookshelf_book_cover_default)).into(
                                    hodler.img_book_cover);
                        }
                        hodler.img_type.setImageResource(R.drawable.search_book_icon);
                    }

                    String content = bean.getSuggest();
                    String finalInput = "";

                    if (editInput != null) {
                        finalInput = AppUtils.deleteAllIllegalChar(editInput);
                    }

                    content = content.replaceAll(finalInput, mColorTag + finalInput + "</font>");
                    hodler.txt_name.setText(Html.fromHtml(content));
                }
                break;
            //item中间的gap显示
            case ITEM_VIEW_TYPE_GAP:

                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(
                            R.layout.item_search_divider, parent, false);
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
        ImageView img_book_cover;
        TextView txt_name;
        ImageView img_type;
        ImageView img_shadow;
    }

    public void setEditInput(String editInput) {
        this.editInput = editInput;
    }
}
