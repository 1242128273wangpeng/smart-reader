package com.intelligent.reader.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;
import com.bumptech.glide.Glide;
import com.ding.basic.bean.LoginResp;
import com.dingyue.contract.router.RouterConfig;
import com.dingyue.contract.router.RouterUtil;
import com.dingyue.contract.util.CommonUtil;
import com.intelligent.reader.R;
import com.intelligent.reader.util.EventBookStore;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.appender_loghub.StartLogClickUtil;
import net.lzbook.kit.book.component.service.DownloadService;
import net.lzbook.kit.book.download.CacheManager;
import net.lzbook.kit.book.view.ConsumeEvent;
import net.lzbook.kit.book.view.MyDialog;
import net.lzbook.kit.book.view.SwitchButton;
import net.lzbook.kit.cache.DataCleanManager;
import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.constants.SPKeys;
import net.lzbook.kit.data.bean.BookTask;
import net.lzbook.kit.user.UserManager;
import net.lzbook.kit.utils.AppUtils;
import net.lzbook.kit.utils.StatServiceUtils;
import net.lzbook.kit.utils.UIHelper;
import net.lzbook.kit.utils.update.ApkUpdateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import iyouqu.theme.BaseCacheableActivity;
import iyouqu.theme.ThemeMode;

public class SettingActivity extends BaseCacheableActivity implements View.OnClickListener, SwitchButton.OnCheckedChangeListener {


    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        try {
            setContentView(R.layout.act_home);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }


    }


}