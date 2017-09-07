package net.lzbook.kit.book.download;

public enum DownloadState {

    NOSTART("NOSTART"),
    DOWNLOADING("DOWNLOADING"),
    WAITTING("WAITTING"),
    PAUSEED("PAUSEED"),
    REFRESH("REFRESH"),
    FINISH("FINISH"),
    LOCKED("LOCKED"),
    NONE_NETWORK("NONE_NETWORK");

    private String tag;

    DownloadState(String t) {
        tag = t;
    }

    public String getTag() {
        return tag;
    }

}
