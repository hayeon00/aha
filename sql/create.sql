

SET FOREIGN_KEY_CHECKS = 0;


DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
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

DROP TABLE IF EXISTS `user_exam`;
CREATE TABLE `user_exam` (
                             `id`         BIGINT   NOT NULL AUTO_INCREMENT,
                             `user_id`    BIGINT   NOT NULL,
                             `exam_id`    BIGINT   NOT NULL,
                             `is_main`    BOOLEAN  NOT NULL DEFAULT FALSE,
                             `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             `updated_at` DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,

                             PRIMARY KEY (`id`),

                             UNIQUE KEY `uk_user_exam_user_exam` (`user_id`, `exam_id`),

                             INDEX `idx_user_exam_user_id` (`user_id`),
                             INDEX `idx_user_exam_exam_id` (`exam_id`),
                             INDEX `idx_user_exam_user_main` (`user_id`, `is_main`),

                             CONSTRAINT `fk_user_exam_user_id`
                                 FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,

                             CONSTRAINT `fk_user_exam_exam_id`
                                 FOREIGN KEY (`exam_id`) REFERENCES `exam` (`id`) ON DELETE CASCADE
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
                                              CONSTRAINT `fk_psgj_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
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



-- 하연 테이블 ======================================================================================================================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `ai_reference`;
DROP TABLE IF EXISTS `ai_message`;
DROP TABLE IF EXISTS `learning_problem_attempt`;
DROP TABLE IF EXISTS `learning_progress`;
DROP TABLE IF EXISTS `learning_session`;
DROP TABLE IF EXISTS `learning_content_body`;
DROP TABLE IF EXISTS `learning_content`;
DROP TABLE IF EXISTS `extracted_content`;
DROP TABLE IF EXISTS `document_processing`;
DROP TABLE IF EXISTS `learning_source_document`;

SET FOREIGN_KEY_CHECKS = 1;


CREATE TABLE `learning_source_document` (
                                            `id`          BIGINT        NOT NULL AUTO_INCREMENT,
                                            `title`       VARCHAR(200)  NOT NULL,
                                            `source_type` VARCHAR(50)   NOT NULL,
                                            `file_name`   VARCHAR(255)      NULL,
                                            `file_path`   VARCHAR(500)      NULL,
                                            `description` VARCHAR(500)      NULL,
                                            `is_active`   BOOLEAN       NOT NULL DEFAULT TRUE,
                                            `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            `updated_at`  DATETIME          NULL ON UPDATE CURRENT_TIMESTAMP,
                                            PRIMARY KEY (`id`)
);


CREATE TABLE `document_processing` (
                                       `id`                 BIGINT        NOT NULL AUTO_INCREMENT,
                                       `source_document_id` BIGINT        NOT NULL,
                                       `status`             VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
                                       `requested_by`       BIGINT            NULL,
                                       `error_message`      VARCHAR(1000)     NULL,
                                       `started_at`         DATETIME          NULL,
                                       `completed_at`       DATETIME          NULL,
                                       `created_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       `updated_at`         DATETIME          NULL ON UPDATE CURRENT_TIMESTAMP,
                                       PRIMARY KEY (`id`),
                                       CONSTRAINT `fk_document_processing_source_document_id`
                                           FOREIGN KEY (`source_document_id`) REFERENCES `learning_source_document` (`id`) ON DELETE CASCADE,
                                       CONSTRAINT `fk_document_processing_requested_by`
                                           FOREIGN KEY (`requested_by`) REFERENCES `users` (`id`) ON DELETE SET NULL
);


CREATE TABLE `extracted_content` (
                                     `id`                   BIGINT    NOT NULL AUTO_INCREMENT,
                                     `source_document_id`   BIGINT    NOT NULL,
                                     `processing_id`        BIGINT        NULL,
                                     `exam_scope_node_id`   BIGINT        NULL,
                                     `chunk_order`          INT       NOT NULL,
                                     `page_no`              INT           NULL,
                                     `content_text`         LONGTEXT  NOT NULL,
                                     `is_used_for_learning` BOOLEAN   NOT NULL DEFAULT FALSE,
                                     `is_used_for_rag`      BOOLEAN   NOT NULL DEFAULT TRUE,
                                     `created_at`           DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     `updated_at`           DATETIME      NULL ON UPDATE CURRENT_TIMESTAMP,
                                     PRIMARY KEY (`id`),
                                     CONSTRAINT `fk_extracted_content_source_document_id`
                                         FOREIGN KEY (`source_document_id`) REFERENCES `learning_source_document` (`id`) ON DELETE CASCADE,
                                     CONSTRAINT `fk_extracted_content_processing_id`
                                         FOREIGN KEY (`processing_id`) REFERENCES `document_processing` (`id`) ON DELETE SET NULL,
                                     CONSTRAINT `fk_extracted_content_exam_scope_node_id`
                                         FOREIGN KEY (`exam_scope_node_id`) REFERENCES `exam_scope_node` (`id`) ON DELETE SET NULL
);


CREATE TABLE `learning_content` (
                                    `id`                 BIGINT       NOT NULL AUTO_INCREMENT,
                                    `exam_scope_node_id` BIGINT       NOT NULL,
                                    `title`              VARCHAR(200) NOT NULL,
                                    `summary`            VARCHAR(500)     NULL,
                                    `rag_enabled`        BOOLEAN      NOT NULL DEFAULT TRUE,
                                    `is_active`          BOOLEAN      NOT NULL DEFAULT TRUE,
                                    `display_order`      INT          NOT NULL DEFAULT 1,
                                    `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    `updated_at`         DATETIME         NULL ON UPDATE CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_learning_content_scope_node_order` (`exam_scope_node_id`, `display_order`),
                                    CONSTRAINT `fk_learning_content_exam_scope_node_id`
                                        FOREIGN KEY (`exam_scope_node_id`) REFERENCES `exam_scope_node` (`id`) ON DELETE CASCADE
);


CREATE TABLE `learning_content_body` (
                                         `id`                  BIGINT       NOT NULL AUTO_INCREMENT,
                                         `learning_content_id` BIGINT       NOT NULL,
                                         `body_type`           VARCHAR(50)  NOT NULL,
                                         `title`               VARCHAR(200)     NULL,
                                         `body_text`           LONGTEXT     NOT NULL,
                                         `display_order`       INT          NOT NULL,
                                         `rag_chunk_order`     INT              NULL,
                                         `is_active`           BOOLEAN      NOT NULL DEFAULT TRUE,
                                         `created_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         `updated_at`          DATETIME         NULL ON UPDATE CURRENT_TIMESTAMP,
                                         PRIMARY KEY (`id`),
                                         UNIQUE KEY `uk_learning_content_body_order` (`learning_content_id`, `display_order`),
                                         CONSTRAINT `fk_learning_content_body_learning_content_id`
                                             FOREIGN KEY (`learning_content_id`) REFERENCES `learning_content` (`id`) ON DELETE CASCADE
);


CREATE TABLE `learning_session` (
                                    `id`                  BIGINT      NOT NULL AUTO_INCREMENT,
                                    `user_id`             BIGINT      NOT NULL,
                                    `exam_scope_node_id`  BIGINT      NOT NULL,
                                    `learning_content_id` BIGINT      NULL,
                                    `status`              VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
                                    `started_at`          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    `ended_at`            DATETIME    NULL,
                                    `last_accessed_at`    DATETIME    NULL,
                                    `created_at`          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    `updated_at`          DATETIME    NULL ON UPDATE CURRENT_TIMESTAMP,

                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_learning_session_user_node` (`user_id`, `exam_scope_node_id`),

                                    CONSTRAINT `fk_learning_session_user_id`
                                        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,

                                    CONSTRAINT `fk_learning_session_exam_scope_node_id`
                                        FOREIGN KEY (`exam_scope_node_id`) REFERENCES `exam_scope_node` (`id`) ON DELETE CASCADE,

                                    CONSTRAINT `fk_learning_session_learning_content_id`
                                        FOREIGN KEY (`learning_content_id`) REFERENCES `learning_content` (`id`) ON DELETE SET NULL
);


CREATE TABLE `learning_problem_attempt` (
                                            `id`                  BIGINT   NOT NULL AUTO_INCREMENT,
                                            `user_id`             BIGINT   NOT NULL,
                                            `learning_session_id` BIGINT       NULL,
                                            `exam_scope_node_id`  BIGINT   NOT NULL,
                                            `problem_id`          BIGINT   NOT NULL,
                                            `selected_choice_id`  BIGINT       NULL,
                                            `is_correct`          BOOLEAN  NOT NULL,
                                            `submitted_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            `created_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            `updated_at`          DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,
                                            PRIMARY KEY (`id`),
                                            CONSTRAINT `fk_learning_problem_attempt_user_id`
                                                FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                                            CONSTRAINT `fk_learning_problem_attempt_session_id`
                                                FOREIGN KEY (`learning_session_id`) REFERENCES `learning_session` (`id`) ON DELETE SET NULL,
                                            CONSTRAINT `fk_learning_problem_attempt_exam_scope_node_id`
                                                FOREIGN KEY (`exam_scope_node_id`) REFERENCES `exam_scope_node` (`id`) ON DELETE CASCADE,
                                            CONSTRAINT `fk_learning_problem_attempt_problem_id`
                                                FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`) ON DELETE CASCADE,
                                            CONSTRAINT `fk_learning_problem_attempt_selected_choice_id`
                                                FOREIGN KEY (`selected_choice_id`) REFERENCES `problem_choice` (`id`) ON DELETE SET NULL
);



CREATE TABLE `ai_message` (
                              `id`                  BIGINT      NOT NULL AUTO_INCREMENT,
                              `learning_session_id` BIGINT          NULL,
                              `user_id`             BIGINT      NOT NULL,
                              `exam_scope_node_id`  BIGINT          NULL,
                              `role`                VARCHAR(20) NOT NULL,
                              `question_type`       VARCHAR(50)     NULL,
                              `message_text`        LONGTEXT    NOT NULL,
                              `created_at`          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              `updated_at`          DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`),
                              CONSTRAINT `fk_ai_message_learning_session_id`
                                  FOREIGN KEY (`learning_session_id`) REFERENCES `learning_session` (`id`) ON DELETE SET NULL,
                              CONSTRAINT `fk_ai_message_user_id`
                                  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                              CONSTRAINT `fk_ai_message_exam_scope_node_id`
                                  FOREIGN KEY (`exam_scope_node_id`) REFERENCES `exam_scope_node` (`id`) ON DELETE SET NULL
);



CREATE TABLE `ai_reference` (
                                `id`                       BIGINT   NOT NULL AUTO_INCREMENT,
                                `ai_message_id`            BIGINT   NOT NULL,
                                `learning_content_id`      BIGINT       NULL,
                                `learning_content_body_id` BIGINT       NULL,
                                `extracted_content_id`     BIGINT       NULL,
                                `similarity_score`         DOUBLE       NULL,
                                `display_order`            INT      NOT NULL,
                                `created_at`               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `updated_at`               DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`),
                                CONSTRAINT `fk_ai_reference_ai_message_id`
                                    FOREIGN KEY (`ai_message_id`) REFERENCES `ai_message` (`id`) ON DELETE CASCADE,
                                CONSTRAINT `fk_ai_reference_learning_content_id`
                                    FOREIGN KEY (`learning_content_id`) REFERENCES `learning_content` (`id`) ON DELETE SET NULL,
                                CONSTRAINT `fk_ai_reference_learning_content_body_id`
                                    FOREIGN KEY (`learning_content_body_id`) REFERENCES `learning_content_body` (`id`) ON DELETE SET NULL,
                                CONSTRAINT `fk_ai_reference_extracted_content_id`
                                    FOREIGN KEY (`extracted_content_id`) REFERENCES `extracted_content` (`id`) ON DELETE SET NULL
);

CREATE TABLE refresh_tokens (
                                id BIGINT NOT NULL AUTO_INCREMENT,
                                user_id BIGINT NOT NULL,
                                token VARCHAR(500) NOT NULL,
                                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                PRIMARY KEY (id),

                                CONSTRAINT uk_refresh_tokens_user_id UNIQUE (user_id),

                                CONSTRAINT fk_refresh_tokens_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES users (id)
                                        ON DELETE CASCADE
);


CREATE INDEX `idx_document_processing_source_document_id` ON `document_processing` (`source_document_id`);

CREATE INDEX `idx_extracted_content_source_document_id` ON `extracted_content` (`source_document_id`);
CREATE INDEX `idx_extracted_content_exam_scope_node_id` ON `extracted_content` (`exam_scope_node_id`);

CREATE INDEX `idx_learning_content_exam_scope_node_id` ON `learning_content` (`exam_scope_node_id`);
CREATE INDEX `idx_learning_content_body_learning_content_id` ON `learning_content_body` (`learning_content_id`);

CREATE INDEX `idx_learning_session_user_id` ON `learning_session` (`user_id`);
CREATE INDEX `idx_learning_session_scope_node_id` ON `learning_session` (`exam_scope_node_id`);
CREATE INDEX idx_learning_session_user_status_node ON learning_session (user_id, status, exam_scope_node_id);

CREATE INDEX `idx_learning_problem_attempt_user_id` ON `learning_problem_attempt` (`user_id`);
CREATE INDEX `idx_learning_problem_attempt_problem_id` ON `learning_problem_attempt` (`problem_id`);

CREATE INDEX `idx_ai_message_user_id` ON `ai_message` (`user_id`);
CREATE INDEX `idx_ai_message_session_id` ON `ai_message` (`learning_session_id`);
CREATE INDEX `idx_ai_reference_message_id` ON `ai_reference` (`ai_message_id`);


-- ===================================================================================================================================