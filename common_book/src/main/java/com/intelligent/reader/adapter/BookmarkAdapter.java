/**
 *
 */
package com.intelligent.reader.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.lzbook.kit.book.adapter.AdapterBase;
import net.lzbook.kit.data.bean.Bookmark;
import net.lzbook.kit.data.bean.EventBookmark;
import net.lzbook.kit.utils.ResourceUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


import com.intelligent.reader.R;

import de.greenrobot.event.EventBus;

/**
 * 书签适配器
 */
public class BookmarkAdapter extends AdapterBase {
    public ArrayList<Bookmark> bookmarkList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm");

    public BookmarkAdapter(Context context, List<Bookmark> list) {
        super(context, list);
        this.bookmarkList = (ArrayList<Bookmark>) list;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewCache viewCache;
        if (convertView == null) {
            convertView = getLayoutInflater().inflate(R.layout.layout_bookmark_item, parent ,false);
            viewCache = new ViewCache(convertView);
            viewCache.bookmark_divider = convertView.findViewById(R.id.bookmark_divider);
            viewCache.getImageViewDelete().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bookmark bookmark = (Bookmark) v.getTag(R.id.tag_first);
                    if (bookmark != null) {
                        EventBus.getDefault().post(new EventBookmark(EventBookmark.type_delete,bookmark));
                    }
                }
            });
            convertView.setTag(viewCache);
        } else {
            viewCache = (ViewCache) convertView.getTag();
        }
        if ("night".equals(ResourceUtil.mode)) {
            viewCache.bookmark_divider.setBackgroundColor(mContext.getResources().getColor(R.color.color_gray_3d3d3d));
        } else {
            viewCache.bookmark_divider.setBackgroundColor(mContext.getResources().getColor(R.color.color_white_ebebeb));
        }
        Bookmark bookmark = bookmarkList.get(position);
        if (bookmark == null) {
            return convertView;
        }
        viewCache.getImageViewDelete().setTag(R.id.tag_first,bookmark);

        viewCache.getChapterName().setText(TextUtils.isEmpty(bookmark.chapter_name) ? "未知" : bookmark.chapter_name);
        if ("night".equals(ResourceUtil.mode)) {
            viewCache.getChapterName().setTextColor(mContext.getResources().getColor(R.color.color_gray_797979));
        } else {
            viewCache.getChapterName().setTextColor(mContext.getResources().getColor(R.color.color_black_343434));
        }
        String content = handleBookmarkData(bookmark.chapter_content);
        viewCache.getDesc().setText(content);
        if ("night".equals(ResourceUtil.mode)) {
            viewCache.getDesc().setTextColor(mContext.getResources().getColor(R.color.color_gray_5d5d5d));
        } else {
            viewCache.getDesc().setTextColor(mContext.getResources().getColor(R.color.color_gray_6e6e6e));
        }
        long datetime = bookmark.last_time;
        viewCache.getTime().setText(dateFormat.format(datetime));
        if ("night".equals(ResourceUtil.mode)) {
            viewCache.getTime().setTextColor(mContext.getResources().getColor(R.color.color_gray_5a5959));
        } else {
            viewCache.getTime().setTextColor(mContext.getResources().getColor(R.color.color_gray_9d9d9d));
        }
        return convertView;
    }

    private String handleBookmarkData(String content) {
        // 去掉结果的标点符号；
        if (content.endsWith(",") || content.endsWith("，") || content.endsWith(".") || content.endsWith("。")
                || content.endsWith("!") || content.endsWith("！")) {
            content = content.substring(0, content.length() - 1);
        }

        // 添加省略号
        return content + "……";
    }

    class ViewCache {
        private View baseView;
        private TextView item_bookmark_title;
        private TextView item_bookmark_desc;
        private TextView item_bookmark_time;
        private ImageView item_bookmark_delete;
        private View bookmark_divider;

        public ViewCache(View baseView) {
            this.baseView = baseView;
        }

        public TextView getChapterName() {
            if (item_bookmark_title == null) {
                item_bookmark_title = (TextView) baseView.findViewById(R.id.item_bookmark_title);
            }
            return item_bookmark_title;
        }

        public TextView getDesc() {
            if (item_bookmark_desc == null) {
                item_bookmark_desc = (TextView) baseView.findViewById(R.id.item_bookmark_desc);
            }

            return item_bookmark_desc;
        }

        public TextView getTime() {
            if (item_bookmark_time == null) {
                item_bookmark_time = (TextView) baseView.findViewById(R.id.item_bookmark_time);
            }
            return item_bookmark_time;
        }

        public ImageView getImageViewDelete() {
            if (item_bookmark_delete == null) {
                item_bookmark_delete = (ImageView) baseView.findViewById(R.id.item_bookmark_delete);
            }
            return item_bookmark_delete;
        }
    }
}