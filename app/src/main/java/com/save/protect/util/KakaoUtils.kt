package com.save.protect.util

import android.content.ActivityNotFoundException
import android.content.Context
import androidx.core.content.ContextCompat.startActivity
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.kakao.sdk.template.model.*
import com.save.protect.helper.Logcat

object KakaoUtils {

    fun shareText(
        context: Context,
        title: String = "Protect",
        description: String = "위치공유 초대가 왔습니다.",
        uid: String = "",
        buttonTitle: String = "참여하기",
        link: Link = Link(
            androidExecutionParams = mapOf("doc_id" to uid),
        )
    ) {

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
                    )
                )
            )
        )

        // 카카오톡 설치여부 확인
        if (ShareClient.instance.isKakaoTalkSharingAvailable(context)) {
            ShareClient.instance.shareDefault(context, defaultFeed) { sharingResult, error ->
                if (error != null) {
                    Logcat.e("카카오톡 공유 실패 : ${error.message}")
                } else if (sharingResult != null) {
                    Logcat.d("카카오톡 공유 성공")
                    sharingResult.intent.let { startActivity(context, it, null) }

                    // 카카오톡 공유에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
                    Logcat.w("카카오톡 Warning Msg: ${sharingResult.warningMsg}")
                    Logcat.w("카카오톡 Argument Msg: ${sharingResult.argumentMsg}")
                }
            }
        } else {
            // 카카오톡 미설치: 웹 공유 사용 권장
            val sharerUrl = WebSharerClient.instance.makeDefaultUrl(defaultFeed)

            try {
                KakaoCustomTabsClient.openWithDefault(context, sharerUrl)
            } catch (e: UnsupportedOperationException) {
                // TODO : CustomTabsServiceConnection 지원 브라우저가 없을 때 예외처리
            }

            // CustomTabsServiceConnection 미지원 브라우저 열기
            try {
                KakaoCustomTabsClient.open(context, sharerUrl)
            } catch (e: ActivityNotFoundException) {
                // TODO :디바이스에 설치된 인터넷 브라우저가 없을 때 예외처리
            }
        }
    }
}
