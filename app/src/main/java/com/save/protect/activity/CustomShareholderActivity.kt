package com.save.protect.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.save.protect.R


class CustomShareholderActivity : AppCompatActivity() {

    private lateinit var editTextMarkLimit: EditText
    private lateinit var editTextUpdateInterval: EditText
    private lateinit var editTextMinimumInterval: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_shareholder)

        editTextMarkLimit = findViewById(R.id.editText_markLimit)
        editTextUpdateInterval = findViewById(R.id.editText_updateInterval)
        editTextMinimumInterval = findViewById(R.id.editText_minimumInterval)

        val buttonOpenNextScreen: Button = findViewById(R.id.button_openNextScreen)
        buttonOpenNextScreen.setOnClickListener {
            val markLimitText = editTextMarkLimit.text.toString()
            val updateIntervalText = editTextUpdateInterval.text.toString()
            val minimumIntervalText = editTextMinimumInterval.text.toString()

            // 입력 값 검증
            if (isValidInput(markLimitText, updateIntervalText, minimumIntervalText)) {
//                // 입력값을 다음 화면으로 넘겨서 처리하는 코드
                val intent = Intent(this, ShareholderActivity::class.java)
                intent.putExtra("MARK_LIMIT", markLimitText.toInt())
                intent.putExtra("UPDATE_INTERVAL", updateIntervalText.toInt())
                intent.putExtra("MINIMUM_INTERVAL", minimumIntervalText.toInt())
                startActivity(intent)
                finish() // 현재 화면 종료
            }
        }
    }

    private fun isValidInput(
        markLimit: String,
        updateInterval: String,
        minimumInterval: String
    ): Boolean {
        if (markLimit.isEmpty() || updateInterval.isEmpty() || minimumInterval.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show()
            return false
        }

        val markLimitValue = markLimit.toIntOrNull()
        val updateIntervalValue = updateInterval.toIntOrNull()
        val minimumIntervalValue = minimumInterval.toIntOrNull()

        if (markLimitValue == null || updateIntervalValue == null || minimumIntervalValue == null ||
            markLimitValue !in 1..10 || updateIntervalValue !in 1..60 || minimumIntervalValue !in 1..60 || minimumIntervalValue > updateIntervalValue
        ) {
            Toast.makeText(this, "입력값이 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}
