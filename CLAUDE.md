# CLAUDE.md — 토닥운(todakun) Android

## 1. 외부 레퍼런스 (작업 전 반드시 확인)

- **API / Swagger**: https://api-dev.todakun.com/swagger-ui/index.html
  - Data 레이어(DTO, DataSource) 작업의 **단일 스펙 소스**. 엔드포인트·요청/응답 스키마를 여기서 확인한다.
- **디자인 / Figma**: https://www.figma.com/design/bLZr7Nh53PmRHuEjX7gNco/Yapp-2%EC%A1%B0--%ED%86%A0%EB%8B%A5%EC%9A%B4-
  - Presentation/Compose UI 작업의 기준. 색·타이포·간격은 `core:designsystem` 토큰과 대응시킨다.
  - Figma MCP(`figma:*` 스킬) 연동 시 노드 링크로 디자인 컨텍스트를 직접 조회할 수 있다.
---

## 2. 아키텍처 & 컨벤션 규칙

- 상세 규칙은 `.claude/rules/`에 레이어별로 분리되어 있다. **모든 단계의 에이전트는 필요에 따라 
.claude/rules/**의 md 파일을 로드해 따른다.**

## 3. 에이전트 코딩 워크플로우

- main/develop 브랜치에 직접 커밋을 금지한다
- 모든 작업은 git worktree에서 진행한다. 
- 각 단계는 전용 서브에이전트에 위임하며, **설계 → 구현 → 검토**가 피드백 루프를 이룬다.
```
  ┌──────────────────────────────────────────────────────────────┐
  │  이슈 #N                                                       │
  │                                                               │
  │  [1] 설계 (Opus 서브에이전트)                                  │
  │        └─ designs/issue-N-<slug>.md 내보내기                  │
  │                     │                                          │
  │              ▼ 사람 검토 게이트 (문서 수정 가능)               │
  │                     │  승인                                    │
  │  [2] 구현 (Sonnet 서브에이전트)                                │
  │                     │                                          │
  │  [3] 검토 (Opus 서브에이전트) ── 유닛테스트 + 린트(TODO)       │
  │            │                                                   │
  │   P1 있음 ─┘ (구현 단계로 회귀) ◄────────────                 │
  │            │                                                   │
  │   P1 없음 → 루프 종료 → 커밋/PR                                │
  └──────────────────────────────────────────────────────────────┘
```

오케스트레이터(메인 Claude)는 `Agent` 툴의 `subagent_type`으로 각 단계를 위임한다. OMC 에이전트는 정의된 모델(Opus/Sonnet)로 실행되며, 필요 시 `model` 파라미터로 재정의할 수 있다.

**서브에이전트 컨텍스트 전달(필수):** 서브에이전트는 이전 대화 맥락을 모르는 콜드 스타트로 시작한다. `Agent` 프롬프트에는 매번 아래를 명시적으로 포함한다.
- 이슈 번호·요약과 (있다면) `designs/issue-<번호>-<slug>.md`의 **경로**(내용을 요약해 넣지 말고 최신 파일을 다시 읽게 한다)
- 해당 단계·레이어에 관련된 `.claude/rules/*.md` **경로 목록**
- 단계별 산출물 요구사항(설계=문서 내보내기, 구현=파일 변경 계획 체크리스트, 검토=심각도 태그)

이 지시가 빠지면 서브에이전트가 규칙 파일을 읽지 않고 진행할 수 있다.

### 단계 0 — 이슈 파악
- 이슈 본문(작업 내용/할 일)과 관련 Swagger·Figma를 확인한다.
- 브랜치를 이슈 단위로 분리한다(`feature/<이슈번호>-<slug>` 권장). `develop`/`main`에 직접 작업하지 않는다.

### 단계 1 — 설계 (Opus) → 문서 내보내기
- **목적**: 구현 전에 레이어별 설계를 확정하고, **사람이 검토·수정할 수 있는 산출물**을 만든다.
- **서브에이전트**: `oh-my-claudecode:planner` 또는 `oh-my-claudecode:architect` (둘 다 Opus, 후자는 read-only 분석). 요구사항이 모호하면 먼저 `oh-my-claudecode:analyst`.
- **스킬**: `superpowers:brainstorming`(설계 착수 전 의도·요구사항 정리) → `superpowers:writing-plans`. 대안으로 `oh-my-claudecode:plan`.
- **산출물(필수)**: `.claude/templates/design-template.md`를 채워 **`designs/issue-<번호>-<slug>.md`** 로 저장(프로젝트 루트의 `designs/`).
  - 문서에는 완료 기준, 레이어별 설계, 파일 변경 계획, **테스트 계획**을 포함한다.
- **게이트**: 여기서 **멈추고 사람의 검토를 받는다.** 설계 문서는 사람이 자유롭게 수정할 수 있으므로,
  - 구현 단계는 **항상 최신 문서 내용을 다시 읽고** 진행한다(에이전트 기억이 아니라 파일이 단일 소스).
  - 사람이 "승인" 또는 문서 상태를 `승인됨(구현 가능)`으로 바꾸기 전에는 구현으로 넘어가지 않는다.

### 단계 2 — 구현 (Sonnet)
- **서브에이전트**: `oh-my-claudecode:executor`(Sonnet, 일반 구현). UI/Compose 비중이 크면 `oh-my-claudecode:designer`(Sonnet).
- **스킬**: `superpowers:test-driven-development`(구현 전 유닛 테스트 먼저) · `superpowers:executing-plans`(설계 문서를 계획으로 실행).
- **입력**: 승인된 `designs/issue-<번호>-<slug>.md` (수정본 반영). `.claude/rules/*` 준수.
- **작업**: 문서의 "파일 변경 계획" 체크리스트를 따라 구현하고, 테스트 계획의 유닛 테스트를 함께 작성한다.

### 단계 3 — 검토 (Opus) → 유닛테스트 + 린트
- **서브에이전트**: `oh-my-claudecode:code-reviewer`(심각도 리뷰) + `oh-my-claudecode:critic`(Opus, 다관점 검토). 완료 판정은 `oh-my-claudecode:verifier`, 테스트 적정성은 `oh-my-claudecode:test-engineer`.
  - ⚠️ `code-reviewer`/`verifier`/`test-engineer`는 OMC 정의상 모델이 고정되어 있지 않을 수 있다. **검토 단계는 반드시 Opus로 실행**해야 하므로 `Agent` 호출 시 `model: "opus"`를 명시적으로 지정한다.
- **스킬**: `superpowers:requesting-code-review` · `superpowers:verification-before-completion`(증거 없이는 완료 주장 금지).
- **검증(반드시 실행)**:
  1. **유닛 테스트**: `./gradlew test` (변경 모듈만: 예 `./gradlew :feature:login:test :core:domain:test`)
  2. **린트**: ⚠️ **TODO — 현재 저장소에 lint 미설정** (아래 6절 참조). 설정 전까지는 `.claude/rules/40`의 Compose/ktlint 규칙을 **에이전트가 수동 점검**한다.
- **판정**(`.claude/rules/50` 기준):
  - **[P1]이 하나라도 있으면** → 단계 2(구현)로 **회귀**해 수정 후 다시 검토(루프 반복). 필요 시 설계 결함이면 단계 1로 회귀.
  - **P2/P3만 남으면** → 반영 후 루프 종료 가능.

### 루프 종료
- 유닛 테스트 통과 + P1 없음이면 종료. 커밋은 필요에 따라 가능하며 PR은 **사용자가 요청할 때만** 수행한다(`oh-my-claudecode:git-master` 활용, PR 템플릿 `.github/pull_request_template.md` 사용, 이슈는 `close #N`으로 연결).
---

## 5. 단계별 스킬·에이전트 매핑 (요약)

| 단계 | 모델 | 서브에이전트(`subagent_type`) | 보조 스킬 |
|------|------|------------------------------|-----------|
| 설계 | Opus | `oh-my-claudecode:planner` / `architect` / `analyst` | `superpowers:brainstorming`, `superpowers:writing-plans`, `oh-my-claudecode:plan` |
| 구현 | Sonnet | `oh-my-claudecode:executor` / `designer` | `superpowers:test-driven-development`, `superpowers:executing-plans` |
| 검토 | Opus | `oh-my-claudecode:code-reviewer` / `critic` / `verifier` / `test-engineer` | `superpowers:requesting-code-review`, `superpowers:verification-before-completion` |

> 여러 이슈를 병렬로 돌릴 땐 이슈별 git worktree로 격리한다(`superpowers:using-git-worktrees`). Figma 연동이 필요한 UI 이슈는 `figma:*` 스킬로 디자인 컨텍스트를 가져온다.

---

## 6. 빌드·검증 명령어

```bash
./gradlew assembleDebug                 # 전체 빌드
./gradlew test                          # 전체 유닛 테스트
./gradlew ktlintCheck                   # 코드 포맷/Compose lint 검사
./gradlew ktlintFormat                  # 코드 자동 포맷
```
---

