/**
 * 
 */
package com.intelligent.reader.adapter;

import com.intelligent.reader.R;

import net.lzbook.kit.data.bean.Source;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.Tools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class SourceAdapter extends BaseAdapter {

    private Context context;
    private List<Source> data;

    public SourceAdapter(Context context, List<Source> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        Source source = data.get(position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_book_end_serial_chapter, parent, false);
            viewHolder.book_source_name = (TextView) convertView.findViewById(R.id.site_text);
            viewHolder.book_source_chapter_name = (TextView) convertView.findViewById(R.id.new_text);
            viewHolder.book_source_update_time = (TextView) convertView.findViewById(R.id.time_text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.book_source_name.setText("来源" + (position + 1) + ": " + source.host);
        viewHolder.book_source_chapter_name.setText(source.last_chapter_name);
        viewHolder.book_source_update_time.setText(Tools.compareTime(AppUtils.formatter, source.update_time));
        return convertView;
    }

    public class ViewHolder {
        public TextView book_source_name;
        public TextView book_source_chapter_name;
        public TextView book_source_update_time;
    }
}