package net.lzbook.kit.request.own;

import net.lzbook.kit.constants.Constants;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.db.BookChapterDao;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.request.DataCache;
import net.lzbook.kit.request.RequestExecutorDefault;
import net.lzbook.kit.request.WriteFileFailException;
import net.lzbook.kit.utils.NetWorkUtils;

import android.text.TextUtils;

import java.util.Map;


/**
 * 获取章节内容抽象类
 */
public class OtherRequestChapterExecutor extends RequestExecutorDefault {

    protected boolean downloadFlag;

    protected BookDaoHelper bookDaoHelper;
    protected BookChapterDao bookChapterDao;

    public static final String CACHE_EXIST = "isChapterExists";

    private OtherRequestChapter requestChapter;

    public OtherRequestChapterExecutor(BookDaoHelper bookDaoHelper, BookChapterDao bookChapterDao) {
        this.bookDaoHelper = bookDaoHelper;
        this.bookChapterDao = bookChapterDao;
    }

    /**
     * 判断是否需要下载
     */
    protected boolean isNeedDownContent(Chapter chapter) {
        if (downloadFlag) {
            if (DataCache.isChapterExists(chapter.sequence, chapter.book_id)) {
                return false;
            }
        } else {
            final String content = DataCache.getChapterFromCache(chapter.sequence, chapter.book_id);
            if (!TextUtils.isEmpty(content) && !("null".equals(content) || CACHE_EXIST.equals(content))
                    || NetWorkUtils.NETWORK_TYPE == NetWorkUtils.NETWORK_NONE) {
                if (NetWorkUtils.NETWORK_TYPE != NetWorkUtils.NETWORK_NONE) {
                    if ((content.length()) <= Constants.CONTENT_ERROR_COUNT) {
                        return true;
                    } else {
                        chapter.content = content;
                        chapter.isSuccess = true;
                        return false;
                    }
                } else {
                    chapter.content = content;
                    chapter.isSuccess = true;
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * 将获取的chapter内容写入本地缓存
     */
    protected void writeChapterCache(Map<String, Chapter> chapterMap) throws Exception {
        for (Map.Entry<String, Chapter> entry : chapterMap.entrySet()) {
            Chapter chapter = entry.getValue();
            if (chapter == null) {
                continue;
            }
            String content = chapter.content;
            if (TextUtils.isEmpty(content)) {
                content = "null";
            }
            boolean write_success = false;
            if (downloadFlag && content.equals(CACHE_EXIST)) {
                write_success = true;

            } else {
                write_success = DataCache.saveChapter(content, chapter.sequence, chapter.book_id);
            }

            if (downloadFlag && !write_success) {
                throw new WriteFileFailException();
            }
        }
    }

    /**
     * 写本地缓存
     */
    protected void writeChapterCache(Chapter chapter) throws Exception {
        if (chapter == null) {
            return;
        }

        if (bookDaoHelper.isBookSubed(chapter.book_id)) {
            String content = chapter.content;
            if (TextUtils.isEmpty(content)) {
                content = "null";
            }
            boolean write_success = false;
            if (downloadFlag && content.equals(CACHE_EXIST)) {
                write_success = true;

            } else {
                write_success = DataCache.saveChapter(content, chapter.sequence, chapter.book_id);
            }

            if (downloadFlag && !write_success) {
                throw new WriteFileFailException();
            }
        }
    }

    /**
     * 获取单章内容
     */
    @Override
    public Chapter requestSingleChapter(int dex, Chapter chapter) throws Exception {

        requestChapter = new OtherRequestChapterExecutorTerminal(bookDaoHelper, bookChapterDao);

        return requestChapter.singleChapter(dex, chapter);
    }

    /**
     * 批量获取章节内容
     */
    @Override
    public void requestBatchChapter(int dex, boolean downloadFlag, Map<String, Chapter> chapterMap) throws Exception {
        this.downloadFlag = downloadFlag;

        requestChapter = new OtherRequestChapterExecutorTerminal(bookDaoHelper, bookChapterDao);

        requestChapter.batchChapter(dex, downloadFlag, chapterMap);
    }

    public interface OtherRequestChapter {

        Chapter singleChapter(int dex, Chapter chapter) throws Exception;

        void batchChapter(int dex, boolean downloadFlag, Map<String, Chapter> chapterMap) throws Exception;
    }
}