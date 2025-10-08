# MessageHub Project

## 프로젝트 개요

MessageHub는 알림톡, SMS, Slack 등 다양한 메시지 채널을 통합 관리하는 메시지 허브 시스템입니다. 멀티모듈 구조로 설계되어 각 모듈이 독립적인 역할을 수행하며, 클라이언트 요청은 단일 API
컨트롤러를 통해 처리됩니다.

## 기술 스택 및 선정 이유

* **Kotlin**: Java와의 100% 호환성을 유지하면서 더 간결하고 안전한 코드 작성이 가능합니다.
* **Java 21**: 최신 LTS(Long-Term Support) 버전으로 안정성과 최신 기능을 동시에 보장합니다. 특히 Virtual Thread(Project Loom) 지원으로 고성능 비동기 처리가
  가능해졌습니다.
* **Spring Boot 3.x**: Spring Framework 6 기반으로 Java 21 및 가상 스레드, Native Image와 같은 최신 기능을 완벽하게 지원합니다.
* **Gradle Multi-Project**: 모듈별 독립적인 빌드와 의존성 관리를 통해 확장성과 유지보수성을 향상시킵니다.

## 멀티모듈 구조

프로젝트는 다음과 같은 모듈들로 구성되어 있습니다:

```
message-hub/
├── api/                    // 메인 애플리케이션 모듈
│   ├── src/main/kotlin/api/
│   │   ├── controller/     // REST API 컨트롤러
│   │   └── MessageHubApplication.kt
│   └── build.gradle.kts
├── common/                 // 공통 유틸리티 및 설정
│   ├── src/main/kotlin/
│   │   ├── enums/          // 공통 열거형
│   │   └── util/           // 유틸리티 클래스
│   └── build.gradle.kts
├── domain/                 // 도메인 모델
│   ├── src/main/kotlin/enums/
│   │   ├── Channel.kt      // 메시지 채널 인터페이스
│   │   ├── MessageStyle.kt // 메시지 스타일
│   │   └── slack/          // Slack 관련 도메인
│   └── build.gradle.kts
├── external/
│   ├── client/             // 외부 API 클라이언트
│   │   ├── src/main/kotlin/
│   │   │   ├── Sender.kt   // 메시지 발송 인터페이스
│   │   │   └── slack/      // Slack 클라이언트 구현
│   │   └── build.gradle.kts
│   └── storage/            // 데이터 저장소 레이어
│       ├── src/main/kotlin/
│       │   └── com/hjm/messagehub/storage/
│       │       └── MessageRepository.kt
│       └── build.gradle.kts
├── provider/               // 메시지 제공자 모듈
│   └── build.gradle.kts
└── worker/                 // 백그라운드 작업 처리
    └── build.gradle.kts
```

### 모듈별 역할

* **api**: REST API 엔드포인트와 메인 애플리케이션 진입점
* **common**: 공통 유틸리티, 열거형, 헬퍼 클래스
* **domain**: 비즈니스 도메인 모델과 인터페이스 정의
* **external/client**: 외부 서비스(Slack, SMS 등) 클라이언트 구현
* **external/storage**: 데이터 저장소 접근 계층
* **provider**: 메시지 제공자별 비즈니스 로직
* **worker**: 큐 폴링 해당 큐 작업

## 계층 구조 및 접근 원칙

현재 프로젝트는 멀티모듈 아키텍처를 기반으로 다음과 같은 계층 구조를 가집니다:

* **API Layer** (`api` 모듈): REST API 엔드포인트 제공
* **Domain Layer** (`domain` 모듈): 비즈니스 도메인 모델 및 인터페이스
* **Provider Layer** (`provider` 모듈): 메시지 제공자별 비즈니스 로직
* **External Layer** (`external` 모듈): 외부 서비스 연동 및 데이터 저장
* **Worker Layer** (`worker` 모듈): 큐 폴링 해당 큐 작업

### 처리 흐름

```
1. 메시지 수신: [API Controller] → [Message Queue] 등록
2. 큐 폴링: [Worker] ← [Message Queue] 폴링
3. 큐 작업 위임: [Worker] → [Provider Service] 호출
4. 비즈니스 처리: [Provider Service] → [External Client] → [External Service]
5. 결과 저장: [Provider Service] → [External Storage] (상태 업데이트)
```

### 모듈 간 의존성 관계

* `api` → `common`, `domain`
* `provider` → `common`, `domain`
* `external/client` → `common`, `domain`
* `external/storage` → `common`, `domain`
* `worker` → `common`, `domain`

### 각 계층의 역할

* **API Layer**: HTTP 요청/응답 처리, 입력 검증, 메시지 큐 등록
* **Provider Layer**: 메시지 제공자별 비즈니스 로직, 실제 메시지 발송 처리, 결과 저장
* **External Client**: 외부 서비스(Slack, SMS 등) API 연동
* **External Storage**: 메시지 데이터 저장 및 조회
* **Worker**: 메시지 큐 폴링, 큐 작업 처리 (비즈니스 로직은 Provider에 위임)

## 예외 처리 전략

* `common.exception.BaseException`은 모든 도메인 예외의 부모 클래스입니다.
* 각 모듈 내부의 `exception` 패키지에 특화된 예외 클래스를 정의합니다.

```kotlin
// 공통 예외
package com.hjm.messagehub.common.exception

abstract class BaseException(message: String) : RuntimeException(message)

// 도메인 예외 예시
package com.hjm.messagehub.sms.exception

import com . hjm . messagehub . common . exception . BaseException

class SmsSendFailedException(message: String) : BaseException(message)
```

## 메시지 큐 시스템

### 큐 처리 방식

* **비동기 처리**: 컨트롤러에서 즉시 응답 반환, 워커에서 백그라운드 처리
* **배치 처리**: 주기적으로 큐에서 메시지를 배치로 가져와 일괄 처리
* **폴링 방식**: 워커가 설정된 간격으로 큐를 폴링하여 새로운 메시지 처리

### 큐 처리 흐름

1. **메시지 등록**: API 컨트롤러에서 메시지 큐에 작업 등록
2. **큐 폴링**: 워커가 주기적으로 큐에서 메시지 조회
3. **서비스 호출**: 워커가 Provider Service에 메시지 처리 위임
4. **메시지 발송**: Provider Service에서 실제 외부 서비스로 메시지 발송
5. **상태 업데이트**: Provider Service에서 발송 결과를 데이터베이스에 저장

### 처리 방식별 특징

* **비동기 처리**: 실시간 응답성 우수, 시스템 부하 분산
* **배치 처리**: 대량 메시지 효율적 처리, 리소스 최적화
* **폴링 방식**: 안정적인 메시지 처리, 장애 복구 용이

## 트랜잭션 및 OSIV(Open Session In View) 설정

* application.yml 설정:

```yaml
spring:
  jpa:
    open-in-view: false
```

* open-in-view를 false로 설정한 이유:

    * 트랜잭션 경계를 명확하게 유지하기 위함
    * Lazy 로딩은 반드시 서비스 계층 내에서 처리 후 DTO로 변환
    * 비동기 작업 중 트랜잭션 누수 및 예외 발생을 방지하기 위함

## 개발 가이드

### 모듈별 개발 규칙

* **API 모듈**: REST API 엔드포인트만 정의하고, 비즈니스 로직은 다른 모듈에 위임
* **Provider 모듈**: 메시지 제공자별 비즈니스 로직 구현 및 처리 전략 정의
* **External 모듈**: 외부 서비스 연동 및 데이터 저장 로직만 포함
* **Domain 모듈**: 순수한 도메인 모델과 인터페이스만 정의 (의존성 없음)
* **Common 모듈**: 공통 유틸리티, 열거형, 헬퍼 클래스 제공

### 의존성 관리

* 모듈 간 순환 의존성을 방지합니다.
* `domain` 모듈은 다른 모듈에 의존하지 않습니다.
* `common` 모듈은 가장 하위 레벨에서 사용됩니다.

---

## CI/CD (GitHub Actions)

### 자동 테스트 설정

프로젝트는 GitHub Actions를 통해 자동 테스트를 수행합니다.

#### 테스트 분류

* **Unit Tests** (`@Tag("unit")`): 단위 테스트, 빠른 실행
* **Integration Tests** (`@Tag("integration")`): 통합 테스트, 외부 의존성 필요

#### 테스트 실행 방식

```bash
# 모든 모듈의 Unit 테스트 실행 (CI/CD에서 실행)
./gradlew test

# 특정 모듈의 테스트만 실행
./gradlew :api:test
./gradlew :common:test
./gradlew :domain:test
./gradlew :external:client:test
./gradlew :external:storage:test
./gradlew :provider:test
./gradlew :worker:test

# Integration 테스트만 실행 (로컬에서 필요시)
./gradlew integrationTest

# 모든 테스트 실행 (Unit + Integration)
./gradlew test integrationTest
```

#### 멀티모듈 테스트 실행

**CI/CD에서는 모든 모듈의 테스트가 병렬로 실행됩니다:**

- `common` 모듈 테스트
- `domain` 모듈 테스트
- `api` 모듈 테스트
- `external/client` 모듈 테스트
- `external/storage` 모듈 테스트
- `provider` 모듈 테스트
- `worker` 모듈 테스트

#### GitHub Actions 워크플로우

**자동 실행 조건:**

* `main`, `develop` 브랜치에 push
* 다음 브랜치로의 PR:
    - ✅ **feature 브랜치** (`feature/*`)
    - ✅ **release 브랜치** (`release/*`)
    - ✅ **hotfix 브랜치** (`hotfix/*`)
    - ✅ **main, develop 브랜치**

**PR 이벤트:**

- ✅ **PR 생성 시** (`opened`)
- ✅ **PR 업데이트 시** (`synchronize`) - 새 커밋 push
- ✅ **PR 재오픈 시** (`reopened`)
- ✅ **PR 리뷰 준비 완료 시** (`ready_for_review`)

**테스트 워크플로우 (`test.yml`):**

* **환경**: Ubuntu, Java 21, Gradle
* **실행 테스트**: 모든 모듈의 Unit 테스트 실행 (Integration 테스트 제외)
* **결과 리포트**: PR에 테스트 결과 자동 표시 및 체크 실행
* **권한**: PR 댓글, 체크 실행, 상태 업데이트 권한
* **캐시**: Gradle 의존성 캐시 활용

**빌드 워크플로우 (`build.yml`):**

* **환경**: Ubuntu, Java 21, Gradle
* **실행 작업**: 프로젝트 빌드 (테스트 제외)
* **아티팩트**: 빌드 결과물 자동 업로드

**PR 검사 워크플로우 (`pr-check.yml`):**

* **환경**: Ubuntu, Java 21, Gradle
* **실행 작업**: 코드 스타일, 단위 테스트, 빌드 검사
* **PR 댓글**: 검사 결과를 PR에 자동 댓글

**PR 체크리스트:**

- [ ] 테스트 통과 (자동)
- [ ] 빌드 성공 (자동)
- [ ] 코드 스타일 검사 통과 (자동)
- [ ] PR 댓글 확인 (자동)
- [ ] 코드 리뷰 완료 (수동)

### 테스트 작성 가이드

```kotlin
// Unit 테스트 예시
@Test
@Tag("unit")
fun `단위 테스트 예시`() {
    // 빠르고 독립적인 테스트
}

// Integration 테스트 예시  
@Test
@Tag("integration")
fun `통합 테스트 예시`() {
    // 외부 의존성이 필요한 테스트
}
```

---

## Git 컨벤션

### 커밋 메시지

* `[TICKET-ID] prefix: 메시지`
* 사용 중인 prefix:

    * `feature:` 기능 추가
    * `fix:` 버그 수정
    * `Refactoring:` 리팩토링

### 브랜치 전략

**Git Flow 전략을 사용합니다.**

#### 브랜치 구조

* **`main`**: 프로덕션 배포 브랜치 (안정화된 코드)
* **`develop`**: 개발 통합 브랜치 (다음 릴리즈를 위한 개발)
* **`feature/*`**: 기능 개발 브랜치 (예: `feature/KAN-1-slack-integration`)
* **`release/*`**: 릴리즈 준비 브랜치 (예: `release/v1.2.0`)
* **`hotfix/*`**: 긴급 수정 브랜치 (예: `hotfix/critical-bug-fix`)

#### 브랜치별 자동 테스트

* **feature → develop**: 단위 테스트, 빌드 검사
* **develop → main**: 단위 테스트, 빌드 검사
* **release → main**: 모든 테스트 (단위 + 통합), 릴리즈 검사
* **hotfix → main**: 모든 테스트, 긴급 배포 검사

### 커밋 트리 관리

* rebase 기반 워크플로우를 통해 커밋 트리를 선형(linear)으로 관리합니다.

---


이 문서는 ChatGPT의 도움을 받아 작성되었습니다. (Documentation assisted by ChatGPT)