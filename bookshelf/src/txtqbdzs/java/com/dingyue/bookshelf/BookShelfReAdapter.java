package com.dingyue.bookshelf;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BookShelfReAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static boolean isList;
    public HashSet<Integer> remove_checked_states;
    public ArrayList<Book> book_list;
    protected Activity mContext;
    int distanceY = -1;
    private boolean isRemoveMode = false;
    private ArrayList<String> update_table;
    private ArrayList<String> down_table;
    private ShelfItemClickListener shelfItemClickListener;
    private ShelfItemLongClickListener shelfItemLongClickListener;

    public BookShelfReAdapter(Activity context, List<Book> list, ShelfItemClickListener itemClick, ShelfItemLongClickListener itemLongClick, boolean isList) {
        mContext = context;
        book_list = (ArrayList<Book>) list;
        shelfItemClickListener = itemClick;
        shelfItemLongClickListener = itemLongClick;
        this.isList = isList;
        update_table = new ArrayList<>();
        down_table = new ArrayList<>();
        remove_checked_states = new HashSet<>();
        resetRemovedState();
        distanceY = (int) mContext.getResources().getDimension(net.lzbook.kit.R.dimen.dimen_view_height_default);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        RecyclerView.ViewHolder holder = null;
        switch (viewType) {
            case 0:

                    view = LayoutInflater.from(mContext).inflate(R.layout
                                    .layout_bookshelf_item_list,
                            parent, false);


                holder = new BookShelfItemHolder(view, shelfItemClickListener,
                        shelfItemLongClickListener);
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case 0: {
                Book book = book_list.get(position);
                ((AbsRecyclerViewHolder<Book>) holder).onBindData(position, book,
                        update_table.contains(book.book_id), isRemoveMode(), remove_checked_states
                                .contains(position));
            }
            break;
        }
    }


    @Override
    public int getItemCount() {
        if (book_list != null) {
            return book_list.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (book_list != null && position < book_list.size() && book_list.get(position) != null) {
            if (book_list.get(position).book_type == 0) {
                return 0;
            } else if (book_list.get(position).book_type == -2) {
                return 1;
            }
            return -1;
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


}
