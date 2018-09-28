package net.lzbook.kit.user;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.constants.Constants;
import com.ding.basic.bean.Book;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ding.basic.bean.RecommendBooksEndResp;
import com.ding.basic.bean.RecommendBooksResp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 项目名称：11m
 * 类描述：
 * 创建人：Zach
 * 创建时间：2017/11/2 0002
 */
@Deprecated
abstract class RecommendBookStrategy {

    RecommendBooksResp bookBeans ;
    List<String> mDislikeBooksList;
    RecommendBooksEndResp mBooksEndBean;

    RecommendBookStrategy(){
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(BaseBookApplication.getGlobalContext());
        String dislikedBookId = defaultSharedPreferences.getString(Constants.DISLIKED_BOOK_ID, null);
        //判断上一次清空的时间
        long lastTime = defaultSharedPreferences.getLong(Constants.RECOMMEND_UPDATE_TIME,0);
        long duration = 24 * 60 * 60 * 1000;
        //距离上一次清空超过30天则清空
        if((System.currentTimeMillis()-lastTime)> duration * 30){
            defaultSharedPreferences.edit().putString(Constants.DISLIKED_BOOK_ID,null).apply();
            defaultSharedPreferences.edit().putLong(Constants.RECOMMEND_UPDATE_TIME,System.currentTimeMillis()).apply();
        }

        //不喜欢的书超过12本则清空
        if(dislikedBookId!=null){
            mDislikeBooksList = Arrays.asList(dislikedBookId.split(","));
            if(mDislikeBooksList.size()>12){
                mDislikeBooksList = new ArrayList<>();
                defaultSharedPreferences.edit().putString(Constants.DISLIKED_BOOK_ID,null).apply();
                defaultSharedPreferences.edit().putLong(Constants.RECOMMEND_UPDATE_TIME,System.currentTimeMillis()).apply();
            }
        }else{
            mDislikeBooksList =  new ArrayList<>();
        }
    }

    abstract Object getBookFromList();

    abstract Book getRecommendBook();

    abstract Book getBookendRecommendBook();

    abstract Book getBookendNewBook();

}