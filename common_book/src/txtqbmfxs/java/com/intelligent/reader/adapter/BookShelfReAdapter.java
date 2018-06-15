package com.intelligent.reader.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import com.intelligent.reader.R;
import com.intelligent.reader.adapter.holder.AbsRecyclerViewHolder;

import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BookShelfReAdapter extends RecyclerView.Adapter<AbsRecyclerViewHolder<Book>> {

    private boolean isList;
    public HashSet<Integer> remove_checked_states;
    protected Activity context;
    private int distanceY = -1;
    private boolean isRemoveMode = false;
    private ArrayList<String> update_table;
    private ArrayList<String> down_table;
    private ShelfItemClickListener shelfItemClickListener;
    private ShelfItemLongClickListener shelfItemLongClickListener;
    public ArrayList<Book> bookList;
    private List<ViewGroup> adList;

    /**
     * 书架构造函数
     * @param context 上下文
     * @param bookList 书籍列表
     * @param adList 广告列表
     * @param isList true ? ListView : GridView
     * @param itemClick 子条目点击事件
     * @param itemLongClick 子条目长按事件
     */
    public BookShelfReAdapter(Activity context,
                              List<Book> bookList, List<ViewGroup> adList,
                              boolean isList,
                              ShelfItemClickListener itemClick,
                              ShelfItemLongClickListener itemLongClick) {
        this.context = context;
        this.bookList = (ArrayList<Book>) bookList;
        this.adList = adList;
        this.isList = isList;
        shelfItemClickListener = itemClick;
        shelfItemLongClickListener = itemLongClick;

        update_table = new ArrayList<>();
        down_table = new ArrayList<>();
        remove_checked_states = new HashSet<>();
        resetRemovedState();
        distanceY = (int) this.context.getResources().getDimension(net.lzbook.kit.R.dimen.dimen_view_height_default);
    }

    @Override
    public AbsRecyclerViewHolder<Book> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        AbsRecyclerViewHolder<Book> holder = null;
        switch (viewType) {
            case 0:// 书架view
                int layoutId = isList ? R.layout.layout_bookshelf_item_list : R.layout.layout_bookshelf_item_list;
                view = LayoutInflater.from(context).inflate(layoutId, parent, false);
                holder = new BookShelfItemHolder(view, shelfItemClickListener, shelfItemLongClickListener);
                break;
            case 1:// 广告view
                view = LayoutInflater.from(context).inflate(R.layout.ad_item_small_layout, parent, false);//列表样式
//              view = LayoutInflater.from(context).inflate(R.layout.layout_bookshelf_item_list_ad, parent, false); //修改广告显示样式为九宫格
                holder = new ADViewHolder(view);
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(AbsRecyclerViewHolder<Book> holder, int position) {
        if (holder == null && bookList == null) return;
        Book book = bookList.get(position);
        switch (getItemViewType(position)) {
            case 0:
                if (holder == null) {
                    return;
                }
                holder.onBindData(position, book, update_table.contains(book.book_id),
                        isRemoveMode(), remove_checked_states.contains(position));

                break;
            case 1:
                if (holder instanceof ADViewHolder) {
                    View adView = getAdView(book);
                    if (adView != null) {
                        ViewParent parent = adView.getParent();
                        if (parent != null && parent instanceof RelativeLayout) {
                            ((RelativeLayout) parent).removeAllViews();
                        }
                        ((ADViewHolder) holder).item_ad_layout.removeAllViews();
                        ((ADViewHolder) holder).item_ad_layout.addView(adView);
                    }
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (bookList != null) {
            return bookList.size();
        }
        return 0;
    }

    private View getAdView(Book book) {
        if (adList == null || adList.isEmpty() || book == null) {
            return null;
        }
        if (book.sequence < adList.size()) {
            return adList.get(book.sequence);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (bookList != null && position >= 0 && position <= bookList.size() - 1) {
            Book book = bookList.get(position);
            if (book != null) {
                if (book.book_type == 0) {
                    return 0;
                } else if (book.book_type == -2) {
                    return 1;
                }
            }
        }
        return -1;
    }

    public void setUpdate_table(ArrayList<String> update_table) {
        this.update_table = update_table;
    }

    public void setBookDownLoad(ArrayList<String> download_table) {
        this.down_table = download_table;
    }

    public void resetRemovedState() {
        remove_checked_states.clear();
    }

    public boolean isRemoveMode() {
        return isRemoveMode;
    }

    public void setRemoveMode(boolean isRemoveMode) {
        this.isRemoveMode = isRemoveMode;
    }

    public void setChecked(int position) {
        if (!remove_checked_states.contains(position)) {
            remove_checked_states.add(position);
        } else {
            remove_checked_states.remove(position);
        }
    }

    public void setAllChecked(boolean checkedAll) {
        if (checkedAll) {
            for (int position = 0; position < bookList.size(); position++) {
                if (!remove_checked_states.contains(position)) {
                    remove_checked_states.add(position);
                }
            }
        } else {
            for (int position = 0; position < bookList.size(); position++) {
                if (remove_checked_states.contains(position)) {
                    remove_checked_states.remove(position);
                }
            }
        }
    }

    public void setListPadding(SuperSwipeRefreshLayout Layout, boolean isShowing) {
        if (isShowing) {
            int height = distanceY;
            Layout.setPadding(0, 0, 0, height);
        } else {
            Layout.setPadding(0, 0, 0, 0);
        }
    }

    public int getCheckedSize() {
        if (remove_checked_states != null) {
            return remove_checked_states.size();
        }
        return 0;
    }

    public interface ShelfItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface ShelfItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    class ADViewHolder extends AbsRecyclerViewHolder<Book> {
        RelativeLayout item_ad_layout;


        public ADViewHolder(View itemView) {
            super(itemView, null, null);
            item_ad_layout = (RelativeLayout) itemView.findViewById(R.id.item_ad_layout);
//            item_ad_layout = (RelativeLayout) itemView.findViewById(R.id.book_shelf_item_ad);
        }

        @Override
        public void onBindData(int position, Book data, boolean update, boolean isRemoveMode, boolean removeMark) {

        }
    }
}
