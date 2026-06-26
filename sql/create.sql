SET FOREIGN_KEY_CHECKS = 0;


DROP TABLE IF EXISTS `ai_reference`;
DROP TABLE IF EXISTS `ai_message`;
DROP TABLE IF EXISTS `learning_problem_attempt`;
DROP TABLE IF EXISTS `learning_session`;

DROP TABLE IF EXISTS `learning_content_body`;
DROP TABLE IF EXISTS `ai_generated_learning_content_body`;
DROP TABLE IF EXISTS `learning_content`;
DROP TABLE IF EXISTS `learning_content_unit_item`;
DROP TABLE IF EXISTS `learning_content_unit`;

DROP TABLE IF EXISTS `learning_source_document`;



DROP TABLE IF EXISTS `learning_coach`;
DROP TABLE IF EXISTS `learning_memo`;
DROP TABLE IF EXISTS `learning_content_reference`;
DROP TABLE IF EXISTS `user_learning_content`;
DROP TABLE IF EXISTS `document_scope_mapping`;
DROP TABLE IF EXISTS `document_chunk`;
DROP TABLE IF EXISTS `document_processing`;
DROP TABLE IF EXISTS `source_document`;
DROP TABLE IF EXISTS `document_processing_group`;
DROP TABLE IF EXISTS `extracted_content`;


DROP TABLE IF EXISTS `refresh_tokens`;

DROP TABLE IF EXISTS `workbook_item_result`;
DROP TABLE IF EXISTS `workbook_part_result`;
DROP TABLE IF EXISTS `workbook_answer`;
DROP TABLE IF EXISTS `workbook_attempt`;
DROP TABLE IF EXISTS `workbook_result`;
DROP TABLE IF EXISTS `workbook_item`;
DROP TABLE IF EXISTS `workbook`;
DROP TABLE IF EXISTS `exam_workbook_type`;
DROP TABLE IF EXISTS `workbook_type`;

DROP TABLE IF EXISTS `problem_available_usage_type`;
DROP TABLE IF EXISTS `problem_choice`;
DROP TABLE IF EXISTS `problem`;
DROP TABLE IF EXISTS `problem_review_detail`;
DROP TABLE IF EXISTS `problem_review`;
DROP TABLE IF EXISTS `ai_generated_problem_choice`;
DROP TABLE IF EXISTS `ai_generated_problem`;
DROP TABLE IF EXISTS `problem_set_generation_job`;
DROP TABLE IF EXISTS `policy_value`;
DROP TABLE IF EXISTS `policy`;
DROP TABLE IF EXISTS `exam_scope_node`;
DROP TABLE IF EXISTS `exam_part`;
DROP TABLE IF EXISTS `exam_version`;
DROP TABLE IF EXISTS `domain_type`;
DROP TABLE IF EXISTS `asset_file`;
DROP TABLE IF EXISTS `user_exam`;
DROP TABLE IF EXISTS `exam`;
DROP TABLE IF EXISTS `users`;

SET FOREIGN_KEY_CHECKS = 1;


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


CREATE TABLE `exam` (
                        `id`         BIGINT       NOT NULL AUTO_INCREMENT,
                        `code`       VARCHAR(50)  NOT NULL,
                        `name`       VARCHAR(100) NOT NULL,
                        `status`     VARCHAR(30)  NOT NULL DEFAULT 'PREPARING',
                        `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        `updated_at` DATETIME         NULL ON UPDATE CURRENT_TIMESTAMP,

                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_exam_code` (`code`)
);



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
                                `status`                   VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
                                `created_at`               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `updated_at`               DATETIME         NULL ON UPDATE CURRENT_TIMESTAMP,

                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_exam_id_version_no` (`exam_id`, `version_no`),

                                CONSTRAINT `fk_exam_version_exam_id`
                                    FOREIGN KEY (`exam_id`) REFERENCES `exam` (`id`)
                                        ON DELETE CASCADE
);


CREATE TABLE `exam_part` (
                             `id`                           BIGINT       NOT NULL AUTO_INCREMENT,
                             `exam_version_id`              BIGINT       NOT NULL,
                             `code`                         VARCHAR(50)  NOT NULL,
                             `name`                         VARCHAR(100) NOT NULL,
                             `default_question_count`       INT          NOT NULL,
                             `default_duration_seconds`     INT              NULL,
                             `total_score`                  INT          NOT NULL,
                             `is_subject_fail_target`       BOOLEAN      NOT NULL DEFAULT FALSE,
                             `subject_fail_threshold_score` INT              NULL,
                             `is_active`                    BOOLEAN      NOT NULL DEFAULT FALSE,
                             `display_order`                INT          NOT NULL,
                             `created_at`                   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             `updated_at`                   DATETIME         NULL ON UPDATE CURRENT_TIMESTAMP,

                             PRIMARY KEY (`id`),
                             UNIQUE KEY `uk_exam_version_id_code` (`exam_version_id`, `code`),

                             CONSTRAINT `fk_exam_part_exam_version_id`
                                 FOREIGN KEY (`exam_version_id`) REFERENCES `exam_version` (`id`)
                                     ON DELETE CASCADE
);



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

                                   INDEX `idx_scope_parent` (`parent_id`),
                                   INDEX `idx_scope_level` (`exam_version_id`, `depth`),
                                   INDEX `idx_scope_exam_part_id` (`exam_part_id`),

                                   CONSTRAINT `fk_exam_scope_node_exam_version_id`
                                       FOREIGN KEY (`exam_version_id`) REFERENCES `exam_version` (`id`)
                                           ON DELETE CASCADE,

                                   CONSTRAINT `fk_exam_scope_node_exam_part_id`
                                       FOREIGN KEY (`exam_part_id`) REFERENCES `exam_part` (`id`)
                                           ON DELETE CASCADE,

                                   CONSTRAINT `fk_exam_scope_node_parent_id`
                                       FOREIGN KEY (`parent_id`) REFERENCES `exam_scope_node` (`id`)
                                           ON DELETE SET NULL
);



CREATE TABLE `user_exam` (
                             `id`              BIGINT   NOT NULL AUTO_INCREMENT,
                             `user_id`         BIGINT   NOT NULL,
                             `exam_version_id` BIGINT   NOT NULL,
                             `is_hidden`       BOOLEAN  NOT NULL DEFAULT FALSE,
                             `last_studied_at` DATETIME     NULL,
                             `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             `updated_at`      DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,

                             PRIMARY KEY (`id`),
                             UNIQUE KEY `uk_user_exam_user_exam_version` (`user_id`, `exam_version_id`),

                             INDEX `idx_user_exam_user_id` (`user_id`),
                             INDEX `idx_user_exam_exam_version_id` (`exam_version_id`),
                             INDEX `idx_user_exam_user_hidden` (`user_id`, `is_hidden`),

                             CONSTRAINT `fk_user_exam_user_id`
                                 FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
                                     ON DELETE CASCADE,

                             CONSTRAINT `fk_user_exam_exam_version_id`
                                 FOREIGN KEY (`exam_version_id`) REFERENCES `exam_version` (`id`)
                                     ON DELETE CASCADE
);


CREATE TABLE `document_processing_group` (
                                             `id`                   BIGINT        NOT NULL AUTO_INCREMENT,
                                             `user_exam_id`         BIGINT        NOT NULL,
                                             `status`               VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
                                             `current_step`         VARCHAR(50)   NOT NULL DEFAULT 'UPLOAD_PENDING',
                                             `progress_rate`        INT           NOT NULL DEFAULT 0,
                                             `total_file_count`     INT           NOT NULL DEFAULT 0,
                                             `completed_file_count` INT           NOT NULL DEFAULT 0,
                                             `failed_file_count`    INT           NOT NULL DEFAULT 0,
                                             `error_message`        VARCHAR(1000)     NULL,
                                             `started_at`           DATETIME          NULL,
                                             `completed_at`         DATETIME          NULL,
                                             `created_at`           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             `updated_at`           DATETIME          NULL ON UPDATE CURRENT_TIMESTAMP,

                                             PRIMARY KEY (`id`),

                                             INDEX `idx_dpg_user_exam_id` (`user_exam_id`),
                                             INDEX `idx_dpg_status` (`status`),
                                             INDEX `idx_dpg_current_step` (`current_step`),
                                             INDEX `idx_dpg_created_at` (`created_at`),

                                             CONSTRAINT `fk_dpg_user_exam_id`
                                                 FOREIGN KEY (`user_exam_id`)
                                                     REFERENCES `user_exam` (`id`)
                                                     ON DELETE CASCADE,

                                             CONSTRAINT `chk_dpg_progress_rate`
                                                 CHECK (`progress_rate` BETWEEN 0 AND 100),

                                             CONSTRAINT `chk_dpg_total_file_count`
                                                 CHECK (`total_file_count` >= 0),

                                             CONSTRAINT `chk_dpg_completed_file_count`
                                                 CHECK (`completed_file_count` >= 0),

                                             CONSTRAINT `chk_dpg_failed_file_count`
                                                 CHECK (`failed_file_count` >= 0)
);


CREATE TABLE `asset_file` (
                              `id`          BIGINT       NOT NULL AUTO_INCREMENT,
                              `storage_key` VARCHAR(500) NOT NULL,
                              `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              `updated_at`  DATETIME         NULL ON UPDATE CURRENT_TIMESTAMP,

                              PRIMARY KEY (`id`)
);



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



CREATE TABLE `policy` (
                          `id`              BIGINT      NOT NULL AUTO_INCREMENT,
                          `exam_version_id` BIGINT      NOT NULL,
                          `domain_type_id`  BIGINT      NOT NULL,
                          `version_no`      VARCHAR(50) NOT NULL,
                          `is_active`       BOOLEAN     NOT NULL,
                          `created_at`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          `updated_at`      DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,

                          PRIMARY KEY (`id`),

                          INDEX `idx_policy_exam_version_id` (`exam_version_id`),
                          INDEX `idx_policy_domain_type_id` (`domain_type_id`),

                          CONSTRAINT `fk_policy_exam_version_id`
                              FOREIGN KEY (`exam_version_id`) REFERENCES `exam_version` (`id`)
                                  ON DELETE CASCADE,

                          CONSTRAINT `fk_policy_domain_type_id`
                              FOREIGN KEY (`domain_type_id`) REFERENCES `domain_type` (`id`)
                                  ON DELETE CASCADE
);


CREATE TABLE `policy_value` (
                                `id`         BIGINT       NOT NULL AUTO_INCREMENT,
                                `policy_id`  BIGINT       NOT NULL,
                                `value_key`  VARCHAR(100) NOT NULL,
                                `value_type` VARCHAR(30)  NOT NULL,
                                `value`      TEXT         NOT NULL,
                                `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `updated_at` DATETIME         NULL ON UPDATE CURRENT_TIMESTAMP,

                                PRIMARY KEY (`id`),

                                INDEX `idx_policy_value_policy_id` (`policy_id`),

                                CONSTRAINT `fk_policy_value_policy_id`
                                    FOREIGN KEY (`policy_id`) REFERENCES `policy` (`id`)
                                        ON DELETE CASCADE
);



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

                                              INDEX `idx_psgj_user_id` (`user_id`),
                                              INDEX `idx_psgj_exam_version_id` (`exam_version_id`),
                                              INDEX `idx_psgj_exam_scope_node_id` (`exam_scope_node_id`),
                                              INDEX `idx_psgj_domain_type_id` (`domain_type_id`),

                                              CONSTRAINT `fk_psgj_user_id`
                                                  FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
                                                      ON DELETE CASCADE,

                                              CONSTRAINT `fk_psgj_exam_version_id`
                                                  FOREIGN KEY (`exam_version_id`) REFERENCES `exam_version` (`id`)
                                                      ON DELETE CASCADE,

                                              CONSTRAINT `fk_psgj_exam_scope_node_id`
                                                  FOREIGN KEY (`exam_scope_node_id`) REFERENCES `exam_scope_node` (`id`)
                                                      ON DELETE SET NULL,

                                              CONSTRAINT `fk_psgj_domain_type_id`
                                                  FOREIGN KEY (`domain_type_id`) REFERENCES `domain_type` (`id`)
                                                      ON DELETE SET NULL
);



CREATE TABLE `ai_generated_problem` (
                                        `id`                            BIGINT      NOT NULL AUTO_INCREMENT,
                                        `problem_set_generation_job_id` BIGINT      NOT NULL,
                                        `exam_id`                       BIGINT      NOT NULL,
                                        `exam_version_id`               BIGINT      NOT NULL,
                                        `exam_scope_node_id`            BIGINT      NOT NULL,
                                        `expression_type`               VARCHAR(50) NOT NULL,
                                        `difficulty`                    VARCHAR(20) NOT NULL,
                                        `question_content_json`         JSON        NOT NULL,
                                        `explanation_json`              JSON        NOT NULL,
                                        `answer_type`                   VARCHAR(30) NOT NULL,
                                        `answer_json`                   JSON        NOT NULL,
                                        `choice_type`                   VARCHAR(30) NOT NULL,
                                        `review_status`                 VARCHAR(30) NOT NULL,
                                        `created_at`                    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        `updated_at`                    DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,

                                        PRIMARY KEY (`id`),

                                        INDEX `idx_agp_job_id` (`problem_set_generation_job_id`),
                                        INDEX `idx_agp_exam_id` (`exam_id`),
                                        INDEX `idx_agp_exam_version_id` (`exam_version_id`),
                                        INDEX `idx_agp_exam_scope_node_id` (`exam_scope_node_id`),

                                        CONSTRAINT `fk_agp_problem_set_generation_job_id`
                                            FOREIGN KEY (`problem_set_generation_job_id`) REFERENCES `problem_set_generation_job` (`id`)
                                                ON DELETE CASCADE,

                                        CONSTRAINT `fk_agp_exam_id`
                                            FOREIGN KEY (`exam_id`) REFERENCES `exam` (`id`)
                                                ON DELETE CASCADE,

                                        CONSTRAINT `fk_agp_exam_version_id`
                                            FOREIGN KEY (`exam_version_id`) REFERENCES `exam_version` (`id`)
                                                ON DELETE CASCADE,

                                        CONSTRAINT `fk_agp_exam_scope_node_id`
                                            FOREIGN KEY (`exam_scope_node_id`) REFERENCES `exam_scope_node` (`id`)
                                                ON DELETE CASCADE
);


CREATE TABLE `ai_generated_problem_choice` (
                                               `id`                      BIGINT   NOT NULL AUTO_INCREMENT,
                                               `ai_generated_problem_id` BIGINT   NOT NULL,
                                               `choice_no`               INT      NOT NULL,
                                               `choice_content_json`     JSON     NOT NULL,
                                               `created_at`              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                               `updated_at`              DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,

                                               PRIMARY KEY (`id`),
                                               UNIQUE KEY `uk_agp_id_choice_no` (`ai_generated_problem_id`, `choice_no`),

                                               CONSTRAINT `fk_agpc_ai_generated_problem_id`
                                                   FOREIGN KEY (`ai_generated_problem_id`) REFERENCES `ai_generated_problem` (`id`)
                                                       ON DELETE CASCADE
);


CREATE TABLE `problem_review` (
                                  `id`                      BIGINT       NOT NULL AUTO_INCREMENT,
                                  `ai_generated_problem_id` BIGINT       NOT NULL,
                                  `review_status`           VARCHAR(30)  NOT NULL,
                                  `fail_reason`             VARCHAR(500)     NULL,
                                  `reviewed_at`             DATETIME         NULL,

                                  PRIMARY KEY (`id`),

                                  INDEX `idx_problem_review_agp_id` (`ai_generated_problem_id`),

                                  CONSTRAINT `fk_problem_review_agp_id`
                                      FOREIGN KEY (`ai_generated_problem_id`) REFERENCES `ai_generated_problem` (`id`)
                                          ON DELETE CASCADE
);


CREATE TABLE `problem_review_detail` (
                                         `id`                BIGINT       NOT NULL AUTO_INCREMENT,
                                         `problem_review_id` BIGINT       NOT NULL,
                                         `policy_id`         BIGINT       NOT NULL,
                                         `check_result`      VARCHAR(20)  NOT NULL,
                                         `message`           VARCHAR(500)     NULL,
                                         `checked_at`        DATETIME         NULL,

                                         PRIMARY KEY (`id`),

                                         INDEX `idx_prd_problem_review_id` (`problem_review_id`),
                                         INDEX `idx_prd_policy_id` (`policy_id`),

                                         CONSTRAINT `fk_prd_problem_review_id`
                                             FOREIGN KEY (`problem_review_id`) REFERENCES `problem_review` (`id`)
                                                 ON DELETE CASCADE,

                                         CONSTRAINT `fk_prd_policy_id`
                                             FOREIGN KEY (`policy_id`) REFERENCES `policy` (`id`)
                                                 ON DELETE CASCADE
);



CREATE TABLE `problem` (
                           `id`                       BIGINT       NOT NULL AUTO_INCREMENT,
                           `exam_id`                  BIGINT       NOT NULL,
                           `exam_version_id`          BIGINT       NOT NULL,
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
                           CONSTRAINT `fk_problem_exam_id` FOREIGN KEY (`exam_id`) REFERENCES `exam` (`id`) ON DELETE CASCADE,
                           CONSTRAINT `fk_problem_exam_version_id` FOREIGN KEY (`exam_version_id`) REFERENCES `exam_version` (`id`) ON DELETE CASCADE,
                           CONSTRAINT `fk_problem_exam_scope_node_id` FOREIGN KEY (`exam_scope_node_id`) REFERENCES `exam_scope_node` (`id`) ON DELETE SET NULL,
                           CONSTRAINT `fk_problem_ai_generated_problem_id` FOREIGN KEY (`ai_generated_problem_id`) REFERENCES `ai_generated_problem` (`id`) ON DELETE SET NULL
);


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


CREATE TABLE `source_document` (
                                   `id`                   BIGINT       NOT NULL AUTO_INCREMENT,
                                   `processing_group_id`  BIGINT       NOT NULL,
                                   `original_file_name`   VARCHAR(255) NOT NULL,
                                   `stored_file_name`     VARCHAR(255) NOT NULL,
                                   `storage_key`          VARCHAR(500) NOT NULL,
                                   `file_extension`       VARCHAR(20)  NOT NULL,
                                   `mime_type`            VARCHAR(100) NOT NULL,
                                   `file_size`            BIGINT       NOT NULL,
                                   `upload_status`        VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
                                   `upload_error_message` VARCHAR(1000)     NULL,
                                   `is_active`            BOOLEAN      NOT NULL DEFAULT TRUE,
                                   `created_at`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   `updated_at`           DATETIME         NULL ON UPDATE CURRENT_TIMESTAMP,

                                   PRIMARY KEY (`id`),

                                   INDEX `idx_source_document_processing_group_id`
                                       (`processing_group_id`),

                                   INDEX `idx_source_document_upload_status`
                                       (`upload_status`),

                                   INDEX `idx_source_document_storage_key`
                                       (`storage_key`),

                                   UNIQUE KEY `uk_source_document_storage_key`
                                       (`storage_key`),

                                   CONSTRAINT `fk_source_document_processing_group_id`
                                       FOREIGN KEY (`processing_group_id`)
                                           REFERENCES `document_processing_group` (`id`)
                                           ON DELETE CASCADE,

                                   CONSTRAINT `chk_source_document_file_size`
                                       CHECK (`file_size` > 0)
);



CREATE TABLE `document_processing` (
                                       `id`                  BIGINT        NOT NULL AUTO_INCREMENT,
                                       `processing_group_id` BIGINT        NOT NULL,
                                       `source_document_id`  BIGINT        NOT NULL,
                                       `status`              VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
                                       `error_message`       VARCHAR(1000)     NULL,
                                       `started_at`          DATETIME          NULL,
                                       `completed_at`        DATETIME          NULL,
                                       `created_at`          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       `updated_at`          DATETIME          NULL ON UPDATE CURRENT_TIMESTAMP,

                                       PRIMARY KEY (`id`),

                                       UNIQUE KEY `uk_document_processing_group_document`
                                           (`processing_group_id`, `source_document_id`),

                                       INDEX `idx_document_processing_group_id`
                                           (`processing_group_id`),

                                       INDEX `idx_document_processing_source_document_id`
                                           (`source_document_id`),

                                       INDEX `idx_document_processing_status`
                                           (`status`),

                                       CONSTRAINT `fk_document_processing_group_id`
                                           FOREIGN KEY (`processing_group_id`)
                                               REFERENCES `document_processing_group` (`id`)
                                               ON DELETE CASCADE,

                                       CONSTRAINT `fk_document_processing_source_document_id`
                                           FOREIGN KEY (`source_document_id`)
                                               REFERENCES `source_document` (`id`)
                                               ON DELETE CASCADE
);

CREATE TABLE `document_chunk` (
                                  `id`                 BIGINT       NOT NULL AUTO_INCREMENT,
                                  `source_document_id` BIGINT       NOT NULL,
                                  `chunk_order`        INT          NOT NULL,
                                  `page_no`            INT              NULL,
                                  `section_title`      VARCHAR(255)     NULL,
                                  `content_type`       VARCHAR(30)  NOT NULL DEFAULT 'TEXT',
                                  `content_text`       LONGTEXT     NOT NULL,
                                  `raw_text`           LONGTEXT         NULL,
                                  `summary`            TEXT             NULL,
                                  `keywords_json`      JSON             NULL,
                                  `structure_json`     JSON             NULL,
                                  `token_count`        INT              NULL,
                                  `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  `updated_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                      ON UPDATE CURRENT_TIMESTAMP,

                                  PRIMARY KEY (`id`),

                                  UNIQUE KEY `uk_document_chunk_source_order`
                                      (`source_document_id`, `chunk_order`),

                                  INDEX `idx_document_chunk_page_no`
                                      (`source_document_id`, `page_no`),

                                  INDEX `idx_document_chunk_content_type`
                                      (`content_type`),

                                  CONSTRAINT `fk_document_chunk_source_document`
                                      FOREIGN KEY (`source_document_id`)
                                          REFERENCES `source_document` (`id`)
                                          ON DELETE CASCADE,

                                  CONSTRAINT `chk_document_chunk_order`
                                      CHECK (`chunk_order` >= 1),

                                  CONSTRAINT `chk_document_chunk_page_no`
                                      CHECK (`page_no` IS NULL OR `page_no` >= 1),

                                  CONSTRAINT `chk_document_chunk_token_count`
                                      CHECK (`token_count` IS NULL OR `token_count` >= 0)
);


CREATE TABLE `document_scope_mapping` (
                                          `id`                 BIGINT        NOT NULL AUTO_INCREMENT,
                                          `document_chunk_id`  BIGINT        NOT NULL,
                                          `exam_scope_node_id` BIGINT        NOT NULL,
                                          `confidence_score`   DECIMAL(5,4)  NOT NULL,
                                          `mapping_reason`     VARCHAR(1000) NULL,
                                          `created_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                          `updated_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                                              ON UPDATE CURRENT_TIMESTAMP,

                                          PRIMARY KEY (`id`),

                                          UNIQUE KEY `uk_dsm_chunk_scope`
                                              (`document_chunk_id`, `exam_scope_node_id`),

                                          CONSTRAINT `fk_dsm_document_chunk_id`
                                              FOREIGN KEY (`document_chunk_id`)
                                                  REFERENCES `document_chunk` (`id`)
                                                  ON DELETE CASCADE,

                                          CONSTRAINT `fk_dsm_exam_scope_node_id`
                                              FOREIGN KEY (`exam_scope_node_id`)
                                                  REFERENCES `exam_scope_node` (`id`)
                                                  ON DELETE CASCADE,

                                          CONSTRAINT `chk_dsm_confidence_score`
                                              CHECK (
                                                  `confidence_score` >= 0
                                                      AND `confidence_score` <= 1
                                                  )
);


CREATE TABLE `user_learning_content` (
                                         `id`                 BIGINT       NOT NULL AUTO_INCREMENT,
                                         `user_exam_id`       BIGINT       NOT NULL,
                                         `exam_scope_node_id` BIGINT       NOT NULL,
                                         `title`              VARCHAR(200) NOT NULL,
                                         `content`            LONGTEXT         NULL,
                                         `keywords_json`      JSON             NULL,
                                         `status`             VARCHAR(30)  NOT NULL DEFAULT 'NOT_MAPPED',
                                         `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         `updated_at`         DATETIME         NULL ON UPDATE CURRENT_TIMESTAMP,

                                         PRIMARY KEY (`id`),
                                         UNIQUE KEY `uk_ulc_user_exam_scope` (`user_exam_id`, `exam_scope_node_id`),

                                         INDEX `idx_ulc_user_exam_id` (`user_exam_id`),
                                         INDEX `idx_ulc_exam_scope_node_id` (`exam_scope_node_id`),
                                         INDEX `idx_ulc_status` (`status`),

                                         CONSTRAINT `fk_ulc_user_exam_id`
                                             FOREIGN KEY (`user_exam_id`) REFERENCES `user_exam` (`id`)
                                                 ON DELETE CASCADE,

                                         CONSTRAINT `fk_ulc_exam_scope_node_id`
                                             FOREIGN KEY (`exam_scope_node_id`) REFERENCES `exam_scope_node` (`id`)
                                                 ON DELETE CASCADE
);


CREATE TABLE `learning_content_reference` (
                                              `id`                       BIGINT   NOT NULL AUTO_INCREMENT,
                                              `user_learning_content_id` BIGINT   NOT NULL,
                                              `document_chunk_id`        BIGINT   NOT NULL,
                                              `page_no`                  INT          NULL,
                                              `snippet`                  TEXT         NULL,
                                              `display_order`            INT      NOT NULL DEFAULT 1,
                                              `created_at`               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              `updated_at`               DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,

                                              PRIMARY KEY (`id`),

                                              INDEX `idx_lcr_user_learning_content_id`
                                                  (`user_learning_content_id`),

                                              INDEX `idx_lcr_document_chunk_id`
                                                  (`document_chunk_id`),

                                              INDEX `idx_lcr_content_order`
                                                  (`user_learning_content_id`, `display_order`),

                                              CONSTRAINT `fk_lcr_user_learning_content_id`
                                                  FOREIGN KEY (`user_learning_content_id`)
                                                      REFERENCES `user_learning_content` (`id`)
                                                      ON DELETE CASCADE,

                                              CONSTRAINT `fk_lcr_document_chunk_id`
                                                  FOREIGN KEY (`document_chunk_id`)
                                                      REFERENCES `document_chunk` (`id`)
                                                      ON DELETE CASCADE,

                                              CONSTRAINT `chk_lcr_page_no`
                                                  CHECK (`page_no` IS NULL OR `page_no` >= 1),

                                              CONSTRAINT `chk_lcr_display_order`
                                                  CHECK (`display_order` >= 1)
);



CREATE TABLE `learning_memo` (
                                 `id`                       BIGINT   NOT NULL AUTO_INCREMENT,
                                 `user_id`                  BIGINT   NOT NULL,
                                 `user_learning_content_id` BIGINT   NOT NULL,
                                 `memo`                     LONGTEXT NOT NULL,
                                 `created_at`               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 `updated_at`               DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,

                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_learning_memo_user_content` (`user_id`, `user_learning_content_id`),

                                 INDEX `idx_learning_memo_user_id` (`user_id`),
                                 INDEX `idx_learning_memo_user_learning_content_id` (`user_learning_content_id`),

                                 CONSTRAINT `fk_learning_memo_user_id`
                                     FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
                                         ON DELETE CASCADE,

                                 CONSTRAINT `fk_learning_memo_user_learning_content_id`
                                     FOREIGN KEY (`user_learning_content_id`) REFERENCES `user_learning_content` (`id`)
                                         ON DELETE CASCADE
);


CREATE TABLE `learning_coach` (
                                  `id`                       BIGINT       NOT NULL AUTO_INCREMENT,
                                  `user_id`                  BIGINT       NOT NULL,
                                  `user_learning_content_id` BIGINT       NOT NULL,
                                  `check_type`               VARCHAR(30)  NOT NULL,
                                  `session_status`           VARCHAR(30)  NOT NULL DEFAULT 'READY',
                                  `result_status`            VARCHAR(30)      NULL,
                                  `question_text`            LONGTEXT         NULL,
                                  `question_json`            JSON             NULL,
                                  `user_answer`              LONGTEXT         NULL,
                                  `feedback`                 LONGTEXT         NULL,
                                  `score`                    INT              NULL,
                                  `created_at`               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  `updated_at`               DATETIME         NULL ON UPDATE CURRENT_TIMESTAMP,

                                  PRIMARY KEY (`id`),

                                  INDEX `idx_learning_coach_user_id` (`user_id`),
                                  INDEX `idx_learning_coach_user_learning_content_id` (`user_learning_content_id`),
                                  INDEX `idx_learning_coach_check_type` (`check_type`),
                                  INDEX `idx_learning_coach_session_status` (`session_status`),
                                  INDEX `idx_learning_coach_result_status` (`result_status`),

                                  CONSTRAINT `fk_learning_coach_user_id`
                                      FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
                                          ON DELETE CASCADE,

                                  CONSTRAINT `fk_learning_coach_user_learning_content_id`
                                      FOREIGN KEY (`user_learning_content_id`) REFERENCES `user_learning_content` (`id`)
                                          ON DELETE CASCADE,

                                  CONSTRAINT `chk_learning_coach_score`
                                      CHECK (`score` IS NULL OR (`score` >= 0 AND `score` <= 100))
);


CREATE TABLE `refresh_tokens` (
                                  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
                                  `user_id`    BIGINT       NOT NULL,
                                  `token`      VARCHAR(500) NOT NULL,
                                  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  `updated_at` DATETIME         NULL ON UPDATE CURRENT_TIMESTAMP,

                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uk_refresh_tokens_user_id` (`user_id`),

                                  CONSTRAINT `fk_refresh_tokens_user`
                                      FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
                                          ON DELETE CASCADE
);



/* =================================================== */

CREATE TABLE workbook_type (
                               id BIGINT NOT NULL AUTO_INCREMENT,
                               code VARCHAR(50) NOT NULL,
                               name VARCHAR(50) NOT NULL,
                               display_order INT NOT NULL DEFAULT 0,
                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,

                               PRIMARY KEY (id),
                               UNIQUE KEY uk_workbook_type_code (code)
);

CREATE TABLE exam_workbook_type (
                                    id BIGINT NOT NULL AUTO_INCREMENT,
                                    exam_id BIGINT NOT NULL,
                                    workbook_type_id BIGINT NOT NULL,
                                    is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,

                                    PRIMARY KEY (id),
                                    UNIQUE KEY uk_exam_workbook_type_exam_id_workbook_type_id (exam_id, workbook_type_id),

                                    CONSTRAINT fk_exam_workbook_type_exam_id
                                        FOREIGN KEY (exam_id)
                                            REFERENCES exam (id)
                                            ON DELETE CASCADE,

                                    CONSTRAINT fk_exam_workbook_type_workbook_type_id
                                        FOREIGN KEY (workbook_type_id)
                                            REFERENCES workbook_type (id)
                                            ON DELETE CASCADE
);


CREATE TABLE workbook (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          exam_workbook_type_id BIGINT NOT NULL,
                          no INT NOT NULL,
                          exam_year INT NULL,
                          total_question_count INT NOT NULL,
                          time_limit INT NULL,
                          status VARCHAR(30) NOT NULL,
                          created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,

                          PRIMARY KEY (id),
                          UNIQUE KEY uk_workbook_exam_version_type_id_no (exam_workbook_type_id, no),

                          CONSTRAINT fk_workbook_exam_workbook_type_id
                              FOREIGN KEY (exam_workbook_type_id)
                                  REFERENCES exam_workbook_type (id)
                                  ON DELETE CASCADE
);


CREATE TABLE workbook_item (
                               id BIGINT NOT NULL AUTO_INCREMENT,
                               workbook_id BIGINT NOT NULL,
                               problem_id BIGINT NOT NULL,
                               item_no INT NOT NULL,
                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               PRIMARY KEY (id),
                               UNIQUE KEY uk_workbook_item_workbook_id_problem_id (workbook_id, problem_id),
                               UNIQUE KEY uk_workbook_item_workbook_id_item_no (workbook_id, item_no),

                               CONSTRAINT fk_workbook_item_workbook_id
                                   FOREIGN KEY (workbook_id)
                                       REFERENCES workbook (id)
                                       ON DELETE CASCADE,

                               CONSTRAINT fk_workbook_item_problem_id
                                   FOREIGN KEY (problem_id)
                                       REFERENCES problem (id)
                                       ON DELETE CASCADE
);

CREATE TABLE workbook_result (
                                 id BIGINT NOT NULL AUTO_INCREMENT,
                                 is_passed BOOLEAN NOT NULL,
                                 fail_reason_code VARCHAR(100) NULL,
                                 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,

                                 PRIMARY KEY (id)

);

CREATE TABLE workbook_attempt (
                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                  user_id BIGINT NOT NULL,
                                  workbook_id BIGINT NOT NULL,
                                  workbook_result_id BIGINT  NULL,
                                  active_workbook_id BIGINT NULL,
                                  status VARCHAR(30) NOT NULL,
                                  due_at DATETIME NULL,
                                  submitted_at DATETIME NULL,
                                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,

                                  PRIMARY KEY (id),
                                  UNIQUE KEY uk_workbook_attempt_workbook_result_id (workbook_result_id),
                                  UNIQUE KEY uk_workbook_attempt_user_id_active_workbook_id (user_id, active_workbook_id),

                                  CONSTRAINT fk_workbook_attempt_user_id
                                      FOREIGN KEY (user_id)
                                          REFERENCES `users` (id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT fk_workbook_attempt_workbook_result_id
                                      FOREIGN KEY (workbook_result_id)
                                          REFERENCES workbook_result (id)
                                          ON DELETE SET NULL,

                                  CONSTRAINT fk_workbook_attempt_workbook_id
                                      FOREIGN KEY (workbook_id)
                                          REFERENCES workbook (id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT fk_workbook_attempt_active_workbook_id
                                      FOREIGN KEY (active_workbook_id)
                                          REFERENCES workbook (id)
                                          ON DELETE SET NULL
);


CREATE TABLE workbook_answer (
                                 id BIGINT NOT NULL AUTO_INCREMENT,
                                 workbook_attempt_id BIGINT NOT NULL,
                                 workbook_item_id BIGINT NOT NULL,
                                 problem_choice_id BIGINT NULL,
                                 answer_content_json JSON NULL,
                                 is_answered BOOLEAN NOT NULL DEFAULT FALSE,
                                 is_marked BOOLEAN NOT NULL DEFAULT FALSE,
                                 is_uncertain BOOLEAN NOT NULL DEFAULT FALSE,
                                 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,

                                 PRIMARY KEY (id),
                                 UNIQUE KEY uk_workbook_answer_attempt_item (workbook_attempt_id, workbook_item_id),

                                 CONSTRAINT fk_workbook_answer_attempt_id
                                     FOREIGN KEY (workbook_attempt_id)
                                         REFERENCES workbook_attempt (id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT fk_workbook_answer_item_id
                                     FOREIGN KEY (workbook_item_id)
                                         REFERENCES workbook_item (id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT fk_workbook_answer_problem_choice_id
                                     FOREIGN KEY (problem_choice_id)
                                         REFERENCES problem_choice (id)
                                         ON DELETE SET NULL
);


CREATE TABLE workbook_part_result (
                                      id BIGINT NOT NULL AUTO_INCREMENT,
                                      workbook_result_id BIGINT NOT NULL,
                                      exam_part_id BIGINT NOT NULL,
                                      problem_count INT NOT NULL,
                                      correct_count INT NOT NULL,
                                      answered_count INT NOT NULL,
                                      created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,

                                      PRIMARY KEY (id),
                                      UNIQUE KEY uk_workbook_part_result_result_part (workbook_result_id, exam_part_id),

                                      CONSTRAINT fk_workbook_part_result_result_id
                                          FOREIGN KEY (workbook_result_id)
                                              REFERENCES workbook_result (id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT fk_workbook_part_result_exam_part_id
                                          FOREIGN KEY (exam_part_id)
                                              REFERENCES exam_part (id)
                                              ON DELETE CASCADE
);

CREATE TABLE workbook_item_result (
                                      id BIGINT NOT NULL AUTO_INCREMENT,
                                      workbook_result_id BIGINT NOT NULL,
                                      workbook_item_id BIGINT NOT NULL,
                                      is_correct BOOLEAN NOT NULL,
                                      score DECIMAL(5,2) NOT NULL,
                                      created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,

                                      PRIMARY KEY (id),
                                      UNIQUE KEY uk_workbook_item_result_result_item (workbook_result_id, workbook_item_id),

                                      CONSTRAINT fk_workbook_item_result_result_id
                                          FOREIGN KEY (workbook_result_id)
                                              REFERENCES workbook_result (id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT fk_workbook_item_result_workbook_item_id
                                          FOREIGN KEY (workbook_item_id)
                                              REFERENCES workbook_item (id)
                                              ON DELETE CASCADE
);