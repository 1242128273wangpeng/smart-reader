package net.lzbook.kit.utils.book;

import net.lzbook.kit.service.CheckNovelUpdateService;

import android.app.NotificationManager;
import android.content.Context;

import java.util.ArrayList;

public class CheckNovelUpdHelper {

    public static void delLocalNotify(Context ctt) {
        CheckNovelUpdateService.cache_list = null;
        NotificationManager nmgr = (NotificationManager) ctt.getSystemService(Context.NOTIFICATION_SERVICE);
        nmgr.cancel(CheckNovelUpdateService.novel_upd_notify_id);
        nmgr.cancel(CheckNovelUpdateService.novel_update_notify_id);
    }

    public static ArrayList<MyBook> combain(Context ctt, ArrayList<MyBook> list) {
        ArrayList<MyBook> old = CheckNovelUpdateService.cache_list;
        if (old == null)
            return list;
        if (list == null)
            return old;
        if (old.size() == 0 || list.size() == 0) {
            old.addAll(list);
            return old;
        }
        ArrayList<MyBook> other = new ArrayList<MyBook>();
        int s = list.size();
        int len = old.size();
        for (int i = 0; i < len; i++) {
            MyBook b1 = old.get(i);
            boolean contain = false;
            label:
            for (int j = 0; j < s; j++) {
                MyBook b2 = list.get(j);
                if (b1.equals(b2)) {
                    b2.num += b1.num;
                    contain = true;
                    break label;
                }
            }
            if (!contain) {
                other.add(b1);
            }
        }
        list.addAll(other);
        return list;

    }

    public static class MyBook {
        public String name;
        public String book_id;
        public int num;

        public MyBook(String name, String book_id, int num) {
            super();
            this.name = name;
            this.book_id = book_id;
            this.num = num;
        }


        @Override
        public boolean equals(Object o) {
            if (o instanceof MyBook && o != null) {
                MyBook b = (MyBook) o;
                return name.equals(b.name) && book_id.equals(b.book_id);
            }
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return book_id.hashCode() + name.hashCode();
        }
    }

}
