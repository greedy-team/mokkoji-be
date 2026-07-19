-- 모꼬지 baseline 스키마
--
-- 2026-07-19 기준 dev 환경(mokkoji_dev)의 실제 스키마를 그대로 옮긴 것이다.
-- Flyway 도입 시점의 기준점 역할을 하며 baseline-on-migrate 설정에 의해 이미 스키마가 존재하는 환경에서는 실행되지 않고 "적용된 것으로 간주"된다.
--
-- 이후 모든 스키마 변경은 V2 이상의 새 마이그레이션 파일로 추가한다.
-- 이 파일은 한 번 머지된 뒤에는 절대 수정하지 않는다(checksum 불일치로 기동 실패).

CREATE TABLE `university`
(
    `id`         bigint       NOT NULL AUTO_INCREMENT,
    `created_at` timestamp    NULL DEFAULT NULL,
    `updated_at` timestamp    NULL DEFAULT NULL,
    `code`       varchar(50)  NOT NULL,
    `logo`       text,
    `name`       varchar(100) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK57sqjuqfhckx8p0d83h8cc98x` (`code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `user`
(
    `id`            bigint      NOT NULL AUTO_INCREMENT,
    `created_at`    timestamp   NULL DEFAULT NULL,
    `updated_at`    timestamp   NULL DEFAULT NULL,
    `code`          varchar(50) NOT NULL,
    `kakao_id`      varchar(50) NOT NULL,
    `name`          varchar(50)      DEFAULT NULL,
    `email`         varchar(50)      DEFAULT NULL,
    `is_email_on`   tinyint(1)  NOT NULL,
    `role`          varchar(50)      DEFAULT NULL,
    `university_id` bigint           DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_user_code` (`code`),
    KEY `FK6egtfhd9smck1965dxo86hpa` (`university_id`),
    CONSTRAINT `FK6egtfhd9smck1965dxo86hpa` FOREIGN KEY (`university_id`) REFERENCES `university` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `admin`
(
    `id`            bigint       NOT NULL AUTO_INCREMENT,
    `created_at`    timestamp    NULL DEFAULT NULL,
    `updated_at`    timestamp    NULL DEFAULT NULL,
    `login_id`      varchar(100) NOT NULL,
    `password`      varchar(100) NOT NULL,
    `role`          varchar(50)  NOT NULL,
    `university_id` bigint            DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `FKfxvtexdxtvf4jjl017d4pfaof` (`university_id`),
    CONSTRAINT `FKfxvtexdxtvf4jjl017d4pfaof` FOREIGN KEY (`university_id`) REFERENCES `university` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `club`
(
    `id`               bigint      NOT NULL AUTO_INCREMENT,
    `created_at`       timestamp   NULL DEFAULT NULL,
    `updated_at`       timestamp   NULL DEFAULT NULL,
    `name`             varchar(50) NOT NULL,
    `club_category`    varchar(20) NOT NULL,
    `club_affiliation` varchar(20) NOT NULL,
    `description`      text,
    `instagram`        text,
    `logo`             text,
    `master_id`        bigint           DEFAULT NULL,
    `university_id`    bigint      NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_club_category` (`club_category`),
    KEY `idx_club_master_id` (`master_id`),
    KEY `FKmf82w0dtc98509noons2e6hww` (`university_id`),
    CONSTRAINT `FK62nm4d35v5fd8ewtiuu8r8r43` FOREIGN KEY (`master_id`) REFERENCES `user` (`id`),
    CONSTRAINT `FKmf82w0dtc98509noons2e6hww` FOREIGN KEY (`university_id`) REFERENCES `university` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `club_application`
(
    `id`               bigint      NOT NULL AUTO_INCREMENT,
    `created_at`       timestamp   NULL DEFAULT NULL,
    `updated_at`       timestamp   NULL DEFAULT NULL,
    `applicant_name`   varchar(50) NOT NULL,
    `club_name`        varchar(50) NOT NULL,
    `club_category`    varchar(20) NOT NULL,
    `club_affiliation` varchar(20) NOT NULL,
    `description`      text        NOT NULL,
    `instagram`        text,
    `logo`             text,
    `status`           varchar(20) NOT NULL,
    `reject_reason`    text,
    `applicant_id`     bigint      NOT NULL,
    `university_id`    bigint      NOT NULL,
    PRIMARY KEY (`id`),
    KEY `FKg9n4pqtyva5xrnk0k2pdx3fcb` (`applicant_id`),
    KEY `FKgtimrbhut3bcfg9b4xxmqc8wf` (`university_id`),
    CONSTRAINT `FKg9n4pqtyva5xrnk0k2pdx3fcb` FOREIGN KEY (`applicant_id`) REFERENCES `user` (`id`),
    CONSTRAINT `FKgtimrbhut3bcfg9b4xxmqc8wf` FOREIGN KEY (`university_id`) REFERENCES `university` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `club_master_application`
(
    `id`            bigint      NOT NULL AUTO_INCREMENT,
    `created_at`    timestamp   NULL DEFAULT NULL,
    `updated_at`    timestamp   NULL DEFAULT NULL,
    `user_name`     varchar(50) NOT NULL,
    `status`        varchar(20) NOT NULL,
    `reject_reason` text,
    `club_id`       bigint      NOT NULL,
    `user_id`       bigint      NOT NULL,
    `university_id` bigint      NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_cma_user_id` (`user_id`),
    KEY `FKmruj573eokbb3d5q45twfob71` (`club_id`),
    KEY `FKj68ywqrfgmy6qjpnmncuc25vk` (`university_id`),
    CONSTRAINT `FKn4lbqhbdmdpc2vu5owbvsx6mj` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `FKmruj573eokbb3d5q45twfob71` FOREIGN KEY (`club_id`) REFERENCES `club` (`id`),
    CONSTRAINT `FKj68ywqrfgmy6qjpnmncuc25vk` FOREIGN KEY (`university_id`) REFERENCES `university` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `recruitment`
(
    `id`                   bigint     NOT NULL AUTO_INCREMENT,
    `created_at`           timestamp  NULL DEFAULT NULL,
    `updated_at`           timestamp  NULL DEFAULT NULL,
    `title`                text       NOT NULL,
    `content`              text,
    `recruit_form`         text,
    `recruit_start`        timestamp  NULL DEFAULT NULL,
    `recruit_end`          timestamp  NULL DEFAULT NULL,
    `is_always_recruiting` tinyint(1) NOT NULL,
    `club_id`              bigint     NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_recruitment_end` (`recruit_end`),
    KEY `FKnn5exigvdqh7l9n7d5gia1p0r` (`club_id`),
    CONSTRAINT `FKnn5exigvdqh7l9n7d5gia1p0r` FOREIGN KEY (`club_id`) REFERENCES `club` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `recruitment_image`
(
    `id`             bigint    NOT NULL AUTO_INCREMENT,
    `created_at`     timestamp NULL DEFAULT NULL,
    `updated_at`     timestamp NULL DEFAULT NULL,
    `image`          text,
    `recruitment_id` bigint    NOT NULL,
    PRIMARY KEY (`id`),
    KEY `FK2nquyvacyne5nclx00sfwdswd` (`recruitment_id`),
    CONSTRAINT `FK2nquyvacyne5nclx00sfwdswd` FOREIGN KEY (`recruitment_id`) REFERENCES `recruitment` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `comment`
(
    `id`         bigint    NOT NULL AUTO_INCREMENT,
    `created_at` timestamp NULL DEFAULT NULL,
    `updated_at` timestamp NULL DEFAULT NULL,
    `content`    text      NOT NULL,
    `rate`       double    NOT NULL,
    `club_id`    bigint    NOT NULL,
    `user_id`    bigint         DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `FK8kcum44fvpupyw6f5baccx25c` (`user_id`),
    KEY `FKfas0jm4664vilp9k50ixnqj0h` (`club_id`),
    CONSTRAINT `FK8kcum44fvpupyw6f5baccx25c` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `FKfas0jm4664vilp9k50ixnqj0h` FOREIGN KEY (`club_id`) REFERENCES `club` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `favorite`
(
    `id`      bigint NOT NULL AUTO_INCREMENT,
    `club_id` bigint NOT NULL,
    `user_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    KEY `FKdew6h96myfn9p3dj7o4h1iky1` (`club_id`),
    KEY `FKh3f2dg11ibnht4fvnmx60jcif` (`user_id`),
    CONSTRAINT `FKdew6h96myfn9p3dj7o4h1iky1` FOREIGN KEY (`club_id`) REFERENCES `club` (`id`),
    CONSTRAINT `FKh3f2dg11ibnht4fvnmx60jcif` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE `feedback`
(
    `id`         bigint    NOT NULL AUTO_INCREMENT,
    `created_at` timestamp NULL DEFAULT NULL,
    `updated_at` timestamp NULL DEFAULT NULL,
    `content`    text      NOT NULL,
    `rating`     int       NOT NULL,
    `user_id`    bigint    NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
