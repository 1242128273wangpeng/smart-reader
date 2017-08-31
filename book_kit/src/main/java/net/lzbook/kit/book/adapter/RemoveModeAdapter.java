package net.lzbook.kit.book.adapter;

import android.content.Context;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import net.lzbook.kit.R;
import net.lzbook.kit.utils.AppLog;

import java.util.HashSet;
import java.util.List;

public abstract class RemoveModeAdapter extends BaseAdapter {

    protected String TAG = "RemoveModeAdapter";
    private boolean isRemoveMode = false;//
    public HashSet<Integer> remove_checked_states;
    private List<?> listData;
    int distanceY = -1;
    RemoveAdapterChild adapter_child;
    public final static int MODE_LEFT = 0x10;
    public final static int MODE_EXCEPT_FIRST = 0x30;
    public final static int MODE_EXCEPT_FIRST_DOWNLOAD = 0x40;
    public final static int MODE_DEFAULT = MODE_LEFT;
    private int mode_default = MODE_DEFAULT;
    ViewHolder holder = null;
    protected Context mContext;

    public List<?> getList() {
        return listData;
    }

    public RemoveModeAdapter(Context context, List<?> list) {
        listData = list;
        mContext = context;
        remove_checked_states = new HashSet<>();
        resetRemovedState();
        distanceY = (int) mContext.getResources().getDimension(R.dimen.dimen_view_height_50);
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View childView;
        if (convertView == null) {
            if (adapter_child != null && mContext != null) {
                holder = adapter_child.wholeHolder();
                try {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.remove_item_base, parent, false);
                } catch (InflateException e) {
                    e.printStackTrace();
                }
                if (convertView != null) {
                    holder.check = (ImageView) convertView.findViewById(R.id.check_delete);
                    holder.childView = (ViewGroup) convertView.findViewById(R.id.rl_anim_item);
                    childView = adapter_child.setChildView(position, convertView, holder);
                    holder.check.setVisibility(View.INVISIBLE);
                    if (childView != null) {
                        holder.childView.addView(childView);
                    }
                    convertView.setTag(holder);
                }
            }
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (remove_checked_states.contains(position)) {
            holder.check.setBackgroundResource(R.drawable.icon_delete_checked);
        } else {
            holder.check.setBackgroundResource(R.drawable.icon_delete_uncheck);
        }
        if (adapter_child != null) {
            adapter_child.setChildAdapterData(position, holder, holder.childView);
        }
        if (mode_default == MODE_EXCEPT_FIRST) {
            if (getItemViewType(position) != 0) {

            } else {
                doRemove(mode_default);
            }
        }
        return convertView;
    }

    public void setAdapterChild(RemoveAdapterChild adapterChild, int mode) {
        adapter_child = adapterChild;
        this.mode_default = mode;
    }

    public interface RemoveAdapterChild {

        View setChildView(int position, View convertView, ViewHolder holder);

        ViewHolder wholeHolder();

        void setChildAdapterData(int position, ViewHolder holder, View childView);
    }

    public static class ViewHolder {
        public ImageView check;
        public ViewGroup childView;
    }

    public void setRemoveMode(boolean isRemoveMode) {
        this.isRemoveMode = isRemoveMode;
    }

    public boolean isRemoveMode() {
        return isRemoveMode;
    }

    public void setChecked(int position) {
        AppLog.d(TAG, "setIsChecked position " + position);
        if (!remove_checked_states.contains(position)) {
            remove_checked_states.add(position);
        } else {
            remove_checked_states.remove(position);
        }
    }

    public void setAllChecked(boolean checkedAll){
        AppLog.d(TAG, "setAllChecked");
        if (checkedAll) {
            for (int position = 0; position < listData.size(); position++) {
                if (!remove_checked_states.contains(position)) {
                    remove_checked_states.add(position);
                }
            }
        }else {
            for (int position = 0; position < listData.size(); position++) {
                if (remove_checked_states.contains(position)) {
                    remove_checked_states.remove(position);
                }
            }
        }
    }

    public void doRemove(int mode) {
        int dx = (int) mContext.getResources().getDimension(R.dimen.dimen_padding_50);
        if (isRemoveMode) {// FIXME
            if (mode == MODE_LEFT) {
                holder.childView.scrollTo(-dx, 0);
            } else if (mode == MODE_EXCEPT_FIRST) {
                holder.childView.scrollTo(-dx, 0);
            } else if (mode == MODE_EXCEPT_FIRST_DOWNLOAD) {
                holder.childView.scrollTo(-dx, 0);
            } else {
                holder.childView.scrollTo(dx, 0);
            }
            holder.check.setVisibility(View.VISIBLE);
        } else {
            holder.childView.scrollTo(0, 0);
            holder.check.setVisibility(View.INVISIBLE);
        }
    }

    public void setListPadding(ListView listview, boolean isShowing) {
        int headViewCount = listview.getHeaderViewsCount();
        int listViewCount = listview.getCount();
        int itemCount = listViewCount - headViewCount;
        int listViewHeight = listview.getHeight();
        for (int i = 0; i < headViewCount; i++) {
            listViewHeight -= listview.getChildAt(i).getHeight();
        }
        View view = null;
        if (itemCount > 0) {
            view = listview.getChildAt(headViewCount + 1);
        }
        int item_height;
        if (view == null) {
            item_height = 0;
        } else {
            item_height = view.getHeight();
        }
        if (listViewHeight <= (itemCount * item_height + distanceY)) {

            if (isShowing) {
                int height = distanceY + 10;
                listview.setPadding(0, 0, 0, height);
            } else {
                listview.setPadding(0, 0, 0, 0);
            }
        } else {
            listview.setPadding(0, 0, 0, 0);
        }
    }

    public void resetRemovedState() {
        remove_checked_states.clear();
    }

    public int getCheckedSize() {
        AppLog.d(TAG, "remove_checked_states.size() " + remove_checked_states.size());
        return remove_checked_states.size();
    }
}