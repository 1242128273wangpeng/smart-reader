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
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.utils.AppLog;

import java.util.ArrayList;
import java.util.List;

public abstract class RemoveModeAdapter extends BaseAdapter {
    public static final int MODE_DEFAULT = 16;
    public static final int MODE_EXCEPT_FIRST = 48;
    public static final int MODE_EXCEPT_FIRST_DOWNLOAD = 64;
    public static final int MODE_LEFT = 16;
    protected String TAG = "RemoveModeAdapter";
    RemoveAdapterChild adapter_child;
    int distanceY = -1;
    ViewHolder holder = null;
    private boolean isRemoveMode = false;
    private List<Book> listData;
    protected Context mContext;
    private int mode_default = 16;
    public ArrayList<Book> remove_checked_states;

    public interface RemoveAdapterChild {
        void setChildAdapterData(int i, ViewHolder viewHolder, View view);

        View setChildView(int i, View view, ViewHolder viewHolder);

        ViewHolder wholeHolder();
    }

    public static class ViewHolder {
        public ImageView check;
        public ViewGroup childView;
    }

    public RemoveModeAdapter(Context context, List<Book> list) {
        this.listData = list;
        this.mContext = context;
        this.remove_checked_states = new ArrayList<>();
        resetRemovedState();
        this.distanceY = (int) this.mContext.getResources().getDimension(R.dimen.dimen_view_height_50);
    }

    public List<?> getList() {
        return this.listData;
    }

    @Override
    public Book getItem(int position) {
        return this.listData.get(position);
    }

    @Override
    public int getCount() {
        return this.listData.size();
    }

    @Override
    public long getItemId(int position) {
        return (long) position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView != null) {
            this.holder = (ViewHolder) convertView.getTag();
        } else if (!(this.adapter_child == null || this.mContext == null)) {
            this.holder = this.adapter_child.wholeHolder();
            try {
                convertView = LayoutInflater.from(this.mContext).inflate(R.layout.remove_item_base, parent, false);
            } catch (InflateException e) {
                e.printStackTrace();
            }
            if (convertView != null) {
                this.holder.check = (ImageView) convertView.findViewById(R.id.check_delete);
                this.holder.childView = (ViewGroup) convertView.findViewById(R.id.rl_anim_item);
                View childView = this.adapter_child.setChildView(position, convertView, this.holder);
                this.holder.check.setVisibility(View.INVISIBLE);
                if (childView != null) {
                    this.holder.childView.addView(childView);
                }
                convertView.setTag(this.holder);
            }
        }
        if (this.remove_checked_states.contains(getItem(position))) {
            this.holder.check.setBackgroundResource(R.drawable.icon_delete_checked);
        } else {
            this.holder.check.setBackgroundResource(R.drawable.icon_delete_uncheck);
        }
        if (this.adapter_child != null) {
            this.adapter_child.setChildAdapterData(position, this.holder, this.holder.childView);
        }
        if (this.mode_default == 48 && getItemViewType(position) == 0) {
            doRemove(this.mode_default);
        }
        return convertView;
    }

    public void setAdapterChild(RemoveAdapterChild adapterChild, int mode) {
        this.adapter_child = adapterChild;
        this.mode_default = mode;
    }

    public boolean isRemoveMode() {
        return this.isRemoveMode;
    }

    public void setRemoveMode(boolean isRemoveMode) {
        this.isRemoveMode = isRemoveMode;
    }

    public void setChecked(int position) {
        AppLog.d(this.TAG, "setIsChecked position " + position);
        Book item = (Book) getItem(position);
        if (item == null) {
            return;
        }
        if (this.remove_checked_states.contains(item)) {
            this.remove_checked_states.remove(item);
        } else {
            this.remove_checked_states.add(item);
        }
    }

    public void setAllChecked(boolean checkedAll) {
        AppLog.d(this.TAG, "setAllChecked");
        int position;
        Book item;
        if (checkedAll) {
            for (position = 0; position < getCount(); position++) {
                item = (Book) getItem(position);
                if (!(item == null || this.remove_checked_states.contains(item))) {
                    this.remove_checked_states.add(item);
                }
            }
            return;
        }
        for (position = 0; position < getCount(); position++) {
            item = (Book) getItem(position);
            if (item != null && this.remove_checked_states.contains(item)) {
                this.remove_checked_states.remove(item);
            }
        }
    }

    public void doRemove(int mode) {
        int dx = (int) this.mContext.getResources().getDimension(R.dimen.dimen_padding_50);
        if (this.isRemoveMode) {
            if (mode == 16) {
                this.holder.childView.scrollTo(-dx, 0);
            } else if (mode == 48) {
                this.holder.childView.scrollTo(-dx, 0);
            } else if (mode == 64) {
                this.holder.childView.scrollTo(-dx, 0);
            } else {
                this.holder.childView.scrollTo(dx, 0);
            }
            this.holder.check.setVisibility(View.VISIBLE);
            return;
        }
        this.holder.childView.scrollTo(0, 0);
        this.holder.check.setVisibility(View.INVISIBLE);
    }

    public void setListPadding(ListView listview, boolean isShowing) {
        int item_height;
        int headViewCount = listview.getHeaderViewsCount();
        int itemCount = listview.getCount() - headViewCount;
        int listViewHeight = listview.getHeight();
        for (int i = 0; i < headViewCount; i++) {
            listViewHeight -= listview.getChildAt(i).getHeight();
        }
        View view = null;
        if (itemCount > 0) {
            view = listview.getChildAt(headViewCount + 1);
        }
        if (view == null) {
            item_height = 0;
        } else {
            item_height = view.getHeight();
        }
        if (listViewHeight > (itemCount * item_height) + this.distanceY) {
            listview.setPadding(0, listview.getListPaddingTop(), 0, 0);
        } else if (isShowing) {
            listview.setPadding(0, listview.getListPaddingTop(), 0, this.distanceY + 10);
        } else {
            listview.setPadding(0, listview.getListPaddingTop(), 0, 0);
        }
    }

    public void resetRemovedState() {
        this.remove_checked_states.clear();
    }

    public int getCheckedSize() {
        AppLog.d(this.TAG, "remove_checked_states.size() " + this.remove_checked_states.size());
        return this.remove_checked_states.size();
    }
}
