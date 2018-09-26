package net.lzbook.kit.utils.book;

import android.content.Context;
import android.content.Intent;

import com.ding.basic.repository.RequestRepositoryFactory;
import com.ding.basic.request.RequestSubscriber;
import com.orhanobut.logger.Logger;

import net.lzbook.kit.app.base.BaseBookApplication;
import net.lzbook.kit.bean.ChapterErrorBean;
import net.lzbook.kit.constants.ActionConstants;
import net.lzbook.kit.utils.sp.SPKey;
import net.lzbook.kit.utils.sp.SPUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class LoadDataManager {


    public LoadDataManager(Context context) {
    }

    //初始化书架，添加默认书籍
    public void addDefaultBooks(int sex) {

        RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestDefaultBooks(sex,new RequestSubscriber<Boolean>() {
            @Override
            public void requestResult(Boolean result) {
                if (result) {
                    SPUtils.INSTANCE.putDefaultSharedBoolean(SPKey.ADD_DEFAULT_BOOKS, true);

                    try {
                        Intent intent = new Intent(ActionConstants.ACTION_ADD_DEFAULT_SHELF);
                        BaseBookApplication.getGlobalContext().sendBroadcast(intent);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }

            @Override
            public void requestError(@NotNull String message) {
                Logger.i("获取默认书籍异常！ " + message);
            }

            @Override
            public void requestComplete() {
                Logger.i("获取默认书籍完成！");
            }
        });
    }

    //阅读页错误反馈
    public void submitBookError(ChapterErrorBean chapterErrorBean) {

        HashMap<String, String> data = new HashMap<>();
        data.put("bookSourceId", chapterErrorBean.bookSourceId);
        data.put("bookName", chapterErrorBean.bookName);
        data.put("author", chapterErrorBean.author);
        data.put("bookChapterId", chapterErrorBean.bookChapterId);
        data.put("chapterId", chapterErrorBean.chapterId);
        data.put("chapterName", chapterErrorBean.chapterName);
        data.put("serial", String.valueOf(chapterErrorBean.serial));
        data.put("host", chapterErrorBean.host);
        data.put("type", String.valueOf(chapterErrorBean.type));


        RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).requestFeedback(data, new RequestSubscriber<Boolean>() {
            @Override
            public void requestResult(@Nullable Boolean result) {

            }

            @Override
            public void requestError(@NotNull String message) {

            }

            @Override
            public void requestComplete() {

            }
        });
    }
}