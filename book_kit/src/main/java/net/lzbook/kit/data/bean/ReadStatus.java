package net.lzbook.kit.data.bean;

import com.dingyueads.sdk.Native.YQNativeAdInfo;

import net.lzbook.kit.utils.AppLog;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
    //中图广告宽度
    public int width_nativead_middle;
    //1 small ad,2 big ad,5 big_inChapter ad
    public int native_type;
    //本地广告y坐标
    public float y_nativead;
    //大图广告y坐标
    public float y_nativead_big;
    //大图广告宽度
    public int width_nativead_big;
    //大图广告高度
    public int height_nativead_big;
    //当前广告信息，章末上
    public YQNativeAdInfo currentAdInfo;
    //当前广告信息，章末下
    public YQNativeAdInfo currentAdInfoDown;
    //当前广告图片
    public YQNativeAdInfo currentAdInfo_image;
    //当前章节内广告信息
    public YQNativeAdInfo currentAdInfo_in_chapter;
    //储存5-2广告位信息的容器
    public ArrayList<HashMap<YQNativeAdInfo, Bitmap>> containerInChapter = new ArrayList<>();
    //广告布局
    public ViewGroup novel_basePageView;
    //章节内大图广告Bitmap
    public Bitmap ad_bimap_big_inChapter;
    //封面页curl
    public String firstChapterCurl = "";
    //大图广告Bitmap
    private Bitmap ad_bitmap_big;
    //小图广告Bitmap
    private Bitmap ad_bitmap;
    //中图广告Bitmap
    private Bitmap ad_bitmap_middle;
    //中图广告Bitmap下
    private Bitmap ad_bitmap_middle_down;

    //5-1&5-2 InMobi广告的父容器
    private ViewGroup ad_inmobi_parent;
    //存储5-2广告信息的容器
    public ArrayList<YQNativeAdInfo> inMobiViewContainerInChapter = new ArrayList<>();
    public boolean shouldShowInMobiAdView = false;
    public boolean isInMobiViewClicking = false;

    //自动阅读速度
    private int _autoReadSpeed;
    private SharedPreferences preferences;

    public ReadStatus(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        _autoReadSpeed = preferences.getInt(kKeyAutoReadSpeed, kDefaultAutoReadSpeed);
    }

    public int autoReadSpeed() {
        return _autoReadSpeed;
    }

    public void setAutoReadSpeed(int speed) {
        if (speed == _autoReadSpeed || speed < kMinAutoReadSpeed || speed > kMaxAutoReadSpeed) {
            return;
        }

        _autoReadSpeed = speed;
        Editor editor = preferences.edit();
        editor.putInt(kKeyAutoReadSpeed, _autoReadSpeed);
        editor.apply();
    }

    public double autoReadFactor() {
        if (_autoReadSpeed == kDefaultAutoReadSpeed) {
            return 1;
        }

        double d = _autoReadSpeed * 1.0 / kDefaultAutoReadSpeed;
        if (_autoReadSpeed < kDefaultAutoReadSpeed) {
            return Math.sqrt(d);
        } else {
            return d * d;
        }
    }

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

    public Bitmap getAd_bitmap_middle() {
        return ad_bitmap_middle;
    }

    public void setAd_bitmap_middle(Bitmap ad_bitmap_middle) {
        if (this.ad_bitmap_middle != null && !this.ad_bitmap_middle.isRecycled()) {
            AppLog.e("ReadStatus", "ReadStatus : recycle ad_bitmap_middle");
            this.ad_bitmap_middle.recycle();
            this.ad_bitmap_middle = null;
        }
        this.ad_bitmap_middle = ad_bitmap_middle;
    }

    public Bitmap getAd_bitmap_middle_down() {
        return ad_bitmap_middle_down;
    }

    public void setAd_bitmap_middle_down(Bitmap ad_bitmap_middle_down) {
        if (this.ad_bitmap_middle_down != null && !this.ad_bitmap_middle_down.isRecycled()) {
            AppLog.e("ReadStatus", "ReadStatus : recycle ad_bitmap_middle_down");
            this.ad_bitmap_middle_down.recycle();
            this.ad_bitmap_middle_down = null;
        }
        this.ad_bitmap_middle_down = ad_bitmap_middle_down;
    }
    public Bitmap getAd_bitmap_big() {
        return ad_bitmap_big;
    }

    public void setAd_bitmap_big(Bitmap ad_bitmap_big) {
        if (this.ad_bitmap_big != null && !this.ad_bitmap_big.isRecycled()) {
            AppLog.e("ReadStatus", "ReadStatus : recycle ad_bitmap_big");
            this.ad_bitmap_big.recycle();
            this.ad_bitmap_big = null;
        }
        this.ad_bitmap_big = ad_bitmap_big;
    }

//    public Bitmap getAd_bitmap_big_inChapter() {
//        return ad_bimap_big_inChapter;
//    }

    public void setAd_bitmap_big_inChapter(Bitmap ad_bimap_big_inChapter) {
        if (this.ad_bimap_big_inChapter != null && !this.ad_bimap_big_inChapter.isRecycled()) {
            this.ad_bimap_big_inChapter.recycle();
            this.ad_bimap_big_inChapter = null;
        }
        this.ad_bimap_big_inChapter = ad_bimap_big_inChapter;
    }

    public Bitmap getAd_bitmap() {
        return ad_bitmap;
    }

    public void setAd_bitmap(Bitmap ad_bitmap) {
        if (this.ad_bitmap != null && !this.ad_bitmap.isRecycled()) {
            AppLog.e("ReadStatus", "ReadStatus : recycle ad_bitmap");
            this.ad_bitmap.recycle();
            this.ad_bitmap = null;
        }
        this.ad_bitmap = ad_bitmap;
    }

    public ViewGroup getAd_inmobi_parent() {
        return ad_inmobi_parent;
    }

    public void setAd_inmobi_parent(ViewGroup ad_inmobi_parent) {
        this.ad_inmobi_parent = ad_inmobi_parent;
    }

    public void recycleResourceNew() {
        if (this.ad_bimap_big_inChapter != null && !this.ad_bimap_big_inChapter.isRecycled()) {
            this.ad_bimap_big_inChapter.recycle();
            this.ad_bimap_big_inChapter = null;
        }

        if (containerInChapter != null && containerInChapter.size() > 0) {
            for (int i = 0; i < containerInChapter.size(); i++) {
                HashMap<YQNativeAdInfo, Bitmap> hashMap = containerInChapter.get(i);
                if (hashMap != null && hashMap.entrySet() != null) {
                    Iterator<Map.Entry<YQNativeAdInfo, Bitmap>> iterator = hashMap.entrySet().iterator();
                    if (iterator == null) continue;
                    while (iterator.hasNext()) {
                        Map.Entry<YQNativeAdInfo, Bitmap> map = iterator.next();
                        Bitmap bitmap = map.getValue();
                        if (bitmap != null && !bitmap.isRecycled()) {
                            bitmap.recycle();
                            bitmap = null;
                        }
                    }
                    hashMap.clear();
                    hashMap = null;
                }
            }
            containerInChapter.clear();
        }

        /*//清除InMobi5-2的广告
        if (inMobiViewContainerInChapter != null && !inMobiViewContainerInChapter.isEmpty()) {
            for(YQNativeAdInfo adInfo : inMobiViewContainerInChapter){
                adInfo.getInMobiNative().destroy();
            }
            inMobiViewContainerInChapter.clear();
            System.gc();
        }*/

        //清除InMobi5-1的广告
//        if(currentAdInfo_image != null && currentAdInfo_image.getInMobiNative() != null) {
//            currentAdInfo_image.getInMobiNative().destroy();
//        }
    }

    public void recycleResource() {
        if (this.preferences != null) {
            this.preferences = null;
        }

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

        if (this.currentAdInfo != null) {
            this.currentAdInfo = null;
        }

        if (this.currentAdInfoDown != null) {
            this.currentAdInfoDown = null;
        }
        if (this.currentAdInfo_image != null) {
            this.currentAdInfo_image = null;
        }

        if (this.currentAdInfo_in_chapter != null) {
            this.currentAdInfo_in_chapter = null;
        }

        if (this.novel_basePageView != null) {
            this.novel_basePageView = null;
        }

        if (this.ad_bitmap != null) {
            this.ad_bitmap.recycle();
            this.ad_bitmap = null;
        }

        if (this.ad_bitmap_middle != null) {
            this.ad_bitmap_middle.recycle();
            this.ad_bitmap_middle = null;
        }

        if (this.ad_bitmap_middle_down != null) {
            this.ad_bitmap_middle_down.recycle();
            this.ad_bitmap_middle_down = null;
        }
        if (this.ad_bitmap_big != null) {
            this.ad_bitmap_big.recycle();
            this.ad_bitmap_big = null;
        }

        if (this.ad_bimap_big_inChapter != null) {
            this.ad_bimap_big_inChapter.recycle();
            this.ad_bimap_big_inChapter = null;
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
        if (width_nativead_middle != that.width_nativead_middle) return false;
        if (native_type != that.native_type) return false;
        if (Float.compare(that.y_nativead, y_nativead) != 0) return false;
        if (Float.compare(that.y_nativead_big, y_nativead_big) != 0) return false;
        if (width_nativead_big != that.width_nativead_big) return false;
        if (height_nativead_big != that.height_nativead_big) return false;
        if (_autoReadSpeed != that._autoReadSpeed) return false;
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
        if (currentAdInfo != null ? !currentAdInfo.equals(that.currentAdInfo) : that.currentAdInfo != null)
            return false;
        if (currentAdInfoDown != null ? !currentAdInfoDown.equals(that.currentAdInfoDown) : that.currentAdInfoDown != null)
            return false; 
        if (currentAdInfo_image != null ? !currentAdInfo_image.equals(that.currentAdInfo_image) : that.currentAdInfo_image != null)
            return false;
        if (currentAdInfo_in_chapter != null ? !currentAdInfo_in_chapter.equals(that.currentAdInfo_in_chapter) : that.currentAdInfo_in_chapter != null)
            return false;
        if (novel_basePageView != null ? !novel_basePageView.equals(that.novel_basePageView) : that.novel_basePageView != null)
            return false;
        if (ad_bimap_big_inChapter != null ? !ad_bimap_big_inChapter.equals(that.ad_bimap_big_inChapter) : that.ad_bimap_big_inChapter != null)
            return false;
        if (ad_bitmap_big != null ? !ad_bitmap_big.equals(that.ad_bitmap_big) : that.ad_bitmap_big != null)
            return false;
        if (ad_bitmap != null ? !ad_bitmap.equals(that.ad_bitmap) : that.ad_bitmap != null)
            return false;
        if (ad_bitmap_middle != null ? !ad_bitmap_middle.equals(that.ad_bitmap_middle) : that.ad_bitmap_middle != null)
            return false;
        if (ad_bitmap_middle_down != null ? !ad_bitmap_middle_down.equals(that.ad_bitmap_middle_down) : that.ad_bitmap_middle_down != null)
            return false;
        if (preferences != null ? !preferences.equals(that.preferences) : that.preferences != null)
            return false;
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
        result = 31 * result + width_nativead_middle;
        result = 31 * result + native_type;
        result = 31 * result + (y_nativead != +0.0f ? Float.floatToIntBits(y_nativead) : 0);
        result = 31 * result + (y_nativead_big != +0.0f ? Float.floatToIntBits(y_nativead_big) : 0);
        result = 31 * result + width_nativead_big;
        result = 31 * result + height_nativead_big;
        result = 31 * result + (currentAdInfo != null ? currentAdInfo.hashCode() : 0);
        result = 31 * result + (currentAdInfoDown != null ? currentAdInfoDown.hashCode() : 0);												  
        result = 31 * result + (currentAdInfo_image != null ? currentAdInfo_image.hashCode() : 0);
        result = 31 * result + (currentAdInfo_in_chapter != null ? currentAdInfo_in_chapter.hashCode() : 0);
        result = 31 * result + (novel_basePageView != null ? novel_basePageView.hashCode() : 0);
        result = 31 * result + (ad_bimap_big_inChapter != null ? ad_bimap_big_inChapter.hashCode() : 0);
        result = 31 * result + (ad_bitmap_big != null ? ad_bitmap_big.hashCode() : 0);
        result = 31 * result + (ad_bitmap != null ? ad_bitmap.hashCode() : 0);
        result = 31 * result + (ad_bitmap_middle != null ? ad_bitmap_middle.hashCode() : 0);
        result = 31 * result + _autoReadSpeed;
        result = 31 * result + (preferences != null ? preferences.hashCode() : 0);
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
                ", width_nativead_middle=" + width_nativead_middle +
                ", native_type=" + native_type +
                ", y_nativead=" + y_nativead +
                ", y_nativead_big=" + y_nativead_big +
                ", width_nativead_big=" + width_nativead_big +
                ", height_nativead_big=" + height_nativead_big +
                ", currentAdInfo=" + currentAdInfo +
                ", currentAdInfoDown=" + currentAdInfoDown +
                ", currentAdInfo_image=" + currentAdInfo_image +
                ", currentAdInfo_in_chapter=" + currentAdInfo_in_chapter +
                ", novel_basePageView=" + novel_basePageView +
                ", ad_bimap_big_inChapter=" + ad_bimap_big_inChapter +
                ", ad_bitmap_big=" + ad_bitmap_big +
                ", ad_bitmap=" + ad_bitmap +
                ", ad_bitmap_middle=" + ad_bitmap_middle +
                ", _autoReadSpeed=" + _autoReadSpeed +
                ", preferences=" + preferences +
                ", firstChapterCurl='" + firstChapterCurl + '\'' +
                '}';
    }
}
