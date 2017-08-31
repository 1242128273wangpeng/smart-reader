package com.intelligent.reader.adapter.holder;

import android.view.View;
import android.widget.TextView;

import com.intelligent.reader.R;
import com.intelligent.reader.adapter.BookShelfReAdapter;

/**
 * Created by Administrator on 2017/4/13 0013.
 */

public class HolderFactory {
    public enum HolderType {
        ListOne, ListTwo, ListThree, ListFour, ListFive, GridOne, GridTwo,GridThree,
        HistoryOne, HistoryTwo, HistoryThree
    }

    public static AbsRecyclerViewHolder createHolder(View itemView,
                                                     BookShelfReAdapter.ShelfItemClickListener shelfItemClickListener,
                                                     BookShelfReAdapter.ShelfItemLongClickListener shelfItemLongClickListener) {
        TextView txtType = (TextView) itemView.findViewById(R.id.bookshelf_item_layout_type);
        HolderType holderType = Enum.valueOf(HolderType.class, txtType.getText().toString());

        switch (holderType) {
            case ListOne:
                return new ListOneHolder(itemView, shelfItemClickListener,
                        shelfItemLongClickListener);
            case ListTwo:
                return new ListTwoHolder(itemView, shelfItemClickListener,
                        shelfItemLongClickListener);
            case ListThree:
                return new ListThreeHolder(itemView, shelfItemClickListener,
                        shelfItemLongClickListener);
            case ListFour:
                return new ListFourHolder(itemView, shelfItemClickListener,
                        shelfItemLongClickListener);
            case ListFive:
                return new ListFiveHolder(itemView, shelfItemClickListener,
                        shelfItemLongClickListener);
            case GridOne:
                return new GridOneHolder(itemView, shelfItemClickListener,
                        shelfItemLongClickListener);
            case GridTwo:
                return new GridTwoHolder(itemView, shelfItemClickListener,
                        shelfItemLongClickListener);
            case GridThree:
                return new GridThreeHolder(itemView, shelfItemClickListener,
                        shelfItemLongClickListener);
            case HistoryOne:
                return new HisOneHolder(itemView, shelfItemClickListener,
                        shelfItemLongClickListener);
            case HistoryTwo:
                return new HisTwoHolder(itemView, shelfItemClickListener,
                        shelfItemLongClickListener);
            case HistoryThree:
                return new HisThreeHolder(itemView, shelfItemClickListener,
                        shelfItemLongClickListener);
            default:
                return new ListOneHolder(itemView, shelfItemClickListener,
                        shelfItemLongClickListener);
        }
    }

}
