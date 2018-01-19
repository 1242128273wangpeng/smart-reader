package net.lzbook.kit.data.bean;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import java.util.ArrayList;

public class ReadStatus {
    //最小阅读速度
    public static final int kMinAutoReadSpeed = 1;
    //最大自动阅读速度
    public static final int kMaxAutoReadSpeed = 20;
    //默认自动阅读速度
    private static final int kDefaultAutoReadSpeed = 16;
    //常量，用于在SP中保存阅读速度时使用
    private static final String kKeyAutoReadSpeed = "ars";
    //请求的数据信息
    public RequestItem requestItem;
    //后台配置config信息
    //public Config requestConfig;
    //小说id
    public String book_id;
    //小说组id
    public int gid;
    //小说id
    public int nid;
    //章节总数
    public int chapterCount;
    //主菜单显示开关
    public boolean isMenuShow;
    //判断是否正在加载
    public boolean isLoading;
    //当前阅读页
    public int currentPage;
    //当前已阅读的页数
    public int pageCount;
    //（替代sort）表示当前是第几章
    public int sequence;
    //书签偏移量
    public int offset;
    //章节名
    public String chapterName;
    //小说名
    public String bookName;
    //小说作者
    public String bookAuthor;
    //当前小说名称
    public String bookSource;
    //当前章节数量
    public ArrayList<ArrayList<NovelLineBean>> mLineList;
    //统计打点用，,获取当前页面的字符数，每次翻页会先至0
    public int currentPageConentLength;
    //统计打点用，书籍源，中间有切换源则多个源使用分隔符"`"进行连接
    public String source_ids;
    //保存开始阅读时间，方便当用户退出阅读时 打点统计
    public long startReadTime;
    //统计打点用  如果是从上一章节跳转到下一章节 记录上一章节的最后一页的数据
    public int lastSequenceRemark;
    public int lastCurrentPageRemark;
    public int lastPageCount;
    public ArrayList<NovelLineBean> bookNameList;
    public String lastChapterId;//记录翻页前一页的章节ID
    //章节名称列表
    public ArrayList<NovelLineBean> chapterNameList;
    //小说bean
    public Book book;
    //小说阅读进度
    public int novel_progress;
    //屏幕宽度
    public int screenWidth;
    //屏幕高度
    public int screenHeight;
    //屏幕密度
    public float screenDensity;
    //屏幕缩放比例
    public float screenScaledDensity;
    //显示底部设置view开关
    public boolean isCanDrawFootView;
    //广告宽度
    public int width_nativead;
    //广告高度
    public int height_nativead;
    //中图广告高度
    public int height_middle_nativead;

    //封面页curl
    public String firstChapterCurl = "";


//    //自动阅读速度
//    private int _autoReadSpeed;
//    private SharedPreferences preferences;

    public ReadStatus(Context context) {
//        preferences = PreferenceManager.getDefaultSharedPreferences(context);
//        _autoReadSpeed = preferences.getInt(kKeyAutoReadSpeed, kDefaultAutoReadSpeed);
    }

//    public int autoReadSpeed() {
//        return _autoReadSpeed;
//    }

//    public void setAutoReadSpeed(int speed) {
//        if (speed == _autoReadSpeed || speed < kMinAutoReadSpeed || speed > kMaxAutoReadSpeed) {
//            return;
//        }
//
//        _autoReadSpeed = speed;
//        if (preferences != null) {
//            Editor editor = preferences.edit();
//            editor.putInt(kKeyAutoReadSpeed, _autoReadSpeed);
//            editor.apply();
//        }
//    }

//    public double autoReadFactor() {
//        if (_autoReadSpeed == kDefaultAutoReadSpeed) {
//            return 1;
//        }
//
//        double d = _autoReadSpeed * 1.0 / kDefaultAutoReadSpeed;
//        if (_autoReadSpeed < kDefaultAutoReadSpeed) {
//            return Math.sqrt(d);
//        } else {
//            return d * d;
//        }
//    }

    public RequestItem getRequestItem() {
        if (this.requestItem == null) {
            if (book != null) {
                requestItem = new RequestItem();
                requestItem.book_id = book.book_id;
//                requestItem.nid = book.nid;
                requestItem.book_source_id = book.book_source_id;
                requestItem.host = book.site;
                requestItem.parameter = book.parameter;
                requestItem.extra_parameter = book.extra_parameter;
                requestItem.name = book.name;
                requestItem.author = book.author;
            }
        }
        return requestItem;
    }

    public void setRequestItem(RequestItem requestItem) {
        if (requestItem != null) {
            this.requestItem = requestItem;
        }
    }
    public void recycleResource() {
//        if (this.preferences != null) {
//            this.preferences = null;
//        }

        if (this.mLineList != null) {
            this.mLineList.clear();
            this.mLineList = null;
        }

        if (this.bookNameList != null) {
            this.bookNameList.clear();
            this.bookNameList = null;
        }

        if (this.chapterNameList != null) {
            this.chapterNameList.clear();
            this.chapterNameList = null;
        }


    }

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;

        ReadStatus that = (ReadStatus) object;

        if (gid != that.gid) return false;
        if (nid != that.nid) return false;
        if (chapterCount != that.chapterCount) return false;
        if (isMenuShow != that.isMenuShow) return false;
        if (isLoading != that.isLoading) return false;
        if (currentPage != that.currentPage) return false;
        if (pageCount != that.pageCount) return false;
        if (sequence != that.sequence) return false;
        if (offset != that.offset) return false;
        if (novel_progress != that.novel_progress) return false;
        if (screenWidth != that.screenWidth) return false;
        if (screenHeight != that.screenHeight) return false;
        if (Float.compare(that.screenDensity, screenDensity) != 0) return false;
        if (Float.compare(that.screenScaledDensity, screenScaledDensity) != 0) return false;
        if (isCanDrawFootView != that.isCanDrawFootView) return false;
        if (width_nativead != that.width_nativead) return false;
        if (height_nativead != that.height_nativead) return false;
        if (height_middle_nativead != that.height_middle_nativead) return false;
//        if (_autoReadSpeed != that._autoReadSpeed) return false;
        if (requestItem != null ? !requestItem.equals(that.requestItem) : that.requestItem != null)
            return false;
        /*if (requestConfig != null ? !requestConfig.equals(that.requestConfig) : that.requestConfig != null)
            return false;*/
        if (book_id != null ? !book_id.equals(that.book_id) : that.book_id != null) return false;
        if (chapterName != null ? !chapterName.equals(that.chapterName) : that.chapterName != null)
            return false;
        if (bookName != null ? !bookName.equals(that.bookName) : that.bookName != null)
            return false;
        if (bookAuthor != null ? !bookAuthor.equals(that.bookAuthor) : that.bookAuthor != null)
            return false;
        if (bookSource != null ? !bookSource.equals(that.bookSource) : that.bookSource != null)
            return false;
        if (mLineList != null ? !mLineList.equals(that.mLineList) : that.mLineList != null)
            return false;
        if (bookNameList != null ? !bookNameList.equals(that.bookNameList) : that.bookNameList != null)
            return false;
        if (chapterNameList != null ? !chapterNameList.equals(that.chapterNameList) : that.chapterNameList != null)
            return false;
        if (book != null ? !book.equals(that.book) : that.book != null) return false;
//        if (preferences != null ? !preferences.equals(that.preferences) : that.preferences != null)
//            return false;
        if (firstChapterCurl != null ? !firstChapterCurl.equals(that.firstChapterCurl) : that.firstChapterCurl != null)
            return false;

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (requestItem != null ? requestItem.hashCode() : 0);
        //result = 31 * result + (requestConfig != null ? requestConfig.hashCode() : 0);
        result = 31 * result + (book_id != null ? book_id.hashCode() : 0);
        result = 31 * result + gid;
        result = 31 * result + nid;
        result = 31 * result + chapterCount;
        result = 31 * result + (isMenuShow ? 1 : 0);
        result = 31 * result + (isLoading ? 1 : 0);
        result = 31 * result + currentPage;
        result = 31 * result + pageCount;
        result = 31 * result + sequence;
        result = 31 * result + offset;
        result = 31 * result + (chapterName != null ? chapterName.hashCode() : 0);
        result = 31 * result + (bookName != null ? bookName.hashCode() : 0);
        result = 31 * result + (bookAuthor != null ? bookAuthor.hashCode() : 0);
        result = 31 * result + (bookSource != null ? bookSource.hashCode() : 0);
        result = 31 * result + (mLineList != null ? mLineList.hashCode() : 0);
        result = 31 * result + (bookNameList != null ? bookNameList.hashCode() : 0);
        result = 31 * result + (chapterNameList != null ? chapterNameList.hashCode() : 0);
        result = 31 * result + (book != null ? book.hashCode() : 0);
        result = 31 * result + novel_progress;
        result = 31 * result + screenWidth;
        result = 31 * result + screenHeight;
        result = 31 * result + (screenDensity != +0.0f ? Float.floatToIntBits(screenDensity) : 0);
        result = 31 * result + (screenScaledDensity != +0.0f ? Float.floatToIntBits(screenScaledDensity) : 0);
        result = 31 * result + (isCanDrawFootView ? 1 : 0);
        result = 31 * result + width_nativead;
        result = 31 * result + height_nativead;
        result = 31 * result + height_middle_nativead;
//        result = 31 * result + _autoReadSpeed;
//        result = 31 * result + (preferences != null ? preferences.hashCode() : 0);
        result = 31 * result + (firstChapterCurl != null ? firstChapterCurl.hashCode() : 0);
        return result;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "ReadStatus{" +
                "requestItem=" + requestItem +
                //", requestConfig=" + requestConfig +
                ", book_id='" + book_id + '\'' +
                ", gid=" + gid +
                ", nid=" + nid +
                ", chapterCount=" + chapterCount +
                ", isMenuShow=" + isMenuShow +
                ", isLoading=" + isLoading +
                ", currentPage=" + currentPage +
                ", pageCount=" + pageCount +
                ", sequence=" + sequence +
                ", offset=" + offset +
                ", chapterName='" + chapterName + '\'' +
                ", bookName='" + bookName + '\'' +
                ", bookAuthor='" + bookAuthor + '\'' +
                ", bookSource='" + bookSource + '\'' +
                ", mLineList=" + mLineList +
                ", bookNameList=" + bookNameList +
                ", chapterNameList=" + chapterNameList +
                ", book=" + book +
                ", novel_progress=" + novel_progress +
                ", screenWidth=" + screenWidth +
                ", screenHeight=" + screenHeight +
                ", screenDensity=" + screenDensity +
                ", screenScaledDensity=" + screenScaledDensity +
                ", isCanDrawFootView=" + isCanDrawFootView +
                ", width_nativead=" + width_nativead +
                ", height_nativead=" + height_nativead +
                ", height_middle_nativead=" + height_middle_nativead +
//                ", _autoReadSpeed=" + _autoReadSpeed +
//                ", preferences=" + preferences +
                ", firstChapterCurl='" + firstChapterCurl + '\'' +
                '}';
    }
}
