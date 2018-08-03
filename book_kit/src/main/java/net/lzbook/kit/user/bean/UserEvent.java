package net.lzbook.kit.user.bean;

/**
 * 项目名称：xzq
 * 类描述：
 * 创建人：Administrator
 * 创建时间：2017/8/11 0011
 */

public class UserEvent {

    public static String LOGIN_OUT_DATE = "login_out_date";
    public static String LOGIN_INVALID = "login_invalid";
    public static String REFRESH_BOOKSHELF = "refresh_bookshelf";

    private String msg;

    public UserEvent(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
