package com.save.protect.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast

object ImageUtils {
    private const val PICK_IMAGE_REQUEST = 1

    // 적절한 이미지 크기 기준 (최대 너비와 최대 높이)
    private const val MAX_WIDTH = 1024
    private const val MAX_HEIGHT = 1024

    fun isImageSizeValid(context: Context, imageUri: Uri): Boolean {
        try {
            // 이미지를 로드하여 크기 확인
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(imageUri),
                null,
                options
            )

            val imageWidth = options.outWidth
            val imageHeight = options.outHeight

            // 이미지 크기가 적절한지 확인하고 결과 반환
            return imageWidth <= MAX_WIDTH && imageHeight <= MAX_HEIGHT
        } catch (e: Exception) {
            return false // 오류 발생
        }
    }

    fun selectImageFromGallery(activity: Activity, onFail: (errorMessage: String) -> Unit) {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activity.startActivityForResult(intent, PICK_IMAGE_REQUEST)
        } catch (e: Exception) {
            onFail("이미지 업로드 실패. $e")
        }
    }

    fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        onSuccess: (uri: Uri) -> Unit
    ) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            if (imageUri != null) {
                onSuccess(imageUri)
            }
        }
    }
}