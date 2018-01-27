package com.intelligent.reader.activity;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.intelligent.reader.R;
import com.intelligent.reader.adapter.FeedBackAdapter;
import com.umeng.fb.FeedbackAgent;
import com.umeng.fb.SyncListener;
import com.umeng.fb.model.Conversation;
import com.umeng.fb.model.Reply;
import com.umeng.fb.model.UserInfo;

import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.utils.AppLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import iyouqu.theme.StatusBarCompat;

public class FeedBackActivity extends Activity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, OnFocusChangeListener {

    private static final String TAG = FeedBackActivity.class.getSimpleName();

    private TextView feedback_head_back;
    private SwipeRefreshLayout feedback_content_layout;
    private RecyclerView feedback_content_list;
    private EditText feedback_sent_edit;
    private TextView feedback_sent_text;

    private Conversation conversation;
    private List<Reply> replies = new ArrayList<>();
    private RecyclerView.LayoutManager layoutManager;
    private FeedBackAdapter adapter;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.compat(this);
        try {
            setContentView(R.layout.act_feedback);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        initView();
        initListener();
        initData();
    }

    private void initView() {
        feedback_head_back = (TextView) findViewById(R.id.feedback_head_back);
        feedback_content_layout = (SwipeRefreshLayout) findViewById(R.id.feedback_content_layout);
        feedback_content_list = (RecyclerView) findViewById(R.id.feedback_content_list);
        feedback_sent_edit = (EditText) findViewById(R.id.feedback_sent_edit);
        feedback_sent_text = (TextView) findViewById(R.id.feedback_sent_text);
    }

    private void initListener() {
        if (feedback_head_back != null) {
            feedback_head_back.setOnClickListener(this);
        }

        if (feedback_sent_text != null) {
            feedback_sent_text.setOnClickListener(this);
        }

        if (feedback_content_layout != null) {
            feedback_sent_edit.setOnFocusChangeListener(this);
            feedback_content_layout.setOnRefreshListener(this);
        }
    }

    private void initData() {

        if (conversation == null) {
            conversation = new FeedbackAgent(getApplicationContext()).getDefaultConversation();
        }

        if (replies == null) {
            replies = new ArrayList<>();
        } else {
            replies.clear();
        }
        replies.addAll(conversation.getReplyList());
        Reply reply = new Reply("", "", "feedback_head", 0);
        replies.add(0, reply);

        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);

        if (adapter == null) {
            adapter = new FeedBackAdapter(getApplicationContext(), replies);
        }

        if (feedback_content_list != null) {
            feedback_content_list.setLayoutManager(layoutManager);
            feedback_content_list.setAdapter(adapter);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.feedback_head_back:
                Map<String, String> data = new HashMap<>();
                data.put("type", "1");
                StartLogClickUtil.upLoadEventLog(this, StartLogClickUtil.PERHELP_PAGE, StartLogClickUtil.BACK, data);
                finish();
                break;
            case R.id.feedback_sent_text:
                String content = feedback_sent_edit.getText().toString();
                String message = adapter.getHeadMessage();
                feedback_sent_edit.getEditableText().clear();
                if (!TextUtils.isEmpty(content)) {
                    if (!TextUtils.isEmpty(message)) {
                        content = message + content;
                    }
                    String userContact = adapter.getContact();
                    if (!TextUtils.isEmpty(userContact)) {
                        final FeedbackAgent feedbackAgent = new FeedbackAgent(this);
                        UserInfo userInfo = feedbackAgent.getUserInfo();
                        Map<String, String> contact = userInfo.getContact();
                        if (contact != null) {
                            AppLog.e(TAG, "AddUserContact  ==> " + userContact);
                            contact.put("contact", userContact);
                            userInfo.setContact(contact);
                            feedbackAgent.setUserInfo(userInfo);

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    feedbackAgent.updateUserInfo();
                                }
                            }).start();
                        }

                    }
                    // 将内容添加到会话列表
                    conversation.addUserReply(content);
                    // 数据同步
                    refreshData();
                } else {
                    Toast.makeText(getApplicationContext(), "反馈不能为空！", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onRefresh() {
        refreshData();
    }

    private void resetDataList() {
        if (conversation != null) {
            if (replies == null) {
                replies = new ArrayList<>();
            } else {
                replies.clear();
            }
            replies.addAll(conversation.getReplyList());
            Reply reply = new Reply("", "", "feedback_head", 0);
            replies.add(0, reply);

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void refreshData() {
        conversation.sync(new SyncListener() {

            @Override
            public void onSendUserReply(List<Reply> replyList) {
            }

            @Override
            public void onReceiveDevReply(List<Reply> replyList) {
                // SwipeRefreshLayout停止刷新
                feedback_content_layout.setRefreshing(false);
                //刷新RecyclerView
                resetDataList();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (feedback_content_list != null) {
                            feedback_content_list.scrollToPosition(adapter.getItemCount() - 1);
                        }
                    }
                }, 500);
            }
        });
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (feedback_content_list != null) {
                        feedback_content_list.scrollToPosition(adapter.getItemCount() - 1);
                    }
                }
            }, 500);
        } else {
            hideInputMethodManager();
        }
    }

    private void hideInputMethodManager() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(feedback_sent_edit, InputMethodManager.SHOW_FORCED);
        imm.hideSoftInputFromWindow(feedback_sent_edit.getWindowToken(), 0);
    }
}
