/**
 *
 */
package com.intelligent.reader.adapter;

import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ding.basic.bean.Chapter;
import com.ding.basic.util.DataCache;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.content_catalog_item, parent, false);
            viewCache = new ViewCache(convertView);
            convertView.setTag(viewCache);
        } else {
            viewCache = (ViewCache) convertView.getTag();
        }
        if (position + 1 > list.size()) {
            return convertView;
        }
        Chapter chapter = list.get(position);
        viewCache.getChapterName().setText(chapter.getName());
        boolean chapterExist;

        chapterExist = DataCache.isChapterCached(chapter);
        if (chapterExist) {
            viewCache.getHasCache().setText(already_cached);
            viewCache.getHasCache().setVisibility(View.VISIBLE);
            viewCache.getChapterName().setTextColor(Color.YELLOW);
        } else {
            viewCache.getHasCache().setVisibility(View.GONE);
        }

        if (chapter.getSequence() == selectedItem) {
            textColor = R.color.dialog_recommend;
            viewCache.getChapterName().setTextColor(resources.getColor(textColor));
        } else {
            if (chapterExist) {
                textColor = R.color.text_color_dark;
                viewCache.getChapterName().setTextColor(resources.getColor(textColor));
            } else {
                textColor = R.color.text_color_light;
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

    class ViewCache {
        private View baseView;
        private TextView textView;
        private TextView has_cache;

        public ViewCache(View baseView) {
            this.baseView = baseView;
        }

        public TextView getChapterName() {
            if (textView == null) {
                textView = baseView.findViewById(R.id.catalog_chapter_name);
            }
            return textView;
        }

        public TextView getHasCache() {
            if (has_cache == null) {
                has_cache = baseView.findViewById(R.id.catalog_chapter_cache);
            }

            return has_cache;
        }
    }
}