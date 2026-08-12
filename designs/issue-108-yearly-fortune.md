# 설계 문서 — #108 연도별 운세 페이지 구현

> 이 문서는 **설계(Design) 단계 산출물**이며 구현 전 **사람이 검토·수정하는 게이트**다.
> 자유롭게 수정해도 되고, 수정 후 구현 단계가 이 문서를 단일 소스로 삼아 진행한다.

- **이슈**: #108 (https://github.com/YAPP-Github/28th-App-Team-2-Android/issues/108)
- **작성**: 설계 에이전트 (Opus) / 2026-08-10
- **상태**: 승인됨(구현 가능) — 4절 리스크 항목은 각 "기본안"대로 진행

## 1. 범위

이 이슈는 2개 화면(연도 선택 + 운세 결과)과 그에 필요한 전 레이어(Domain/Data/Presentation)를 다룬다.
기존 빈 `feature:saju-contents` 모듈 안에 구현하며, 신규 모듈 생성은 불필요하다.

Files Changed 10개 내외 유지를 위해 **2개 작업 단위**로 분할한다.

### 작업 단위 A — Data + Domain 레이어 (선행)

연도별 운세 API 연동에 필요한 DTO, DataSource, Repository, UseCase, Domain Model.

### 작업 단위 B — Presentation 레이어 (A 의존)

연도 선택 화면, 운세 결과 화면, ViewModel, Navigation 라우트.

```
작업 단위 A (Data+Domain)
       |
       v
작업 단위 B (Presentation)
```

### 완료 기준

1. 연도 선택 화면에서 2021~2031 중 하나를 선택하고 CTA 버튼을 누르면 API 호출이 발생한다.
2. API 응답(score, title, content, fortuneCategories)이 결과 화면에 표시된다.
3. 로딩/에러 상태가 처리된다.
4. 뒤로가기로 연도 선택 화면에 복귀할 수 있다.
5. 현재 연도(2026)가 기본 선택되고 "올해" 라벨이 붙는다.
6. ViewModel 유닛 테스트가 통과한다.

## 2. 설계 및 실행 계획

### 2-1. API 엔드포인트 (Swagger 기준 — 단일 스펙 소스)

```
POST /api/v1/year-fortunes/{year}
- Path: year (int32)
- Request Body: 없음
- Response: CommonResponse<YearSelectionFortuneResponse>
```

**YearSelectionFortuneResponse**:

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | UUID | 운세 레코드 ID |
| `year` | int | 대상 연도 |
| `score` | int | 전체 운세 점수 |
| `title` | string | 운세 제목 |
| `content` | string | 운세 상세 내용 |
| `fortuneCategories` | `FortuneCategoryStarResponse[]` | 카테고리별 별점 |

**FortuneCategoryStarResponse**:

| 필드 | 타입 | 설명 |
|------|------|------|
| `fortuneCategory` | enum | RELATIONSHIP, LOVE, ACHIEVEMENT, MONEY, HEALTH |
| `star` | int | 별점 |

### 2-2. Domain Model 설계

```kotlin
// core/domain - model/fortune/YearFortune.kt
data class YearFortune(
    val id: String,
    val year: Int,
    val score: Int,
    val title: String,
    val content: String,
    val categories: List<FortuneCategoryStar>,
)

data class FortuneCategoryStar(
    val category: FortuneCategory,
    val star: Int,
)

enum class FortuneCategory {
    RELATIONSHIP, LOVE, ACHIEVEMENT, MONEY, HEALTH
}
```

근거: `data class` + `val` 불변 필드 (rules/10-domain P1). DTO 노출 금지 — 별도 Domain Model 정의.

### 2-3. Repository 인터페이스 (Domain)

```kotlin
// core/domain - repository/FortuneRepository.kt
interface FortuneRepository {
    suspend fun getYearFortune(year: Int): Result<YearFortune>
}
```

근거: `Result<T>` 반환 (rules/10-domain P1). "fortune" 관심사 단위 분리.

### 2-4. UseCase (Domain)

```kotlin
// core/domain - usecase/GetYearFortuneUseCase.kt
class GetYearFortuneUseCase @Inject constructor(
    private val fortuneRepository: FortuneRepository,
) {
    suspend operator fun invoke(year: Int): Result<YearFortune> =
        fortuneRepository.getYearFortune(year)
}
```

근거: 동사 시작 네이밍 (rules/10-domain P2). 현재 단일 Repository 호출이므로 pass-through이지만, 향후 일별 운세 등과 조합될 가능성을 고려해 UseCase로 분리.

### 2-5. Data 레이어 설계

**DTO** (`core:data-remote`):

```kotlin
// YearFortuneResponse.kt
@Serializable
data class YearFortuneResponse(
    val id: String,
    val year: Int,
    val score: Int,
    val title: String,
    val content: String,
    val fortuneCategories: List<FortuneCategoryStarResponse>,
)

@Serializable
data class FortuneCategoryStarResponse(
    val fortuneCategory: String,
    val star: Int,
)
```

`~Response` 네이밍 (rules/20-data P2). Raw 서버 형태 유지.

**Mapper** (DTO -> Domain, `core:data-remote` 내 확장 함수):

```kotlin
fun YearFortuneResponse.toDomain(): YearFortune = YearFortune(
    id = id,
    year = year,
    score = score,
    title = title,
    content = content,
    categories = fortuneCategories.map { it.toDomain() },
)

fun FortuneCategoryStarResponse.toDomain(): FortuneCategoryStar = FortuneCategoryStar(
    category = FortuneCategory.valueOf(fortuneCategory),
    star = star,
)
```

**DataSource 인터페이스** (`core:data`):

```kotlin
interface RemoteFortuneDataSource {
    suspend fun postYearFortune(year: Int): YearFortune
}
```

근거: REST 엔드포인트 1:1 대응 (rules/20-data P2). 동사 접두사 `post` = HTTP POST.
DataSource는 예외를 삼키지 않고 throw (rules/20-data P1). Domain Model 반환.

**DataSource 구현** (`core:data-remote`):

```kotlin
class RemoteFortuneDataSourceImpl @Inject constructor(
    private val client: Lazy<HttpClient>,
) : RemoteFortuneDataSource {
    override suspend fun postYearFortune(year: Int): YearFortune =
        client.get()
            .post("api/v1/year-fortunes/$year")
            .bodyNotNull<YearFortuneResponse>()
            .toDomain()

}
```

기존 `RemoteAuthDataSourceImpl` 패턴 재사용: `Lazy<HttpClient>`, `bodyNotNull()`.

**RepositoryImpl** (`core:data`):

```kotlin
class FortuneRepositoryImpl @Inject constructor(
    private val remoteFortuneDataSource: RemoteFortuneDataSource,
) : FortuneRepository {
    override suspend fun getYearFortune(year: Int): Result<YearFortune> =
        runCatchingCancellable {
            remoteFortuneDataSource.postYearFortune(year)
        }
}
```

근거: `runCatchingCancellable` + `Result` (rules/20-data P1). 기존 `AuthRepositoryImpl` 패턴과 동일.

**DI 바인딩**: 기존 `RepositoryModule`과 `RemoteDataSourceModule`에 `@Binds` 추가.

### 2-6. Presentation 레이어 설계

**Figma 노드 매핑** (node-id=1100:7469 "연도별 운세 filled"):

| Figma 요소 | 디자인시스템 컴포넌트 | 토큰 |
|------------|---------------------|------|
| 상단 헤더 (뒤로가기 + "연도별 운세") | `TodakunSubHeader(title="연도별 운세")` | body2SemiBold, gray925 |
| 제목 "어느 해가 궁금하시나요?" | `Text` | heading3Bold, gray975 |
| 연도 선택 박스 (2x grid) | `TodakunSelectBox` | body1Medium, primary50/600/700 (selected), coolGray300/800 (default) |
| 하단 CTA 버튼 | `PrimaryButton(size=Large)` "무료로 연도별 운세 보기" | primary600, white, body2SemiBold |
| 하단 그라데이션 | `Brush.verticalGradient(transparent -> white)` | - |

**node-id=2002:22541 "연도별 운세 - 상세"** (결과 화면 — TalkToFigma로 재조회 완료):

다크 배경(`#00010b`) 위 카드형 레이아웃. 위에서부터:

| Figma 요소 | 내용/데이터 소스 | 비고 |
|------------|------------------|------|
| Header | 뒤로가기 + "연도별 운세 결과" + 공유 아이콘(우측) | 공유 아이콘 탭 → 아래 공유 바텀시트 |
| 연도 칩 | "2026 병오년(丙午年)" | `year`(API) + **육십갑자 계산값**(클라이언트, API에 없음 — 리스크 참조). Figma 목업 원문 "정미년"은 2027년 값으로 오기이며, 검토 단계에서 `(year-4)%60` 계산이 맞음을 확인함 |
| 타이틀 | "2026년 토닥이님의 운세" | `year` 조합 문자열, 템플릿 |
| 원형 점수 배지 | "85" + "연간 종합 점수" | `score`(API) |
| 한줄 헤드라인 | "새로운 도전이 결실을 맺는 해예요." | `title`(API) |
| 카테고리 별점 3개 | 성취운/재물운/관계운 각 별 3개(칠해진 개수 = star) | `fortuneCategories`(API) — **Figma는 3개만 노출, API enum은 5개(RELATIONSHIP/LOVE/ACHIEVEMENT/MONEY/HEALTH) — 리스크 참조** |
| "종합 총평" 카드 | 긴 본문 문단 | `content`(API) |
| 하단 CTA | "토닥이에게 더 물어보기" 버튼 + 말풍선 칩("시간대는 언제가 좋아?") | **`feature:chat` 연동 — 리스크 참조** |

**node-id=2266:21869 "연도별 운세 - 공유"**: 별도 화면이 아니라 **위 결과 화면 위에 반투명 딤(40% 블랙) + 공유 바텀시트가 열린 상태**다. 신규 화면이 아니라 `ShareBottomSheet` 컴포넌트로 설계한다.

| Figma 요소 | 내용 | 비고 |
|------------|------|------|
| 타이틀 | "운세 공유하기" | |
| 버튼 1 | "카카오톡 공유" (노란색) | **Kakao Share/Link SDK 미설치 — 리스크 참조** (현재 `kakao.user`만 있음) |
| 버튼 2 | "URL 복사" | 클립보드 복사, 신규 의존성 불필요 |
| 버튼 3 | "취소" | 바텀시트 닫기 |

> 빨간 안내문구("본문 내용을 입력해주세요.")는 Kakao Link 에디터의 미입력 placeholder로 보이며 실제 노출 문구가 아닐 가능성이 있다 — 구현 시 무시하고 진행.

**ViewModel (MVI + Orbit)**:

```
// State
data class YearFortuneState(
    val years: List<Int> = (2021..2031).toList(),
    val selectedYear: Int = currentYear,
    val currentYear: Int = currentYear,
    val fortuneResult: YearFortune? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

// SideEffect
sealed interface YearFortuneSideEffect {
    data class NavigateToResult(val year: Int) : YearFortuneSideEffect
    data object NavigateBack : YearFortuneSideEffect
}
```

ViewModel은 `ContainerHost` 위임 (`by(Delegate)`) 사용 (rules/30-presentation).
비즈니스 로직은 `GetYearFortuneUseCase`에 위임 (rules/30-presentation P2).

**데이터 흐름**:

```
[YearSelectionScreen]
  사용자: 연도 선택 -> Intent: selectYear(year)
  사용자: CTA 클릭 -> Intent: requestFortune()
    -> ViewModel: GetYearFortuneUseCase(selectedYear)
      -> FortuneRepository.getYearFortune(year)
        -> RemoteFortuneDataSource.postYearFortune(year)
          -> POST /api/v1/year-fortunes/{year}
    -> Result 성공: SideEffect NavigateToResult
    -> Result 실패: State.error 업데이트

[YearFortuneResultScreen]
  ViewModel state에서 fortuneResult 읽어서 표시
  뒤로가기 -> SideEffect NavigateBack
```

**Navigation**: `core:navigation`에 라우트 정의. Navigation 3 패턴 사용.

### 2-7. 테스트 계획

| 대상 | 테스트 | 모듈 |
|------|--------|------|
| `FortuneRepositoryImpl` | 성공/실패 시 `Result` 래핑 검증 | `core:data` |
| `GetYearFortuneUseCase` | Repository 위임 검증 (pass-through이므로 간단) | `core:domain` |
| `YearFortuneViewModel` | 연도 선택 상태 변경, API 호출 성공/실패 시 상태 전이, SideEffect 발행 | `feature:saju-contents` |
| `RemoteFortuneDataSourceImpl` | Ktor MockEngine으로 API 응답 파싱 검증 | `core:data-remote` |

## 3. 파일 변경 계획 (구현 체크리스트)

### 작업 단위 A — Data + Domain

```
core/domain/src/main/java/com/kikidan/domain/
  model/fortune/
    - [ ] YearFortune.kt — Domain Model (data class)
    - [ ] FortuneCategory.kt — enum
    - [ ] FortuneCategoryStar.kt — data class
  repository/
    - [ ] FortuneRepository.kt — Repository 인터페이스
  usecase/
    - [ ] GetYearFortuneUseCase.kt — UseCase

core/data/src/main/java/com/kikidan/data/
  datasource/
    - [ ] RemoteFortuneDataSource.kt — DataSource 인터페이스
  repository/
    - [ ] FortuneRepositoryImpl.kt — RepositoryImpl
  di/
    - [ ] RepositoryModule.kt — @Binds 추가 (기존 파일 수정)

core/data-remote/src/main/java/com/kikidan/data_remote/
  dto/fortune/
    - [ ] YearFortuneResponse.kt — DTO + toDomain() Mapper
  datasource/
    - [ ] RemoteFortuneDataSourceImpl.kt — DataSource 구현
  di/
    - [ ] RemoteDataSourceModule.kt — @Binds 추가 (기존 파일 수정)
```

### 작업 단위 B — Presentation

```
feature/saju-contents/src/main/java/com/kikidan/sajucontents/
  yearfortune/
    - [ ] YearFortuneViewModel.kt — ViewModel (Orbit MVI)
    - [ ] YearFortuneState.kt — State + SideEffect 정의
    - [ ] YearSelectionScreen.kt — 연도 선택 화면 Composable
    - [ ] YearFortuneResultScreen.kt — 운세 결과 화면 Composable (헤더 공유 아이콘 포함)
    - [ ] ShareBottomSheet.kt — 공유 바텀시트(카카오톡 공유/URL 복사/취소)

core/domain/src/main/java/com/kikidan/domain/util/
    - [x] YearGanjiFormatter.kt — 연도 → 육십갑자 문자열 변환 (예: 2026 → "병오년(丙午年)"), 순수 함수 — 구현 완료, 검토 단계에서 계산값 검증됨

core/navigation/src/main/java/com/kikidan/navigation/
    - [ ] YearFortuneRoute.kt — Navigation 3 라우트 정의 (또는 기존 라우트 파일에 추가)
```

### 테스트 파일

```
core/data/src/test/.../repository/
    - [ ] FortuneRepositoryImplTest.kt

core/data-remote/src/test/.../datasource/
    - [ ] RemoteFortuneDataSourceImplTest.kt

feature/saju-contents/src/test/.../yearfortune/
    - [ ] YearFortuneViewModelTest.kt
```

## 4. 리스크 / 미해결 질문 (사람 확인 필요)

- [x] **육십갑자 계산**: `YearGanjiFormatter`(`(year-4)%60` 순환식)로 구현·검토 완료. 2026 → "병오년(丙午年)"이 맞는 값이며, 최초 Figma 목업/설계문서에 적혀 있던 "정미년(丁未年)"(2027년 값)은 오기였음 — 문서 정정함. 서버가 이 문자열을 내려줄 계획이 있다면 알려달라(현재는 클라이언트 계산 유지).
- [ ] **카테고리 3개 vs 5개**: Figma는 성취운/재물운/관계운 3개만 보여주지만 Swagger enum은 5개(RELATIONSHIP/LOVE/ACHIEVEMENT/MONEY/HEALTH)다. **기본안**: API가 내려주는 `fortuneCategories` 리스트를 그대로 순회해 렌더링(하드코딩 3개 금지) — 연도별 운세는 실제로 3개만 오는 것으로 추정하고 구현하되, 5개가 오면 자동으로 5개가 표시되게 한다. 의도적으로 3개만 표시해야 한다면 알려달라.
- [ ] **`feature:chat` 의존성 없음**: "토닥이에게 더 물어보기" 버튼은 채팅 기능으로 이동하는 것으로 보이나, 현재 base 브랜치(`feat/105-saju-contents`)엔 `feature:chat` 모듈이 없다(다른 브랜치 `feat/80-history`에만 존재). **기본안**: 이번 이슈에서는 버튼을 배치하고 `core:navigation`에 라우트 자리만 만들되, 실제 이동은 TODO로 남기거나 no-op으로 둔다. 채팅 기능과의 통합 시점을 알려달라.
- [ ] **카카오톡 공유 SDK 미설치**: `feature/auth`엔 로그인용 `kakao.user`만 있고 공유용 Kakao SDK(`v2-share`/`v2-talk`)는 없다. 신규 의존성 추가가 필요하므로 **이번 이슈 범위에 카카오톡 공유를 포함할지, URL 복사만 먼저 구현할지** 확인 필요. (URL 복사·취소는 신규 의존성 없이 구현 가능)
- [ ] **연도 범위(2021~2031) 하드코딩 여부**: Figma에는 2021~2031이 표시되어 있다. 서버에 지원 연도 목록 API가 없으므로 클라이언트에서 하드코딩할 예정이다. 범위가 달라져야 하면 알려달라.
- [ ] **"무료로" 문구**: CTA 버튼 텍스트가 "무료로 연도별 운세 보기"인데, 유료 전환 계획이 있는지? 있다면 결제 플로우 연동 범위가 달라진다. 현재는 무조건 무료 호출로 구현한다.
- [ ] **FortuneCategory enum 매핑 안전성**: 서버가 새 카테고리를 추가할 경우 `valueOf`가 실패한다. `UNKNOWN` fallback을 넣을지 여부.
- [ ] **Navigation 패턴**: `core:navigation`에 아직 라우트 파일이 없다 (빈 모듈). 기존 앱에서 사용 중인 Navigation 3 패턴이 있다면 그것을 따르겠지만, 현재 워크트리에는 참조할 패턴이 없다. Navigation 3 기본 패턴으로 진행해도 되는지?
