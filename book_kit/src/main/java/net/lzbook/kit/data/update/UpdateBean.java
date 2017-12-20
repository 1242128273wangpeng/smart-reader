package net.lzbook.kit.data.update;

import java.util.List;

/**
 * Created by yuchao on 2017/11/1 0001.
 */

public class UpdateBean {

    public final static String REQUEST_SUCCESS = "20000";

    private String respCode;
    private String message;
    private DataBean data;

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private List<FixBookBean> fix_book;
        private List<FixContentBean> fix_content;
        private List<UpdateBookBean> update_book;

        public List<FixBookBean> getFix_book() {
            return fix_book;
        }

        public void setFix_book(List<FixBookBean> fix_book) {
            this.fix_book = fix_book;
        }

        public List<FixContentBean> getFix_content() {
            return fix_content;
        }

        public void setFix_content(List<FixContentBean> fix_content) {
            this.fix_content = fix_content;
        }

        public List<UpdateBookBean> getUpdate_book() {
            return update_book;
        }

        public void setUpdate_book(List<UpdateBookBean> update_book) {
            this.update_book = update_book;
        }

        public static class FixBookBean {
            /**
             * book_id : 58c08020df43fe69c00d6cdc
             * book_source_id : 58c08020df43fe69c00d6cdd
             * last_update : 1509095425016
             * list_version : 1
             * c_version : 1
             */

            private String book_id;
            private String book_source_id;
            private long last_update;
            private int list_version;
            private int c_version;

            public String getBook_id() {
                return book_id;
            }

            public void setBook_id(String book_id) {
                this.book_id = book_id;
            }

            public String getBook_source_id() {
                return book_source_id;
            }

            public void setBook_source_id(String book_source_id) {
                this.book_source_id = book_source_id;
            }

            public long getLast_update() {
                return last_update;
            }

            public void setLast_update(long last_update) {
                this.last_update = last_update;
            }

            public int getList_version() {
                return list_version;
            }

            public void setList_version(int list_version) {
                this.list_version = list_version;
            }

            public int getC_version() {
                return c_version;
            }

            public void setC_version(int c_version) {
                this.c_version = c_version;
            }
        }

        public static class FixContentBean {
            /**
             * book_id : 58c08020df43fe69c00d6cdc
             * book_source_id : 58c08020df43fe69c00d6cdd
             * last_update : 1509095425016
             * list_version : 1
             * c_version : 1
             * dex : 0
             * chapters : [{"id":"59f17a6885b1ce043c5439b7","book_souce_id":"58c08020df43fe69c00d6cdd","name":"第七百八十九章 毒魂体","serial_number":844,"host":"www.basicnos.cn","url":"https://www.basicnos.cn/v3/book/chaptersContents?chapterId=59f17a6885b1ce043c5439b7","url1":null,"terminal":"WEB","status":"ENABLE","update_time":1508997736204,"word_count":4423,"vip":0,"price":0},{"id":"59f1c0af85b1ce043c54427b","book_souce_id":"58c08020df43fe69c00d6cdd","name":"第七百九十章 十叶花","serial_number":845,"host":"www.basicnos.cn","url":"https://www.basicnos.cn/v3/book/chaptersContents?chapterId=59f1c0af85b1ce043c54427b","url1":null,"terminal":"WEB","status":"ENABLE","update_time":1509015727637,"word_count":4026,"vip":0,"price":0},{"id":"59f2cc2485b1ce043c546458","book_souce_id":"58c08020df43fe69c00d6cdd","name":"第七百九十一章 价格","serial_number":846,"host":"www.basicnos.cn","url":"https://www.basicnos.cn/v3/book/chaptersContents?chapterId=59f2cc2485b1ce043c546458","url1":null,"terminal":"WEB","status":"ENABLE","update_time":1509084196582,"word_count":3485,"vip":0,"price":0}]
             */

            private String book_id;
            private String book_source_id;
            private long last_update;
            private int list_version;
            private int c_version;
            private int dex;
            private List<ChaptersBean> chapters;

            public String getBook_id() {
                return book_id;
            }

            public void setBook_id(String book_id) {
                this.book_id = book_id;
            }

            public String getBook_source_id() {
                return book_source_id;
            }

            public void setBook_source_id(String book_source_id) {
                this.book_source_id = book_source_id;
            }

            public long getLast_update() {
                return last_update;
            }

            public void setLast_update(long last_update) {
                this.last_update = last_update;
            }

            public int getList_version() {
                return list_version;
            }

            public void setList_version(int list_version) {
                this.list_version = list_version;
            }

            public int getC_version() {
                return c_version;
            }

            public void setC_version(int c_version) {
                this.c_version = c_version;
            }

            public int getDex() {
                return dex;
            }

            public void setDex(int dex) {
                this.dex = dex;
            }

            public List<ChaptersBean> getChapters() {
                return chapters;
            }

            public void setChapters(List<ChaptersBean> chapters) {
                this.chapters = chapters;
            }

            public static class ChaptersBean {
                /**
                 * id : 59f17a6885b1ce043c5439b7
                 * book_souce_id : 58c08020df43fe69c00d6cdd
                 * name : 第七百八十九章 毒魂体
                 * serial_number : 844
                 * host : www.basicnos.cn
                 * url : https://www.basicnos.cn/v3/book/chaptersContents?chapterId=59f17a6885b1ce043c5439b7
                 * url1 : null
                 * terminal : WEB
                 * status : ENABLE
                 * update_time : 1508997736204
                 * word_count : 4423
                 * vip : 0
                 * price : 0
                 */

                private String id;
                private String book_souce_id;
                private String name;
                private int serial_number;
                private String host;
                private String url;
                private Object url1;
                private String terminal;
                private String status;
                private long update_time;
                private int word_count;
                private int vip;
                private int price;

                public String getId() {
                    return id;
                }

                public void setId(String id) {
                    this.id = id;
                }

                public String getBook_souce_id() {
                    return book_souce_id;
                }

                public void setBook_souce_id(String book_souce_id) {
                    this.book_souce_id = book_souce_id;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public int getSerial_number() {
                    return serial_number;
                }

                public void setSerial_number(int serial_number) {
                    this.serial_number = serial_number;
                }

                public String getHost() {
                    return host;
                }

                public void setHost(String host) {
                    this.host = host;
                }

                public String getUrl() {
                    return url;
                }

                public void setUrl(String url) {
                    this.url = url;
                }

                public Object getUrl1() {
                    return url1;
                }

                public void setUrl1(Object url1) {
                    this.url1 = url1;
                }

                public String getTerminal() {
                    return terminal;
                }

                public void setTerminal(String terminal) {
                    this.terminal = terminal;
                }

                public String getStatus() {
                    return status;
                }

                public void setStatus(String status) {
                    this.status = status;
                }

                public long getUpdate_time() {
                    return update_time;
                }

                public void setUpdate_time(long update_time) {
                    this.update_time = update_time;
                }

                public int getWord_count() {
                    return word_count;
                }

                public void setWord_count(int word_count) {
                    this.word_count = word_count;
                }

                public int getVip() {
                    return vip;
                }

                public void setVip(int vip) {
                    this.vip = vip;
                }

                public int getPrice() {
                    return price;
                }

                public void setPrice(int price) {
                    this.price = price;
                }
            }
        }

        public static class UpdateBookBean {
            /**
             * book_id : 58c08020df43fe69c00d6cdc
             * book_source_id : 58c08020df43fe69c00d6cdd
             * last_update : 1509095425016
             * dex : 0
             * chapters : [{"id":"59f17a6885b1ce043c5439b7","book_souce_id":"58c08020df43fe69c00d6cdd","name":"第七百八十九章 毒魂体","serial_number":844,"host":"www.basicnos.cn","url":"https://www.basicnos.cn/v3/book/chaptersContents?chapterId=59f17a6885b1ce043c5439b7","url1":null,"terminal":"WEB","status":"ENABLE","update_time":1508997736204,"word_count":4423,"vip":0,"price":0},{"id":"59f1c0af85b1ce043c54427b","book_souce_id":"58c08020df43fe69c00d6cdd","name":"第七百九十章 十叶花","serial_number":845,"host":"www.basicnos.cn","url":"https://www.basicnos.cn/v3/book/chaptersContents?chapterId=59f1c0af85b1ce043c54427b","url1":null,"terminal":"WEB","status":"ENABLE","update_time":1509015727637,"word_count":4026,"vip":0,"price":0},{"id":"59f2cc2485b1ce043c546458","book_souce_id":"58c08020df43fe69c00d6cdd","name":"第七百九十一章 价格","serial_number":846,"host":"www.basicnos.cn","url":"https://www.basicnos.cn/v3/book/chaptersContents?chapterId=59f2cc2485b1ce043c546458","url1":null,"terminal":"WEB","status":"ENABLE","update_time":1509084196582,"word_count":3485,"vip":0,"price":0}]
             */

            private String book_id;
            private String book_source_id;
            private long last_update;
            private int dex;
            private List<ChaptersBeanX> chapters;

            public String getBook_id() {
                return book_id;
            }

            public void setBook_id(String book_id) {
                this.book_id = book_id;
            }

            public String getBook_source_id() {
                return book_source_id;
            }

            public void setBook_source_id(String book_source_id) {
                this.book_source_id = book_source_id;
            }

            public long getLast_update() {
                return last_update;
            }

            public void setLast_update(long last_update) {
                this.last_update = last_update;
            }

            public int getDex() {
                return dex;
            }

            public void setDex(int dex) {
                this.dex = dex;
            }

            public List<ChaptersBeanX> getChapters() {
                return chapters;
            }

            public void setChapters(List<ChaptersBeanX> chapters) {
                this.chapters = chapters;
            }

            public static class ChaptersBeanX {
                /**
                 * id : 59f17a6885b1ce043c5439b7
                 * book_souce_id : 58c08020df43fe69c00d6cdd
                 * name : 第七百八十九章 毒魂体
                 * serial_number : 844
                 * host : www.basicnos.cn
                 * url : https://www.basicnos.cn/v3/book/chaptersContents?chapterId=59f17a6885b1ce043c5439b7
                 * url1 : null
                 * terminal : WEB
                 * status : ENABLE
                 * update_time : 1508997736204
                 * word_count : 4423
                 * vip : 0
                 * price : 0
                 */

                private String id;
                private String book_souce_id;
                private String name;
                private int serial_number;
                private String host;
                private String url;
                private Object url1;
                private String terminal;
                private String status;
                private long update_time;
                private int word_count;
                private int vip;
                private int price;

                public String getId() {
                    return id;
                }

                public void setId(String id) {
                    this.id = id;
                }

                public String getBook_souce_id() {
                    return book_souce_id;
                }

                public void setBook_souce_id(String book_souce_id) {
                    this.book_souce_id = book_souce_id;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public int getSerial_number() {
                    return serial_number;
                }

                public void setSerial_number(int serial_number) {
                    this.serial_number = serial_number;
                }

                public String getHost() {
                    return host;
                }

                public void setHost(String host) {
                    this.host = host;
                }

                public String getUrl() {
                    return url;
                }

                public void setUrl(String url) {
                    this.url = url;
                }

                public Object getUrl1() {
                    return url1;
                }

                public void setUrl1(Object url1) {
                    this.url1 = url1;
                }

                public String getTerminal() {
                    return terminal;
                }

                public void setTerminal(String terminal) {
                    this.terminal = terminal;
                }

                public String getStatus() {
                    return status;
                }

                public void setStatus(String status) {
                    this.status = status;
                }

                public long getUpdate_time() {
                    return update_time;
                }

                public void setUpdate_time(long update_time) {
                    this.update_time = update_time;
                }

                public int getWord_count() {
                    return word_count;
                }

                public void setWord_count(int word_count) {
                    this.word_count = word_count;
                }

                public int getVip() {
                    return vip;
                }

                public void setVip(int vip) {
                    this.vip = vip;
                }

                public int getPrice() {
                    return price;
                }

                public void setPrice(int price) {
                    this.price = price;
                }
            }
        }
    }
}
