package com.intelligent.reader.read.help;

public interface CallBack {
    //阅读的CallBack
    void onShowMenu(boolean show);

    void onShowAutoMenu(boolean show);

    void onCancelPage();

    void onResize();
}
