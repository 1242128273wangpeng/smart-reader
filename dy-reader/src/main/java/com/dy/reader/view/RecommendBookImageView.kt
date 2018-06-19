package com.dy.reader.view

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.ding.basic.bean.Book
import com.dy.reader.R
import kotlinx.android.synthetic.main.iv_recommend.view.*


/**
 * 项目名称：11m
 * 类描述：
 * 创建人：Zach
 * 创建时间：2017/10/31 0031
 */

class RecommendBookImageView: RelativeLayout {

    var mSelectedFlag = false

    constructor(context: Context? ) : super(context){
        initView(context,null)

    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        initView(context,attrs)

    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        initView(context,attrs)

    }

    fun initView(context: Context?,attrs: AttributeSet?){
        val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.RecommendBookImageView)
        mLabelColor = optColor(typedArray, R.styleable.RecommendBookImageView_label_color, 0XffAAAAAA.toInt())!!
        mLabelHeight = optPixelSize(typedArray, R.styleable.RecommendBookImageView_label_height, dp2pxInt(1.5f))!!
        mLabelWidth = optPixelSize(typedArray, R.styleable.RecommendBookImageView_label_width, dp2pxInt(1.5f))!!
        mLabelTextSize = optPixelSize(typedArray, R.styleable.RecommendBookImageView_label_text_size, dp2pxInt(1.5f))!!
        mLabelTextColor = optColor(typedArray, R.styleable.RecommendBookImageView_label_text_color, 0XffAAAAAA.toInt())!!
        mLabelTextContent = optString(typedArray, R.styleable.RecommendBookImageView_label_text)
        labelMarginBottom = optPixelSize(typedArray, R.styleable.RecommendBookImageView_label_margin_bottom, dp2pxInt(1.5f))!!

        mSelectorImg = optDrawable(typedArray, R.styleable.RecommendBookImageView_selector_icon)
        mIconSelectorIsShow = optBoolean(typedArray, R.styleable.RecommendBookImageView_selector_isShown,true)!!
        mIconSelectorHeight = optPixelSize(typedArray, R.styleable.RecommendBookImageView_selector_height, dp2pxInt(1.5f))!!
        mIconSelectorWidth = optPixelSize(typedArray, R.styleable.RecommendBookImageView_selector_width, dp2pxInt(1.5f))!!
        mIconSelectorMarginTop = optPixelSize(typedArray, R.styleable.RecommendBookImageView_selector_margin_top, dp2pxInt(1.5f))!!
        mIconSelectorMarginRight = optPixelSize(typedArray, R.styleable.RecommendBookImageView_selector_margin_right, dp2pxInt(1.5f))!!

        typedArray?.recycle()

        //初始化视图
        LayoutInflater.from(context).inflate(R.layout.iv_recommend, this, true)

        //设置标签的属性
        val labelParams = RelativeLayout.LayoutParams(mLabelWidth, mLabelHeight)
        labelParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        labelParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        labelParams.setMargins(0, 0, 0, labelMarginBottom)

        //初始化布局标签字体的大小颜色
        icon_label?.layoutParams = labelParams
        icon_label?.gravity = Gravity.CENTER
        icon_label?.text = mLabelTextContent
        icon_label?.textSize = mLabelTextSize.toFloat()
        icon_label?.setTextColor(mLabelTextColor)
        //设置选择器的属性
        val selectorParams = RelativeLayout.LayoutParams(mIconSelectorWidth, mIconSelectorHeight)
        selectorParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)

        selectorParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        selectorParams.setMargins(0, mIconSelectorMarginTop, mIconSelectorMarginRight, 0)

        //初始化图片内容
        icon_selector?.layoutParams = selectorParams

        bg?.setBackgroundResource(R.drawable.book_cover_default_icon)
        icon_selector?.setBackgroundDrawable(mSelectorImg)
        if(!mIconSelectorIsShow){
            icon_selector.visibility= View.GONE
        }
        setOnClickListener {
            mSelectedFlag= !mSelectedFlag
            icon_selector?.isSelected = mSelectedFlag
            listener?.onChecked(mSelectedFlag)
        }
    }

    var listener: onCheckListener? = null

    fun setOnCheckedListener(listener: onCheckListener) {
        this.listener = listener
    }

    interface onCheckListener{
        fun onChecked(b:Boolean)
    }

    /***********************成员变量属性********************************/

    /**
     * 标签背景颜色
     */
    var mLabelColor: Int = 0
    /**
     * 标签背景高度
     */
    var mLabelHeight: Int = 0
    /**
     * 标签背景宽度
     */
    var mLabelWidth: Int = 0
    /**
     * 标签字体大小
     */
    var mLabelTextSize: Int = 0
    /**
     * 标签距离底部距离
     */
    var labelMarginBottom: Int = 0
    /**
     * 标签字体颜色
     */
    var mLabelTextColor: Int = 0
    /**
     * 选择状态图片
     */
    var mSelectorImg: Drawable? = null
    /**
     * 标签字体内容
     */
    var mLabelTextContent: String? = null
    /**
     * 选择器icon的宽
     */
    var mIconSelectorWidth: Int = 0
    /**
     * 选择器icon的高
     */
    var mIconSelectorIsShow: Boolean = true
    /**
     * 选择器icon的高
     */
    var mIconSelectorHeight: Int = 0
    /**
     * 选择器距离顶部的距离
     */
    var mIconSelectorMarginTop: Int = 0
    /**
     * 选择器距离右边的距离
     */
    var mIconSelectorMarginRight: Int = 0

    var mBook: Book? = null

    /**********************XML属性解析*********************************/

    private fun optDrawable(typedArray: TypedArray?,index: Int): Drawable? {
        return typedArray?.getDrawable(index)
    }

    private fun optPixelSize(typedArray: TypedArray?, index: Int, def: Int): Int? {
        return typedArray?.getDimensionPixelOffset(index, def)
    }

    private fun optColor(typedArray: TypedArray?, index: Int,def: Int): Int? {
        return typedArray?.getColor(index, def)
    }

    private fun optBoolean(typedArray: TypedArray?, index: Int,def: Boolean): Boolean? {
        return typedArray?.getBoolean(index, def)
    }

    private fun optString(typedArray: TypedArray?,index: Int): String? {
        return typedArray?.getString(index)
    }

    private fun dp2pxInt(dp: Float): Int {
        return dp2px(dp).toInt()
    }

    private fun dp2px(dp: Float): Float {
        val r = Resources.getSystem()
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.displayMetrics)
    }

    fun getBackGroundImage():ImageView{
        return bg
    }

    /**
     * 智能书籍可能会有多重标签的情况，在这种情况下以选取第一个标签
     * 如果第一个标签字数大于4则只取4个字
     * */
    fun setLabelText(content:String){
        val split = content.split(",")
        if(split.size>1){
            val label = split[0]
            if(label.length>4){
                icon_label.text = label.substring(0,4)
            }else{
                icon_label.text = label
            }
        }else{
            icon_label.text = content
        }
    }

    /**
     * 绑定书籍书籍，在书末推荐时用来传递数据打开对应的封面页
     * */
    fun bindBook(book: Book){
        mBook = book
    }

    fun getBook():Book?{
        return mBook
    }
}