package net.lzbook.kit.data.bean;

import java.io.Serializable;

public class ParseJarBean implements Serializable{

    public boolean success;
    //返回的错误信息
    public int version;
    public String url;
    public String md5;
    public String dynamicPackage;

}
