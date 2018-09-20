package net.lzbook.kit.utils.download;

public enum DownloadState {

    NOSTART("NOSTART"),
    DOWNLOADING("DOWNLOADING"),
    WAITTING("WAITTING"),
    WAITTING_WIFI("WAITTING_WIFI"),
    PAUSEED("PAUSEED"),
    FINISH("FINISH"),
    NONE_NETWORK("NONE_NETWORK");

    private String tag;

    DownloadState(String t) {
        tag = t;
    }

    public String getTag() {
        return tag;
    }

}
