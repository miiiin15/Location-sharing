package com.save.protect.custom

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.save.protect.R

enum class ButtonType {
    PRIMARY,
    WHITE,
    GREY
}

class CustomButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.buttonStyle
) : AppCompatButton(context, attrs, defStyleAttr) {

    private var buttonType: ButtonType = ButtonType.PRIMARY

    init {
        init(attrs)
        context.theme.obtainStyledAttributes(attrs, R.styleable.CustomButton, 0, 0).apply {
            try {
                val type = getString(R.styleable.CustomButton_buttonType) ?: "NORMAL"
                val buttonType = runCatching { enumValueOf<ButtonType>(type.toUpperCase()) }
                    .getOrElse { ButtonType.PRIMARY }

                setButtonType(buttonType)
            } finally {
                recycle()
            }
        }
    }

    private fun init(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.CustomButton)
            val textValue = typedArray.getString(R.styleable.CustomButton_android_text)
            typedArray.recycle()

            // 텍스트 설정
            setTextStyle(textValue)
        }
        applyButtonDesign()
    }


    private fun setTextStyle(textValue: String?) {
        text = textValue ?: "확인"
        textSize = 16F
    }

    fun setEnable(isEnabled: Boolean) {
        this.isEnabled = isEnabled
    }

    fun setButtonType(type: ButtonType) {
        buttonType = type
        applyButtonDesign()
    }

    private fun applyButtonDesign() {
        when (buttonType) {
            ButtonType.PRIMARY -> {
                background = ContextCompat.getDrawable(context, R.drawable.custom_button_selector)
                setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            else -> {
                background = ContextCompat.getDrawable(context, R.drawable.custom_button_selector)
                setTextColor(ContextCompat.getColor(context, R.color.white))
            }
        }
    }
}
