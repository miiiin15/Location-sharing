package com.save.protect.custom

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.save.protect.R

// 커스텀 다이얼로그 클래스 정의
class CustomDialog : Dialog {
    constructor(context: Context, theme: Int) : super(context, theme) {}

    constructor(context: Context) : super(context) {}

    // 커스텀 다이얼로그를 만드는 Builder 클래스 정의
    class Builder(private val context: Context) {
        private var title: String? = null
        private var message: String? = null
        private var cancelable: Boolean = false
        private var positiveButtonText: String? = null
        private var negativeButtonText: String? = null
        private var contentView: View? = null

        private var positiveButtonClickListener: DialogInterface.OnClickListener? = null
        private var negativeButtonClickListener: DialogInterface.OnClickListener? = null

        // 취소 가능 여부 설정
        fun setCancelable(cancelable: Boolean): Builder {
            this.cancelable = cancelable
            return this
        }

        // 메시지 설정
        fun setMessage(message: String): Builder {
            this.message = message
            return this
        }

        // 메시지 리소스로 설정
        fun setMessage(message: Int): Builder {
            this.message = context.getText(message) as String
            return this
        }

        // 타이틀 설정
        fun setTitle(title: Int): Builder {
            this.title = context.getText(title) as String
            return this
        }

        // 타이틀 설정
        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        // 커스텀 콘텐츠뷰 설정
        fun setContentView(v: View): Builder {
            this.contentView = v
            return this
        }

        // 긍정 버튼 설정
        fun setPositiveButton(
            positiveButtonText: Int,
            listener: DialogInterface.OnClickListener
        ): Builder {
            this.positiveButtonText = context.getText(positiveButtonText) as String
            this.positiveButtonClickListener = listener
            return this
        }

        // 긍정 버튼 설정
        fun setPositiveButton(
            positiveButtonText: String,
            listener: DialogInterface.OnClickListener
        ): Builder {
            this.positiveButtonText = positiveButtonText
            this.positiveButtonClickListener = listener
            return this
        }

        // 긍정 버튼 설정
        fun setPositiveButton(listener: DialogInterface.OnClickListener): Builder {
            this.positiveButtonClickListener = listener
            return this
        }

        // 부정 버튼 설정
        fun setNegativeButton(
            negativeButtonText: Int,
            listener: DialogInterface.OnClickListener
        ): Builder {
            this.negativeButtonText = context.getText(negativeButtonText) as String
            this.negativeButtonClickListener = listener
            return this
        }

        // 부정 버튼 설정
        fun setNegativeButton(
            negativeButtonText: String,
            listener: DialogInterface.OnClickListener
        ): Builder {
            this.negativeButtonText = negativeButtonText
            this.negativeButtonClickListener = listener
            return this
        }

        // 부정 버튼 설정
        fun setNegativeButton(listener: DialogInterface.OnClickListener): Builder {
            this.negativeButtonClickListener = listener
            return this
        }

        // 커스텀 다이얼로그 생성
        fun create(): AlertDialog {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout = inflater.inflate(R.layout.dialog_layout, null)
            val dialogBuilder = AlertDialog.Builder(context)
            dialogBuilder.setView(layout)
            val dialog = dialogBuilder.create()
            dialog.setCancelable(cancelable)

            // 타이틀 설정
            if (title != null) {
                (layout.findViewById<View>(R.id.llDlgTitleLayout) as LinearLayout).visibility =
                    View.VISIBLE
                (layout.findViewById<View>(R.id.txvDlgTitle) as TextView).text = title
            } else {
                (layout.findViewById<View>(R.id.llDlgTitleLayout) as LinearLayout).visibility =
                    View.GONE
                (layout.findViewById<View>(R.id.txvDlgTitle) as TextView).visibility = View.GONE
            }

            // 긍정 버튼 설정
            if (positiveButtonClickListener != null) {
                if (positiveButtonText != null) {
                    (layout.findViewById<View>(R.id.btnDlgPositive) as Button).text =
                        positiveButtonText
                }
                if (positiveButtonClickListener != null) {
                    (layout.findViewById<View>(R.id.btnDlgPositive) as Button).setOnClickListener {
                        positiveButtonClickListener!!.onClick(
                            dialog,
                            DialogInterface.BUTTON_POSITIVE
                        )
                    }
                }
            } else {
                // 긍정 버튼이 없으면 GONE으로 설정
                layout.findViewById<View>(R.id.btnDlgPositive).visibility = View.GONE
            }

            // 취소 버튼 설정
            if (negativeButtonClickListener != null) {
                if (negativeButtonText != null) {
                    // 부정 버튼 텍스트 설정
                    (layout.findViewById<View>(R.id.btnDlgNegative) as Button).text =
                        negativeButtonText
                }
                if (negativeButtonClickListener != null) {
                    // 부정 버튼 리스너 설정
                    (layout.findViewById<View>(R.id.btnDlgNegative) as Button).setOnClickListener {
                        negativeButtonClickListener!!.onClick(
                            dialog,
                            DialogInterface.BUTTON_NEGATIVE
                        )
                    }
                }
            } else {
                // 부정 버튼이 없는 경우 가시성을 GONE으로 설정
                layout.findViewById<View>(R.id.btnDlgNegative).visibility = View.GONE
            }

            // 콘텐츠 메시지 설정
            if (message != null) {
                // 메시지 설정
                (layout.findViewById<View>(R.id.txvDlgContent) as TextView).text = message
            } else if (contentView != null) {
                // 메시지가 없는 경우
                // 컨텐츠뷰를 대화상자에 추가
                (layout.findViewById<View>(R.id.llDlgContentLayout) as android.widget.LinearLayout).removeAllViews()
                (layout.findViewById<View>(R.id.llDlgContentLayout) as android.widget.LinearLayout).addView(
                    contentView
                )
            }

            // 대화상자의 콘텐츠뷰 설정
            dialog.setContentView(layout)
            return dialog
        }

    }
}