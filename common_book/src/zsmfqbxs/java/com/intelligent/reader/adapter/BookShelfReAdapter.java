package com.intelligent.reader.adapter;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.dingyueads.sdk.Bean.AdSceneData;
import com.dingyueads.sdk.Bean.Advertisement;
import com.dingyueads.sdk.Native.YQNativeAdInfo;
import com.dingyueads.sdk.NativeInit;
import com.intelligent.reader.R;
import com.intelligent.reader.adapter.holder.AbsRecyclerViewHolder;

import net.lzbook.kit.cache.imagecache.ImageCacheManager;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.EventBookshelfAd;
import net.lzbook.kit.pulllist.SuperSwipeRefreshLayout;
import net.lzbook.kit.utils.ImageUtils;
import net.lzbook.kit.utils.StatServiceUtils;
import net.lzbook.kit.utils.StatisticManager;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import de.greenrobot.event.EventBus;

public class BookShelfReAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static boolean isList;
    public HashSet<Integer> remove_checked_states;
    public ArrayList<Book> book_list;
    protected Activity mContext;
    int distanceY = -1;
    private boolean isRemoveMode = false;
    private ArrayList<String> update_table;
    private ArrayList<String> down_table;
    private StatisticManager statisticManager;
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
                if (isList) {
                    view = LayoutInflater.from(mContext).inflate(R.layout
                                    .layout_bookshelf_item_list,
                            parent, false);
                } else {
                    view = LayoutInflater.from(mContext).inflate(R.layout.layout_bookshelf_item_grid, parent, false);
                }

                holder = new BookShelfItemHolder(view, shelfItemClickListener,
                        shelfItemLongClickListener);
                break;
            case 1:
                view = LayoutInflater.from(mContext).inflate(R.layout.ad_item_small_layout, parent, false);
                holder = new ADViewHolder(view, shelfItemClickListener, shelfItemLongClickListener);
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
            case 1:
                setADView(holder, position);
                break;
        }
    }

    private void setADView(RecyclerView.ViewHolder holder, final int position) {
        final ADViewHolder aDViewHolder = (ADViewHolder) holder;
        if (aDViewHolder == null) {
            return;
        }
        if (book_list == null) {
            return;
        }
        final Book book = book_list.get(position);
        if (book == null)
            return;

        if (book.info == null) return;
        final YQNativeAdInfo nativeAdInfo = book.info;

        if (nativeAdInfo == null) {
            return;
        }

        Advertisement advertisement = nativeAdInfo.getAdvertisement();

        if (advertisement == null) {
            return;
        }

        if (!TextUtils.isEmpty(advertisement.iconUrl)) {

            ImageCacheManager.getInstance().getImageLoader().get(advertisement.iconUrl, new
                    ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            if (imageContainer != null) {
                                Bitmap bitmap = imageContainer.getBitmap();
                                if (bitmap != null) {
                                    Bitmap roundedCornerBitmap = ImageUtils.getRoundedCornerBitmap
                                            (bitmap, 40);
                                    if (roundedCornerBitmap != null && aDViewHolder.item_ad_image != null) {
                                        aDViewHolder.item_ad_image.setImageBitmap(roundedCornerBitmap);
                                    } else {
                                        aDViewHolder.item_ad_layout.setVisibility(View.GONE);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            aDViewHolder.item_ad_layout.setVisibility(View.GONE);
                        }
                    });

            if (aDViewHolder.item_ad_title != null) {
                aDViewHolder.item_ad_title.setText(TextUtils.isEmpty(advertisement.title) ? "" : advertisement.title);
            }

            if (aDViewHolder.item_ad_extension != null) {
                aDViewHolder.item_ad_extension.setRating(book.rating);
            }
            if (aDViewHolder.item_ad_desc != null) {
                aDViewHolder.item_ad_desc.setText(TextUtils.isEmpty(advertisement.description) ? "" : advertisement.description);
            }

            if (aDViewHolder.item_ad_right_down != null) {
                if ("广点通".equals(advertisement.rationName)) {
                    aDViewHolder.item_ad_right_down.setImageResource(R.drawable.icon_ad_gdt);
                } else if ("百度".equals(advertisement.rationName)) {
                    aDViewHolder.item_ad_right_down.setImageResource(R.drawable.icon_ad_bd);
                } else if ("360".equals(advertisement.rationName)) {
                    aDViewHolder.item_ad_right_down.setImageResource(R.drawable.icon_ad_360);
                } else {
                    aDViewHolder.item_ad_right_down.setImageResource(R.drawable.icon_ad_default);
                }
            }
            aDViewHolder.item_ad_layout.setTag(nativeAdInfo);
            try {
                if (statisticManager == null) {
                    statisticManager = StatisticManager.getStatisticManager();
                }
                AdSceneData adSceneData = nativeAdInfo.getAdSceneData();
                if (adSceneData != null) {
                    adSceneData.ad_showSuccessTime = String.valueOf(System.currentTimeMillis() / 1000L);
                }
                statisticManager.schedulingRequest(mContext, aDViewHolder.item_ad_layout, nativeAdInfo, null, StatisticManager.TYPE_SHOW, NativeInit
                        .ad_position[0]);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            aDViewHolder.item_ad_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (view.getTag() != null) {
                        try {
                            if (statisticManager == null) {
                                statisticManager = StatisticManager.getStatisticManager();
                            }
                            statisticManager.schedulingRequest(mContext, view, nativeAdInfo, null, StatisticManager.TYPE_CLICK, NativeInit.ad_position[0]);
                            if (nativeAdInfo != null && com.dingyueads.sdk.Constants.AD_TYPE_360 == nativeAdInfo.getAdvertisement().platformId) {
                                EventBookshelfAd eventBookshelfAd = new EventBookshelfAd("bookshelfclick_360", position / Constants.dy_shelf_ad_freq, nativeAdInfo);
                                EventBus.getDefault().post(eventBookshelfAd);
                            }
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                        StatServiceUtils.statBookEventClick(mContext, StatServiceUtils.type_ad_shelf);
                        if (Constants.DEVELOPER_MODE) {
                            Toast.makeText(mContext, "你点击了广告", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
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

    class ADViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        RelativeLayout item_ad_layout;
        ImageView item_ad_image;
        TextView item_ad_title;
        TextView item_ad_desc;
        RatingBar item_ad_extension;
        ImageView item_ad_right_down;

        ShelfItemClickListener shelfItemClickListener;
        ShelfItemLongClickListener shelfItemLongClickListener;

        public ADViewHolder(View itemView, ShelfItemClickListener shelfItemClickListener, ShelfItemLongClickListener shelfItemLongClickListener) {
            super(itemView);
            this.shelfItemClickListener = shelfItemClickListener;
            this.shelfItemLongClickListener = shelfItemLongClickListener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            item_ad_layout = (RelativeLayout) itemView.findViewById(R.id.item_ad_layout);
            item_ad_image = (ImageView) itemView.findViewById(R.id.item_ad_image);
            item_ad_title = (TextView) itemView.findViewById(R.id.item_ad_title);
            item_ad_extension = (RatingBar) itemView.findViewById(R.id.item_ad_extension);
            item_ad_desc = (TextView) itemView.findViewById(R.id.item_ad_desc);
            item_ad_right_down = (ImageView) itemView.findViewById(R.id.item_ad_right_down);
        }

        @Override
        public void onClick(View v) {
            if (shelfItemClickListener != null) {
                shelfItemClickListener.onItemClick(v, getPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (shelfItemLongClickListener != null) {
                shelfItemLongClickListener.onItemLongClick(v, getPosition());
            }
            return true;
        }
    }
}
