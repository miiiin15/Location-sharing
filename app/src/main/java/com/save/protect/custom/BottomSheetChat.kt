package com.save.protect.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.save.protect.R

interface ButtonListener {
    fun callback()
}


class BottomSheetChat @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private lateinit var sheetInput: CustomInput
    private lateinit var sendButton: ImageButton
    private lateinit var sheetTitleTextView: TextView

    private var buttonListener: ButtonListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.bottom_sheet_chat, this, true)

        // View 초기화
        sheetInput = findViewById(R.id.sheet_input)
        sendButton = findViewById(R.id.send_button)
        sheetTitleTextView = findViewById(R.id.sheet_title_text_view) // TextView 초기화

        sheetInput.setIsValidListener(object : IsValidListener {
            override fun isValid(text: String): Boolean {
                updateButtonImage(text.isNotEmpty())
                return text.isNotEmpty()
            }
        })


        sendButton.setOnClickListener {
            buttonListener?.callback()
            sheetInput.setText("")
        }


    }


    fun setTitle(text: String) {
        sheetTitleTextView.text = text
    }

    fun setButtonListener(callback: ButtonListener) {
        buttonListener = callback
    }

    fun moveFocusInput() {
        sheetInput.requestFocus()
    }

    fun getMessage(): String {
        return sheetInput.text.toString()
    }

    fun updateButtonImage(validate: Boolean) {
        if (validate) {
            sendButton.setImageResource(R.drawable.icon_check) // 활성화된 이미지 리소스
        } else {
            sendButton.setImageResource(R.drawable.icon_check_none) // 비활성화된 이미지 리소스
        }
    }
}
