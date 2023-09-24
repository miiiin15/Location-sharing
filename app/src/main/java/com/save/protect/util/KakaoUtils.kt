// PermissionUtils.kt

package com.save.protect.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.kakao.sdk.template.model.*


object KakaoUtils {

    fun shareText(
        context: Context,
        title: String = "Protect",
        description: String = "위치공유 초대가 왔습니다.",
        uid: String = "",
        buttonTitle: String = "참여하기",
        link: Link = Link(
            androidExecutionParams = mapOf("doc_id" to uid),
//                        iosExecutionParams = mapOf("key1" to uid, "key2" to "value2")
        )
    ) {

        val deepLinkUri = "myapp://AudienceActivity?param=${uid}"

        val defaultFeed = FeedTemplate(
            content = Content(
                title = title,
                description = description,
                imageUrl = "",
                link = link
            ),

            buttons = listOf(
                Button(
                    buttonTitle,
                    Link(
                        androidExecutionParams = mapOf("doc_id" to uid),
//                        iosExecutionParams = mapOf("key1" to uid, "key2" to "value2")
                    )
                )
            )
        )

// 카카오톡 설치여부 확인
        if (ShareClient.instance.isKakaoTalkSharingAvailable(context)) {
            // 카카오톡으로 카카오톡 공유 가능
            ShareClient.instance.shareDefault(context, defaultFeed) { sharingResult, error ->
                if (error != null) {
                    Log.e("카카오톡", "카카오톡 공유 실패", error)
                } else if (sharingResult != null) {
                    Log.d("카카오톡", "카카오톡 공유 성공 ${sharingResult.intent}")
                    sharingResult?.intent?.let { startActivity(context, it, null) }

                    // 카카오톡 공유에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
                    Log.w("카카오톡", "Warning Msg: ${sharingResult.warningMsg}")
                    Log.w("카카오톡", "Argument Msg: ${sharingResult.argumentMsg}")
                }
            }
        } else {
            // 카카오톡 미설치: 웹 공유 사용 권장
            // 웹 공유 예시 코드
            val sharerUrl = WebSharerClient.instance.makeDefaultUrl(defaultFeed)

            // CustomTabs으로 웹 브라우저 열기

            // 1. CustomTabsServiceConnection 지원 브라우저 열기
            // ex) Chrome, 삼성 인터넷, FireFox, 웨일 등
            try {
                KakaoCustomTabsClient.openWithDefault(context, sharerUrl)
            } catch (e: UnsupportedOperationException) {
                // CustomTabsServiceConnection 지원 브라우저가 없을 때 예외처리
            }

            // 2. CustomTabsServiceConnection 미지원 브라우저 열기
            // ex) 다음, 네이버 등
            try {
                KakaoCustomTabsClient.open(context, sharerUrl)
            } catch (e: ActivityNotFoundException) {
                // 디바이스에 설치된 인터넷 브라우저가 없을 때 예외처리
            }
        }
    }
}
