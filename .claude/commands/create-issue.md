---
description: 이슈 생성 (작업코드 컨벤션에 맞춰 gh issue create 실행)
---

# 실행 절차

## 1. 이슈 내용 질문

* [사용자 화면]
  ```
  "생성할 Issue에 대해 간단히 적어주세요."
  ```

## 2. 제목 생성

- `docs/work-convention.md`와 `.github/ISSUE_TEMPLATE/issue-template.md`를 **병렬로 동시에** 읽기 (Read 2회를 한 메시지에서 호출)
- `docs/work-convention.md` 참고, `작업코드: 작업내용` 형식으로 이슈 제목 생성

## 3. 초안 작성

- 초안을 `.claude/tmp/issue_draft.md`에 저장 (Write 도구가 폴더를 자동 생성하므로 mkdir 불필요)
- 초안 파일 형식:
    * 1번째 줄: `작업코드: 작업내용`
    * 2번째 줄: `---`
    * 3번째 줄부터: `.github/ISSUE_TEMPLATE/issue-template.md` 형식의 본문

## 4. 초안 확인 및 선택지 제시

* [사용자 화면]
  ```
  (이슈 제목 표시)
  (본문 내용 코드블록으로 표시)
  
  📄 [.claude/tmp/issue_draft.md](.claude/tmp/issue_draft.md)

  원하시는 작업을 선택해주세요. ex)1

  [1] 이슈 생성

  [2] 취소

  (수정은 채팅으로 요청 or 파일 직접 수정)
  ```
* 선택지 각 줄 사이의 빈 줄을 유지한 채 그대로 출력 (마크다운에서 단일 줄바꿈이 합쳐지는 것 방지)

## 5. 전처리

- Read 도구로 `.claude/tmp/issue_draft.md`를 최신 내용 로드
    * 이전 대화 컨텍스트에 있는 초안 내용을 그대로 신뢰하지 않음 (사용자가 파일을 직접 수정했을 수 있음).
    * 제목·본문 모두 파일 내용 기준으로 반영 (1번째 줄 = 제목, 3번째 줄부터 = 본문)

## 6. 선택처리

* 사용자의 숫자(1/2) 또는 문구 답변에 따라 분기

### [1]이슈 생성

* 아래 명령어 실행 후 "7. 완료 안내"로 이동 (제목은 초안 1번째 줄, 본문은 3번째 줄부터 사용)

```bash
gh issue create \
  --repo greedy-team/mokkoji-be \
  --title "$(sed -n '1s/^제목: //p' .claude/tmp/issue_draft.md)" \
  --assignee @me \
  --body "$(tail -n +3 .claude/tmp/issue_draft.md)"
```

### [2] 취소

1. `rm .claude/tmp/issue_draft.md` 실행하여 초안 파일 삭제
2. 종료

### 그 외 문구 답변 (수정 요청)

1. 요청한 수정사항을 초안 파일에 반영
2. 4번으로 복귀

## 7. 완료 안내

[사용자 화면]

  ```
  "이슈가 생성되었습니다"
  (생성된 이슈 URL)
  ```

## 8. 임시 파일 정리

- `rm .claude/tmp/issue_draft.md` 실행하여 초안 파일 삭제
