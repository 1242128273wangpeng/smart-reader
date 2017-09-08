package net.lzbook.kit.data.bean;

/**
 * Created by Administrator on 2017\9\5 0005.
 */

public class SearchCommonBean {

    private String suggest;
    private String wordtype;
    private String pv;

    public String getSuggest() {
        return suggest;
    }

    public void setSuggest(String suggest) {
        this.suggest = suggest;
    }

    public String getWordtype() {
        return wordtype;
    }

    public void setWordtype(String wordtype) {
        this.wordtype = wordtype;
    }

    public String getPv() {
        return pv;
    }

    public void setPv(String pv) {
        this.pv = pv;
    }

    @Override
    public String toString() {
        return "SearchCommonBean{" +
                "suggest='" + suggest + '\'' +
                ", wordtype='" + wordtype + '\'' +
                ", pv='" + pv + '\'' +
                '}';
    }
}
