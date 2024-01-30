package com.save.protect.database

import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import java.io.IOException

object FirebaseStorageManager {

    private val storage = Firebase.storage

    fun uploadImageToFirebaseStorage(uid: String?, imageUri: Uri, onComplete: (String?) -> Unit) {
        // Firebase Storage에 업로드 파일의 이름과 경로
        val fileName = "/userProfile/$uid.jpg"

        val storageRef: StorageReference = storage.reference
        val imageRef: StorageReference = storageRef.child(fileName)
        val uploadTask: UploadTask = imageRef.putFile(imageUri)

        try {
            uploadTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 업로드 성공 시 이미지의 다운로드 URL을 가져옵니다.
                    imageRef.downloadUrl
                        .addOnSuccessListener { imageUrl ->
                            // 다운로드 URL을 콜백으로 전달
                            onComplete(imageUrl.toString())
                        }
                        .addOnFailureListener { exception ->
                            // 다운로드 URL을 가져오지 못한 경우 예외 처리
                            onComplete(null)
                        }
                } else {
                    // 업로드 실패 시 예외 처리
                    onComplete(null)
                }
            }
        } catch (e: IOException) {
            onComplete(null) // 파일 경로를 읽을 수 없을 때 null을 콜백으로 전달
        }
    }
}
