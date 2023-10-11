package com.save.protect.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

object ImageUtils {
    private const val PICK_IMAGE_REQUEST = 1

    // 적절한 이미지 크기 기준 (최대 너비와 최대 높이)
    private const val MAX_WIDTH = 2400
    private const val MAX_HEIGHT = 2400


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


    fun loadImageAndResize(context: Context, imageUri: Uri, resize: Int): Bitmap? {
        var resizeBitmap: Bitmap? = null

        val options = BitmapFactory.Options()
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            if (inputStream != null) {
                // 이미지 크기를 확인하고 필요한 경우 리사이징
                BitmapFactory.decodeStream(inputStream, null, options)
                var width = options.outWidth
                var height = options.outHeight
                var sampleSize = 1

                while (true) {
                    if (width / 2 < resize || height / 2 < resize) break
                    width /= 2
                    height /= 2
                    sampleSize *= 2
                }

                options.inSampleSize = sampleSize
                inputStream.close()

                // 이미지를 다시 읽고 회전 정보를 고려하여 이미지를 회전
                val bitmap = BitmapFactory.decodeStream(
                    context.contentResolver.openInputStream(imageUri),
                    null,
                    options
                )

                val rotation = getRotationFromExif(context, imageUri)
                resizeBitmap = bitmap?.let { rotateBitmap(it, rotation) }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return resizeBitmap
    }

    //Uri 경로상 파일을 회전
    fun getRotationFromExif(context: Context, imageUri: Uri): Float {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            if (inputStream != null) {
                val exif = androidx.exifinterface.media.ExifInterface(inputStream)
                val rotation = exif.getAttributeInt(
                    androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
                )
                inputStream.close()

                return when (rotation) {
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return 0f
    }

    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
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