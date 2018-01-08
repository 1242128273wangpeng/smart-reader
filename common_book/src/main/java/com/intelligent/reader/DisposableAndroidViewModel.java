package com.intelligent.reader;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * @author lijun Lee
 * @desc 一个事件容器ViewModel基类，建议每个ViewModel继承此类，统一取消事件订阅
 * @mail jun_li@dingyuegroup.cn
 * @data 2017/11/17 12:05
 */

public class DisposableAndroidViewModel {
    private final CompositeDisposable mDisposable;

    public DisposableAndroidViewModel() {
        mDisposable = new CompositeDisposable();
    }

    public void addDisposable(Disposable d) {
        mDisposable.add(d);
    }

    public void unSubscribe() {
        mDisposable.clear();
    }
}
