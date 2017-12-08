package net.lzbook.kit.purchase;

/**
 * Created by Administrator on 2017/9/10 0010.
 */

public class AutoPurchaseBean {


    /**
     * state : true
     * code : 20000
     */

    private boolean state;
    private String code;

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
