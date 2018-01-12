package com.intelligent.reader.read.selection;

/**
 * @author lijun Lee
 * @desc 已选词信息
 * @mail jun_li@dingyuegroup.cn
 * @data 2018/1/11 17:55
 */

public class SelectableInfo {
    private Selectable selectable;

    private String key;

    public SelectableInfo(Selectable selectable) {
//        this.start = 0;
//        this.end = 0;
//        this.selectedText = "";
        this.selectable = selectable;
//        this.text = selectable.getText();
        this.key = selectable.getKey();
    }

    public Selectable getSelectable() {
        return selectable;
    }

    public void setSelectable(Selectable selectable) {
        this.selectable = selectable;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
