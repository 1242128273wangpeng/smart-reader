package com.intelligent.reader.view.login

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.EditText
import com.intelligent.reader.R
import net.lzbook.kit.utils.logd
import net.lzbook.kit.utils.logi


/**
 * Desc 手机号 EditText
 * Author qiantao
 * Mail tao_qian@dingyuegroup.cn
 * Date 2018/3/29 0029 10:41
 */
class MobileNumberEditText : EditText {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var hintSize = DEFAULT_HINT_SIZE
    private var numTextSize = DEFAULT_TEXT_SIZE
    private val normalBgResId = R.drawable.login_etxt_normal_bg
    private val errorBgResId = R.drawable.login_etxt_error_bg
    private val normalTextColor = ContextCompat.getColor(context, R.color.login_mobile_number)
    private val errorTextColor = ContextCompat.getColor(context, R.color.login_etxt_error)

    private var stateListener: ((stateList: ArrayList<Int>) -> Unit)? = null

    private var isInputComplete = false

    private val numberFilters = arrayOf<InputFilter>(NumberFilter())

    private val stateList = arrayListOf<Int>()

    private val mobileRegex = Regex("^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\\d{8}$")

    init {

        filters = numberFilters

        addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onTextChanged(s, start, before)
            }
        })

    }

    private fun onTextChanged(s: CharSequence?, start: Int?, before: Int?) {
        logd("onTextChanged")
        stateList.clear()
        when {
            s?.length == 0 -> {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, hintSize)
                setBackgroundResource(normalBgResId)
                stateList.add(State.EMPTY)
                stateList.add(State.CORRECT)
                stateList.add(State.NOT_COMPLETE)
            }
            s?.startsWith("1") == false -> {
                setBackgroundResource(errorBgResId)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, numTextSize)
                setTextColor(errorTextColor)
                stateList.add(State.NOT_EMPTY)
                stateList.add(State.INCORRECT)
                stateList.add(State.NOT_COMPLETE)
            }
            else -> {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, numTextSize)
                setBackgroundResource(normalBgResId)
                setTextColor(normalTextColor)
                if (start != null && before != null) {
                    s?.addSpace(start, before)
                }
                if (s?.length == 13) {
                    isInputComplete = true
                    stateList.add(State.COMPLETE)
                    if (s.replace("\\s+".toRegex(), "")//去除空格
                                    .matches(mobileRegex)) {//是手机号
                        setBackgroundResource(normalBgResId)
                        setTextColor(normalTextColor)
                        stateList.add(State.CORRECT)
                    } else {
                        setBackgroundResource(errorBgResId)
                        setTextColor(errorTextColor)
                        stateList.add(State.INCORRECT)
                    }
                } else if (isInputComplete) {
                    isInputComplete = false
                    stateList.add(State.CORRECT)
                    stateList.add(State.NOT_COMPLETE)
                }
                stateList.add(State.NOT_EMPTY)
            }
        }
        stateListener?.invoke(stateList)
    }

    fun setStateListener(listener: (stateList: ArrayList<Int>) -> Unit) {
        stateListener = listener
    }

    fun isComplete(): Boolean {
        return stateList.contains(State.COMPLETE)
    }

    fun isCorrect(): Boolean {
        return stateList.contains(State.CORRECT)
    }

    fun getMobileNumber(): String {
        return this.text.toString().replace("\\s+".toRegex(), "")
    }

    private fun CharSequence.addSpace(start: Int, before: Int) {
        val stringBuilder = StringBuilder()
        for (i in 0 until length()) {
            if (i != 3 && i != 8 && get(i) == ' ') {
                continue
            } else {
                stringBuilder.append(get(i))
                if ((stringBuilder.length == 4 || stringBuilder.length == 9) && stringBuilder[stringBuilder.length - 1] != ' ') {
                    stringBuilder.insert(stringBuilder.length - 1, ' ')
                }
            }
        }
        if (stringBuilder.toString() != toString()) {
            var index = start + 1
            if (stringBuilder[start] == ' ') {
                if (before == 0) {
                    index++
                } else {
                    index--
                }
            } else {
                if (before == 1) {
                    index--
                }
            }
            setText(stringBuilder.toString())
            setSelection(index)
        }
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        val consumed = super.onTextContextMenuItem(id)
        if (id == android.R.id.paste) {
            logi("paste: ${this.text}")
            onTextChanged(this.text, null, null)
        }
        return consumed
    }

    private inner class NumberFilter : InputFilter {
        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence {
            if (dest.isNotEmpty() && !dest.startsWith("1")) return ""
            if (dest.length >= 13) return ""
            return source
        }

    }

    object State {
        const val INCORRECT = 10
        const val CORRECT = 11
        const val EMPTY = 20
        const val NOT_EMPTY = 21
        const val COMPLETE = 30
        const val NOT_COMPLETE = 31
    }

    companion object {
        private const val DEFAULT_HINT_SIZE = 14F
        private const val DEFAULT_TEXT_SIZE = 20f
    }

}

