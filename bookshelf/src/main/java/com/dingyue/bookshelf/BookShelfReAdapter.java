package com.dingyue.bookshelf;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BookShelfReAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public HashSet<Integer> remove_checked_states;
    protected Activity mContext;
    private int distanceY = -1;
    private boolean isRemoveMode = false;
    private ArrayList<Book> book_list;
    private ArrayList<String> update_table;
    private ArrayList<String> down_table;
    private ShelfItemClickListener shelfItemClickListener;
    private ShelfItemLongClickListener shelfItemLongClickListener;
    private ViewGroup parentView;
    private List<ViewGroup> mAdViews;

    private static final int TYPE_BOOK = 0;
    private static final int TYPE_AD = 1;
    private static final int TYPE_ADD = 2;

    public BookShelfReAdapter(Activity context, List<Book> list, List<ViewGroup> adViews,
                              ShelfItemClickListener itemClick, ShelfItemLongClickListener itemLongClick) {
        mContext = context;
        book_list = (ArrayList<Book>) list;
        mAdViews = adViews;
        shelfItemClickListener = itemClick;
        shelfItemLongClickListener = itemLongClick;
        update_table = new ArrayList<>();
        down_table = new ArrayList<>();
        remove_checked_states = new HashSet<>();
        resetRemovedState();

        distanceY = (int) mContext.getResources().getDimension(R.dimen.dimen_view_height_50);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder holder = null;
        switch (viewType) {
            case TYPE_BOOK:
                view = LayoutInflater.from(mContext).inflate(R.layout.layout_bookshelf_item_grid, parent, false);

                holder = new BookShelfItemHolder(view, shelfItemClickListener,
                        shelfItemLongClickListener);
                break;
            case TYPE_AD:
                view = LayoutInflater.from(mContext).inflate(R.layout.layout_bookshelf_item_list_ad, parent, false);
                holder = new ADViewHolder(view);
                break;
            case TYPE_ADD:
                view = LayoutInflater.from(mContext).inflate(R.layout.item_bookshelf_add, parent, false);
                holder = new AddViewHolder(view, shelfItemClickListener);
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder == null && book_list == null) return;
        Book book;
        switch (getItemViewType(position)) {
            case TYPE_BOOK:
                book = book_list.get(position);
                if (holder instanceof BookShelfItemHolder) {
                    ((BookShelfItemHolder) holder).onBindData(position, book, update_table.contains(book.book_id),
                            isRemoveMode(), remove_checked_states.contains(position));
                }
                break;
            case TYPE_AD:
                book = book_list.get(position);
                if (holder instanceof ADViewHolder) {
                    View adView = getAdView(book);
                    if (adView != null) {
                        ViewParent parent = adView.getParent();
                        if (parent != null && parent instanceof RelativeLayout) {
                            ((RelativeLayout) parent).removeAllViews();
                        }
                        ((ADViewHolder) holder).book_shelf_item_ad.removeAllViews();
                        ((ADViewHolder) holder).book_shelf_item_ad.addView(adView);
                    }
                }
                break;
            case TYPE_ADD:
                if (holder instanceof AddViewHolder) {
                    if (isRemoveMode) {
                        ((AddViewHolder) holder).rl_add.setVisibility(View.INVISIBLE);
                    } else {
                        ((AddViewHolder) holder).rl_add.setVisibility(View.VISIBLE);
                    }
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        if ((book_list != null && book_list.size() > 0)) {
            if (book_list.size() >= 50) {
                return book_list.size();
            } else {
                return book_list.size() + 1;
            }
        }
        return 0;
    }

    private View getAdView(Book book) {
        if (mAdViews == null || mAdViews.isEmpty() || book == null) {
            return null;
        }
        if (book.sequence < mAdViews.size()) {
            return mAdViews.get(book.sequence);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (book_list != null && position >= 0) {
            if (position < book_list.size()) {
                Book book = book_list.get(position);
                if (book != null) {
                    if (book.book_type == 0) {
                        return TYPE_BOOK;
                    } else if (book.book_type == -2) {
                        return TYPE_AD;
                    }
                }
            } else if (position == book_list.size()) {
                return TYPE_ADD;
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
            for (int position = 0; position < book_list.size(); position++) {
                if (!remove_checked_states.contains(position)) {
                    remove_checked_states.add(position);
                }
            }
        } else {
            for (int position = 0; position < book_list.size(); position++) {
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

    class ADViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout book_shelf_item_ad;

        ADViewHolder(View itemView) {
            super(itemView);
            book_shelf_item_ad = (RelativeLayout) itemView.findViewById(R.id.book_shelf_item_ad);
        }
    }

    class AddViewHolder extends AbsRecyclerViewHolder<Book> {
        RelativeLayout rl_add;

        AddViewHolder(View itemView, ShelfItemClickListener shelfItemClickListener) {
            super(itemView, shelfItemClickListener, null);
            rl_add = (RelativeLayout) itemView.findViewById(R.id.rl_add);
        }

        @Override
        public void onBindData(int position, Book data, boolean update, boolean isRemoveMode, boolean removeMark) {
        }
    }

}
