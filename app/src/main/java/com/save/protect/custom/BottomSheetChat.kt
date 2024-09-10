package com.save.protect.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
    private lateinit var buttonSend: ImageButton
    private lateinit var textViewSheetTitle: TextView

    private var buttonListener: ButtonListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.bottom_sheet_chat, this, true)

        // View 초기화
        sheetInput = findViewById(R.id.sheet_input)
        buttonSend = findViewById(R.id.button_send)
        textViewSheetTitle = findViewById(R.id.textView_sheet_title) // TextView 초기화

        sheetInput.setIsValidListener(object : IsValidListener {
            override fun isValid(text: String): Boolean {
                updateButtonImage(text.isNotEmpty())
                return text.isNotEmpty()
            }
        })


        buttonSend.setOnClickListener {
            buttonListener?.callback()
            sheetInput.setText("")
        }


    }


    fun setTitle(text: String) {
        textViewSheetTitle.text = text
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
            buttonSend.setImageResource(R.drawable.icon_check) // 활성화된 이미지 리소스
        } else {
            buttonSend.setImageResource(R.drawable.icon_check_none) // 비활성화된 이미지 리소스
        }
    }
}
