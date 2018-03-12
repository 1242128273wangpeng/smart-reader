package com.intelligent.reader.widget;

import com.intelligent.reader.R;
import com.intelligent.reader.adapter.SourceAdapter;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.data.bean.Source;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lijun Lee
 * @desc 换源窗口
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/2/28 15:37
 */

public class ChangeSourcePopWindow extends PopupWindow {

    private View mPopInflater;

    private ChangeSourcePopWindow(View inflate, int matchParent, int wrapContent, ChangeSourcePopWindow.Builder builder) {
        super(inflate, matchParent, wrapContent);
        mPopInflater = builder.popInflater;
    }

    public static Builder newBuilder(Context context) {
        return new Builder(context);
    }

    public void show() {
        showAtLocation(mPopInflater, Gravity.BOTTOM, 0, 0);
    }

    public static final class Builder {

        Context context;

        List<Source> sourceList;

        View popInflater;

        OnSourceItemClickListener onSourceItemClickListener;

        private Builder(Context context) {
            this.context = context;
        }

        public ChangeSourcePopWindow.Builder setSourceData(List<Source> sourceList) {
            this.sourceList = sourceList;
            return this;
        }

        public ChangeSourcePopWindow.Builder setOnSourceItemClick(OnSourceItemClickListener onSourceItemClickListener) {
            this.onSourceItemClickListener = onSourceItemClickListener;
            return this;
        }

        public ChangeSourcePopWindow build() {
            View popupView = LayoutInflater.from(context).inflate(R.layout.change_source_pop_layout, null);
            this.popInflater = popupView;
            final ChangeSourcePopWindow popupWindow = new ChangeSourcePopWindow(popupView,
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, this);
            popupWindow.setFocusable(true);
            popupWindow.setTouchable(true);
            popupWindow.setOutsideTouchable(false);

            ListView changeSourceList = (ListView) popupView.findViewById(R.id.change_source_list);
            TextView cleanTv = (TextView) popupView.findViewById(R.id.change_source_original_web);

            final SourceAdapter sourceListAdapter = new SourceAdapter(context, sourceList);
            changeSourceList.setAdapter(sourceListAdapter);
            changeSourceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Source source = (Source) sourceListAdapter.getItem(position);
                    if (onSourceItemClickListener != null) {
                        onSourceItemClickListener.onSourceItemClick(source);
                    }
                    popupWindow.dismiss();
                }
            });
            cleanTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, String> map1 = new HashMap<String, String>();
                    map1.put("type", "1");
                    StartLogClickUtil.upLoadEventLog(context, StartLogClickUtil.READPAGEMORE_PAGE, StartLogClickUtil.READ_SOURCECHANGE, map1);
                    popupWindow.dismiss();
                }
            });
            return popupWindow;
        }
    }

    public interface OnSourceItemClickListener {
        void onSourceItemClick(Source source);
    }
}
