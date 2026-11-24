-- 구버전 애플리케이션의 조회수 쓰기를 중단하고 진행 중 요청을 모두 처리한 뒤 적용한다.
-- 기존 조회수는 이관하지 않으며 새 수집 시작일부터 집계한다.
-- 대상이 삭제되더라도 이미 수집한 통계를 보존하기 위해 FK를 두지 않는다.
CREATE TABLE view_count
(
    target_type    varchar(32) NOT NULL,
    target_id      bigint      NOT NULL,
    stat_date      date        NOT NULL,
    view_count     bigint      NOT NULL DEFAULT 0,
    PRIMARY KEY (target_type, target_id, stat_date),
    CONSTRAINT chk_view_count_nonnegative CHECK (view_count >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_bin;

CREATE TABLE view_count_batch_history
(
    batch_id   varchar(64)  NOT NULL,
    applied_at timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (batch_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_bin;

ALTER TABLE club DROP COLUMN view_count;
