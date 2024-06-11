package com.save.protect.custom

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.ScrollView
import com.save.protect.R

// MaxHeightScrollView 클래스 정의
class MaxHeightScrollView : ScrollView {

	// 스크롤 뷰의 최대 높이를 저장할 변수
	private var maxHeight: Int = 0

	// 기본 생성자
	constructor(context: Context) : super(context) {}

	// AttributeSet을 사용하는 생성자
	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
		init(context, attrs)
	}

	// AttributeSet 및 defStyleAttr을 사용하는 생성자
	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
		context,
		attrs,
		defStyleAttr
	) {
		init(context, attrs)
	}

	// Lollipop(API 21) 이상에서 사용하는 생성자
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
		context,
		attrs,
		defStyleAttr,
		defStyleRes
	) {
		init(context, attrs)
	}

	// 속성 초기화를 위한 init 메서드
	private fun init(context: Context, attrs: AttributeSet?) {
		if (attrs != null) {
			// 속성 값 얻어오기
			val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightScrollView)
			maxHeight = styledAttrs.getDimensionPixelSize(
				R.styleable.MaxHeightScrollView_maxHeight, 200
			) //200은 기본 값
			styledAttrs.recycle()
		}
	}

	// 측정 메서드 재정의
	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		var heightMeasureSpec = heightMeasureSpec
		// 최대 높이로 heightMeasureSpec 설정
		heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST)
		// 기본 측정 메서드 호출
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)
	}
}