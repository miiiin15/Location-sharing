package com.save.protect.database

import android.net.Uri
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.FileInputStream
import java.io.IOException

object FirebaseStorageManager {

    private val storage = Firebase.storage

    fun uploadImageToFirebaseStorage(uid: String?, imageUri: Uri, onComplete: (String?) -> Unit) {
        // Firebase Storage에 업로드 파일의 이름과 경로
        val fileName = "/userProfile/$uid.jpg"

        // Firebase Storage의 레퍼런스를 얻어옵니다.
        val storageRef: StorageReference = storage.reference

        // 업로드할 파일의 레퍼런스를 생성합니다.
        val imageRef: StorageReference = storageRef.child(fileName)

        // Uri를 사용하여 파일 업로드를 수행합니다.
        val uploadTask: UploadTask = imageRef.putFile(imageUri)

        try {
            // 업로드 작업 완료 리스너를 추가합니다.
            uploadTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 업로드 성공 시 이미지의 다운로드 URL을 가져옵니다.
                    imageRef.downloadUrl
                        .addOnSuccessListener { imageUrl ->
                            // 다운로드 URL을 콜백으로 전달
                            onComplete(imageUrl.toString())
                            Log.d("사진 등록", " 성공")
                        }
                        .addOnFailureListener { exception ->
                            // 다운로드 URL을 가져오지 못한 경우 예외 처리
//                            onComplete(null, exception)
                            Log.d("사진 등록", " URL 가져오기 실패 : ${exception}")
                            onComplete(null)
                        }
                } else {
                    // 업로드 실패 시 예외 처리
//                    onComplete(null, task.exception)
                    Log.d("사진 등록", " 실패 : ${task.exception}")
                    onComplete(null)
                }
            }
        } catch (e: IOException) {
            Log.d("사진 등록 실패", "${e}")
            onComplete(null) // 파일 경로를 읽을 수 없을 때 null을 콜백으로 전달
        }
    }
}
