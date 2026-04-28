# Azit 프로젝트 코드 생성 가이드

이 파일은 Claude가 Azit 프로젝트에서 코드를 생성하거나 리뷰할 때 항상 준수해야 하는 규칙을 정의합니다.

---

## 1. 아키텍처: 헥사고날 아키텍처 (Hexagonal Architecture)

도메인 중심 설계(DDD)를 기반으로 하며, 의존성은 반드시 **Adapter → Application → Domain** 방향으로만 흐릅니다.

### 계층 구조 및 책임

```
com.youthexpedition.azit
├── infrastructure/          # 전역 설정 (Security, JWT, Redis, GlobalExceptionHandler)
└── modules/
    └── {module}/            # auth, member, crew, store, location, image ...
        ├── domain/
        │   └── model/       # 순수 비즈니스 로직 및 도메인 모델 (외부 의존성 금지)
        │       └── enums/   # 도메인 에러 코드, 상태값 Enum
        ├── application/
        │   ├── port/
        │   │   ├── in/      # UseCase 인터페이스, Command/Query DTO
        │   │   └── out/     # 영속성/외부 API 연동 Port 인터페이스
        │   └── service/     # UseCase 구현체
        └── adapter/
            ├── in/web/      # REST Controller (CommonResponse 규격 준수)
            └── out/
                ├── persistence/        # PersistenceAdapter
                │   └── entity/         # JPA Entity (*Entity 네이밍)
                └── mapper/             # Domain ↔ Entity 매핑
```

### Domain 계층 규칙

- **순수 POJO**여야 하며, Spring / JPA / Jakarta 등 외부 프레임워크 의존성을 가져선 안 됩니다.
- `@Entity`, `@Table`, `@NotBlank` 등의 어노테이션은 **도메인 모델에 직접 사용 금지**입니다.
- JPA 매핑은 `adapter/out/persistence/entity/` 아래 `*Entity` 클래스에서만 합니다.
- **Lombok 허용 (실용적 예외)**: `@Getter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` 등 컴파일 타임 어노테이션은 허용합니다.
- **BusinessException 허용 (실용적 예외)**: 도메인 내부에서 비즈니스 규칙 위반을 표현할 때 `BusinessException`을 직접 던지는 것을 허용합니다.

```java
// ✅ 올바른 도메인 모델
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    private Long id;
    private String nickname;
    private MemberStatus status;

    public void withdraw() {
        if (this.status == MemberStatus.WITHDRAWN) {
            throw new BusinessException(MemberErrorCode.MEMBER_ALREADY_WITHDRAWN);
        }
        this.status = MemberStatus.WITHDRAWN;
    }
}
```

### Application 계층 규칙

- **Inbound Port**: UseCase 인터페이스를 정의합니다.
- **Outbound Port**: 영속성/외부 API 연동을 위한 인터페이스를 정의합니다.
- **도메인 모델을 Controller 등 외부 계층에 직접 노출하지 않습니다.** 반드시 Command/Query DTO로 변환합니다.

```java
// Inbound Port
public interface MemberUseCase {
    void withdraw(Long memberId, String accessToken);
}

// Outbound Port
public interface SaveMemberPort {
    void save(Member member);
}
```

### Adapter 계층 규칙

- Web Adapter: 요청 DTO → Command 변환 후 UseCase 호출, 응답은 `CommonResponse` 규격 사용.
- Persistence Adapter: JPA Entity ↔ Domain Model 매핑 책임을 가집니다.

### Infrastructure 계층 규칙

- 특정 도메인에 종속되지 않는 전역 기술 기반 요소만 위치합니다.
- 구성 요소 예시: `SecurityConfig`, `JwtProvider`, `RedisConfig`, `GlobalExceptionHandler`

---

## 2. 명명 규칙 (Naming Convention)

### 허용하는 표준 약어

| 약어 | 사용 예 |
|------|---------|
| `DTO` | `UserSignUpDTO` |
| `VO` | `MoneyVO` |
| `Impl` | `UserServiceImpl` |
| `API` | `PaymentAPI` |
| `ID` | `UserId` |

### 지양하는 줄임말 → 풀네임으로 대체

| ❌ 지양 | ✅ 사용 |
|--------|--------|
| `req` | `Request` |
| `res` | `Response` |
| `cnt` | `Count` |
| `svc` | `Service` |
| `mgr` | `Manager` |

```java
// ❌
public CommonResponse<Void> signup(UserSignupReq req) { ... }

// ✅
public CommonResponse<Void> signup(UserSignupRequest request) { ... }
```

---

## 3. 기술 스택 준수

- **Java 21**: Switch Expressions, Record, Virtual Threads 등 최신 문법 적극 활용
- **Spring Boot 3.4.1**: Security Config는 람다 스타일(`.authorizeHttpRequests(auth -> auth...)`) 사용
- **Jakarta**: `javax.*` 패키지 대신 `jakarta.*` 패키지 사용

```java
// ❌
import javax.persistence.Entity;

// ✅
import jakarta.persistence.Entity;
```

---

## 4. 공통 응답 및 예외 처리

### API 응답 규격

모든 API 응답은 `CommonResponse<T>`를 사용하며, 반드시 `CommonSuccessCode`를 함께 전달합니다.

```java
// 데이터 있는 성공 응답
return CommonResponse.of(CommonSuccessCode.SUCCESS, response);

// 데이터 없는 성공 응답
return CommonResponse.of(CommonSuccessCode.SUCCESS);
```

### 예외 처리 규격

비즈니스 예외는 반드시 `BusinessException`과 도메인별 에러 코드를 사용합니다.

```java
// ✅ 올바른 예외 처리
throw new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
throw new BusinessException(AuthErrorCode.TOKEN_REUSE_DETECTED);

// ❌ 지양 (메시지 하드코딩)
throw new RuntimeException("User not found");
```

에러 코드는 `BaseErrorCode`를 구현한 도메인별 Enum으로 관리합니다.
**필드 순서**: `code` → `message` → `status` (HttpStatus)

```java
@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {
    MEMBER_NOT_FOUND("MEMBER_NOT_FOUND", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
    MEMBER_ALREADY_WITHDRAWN("MEMBER_ALREADY_WITHDRAWN", "이미 탈퇴한 사용자입니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
```

---

## 5. 단위 테스트 코드 작성 규칙

### 기본 원칙

- 모든 핵심 비즈니스 로직(Domain, Application 계층)에는 **반드시 단위 테스트**를 포함합니다.
- 테스트는 외부 의존성을 Mockito로 격리하여 **빠르고 독립적**으로 실행 가능해야 합니다.
- 테스트 메서드명은 **한글 Given_When_Then 구조**를 따릅니다.

### 메서드 명명 패턴

```java
@Test
void 이메일이_중복될_때_회원가입_실패() { ... }

@Test
void 유효한_정보로_회원가입_성공() { ... }

@Test
void 존재하지_않는_사용자_조회시_예외발생() { ... }
```

### 테스트 구조 템플릿

```java
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private LoadMemberPort loadMemberPort;

    @Mock
    private SaveMemberPort saveMemberPort;

    @Test
    void 이미_탈퇴한_회원이_탈퇴_요청시_예외발생() {
        // given
        Member member = Member.builder()
                .id(1L)
                .status(MemberStatus.WITHDRAWN)
                .build();
        given(loadMemberPort.findById(1L)).willReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> memberService.withdraw(1L, "accessToken"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_ALREADY_WITHDRAWN);
    }
}
```

### 계층별 테스트 전략

| 계층 | 테스트 종류 | 사용 도구 |
|------|-----------|---------|
| Domain | 순수 단위 테스트 | JUnit 5 (Mock 불필요) |
| Application (UseCase) | 단위 테스트 + Mock | JUnit 5 + Mockito |
| Web Adapter (Controller) | 슬라이스 테스트 | `@WebMvcTest` + MockMvc |
| Persistence Adapter | 슬라이스 테스트 | `@DataJpaTest` |

### 도메인 테스트 예시 (Mock 불필요)

```java
class MemberTest {

    @Test
    void 정상적으로_탈퇴_처리() {
        // given
        Member member = Member.builder()
                .status(MemberStatus.ACTIVE)
                .build();

        // when
        member.withdraw();

        // then
        assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
    }

    @Test
    void 이미_탈퇴한_회원이_재탈퇴_시도시_예외발생() {
        // given
        Member member = Member.builder()
                .status(MemberStatus.WITHDRAWN)
                .build();

        // when & then
        assertThatThrownBy(member::withdraw)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_ALREADY_WITHDRAWN);
    }
}
```

### 테스트 픽스처 관리

반복 사용되는 테스트 데이터는 **별도의 Fixture 클래스**로 분리합니다.

```java
public class MemberFixture {

    public static Member 활성_회원() {
        return Member.builder()
                .id(1L)
                .nickname("테스트유저")
                .status(MemberStatus.ACTIVE)
                .build();
    }
}
```

---

## 6. 빠른 체크리스트

코드 생성 또는 PR 리뷰 시 아래 항목을 확인합니다.

- [ ] Domain 계층에 `@Entity`, `@NotBlank` 등 외부 어노테이션이 없는가?
- [ ] JPA Entity는 `adapter/out/persistence/entity/` 아래 `*Entity` 클래스로 분리되어 있는가?
- [ ] 도메인 모델이 Controller 레이어까지 직접 노출되지 않는가?
- [ ] `javax.*` 대신 `jakarta.*`를 사용하고 있는가?
- [ ] `req`, `res`, `svc` 등 모호한 줄임말을 사용하지 않았는가?
- [ ] 비즈니스 예외는 `BusinessException` + 도메인 에러 코드로 처리했는가?
- [ ] 모든 API 응답이 `CommonResponse.of(CommonSuccessCode.SUCCESS, ...)` 규격을 따르는가?
- [ ] ErrorCode Enum 필드 순서가 `code → message → status` 순인가?
- [ ] 핵심 로직에 단위 테스트가 작성되어 있는가?
- [ ] 테스트 메서드명이 한글 Given_When_Then 구조를 따르는가?
