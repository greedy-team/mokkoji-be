# JOIN FETCH 도입 배경 및 성능 측정

## 문제: N+1 쿼리

`Recruitment`와 `Club`은 `@ManyToOne(fetch = LAZY)` 관계다.  
스케줄러가 모집 알림 발송 대상을 조회할 때 세 가지 쿼리를 실행한다.

```java
recruitmentRepository.findAllByRecruitStartToday(today)   // 모집 시작일 = 오늘
recruitmentRepository.findAllByRecruitEndToday(today)     // 모집 마감일 = 오늘
recruitmentRepository.findAllByRecruitEndInThreeDays(d)   // 마감 3일 전
```

이후 `EmailService`에서 각 `Recruitment`의 `club.getName()`을 호출하는데,  
JOIN FETCH 없이는 club이 아직 로딩되지 않았으므로 모집글 수(N)만큼 SELECT가 추가 발생한다.

```
SELECT * FROM recruitment WHERE date(recruit_start) = ?    -- 1건
SELECT * FROM club WHERE id = 1                            -- lazy 로딩
SELECT * FROM club WHERE id = 2
...
SELECT * FROM club WHERE id = N
                                             합계: N+1건
```

---

## 해결: JOIN FETCH

```java
@Query("SELECT r FROM Recruitment r JOIN FETCH r.club WHERE FUNCTION('DATE', r.recruitStart) = :currentDate")
List<Recruitment> findAllByRecruitStartToday(LocalDate currentDate);
```

recruitment와 club을 한 번의 JOIN으로 가져와 추가 SELECT를 제거한다.

```sql
SELECT r.*, c.*
FROM recruitment r
JOIN club c ON c.id = r.club_id
WHERE date(r.recruit_start) = ?                            -- 1건
```

---

## 성능 측정

**환경**
- DB: MySQL 8.0 (Testcontainer)
- 데이터: 동아리 50개, 모집글 50개 (동아리당 1개)
- SQL 카운트: Hibernate `StatementInspector` 실측
- 측정일: 2026-08-21

**결과**

|              | SQL 건수                        | 실행 시간 |
|--------------|---------------------------------|-----------|
| N+1          | 51건 (recruitment 1 + club 50)  | 62ms      |
| JOIN FETCH   | 1건                             | 19ms      |
| 개선         | **51배 감소**                   | **약 3배 단축** |

> 이 수치는 테스트 환경 실측값이다. 프로덕션 DB 환경 및 데이터 규모에 따라 달라질 수 있다.

---

## 적용 범위

스케줄러에서 사용하는 세 가지 쿼리 모두에 JOIN FETCH를 적용했다.

| 메서드 | 조건 |
|--------|------|
| `findAllByRecruitStartToday` | 모집 시작일 = 오늘 |
| `findAllByRecruitEndToday` | 모집 마감일 = 오늘 |
| `findAllByRecruitEndInThreeDays` | 모집 마감일 = 오늘 + 3일 |

---

## 관련 파일

- [`RecruitmentRepository.java`](../src/main/java/com/greedy/mokkoji/db/recruitment/repository/RecruitmentRepository.java)
- [`JoinFetchBenchmarkTest.java`](../src/test/java/com/greedy/mokkoji/notification/JoinFetchBenchmarkTest.java)
