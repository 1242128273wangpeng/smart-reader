package com.intelligent.reader.search;

import android.content.Context;
import android.text.Html;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ding.basic.bean.SearchCommonBeanYouHua;
import com.intelligent.reader.R;

import net.lzbook.kit.utils.AppUtils;

import java.util.List;

/**
 * Function：搜索自动补全子条目
 *
 * Created by JoannChen on 2018/5/30 0030 17:28
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
public class SearchSuggestAdapter extends BaseAdapter {


    private Context mContext;
    private List<Object> mData;
    private String editInput = "";
    //item的类型
    private static final int ITEM_VIEW_TYPE_DATA = 0;
    private static final int ITEM_VIEW_TYPE_GAP = 1;
    private static final int ITEM_VIEW_TYPE_COUNT = 2;
    private ViewHolder holder = null;

    SearchSuggestAdapter(Context context, List<Object> mData, String editInput) {
        this.mContext = context;
        this.mData = mData;
        this.editInput = editInput;

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
        //判断view的type，通过type判断item是显示数据还是Title
        switch (getItemViewType(position)) {
            //数据填充item
            case ITEM_VIEW_TYPE_DATA:

                if (convertView == null) {
                    try {
                        LayoutInflater inflater = LayoutInflater.from(mContext);
                        convertView = inflater.inflate(R.layout.item_search_suggest, parent, false);
                    } catch (InflateException e) {
                        e.printStackTrace();
                    }
                    if (convertView != null) {
                        holder = new ViewHolder();
                        holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
                        holder.iv_type = (ImageView) convertView.findViewById(R.id.iv_type);
                        holder.tv_2 = (TextView) convertView.findViewById(R.id.tv_search_item_2);
                        holder.iv_shadow = (ImageView) convertView.findViewById(R.id.iv_shadow);
                        holder.rl_book = (RelativeLayout) convertView.findViewById(R.id.rl_book);
                        holder.tv_book_name = (TextView) convertView.findViewById(
                                R.id.tv_book_name);
                        holder.tv_author = (TextView) convertView.findViewById(R.id.tv_author);
                        convertView.setTag(holder);
                    }
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }


                SearchCommonBeanYouHua bean = (SearchCommonBeanYouHua) mData.get(position);

                String content = bean.getSuggest();
                String finalInput = "";

                if (editInput != null) {
                    finalInput = AppUtils.deleteAllIllegalChar(editInput);
                }
                content = content.replaceAll(finalInput,
                        "<font color='#FFBA01'>" + finalInput + "</font>");


                String type = bean.getWordtype();
                if (holder != null) {
                    switch (type) {
                        case "author":
                            isShowBook(false);
                            holder.tv_2.setText(Html.fromHtml(content));
                            break;
                        case "label":
                            isShowBook(false);
                            holder.tv_2.setText(Html.fromHtml(content));
                            break;
                        case "name":
                            isShowBook(true);
                            holder.tv_book_name.setText(Html.fromHtml(content));
                            holder.tv_author.setText(bean.getAuthor());
                            //如果不是以上三种的话，说明返回的数据为书籍名，则通过url加载后台返回的图片URL地址（加上非空判断）
                            if (bean.getImage_url() != null) {
                                Glide.with(mContext).load(bean.getImage_url()).placeholder(
                                        R.mipmap.bookshelf_book_cover_default).error(
                                        (R.mipmap.bookshelf_book_cover_default)).into(holder.iv_icon);
                            }
                            break;
                    }


                }
                break;
            //item中间的gap显示
            case ITEM_VIEW_TYPE_GAP:

                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(
                            R.layout.item_search_history_gap, parent, false);
                    TextView descText = (TextView) convertView.findViewById(R.id.tv_desc);

                    if ((position + 1) < mData.size()) {
                        String type1 = ((SearchCommonBeanYouHua) mData.get(
                                position + 1)).getWordtype();

                        switch (type1) {
                            case "author":
                                descText.setText("作者");
                                break;
                            case "name":
                                descText.setText("图书");
                                break;
                            case "label":
                                descText.setText("标签");
                                break;
                        }
                    }

                }
                break;
            default:
                break;
        }

        return convertView;
    }


    /**
     * 展示图书 or 作者、标签
     *
     * @param isBook true 图书
     */
    private void isShowBook(boolean isBook) {
        holder.rl_book.setVisibility(isBook ? View.VISIBLE : View.GONE);
        holder.tv_2.setVisibility(isBook ? View.GONE : View.VISIBLE);
    }

    public void clear() {
        if (mData != null) {
            mData.clear();
        }
    }

    private static class ViewHolder {
        RelativeLayout rl_book;
        ImageView iv_icon;
        TextView tv_2;
        TextView tv_book_name, tv_author;
        ImageView iv_type;
        ImageView iv_shadow;
    }

    public void setEditInput(String editInput) {
        this.editInput = editInput;
    }

}
