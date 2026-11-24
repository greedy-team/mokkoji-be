ALTER TABLE `feedback`
    ADD COLUMN `school` varchar(255) NOT NULL DEFAULT '' AFTER `content`;

ALTER TABLE `feedback`
    ALTER COLUMN `school` DROP DEFAULT;
