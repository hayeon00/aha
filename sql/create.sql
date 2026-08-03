SET FOREIGN_KEY_CHECKS = 0;



DROP TABLE IF EXISTS `learning_content_reference`;
DROP TABLE IF EXISTS `user_document_concept`;
DROP TABLE IF EXISTS `user_learning_content`;
DROP TABLE IF EXISTS `document_scope_mapping`;
DROP TABLE IF EXISTS `document_chunk`;
DROP TABLE IF EXISTS `document_processing`;
DROP TABLE IF EXISTS `source_document`;
DROP TABLE IF EXISTS `document_processing_group`;
DROP TABLE IF EXISTS `document_chunk_embedding`;
DROP TABLE IF EXISTS `exam_scope_node_embedding`;

DROP TABLE IF EXISTS `oauth_authorization_code`;
DROP TABLE IF EXISTS `refresh_tokens`;
DROP TABLE IF EXISTS `social_accounts`;
DROP TABLE IF EXISTS `user_answer`;
DROP TABLE IF EXISTS `workbook_attempt`;
DROP TABLE IF EXISTS `workbook_result`;
DROP TABLE IF EXISTS `workbook_item`;
DROP TABLE IF EXISTS `past_exam_workbook`;
DROP TABLE IF EXISTS `workbook`;
DROP TABLE IF EXISTS `exam_workbook_type`;
DROP TABLE IF EXISTS `workbook_type`;

DROP TABLE IF EXISTS `problem_choice`;
DROP TABLE IF EXISTS `problem`;

DROP TABLE IF EXISTS `exam_scope_node`;
DROP TABLE IF EXISTS `exam_part`;
DROP TABLE IF EXISTS `exam_version`;
DROP TABLE IF EXISTS `user_exam`;
DROP TABLE IF EXISTS `exam`;
DROP TABLE IF EXISTS `users`;

DROP TABLE IF EXISTS `user_answer`;
DROP TABLE IF EXISTS `past_paper_attempt`;
DROP TABLE IF EXISTS `past_paper_item`;
DROP TABLE IF EXISTS `problem_choice`;
DROP TABLE IF EXISTS `past_paper`;
DROP TABLE IF EXISTS `problem`;

DROP TABLE IF EXISTS `active_study_room_participation`;
DROP TABLE IF EXISTS `study_room_member`;
DROP TABLE IF EXISTS `study_room`;

SET FOREIGN_KEY_CHECKS = 1;


CREATE TABLE `users`
(
    `id`                        BIGINT       NOT NULL AUTO_INCREMENT,
    `email`                     VARCHAR(255) NOT NULL,
    `password`                  VARCHAR(255) NULL,
    `name`                      VARCHAR(50)  NOT NULL,
    `nickname`                  VARCHAR(50)  NOT NULL,
    `role`                      VARCHAR(20)  NOT NULL DEFAULT 'USER',
    `status`                    VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    `last_login_at`             DATETIME     NULL,
    `is_email_verified`         BOOLEAN      NOT NULL DEFAULT FALSE,
    `profile_image_url`         VARCHAR(500) NULL,
    `exam_onboarding_completed` BOOLEAN      NOT NULL DEFAULT FALSE,
    `created_at`                DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`                DATETIME     NULL     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_email` (`email`),
    UNIQUE KEY `uk_user_nickname` (`nickname`),

    CONSTRAINT `chk_user_role`
        CHECK (`role` IN ('USER', 'ADMIN')),
    CONSTRAINT `chk_user_status`
        CHECK (`status` IN ('ACTIVE', 'INACTIVE', 'WITHDRAWN', 'SUSPENDED'))
);


CREATE TABLE `social_accounts`
(
    `id`                BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`           BIGINT       NOT NULL,
    `provider`          VARCHAR(20)  NOT NULL,
    `provider_id`       VARCHAR(255) NOT NULL,
    `provider_email`    VARCHAR(100) NULL,
    `provider_name`     VARCHAR(100) NULL,
    `profile_image_url` VARCHAR(500) NULL,
    `last_login_at`     DATETIME     NULL,
    `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME     NULL     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),

    UNIQUE KEY `uk_social_account_provider_id`
        (`provider`, `provider_id`),
    UNIQUE KEY `uk_social_account_user_provider`
        (`user_id`, `provider`),

    CONSTRAINT `fk_social_account_user`
        FOREIGN KEY (`user_id`)
            REFERENCES `users` (`id`)
            ON DELETE CASCADE,

    CONSTRAINT `chk_social_account_provider`
        CHECK (`provider` IN ('KAKAO', 'GOOGLE'))
);


CREATE TABLE `oauth_authorization_code`
(
    `id`              BIGINT      NOT NULL AUTO_INCREMENT,
    `code_hash`       VARCHAR(64) NOT NULL,
    `session_id_hash` VARCHAR(64) NOT NULL,
    `user_id`         BIGINT      NOT NULL,
    `expires_at`      DATETIME(6) NOT NULL,
    `used_at`         DATETIME(6) NULL,
    `created_at`      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_oauth_authorization_code_hash` (`code_hash`),
    INDEX `idx_oauth_authorization_code_user_id` (`user_id`),
    INDEX `idx_oauth_authorization_code_expires_at` (`expires_at`),

    CONSTRAINT `fk_oauth_authorization_code_user`
        FOREIGN KEY (`user_id`)
            REFERENCES `users` (`id`)
            ON DELETE CASCADE
);


CREATE TABLE `exam`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `code`       VARCHAR(50)  NOT NULL,
    `name`       VARCHAR(100) NOT NULL,
    `status`     VARCHAR(30)  NOT NULL DEFAULT 'PREPARING',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_exam_code` (`code`)
);



CREATE TABLE `exam_version`
(
    `id`                       BIGINT       NOT NULL AUTO_INCREMENT,
    `exam_id`                  BIGINT       NOT NULL,
    `version_no`               INT          NOT NULL,
    `version_name`             VARCHAR(100) NOT NULL,
    `default_question_count`   INT          NOT NULL,
    `duration_type`            VARCHAR(50)  NOT NULL,
    `default_duration_seconds` INT          NULL,
    `total_score`              INT          NOT NULL,
    `passing_rule_type`        VARCHAR(50)  NOT NULL,
    `passing_score`            INT          NOT NULL,
    `has_subject_fail_rule`    BOOLEAN      NOT NULL DEFAULT FALSE,
    `subject_fail_threshold`   INT          NULL,
    `status`                   VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    `created_at`               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`               DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_exam_id_version_no` (`exam_id`, `version_no`),

    CONSTRAINT `fk_exam_version_exam_id`
        FOREIGN KEY (`exam_id`) REFERENCES `exam` (`id`)
            ON DELETE CASCADE
);


CREATE TABLE `exam_part`
(
    `id`                           BIGINT       NOT NULL AUTO_INCREMENT,
    `exam_version_id`              BIGINT       NOT NULL,
    `code`                         VARCHAR(50)  NOT NULL,
    `name`                         VARCHAR(100) NOT NULL,
    `default_question_count`       INT          NOT NULL,
    `default_duration_seconds`     INT          NULL,
    `total_score`                  INT          NOT NULL,
    `is_subject_fail_target`       BOOLEAN      NOT NULL DEFAULT FALSE,
    `subject_fail_threshold_score` INT          NULL,
    `is_active`                    BOOLEAN      NOT NULL DEFAULT FALSE,
    `display_order`                INT          NOT NULL,
    `created_at`                   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`                   DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_exam_version_id_code` (`exam_version_id`, `code`),

    CONSTRAINT `fk_exam_part_exam_version_id`
        FOREIGN KEY (`exam_version_id`) REFERENCES `exam_version` (`id`)
            ON DELETE CASCADE
);

CREATE TABLE `exam_scope_node`
(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `exam_version_id` BIGINT       NOT NULL,
    `exam_part_id`    BIGINT       NOT NULL,
    `parent_id`       BIGINT       NULL,
    `code`            VARCHAR(100) NOT NULL,
    `node_type`       VARCHAR(30)  NOT NULL,
    `depth`           INT          NOT NULL,
    `title`           VARCHAR(200) NOT NULL,
    `description`     TEXT         NULL,
    `keywords_json`   JSON         NULL,
    `is_leaf`         BOOLEAN      NOT NULL DEFAULT FALSE,
    `is_active`       BOOLEAN      NOT NULL DEFAULT TRUE,
    `display_order`   INT          NOT NULL DEFAULT 0,
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),

    UNIQUE KEY `uk_exam_scope_node_version_code`
        (`exam_version_id`, `code`),
    UNIQUE KEY `uk_exam_scope_node_parent_order`
        (`exam_version_id`, `parent_id`, `display_order`),
    UNIQUE KEY `uk_exam_scope_node_parent_title`
        (`exam_version_id`, `parent_id`, `title`),
    INDEX `idx_scope_node_exam_version`
        (`exam_version_id`),
    INDEX `idx_scope_node_exam_part`
        (`exam_part_id`),
    INDEX `idx_scope_node_parent`
        (`parent_id`),
    INDEX `idx_scope_node_depth`
        (`exam_version_id`, `depth`),
    INDEX `idx_scope_node_type`
        (`node_type`),
    INDEX `idx_scope_node_active`
        (`is_active`),

    CONSTRAINT `fk_scope_node_exam_version`
        FOREIGN KEY (`exam_version_id`)
            REFERENCES `exam_version` (`id`)
            ON DELETE CASCADE,
    CONSTRAINT `fk_scope_node_exam_part`
        FOREIGN KEY (`exam_part_id`)
            REFERENCES `exam_part` (`id`)
            ON DELETE CASCADE,
    CONSTRAINT `fk_scope_node_parent`
        FOREIGN KEY (`parent_id`)
            REFERENCES `exam_scope_node` (`id`)
            ON DELETE SET NULL,
    CONSTRAINT `chk_scope_node_depth`
        CHECK (`depth` >= 0),
    CONSTRAINT `chk_scope_node_display_order`
        CHECK (`display_order` >= 0)
);

CREATE TABLE `exam_scope_node_embedding`
(
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT,
    `exam_scope_node_id`  BIGINT       NOT NULL,
    `embedding_model`     VARCHAR(100) NOT NULL,
    `embedding_json`      JSON         NOT NULL,
    `embedding_dimension` INT          NOT NULL,
    `embedding_text_hash` VARCHAR(64)  NOT NULL,
    `created_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),

    UNIQUE KEY `uk_scope_node_embedding_scope_model`
        (`exam_scope_node_id`, `embedding_model`),

    INDEX `idx_scope_node_embedding_scope`
        (`exam_scope_node_id`),
    INDEX `idx_scope_node_embedding_model`
        (`embedding_model`),
    INDEX `idx_scope_node_embedding_text_hash`
        (`embedding_text_hash`),

    CONSTRAINT `fk_scope_node_embedding_scope_node`
        FOREIGN KEY (`exam_scope_node_id`)
            REFERENCES `exam_scope_node` (`id`)
            ON DELETE CASCADE,

    CONSTRAINT `chk_scope_node_embedding_dimension`
        CHECK (`embedding_dimension` > 0)
);


CREATE TABLE `user_exam`
(
    `id`              BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`         BIGINT   NOT NULL,
    `exam_version_id` BIGINT   NOT NULL,
    `is_hidden`       BOOLEAN  NOT NULL DEFAULT FALSE,
    `last_studied_at` DATETIME NULL,
    `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,

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



CREATE TABLE `document_processing_group`
(
    `id`                   BIGINT        NOT NULL AUTO_INCREMENT,
    `user_exam_id`         BIGINT        NOT NULL,
    `status`               VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    `current_step`         VARCHAR(50)   NOT NULL DEFAULT 'UPLOAD_PENDING',
    `progress_rate`        INT           NOT NULL DEFAULT 0,
    `total_file_count`     INT           NOT NULL DEFAULT 0,
    `completed_file_count` INT           NOT NULL DEFAULT 0,
    `failed_file_count`    INT           NOT NULL DEFAULT 0,
    `error_message`        VARCHAR(1000) NULL,
    `started_at`           DATETIME      NULL,
    `completed_at`         DATETIME      NULL,
    `created_at`           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`           DATETIME      NULL ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),

    INDEX `idx_dpg_user_exam_id` (`user_exam_id`),
    INDEX `idx_dpg_status` (`status`),
    INDEX `idx_dpg_current_step` (`current_step`),
    INDEX `idx_dpg_created_at` (`created_at`),

    CONSTRAINT `fk_dpg_user_exam_id`
        FOREIGN KEY (`user_exam_id`)
            REFERENCES `user_exam` (`id`)
            ON DELETE CASCADE,

    CONSTRAINT `chk_dpg_total_file_count`
        CHECK (`total_file_count` >= 0)


);

CREATE TABLE `source_document`
(
    `id`                   BIGINT        NOT NULL AUTO_INCREMENT,
    `processing_group_id`  BIGINT        NOT NULL,
    `original_file_name`   VARCHAR(255)  NOT NULL,
    `stored_file_name`     VARCHAR(255)  NOT NULL,
    `storage_key`          VARCHAR(500)  NOT NULL,
    `file_extension`       VARCHAR(20)   NOT NULL,
    `mime_type`            VARCHAR(100)  NOT NULL,
    `file_size`            BIGINT        NOT NULL,
    `upload_status`        VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    `upload_error_message` VARCHAR(1000) NULL,
    `is_active`            BOOLEAN       NOT NULL,
    `created_at`           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`           DATETIME      NULL ON UPDATE CURRENT_TIMESTAMP,

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

CREATE TABLE `document_chunk`
(
    `id`                 BIGINT        NOT NULL AUTO_INCREMENT,
    `source_document_id` BIGINT        NOT NULL,
    `chunk_order`        INT           NOT NULL,
    `page_no`            INT           NULL,
    `section_title`      VARCHAR(255)  NULL,
    `heading_path`       VARCHAR(1000) NULL,
    `content_type`       VARCHAR(30)   NOT NULL DEFAULT 'TEXT',
    `code_language`      VARCHAR(30)   NULL,
    `content_text`       LONGTEXT      NOT NULL,
    `raw_text`           LONGTEXT      NULL,
    `summary`            TEXT          NULL,
    `keywords_json`      JSON          NULL,
    `structure_json`     JSON          NULL,
    `token_count`        INT           NULL,
    `mapping_status`     VARCHAR(30)   NOT NULL DEFAULT 'UNASSIGNED',
    `mapping_confidence` DECIMAL(5, 4) NULL,
    `created_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),

    UNIQUE KEY `uk_document_chunk_source_order`
        (`source_document_id`, `chunk_order`),

    INDEX `idx_document_chunk_page_no`
        (`source_document_id`, `page_no`),
    INDEX `idx_document_chunk_content_type`
        (`content_type`),
    INDEX `idx_document_chunk_code_language`
        (`code_language`),
    INDEX `idx_document_chunk_heading_path`
        (`source_document_id`, `heading_path`(255)),
    INDEX `idx_document_chunk_mapping_status`
        (`mapping_status`),

    CONSTRAINT `fk_document_chunk_source_document`
        FOREIGN KEY (`source_document_id`)
            REFERENCES `source_document` (`id`)
            ON DELETE CASCADE,
    CONSTRAINT `chk_document_chunk_order`
        CHECK (`chunk_order` >= 1),
    CONSTRAINT `chk_document_chunk_page_no`
        CHECK (`page_no` IS NULL OR `page_no` >= 1),
    CONSTRAINT `chk_document_chunk_token_count`
        CHECK (`token_count` IS NULL OR `token_count` >= 0),
    CONSTRAINT `chk_document_chunk_code_language`
        CHECK (
            `code_language` IS NULL
                OR `content_type` IN ('CODE', 'COMMAND', 'CONFIG')
            )
);

CREATE TABLE `document_chunk_embedding`
(
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT,
    `document_chunk_id`   BIGINT       NOT NULL,
    `embedding_model`     VARCHAR(100) NOT NULL,
    `embedding_json`      JSON         NOT NULL,
    `embedding_dimension` INT          NOT NULL,
    `embedding_text_hash` VARCHAR(64)  NOT NULL,
    `created_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),

    UNIQUE KEY `uk_document_chunk_embedding_chunk_model`
        (`document_chunk_id`, `embedding_model`),

    INDEX `idx_document_chunk_embedding_chunk`
        (`document_chunk_id`),
    INDEX `idx_document_chunk_embedding_model`
        (`embedding_model`),
    INDEX `idx_document_chunk_embedding_text_hash`
        (`embedding_text_hash`),

    CONSTRAINT `fk_document_chunk_embedding_chunk`
        FOREIGN KEY (`document_chunk_id`)
            REFERENCES `document_chunk` (`id`)
            ON DELETE CASCADE,

    CONSTRAINT `chk_document_chunk_embedding_dimension`
        CHECK (`embedding_dimension` > 0)
);

CREATE TABLE `document_scope_mapping`
(
    `id`                 BIGINT        NOT NULL AUTO_INCREMENT,
    `document_chunk_id`  BIGINT        NOT NULL,
    `exam_scope_node_id` BIGINT        NOT NULL,
    `rank_no`            INT           NOT NULL,
    `confidence_score`   DECIMAL(5, 4) NOT NULL,
    `mapping_reason`     TEXT          NULL,
    `created_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),

    UNIQUE KEY `uk_mapping_chunk_scope`
        (`document_chunk_id`, `exam_scope_node_id`),
    UNIQUE KEY `uk_mapping_chunk_rank`
        (`document_chunk_id`, `rank_no`),

    INDEX `idx_mapping_chunk`
        (`document_chunk_id`),
    INDEX `idx_mapping_scope_node`
        (`exam_scope_node_id`),
    INDEX `idx_mapping_confidence`
        (`confidence_score`),

    CONSTRAINT `fk_mapping_document_chunk`
        FOREIGN KEY (`document_chunk_id`)
            REFERENCES `document_chunk` (`id`)
            ON DELETE CASCADE,
    CONSTRAINT `fk_mapping_exam_scope_node`
        FOREIGN KEY (`exam_scope_node_id`)
            REFERENCES `exam_scope_node` (`id`)
            ON DELETE CASCADE,
    CONSTRAINT `chk_mapping_rank_no`
        CHECK (`rank_no` >= 1),
    CONSTRAINT `chk_mapping_confidence_score`
        CHECK (`confidence_score` >= 0 AND `confidence_score` <= 1)
);


CREATE TABLE `user_learning_content`
(
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT,
    `user_exam_id`       BIGINT       NOT NULL,
    `exam_scope_node_id` BIGINT       NOT NULL,
    `title`              VARCHAR(200) NOT NULL,
    `content`            LONGTEXT     NULL,
    `source_type`        VARCHAR(30)  NOT NULL DEFAULT 'DOCUMENT',
    `keywords_json`      JSON         NULL,
    `status`             VARCHAR(30)  NOT NULL DEFAULT 'NOT_MAPPED',
    `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`         DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ulc_user_exam_scope` (`user_exam_id`, `exam_scope_node_id`),

    INDEX `idx_ulc_user_exam_id` (`user_exam_id`),
    INDEX `idx_ulc_exam_scope_node_id` (`exam_scope_node_id`),
    INDEX `idx_ulc_status` (`status`),
    INDEX `idx_ulc_scope_source` (`exam_scope_node_id`, `source_type`),

    CONSTRAINT `fk_ulc_user_exam_id`
        FOREIGN KEY (`user_exam_id`) REFERENCES `user_exam` (`id`)
            ON DELETE CASCADE,

    CONSTRAINT `fk_ulc_exam_scope_node_id`
        FOREIGN KEY (`exam_scope_node_id`) REFERENCES `exam_scope_node` (`id`)
            ON DELETE CASCADE
);

CREATE TABLE `user_document_concept`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT       NOT NULL,
    `document_id` BIGINT       NOT NULL,
    `toc_id`      BIGINT       NOT NULL,
    `source_type` VARCHAR(30)  NOT NULL,
    `title`       VARCHAR(255) NOT NULL,
    `content`     LONGTEXT     NOT NULL,
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_document_concept_tenant_document_toc` (`user_id`, `document_id`, `toc_id`),
    INDEX `idx_udc_document_source` (`document_id`, `source_type`),
    INDEX `idx_udc_user_document` (`user_id`, `document_id`),
    CONSTRAINT `fk_udc_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_udc_document` FOREIGN KEY (`document_id`) REFERENCES `source_document` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_udc_toc` FOREIGN KEY (`toc_id`) REFERENCES `exam_scope_node` (`id`) ON DELETE CASCADE
);


CREATE TABLE `learning_content_reference`
(
    `id`                       BIGINT   NOT NULL AUTO_INCREMENT,
    `user_learning_content_id` BIGINT   NOT NULL,
    `document_chunk_id`        BIGINT   NOT NULL,
    `page_no`                  INT      NULL,
    `snippet`                  TEXT     NULL,
    `display_order`            INT      NOT NULL DEFAULT 1,
    `created_at`               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`               DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,

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



CREATE TABLE `refresh_tokens`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT       NOT NULL,
    `token`      VARCHAR(500) NOT NULL,
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refresh_tokens_user_id` (`user_id`),

    CONSTRAINT `fk_refresh_tokens_user`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
            ON DELETE CASCADE
);


CREATE TABLE `problem`
(
    `id`                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    `exam_scope_node_id` BIGINT       NOT NULL,
    `format`             VARCHAR(30)  NOT NULL,
    `content`            TEXT         NOT NULL,
    `score`              INT          NOT NULL,
    `answer`             VARCHAR(500) NOT NULL,
    `explanation`        TEXT         NOT NULL,
    `choice_count`       INT          NOT NULL,
    `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_problem_exam_scope_node_id`
        FOREIGN KEY (`exam_scope_node_id`) REFERENCES `exam_scope_node` (`id`),
    CONSTRAINT `chk_problem_score`
        CHECK (`score` > 0),
    CONSTRAINT `chk_problem_choice_count`
        CHECK (`choice_count` >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `past_paper`
(
    `id`               BIGINT PRIMARY KEY AUTO_INCREMENT,
    `exam_version_id`  BIGINT      NOT NULL,
    `status`           VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    `total_item_count` INT         NOT NULL,
    `is_reviewed`      TINYINT     NOT NULL DEFAULT 0,
    `year`             INT         NOT NULL,
    `round_no`         INT         NOT NULL,
    `time_limit`       INT         NOT NULL,
    `exam_date`        DATE        NOT NULL,
    `created_at`       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `chk_past_paper_total_item_count`
        CHECK (total_item_count >= 0),
    CONSTRAINT `chk_past_paper_year`
        CHECK (year >= 2013),
    CONSTRAINT `chk_past_paper_round_no`
        CHECK (round_no > 0),
    CONSTRAINT `chk_past_paper_time_limit`
        CHECK (time_limit > 0),
    CONSTRAINT `chk_past_paper_status`
        CHECK (`status` IN ('DRAFT', 'PUBLISHED', 'UNPUBLISHED')),
    CONSTRAINT `chk_past_paper_is_reviewed`
        CHECK (`is_reviewed` IN (0, 1)),
    UNIQUE KEY `uk_past_paper_exam_version_year_round_no` (exam_version_id, year, round_no),
    CONSTRAINT `fk_past_paper_exam_version_id`
        FOREIGN KEY (`exam_version_id`) REFERENCES `exam_version` (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE `past_paper_item`
(
    `id`            BIGINT AUTO_INCREMENT PRIMARY KEY,
    `past_paper_id` BIGINT   NOT NULL,
    `problem_id`    BIGINT   NOT NULL,
    `sort_order`    INT      NOT NULL,
    `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_past_paper_item_past_paper_problem` (`past_paper_id`, `problem_id`),
    UNIQUE KEY `uk_past_paper_item_past_paper_sort_order` (`past_paper_id`, `sort_order`),
    CONSTRAINT `fk_past_paper_item_past_paper_id`
        FOREIGN KEY (`past_paper_id`) REFERENCES `past_paper` (`id`),
    CONSTRAINT `fk_past_paper_item_problem_id`
        FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`),
    CONSTRAINT `chk_past_paper_item_sort_order`
        CHECK (`sort_order` >= 1)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `past_paper_attempt`
(
    `id`            BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id`       BIGINT      NOT NULL,
    `past_paper_id` BIGINT      NOT NULL,
    `status`        VARCHAR(30) NOT NULL DEFAULT 'SOLVING',
    `user_score`    INT         NULL     DEFAULT NULL,
    `passed`        TINYINT(1)  NULL     DEFAULT NULL,
    `elapsed_time`  INT                  DEFAULT NULL,
    `started_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_past_paper_attempt_user_id`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_past_paper_attempt_past_paper_id`
        FOREIGN KEY (`past_paper_id`) REFERENCES `past_paper` (`id`),
    CONSTRAINT `chk_past_paper_attempt_user_score`
        CHECK (`user_score` >= 0),
    CONSTRAINT `chk_past_paper_attempt_status`
        CHECK (`status` IN ('SOLVING', 'GRADED')),
    CONSTRAINT `chk_past_paper_attempt_elapsed_time`
        CHECK (`elapsed_time` >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE UNIQUE INDEX `uk_sub_user_past_paper_solving`
    ON `past_paper_attempt` (
                             `user_id`,
                             `past_paper_id`,
        (CASE WHEN `status` = 'SOLVING' THEN 'SOLVING' ELSE NULL END)
        );

CREATE TABLE `user_answer`
(
    `id`                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    `past_paper_attempt_id` BIGINT       NOT NULL,
    `problem_id`            BIGINT       NOT NULL,
    `user_answer`           VARCHAR(500) NULL,
    `correct`               TINYINT(1)   NULL     DEFAULT NULL,
    `marked_for_review`     TINYINT(1)   NOT NULL DEFAULT FALSE,
    `created_at`            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_answer_attempt_problem` (`past_paper_attempt_id`, `problem_id`),
    CONSTRAINT `fk_user_answer_past_paper_attempt_id`
        FOREIGN KEY (`past_paper_attempt_id`) REFERENCES `past_paper_attempt` (`id`),
    CONSTRAINT `fk_user_answer_problem_id`
        FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `problem_choice`
(
    `id`         BIGINT AUTO_INCREMENT PRIMARY KEY,
    `problem_id` BIGINT       NOT NULL,
    `sort_order` INT          NOT NULL,
    `content`    VARCHAR(500) NOT NULL,
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_problem_choice_order` (`problem_id`, `sort_order`),
    CONSTRAINT `fk_problem_choice_problem_id`
        FOREIGN KEY (`problem_id`) REFERENCES `problem` (`id`),
    CONSTRAINT `chk_problem_choice_sort_order`
        CHECK (`sort_order` >= 1)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE `study_room`
(
    `id`            BIGINT AUTO_INCREMENT PRIMARY KEY,
    `past_paper_id` BIGINT       NOT NULL,
    `created_by`    BIGINT       NOT NULL,
    `title`         VARCHAR(100) NOT NULL,
    `description`   VARCHAR(500) NOT NULL,
    `capacity`      INT          NOT NULL,
    `time_limit`    INT          NOT NULL,
    `status`        VARCHAR(30)  NOT NULL DEFAULT 'WAITING',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_study_room_past_paper_id`
        FOREIGN KEY (`past_paper_id`) REFERENCES `past_paper` (`id`),
    CONSTRAINT `fk_study_room_created_by`
        FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
    CONSTRAINT `chk_study_room_capacity`
        CHECK (`capacity` BETWEEN 2 AND 5),
    CONSTRAINT `chk_study_room_status`
        CHECK (`status` IN ('WAITING', 'SOLVING', 'FEEDBACK', 'CANCELED')),
    CONSTRAINT `chk_study_room_time_limit`
        CHECK (`time_limit` > 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `study_room_member`
(
    `id`                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id`               BIGINT      NOT NULL,
    `study_room_id`         BIGINT      NOT NULL,
    `past_paper_attempt_id` BIGINT      NULL,
    `role`                  VARCHAR(30) NOT NULL,
    `is_ready`              BOOLEAN     NOT NULL DEFAULT FALSE,
    `joined_at`             DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`            DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_study_room_member_room_user` (`study_room_id`, `user_id`),
    UNIQUE KEY `uk_study_room_member_past_paper_attempt` (`past_paper_attempt_id`),
    CONSTRAINT `fk_study_room_member_user_id`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_study_room_member_study_room_id`
        FOREIGN KEY (`study_room_id`) REFERENCES `study_room` (`id`),
    CONSTRAINT `fk_study_room_member_past_paper_attempt_id`
        FOREIGN KEY (`past_paper_attempt_id`) REFERENCES `past_paper_attempt` (`id`),
    CONSTRAINT `chk_study_room_member_role`
        CHECK (`role` IN ('HOST', 'MEMBER'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `active_study_room_participation`
(
    `id`            BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id`       BIGINT   NOT NULL,
    `study_room_id` BIGINT   NOT NULL,
    `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_active_study_room_participation_user_id` (`user_id`),
    CONSTRAINT `fk_active_study_room_participation_user_id`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_active_study_room_participation_study_room_id`
        FOREIGN KEY (`study_room_id`) REFERENCES `study_room` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
