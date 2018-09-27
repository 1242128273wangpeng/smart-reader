package net.lzbook.kit.utils.cache;

import net.lzbook.kit.app.base.BaseBookApplication;
import net.lzbook.kit.utils.download.CacheManager;

import android.os.Message;

import com.ding.basic.bean.Book;
import com.ding.basic.RequestRepositoryFactory;

import java.util.List;

public class UIHelper {

    public static void clearAppCache() {
        Message msg = new Message();
        try {

            List<Book> books = RequestRepositoryFactory.Companion.loadRequestRepositoryFactory(BaseBookApplication.getGlobalContext()).loadBooks();

            if (books != null && books.size() > 0) {
                for (int i = 0; i < books.size(); i++) {
                    CacheManager.INSTANCE.remove(books.get(i).getBook_id());
                }
            }

            DataCleanManager.cleanInternalCache(BaseBookApplication.getGlobalContext());

            msg.what = 1;
        } catch (Exception e) {
            e.printStackTrace();
            msg.what = -1;
        }
    }
}