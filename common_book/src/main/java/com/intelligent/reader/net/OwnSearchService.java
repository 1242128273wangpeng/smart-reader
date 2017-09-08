package com.intelligent.reader.net;

import net.lzbook.kit.data.bean.SearchAutoCompleteBean;
import net.lzbook.kit.data.bean.SearchHotBean;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Administrator on 2017\9\4 0004.
 */

public interface OwnSearchService {
    String SEARCH_HOT = "/v3/search/hotWords";
    String SEARCH_AUTO_COMPLETE = "/v3/search/autoComplete";
    String PATH_LOGOUT = "/v3/user/logout";


    @GET(SEARCH_HOT)
    Observable<SearchHotBean> getHotWord();


    @GET(SEARCH_AUTO_COMPLETE)
    Observable<SearchAutoCompleteBean> searchAutoComplete(@Query("word") String word);



}
