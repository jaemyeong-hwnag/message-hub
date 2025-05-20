# MessageHub Project

## 프로젝트 개요

MessageHub는 알림톡, SMS, Slack 등 다양한 메시지 채널을 통합 관리하는 메시지 허브 시스템입니다. 각 메시지 채널은 독립된 도메인으로 관리되며, 클라이언트 요청은 단일 컨트롤러를 통해 처리됩니다.

## 기술 스택 및 선정 이유

* **Java 21**: 최신 LTS(Long-Term Support) 버전으로 안정성과 최신 기능을 동시에 보장합니다. 특히 Virtual Thread(Project Loom) 지원으로 고성능 비동기 처리가
  가능해졌습니다.
* **Spring Boot 3.4.5**: Spring Framework 6 기반으로 Java 21 및 가상 스레드, Native Image와 같은 최신 기능을 완벽하게 지원합니다.
* **Virtual Threads**: 기존 플랫폼 스레드에 비해 메모리와 성능 효율이 뛰어나며, 대량의 동시 처리에 적합한 구조를 구현할 수 있도록 지원합니다.
* **MapStruct**: 컴파일 타임에 타입 안전한 매핑 코드를 자동 생성해주어 반복적인 DTO ↔ 도메인 ↔ 엔티티 매핑 작업을 줄이고 유지보수를 쉽게 해줍니다.

## 패키지 구조

```
src
└── main
    └── java
        └── com.hjm.messagehub
            ├── alimtalk        // Alimtalk 메시지 도메인
            ├── auth            // 인증 도메인
            ├── config          // 설정 클래스 (모든 클래스는 @Configuration 필수)
            ├── controller      // 통합 메시지 컨트롤러
            ├── slack           // Slack 메시지 도메인
            ├── sms             // SMS 메시지 도메인
            ├── common          // 공통 예외, 유틸리티, 상위 인터페이스 등
            └── MessageHubApplication.java
```

## 계층 구조 및 접근 원칙

* 컨트롤러는 반드시 도메인의 Facade만을 통해 도메인에 접근해야 하며, 도메인 서비스(Service), 저장소(Repository), 조회 리더(Reader) 등에는 직접 접근하지 않습니다.
* 전체 처리 흐름은 다음과 같습니다:

```
[Controller] → [Facade] → [Service] → [Reader / Store] → [Repository]
```

* 각 계층의 역할:

    * **Facade**: 트랜잭션 경계를 가지며 도메인 조합 및 유즈케이스 조율
    * **Service**: 핵심 비즈니스 로직 처리
    * **Reader / Store**: 조회/저장 분리 책임 수행 (CQRS 스타일)
    * **Repository**: JPA 기반 영속성 처리

## 예외 처리 전략

* `common.exception.BaseException`은 모든 도메인 예외의 부모 클래스입니다.
* 각 도메인 내부의 `exception` 패키지에 특화된 예외 클래스를 정의합니다.

```java
// 공통 예외
package com.hjm.messagehub.common.exception;

public abstract class BaseException extends RuntimeException {
    public BaseException(String message) {
        super(message);
    }
}

// 도메인 예외 예시
package com.hjm.messagehub.sms.exception;

import com.hjm.messagehub.common.exception.BaseException;

public class SmsSendFailedException extends BaseException {
    public SmsSendFailedException(String message) {
        super(message);
    }
}
```

## 객체 매핑 전략 (MapStruct)

* DTO ↔ 도메인 모델 ↔ JPA 엔티티 간의 변환은 MapStruct 기반 매퍼 인터페이스로 관리됩니다.
* 반복적인 변환 로직 제거 및 타입 안전성을 확보할 수 있습니다.

```java

@Mapper(componentModel = "spring")
public interface SmsMapper {
    Sms toDomain(SmsRequest request);

    SmsEntity toEntity(Sms domain);

    SmsResponse toResponse(Sms domain);
}
```

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

* 모든 컨트롤러 및 외부 계층은 반드시 해당 도메인의 Facade를 통해서만 도메인 로직에 접근합니다.
* 각 도메인은 도메인 모델(model)을 중심으로 구성되며, Entity는 반드시 도메인 모델을 거쳐서 사용해야 합니다.
* Facade 계층은 단일 도메인뿐 아니라 복합 유즈케이스를 조합할 수 있으며, 트랜잭션의 시작 지점으로 사용됩니다.
* config 패키지에는 `@Configuration`이 명시된 설정 클래스만 포함됩니다.

---

## Git 컨벤션

### 커밋 메시지

* `[TICKET-ID] prefix: 메시지` 형식으로 작성하지만, 현재는 사이드 프로젝트이므로 따로 티켓 없이 prefix 위주로 작성합니다. 추후 GitHub issue ID로 티켓 관리할 예정입니다.
* 사용 중인 prefix:

    * `feat:` 기능 추가
    * `fix:` 버그 수정
    * `style:` 코드 포맷팅, 세미콜론 누`r`락 등 기능 변경 없는 스타일 수정
    * `docs:` 문서 작성 또는 수정

### 브랜치 전략

* Git Flow 전략을 사용합니다.

### 커밋 트리 관리

* rebase 기반 워크플로우를 통해 커밋 트리를 선형(linear)으로 관리합니다.

---


이 문서는 ChatGPT의 도움을 받아 작성되었습니다. (Documentation assisted by ChatGPT)