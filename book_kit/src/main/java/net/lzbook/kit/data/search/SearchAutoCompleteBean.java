package net.lzbook.kit.data.search;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2017\9\5 0005.
 */

public class SearchAutoCompleteBean implements Serializable {


    /**
     * suc : 200
     * errCode : null
     * data : {"label":[{"suggest":"大主宰","wordtype":"label","pv":"9235"}],"name":[{"suggest":"大主宰","wordtype":"name","pv":"1372678"},{"suggest":"大主宰外传","wordtype":"name","pv":"517"},{"suggest":"混沌大主宰","wordtype":"name","pv":"479"},{"suggest":"斗破苍穹之大主宰","wordtype":"name","pv":"330"},{"suggest":"大主宰天蚕土豆","wordtype":"name","pv":"301"},{"suggest":"神通大主宰","wordtype":"name","pv":"297"},{"suggest":"诸天大主宰","wordtype":"name","pv":"233"},{"suggest":"大主宰_沦陷的书生","wordtype":"name","pv":"212"},{"suggest":"隋唐大主宰","wordtype":"name","pv":"211"},{"suggest":"大主宰之萧玄传奇","wordtype":"name","pv":"165"},{"suggest":"武动乾坤之大主宰","wordtype":"name","pv":"111"},{"suggest":"斗破大主宰","wordtype":"name","pv":"110"},{"suggest":"NPC大主宰","wordtype":"name","pv":"82"},{"suggest":"傲世大主宰","wordtype":"name","pv":"77"},{"suggest":"召唤大主宰","wordtype":"name","pv":"50"}],"authors":[{"suggest":"大主宰","wordtype":"author","pv":"2"},{"suggest":"幕后大主宰","wordtype":"author","pv":"1"}]}
     */

    private String suc;
    private Object errCode;
    private DataBean data;

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

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private List<LabelBean> label;
        private List<NameBean> name;
        private List<AuthorsBean> authors;

        public List<LabelBean> getLabel() {
            return label;
        }

        public void setLabel(List<LabelBean> label) {
            this.label = label;
        }

        public List<NameBean> getName() {
            return name;
        }

        public void setName(List<NameBean> name) {
            this.name = name;
        }

        public List<AuthorsBean> getAuthors() {
            return authors;
        }

        public void setAuthors(List<AuthorsBean> authors) {
            this.authors = authors;
        }

        public static class LabelBean {
            /**
             * suggest : 大主宰
             * wordtype : label
             * pv : 9235
             */

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
                return "LabelBean{" +
                        "suggest='" + suggest + '\'' +
                        ", wordtype='" + wordtype + '\'' +
                        ", pv='" + pv + '\'' +
                        '}';
            }
        }

        public static class NameBean {
            /**
             * suggest : 大主宰
             * wordtype : name
             * pv : 1372678
             */

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
                return "NameBean{" +
                        "suggest='" + suggest + '\'' +
                        ", wordtype='" + wordtype + '\'' +
                        ", pv='" + pv + '\'' +
                        '}';
            }
        }

        public static class AuthorsBean {
            /**
             * suggest : 大主宰
             * wordtype : author
             * pv : 2
             */

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
                return "AuthorsBean{" +
                        "suggest='" + suggest + '\'' +
                        ", wordtype='" + wordtype + '\'' +
                        ", pv='" + pv + '\'' +
                        '}';
            }
        }
    }

    @Override
    public String toString() {
        return "SearchAutoCompleteBean{" +
                "suc='" + suc + '\'' +
                ", errCode=" + errCode +
                ", data=" + data +
                '}';
    }
}
