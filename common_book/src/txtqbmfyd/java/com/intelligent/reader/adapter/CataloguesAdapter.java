package com.intelligent.reader.adapter;

import com.intelligent.reader.R;
import com.quduquxie.network.DataCache;

import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Chapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * @author lijun Lee
 * @desc 书籍目录
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/3/16 11:01
 */

public class CataloguesAdapter extends RecyclerView.Adapter<CataloguesAdapter.ViewHolder> {

    private List<Chapter> mList;

    private int mSelectedItem;

    private Context mContext;

    private OnChapterItemClickListener mOnChapterItemClickListener;

    public CataloguesAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.content_catalog_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Chapter chapter = mList.get(position);

        boolean chapterExist;
        if (Constants.QG_SOURCE.equals(chapter.site)) {
            chapterExist = DataCache.isChapterExists(chapter.chapter_id, chapter.book_id);
        } else {
            chapterExist = BookHelper.isChapterExist(chapter);
        }

        if (chapterExist) {
            holder.chapterCacheTv.setVisibility(View.VISIBLE);
        } else {
            holder.chapterCacheTv.setVisibility(View.GONE);
        }

        if (chapter.sequence == mSelectedItem) {
            holder.chapterNameTv.setTextColor(mContext.getResources().getColor(R.color.dialog_recommend));
        } else {
            if (chapterExist) {
                holder.chapterNameTv.setTextColor(mContext.getResources().getColor(R.color.text_color_dark));
            } else {
                holder.chapterNameTv.setTextColor(mContext.getResources().getColor(R.color.text_color_light));
            }
        }
        holder.chapterNameTv.setText(chapter.chapter_name);
        holder.itemView.setOnClickListener(new OnChapterClickListener(position, chapter));
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public void setData(List<Chapter> data) {
        this.mList = data;
        notifyDataSetChanged();
    }

    public void setSelectedItem(int position) {
        if (mList == null) {
            mSelectedItem = 0;
            return;
        }

        if (position >= mList.size()) {
            position = mList.size() - 1;
        }
        mSelectedItem = position;
    }

    private class OnChapterClickListener implements View.OnClickListener {

        private Chapter chapter;

        private int position;

        OnChapterClickListener(int position, Chapter chapter) {
            this.chapter = chapter;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (mOnChapterItemClickListener != null) {
                mOnChapterItemClickListener.onChapterClick(position, chapter);
            }
        }
    }

    public interface OnChapterItemClickListener {
        void onChapterClick(int position, Chapter chapter);
    }

    public void setOnChapterItemClickListener(OnChapterItemClickListener onChapterItemClickListener) {
        mOnChapterItemClickListener = onChapterItemClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView chapterNameTv;

        TextView chapterCacheTv;

        public ViewHolder(View itemView) {
            super(itemView);
            chapterNameTv = (TextView) itemView.findViewById(R.id.catalog_chapter_name);
            chapterCacheTv = (TextView) itemView.findViewById(R.id.catalog_chapter_cache);
        }
    }
}
