package com.save.protect.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

class ClipboardUtils(private val context: Context) {

    // 클립보드 관리자 가져오기
    private val clipboardManager: ClipboardManager? =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?

    // 클립보드에 복사된 값을 반환하는 함수
    fun getClipboardText(): String? {
        return clipboardManager?.let { manager ->
            val clipData = manager.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val clipItem = clipData.getItemAt(0)
                clipItem.text?.toString()
            } else {
                null
            }
        }
    }

    // 문자열을 클립보드에 복사하는 함수
    fun copyTextToClipboard(textToCopy: String) {
        clipboardManager?.let { manager ->
            val clipData = ClipData.newPlainText("text", textToCopy)
            manager.setPrimaryClip(clipData)
        }
    }
}
