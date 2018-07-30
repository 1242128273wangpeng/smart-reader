package com.intelligent.reader.view.login

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.EditText
import com.intelligent.reader.R
import net.lzbook.kit.utils.logi


/**
 * Desc 验证码 EditText
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/29 0029 10:41
 */
class VerifyCodeEditText : EditText {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var hintSize = DEFAULT_HINT_SIZE
    private var numTextSize = DEFAULT_TEXT_SIZE
    private val normalBgResId = R.drawable.login_etxt_normal_bg
    private val normalTextColor = ContextCompat.getColor(context, R.color.login_mobile_number)

    private var onCompleteListener: ((isComplete: Boolean) -> Unit)? = null

    var isComplete = false

    init {

        addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onTextChanged(s)
            }
        })

    }

    private fun onTextChanged(s: CharSequence?) {
        when {
            s?.length == 0 -> {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, hintSize)
                setBackgroundResource(normalBgResId)
                onCompleteListener?.invoke(false)
                isComplete = false
            }
            else -> {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, numTextSize)
                setBackgroundResource(normalBgResId)
                setTextColor(normalTextColor)
                if (s?.length == 6) {
                    onCompleteListener?.invoke(true)
                    isComplete = true
                } else {
                    onCompleteListener?.invoke(false)
                    isComplete = false
                }
            }
        }
    }

    fun setOnCompleteListener(listener: (isComplete: Boolean) -> Unit) {
        this.onCompleteListener = listener
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        val consumed = super.onTextContextMenuItem(id)
        if (id == android.R.id.paste) {
            logi("paste: ${this.text}")
            onTextChanged(this.text)
        }
        return consumed
    }

    companion object {
        private const val DEFAULT_HINT_SIZE = 14F
        private const val DEFAULT_TEXT_SIZE = 20f
    }

}

