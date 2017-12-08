package net.lzbook.kit.purchase;

import java.util.List;

/**
 * 项目名称：kdqbxsBookPay
 * 类描述：
 * 创建人：Administrator
 * 创建时间：2017/8/19 0019
 */

public class MutiPurchaseDialogBean {

    /**
     * book_id : 59ca4d0d5649f35573988d0b
     * section_margin : 172
     * gold : 0
     * section_obj : [{"id":1,"bookId":"","break_":90,"chapterNum":10,"packageName":"","createTime":1501658621000,"updateTime":1501658624000},{"id":2,"bookId":"","break_":80,"chapterNum":50,"packageName":"","createTime":1501658626000,"updateTime":1501658627000},{"id":3,"bookId":"","break_":70,"chapterNum":100,"packageName":"","createTime":1501658629000,"updateTime":1501658631000}]
     * uid : 0
     * code : 20000
     * state : true
     * pay_first_chapter_name : 第二十二章:林大厨师
     * pay_first_chapter_id : 59cbb20c5649f30e53ff27c8
     */

    private String book_id;
    private int section_margin;
    private int gold;
    private int uid;
    private String code;
    private boolean state;
    private String pay_first_chapter_name;
    private String pay_first_chapter_id;
    private List<SectionObjBean> section_obj;

    public String getBook_id() {
        return book_id;
    }

    public void setBook_id(String book_id) {
        this.book_id = book_id;
    }

    public int getSection_margin() {
        return section_margin;
    }

    public void setSection_margin(int section_margin) {
        this.section_margin = section_margin;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public String getPay_first_chapter_name() {
        return pay_first_chapter_name;
    }

    public void setPay_first_chapter_name(String pay_first_chapter_name) {
        this.pay_first_chapter_name = pay_first_chapter_name;
    }

    public String getPay_first_chapter_id() {
        return pay_first_chapter_id;
    }

    public void setPay_first_chapter_id(String pay_first_chapter_id) {
        this.pay_first_chapter_id = pay_first_chapter_id;
    }

    public List<SectionObjBean> getSection_obj() {
        return section_obj;
    }

    public void setSection_obj(List<SectionObjBean> section_obj) {
        this.section_obj = section_obj;
    }

    public static class SectionObjBean {
        /**
         * id : 1
         * bookId :
         * break_ : 90
         * chapterNum : 10
         * packageName :
         * createTime : 1501658621000
         * updateTime : 1501658624000
         */

        private int id;
        private String bookId;
        private int break_;
        private int chapterNum;
        private String packageName;
        private long createTime;
        private long updateTime;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getBookId() {
            return bookId;
        }

        public void setBookId(String bookId) {
            this.bookId = bookId;
        }

        public int getBreak_() {
            return break_;
        }

        public void setBreak_(int break_) {
            this.break_ = break_;
        }

        public int getChapterNum() {
            return chapterNum;
        }

        public void setChapterNum(int chapterNum) {
            this.chapterNum = chapterNum;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public long getCreateTime() {
            return createTime;
        }

        public void setCreateTime(long createTime) {
            this.createTime = createTime;
        }

        public long getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(long updateTime) {
            this.updateTime = updateTime;
        }
    }
}
