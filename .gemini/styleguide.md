# Azit 프로젝트 코딩 및 아키텍처 스타일 가이드

이 문서는 Azit 프로젝트의 코드 생성 및 리뷰를 위한 가이드라인입니다. Gemini는 모든 코드 제안 시 아래 규칙을 준수해야 합니다.

## 1. 아키텍처 원칙: 헥사고날 아키텍처 (Hexagonal Architecture)
도메인 중심 설계(DDD)를 기반으로 기술적 세부 사항이 비즈니스 로직을 침범하지 않도록 계층을 분리합니다.

### 계층별 규칙
- **Domain 계층**:
  - 모든 비즈니스 핵심 로직과 엔티티를 포함합니다.
  - **도메인 순수성**: 외부 프레임워크(Spring, JPA, Jakarta 등) 및 라이브러리에 대한 의존성이 전혀 없는 **순수 자바 객체(POJO)**여야 합니다.
  - `@Entity`는 물론, `@NotBlank`와 같은 유효성 검사 어노테이션도 도메인 모델에는 직접 사용하지 않습니다.
- **Application 계층**:
  - 비즈니스 유즈케이스를 수행하고 흐름을 제어합니다.
  - **포트(Port) 정의**: 외부와의 통신을 위한 인터페이스(Inbound: UseCase / Outbound: Port)를 정의합니다.
  - **DTO 전략**: 외부 어댑터로부터 전달받은 데이터를 비즈니스 로직에 맞게 가공한 **Command/Query DTO**를 정의합니다.
  - **도메인 보호**: 도메인 모델을 애플리케이션 계층 외부(Controller 등)로 직접 노출하지 않습니다.
- **Adapter 계층**:
  - 외부 기술(Web, Persistence, 외부 API 등)과의 실제 연결을 담당합니다.
  - **변환 책임**: 외부 요청 DTO를 애플리케이션 명령(Command)으로 변환하거나, 내부 결과를 외부 응답 규격으로 변환하는 책임을 가집니다.
  - **Web Adapter**: REST Controller가 위치하며, 응답 시 `CommonResponse` 규격을 준수합니다.
  - **Persistence Adapter**: JPA Repository, QueryDSL 등을 사용하여 아웃바운드 포트를 구현하며, 엔티티와 도메인 모델 간의 매핑을 수행합니다.
- **Infrastructure 계층**:
  - 특정 도메인에 종속되지 않는 **시스템 전역의 기술적 기반**을 담당합니다.
  - 구성 요소: `SecurityConfig`, `JwtProvider`, `RedisConfig`, `GlobalExceptionHandler` 등.
  - 전역적인 보안 필터링, 예외 처리, 공통 라이브러리 설정을 관리합니다.

### 의존성 방향
- 의존성은 항상 **바깥쪽에서 안쪽(Adapter -> Application -> Domain)**으로 향해야 합니다.
- 내부 계층(Domain, Application)은 외부 계층(Adapter)의 존재를 몰라야 합니다.

## 2. 명명 규칙 및 코드 스타일 (Naming & Style)
가독성을 최우선으로 하며, 협업 시 오해를 줄이기 위해 명확한 용어를 사용합니다.

### 줄임말 사용 가이드 (Practical Naming)
무분별한 줄임말은 지양하되, 업계 표준으로 통용되는 기술 용어는 허용합니다.

- **허용하는 표준 약어**:
    - `DTO`, `VO`, `Impl`, `API`, `DAO`, `ID`
    - 예: `UserSignUpDTO`, `UserServiceImpl` (O)
- **지양하는 모호한 줄임말 (풀네임 권장)**:
    - `req` -> `Request`, `res` -> `Response`
    - `cnt` -> `Count`, `svc` -> `Service`
    - `mgr` -> `Manager`, `param` -> `Parameter`
    - `info` -> `Information`
- **적용 예시**:
    - `UserSignupReq` (X) -> `UserSignupRequest` (O)
    - `UserSvc` (X) -> `UserService` (O)

### 기술 스택 준수
- **Java 21**: 최신 문법(Switch Expressions 등)과 가상 스레드(Virtual Threads) 사용을 고려하십시오.
- **Spring Boot 3.5.x**: 최신 설정 방식(특히 Security Config의 람다 스타일)을 사용하십시오.
- **Jakarta**: `javax` 패키지 대신 `jakarta` 패키지를 사용하십시오.

## 3. 공통 응답 및 예외 처리 (Error Handling)
- 모든 API 응답은 프로젝트에서 정의한 `CommonResponse<T>` 또는 `CommonErrorResponse`를 사용하십시오.
- **비즈니스 예외 상황에서는 `BusinessException`을 사용하십시오.**
- 예외 발생 시 `throw new BusinessException(UserErrorCode.USER_NOT_FOUND)`와 같이 도메인별 에러 코드(BaseErrorCode 구현체)를 인자로 전달하는 방식을 제안하십시오.

## 4. 테스트 코드
- 모든 핵심 로직에는 단위 테스트를 포함하십시오.
- 테스트 메서드 명은 `Given_When_Then` 구조를 따르는 한글 이름을 허용합니다.
- 예: `회원가입_성공_테스트()`