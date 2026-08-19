# 설계 문서 — #107 [Feat] 택일 운세 페이지 구현

> 이 문서는 **설계(Design) 단계 산출물**이며 구현 단계가 이 문서를 단일 소스로 삼아 진행한다.

- **이슈**: [#107](https://github.com/YAPP-Github/28th-App-Team-2-Android/issues/107)
- **작성**: 설계 에이전트 (Opus) + 코디네이터 정리 / 2026-08-10
- **상태**: `승인됨(구현 가능) — Part A, Part B1/B2(블로커 B-2 제외 전부 확정)`

---

## 1. 범위

### 문제 정의 / 목표

사용자가 **목적**(계약·이사 / 개업 / 여행 / 고백·소개팅 / 시험·면접)과 **후보 날짜(최대 5개)** 를 고르면, 날짜별 택일 운세(점수 · 제목 · 요약 분석 · 카테고리별 별점)를 받아 보여준다.

서버가 사주를 JWT로 식별하므로 앱에서 생년월일 등 사주 입력을 다시 받지 않는다(2절 참고 스펙 근거). 단, **성별 선택 UI는 예외** — 사주 재입력이 아니라 이 화면 전용 신규 입력값으로 확인됐다(4절 블로커 B-2).

완료 기준(Acceptance Criteria):

**Part A — 데이터/도메인**
- [ ] `POST /api/v1/day-fortunes` 호출로 `List<DayFortune>` 도메인 모델을 얻는 경로가 완성된다.
- [ ] 성공/실패가 `Result<T>`로 표현되고 예외가 UI까지 raw로 새지 않는다.
- [ ] 목적 5종 · 카테고리 5종이 도메인 enum이며, 서버가 모르는 값을 보내도 앱이 죽지 않는다.
- [ ] `./gradlew :core:domain:test :core:data:test :core:data-remote:test` 통과.

**Part B1 — 입력 화면 + 캘린더 바텀시트**
- [ ] 목적 칩 5개 중 1개 선택 → 선택 상태가 시각적으로 구분된다.
- [ ] 성별(남성/여성) 토글이 동작하고 상태에 반영된다. ※ 서버 전송은 B-2 해소 후 (전송 안 해도 완료로 본다)
- [ ] `SelectField` 탭 → 캘린더 바텀시트가 열린다.
- [ ] 캘린더에서 과거 날짜는 선택 불가, 오늘은 선택 가능, **5개** 초과 시 토스트.
- [ ] 선택 날짜가 칩으로 표시되고 개별 삭제·전체 초기화가 동작한다.
- [ ] 목적 미선택 또는 날짜 0개면 CTA 비활성.
- [ ] 제출 중 CTA 비활성 + 인디케이터, 실패 시 스낵바.

**Part B2 — 결과 화면**
- [ ] 날짜 탭 전환 시 해당 날짜의 점수·제목·본문·별점이 바뀐다.
- [ ] 응답의 `fortuneCategories` 개수만큼 카테고리 칸이 그려진다 (1~5개 모두 깨지지 않음).
- [ ] 별은 `star` 값만큼 채워 그린다 (상한 상수 없음).
- [ ] 다크 배경 + 카드 레이아웃이 Figma와 일치한다.
- [ ] 로딩/에러 상태가 화면에 표현된다.

**공통**
- [ ] 색·타이포·간격이 `core:designsystem` 토큰만 사용 (하드코딩 0건).
- [ ] `./gradlew ktlintCheck` 통과.

### 포함
- `core:domain` / `core:data` / `core:data-remote` — 택일 운세 API 연동 (Part A)
- `feature:saju-contents` — 입력 화면 + 캘린더 바텀시트 + 결과 화면 (Part B1/B2)
- `core:designsystem` — 캘린더 컴포넌트 신규, `TodakunChip` 버그 수정, 별점/공유 아이콘 추가

### 제외 (Non-goals)
- **앱 전역 Navigation 3 호스트 구축.** `core:navigation`에 `src/main` Kotlin 소스가 한 줄도 없고 `MainActivity`는 아직 `Greeting("Android")` 스텁이다. → **화면 컴포저블만 만들고 `MainActivity`에서 임시 직배선한다.** 정식 nav3 인프라 구축은 별도 이슈로 분리 (사람 확정).
- **"토닥이에게 더 물어보기" 채팅 연결.** 채팅 화면 자체가 미구현. 버튼은 배치하되 콜백만 노출, 배선은 후속 이슈.
- **결과 공유 / 캘린더 내보내기 실제 동작.** Swagger에 대응 엔드포인트가 없다. → 아이콘만 배치, 콜백은 no-op (사람 확정).
- **유료/프리미엄 게이팅.** CTA "무료로 택일 보기"는 마케팅 문구로 취급한다. Swagger에 무료/유료 구분 필드가 전혀 없다. → **게이팅 없음, 전체 무료로 구현** (사람 확정).
- **성별 값의 서버 전송.** UI는 만들되 전송은 백엔드 대응 후 (블로커 **B-2**, 4절).
- 로컬 캐싱. 서버가 멱등하다 ("이미 생성된 (목적, 날짜) 조합이 있다면 기존 결과를 반환한다").

---

## 2. 설계 및 실행 계획

### 2-1. 참고 스펙

#### Swagger — 확정 ✅

**`POST /api/v1/day-fortunes`** — "택일 운세 생성"
> 회원이 선택한 목적과 후보 날짜(최대 5개)에 대한 택일 운세를 생성한다. 이미 생성된 (목적, 날짜) 조합이 있다면 기존 결과를 반환한다(멱등).

Request — `CreateDaySelectionFortuneRequest`

| 필드 | 타입 | 제약 |
|------|------|------|
| `purpose` | `string` | 필수 |
| `targetDates` | `string[]` (`format: date`) | 필수. **minItems 1 / maxItems 5**, 과거 날짜 불가 |

Response — `CommonResponse<List<DaySelectionFortuneResponse>>`

| 필드 | 타입 | 비고 |
|------|------|------|
| `id` | `string` (uuid) | 필수 |
| `purpose` | enum | `CONTRACT_MOVING`, `BUSINESS_OPENING`, `TRAVEL`, `CONFESSION_DATING`, `EXAM_INTERVIEW` |
| `targetDate` | `string` (date) | 필수 |
| `score` | `int32` | 필수. Figma 예시 85 |
| `title` | `string` | 필수. Figma 예시 "이 날짜엔 새로운 시작이 아주 잘 맞아요." |
| `content` | `string` | 필수. "요약 분석" 본문 |
| `fortuneCategories` | `[{ fortuneCategory, star }]` | enum: `RELATIONSHIP`, `LOVE`, `ACHIEVEMENT`, `MONEY`, `HEALTH` / `star`: int32 (상한 미문서화) |

확정되는 사실: 목적은 정확히 5종 · **후보 날짜 상한 5개 확정**(`maxItems=5` + Figma 유효성검사 토스트 등 6개 노드 근거) · 사주 입력 UI 불필요(`GET /api/v1/saju/me`로 서버가 이미 조회) · 1회 호출로 최대 5일치 동시 수신 · 인증은 `NetworkModule`의 `installBearerAuth`로 이미 해결됨.

#### Figma — 8개 노드 전수 조사 결과 ✅ (TalkToFigma 소켓, 채널 `qbnf3csu`)

파일 `bLZr7Nh53PmRHuEjX7gNco`. 모든 프레임 393x852 기준(iPhone).

| # | node-id | 프레임 이름 | 역할 |
|---|---------|-------------|------|
| 1 | `1086:24067` | 택일 운세 default | 입력 화면 — 초기(빈) 상태 |
| 2 | `1100:7253` | 택일 운세 filled | 입력 화면 — 목적·날짜 채워진 상태 (성별 칩 포함) |
| 3 | `1099:6279` | 후보 날짜 선택_default | 캘린더 바텀시트 — 선택 0개 |
| 4 | `1099:6584` | 후보 날짜 선택_선택중 | 캘린더 바텀시트 — 2개 선택, 칩 행 없음(갱신 누락) |
| 5 | `1099:6998` | 후보 날짜 선택_선택중 | 캘린더 바텀시트 — 2개 선택 + 칩 행 (최신) |
| 6 | `1284:7825` | 후보 날짜 선택_유효성검사 | 캘린더 바텀시트 — 상한 초과 토스트 ("5개까지만") |
| 7 | `2926:29057` | 택일 운세 - 상세 | 결과 화면 (height 1175, 스크롤 전체) |
| 8 | `2926:29649` | 택일 운세 - 상세 | 7번과 동일 + 딤 오버레이 (중복) |

**(A) 입력 화면 — `1086:24067` / `1100:7253`**
```
Status Bar 52 / Progress bar 393x48 (chevron 20 + track 316x6, 진행률 210/316≈66% + "택일 운세")
"무슨 날을 정하시나요?"  Noto Sans KR Bold 24 / #171717
[성별 선택]  168.5x48 두 칸(남성/여성), r8, 그라데이션 배경  ← 실제 기능 확정 (블로커 B-2)
목적 칩 353x108 (2줄, gap 12): 계약·이사(선택 fill #9c8af6) / 개업 / 여행 / 고백·소개팅 / 시험·면접
  비선택: fill #f5f5f5, text #505866
"후보 날짜 (5개까지 가능)"  Noto Sans KR Bold 24 + Pretendard Regular 14 / #737373
SelectField 353x48 r12 fill #fafafa — default: placeholder / filled: 선택 날짜 나열 + 개별삭제 + chevron
CTA 353x52 r12 fill #7f73ea "무료로 택일 보기"
```

**(B) 캘린더 바텀시트 — `1099:6279`/`1099:6584`/`1099:6998`/`1284:7825`**
```
Bottom Sheet 393x716, Handle 32x4
"후보 날짜 선택" + "5개까지 날짜 선택 가능해요"
월 리스트(세로 스크롤) — 요일행 + 날짜셀 44~48x44~48 (선택 상태 그린 노드 기준 48로 채택, 터치타겟 권장치와도 부합)
  기본 #171717 / 오늘 #7f73ea+"오늘"라벨 / 선택 fill #a99af6 원형
선택 칩 행(≥1일 때만) — 날짜 1개당 칩 1개(범위표기 노드는 더미로 판단), r99 stroke #c1b4f6, 개별삭제
버튼 행: [초기화] 88x52 / [선택 완료] 257x52 (비활성 #f1f1f1 / 활성 #7f73ea)
Toast: "후보 날짜는 5개까지만 선택 가능합니다." (#000000, 236x36 r8)
```

**(C) 결과 화면 — `2926:29057`**
```
배경 #00010b + 반투명 이미지(다크 전용, 신규 토큰 필요)
Header: 뒤로가기 + 진행바(210/316, 동일 스펙) + "택일 운세 결과" + [공유/캘린더내보내기] 아이콘(no-op)
chip2 "계약 ∙ 이사" (fill #5757d7)
"선택하신 후보 일자 중 최고의 기운을 찾아봤어요"
날짜 탭 353x54 — SelectBox N개(gap14, results.size만큼 동적): 선택 fill #7f73ea / 비선택 white@5%
점수 카드 353x385 r16 fill white@10%
  원 180x181 fill #a99af6 → "85" Bold40 + "운세 점수" white@60%
  제목 Noto Sans KR Bold 22
  카테고리 별점 — Figma 시안은 3칸(재물운/화합운/사고예방)이지만 **실제 구현은 API 응답 개수(최대 5개)를 그대로 렌더링** (2-2절 D9)
요약 분석 카드 353x286 r16 — "요약 분석" + 본문
CTA "토닥이에게 더 물어보기"
```

Figma에 없는 것: 로딩 상태, 에러 상태, 빈 결과 상태, 결과 화면 진행바의 "완료" 단계 표현(입력·결과 모두 210/316로 동일 — 갱신 누락 추정, 4절 Q10) → **로딩은 CTA 비활성+인디케이터, 에러는 스낵바로 확정** (사람 결정).

#### `core:designsystem` 매핑

색상은 대부분 기존 토큰으로 커버됨: `#9c8af6`→`primary500`, `#7f73ea`→`primary600`, `#5757d7`→`primary700`, `#a99af6`→`primary400`, `#c1b4f6`→`primary300`, `#f5f3fe`→`primary50`, `#a8bbff`→`sky400`, `#171717`→`gray975`, `#303030`→`gray925`, `#737373`→`gray700`, `#8a8a8a`→`gray600`, `#b8b8b8`→`gray400`, `#f1f1f1`→`gray100`, `#f5f5f5`→`gray50`, `#fafafa`→`gray25`, `#e8e8e8`→`gray200`, `#505866`→`coolGray700`. 신규 필요: `#00010b`(결과 배경 → `TodakunColor.night`), `#e3e3e4`(핸들 → `gray200`로 근사), `#f7f7f8`(토스트 텍스트 → `white`/`gray25`로 근사).

컴포넌트 재사용 판정:

| Figma | 기존 컴포넌트 | 판정 |
|-------|---------------|------|
| SelectField | `TodakunSelectField` | ✅ 그대로 사용 — gray25/r12/h48/placeholder gray600/value gray975/`ic_circle_x_fill`/chevron 전부 일치 |
| 결과 chip2 | `TodakunChip2` | ✅ `primary700` + `body3Medium` + r100 — 일치 |
| 목적 칩 | `TodakunChip` | ⚠️ **버그 3건 발견, 수정 필요** (아래) |
| 성별 토글 | 없음 | ❌ **`GenderSelector` 신규.** Figma는 168.5x48 r8 + 그라데이션 2종(`#f3f6ff→#f5f5f5`, `#f5f5f5→#f5f3fe`), 텍스트 `#5c5c5c`(`gray800`) Medium 18 — 기존 컴포넌트와 형태가 다름 |
| 진행바 | `TodakunProgressBar` | ⚠️ 뒤로가기 아이콘이 `ic_arrow_back` 8x16/gray400인데 Figma는 chevron 20x20 — 파라미터화 필요 |
| 토스트 | `TodakunSnackbar` | 🔍 색/치수 대조 필요 (Figma: 236x36 r8 #000000) |
| 툴팁 | `TodakunTooltip`/`TodakunPopover` | 🔍 꼬리 달린 gray925 r99 형태와 대조 필요 |
| 바텀시트 | `WheelPickerBottomSheet` | ⚠️ `internal` + 휠 전용 — 캘린더용은 분리 필요 |
| **캘린더** | 없음 | ❌ **신규 개발** — 이 이슈 최대 공수. `feature:saju-contents`에 먼저 만들고(사용처 1개), 두 번째 사용처가 생기면 designsystem으로 승격 (사람 확정) |
| **별점(STAR)** | 없음 | ❌ 아이콘 에셋 + 소형 컴포저블 신규 |

**`TodakunChip` 버그** (`core/designsystem/.../component/TodakunChip.kt`) — 현재 사용처가 0곳이라 **이 이슈에서 바로 고친다** (사람 확정):
```kotlin
// 현재 → 수정
style = if (selected) typography.body2Medium else typography.body2SemiBold  // 굵기가 뒤바뀜 → 선택=SemiBold, 비선택=Medium
color = if (selected) ... else colors.gray25   // → gray50 (#f5f5f5)
color = if (selected) ... else colors.gray700  // → coolGray700 (#505866)
```

필요한 신규 drawable: `ic_star_fill.xml`(규칙 40 `ic_` 접두사), `ic_share.xml`/`ic_event_export.xml`(아이콘 배치 확정에 따라 필요), 결과 화면 배경 `img_date_fortune_bg`(`img_` 접두사) — **아이콘·이미지 전부 사람이 Figma에서 export해 전달해야 한다** (에이전트가 노드 JSON만으로는 이미지 에셋을 추출할 수 없음, 4절 N5).

#### 운세 카테고리 한국어 라벨 (⚠️ 임시값 — 기획 확정 전)

| enum | 라벨 | 근거 |
|------|------|------|
| `MONEY` | 재물운 | Figma `2926:29057`에 실재 |
| `RELATIONSHIP` | 관계운 | Figma는 "화합운" — 임시 |
| `HEALTH` | 건강운 | Figma는 "사고 예방" — 임시 |
| `LOVE` | 애정운 | Figma에 없음 — 임시 |
| `ACHIEVEMENT` | 성취운 | Figma에 없음 — 임시 |

전부 `strings.xml` 리소스로 뺀다. 기획이 확정되면 문자열만 교체하면 되고 코드는 손대지 않는다.

### 2-2. 핵심 설계 결정

**D1. Repository 1 · UseCase 1 · ViewModel 1.** 엔드포인트가 하나뿐이고 조합할 다른 관심사가 없다. nav3 그래프가 없어 화면 간 배관 비용이 크므로 `step`을 상태로 든 단일 위저드 ViewModel이 가장 적은 코드로 동작한다. 결과 화면까지 같은 ViewModel이 담당한다.

**D2. `FortuneCategory`는 처음부터 공용 위치에.** `RELATIONSHIP/LOVE/ACHIEVEMENT/MONEY/HEALTH`는 Swagger에서 `DayFortune`·`LuckAction`·`DailyFortune` 3곳이 동일하게 쓴다 → `core/domain/model/fortune/FortuneCategory.kt` 단독 배치 (규칙 00 "공유 필요 시 domain으로 승격").

**D3. DTO는 날짜·enum을 `String` raw로, 도메인은 `LocalDate`·enum.** 규칙 20 "DTO는 Raw 유지". kotlinx-serialization이 `java.time.LocalDate`를 기본 지원하지 않아 커스텀 serializer가 필요한데 Raw 원칙에 어긋난다. Mapper에서 `LocalDate.parse()`. `core:domain`은 순수 `java-library`(JVM) + minSdk 26이라 desugaring 없이 `java.time` 사용 가능.

**D4. Mapper는 Response 파일 안 확장 함수.** `RemoteAuthDataSourceImpl`이 `dto.auth.toDomain`을 import하는 기존 선례와 동일. 파일 4개를 아낀다.

**D5. 서버가 모르는 enum 값에 죽지 않기.** DTO는 `String`으로 받고 Mapper에서 이름 매칭. `purpose` 미매칭은 예외(→`Result.failure`), `fortuneCategories`의 미지 항목은 필터링해서 버린다.

**D6. 캘린더는 `core:designsystem`이 아니라 `feature:saju-contents`에 먼저 만든다.** 사용처가 하나뿐인 지금 designsystem으로 올리면 "쓰는 곳 하나짜리 공용 컴포넌트"가 된다. 다중 선택·과거 비활성·오늘 표시는 처음부터 파라미터로 열어 둬 승격 비용을 낮춘다.

**D7. 후보 날짜 상한은 상수 1곳(`DateFortuneDefaults.MAX_TARGET_DATES = 5`)에서만 정의한다.** UseCase 검증·캘린더 시트·안내 문구·토스트 문구가 전부 이 상수를 참조한다.

**D8. 결과 화면은 다크 테마가 아니라 "어두운 배경 위의 화면"으로 취급한다.** `TodakunTheme`이 `content()` 통과 함수라 다크/라이트 전환 개념이 없다. `#ffffff@10%` 같은 값은 `TodakunColor.white.copy(alpha = 0.1f)`로 표현하고, `#00010b`만 신규 토큰(`TodakunColor.night`)으로 추가한다.

**D9. 카테고리 별점은 개수를 고정하지 않는다.** Figma는 3칸×별3개 고정 레이아웃이지만, 그건 목업 데이터의 모양이지 계약이 아니다. 계약은 Swagger이고 카테고리 5종에 `star: int32`(상한 미문서화)다. → **칸 수 = `categoryStars.size`, 별 개수 = `star` 값. 상한 상수를 두지 않는다.** 상한을 3으로 박으면 서버가 4를 보내는 순간 조용히 잘리고, 5로 박으면 3을 보낼 때 빈 별이 의미 없이 남는다. "빈 별"은 그리지 않는다 — 총점 개념이 없어 분모가 없다. 레이아웃은 Figma의 3칸/gap28을 5칸에 그대로 쓰면 라벨("사고 예방" 5자)이 깨지므로 **`FlowRow`로 감싸 자동 줄바꿈**(칸 최소폭 80dp, gap 12dp) — 3개면 한 줄, 5개면 자연스럽게 3+2 두 줄이 되어 별도 분기가 필요 없다.

**D10. 성별 UI와 성별 전송을 분리한다.** 성별은 실제 기능으로 확정됐지만(Figma 2개 노드에 실재) 서버 Request에 필드가 없다. 둘을 한 커밋에 묶으면 백엔드 대기 때문에 UI까지 통째로 멈춘다. → **State·UI는 지금 만들고, UseCase 시그니처는 건드리지 않는다.** `selectedGender`는 ViewModel 상태에만 존재하고 `CreateDayFortunesUseCase(purpose, targetDates)` 호출에는 들어가지 않는다. 백엔드 대응 후 변경점은 정확히 3줄: `CreateDayFortuneRequest`에 `gender` 추가 / DataSource 파라미터 추가 / UseCase 파라미터 추가. `Gender` enum은 지금 `core:domain`에 만든다(`model/user/Gender.kt`) — UI가 `String`을 들고 있다가 나중에 enum으로 바꾸는 것보다 처음부터 타입을 잡는 편이 나중 diff를 줄인다.

### 2-3. 기존 코드베이스 실사

| 항목 | 실제 상태 | 영향 |
|------|-----------|------|
| `feature/saju-contents` | `build.gradle.kts` + 빈 매니페스트만. 소스 0개 | 패키지 구조를 이번에 정한다 |
| `todakun.feature` 컨벤션 플러그인 | Compose·Hilt·KSP·Orbit(core/viewmodel/compose)·`orbit-test`·`hilt-navigation-compose`·`core:domain`/`navigation`/`designsystem`·`kotlinx-collections-immutable` 전부 포함 | `build.gradle.kts` 수정 불필요 |
| `feature/auth` | presentation 없음. OAuth 로직만 | Compose+Orbit 선례가 없다 — 이번이 최초 |
| `core:navigation` | `src/main` Kotlin 소스 0개, nav3 의존성도 없음 | 라우트 인프라는 별도 이슈 |
| `MainActivity` | `Greeting("Android")` 스텁 | 이번 이슈에서 임시 직배선 |
| `core:designsystem` | 토큰 완비. 컴포넌트 다수 존재하나 캘린더 없음 | 2-1절 |
| `TodakunTheme` | `content()` 통과 함수 | `MaterialTheme.*` 쓰지 말 것 |
| Ktor Bearer | `installBearerAuth` 전역 적용 | 인증 작업 0 |
| ktlint | 루트에서 전 서브프로젝트 + `nlopez-compose-rules` 적용 중 | CLAUDE.md 6절 "미설정 TODO"는 이미 해소됨 |

### 2-4. 실행 순서와 의존성

```
Part A (domain+data)  ─────┐   gender 없이 진행 (B-2)
   ↑ 즉시 착수 가능          │
                           ├──▶ Part B1 (입력 + 캘린더 시트 + GenderSelector)
                           │        · MAX_TARGET_DATES = 5 (확정)
                           │        · selectedGender 는 State 까지만, UseCase 로 전달 X
                           │
                           └──▶ Part B2 (결과 화면)
                                    · 카테고리 N칸 동적 (확정)
                                    · ⛔ 배경 이미지 · 별 아이콘 export 필요 (4절 N5)
                                        │
                        ┌───────────────┴───────────────┐
                        ▼                               ▼
              Part C (MainActivity 임시 배선)   Part D ⛔ gender 전송
                                                 ↑ 블로커 B-2 해소 후
                                                 ↑ DTO/DataSource/UseCase 3줄
```
Part A는 지금 착수 가능, B1/B2와 병렬. B1과 B2는 같은 ViewModel을 쓰지만 화면 파일이 분리돼 병렬 가능. Part D(성별 전송)는 이 이슈에서 하지 않는다 — 백엔드가 필드를 추가한 뒤 후속 커밋/이슈로 처리한다.

### 2-5. 트레이드오프

| 고민 | 선택 | 버린 것 |
|------|------|---------|
| 날짜 상한 3 vs 5 | **5 확정** (+ 상수 1곳) | Figma 내부 불일치가 있었으나 Swagger+다수 노드 근거로 5 채택. 3이 맞다면 상수 1줄 수정으로 끝남 |
| 카테고리 칸 수 고정 vs 동적 | **동적**(`categoryStars.size`) | Figma 픽셀 재현. 3칸 고정은 서버가 4~5개 보내면 조용히 잘린다 |
| 별 최대 개수 상수 vs 응답 그대로 | **응답 그대로** | "★★★☆☆" 식 총점 표현. Swagger에 상한이 없어 분모를 만들 근거가 없다 |
| 성별: UI+전송 한 번에 vs UI만 먼저 | **UI만 먼저** | 기능 완결성. 묶으면 백엔드 대기로 UI까지 멈춘다. 나중 diff는 3줄 |
| `Gender` enum 위치: feature vs domain | **domain** | 곧 UseCase 파라미터가 되는 게 확정이라 feature→domain 이동 비용이 지금 작성 비용보다 크다 |
| 화면별 ViewModel 3개 vs 1개 | **1개** | 단계별 격리. nav3 배관이 없는 지금은 비용만 크다 |
| 캘린더 위치: designsystem vs feature | **feature 먼저** | 선제적 공용화. 사용처 1개짜리 공용 컴포넌트를 피했다 |
| `TodakunChip` 수정 지금 vs 나중 | **지금** | 범위 최소화. 현재 사용처가 0이라 지금이 가장 싸다 |
| 결과 화면 다크: 테마 확장 vs 로컬 처리 | **로컬 처리** | `TodakunTheme`이 빈 껍데기라 테마 확장은 이 이슈 범위를 넘는다 |
| 모듈: saju-contents vs 신규 feature | **saju-contents** | #105가 이미 "사주 콘텐츠 묶음"으로 만들어 뒀다 |

### 2-6. 데이터 흐름

```
[Compose] DateFortuneInputScreen
    │  onPurposeSelect / onGenderSelect / onOpenDateSheet / onDateToggle / onDateRemove / onReset / onSubmit
    ▼
[Orbit] DateFortuneViewModel : ContainerHost<DateFortuneState, DateFortuneSideEffect> by container(...)
    ▼
[UseCase] CreateDayFortunesUseCase(purpose, targetDates) : Result<List<DayFortune>>
    │      └ 1..MAX_TARGET_DATES 개수 / 과거 날짜 사전 검증 (서버 왕복 전)
    ▼
[Repository] DayFortuneRepository.createDayFortunes(...) : Result<List<DayFortune>>
    │  impl: runCatchingCancellable { ... }          ← 규칙 20
    ▼
[DataSource] RemoteDayFortuneDataSource.postDayFortunes(...) : List<DayFortune>
    │  impl: client.post("api/v1/day-fortunes") { setBody(CreateDayFortuneRequest(...)) }
    │        .bodyNotNull<List<DayFortuneResponse>>().map { it.toDomain() }
    │  ※ try-catch 하지 않고 그대로 throw (규칙 20)
    ▼
[Ktor] POST /api/v1/day-fortunes   (Bearer 자동 첨부 + 401 자동 refresh)
```
반환 경로는 역순. 예외는 `DayFortuneRepositoryImpl`의 `runCatchingCancellable`에서 한 번만 `Result.failure`로 봉인되고 `CancellationException`은 통과한다. 성공 시 ViewModel이 `results: ImmutableList<DayFortune>` + `selectedResultIndex = 0`으로 reduce하고 `SideEffect.NavigateToResult`를 낸다. `selectedGender`는 State에만 존재하고 UseCase 호출 인자에 들어가지 않는다(D10).

---

## 3. 파일 변경 계획 (구현 체크리스트)

### 3-1. Part A — domain + data (신규 10 / 수정 3 = 13)

> **gender 필드는 넣지 않는다** (블로커 B-2). `Gender` enum만 미리 만들어 둔다(D10).

```
core/
├─ domain/
│  ├─ build.gradle.kts                                 [수정] testImplementation junit + coroutines-test
│  └─ src/main/java/com/kikidan/domain/
│     ├─ model/user/Gender.kt                           [신규] enum { MALE, FEMALE }  ※B1 UI 전용, 아직 전송 X
│     ├─ model/fortune/FortuneCategory.kt               [신규] enum 5종 (공용)
│     ├─ model/dayfortune/DayFortune.kt                 [신규] DayFortune + DayFortunePurpose + FortuneCategoryStar
│     ├─ repository/DayFortuneRepository.kt             [신규] interface
│     └─ usecase/CreateDayFortunesUseCase.kt            [신규] + DateFortuneDefaults.MAX_TARGET_DATES = 5
├─ data/src/main/java/com/kikidan/data/
│  ├─ datasource/RemoteDayFortuneDataSource.kt          [신규] interface
│  ├─ repository/DayFortuneRepositoryImpl.kt            [신규]
│  └─ di/RepositoryModule.kt                            [수정] @Binds 1줄
└─ data-remote/src/main/java/com/kikidan/data_remote/
   ├─ dto/dayfortune/CreateDayFortuneRequest.kt         [신규]
   ├─ dto/dayfortune/DayFortuneResponse.kt              [신규] Response + toDomain()
   ├─ datasource/RemoteDayFortuneDataSourceImpl.kt      [신규]
   └─ di/RemoteDataSourceModule.kt                      [수정] @Binds 1줄
```

- [ ] `Gender.kt` — `enum class Gender { MALE, FEMALE }`. UseCase/Repository/DTO 어디에도 아직 넣지 않는다 (B-2)
- [ ] `FortuneCategory.kt` — `enum class FortuneCategory { RELATIONSHIP, LOVE, ACHIEVEMENT, MONEY, HEALTH }`
- [ ] `DayFortune.kt` — `data class DayFortune(val id: String, val purpose: DayFortunePurpose, val targetDate: LocalDate, val score: Int, val title: String, val content: String, val categoryStars: List<FortuneCategoryStar>)`, `enum class DayFortunePurpose { CONTRACT_MOVING, BUSINESS_OPENING, TRAVEL, CONFESSION_DATING, EXAM_INTERVIEW }`, `data class FortuneCategoryStar(val category: FortuneCategory, val star: Int)` — 전 필드 `val` (규칙 10, `var`는 P1)
- [ ] `DayFortuneRepository.kt` — `suspend fun createDayFortunes(purpose: DayFortunePurpose, targetDates: List<LocalDate>): Result<List<DayFortune>>` (규칙 10: 반환 `Result<T>`, 위반 P1)
- [ ] `CreateDayFortunesUseCase.kt` — 동사 시작(규칙 10). `operator fun invoke(purpose, targetDates)`. 개수 1..MAX / 과거 날짜 검증을 여기서. `object DateFortuneDefaults { const val MAX_TARGET_DATES = 5 }` 동거(D7). **gender 파라미터 없음**(B-2)
- [ ] `RemoteDayFortuneDataSource.kt` — `suspend fun postDayFortunes(...): List<DayFortune>` (Domain Model 반환 — 규칙 20)
- [ ] `DayFortuneRepositoryImpl.kt` — `runCatchingCancellable { ... }` (규칙 20, try-catch 누락 P1)
- [ ] `RepositoryModule.kt` **[수정]** — `@Binds @Singleton bindDayFortuneRepository`
- [ ] `CreateDayFortuneRequest.kt` — `@Serializable data class CreateDayFortuneRequest(val purpose: String, val targetDates: List<String>)`
- [ ] `DayFortuneResponse.kt` — 전 필드 raw `String`/`Int` + `FortuneCategoryStarResponse` + `toDomain()` (LocalDate.parse, enum 이름 매칭, D5)
- [ ] `RemoteDayFortuneDataSourceImpl.kt` — `companion object { private const val DAY_FORTUNES_URL = "api/v1/day-fortunes" }`. try-catch 금지 (규칙 20, P1)
- [ ] `RemoteDataSourceModule.kt` **[수정]** — `@Binds @Singleton bindDayFortuneRemoteDataSource`
- [ ] `core/domain/build.gradle.kts` **[수정]** — 테스트 의존성 추가

### 3-2. Part B1 — 입력 화면 + 캘린더 바텀시트 (신규 9 / 수정 2 = 11)

```
core/designsystem/src/main/
├─ java/com/kikidan/designsystem/
│  ├─ component/TodakunChip.kt                          [수정] 2-1절 버그 3건
│  └─ theme/TodakunColor.kt                              [수정] night = Color(0xFF00010B)  ※B2와 공유
feature/saju-contents/src/main/
├─ java/com/kikidan/sajucontents/dayfortune/
│  ├─ DateFortuneContract.kt                            [신규] State / SideEffect
│  ├─ DateFortuneViewModel.kt                           [신규] ContainerHost by container(...)
│  ├─ DateFortuneInputScreen.kt                         [신규] Route + stateless Screen + @Preview
│  └─ component/
│     ├─ PurposeChipGroup.kt                            [신규] FlowRow + TodakunChip
│     ├─ GenderSelector.kt                              [신규] 남성/여성 토글 (D10 / B-2)
│     ├─ DateSelectBottomSheet.kt                       [신규] ModalBottomSheet + 헤더/버튼/칩행
│     ├─ MultiSelectCalendar.kt                         [신규] 월 리스트 + 요일행 + 날짜셀
│     └─ SelectedDateChipRow.kt                         [신규] r99 stroke 칩 + 삭제
└─ res/values/strings.xml                               [신규] 화면 문구
```

- [ ] `DateFortuneContract.kt` — `data class DateFortuneState(val selectedPurpose: DayFortunePurpose?, val selectedGender: Gender?, val selectedDates: ImmutableList<LocalDate>, val isSheetVisible: Boolean, val isLoading: Boolean, val results: ImmutableList<DayFortune>, val selectedResultIndex: Int)` — 컬렉션은 `ImmutableList`(규칙 40, P2). `sealed interface DateFortuneSideEffect { data class ShowToast(val messageRes: Int); data class ShowError(val messageRes: Int); data object NavigateToResult; data object NavigateBack }`. 파생값 `val canSubmit get() = selectedPurpose != null && selectedDates.isNotEmpty() && !isLoading`. ⚠️ `selectedGender`는 `canSubmit` 조건에 넣지 않는다 — 서버로 안 보내는 값을 필수로 걸면 사용자가 이유 없이 막힌다(B-2)
- [ ] `DateFortuneViewModel.kt` — `@HiltViewModel class DateFortuneViewModel @Inject constructor(private val createDayFortunes: CreateDayFortunesUseCase) : ViewModel(), ContainerHost<...> by container(...)` — `by container(...)` 형태(규칙 30). 도메인 로직 직접 수행 금지(규칙 30, P2). 날짜 토글 시 5개 초과면 `ShowToast`(상태 변경 없음). 제출 시 `createDayFortunes(purpose, dates)` — **`selectedGender`를 넘기지 않는다**(B-2), `// ponytail: gender는 서버 Request 필드 추가 후 전달 (블로커 B-2)` 주석 1줄. 실패 시 `isLoading=false` + `ShowError`
- [ ] `DateFortuneInputScreen.kt` — Route + stateless Screen + `@Preview`. 제출 중 CTA 비활성 + 인디케이터
- [ ] `PurposeChipGroup.kt` — `FlowRow` + `TodakunChip`, 2줄 배치
- [ ] `GenderSelector.kt` — `Row` 2등분, 각 168.5x48 `r8`. 배경 그라데이션 2종(`Brush.horizontalGradient`): 좌 `sky50→gray50`, 우 `gray50→primary50`. 텍스트 `gray800` Medium 18. ⚠️ Figma에 "선택됨" 상태 시안이 없다 — 선택 시 `primary500` 배경 + `white` 텍스트로 목적 칩과 동일 규칙 적용 (4절 Q12)
- [ ] `DateSelectBottomSheet.kt` — 핸들/타이틀/"5개까지 날짜 선택 가능해요"(상수 주입)/닫기/[초기화][선택완료]
- [ ] `MultiSelectCalendar.kt` — `LazyColumn` 월 섹션 반복. 셀 상태 4종(기본/오늘/선택/과거비활성)
- [ ] `SelectedDateChipRow.kt` — 날짜 1개당 칩 1개(D7). `M.d(E)` 포맷
- [ ] `strings.xml` — 목적 5종, 성별 2종, 카테고리 5종(2-1절 임시값), 안내·토스트·에러 문구. 개수 문구는 `%d` 플레이스홀더 필수
- [ ] `TodakunChip.kt` **[수정]**, `TodakunColor.kt` **[수정]**

### 3-3. Part B2 — 결과 화면 (신규 7 / 수정 0 = 7 + 에셋 3)

> 카테고리 칸 수는 `categoryStars.size`, 별 개수는 `star` 값. 고정 상수 없음(D9).

```
core/designsystem/src/main/res/drawable/
├─ ic_star_fill.xml                                     [신규·에셋] 벡터 (규칙 40 ic_ 접두사)
├─ ic_share.xml                                         [신규·에셋] 벡터
└─ ic_event_export.xml                                  [신규·에셋] 벡터
feature/saju-contents/src/main/
├─ java/com/kikidan/sajucontents/dayfortune/
│  ├─ DateFortuneResultScreen.kt                        [신규] Route + stateless Screen + @Preview
│  └─ component/
│     ├─ FortuneScoreCard.kt                            [신규] 원형 점수 + 제목 + 카테고리 영역
│     ├─ FortuneCategoryStars.kt                        [신규] FlowRow, N칸 동적
│     ├─ CategoryStarItem.kt                            [신규] 라벨 + 별 N개 (칸 1개)
│     └─ ResultDateTabRow.kt                            [신규] SelectBox N개 상태
└─ res/drawable/img_date_fortune_bg.webp                [신규·에셋] Figma export 필요
```

- [ ] `DateFortuneResultScreen.kt` — 배경 `TodakunColor.night` + 배경 이미지(`alpha=0.5f`), `verticalScroll`. 헤더 우측 `ic_event_export`+`ic_share`는 `onClick={}` no-op. CTA도 콜백만 노출(채팅 미구현)
- [ ] `FortuneScoreCard.kt` — 카드 `white.copy(alpha=0.1f)` r16, 원 `primary400` 180dp, 점수 Bold 40 + "운세 점수" `white@60%`
- [ ] `FortuneCategoryStars.kt` — `FlowRow`(`spacedBy(12.dp)` 가로/세로), 아이템 `widthIn(min=80.dp)`. `categoryStars.forEach { CategoryStarItem(it) }` — 개수 분기 없음. 빈 리스트면 영역 자체를 그리지 않는다
- [ ] `CategoryStarItem.kt` — 라벨(`stringResource`) `white@60%` Regular 12 + `repeat(star) { Icon(ic_star_fill, tint=primary400) }`. 빈 별(`ic_star_line`)은 만들지 않는다 — 총점 분모가 없다(D9). `star<=0`이면 별 영역 생략
- [ ] `ResultDateTabRow.kt` — 선택 `primary600`+`primary500` 테두리 / 비선택 `white@5%`+`white@30%`. 탭 개수는 `results.size`에 따라 동적(1~5개)
- [ ] 배경 이미지·별/공유/내보내기 아이콘 export — **사람 작업 필요** (4절 N5)

### 3-4. Part C — app 배선

- [ ] `app/build.gradle.kts` **[수정]** — `implementation(projects.feature.sajuContents)` (현재 `feature.auth`만)
- [ ] `MainActivity.kt` **[수정]** — `Greeting("Android")` 대신 `DateFortuneInputRoute` 임시 직배선. 정식 nav3 인프라는 별도 이슈

### 3-5. 테스트 계획

**Part A**
> ⚠️ `core/domain/build.gradle.kts`에 테스트 의존성이 없다. `testImplementation(libs.junit)` + `libs.kotlinx.coroutines.test` 선행.

`CreateDayFortunesUseCaseTest` (Fake Repository)
- [ ] 날짜 0개 → `failure`, Repository 미호출
- [ ] 날짜 `MAX+1`개(6개) → `failure`, Repository 미호출
- [ ] 과거 날짜 포함 → `failure`, Repository 미호출
- [ ] 오늘 날짜 → 통과 (경계값: "과거 불가"이지 "미래만"이 아니다)
- [ ] 날짜 정확히 5개 → 통과 (상한 경계)
- [ ] 유효 입력 → Repository 반환값 그대로 전달
- [ ] Repository 실패 → 실패 전파

`DayFortuneRepositoryImplTest` (`FakeRemoteDayFortuneDataSource`, 기존 `core/data/src/test/.../fake/` 패턴)
- [ ] DataSource 정상 → `success`, 값 동일
- [ ] DataSource `IOException` throw → `failure`, 예외가 밖으로 새지 않음
- [ ] DataSource `CancellationException` throw → `Result`로 감싸지 않고 전파

`RemoteDayFortuneDataSourceImplTest` (`ktor-client-mock`, 기존 `RemoteAuthDataSourceImplTest` 패턴)
- [ ] 경로 `api/v1/day-fortunes`, 메서드 `POST`
- [ ] body의 `purpose`가 enum name 문자열, `targetDates`가 `yyyy-MM-dd` 배열
- [ ] 정상 응답 → `targetDate`가 `LocalDate`, `fortuneCategory`가 enum으로 매핑
- [ ] `data`가 `null` → `bodyNotNull`이 `IllegalArgumentException` throw
- [ ] 알 수 없는 `fortuneCategory` 포함 → 해당 항목만 제외, 나머지 정상 (D5)
- [ ] 알 수 없는 `purpose` → 예외 throw (D5)
- [ ] 5xx → 예외를 삼키지 않고 throw

**Part B** — `DateFortuneViewModelTest` (`orbit-test`)
- [ ] 목적 선택 → `selectedPurpose` 변경
- [ ] 날짜 토글(추가/제거) → `selectedDates` 반영
- [ ] 5개 선택 상태에서 추가 시도 → 상태 불변 + `ShowToast`
- [ ] 초기화 → `selectedDates` 비고 CTA 비활성
- [ ] 목적 미선택 또는 날짜 0개 → `canSubmit == false`
- [ ] 성별 선택 → `selectedGender` 변경
- [ ] 성별 미선택이어도 `canSubmit == true` (목적·날짜만 충족하면) — B-2 회귀 방지용
- [ ] 제출 시 UseCase에 전달된 인자가 `(purpose, dates)` 2개뿐 — gender가 새어 들어가지 않음을 고정 (B-2 해소 시 이 테스트를 의도적으로 깨고 갱신)
- [ ] 제출 → `isLoading=true` → 성공 시 `false` + `results` 반영 + `NavigateToResult`
- [ ] 제출 중 `canSubmit == false` (중복 제출 방지)
- [ ] UseCase 실패 → `isLoading=false` + `ShowError`, 크래시 없음
- [ ] 결과 탭 전환 → `selectedResultIndex` 변경

Compose UI 계측 테스트는 이 이슈에서 필수로 두지 않는다(`@Preview` + 유닛 테스트로 충분). 단 캘린더 셀 상태 판정(과거/오늘/선택)은 순수 함수로 분리해 유닛 테스트를 붙인다.
- [ ] `CalendarDayState` 판정 순수 함수 테스트: 과거 / 오늘 / 미래·미선택 / 미래·선택

**검증 명령**
```bash
./gradlew :core:domain:test :core:data:test :core:data-remote:test   # Part A
./gradlew :feature:saju-contents:test                                # Part B
./gradlew ktlintCheck
```

---

## 4. 리스크 / 미해결 질문 (사람 확인 필요)

### 🔴 남은 블로커

- [ ] **B-2. 성별(gender) 서버 전송.** Figma 2개 노드(`1086:24067`, `1100:7253`)에 성별 선택 UI가 실제로 존재하는 것으로 확정됐으나, 현재 Swagger `CreateDaySelectionFortuneRequest`에는 gender 필드가 없다. **백엔드가 필드를 추가해야 실제 전송이 가능하다.** 이번 이슈에서는 `GenderSelector` UI와 State만 만들고, 값 전송은 백엔드 대응 후 후속 커밋으로 미룬다 (2-2절 D10, 3-1/3-2절 반영 완료).

### ✅ 결정 완료 (사람 확인 완료)

| # | 쟁점 | 결정 |
|---|------|------|
| D-1 | 후보 날짜 상한 3 vs 5 | **5.** Swagger `maxItems=5` + Figma 유효성검사 토스트 등 6개 노드 근거 |
| D-2 | 결과 카테고리 3칸 vs API 5종 | **API 응답 개수(최대 5개) 그대로 렌더링.** 한국어 라벨 5종은 임시값(2-1절), 기획 확정 시 `strings.xml`만 교체 |
| D-3 | "남성/여성" 칩 | **실제 기능.** 서버 전송은 블로커 B-2 해소 후 |
| D-4 | 무료/유료 게이팅 | **게이팅 없음, 전체 무료** |
| D-5 | 로딩/에러 상태 | **CTA 비활성+인디케이터(로딩) / 스낵바(에러)** |
| Q1 | 앱 네비게이션 범위 | **화면만 구현 + `MainActivity` 임시 직배선.** 정식 nav3 인프라는 별도 이슈 |
| Q7 | 공유/캘린더 내보내기 아이콘 | **아이콘만 배치, 콜백 no-op** |
| Q8 | `TodakunChip` 버그 수정 시점 | **이 이슈에서 즉시 수정** |
| Q9 | 캘린더 위치 | **`feature:saju-contents`에 먼저 구현**, 2번째 사용처 등장 시 designsystem 승격 검토 |

### 🟠 열려 있는 질문 (구현에 큰 영향 없음, 진행하며 확인)

- [ ] **Q10. 진행바 단계 수 불명.** 입력·결과 화면 모두 진행률 210/316(≈66%)로 동일해 몇 단계 중 몇 번째인지 불명확하다. → 구현은 하드코딩하지 말고 `progress: Float` 파라미터로 받게 해서 나중에 값이 바뀌어도 컴포넌트 시그니처가 안 바뀌게 한다.
- [ ] **Q11. 카테고리 5종 한국어 라벨 최종본.** 2-1절 임시값(`관계운/애정운/성취운/재물운/건강운`)은 기획 확정 전 추정치다. 확정되면 `strings.xml`만 교체.
- [ ] **Q12. 성별 선택(GenderSelector) 활성 상태 색상.** Figma에 "선택됨" 상태 시안이 없다. 목적 칩과 동일하게 `primary500`+`white`로 잠정 처리했다(3-2절). 실제 시안이 나오면 교체.

### 🟡 참고 사항 (차단 아님)

- **N1.** CLAUDE.md 6절 "린트 미설정 TODO"는 이미 해소됨 — 루트 `build.gradle.kts`가 전 서브프로젝트에 ktlint + `nlopez-compose-rules` 적용 중. 문서 갱신 권장.
- **N2.** `core/domain`에 테스트 의존성이 없다 — UseCase 테스트 전에 추가 필요 (3-1절에 포함됨).
- **N3.** `TodakunTheme`이 `content()` 통과 함수다. `MaterialTheme.colorScheme` 계열을 쓰면 기본값이 나온다. 반드시 `TodakunColor.*`/`TodakunTypography.*` 직접 참조.
- **N4.** `feature:saju-contents`가 `core:data`에 직접 의존하면 **P1**. 컨벤션 플러그인이 `core:domain`만 주입하므로 자연히 지켜지지만, 구현 중 이 경계를 넘고 싶어지면 설계가 틀렸다는 신호다.
- **N5.** 결과 화면 배경 이미지와 별/공유/내보내기 아이콘은 에이전트가 노드 JSON으로 가져올 수 없다. 사람이 Figma에서 export해 넣어줘야 Part B2를 끝낼 수 있다.
- **N6.** 캘린더 바텀시트 노드의 `SubText` 슬롯에 Lorem ipsum이 들어 있음 — 미사용 슬롯으로 판단해 무시.
- **N7.** 노드 `1099:6279`에 빨간 테두리 사각형(디자이너 주석용 마커로 추정)이 있음 — 무시.
- **N8.** 이슈 본문이 사실상 빈 템플릿이었다. 승인 시 GitHub 이슈의 "할 일" 체크리스트를 3절 기준으로 갱신 권장.

---

## 다음 단계

1. **에이전트**: Part A 구현 (`oh-my-claudecode:executor`, Sonnet) + 유닛 테스트 — 지금 착수 가능
2. **에이전트**: Part B1/B2 구현 (`oh-my-claudecode:designer`, Sonnet) — Part A와 병렬 가능. 배경 이미지·별 아이콘(N5)이 없으면 Part B2 최종 마무리가 막히니 그 전까지는 플레이스홀더로 진행
3. **사람**: 배경 이미지·별/공유/내보내기 아이콘 Figma export, Q10~Q12 확정
4. **에이전트**: 검토 (Opus) — `./gradlew test` + `./gradlew ktlintCheck` + P1 판정
