package com.save.protect

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.os.Bundle
import android.util.TypedValue
import android.view.MotionEvent
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.save.protect.custom.LoadingDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

abstract class BaseActivity : AppCompatActivity() {

    lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        loadingDialog = LoadingDialog()

    }

    // EditText 밖으로 터치 할 때 키보드 숨기기
    // 단순 ActionUp 일 때만 키보드 숨기기
    val editTextActionMoveCount = HashMap<EditText, Int>()

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {

        currentFocus?.let {
            if (it is EditText) {

                // 터치 뗄 때
                if (event?.action === MotionEvent.ACTION_UP) {

                    // ACTION_MOVE 이력이 없는 경우
                    if (editTextActionMoveCount.get(it) == null) {

                        // EditText 바깥쪽 터치 일 때 키보드 숨기기
                        val outRect = Rect()
                        it.getGlobalVisibleRect(outRect)
                        if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
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
                                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
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
                else if (event?.action === MotionEvent.ACTION_MOVE) {

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

        return super.dispatchTouchEvent(event)
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

    fun getTimeDifference(inputTime: String?): String {
        if (inputTime == null) {
            return "기록 없음"
        }
        // 입력된 시간을 Date 객체로 변환
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val targetDate = dateFormat.parse(inputTime) ?: return "Invalid date format"

        // 현재 시간 가져오기
        val currentDate = Date()

        // 시간 차이 계산 (밀리초 단위)
        val diffInMillis = Math.abs(currentDate.time - targetDate.time)

        // 1년, 1개월, 1주, 1일, 1시간, 1분에 해당하는 밀리초
        val oneYearInMillis = 365L * 24 * 60 * 60 * 1000
        val oneMonthInMillis = 30L * 24 * 60 * 60 * 1000
        val oneWeekInMillis = 7L * 24 * 60 * 60 * 1000
        val oneDayInMillis = 24 * 60 * 60 * 1000
        val oneHourInMillis = 60 * 60 * 1000
        val oneMinuteInMillis = 60 * 1000


        return when {
            // 1년 이상 차이
            diffInMillis >= oneYearInMillis -> {
                val years = diffInMillis / oneYearInMillis
                "${years}년 전"
            }
            // 1개월 이상 차이
            diffInMillis >= oneMonthInMillis -> {
                val months = diffInMillis / oneMonthInMillis
                "${months}개월 전"
            }
            // 1주 이상 차이
            diffInMillis >= oneWeekInMillis -> {
                val weeks = diffInMillis / oneWeekInMillis
                "${weeks}주 전"
            }
            // 하루 이상 차이
            diffInMillis >= oneDayInMillis -> {
                val days = diffInMillis / oneDayInMillis
                "${days}일 전"
            }
            // 24시간 이내 차이
            else -> {
                val hours = diffInMillis / oneHourInMillis
                val minutes = (diffInMillis % oneHourInMillis) / oneMinuteInMillis


                if (hours < 1 && minutes > 0) {
                    "${minutes}분 전"
                } else if (hours > 0) {
                    "${hours}시간 ${minutes}분 전"
                } else {
                    "방금 전"
                }
            }
        }
    }

}
