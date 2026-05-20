
SET FOREIGN_KEY_CHECKS = 0;


DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
                        `id`                BIGINT          NOT NULL AUTO_INCREMENT,
                        `email`             VARCHAR(100)    NOT NULL,
                        `password`          VARCHAR(255)    NOT NULL,
                        `name`              VARCHAR(50)     NOT NULL,
                        `nickname`          VARCHAR(50)     NOT NULL,
                        `role`              VARCHAR(20)     NOT NULL DEFAULT 'USER',
                        `status`            VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
                        `login_type`        VARCHAR(20)     NOT NULL DEFAULT 'LOCAL',
                        `last_login_at`     DATETIME            NULL,
                        `is_email_verified` BOOLEAN         NOT NULL DEFAULT FALSE,
                        `profile_image_url` VARCHAR(255)        NULL,
                        `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        `updated_at`        DATETIME            NULL ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_user_email` (`email`),
                        UNIQUE KEY `uk_user_nickname` (`nickname`)
);

DROP TABLE IF EXISTS `exam`;
CREATE TABLE `exam` (
                        `id`         BIGINT       NOT NULL AUTO_INCREMENT,
                        `code`       VARCHAR(50)  NOT NULL,
                        `name`       VARCHAR(100) NOT NULL,
                        `is_active`  BOOLEAN      NOT NULL DEFAULT FALSE,
                        `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        `updated_at` DATETIME         NULL ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_exam_code` (`code`)
);

DROP TABLE IF EXISTS `asset_file`;
CREATE TABLE `asset_file` (
                              `id`          BIGINT        NOT NULL AUTO_INCREMENT,
                              `storage_key` VARCHAR(500)  NOT NULL,
                              `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              `updated_at`  DATETIME          NULL ON UPDATE CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `domain_type`;
CREATE TABLE `domain_type` (
                               `id`         BIGINT       NOT NULL AUTO_INCREMENT,
                               `code`       VARCHAR(50)  NOT NULL,
                               `name`       VARCHAR(100) NOT NULL,
                               `is_active`  BOOLEAN      NOT NULL,
                               `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               `updated_at` DATETIME         NULL ON UPDATE CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uk_domain_type_code` (`code`)
);



DROP TABLE IF EXISTS `exam_version`;
CREATE TABLE `exam_version` (
                                `id`                       BIGINT       NOT NULL AUTO_INCREMENT,
                                `exam_id`                  BIGINT       NOT NULL,
                                `version_no`               INT          NOT NULL,
                                `version_name`             VARCHAR(100) NOT NULL,
                                `default_question_count`   INT          NOT NULL,
                                `duration_type`            VARCHAR(50)  NOT NULL,
                                `default_duration_seconds` INT              NULL,
                                `total_score`              INT          NOT NULL,
                                `passing_rule_type`        VARCHAR(50)  NOT NULL,
                                `passing_score`            INT          NOT NULL,
                                `has_subject_fail_rule`    BOOLEAN      NOT NULL DEFAULT FALSE,
                                `subject_fail_threshold`   INT              NULL,
                                `status`                   VARCHAR(20)  NOT NULL,
                                `created_at`               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `updated_at`               DATETIME         NULL ON UPDATE CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_exam_id_version_no` (`exam_id`, `version_no`),
                                CONSTRAINT `fk_exam_version_exam_id` FOREIGN KEY (`exam_id`) REFERENCES `exam` (`id`) ON DELETE CASCADE
);

DROP TABLE IF EXISTS `exam_part`;
CREATE TABLE `exam_part` (
                             `id`                       BIGINT       NOT NULL AUTO_INCREMENT,
                             `exam_version_id`          BIGINT       NOT NULL,
                             `code`                     VARCHAR(50)  NOT NULL,
                             `name`                     VARCHAR(100) NOT NULL,
                             `default_question_count`   INT          NOT NULL,
                             `default_duration_seconds` INT              NULL,
                             `total_score`              INT          NOT NULL,
                             `is_subject_fail_target`   BOOLEAN      NOT NULL DEFAULT FALSE,
                             `subject_fail_threshold_score` INT          NULL,
                             `is_active`                BOOLEAN      NOT NULL DEFAULT FALSE,
                             `display_order`            INT          NOT NULL,
                             `created_at`               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             `updated_at`               DATETIME         NULL ON UPDATE CURRENT_TIMESTAMP,
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `uk_exam_version_id_code` (`exam_version_id`, `code`),
                             CONSTRAINT `fk_exam_part_exam_version_id` FOREIGN KEY (`exam_version_id`) REFERENCES `exam_version` (`id`) ON DELETE CASCADE
);

DROP TABLE IF EXISTS `policy`;
CREATE TABLE `policy` (
                          `id`              BIGINT       NOT NULL AUTO_INCREMENT,
                          `exam_version_id` BIGINT       NOT NULL,
                          `domain_type_id`  BIGINT       NOT NULL,
                          `version_no`      VARCHAR(50)  NOT NULL,
                          `is_active`       BOOLEAN      NOT NULL,
                          `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          `updated_at`      DATETIME         NULL ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`),
                          CONSTRAINT `fk_policy_exam_version_id` FOREIGN KEY (`exam_version_id`) REFERENCES `exam_version` (`id`) ON DELETE CASCADE,
                          CONSTRAINT `fk_policy_domain_type_id` FOREIGN KEY (`domain_type_id`) REFERENCES `domain_type` (`id`) ON DELETE CASCADE
);



DROP TABLE IF EXISTS `exam_scope_node`;
CREATE TABLE `exam_scope_node` (
                                   `id`               BIGINT       NOT NULL AUTO_INCREMENT,
                                   `exam_version_id`  BIGINT       NOT NULL,
                                   `exam_part_id`     BIGINT       NOT NULL,
                                   `code`             VARCHAR(100) NOT NULL,
                                   `parent_id`        BIGINT           NULL,
                                   `node_type`        VARCHAR(30)  NOT NULL,
                                   `depth`            INT          NOT NULL,
                                   `title`            VARCHAR(200) NOT NULL,
                                   `is_leaf`          BOOLEAN      NOT NULL,
                                   `is_active`        BOOLEAN      NOT NULL,
                                   `display_order`    INT          NOT NULL DEFAULT 0,
                                   `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   `updated_at`       DATETIME         NULL ON UPDATE CURRENT_TIMESTAMP,
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_exam_version_id_code` (`exam_version_id`, `code`),
                                   CONSTRAINT `fk_exam_scope_node_exam_version_id` FOREIGN KEY (`exam_version_id`) REFERENCES `exam_version` (`id`) ON DELETE CASCADE,
                                   CONSTRAINT `fk_exam_scope_node_exam_part_id` FOREIGN KEY (`exam_part_id`) REFERENCES `exam_part` (`id`) ON DELETE CASCADE,
                                   CONSTRAINT `fk_exam_scope_node_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `exam_scope_node` (`id`) ON DELETE SET NULL
);

DROP TABLE IF EXISTS `policy_value`;
CREATE TABLE `policy_value` (
                                `id`         BIGINT       NOT NULL AUTO_INCREMENT,
                                `policy_id`  BIGINT       NOT NULL,
                                `value_key`  VARCHAR(100) NOT NULL,
                                `value_type` VARCHAR(30)  NOT NULL,
                                `value`      TEXT         NOT NULL,
                                `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `updated_at` DATETIME         NULL ON UPDATE CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`),
                                CONSTRAINT `fk_policy_value_policy_id` FOREIGN KEY (`policy_id`) REFERENCES `policy` (`id`) ON DELETE CASCADE
);

DROP TABLE IF EXISTS `problem_set_generation_job`;
CREATE TABLE `problem_set_generation_job` (
                                              `id`                  BIGINT       NOT NULL AUTO_INCREMENT,
                                              `user_id`             BIGINT       NOT NULL,
                                              `exam_version_id`     BIGINT       NOT NULL,
                                              `exam_scope_node_id`  BIGINT           NULL,
                                              `domain_type_id`      BIGINT           NULL,
                                              `workbook_type_id`    BIGINT           NULL,
                                              `workbook_id`         BIGINT           NULL,
                                              `retry_count`         INT          NOT NULL,
                                              `requested_count`     INT          NOT NULL,
                                              `status`              VARCHAR(30)  NOT NULL,
                                              `current_step`        VARCHAR(50)      NULL,
                                              `failure_code`        VARCHAR(100)     NULL,
                                              `started_at`          DATETIME         NULL,
                                              `completed_at`        DATETIME         NULL,
                                              `created_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              `updated_at`          DATETIME         NULL ON UPDATE CURRENT_TIMESTAMP,
                                              PRIMARY KEY (`id`),
                                              CONSTRAINT `fk_psgj_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
                                              CONSTRAINT `fk_psgj_exam_version_id` FOREIGN KEY (`exam_version_id`) REFERENCES `exam_version` (`id`) ON DELETE CASCADE,
                                              CONSTRAINT `fk_psgj_exam_scope_node_id` FOREIGN KEY (`exam_scope_node_id`) REFERENCES `exam_scope_node` (`id`) ON DELETE SET NULL,
                                              CONSTRAINT `fk_psgj_domain_type_id` FOREIGN KEY (`domain_type_id`) REFERENCES `domain_type` (`id`) ON DELETE SET NULL
);



DROP TABLE IF EXISTS `ai_generated_problem`;
CREATE TABLE `ai_generated_problem` (
                                        `id`                             BIGINT        NOT NULL AUTO_INCREMENT,
                                        `problem_set_generation_job_id`  BIGINT        NOT NULL,
                                        `exam_id`                        BIGINT        NOT NULL,
                                        `exam_version_id`                BIGINT        NOT NULL,
                                        `exam_scope_node_id`             BIGINT        NOT NULL,
                                        `expression_type`                VARCHAR(50)   NOT NULL,
                                        `difficulty`                     VARCHAR(20)   NOT NULL,
                                        `question_content_json`          JSON          NOT NULL,
                                        `explanation_json`               JSON          NOT NULL,
                                        `answer_type`                    VARCHAR(30)   NOT NULL,
                                        `answer_json`                    JSON          NOT NULL,
                                        `choice_type`                    VARCHAR(30)   NOT NULL,
                                        `review_status`                  VARCHAR(30)   NOT NULL,
                                        `created_at`                     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        `updated_at`                     DATETIME          NULL ON UPDATE CURRENT_TIMESTAMP,
                                        PRIMARY KEY (`id`),
                                        CONSTRAINT `fk_agp_problem_set_generation_job_id` FOREIGN KEY (`problem_set_generation_job_id`) REFERENCES `problem_set_generation_job` (`id`) ON DELETE CASCADE,
                                        CONSTRAINT `fk_agp_exam_id` FOREIGN KEY (`exam_id`) REFERENCES `exam` (`id`) ON DELETE CASCADE,
                                        CONSTRAINT `fk_agp_exam_version_id` FOREIGN KEY (`exam_version_id`) REFERENCES `exam_version` (`id`) ON DELETE CASCADE,
                                        CONSTRAINT `fk_agp_exam_scope_node_id` FOREIGN KEY (`exam_scope_node_id`) REFERENCES `exam_scope_node` (`id`) ON DELETE CASCADE
);

DROP TABLE IF EXISTS `ai_generated_problem_choice`;
CREATE TABLE `ai_generated_problem_choice` (
                                               `id`                      BIGINT   NOT NULL AUTO_INCREMENT,
                                               `ai_generated_problem_id` BIGINT   NOT NULL,
                                               `choice_no`               INT      NOT NULL,
                                               `choice_content_json`     JSON     NOT NULL,
                                               `created_at`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                               `updated_at`              DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,
                                               PRIMARY KEY (`id`),
                                               UNIQUE KEY `uk_agp_id_choice_no` (`ai_generated_problem_id`, `choice_no`),
                                               CONSTRAINT `fk_agpc_ai_generated_problem_id` FOREIGN KEY (`ai_generated_problem_id`) REFERENCES `ai_generated_problem` (`id`) ON DELETE CASCADE
);

DROP TABLE IF EXISTS `problem_review`;
CREATE TABLE `problem_review` (
                                  `id`                      BIGINT       NOT NULL AUTO_INCREMENT,
                                  `ai_generated_problem_id` BIGINT       NOT NULL,
                                  `review_status`           VARCHAR(30)  NOT NULL,
                                  `fail_reason`             VARCHAR(500)     NULL,
                                  `reviewed_at`             DATETIME         NULL,
                                  PRIMARY KEY (`id`),
                                  CONSTRAINT `fk_problem_review_agp_id` FOREIGN KEY (`ai_generated_problem_id`) REFERENCES `ai_generated_problem` (`id`) ON DELETE CASCADE
);

DROP TABLE IF EXISTS `problem_review_detail`;
CREATE TABLE `problem_review_detail` (
                                         `id`                BIGINT       NOT NULL AUTO_INCREMENT,
                                         `problem_review_id` BIGINT       NOT NULL,
                                         `policy_id`         BIGINT       NOT NULL,
                                         `check_result`      VARCHAR(20)  NOT NULL,
                                         `message`           VARCHAR(500)     NULL,
                                         `checked_at`        DATETIME         NULL,
                                         PRIMARY KEY (`id`),
                                         CONSTRAINT `fk_prd_problem_review_id` FOREIGN KEY (`problem_review_id`) REFERENCES `problem_review` (`id`) ON DELETE CASCADE,
                                         CONSTRAINT `fk_prd_policy_id` FOREIGN KEY (`policy_id`) REFERENCES `policy` (`id`) ON DELETE CASCADE
);



DROP TABLE IF EXISTS `problem`;
CREATE TABLE `problem` (
                           `id`                       BIGINT       NOT NULL AUTO_INCREMENT,
                           `exam_id`                  BIGINT       NOT NULL,
                           `exam_version_id`          BIGINT       NOT NULL,
                           `content_hash`             VARCHAR(64)  NOT NULL,
                           `exam_scope_node_id`       BIGINT           NULL,
                           `ai_generated_problem_id`  BIGINT           NULL,
                           `expression_type`          VARCHAR(30)  NOT NULL,
                           `difficulty`               VARCHAR(20)  NOT NULL,
                           `question_content_json`    JSON         NOT NULL,
                           `explanation_json`         JSON         NOT NULL,
                           `answer_type`              VARCHAR(30)  NOT NULL,
                           `answer_json`              JSON         NOT NULL,
                           `choice_type`              VARCHAR(30)  NOT NULL,
                           `source_type`              VARCHAR(30)  NOT NULL,
                           `is_active`                BOOLEAN      NOT NULL DEFAULT TRUE,
                           `created_at`               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           `updated_at`               DATETIME         NULL ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `uk_exam_version_id_content_hash` (`exam_version_id`, `content_hash`),
                           CONSTRAINT `fk_problem_exam_id` FOREIGN KEY (`exam_id`) REFERENCES `exam` (`id`) ON DELETE CASCADE,
                           CONSTRAINT `fk_problem_exam_version_id` FOREIGN KEY (`exam_version_id`) REFERENCES `exam_version` (`id`) ON DELETE CASCADE,
                           CONSTRAINT `fk_problem_exam_scope_node_id` FOREIGN KEY (`exam_scope_node_id`) REFERENCES `exam_scope_node` (`id`) ON DELETE SET NULL,
                           CONSTRAINT `fk_problem_ai_generated_problem_id` FOREIGN KEY (`ai_generated_problem_id`) REFERENCES `ai_generated_problem` (`id`) ON DELETE SET NULL
);

DROP TABLE IF EXISTS `problem_choice`;
CREATE TABLE `problem_choice` (
                                  `id`                  BIGINT   NOT NULL AUTO_INCREMENT,
                                  `problem_id`          BIGINT   NOT NULL,
                                  `choice_no`           INT      NOT NULL,
                                  `choice_content_json` JSON     NOT NULL,
                                  `created_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  `updated_at`          DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uk_problem_id_choice_no` (`problem_id`, `choice_no`),
                                  CONSTRAINT `fk_problem_choice_problem_id` FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`) ON DELETE CASCADE
);

DROP TABLE IF EXISTS `problem_available_usage_type`;
CREATE TABLE `problem_available_usage_type` (
                                                `id`             BIGINT   NOT NULL AUTO_INCREMENT,
                                                `problem_id`     BIGINT   NOT NULL,
                                                `domain_type_id` BIGINT   NOT NULL,
                                                `created_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                `updated_at`     DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,
                                                PRIMARY KEY (`id`),
                                                CONSTRAINT `fk_paut_problem_id` FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`) ON DELETE CASCADE,
                                                CONSTRAINT `fk_paut_domain_type_id` FOREIGN KEY (`domain_type_id`) REFERENCES `domain_type` (`id`) ON DELETE CASCADE
);

SET FOREIGN_KEY_CHECKS = 1;