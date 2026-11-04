-- 모집글 "지원하기" 클릭 수집용 테이블.
-- 클릭 한 건이 한 행이며, 중복 제거는 하지 않는다(같은 사람이 세 번 누르면 3건).
-- 일/주/월별 집계는 clicked_at을 축으로 삼는다.
CREATE TABLE `recruitment_apply_click`
(
    `id`             bigint    NOT NULL AUTO_INCREMENT,
    `created_at`     timestamp NULL DEFAULT NULL,
    `updated_at`     timestamp NULL DEFAULT NULL,
    `recruitment_id` bigint    NOT NULL,
    `clicked_at`     timestamp NOT NULL,
    PRIMARY KEY (`id`),
    KEY              `idx_apply_click_recruitment_clicked` (`recruitment_id`, `clicked_at`),
    CONSTRAINT `fk_apply_click_recruitment` FOREIGN KEY (`recruitment_id`) REFERENCES `recruitment` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
