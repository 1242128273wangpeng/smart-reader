package net.lzbook.kit.request.own;

import android.text.TextUtils;

import net.lzbook.kit.app.BaseBookApplication;
import net.lzbook.kit.data.bean.Book;
import net.lzbook.kit.data.bean.Chapter;
import net.lzbook.kit.data.db.BookChapterDao;
import net.lzbook.kit.data.db.BookDaoHelper;
import net.lzbook.kit.request.UrlUtils;
import net.lzbook.kit.utils.BaseBookHelper;
import net.lzbook.kit.utils.NetWorkUtils;
import net.lzbook.kit.utils.UpdateJarUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * 通过端转换获取章节内容
 */
public class OtherRequestChapterExecutorTerminal extends OtherRequestChapterExecutor implements
        OtherRequestChapterExecutor.OtherRequestChapter {

    public OtherRequestChapterExecutorTerminal(BookDaoHelper bookDaoHelper, BookChapterDao bookChapterDao) {
        super(bookDaoHelper, bookChapterDao);
    }

    @Override
    public Chapter singleChapter(int dex, Chapter chapter) throws Exception {
        if (chapter == null) {
            return null;
        }
        if (isNeedDownContent(chapter)) {

            Chapter retChapter = getSourceChapter(dex, chapter);
            if (retChapter != null && !TextUtils.isEmpty(retChapter.content)) {
                retChapter.isSuccess = true;
                // 自动切源需要就更新目录
                if (retChapter.flag == 1 && (!TextUtils.isEmpty(retChapter.content))) {
                    bookChapterDao.updateBookCurrentChapter(retChapter, retChapter.sequence);
                }
            }


            writeChapterCache(retChapter);
            return retChapter;
        }
        return chapter;
    }

    @Override
    public void batchChapter(int dex, boolean downloadFlag, Map<String, Chapter> chapterMap) throws Exception {
        Iterator iterator = chapterMap.entrySet().iterator();
        ArrayList<Chapter> chapters = new ArrayList<>();
        int index = 0;
        while (iterator.hasNext()) {
            if (NetWorkUtils.getNetWorkType(mContext) == NetWorkUtils.NETWORK_NONE){
                mRquestChaptersListener.requestFailed(RequestChaptersListener.ERROR_TYPE_NETWORK_NONE,"没有网络连接",index);
                return;
            }else {
                Map.Entry entry = (Map.Entry) iterator.next();
                Chapter chapter = (Chapter) entry.getValue();
                Chapter result = singleChapter(dex, chapter);
                chapters.add(result);
            }
            index ++;
        }
        mRquestChaptersListener.requestSuccess(chapters);
    }

    /**
     * 从原网址获取章节内容并转化
     */
    private Chapter getSourceChapter(int dex, Chapter chapter) throws Exception {
        String url = null;
        if (chapter != null && !TextUtils.isEmpty(chapter.curl)) {
            url = UrlUtils.buildContentUrl(chapter.curl);

            try {
                chapter.content = BaseBookApplication.getGlobalContext().getMainExtractorInterface().extract(url);
            } catch (Exception e) {
                chapter.status = Chapter.Status.SOURCE_ERROR;
                e.printStackTrace();
            }

            if (!TextUtils.isEmpty(chapter.content)) {
                chapter.content = chapter.content.replace("\\n", "\n");
                chapter.content = chapter.content.replace("\\n\\n", "\n");
                chapter.content = chapter.content.replace("\\n \\n", "\n");
                chapter.content = chapter.content.replace("\\", "");
            }
        }
        return chapter;
    }
}
