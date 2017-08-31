package iyouqu.theme;

/**
 * 主题种类：主题一为默认主题；
 * 其他三种主题，可从SVN中拉取
 */
public enum ThemeMode {

    THEME1("THEME1", 1),
    THEME2("THEME2", 2),
    THEME3("THEME3", 3),
    THEME4("THEME4", 4),
    NIGHT("NIGHT", 5),
    DEFAULT("DEFAULT", 6); //用于保存用户上次选取的主题


    private String mName;
    private int mCode;

    private ThemeMode(String name, int code) {
        this.mName = name;
        this.mCode = code;
    }

    public int getCode() {
        return mCode;
    }

    public void setCode(int code) {
        this.mCode = code;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

}
