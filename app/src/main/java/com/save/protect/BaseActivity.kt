package com.save.protect

import android.content.*
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.os.Bundle
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.save.protect.custom.CustomDialog
import com.save.protect.custom.LoadingDialog

//import com.save.protect.app.data.Constants.context
//import com.save.protect.esbank.ui.view13_etc.ErrorPageActivity
//import com.save.protect.utils.KeyboardVisibilityUtils
//import com.save.protect.utils.OnSingleClickListener
//import com.save.protect.app.data.DataProvider

abstract class BaseActivity : AppCompatActivity() {

    lateinit var loadingDialog: LoadingDialog
//    lateinit var keyboardVisibilityUtils: KeyboardVisibilityUtils   //키보드 유틸

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
//			window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        loadingDialog = LoadingDialog()

//        DataProvider.unCaughtException(this, ErrorPageActivity())   //Global Exception
    }

    // EditText 밖으로 터치됐을 때 키보드 숨기기
    // 스크롤 할땐 무시,
    // 단순 ActionUp 일 때만 키보드 숨기기
    val editTextActionMoveCount = HashMap<EditText, Int>()

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {

        currentFocus?.let {
            if (it is EditText) {

//				Logcat.e("MotionEvent: ${ev?.action?.let { MotionEvent.actionToString(it) }?: "노액션" }")

                // 터치 뗄 때
                if (ev?.action === MotionEvent.ACTION_UP) {

                    // ACTION_MOVE 이력이 없는 경우
                    if (editTextActionMoveCount.get(it) == null) {

                        // EditText 바깥쪽 터치 일 때 키보드 숨기기
                        val outRect = Rect()
                        it.getGlobalVisibleRect(outRect)
                        if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                            it.clearFocus()

                            val imm =
                                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(it.getWindowToken(), 0)
                        }
                    }

                    // ACTION_MOVE 이력이 있는 경우
                    else {

                        // N번 미만으로 ACTION_MOVE가 호출되었을 때만 키보드 숨기기
                        // ACTION_MOVE 이력이 조금만 움직여도 바로 쌓인다.
                        editTextActionMoveCount.get(it)?.let { count ->

                            if (count < 5) {

                                // EditText 바깥쪽 터치 일 때 키보드 숨기기
                                val outRect = Rect()
                                it.getGlobalVisibleRect(outRect)
                                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                                    it.clearFocus()

                                    val imm =
                                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    imm.hideSoftInputFromWindow(it.getWindowToken(), 0)
                                }
                            }
                        }
                    }

                    editTextActionMoveCount.clear()

                }

                // 스크롤링
                else if (ev?.action === MotionEvent.ACTION_MOVE) {

                    // ACTION_MOVE 이력이 없는 경우
                    if (editTextActionMoveCount.get(it) == null) {
                        editTextActionMoveCount.put(it, 1)

                    }

                    // ACTION_MOVE 이력이 있는 경우
                    else {
                        editTextActionMoveCount.get(it)?.let { count ->
                            editTextActionMoveCount.put(it, count + 1)
                        }
                    }
                }
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    //중복클릭방지
//    fun View.setOnSingleClickListener(onSingleClick: (View) -> Unit) {
//        val oneClick = OnSingleClickListener {
//            onSingleClick(it)
//        }
//        setOnClickListener(oneClick)
//    }

    fun showAlert(msg: String, title: String = "") {
        try {
            val builder = CustomDialog.Builder(this)
            builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                })

            // 제목이 비어있지 않고 null이 아닌 경우에만 제목을 설정
            if (title.isNotBlank()) {
                builder.setTitle(title)
            }

            builder.create().show()
        } catch (e: WindowManager.BadTokenException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun showKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    //키보드 내리기
    fun closeKeyboard() {
        var view = this.currentFocus

        if (view != null) {
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }

}
