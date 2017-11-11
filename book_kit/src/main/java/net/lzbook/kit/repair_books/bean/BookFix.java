package net.lzbook.kit.repair_books.bean;

/**
 * Created by yuchao on 2017/11/2 0002.
 */

public class BookFix {

    public String book_id;
    // 1--修复章节内容, 2--修复目录
    public int fix_type;
    public int list_version;
    public int c_version;
    public int dialog_flag = 0;

    @Override
    public String toString() {
        return "BookFix{" +
                "book_id='" + book_id + '\'' +
                ", fix_type=" + fix_type +
                ", list_version=" + list_version +
                ", c_version=" + c_version +
                ", dialog_flag=" + dialog_flag +
                '}';
    }
}
