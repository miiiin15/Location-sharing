package com.save.protect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var btn_open: Button
    private lateinit var btn_enter: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_open = findViewById(R.id.button_open)
        btn_enter = findViewById(R.id.button_enter)

        btn_open.setOnClickListener {
            //긴급방 만들때 쓰기
            //val intent = Intent(this, ShareholderActivity::class.java)
            val intent = Intent(this, CustomShareholderActivity::class.java)

            startActivity(intent)
        }
        btn_enter.setOnClickListener {
            val intent = Intent(this, AudienceActivity::class.java)

            intent.putExtra("doc_id", "test1234")

            startActivity(intent)
        }
    }

}