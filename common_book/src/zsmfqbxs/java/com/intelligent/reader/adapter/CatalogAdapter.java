package com.intelligent.reader.adapter;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ding.basic.bean.Chapter;
import com.intelligent.reader.R;
import com.intelligent.reader.activity.CataloguesActivity;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 目录页面适配器
 */
public class CatalogAdapter extends BaseAdapter {
    private int selectedItem;
    private Resources resources;
    private String already_cached;
    private String book_site;//书籍的来源站
    private CataloguesActivity context;
    private List<Chapter> list;
    private int textColor;

    private WeakReference<CataloguesActivity> activityWeakReference;


    public CatalogAdapter(CataloguesActivity activity, List list, String book_site) {
        resources = activity.getResources();
        activityWeakReference = new WeakReference<>(activity);
        if (activityWeakReference != null) {
            context = activityWeakReference.get();
        }

        this.list = list;
        this.book_site = book_site;
        resources = context.getResources();
        already_cached = resources.getString(R.string.already_cached);
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    public List<Chapter> getList() {
        return list;
    }

    public void setList(List<Chapter> list) {
        this.list = list;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewCache viewCache;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_content_catalog, parent, false);
            viewCache = new ViewCache(convertView);
            viewCache.catalog_chapter_divider = convertView.findViewById(R.id.catalog_chapter_divider);
            convertView.setTag(viewCache);
        } else {
            viewCache = (ViewCache) convertView.getTag();
        }
        if (position + 1 > list.size()) {
            return convertView;
        }
        Chapter chapter = list.get(position);
        String text = (chapter.getSequence() + 1) + " " + chapter.getName();
        viewCache.getChapterName().setText(text);
        boolean chapterExist = false;
       /* if (book_site.equals(Constants.QG_SOURCE)) {
            chapterExist = DataCache.isChapterExists(chapter.getChapter_id(), chapter.getBook_id());
        } else {
            chapterExist = BookHelper.isChapterExist(chapter);
        }*/

        if (chapter.getSequence() == selectedItem) {
            textColor = R.color.primary;
            viewCache.getChapterName().setTextColor(resources.getColor(textColor));
        } else {
            if (chapterExist) {
                textColor = R.color.directory_chapter_text_color;
                viewCache.getChapterName().setTextColor(resources.getColor(textColor));
            } else {
                textColor = R.color.directory_uncached_chapter_text_color;
                viewCache.getChapterName().setTextColor(resources.getColor(textColor));
            }
        }
        return convertView;
    }

    public void setSelectedItem(int position) {
        if (position >= list.size()) {
            position = list.size() - 1;
        }
        selectedItem = position;
    }

    public void recycleResource() {

        if (this.resources != null) {
            this.resources = null;
        }

        if (this.context != null) {
            this.context = null;
        }
    }

    class ViewCache {
        private View baseView;
        private TextView textView;
//        private TextView has_cache;
        private View catalog_chapter_divider;

        public ViewCache(View baseView) {
            this.baseView = baseView;
        }

        public TextView getChapterName() {
            if (textView == null) {
                textView = (TextView) baseView.findViewById(R.id.catalog_chapter_name);
            }
            return textView;
        }

//        public TextView getHasCache() {
//            if (has_cache == null) {
//                has_cache = (TextView) baseView.findViewById(R.id.catalog_chapter_cache);
//            }
//
//            return has_cache;
//        }
    }
}