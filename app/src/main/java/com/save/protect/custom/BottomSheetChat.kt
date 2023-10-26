package com.save.protect.custom

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

object BottomSheetChat {
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    // 바텀시트 초기화
    fun initBottomSheet(
        bottomSheet: View,
        onChange: (bottomSheet: View, newState: Int) -> Unit,
        onSlide: (bottomSheet: View, slideOffset: Float) -> Unit
    ) {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            // bottom sheet의 상태값 변경
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                onChange(bottomSheet, newState)
            }
            // BottomSheet가 스크롤될 때 호출
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                onSlide(bottomSheet, slideOffset)
            }
        }
        )

    }
}