package com.save.protect.custom

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.save.protect.R


class CustomButton : AppCompatButton {

    // 생성자
    constructor(context: Context) : super(context) {
        init()
    }

    // XML에서 사용할 때 호출되는 생성자
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        text = "확인"
        background = ContextCompat.getDrawable(context, R.drawable.custom_button_selector)
        setTextColor(ContextCompat.getColor(context, R.color.white))
    }

    // isEnable 속성을 설정하는 함수
    fun setEnable(isEnabled: Boolean) {
        this.isEnabled = isEnabled
    }

}