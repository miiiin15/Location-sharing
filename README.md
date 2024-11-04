# 🛰️ 프로젝트 소개 ![Watchers](https://img.shields.io/github/watchers/miiiin15/Protect?style=social)
![헤더](https://capsule-render.vercel.app/api?type=rect&height=100&color=D1DFE1&text=🗺️위치공유&fontColor=4F83CC&animation=blinking&fontSize=45&desc=실시간으로%20나의%20위치를%20공유해보자&descAlignY=80&fontAlignY=40&descSize=20&textBg=false)

![GitHub commit activity](https://img.shields.io/github/commit-activity/m/miiiin15/Protect?color=%234F83CC&label=Monthly%20Commit)
![Weekly Commits](https://img.shields.io/github/commit-activity/w/miiiin15/Protect?color=%234F83CC&label=Weekly%20Commits)
![GitHub last commit](https://img.shields.io/github/last-commit/miiiin15/Protect?color=%234F83CC&label=Last%20Commit)
![Repository Size](https://img.shields.io/github/repo-size/miiiin15/Protect?color=%234F83CC) 

이 프로젝트는 카톡이나 초대 코드공유를 통해 **실시간 위치 공유** 하여 사용자간 상호작용을 하는 앱입니다.
# 📧 연락처

문의사항이 있을 경우 이메일로 연락 부탁드립니다:

이메일 문의: gg04253@gmail.com

# 🛠️ 기술 스택

- **언어**: Kotlin
- **네트워킹**: retrofit2
- **지도 서비스**: Naver Mobile Dynamic Map Android
- **이미지 로딩**: Glide
- **Firebase**: Firestore, Authentication, Storage
- **Deep Link**: 카카오톡sdk 친구/메시지, 공유 

# ✨ 주요 기능

1. **실시간 위치 공유**: 방장이 라이브 세션을 생성하여 자신의 위치를 공유하고, 여러 참여자가 실시간으로 시청할 수 있는 기능.
![protect_sh3](https://github.com/user-attachments/assets/4f58a5b7-0661-4fd5-a358-864aabdaa0a3)
2. **공유자 간 소통**: 방장과 참여자가 간단한 메시지로 소통하는 기능

![protect_sh1](https://github.com/user-attachments/assets/b3cc79e8-de6b-423d-952a-230547b73509) ![protect_sh2](https://github.com/user-attachments/assets/a4ab55da-331f-43ad-b23b-ee8c8c4890ee) 

3. **위치공유 초대**: 방장이 카카오톡 공유(딥링크) 기능 또는 기본 안드로이드 공유 기능으로 초대 가능

![protect_sh9](https://github.com/user-attachments/assets/e371a4bb-e2f2-411b-92b4-a4318d1c4771) 

![image](https://github.com/user-attachments/assets/a3ab919b-7b70-48a8-a0ba-0c7486c1a51b)
![protect_sh8](https://github.com/user-attachments/assets/309be521-318a-46c4-b518-ea45f411ba38)


4. **프로필 설정**: 사용자가 원하는 사진과 닉네임을 프로필에 적용할 수 있음

![image](https://github.com/user-attachments/assets/67b5d99f-c0a1-4dd5-9b33-ed74d3ce1ea1)


  

# 📲 실행 방법

1. **환경 요구 사항**
   - **Java Development Kit (JDK)**: 1.8 이상
   - **Gradle**: 프로젝트에 포함된 `gradle-wrapper.properties` 파일에서 버전을 확인하십시오.
   - **Android SDK**: 최소 SDK 31 이상, 타겟 SDK 34 이상
   - **Kotlin**: 버전 1.5 이상

2. **프로젝트 클론 및 설정**
   ```bash
   git clone https://github.com/miiiin15/Protect.git
   cd Protect
   ```

3. **Android Studio에서 프로젝트 열기**
   - Android Studio를 열고 `File > Open`을 통해 클론한 프로젝트를 선택합니다.
   - 필요한 Gradle 종속성 파일이 자동으로 다운로드됩니다.

4. **Gradle 파일 동기화**
   - `build.gradle` 파일과 함께 필요한 플러그인 및 종속성을 동기화합니다.
   - 동기화 과정에서 문제가 발생하면 Android SDK와 JDK 버전을 확인하고, 요구 사항에 맞게 설정합니다.

5. **프로젝트 빌드 및 실행**
   - Android Studio의 `Run` 버튼을 클릭하여 프로젝트를 빌드하고 실행합니다.
   - **에뮬레이터** 또는 **실제 기기**에서 테스트할 수 있습니다.

