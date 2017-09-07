package com.intelligent.reader.adapter;

import com.intelligent.reader.R;
import com.umeng.fb.model.Reply;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class FeedBackAdapter extends RecyclerView.Adapter<FeedBackAdapter.ViewHolder> {

    private static final String TAG = FeedBackAdapter.class.getSimpleName();

    private static final int TYPE_HEAD = 0x10;
    private static final int TYPE_USER_REPLY = 0x11;
    private static final int TYPE_DEV_REPLY = 0x12;

    private static final int FIRST_PROBLEM = 0x20;
    private static final int SECOND_PROBLEM = 0x21;
    private static final int THIRD_PROBLEM = 0x22;
    private static final int FOURTH_PROBLEM = 0x23;
    private WeakReference<Context> weakReference;
    private List<Reply> replies;
    private HashMap<Integer, String> message = new HashMap<>();
    private String contact = "";

    private Drawable icon_open_answer;
    private Drawable icon_close_answer;

    public FeedBackAdapter(Context context, List<Reply> replies) {
        this.weakReference = new WeakReference<>(context);
        this.replies = replies;
        icon_open_answer = weakReference.get().getResources().getDrawable(R.drawable.icon_open_answer);
        icon_close_answer = weakReference.get().getResources().getDrawable(R.drawable.icon_close_answer);

    }

    @Override
    public int getItemCount() {
        return replies.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEAD) {
            return new ViewHolder(LayoutInflater.from(weakReference.get()).inflate(R.layout.feedback_head, parent, false), viewType);
        } else if (viewType == TYPE_USER_REPLY) {
            return new ViewHolder(LayoutInflater.from(weakReference.get()).inflate(R.layout.feedback_user_reply, parent, false), viewType);
        } else {
            return new ViewHolder(LayoutInflater.from(weakReference.get()).inflate(R.layout.feedback_dev_reply, parent, false), viewType);
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Reply reply = replies.get(position);
        if (reply == null) {
            return;
        }

        if ("feedback_head".equals(reply.type)) {

            if (holder.help_first_problem != null) {
                holder.help_first_problem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (holder.help_first_answer.isShown()) {
                            holder.help_first_answer.setVisibility(View.GONE);
                            holder.help_first_problem_more.setImageDrawable(icon_open_answer);
                        } else {
                            holder.help_first_answer.setVisibility(View.VISIBLE);
                            holder.help_first_problem_more.setImageDrawable(icon_close_answer);
                        }
                    }
                });
            }

            if (holder.help_second_problem != null) {
                holder.help_second_problem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (holder.help_second_answer.isShown()) {
                            holder.help_second_answer.setVisibility(View.GONE);
                            holder.help_second_problem_more.setImageDrawable(icon_open_answer);
                        } else {
                            holder.help_second_answer.setVisibility(View.VISIBLE);
                            holder.help_second_problem_more.setImageDrawable(icon_close_answer);
                        }
                    }
                });
            }

            if (holder.help_third_problem != null) {
                holder.help_third_problem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (holder.help_third_answer.isShown()) {
                            holder.help_third_answer.setVisibility(View.GONE);
                            holder.help_third_problem_more.setImageDrawable(icon_open_answer);
                        } else {
                            holder.help_third_answer.setVisibility(View.VISIBLE);
                            holder.help_third_problem_more.setImageDrawable(icon_close_answer);
                        }
                    }
                });
            }

            if (holder.help_fourth_problem != null) {
                holder.help_fourth_problem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (holder.help_fourth_answer.isShown()) {
                            holder.help_fourth_answer.setVisibility(View.GONE);
                            holder.help_fourth_problem_more.setImageDrawable(icon_open_answer);
                        } else {
                            holder.help_fourth_answer.setVisibility(View.VISIBLE);
                            holder.help_fourth_problem_more.setImageDrawable(icon_close_answer);
                        }
                    }
                });
            }

            if (holder.help_fifth_problem != null) {
                holder.help_fifth_problem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (holder.help_fifth_answer.isShown()) {
                            holder.help_fifth_answer.setVisibility(View.GONE);
                            holder.help_fifth_problem_more.setImageDrawable(icon_open_answer);
                        } else {
                            holder.help_fifth_answer.setVisibility(View.VISIBLE);
                            holder.help_fifth_problem_more.setImageDrawable(icon_close_answer);
                        }
                    }
                });
            }

            if (holder.help_sixth_problem != null) {
                holder.help_sixth_problem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (holder.help_sixth_answer.isShown()) {
                            holder.help_sixth_answer.setVisibility(View.GONE);
                            holder.help_sixth_problem_more.setImageDrawable(icon_open_answer);
                        } else {
                            holder.help_sixth_answer.setVisibility(View.VISIBLE);
                            holder.help_sixth_problem_more.setImageDrawable(icon_close_answer);
                        }
                    }
                });
            }

            if (holder.feedback_first_problem != null) {
                holder.feedback_first_problem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (message.containsKey(FIRST_PROBLEM)) {
                            holder.feedback_first_problem_check.setImageResource(R.drawable.feedback_check);
                            message.remove(FIRST_PROBLEM);
                        } else {
                            holder.feedback_first_problem_check.setImageResource(R.drawable.feedback_checked);
                            message.put(FIRST_PROBLEM, "书籍无法阅读");
                        }
                    }
                });
            }

            if (holder.feedback_first_problem_check != null) {
                if (message.containsKey(FIRST_PROBLEM)) {
                    holder.feedback_first_problem_check.setImageResource(R.drawable.feedback_checked);
                } else {
                    holder.feedback_first_problem_check.setImageResource(R.drawable.feedback_check);
                }
            }

            if (holder.feedback_second_problem != null) {
                holder.feedback_second_problem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (message.containsKey(SECOND_PROBLEM)) {
                            holder.feedback_second_problem_check.setImageResource(R.drawable.feedback_check);
                            message.remove(SECOND_PROBLEM);
                        } else {
                            holder.feedback_second_problem_check.setImageResource(R.drawable.feedback_checked);
                            message.put(SECOND_PROBLEM, "内容章节出错");
                        }
                    }
                });
            }

            if (holder.feedback_second_problem_check != null) {
                if (message.containsKey(SECOND_PROBLEM)) {
                    holder.feedback_second_problem_check.setImageResource(R.drawable.feedback_checked);
                } else {
                    holder.feedback_second_problem_check.setImageResource(R.drawable.feedback_check);
                }
            }

            if (holder.feedback_third_problem != null) {
                holder.feedback_third_problem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (message.containsKey(THIRD_PROBLEM)) {
                            holder.feedback_third_problem_check.setImageResource(R.drawable.feedback_check);
                            message.remove(THIRD_PROBLEM);
                        } else {
                            holder.feedback_third_problem_check.setImageResource(R.drawable.feedback_checked);
                            message.put(THIRD_PROBLEM, "缓存失败");
                        }
                    }
                });
            }

            if (holder.feedback_third_problem_check != null) {
                if (message.containsKey(THIRD_PROBLEM)) {
                    holder.feedback_third_problem_check.setImageResource(R.drawable.feedback_checked);
                } else {
                    holder.feedback_third_problem_check.setImageResource(R.drawable.feedback_check);
                }
            }

            if (holder.feedback_fourth_problem != null) {
                holder.feedback_fourth_problem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (message.containsKey(FOURTH_PROBLEM)) {
                            holder.feedback_fourth_problem_check.setImageResource(R.drawable.feedback_check);
                            message.remove(FOURTH_PROBLEM);
                        } else {
                            holder.feedback_fourth_problem_check.setImageResource(R.drawable.feedback_checked);
                            message.put(FOURTH_PROBLEM, "其他");
                        }
                    }
                });
            }

            if (holder.feedback_fourth_problem_check != null) {
                if (message.containsKey(FOURTH_PROBLEM)) {
                    holder.feedback_fourth_problem_check.setImageResource(R.drawable.feedback_checked);
                } else {
                    holder.feedback_fourth_problem_check.setImageResource(R.drawable.feedback_check);
                }
            }

            if (holder.feedback_contact != null) {
                holder.feedback_contact.setText(contact);
                if (TextUtils.isEmpty(contact)) {
                    holder.feedback_contact.clearFocus();
                }
                holder.feedback_contact.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        contact = "";
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        contact = s.toString();
                    }
                });
            }

        } else {

            if (holder.reply_content != null && !TextUtils.isEmpty(reply.content)) {
                holder.reply_content.setText(reply.content);
            }

            if (holder.reply_failed != null) {
                if (Reply.STATUS_NOT_SENT.equals(reply.status)) {
                    holder.reply_failed.setVisibility(View.VISIBLE);
                } else {
                    holder.reply_failed.setVisibility(View.GONE);
                }
            }

            if (holder.reply_data != null) {
                if (position + 1 < replies.size()) {
                    Reply nextReply = replies.get(position + 1);
                    if (nextReply.created_at - reply.created_at > 100000) {
                        Date time = new Date(reply.created_at);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        holder.reply_data.setText(simpleDateFormat.format(time));
                        holder.reply_data.setVisibility(View.VISIBLE);
                    } else {
                        holder.reply_data.setVisibility(View.GONE);
                    }
                }
            }
        }

    }

    @Override
    public int getItemViewType(int position) {
        Reply reply = replies.get(position);
        if ("feedback_head".equals(reply.type)) {
            return TYPE_HEAD;
        } else if (Reply.TYPE_USER_REPLY.equals(reply.type)) {
            return TYPE_USER_REPLY;
        } else if (Reply.TYPE_NEW_FEEDBACK.equals(reply.type)) {
            return TYPE_USER_REPLY;
        } else {
            return TYPE_DEV_REPLY;
        }
    }

    public String getHeadMessage() {
        StringBuilder stringBuilder = new StringBuilder();
        Iterator iterator = message.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            stringBuilder.append(entry.getValue() + "    ");
        }
        message.clear();
        notifyItemChanged(0);
        return stringBuilder.toString();
    }

    public String getContact() {
        return contact;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView reply_image;
        public TextView reply_content;
        public ImageView reply_failed;
        public TextView reply_data;
        private RelativeLayout help_first_problem;
        private ImageView help_first_problem_more;
        private TextView help_first_answer;
        private RelativeLayout help_second_problem;
        private ImageView help_second_problem_more;
        private TextView help_second_answer;
        private RelativeLayout help_third_problem;
        private ImageView help_third_problem_more;
        private TextView help_third_answer;
        private RelativeLayout help_fourth_problem;
        private ImageView help_fourth_problem_more;
        private TextView help_fourth_answer;
        private RelativeLayout help_fifth_problem;
        private ImageView help_fifth_problem_more;
        private TextView help_fifth_answer;
        private RelativeLayout help_sixth_problem;
        private ImageView help_sixth_problem_more;
        private TextView help_sixth_answer;
        private RelativeLayout feedback_first_problem;
        private ImageView feedback_first_problem_check;
        private RelativeLayout feedback_second_problem;
        private ImageView feedback_second_problem_check;
        private RelativeLayout feedback_third_problem;
        private ImageView feedback_third_problem_check;
        private RelativeLayout feedback_fourth_problem;
        private ImageView feedback_fourth_problem_check;
        private EditText feedback_contact;

        public ViewHolder(View view, int type) {
            super(view);
            if (type == TYPE_HEAD) {
                help_first_problem = (RelativeLayout) view.findViewById(R.id.help_first_problem);
                help_first_problem_more = (ImageView) view.findViewById(R.id.help_first_problem_more);
                help_first_answer = (TextView) view.findViewById(R.id.help_first_answer);

                help_second_problem = (RelativeLayout) view.findViewById(R.id.help_second_problem);
                help_second_problem_more = (ImageView) view.findViewById(R.id.help_second_problem_more);
                help_second_answer = (TextView) view.findViewById(R.id.help_second_answer);

                help_third_problem = (RelativeLayout) view.findViewById(R.id.help_third_problem);
                help_third_problem_more = (ImageView) view.findViewById(R.id.help_third_problem_more);
                help_third_answer = (TextView) view.findViewById(R.id.help_third_answer);

                help_fourth_problem = (RelativeLayout) view.findViewById(R.id.help_fourth_problem);
                help_fourth_problem_more = (ImageView) view.findViewById(R.id.help_fourth_problem_more);
                help_fourth_answer = (TextView) view.findViewById(R.id.help_fourth_answer);

                help_fifth_problem = (RelativeLayout) view.findViewById(R.id.help_fifth_problem);
                help_fifth_problem_more = (ImageView) view.findViewById(R.id.help_fifth_problem_more);
                help_fifth_answer = (TextView) view.findViewById(R.id.help_fifth_answer);

                help_sixth_problem = (RelativeLayout) view.findViewById(R.id.help_sixth_problem);
                help_sixth_problem_more = (ImageView) view.findViewById(R.id.help_sixth_problem_more);
                help_sixth_answer = (TextView) view.findViewById(R.id.help_sixth_answer);

                feedback_first_problem = (RelativeLayout) view.findViewById(R.id.feedback_first_problem);
                feedback_first_problem_check = (ImageView) view.findViewById(R.id.feedback_first_problem_check);
                feedback_second_problem = (RelativeLayout) view.findViewById(R.id.feedback_second_problem);
                feedback_second_problem_check = (ImageView) view.findViewById(R.id.feedback_second_problem_check);
                feedback_third_problem = (RelativeLayout) view.findViewById(R.id.feedback_third_problem);
                feedback_third_problem_check = (ImageView) view.findViewById(R.id.feedback_third_problem_check);
                feedback_fourth_problem = (RelativeLayout) view.findViewById(R.id.feedback_fourth_problem);
                feedback_fourth_problem_check = (ImageView) view.findViewById(R.id.feedback_fourth_problem_check);
                feedback_contact = (EditText) view.findViewById(R.id.feedback_contact);
            } else {
                reply_image = (ImageView) view.findViewById(R.id.feedback_reply_image);
                reply_data = (TextView) view.findViewById(R.id.feedback_reply_data);
                reply_content = (TextView) view.findViewById(R.id.feedback_reply_text);
                reply_failed = (ImageView) view.findViewById(R.id.feedback_reply_status);
            }
        }
    }
}
