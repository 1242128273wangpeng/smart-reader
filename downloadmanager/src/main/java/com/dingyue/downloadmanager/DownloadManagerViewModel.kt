package com.dingyue.downloadmanager

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.dingyue.downloadmanager.contract.BookHelperContract
import com.dingyue.downloadmanager.contract.CacheManagerContract
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import net.lzbook.kit.constants.Constants
import net.lzbook.kit.data.bean.Book
import java.util.*

class DownloadManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val bookListLiveData = MutableLiveData<List<Book>>()

    private val deleteCacheLiveData = MutableLiveData<String>()

    fun loadBookListLiveData(): LiveData<List<Book>> {
        return bookListLiveData
    }

    fun loadDeleteCacheLiveData(): LiveData<String> {
        return deleteCacheLiveData
    }

    fun deleteCache(books: ArrayList<Book>) {
        Observable.fromIterable(books)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribeBy(onNext = {
                    CacheManagerContract.removeBookTask(it.book_id)
                    BookHelperContract.removeChapterCacheFile(it)
                }, onComplete = {
                    deleteCacheLiveData.postValue("Deleted")
                })
    }

    fun refreshBooks() {
        CacheManagerContract.freshBookTasks()
        bookListLiveData.value = BookHelperContract.querySortedBookList(Constants.book_list_sort_type)
    }
}