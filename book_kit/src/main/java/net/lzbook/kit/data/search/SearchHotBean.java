package net.lzbook.kit.data.search;

import java.io.Serializable;
import java.util.List;

/**
 * Function：搜索热词(一期)
 *
 * Created by JoannChen on 2018/7/19 0019 16:20
 * E-mail:yongzuo_chen@dingyuegroup.cn
 */
public class SearchHotBean implements Serializable{


    /**
     * suc : 200
     * errCode : null
     * data : [{"wordType":0,"word":"异界大陆","sort":0},{"wordType":0,"word":"冰山","sort":1}]
     */

    private String suc;
    private Object errCode;
    private List<DataBean> data;

    public String getSuc() {
        return suc;
    }

    public void setSuc(String suc) {
        this.suc = suc;
    }

    public Object getErrCode() {
        return errCode;
    }

    public void setErrCode(Object errCode) {
        this.errCode = errCode;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        public DataBean(int wordType, String word, int sort) {
            this.wordType = wordType;
            this.word = word;
            this.sort = sort;
        }

        /**
         * wordType : 0
         * word : 异界大陆
         * sort : 0
         */



        private int wordType;
        private String word;
        private int sort;

        public int getWordType() {
            return wordType;
        }

        public void setWordType(int wordType) {
            this.wordType = wordType;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public int getSort() {
            return sort;
        }

        public void setSort(int sort) {
            this.sort = sort;
        }

        @Override
        public String toString() {
            return "DataBean{" +
                    "wordType=" + wordType +
                    ", word='" + word + '\'' +
                    ", sort=" + sort +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "SearchHotBean{" +
                "suc='" + suc + '\'' +
                ", errCode=" + errCode +
                ", data=" + data +
                '}';
    }
}
