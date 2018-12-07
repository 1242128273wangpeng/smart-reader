package net.lzbook.kit.utils.web

/**
 * Desc WebView索引
 * Author crazylei
 * Mail crazylei911228@gmail.com
 * Date 2018/11/16 17:35
 */
object WebViewIndex {

    /**
     * 精选
     */

    // 今日多看
    const val recommend_gender = "#/recommend?gender="
    const val recommend_gender_male = "#/recommend?gender=male"
    const val recommend_gender_female = "#/recommend?gender=female"
    const val recommend_cate_finish = "#/finish?cate=finish"    //完本
    const val recommend_cate_fantasy = "#/finish?cate=fantasy"  //玄幻
    const val recommend_cate_modern = "#/finish?cate=modern"    //现代言情

    const val finish_Detail_finish = "#/finishDetail?genre=&id=完结"    //完结
    const val finish_Detail_fantasy = "#/finishDetail?genre=玄幻&id="   //玄幻传参
    const val finish_Detail_modern = "#/finishDetail?genre=现代言情&id=" //现代言情传参


    // 新壳4
    const val recommend = "#/recommend"
    const val recommend_male = "#/gender?gender=male"
    const val recommend_female = "#/gender?gender=female"
    const val recommend_finish = "#/finish"

    /**
     * 分类
     */

    // 今日多看
    const val category = "#/category"

    // 新壳4
    const val category_male = "#/category?gender=male"
    const val category_female = "#/category?gender=female"

    /**
     * 榜单
     */
    const val rank = "#/rank"


    /**
     * 搜索
     */
    const val search = "#/search"

}