package net.lzbook.kit.purchase;

/**
 * Created by Administrator on 2017/11/21 0021.
 */

public class PurchaseResult {


    /**
     * state : false
     * code : 10005
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
