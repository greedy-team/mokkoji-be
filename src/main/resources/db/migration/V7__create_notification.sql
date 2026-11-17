CREATE TABLE `notification`
(
    `id`         bigint       NOT NULL AUTO_INCREMENT,
    `created_at` timestamp NULL DEFAULT NULL,
    `updated_at` timestamp NULL DEFAULT NULL,
    `user_id`    bigint       NOT NULL,
    `type`       varchar(50)  NOT NULL,
    `title`      varchar(100) NOT NULL,
    `content`    text,
    `target_id`  bigint                DEFAULT NULL,
    `is_read`    tinyint(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_notification_user_created` (`user_id`, `created_at` DESC),
    KEY `idx_notification_user_read` (`user_id`, `is_read`),
    CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
