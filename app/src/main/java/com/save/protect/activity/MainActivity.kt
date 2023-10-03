package com.save.protect.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.save.protect.util.ClipboardUtils
import com.save.protect.R

class MainActivity : AppCompatActivity() {

    private lateinit var btn_userInfo: Button
    private lateinit var btn_open: Button
    private lateinit var btn_enter: Button


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        btn_userInfo = findViewById(R.id.button_userInfo)
        btn_open = findViewById(R.id.button_open)
        btn_enter = findViewById(R.id.button_enter)

        btn_userInfo.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)

            startActivity(intent)
        }
        btn_open.setOnClickListener {
            //긴급방 만들때 쓰기
            //val intent = Intent(this, ShareholderActivity::class.java)
            val intent = Intent(this, CustomShareholderActivity::class.java)

            startActivity(intent)
        }

        btn_enter.setOnClickListener {
            // 버튼을 누르면 다이얼로그를 띄웁니다.
            showInputDialog()
//            val intent = Intent(this, AudienceActivity::class.java)
//            intent.putExtra("doc_id", "test1234")
//            startActivity(intent)
        }
    }

    private fun showInputDialog() {
        val clipboardUtils = ClipboardUtils(this)

        val copiedText = clipboardUtils.getClipboardText()
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_audience, null)

        val editText = dialogView.findViewById<EditText>(R.id.editText)
        val buttonPaste = dialogView.findViewById<Button>(R.id.button_paste)
        val buttonConfirm = dialogView.findViewById<Button>(R.id.button_confirm)
        val buttonCancel = dialogView.findViewById<Button>(R.id.button_cancel)

        builder.setView(dialogView)
        val dialog = builder.create()

        // 붙여넣기 버튼에 아무 동작을 넣지 않음
        buttonPaste.setOnClickListener {
            copiedText.let { editText.setText(it) }
        }

        buttonConfirm.setOnClickListener {
            val userInput = editText.text.toString()
            if (userInput.length in 1..40) {
                // 최대 40자 이내의 문자열이 입력된 경우에만 확인 버튼 동작 추가
                val intent = Intent(this, AudienceActivity::class.java)

                intent.putExtra("doc_id", userInput)

                startActivity(intent)
                dialog.dismiss() // 다이얼로그 닫기
            } else {
                // 40자를 초과한 경우
            }
        }

        buttonCancel.setOnClickListener {
            dialog.dismiss() // 다이얼로그 닫기
        }

        dialog.show()
    }
}
