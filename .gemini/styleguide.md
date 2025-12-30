# Azit 프로젝트 코딩 및 아키텍처 스타일 가이드

이 문서는 Azit 프로젝트의 코드 생성 및 리뷰를 위한 가이드라인입니다. Gemini는 모든 코드 제안 시 아래 규칙을 준수해야 합니다.

## 1. 아키텍처 원칙: 헥사고날 아키텍처 (Hexagonal Architecture)
도메인 중심 설계(DDD)를 기반으로 기술적 세부 사항이 비즈니스 로직을 침범하지 않도록 계층을 분리합니다.

### 계층별 규칙
- **Domain 계층**:
    - 모든 비즈니스 핵심 로직과 엔티티를 포함합니다.
    - **중요**: 프레임워크(JPA, Spring, Hibernate 등)에 대한 의존성이 전혀 없는 **순수 자바 객체(POJO)**여야 합니다.
    - `@Entity` 어노테이션을 도메인 모델에 직접 사용하지 마십시오.
- **Application 계층**:
    - 비즈니스 유즈케이스(UseCase)와 포트(Inbound/Outbound Port)를 정의합니다.
    - 도메인 모델 간의 흐름을 제어하며, 구체적인 구현 기술을 알지 못해야 합니다.
- **Adapter 계층**:
    - 외부 기술(Web, DB, Redis 등)과의 연결을 담당합니다.
    - **Inbound Adapter**: REST Controller 등이 위치하며, `CommonResponse`로 응답해야 합니다.
    - **Outbound Adapter**: JPA Repository, QueryDSL 등을 사용하여 포트를 구현합니다.

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
- **Lombok**: `@Data` 사용을 금지하고, `@Getter`, `@Builder`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)`를 조합하여 사용하십시오.

### Record 활용 지침
Java 21의 `record`를 상황에 맞게 전략적으로 사용합니다.
- **DTO 및 VO**: 데이터 전달이 목적인 객체와 도메인 내 값 객체(VO)는 `record` 사용을 우선적으로 고려하십시오.
- **Domain Entity**: 식별자가 있고 상태 변경이 빈번한 도메인 엔티티는 `@Getter`와 `@Builder`를 갖춘 `class`를 사용하십시오.

## 3. 공통 응답 및 예외 처리 (Error Handling)
- 모든 API 응답은 프로젝트에서 정의한 `CommonResponse<T>` 또는 `CommonErrorResponse`를 사용하십시오.
- 비즈니스 예외 발생 시 `BusinessException(CommonErrorCode)`를 던지도록 제안하십시오.
- 유효성 검증 실패 시 `@Valid`와 함께 `MethodArgumentNotValidException`을 처리하는 로직을 포함하십시오.

## 4. 테스트 코드
- 모든 핵심 로직에는 단위 테스트를 포함하십시오.
- 테스트 메서드 명은 `Given_When_Then` 구조를 따르는 한글 이름을 허용합니다.
- 예: `회원가입_성공_테스트()`