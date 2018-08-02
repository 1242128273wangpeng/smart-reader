package com.intelligent.reader.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.intelligent.reader.R;
import com.intelligent.reader.activity.CommentActivity;
import com.intelligent.reader.activity.FindBookDetail;
import com.intelligent.reader.view.ExpandInformation;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.constants.ReplaceConstants;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.RequestItem;
import net.lzbook.kit.data.comment.CommentEntity;
import net.lzbook.kit.utils.AppUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhenXiang on 2017/10/30.
 */

public class CoverCommentAdapter extends RecyclerView.Adapter<CoverCommentAdapter.ViewHolder> implements ExpandInformation.ClickToActListener {

    private WeakReference<Context> weakReference;
    private List<CommentEntity.DataBean.EntityListBean> commentEntities = new ArrayList<>();
    private RequestItem requestItem;
    private Book book;

    public CoverCommentAdapter(Context context, List<CommentEntity.DataBean.EntityListBean> commentEntities, RequestItem requestItem, Book book) {
        this.weakReference = new WeakReference<>(context);
        this.commentEntities = commentEntities;
        this.requestItem = requestItem;
        this.book = book;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(weakReference.get()).inflate(R.layout.item_cover_comment_list, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        CommentEntity.DataBean.EntityListBean entity = commentEntities.get(position);
        if(entity != null){
            holder.tv_content.initializeContent(entity.getComments()+"");
            holder.tv_user_name.setText(entity.getNickName()+"");
            if(!AppUtils.isContainChinese(entity.getCommentTime()+"")){
                holder.tv_time.setText(AppUtils.getCommentTime(entity.getCommentTime())+"");
            }
        }

        if(position % 2 == 0){
            Glide.with(weakReference.get()).load(R.mipmap.coverpage_icon_head2).placeholder(R.mipmap.coverpage_icon_head2).into(holder.iv_head);
        }else if(position % 3 == 0){
            Glide.with(weakReference.get()).load(R.mipmap.coverpage_icon_head3).placeholder(R.mipmap.coverpage_icon_head3).into(holder.iv_head);
        }else if(position % 4 == 0){
            Glide.with(weakReference.get()).load(R.mipmap.coverpage_icon_head4).placeholder(R.mipmap.coverpage_icon_head4).into(holder.iv_head);
        }else if(position % 5 == 0){
            Glide.with(weakReference.get()).load(R.mipmap.coverpage_icon_head5).placeholder(R.mipmap.coverpage_icon_head5).into(holder.iv_head);
        }else{
            Glide.with(weakReference.get()).load(R.mipmap.coverpage_icon_head1).placeholder(R.mipmap.coverpage_icon_head1).into(holder.iv_head);
        }

    }

    @Override
    public int getItemCount() {
        if (commentEntities.size() != 0) {
            return commentEntities.size();
        }
        return 0;
    }



    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView iv_head;
        private TextView tv_user_name, tv_time;
        private ExpandInformation tv_content;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            iv_head = (ImageView) itemView.findViewById(R.id.iv_head);
            tv_user_name = (TextView) itemView.findViewById(R.id.tv_user_name);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            tv_content = (ExpandInformation) itemView.findViewById(R.id.book_cover_description);
            tv_content.setListener(CoverCommentAdapter.this);

        }

        @Override
        public void onClick(View v) {



        }
    }
    @Override
    public void gotoAct() {
        if(requestItem == null ||  book == null || weakReference.get() == null)
            return;
        Map<String,String> data = new HashMap<>();
        data.put("type","1");
        data.put("bookid",requestItem.book_id);
        StartLogClickUtil.upLoadEventLog(weakReference.get(), StartLogClickUtil.BOOOKDETAIL_PAGE, StartLogClickUtil.CLICK, data);

        Bundle bundle = new Bundle();
        bundle.putSerializable("cover", book);
        bundle.putSerializable(Constants.REQUEST_ITEM, requestItem);
        Intent intent = new Intent(weakReference.get(),CommentActivity.class);
        intent.putExtras(bundle);
        weakReference.get().startActivity(intent);
    }
}
