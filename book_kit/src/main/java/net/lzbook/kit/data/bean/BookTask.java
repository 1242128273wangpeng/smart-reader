package net.lzbook.kit.data.bean;


import net.lzbook.kit.book.download.BreakPointFileLoader;
import net.lzbook.kit.book.download.CallBackDownload;
import net.lzbook.kit.book.download.DownloadState;


public class BookTask {
	//开始下载的位置
	public int startSequence;
	//结束下载的位置
	public int endSequence;
	//监听下载的回调接口
	public CallBackDownload mCallBack;
	//下载的小说
	public Book book;
	//小说id
	public String book_id;
	//下载的状态
	public DownloadState state;
	//是否开启自动下载
	public boolean isAutoState = false;

	public BreakPointFileLoader cacheLoader = null;
	public int progress = 0;

	public BookTask(Book book, DownloadState state, int startSequence, int endSequence, CallBackDownload mCallBack) {
		super();
		if (book == null) {
			throw new IllegalArgumentException("book may not be null");
		}

		if (mCallBack == null) {
			throw new IllegalArgumentException("mCallBack may not be null");
		}
		this.book = book;
		this.book_id = book.book_id;
		this.state = state;
		this.startSequence = startSequence;
		this.endSequence = endSequence;
		this.mCallBack = mCallBack;
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof BookTask) {
			BookTask ao = (BookTask) o;
			return book.book_id.equals(ao.book.book_id);
		}
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return book.book_id.hashCode();
	}

	@Override
	public String toString() {
		return super.toString();
	}

}
