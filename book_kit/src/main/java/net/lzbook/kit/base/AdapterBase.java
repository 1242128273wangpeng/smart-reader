package net.lzbook.kit.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * AdapterBase.java
 * 适配器基类
 */
public abstract class AdapterBase extends BaseAdapter {

    protected Context mContext;
    private List mList;
    private LayoutInflater mLayoutInflater;

    public AdapterBase(Context context, List list) {
        mContext = context;
        mList = list;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public int getCount() {
        return mList.size();
    }

    public Object getItem(int pPosition) {
        return mList.get(pPosition);
    }

    public long getItemId(int pPosition) {
        return pPosition;
    }

    public LayoutInflater getLayoutInflater() {
        return mLayoutInflater;
    }
}
