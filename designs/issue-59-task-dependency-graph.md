# 작업 의존성 그래프 — 이슈 #59 하위 9개 단위

> 이 문서는 이슈 #59의 9개 작업 단위(A/B/C1/C2/D/F-chat/G-chat/F-history/G-history)의 **실행 의존성 단일 소스**다.
> 각 단위 설계 문서는 이 파일을 참조하고, 자체적으로 그래프를 다시 그리지 않는다(중복 작성 시 어긋남이 생긴다 — 아래 "발견된 불일치" 참고).

## 범례

- `──▶` **컴파일 의존**: 화살표 대상 유닛의 코드가 화살표 출발 유닛의 타입/함수를 소스에서 직접 import한다. 이게 없으면 컴파일 자체가 안 된다.
- `┄┄▷` **런타임/DI 의존**: 컴파일은 독립적이지만, Hilt 바인딩이 없으면 **앱 실행 시 크래시**하거나 기능이 동작하지 않는다.

## ASCII 그래프

```
[A] core:domain — chat 모델 + ChatRepository 인터페이스
 │
 ├──▶ [B] core:domain — chat UseCase 5종
 │
 ├──▶ [D] core:data / core:data-remote — DataSource + RepositoryImpl + Mapper ◀── [C1] core:data-remote — SSE 클라이언트 기반
 │      (C1, C2도 필요)                                                       ◀── [C2] core:data-remote — chat DTO
 │      ※ D는 B를 호출하지 않는다(UseCase→Repository 단방향). B 없이도 컴파일된다.
 │
 └──▶ [F-chat] feature:chat — ChatViewModel(Orbit)  ◀──[B]
        │  ┄┄▷ [D] (Hilt 바인딩 없으면 앱이 DI 그래프 생성 시점에 크래시)
        ▼
      [G-chat] feature:chat — ChatScreen + Navigation 3 최소 골격
        │
        ├──▶ [F-history] feature:chat — HistoryViewModel(Orbit)  ◀──[B] (GetConversationsUseCase/DeleteConversationUseCase 직접 주입)
        │      │  ┄┄▷ [D]
        │      ▼
        └──▶ [G-history] feature:chat — HistoryScreen + 히스토리 라우트
               (G-chat의 ChatKey ◀──▶ ChatHistoryKey 양방향 이동)
```

## 단위별 의존성 표

| 유닛 | 컴파일 의존 | 런타임/DI 의존 | 비고 |
|------|-------------|----------------|------|
| A | 없음 | 없음 | 최상위. 순수 선언 |
| B | A | 없음 | `ChatRepository` 인터페이스만 참조, 구현체는 몰라도 됨 |
| C1 | 없음 | 없음 | 채팅 도메인 지식 없는 범용 SSE 인프라. A/B와 완전 병렬 |
| C2 | 없음 | 없음 | 서버 스키마 그대로의 DTO. 도메인 매핑은 D가 담당 |
| D | A, C1, C2 | 없음 (자기 자신이 구현체) | **B는 컴파일 의존이 아니다** — UseCase가 Repository를 호출하는 방향이라 역방향 참조가 생기지 않는다 |
| F-chat | A, B | D | `ChatViewModel`이 `GetChatEntryUseCase`/`GetConversationDetailUseCase`/`SendChatMessageUseCase`를 생성자로 직접 주입 |
| G-chat | F-chat | D (F-chat 경유) | `ChatState`/`ChatViewModel` 필요 |
| F-history | A, B, F-chat(모듈 스캐폴딩 재사용) | D | `HistoryViewModel`이 `GetConversationsUseCase`/`DeleteConversationUseCase`를 직접 주입 — **G-chat 자체는 필요 없음**(같은 모듈에 있을 뿐, `ChatState`/`ChatViewModel`을 참조하지 않는다) |
| G-history | F-history, G-chat | D (F-history 경유) | G-chat의 `ChatKey` 라우트로 이동해야 하므로 G-chat 필요 |

## 권장 머지 순서 (위상 정렬)

```
A → { B, C1, C2 (병렬) } → D → F-chat → G-chat → F-history → G-history
```

자유도가 있는 지점은 단 하나: **B와 D의 순서**. D는 B에 컴파일 의존이 없으므로 이론상 바꿔도 되지만, 위 순서(B를 D보다 먼저)를 권장한다 — F-chat이 A/B/D 셋 다 필요하므로 어차피 B가 F-chat보다 먼저 있어야 하고, 병렬로 진행 중인 B를 D보다 일찍 끝내 두면 대기가 줄어든다.

## 발견된 불일치 (이 문서를 만들게 된 배경)

각 단위 문서가 자체적으로 "선행" 줄과 그래프를 그렸을 때, **F-history 문서가 실제로는 B(`GetConversationsUseCase`/`DeleteConversationUseCase`를 생성자에서 직접 주입)에 컴파일 의존이 있음에도 "선행" 줄에 B를 적지 않은 누락**이 있었다. 그래프를 문서마다 중복해서 손으로 그리면 이런 어긋남이 반복될 수 있어, 이후 개별 문서는 이 파일을 참조하고 자체 그래프 재작성을 하지 않는다.
