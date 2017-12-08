package net.lzbook.kit.purchase;

/**
 * 项目名称：kdqbxsBookPay
 * 类描述：
 * 创建人：Administrator
 * 创建时间：2017/8/23 0023
 */

public class ChapterPriceInfo {

    /**
     * state : true
     * code : 20000
     * money : 36
     * buy_num : 1
     * discount : 100
     */

    private boolean state;
    private String code;
    private int money;
    private int buy_num;
    private int discount;

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

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getBuy_num() {
        return buy_num;
    }

    public void setBuy_num(int buy_num) {
        this.buy_num = buy_num;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }
}
