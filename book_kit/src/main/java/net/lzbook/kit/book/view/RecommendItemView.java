package net.lzbook.kit.book.view;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import net.lzbook.kit.R;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.utils.BookCoverUtil;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RecommendItemView extends RelativeLayout {
    private String mTitle;
    private ImageView mCovorImageView;
    private TextView mTitleTextView;
    private TextView mAuthorTextView;
    private TextView mFansTextView;

    public RecommendItemView(Context context, int type) {
        super(context);
        if (context == null) {
            return;
        }
        try {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            if (type == BookCoverUtil.TYPE_AUTHOR) {
                inflater.inflate(R.layout.layout_recommend_item_vertical, this);
            } else if (type == BookCoverUtil.TYPE_CATEGORY) {
                inflater.inflate(R.layout.layout_recommend_item_horizontal, this);
            }
        } catch (InflateException e) {
            e.printStackTrace();
        }
        mCovorImageView = findViewById(R.id.layout_recommend_item_cover);
        mTitleTextView = findViewById(R.id.layout_recommend_item_title);
        mAuthorTextView = findViewById(R.id.layout_recommend_item_author);

        if (type == BookCoverUtil.TYPE_AUTHOR) {
            mFansTextView = findViewById(R.id.layout_recommend_item_fans);
        } else {
            mFansTextView = null;
        }
    }

    public RecommendItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setArgs(String imgUrl, String title, String author, int fans) {
        mTitle = title;
        if (!TextUtils.isEmpty(imgUrl) && mCovorImageView != null && !imgUrl.equals(
                ReplaceConstants.getReplaceConstants().DEFAULT_IMAGE_URL)) {
            Glide.with(getContext().getApplicationContext()).load(imgUrl).placeholder(
                    R.drawable.icon_book_cover_default).error(
                    (R.drawable.icon_book_cover_default)).diskCacheStrategy(
                    DiskCacheStrategy.ALL).into(mCovorImageView);
        } else {
            Glide.with(getContext().getApplicationContext()).load(
                    R.drawable.icon_book_cover_default).into(mCovorImageView);
        }
        if (mTitleTextView != null) {
            mTitleTextView.setText(title);
        }
        if (mAuthorTextView != null) {
            mAuthorTextView.setText(author);
        }
        if (mFansTextView != null) {
            mFansTextView.setText(Html.fromHtml(fans + "<font color=\"#999999\">"
                    + " 人追" + "</font>"));
        }
    }

    public String getTitle() {
        return mTitle;
    }
}