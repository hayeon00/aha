START TRANSACTION;

INSERT INTO `exam` (`code`, `name`, `status`) VALUES ('SQLD', 'SQL 개발자(SQLD)', 'ACTIVE');
SET @exam_id = LAST_INSERT_ID();

INSERT INTO `exam` (`code`, `name`, `status`) VALUES ('INFOPRO', '정보처리기사', 'ACTIVE');
SET @infra_id = LAST_INSERT_ID();

INSERT INTO `exam` (`code`, `name`, `status`) VALUES ('ADSP', '데이터분석 준전문가(ADsP)', 'ACTIVE');
SET @adsp_id = LAST_INSERT_ID();

INSERT INTO `exam_version` (`exam_id`, `version_no`, `version_name`, `default_question_count`, `duration_type`, `default_duration_seconds`, `total_score`, `passing_rule_type`, `passing_score`, `has_subject_fail_rule`, `subject_fail_threshold`, `status`)
VALUES (@exam_id, 2025, 'SQLD 2025 개정판', 50, 'TOTAL', 5400, 100, 'TOTAL', 60, 1, 40, 'ACTIVE');
SET @exam_version_id = LAST_INSERT_ID();

INSERT INTO `exam_part` (`exam_version_id`, `code`, `name`, `default_question_count`, `default_duration_seconds`, `total_score`, `is_subject_fail_target`, `subject_fail_threshold_score`, `is_active`, `display_order`) VALUES
                                                                                                                                                                                                                             (@exam_version_id, 'SUBJECT_1', '1과목 데이터 모델링의 이해', 10, NULL, 20, 1, 8, 1, 1),
                                                                                                                                                                                                                             (@exam_version_id, 'SUBJECT_2', '2과목 SQL 기본 및 활용', 40, NULL, 80, 1, 32, 1, 2);

SET @part01_id = (SELECT `id` FROM `exam_part` WHERE `exam_version_id` = @exam_version_id AND `code` = 'SUBJECT_1' LIMIT 1);
SET @part02_id = (SELECT `id` FROM `exam_part` WHERE `exam_version_id` = @exam_version_id AND `code` = 'SUBJECT_2' LIMIT 1);

INSERT INTO `exam_scope_node` (`exam_version_id`, `exam_part_id`, `code`, `parent_id`, `node_type`, `depth`, `title`, `is_leaf`, `is_active`, `display_order`) VALUES
                                                                                                                                                                   (@exam_version_id, @part01_id, 'SQLD-MODELING-01', NULL, 'SECTION', 1, '데이터 모델링의 이해', 0, 1, 1),
                                                                                                                                                                   (@exam_version_id, @part01_id, 'SQLD-MODELING-02', NULL, 'SECTION', 1, '데이터 모델과 SQL', 0, 1, 2),
                                                                                                                                                                   (@exam_version_id, @part02_id, 'SQLD-SQL-01', NULL, 'SECTION', 1, 'SQL 기본', 0, 1, 1),
                                                                                                                                                                   (@exam_version_id, @part02_id, 'SQLD-SQL-02', NULL, 'SECTION', 1, 'SQL 활용', 0, 1, 2),
                                                                                                                                                                   (@exam_version_id, @part02_id, 'SQLD-SQL-03', NULL, 'SECTION', 1, '관리 구문', 0, 1, 3);

INSERT INTO `exam_scope_node` (`exam_version_id`, `exam_part_id`, `code`, `parent_id`, `node_type`, `depth`, `title`, `is_leaf`, `is_active`, `display_order`)
SELECT p.`exam_version_id`, p.`exam_part_id`, x.`code`, p.`id`, 'TOPIC', 2, x.`title`, 1, 1, x.`display_order`
FROM `exam_scope_node` p
         JOIN (
    SELECT 'SQLD-MODELING-01' parent_code, 'SQLD-MODELING-01-01' code, '데이터모델의 이해' title, 1 display_order
    UNION ALL SELECT 'SQLD-MODELING-01', 'SQLD-MODELING-01-02', '엔터티', 2
    UNION ALL SELECT 'SQLD-MODELING-01', 'SQLD-MODELING-01-03', '속성', 3
    UNION ALL SELECT 'SQLD-MODELING-01', 'SQLD-MODELING-01-04', '관계', 4
    UNION ALL SELECT 'SQLD-MODELING-01', 'SQLD-MODELING-01-05', '식별자', 5
    UNION ALL SELECT 'SQLD-MODELING-02', 'SQLD-MODELING-02-01', '정규화', 1
    UNION ALL SELECT 'SQLD-MODELING-02', 'SQLD-MODELING-02-02', '관계와 조인의 이해', 2
    UNION ALL SELECT 'SQLD-MODELING-02', 'SQLD-MODELING-02-03', '모델이 표현하는 트랜잭션의 이해', 3
    UNION ALL SELECT 'SQLD-MODELING-02', 'SQLD-MODELING-02-04', 'Null 속성의 이해', 4
    UNION ALL SELECT 'SQLD-MODELING-02', 'SQLD-MODELING-02-05', '본질식별자 vs 인조식별자', 5
    UNION ALL SELECT 'SQLD-SQL-01', 'SQLD-SQL-01-01', '관계형 데이터베이스 개요', 1
    UNION ALL SELECT 'SQLD-SQL-01', 'SQLD-SQL-01-02', 'SELECT 문', 2
    UNION ALL SELECT 'SQLD-SQL-01', 'SQLD-SQL-01-03', '함수', 3
    UNION ALL SELECT 'SQLD-SQL-01', 'SQLD-SQL-01-04', 'WHERE 절', 4
    UNION ALL SELECT 'SQLD-SQL-01', 'SQLD-SQL-01-05', 'GROUP BY, HAVING 절', 5
    UNION ALL SELECT 'SQLD-SQL-01', 'SQLD-SQL-01-06', 'ORDER BY 절', 6
    UNION ALL SELECT 'SQLD-SQL-01', 'SQLD-SQL-01-07', '조인', 7
    UNION ALL SELECT 'SQLD-SQL-01', 'SQLD-SQL-01-08', '표준 조인', 8
    UNION ALL SELECT 'SQLD-SQL-02', 'SQLD-SQL-02-01', '서브쿼리', 1
    UNION ALL SELECT 'SQLD-SQL-02', 'SQLD-SQL-02-02', '집합 연산자', 2
    UNION ALL SELECT 'SQLD-SQL-02', 'SQLD-SQL-02-03', '그룹 함수', 3
    UNION ALL SELECT 'SQLD-SQL-02', 'SQLD-SQL-02-04', '윈도우 함수', 4
    UNION ALL SELECT 'SQLD-SQL-02', 'SQLD-SQL-02-05', 'Top N 쿼리', 5
    UNION ALL SELECT 'SQLD-SQL-02', 'SQLD-SQL-02-06', '계층형 질의와 셀프 조인', 6
    UNION ALL SELECT 'SQLD-SQL-02', 'SQLD-SQL-02-07', 'PIVOT 절과 UNPIVOT절', 7
    UNION ALL SELECT 'SQLD-SQL-02', 'SQLD-SQL-02-08', '정규 표현식', 8
    UNION ALL SELECT 'SQLD-SQL-03', 'SQLD-SQL-03-01', 'DML', 1
    UNION ALL SELECT 'SQLD-SQL-03', 'SQLD-SQL-03-02', 'TCL', 2
    UNION ALL SELECT 'SQLD-SQL-03', 'SQLD-SQL-03-03', 'DDL', 3
    UNION ALL SELECT 'SQLD-SQL-03', 'SQLD-SQL-03-04', 'DCL', 4
) x ON p.`code` = x.`parent_code`
WHERE p.`exam_version_id` = @exam_version_id AND p.`node_type` = 'SECTION';

INSERT INTO `workbook_type` (`code`, `name`, `display_order`, `created_at`, `updated_at`)
VALUES
    ('PAST', '복원기출', 1, NOW(), NOW()),
    ('TYPE2', '테스트유형2', 2, NOW(), NOW()),
    ('TYPE3', '테스트유형3', 3, NOW(), NOW());


-- 2) 개정판 시험 정보 및 기출 타입 바인딩
SET @exam_version_id = (SELECT `id` FROM `exam_version` WHERE `version_name` = 'SQLD 2025 개정판' LIMIT 1);
SET @workbook_type_id = (SELECT `id` FROM `workbook_type` WHERE `code` = 'PAST' LIMIT 1);

-- 3) '2025년 제1회 복원기출문제집' 신규 등록
INSERT INTO `workbook` (`exam_version_id`, `workbook_type_id`, `status`, `created_at`, `updated_at`)
VALUES (@exam_version_id, @workbook_type_id, 'PUBLISHED', NOW(), NOW());
SET @workbook_id = LAST_INSERT_ID();

INSERT INTO `past_exam_workbook` (`workbook_id`, `is_reviewed`, `year`, `round_no`, `total_problem_count`, `time_limit`, `created_at`, `updated_at`,`exam_date`)
VALUES (@workbook_id, 1, 2025, 1, 10,5400, NOW(), NOW(),'2026-03-01 09:00:00');


-- =================================================================
-- 2. 세부 과목 노드 ID 일괄 조회
-- =================================================================
SET @node_model_understand = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-MODELING-01-01' LIMIT 1); -- 데이터모델의 이해
SET @node_entity           = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-MODELING-01-02' LIMIT 1); -- 엔터티
SET @node_attribute        = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-MODELING-01-03' LIMIT 1); -- 속성
SET @node_identifier       = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-MODELING-01-05' LIMIT 1); -- 식별자
SET @node_normalization    = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-MODELING-02-01' LIMIT 1); -- 정규화
SET @node_select_stmt      = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-SQL-01-02' LIMIT 1);   -- SELECT 문
SET @node_where_clause     = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-SQL-01-04' LIMIT 1);   -- WHERE 절
SET @node_groupby_clause   = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-SQL-01-05' LIMIT 1);   -- GROUP BY, HAVING 절
SET @node_subquery         = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-SQL-02-01' LIMIT 1);   -- 서브쿼리
SET @node_window_func      = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-SQL-02-04' LIMIT 1);   -- 윈도우 함수


-- =================================================================
-- 3. 1번 ~ 10번 문제 등록
-- =================================================================

-- -----------------------------------------------------------------
-- [문제 1] 데이터 모델링의 세 가지 관점 (기본 개념 문항)
-- -----------------------------------------------------------------
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (
           @node_model_understand,
           'SINGLE_CHOICE',
           '### 다음 중 데이터 모델링이 갖추어야 할 3가지 관점(Perspective)에 해당하지 않는 것은?

       데이터 모델링은 복잡한 현실 세계의 비즈니스 프로세스를 추상화하고 단순화하여 정보 시스템으로 이전하는 핵심 기법입니다. 이때 관점 중심의 설계가 필수적입니다.',
           2,
           '4',
           '**정답 설명:**
       데이터 모델링의 3대 관점은 다음과 같습니다.
       * **데이터 관점(What)**: 업무가 어떤 데이터와 관련이 있으며 데이터 사이의 관계는 무엇인지 규명합니다.
       * **프로세스 관점(How)**: 업무가 실제 어떤 시나리오와 흐름으로 수행되는지 규명합니다.
       * **상관 관점(Data vs Process)**: 업무 흐름에 따라 데이터가 어떤 영향을 받는지(CRUD 분석 등) 규명합니다.

       따라서 `통제/제어 관점`은 속해 있지 않습니다.',
           4, NOW(), NOW()
       );
SET @prob_id_1 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_1, 1, '업무가 어떤 데이터를 필요로 하는지 분석하는 **데이터 관점**', NOW(), NOW()),
                                                                                                     (@prob_id_1, 2, '업무가 실제 어떤 일을 하는지 분석하는 **프로세스 관점**', NOW(), NOW()),
                                                                                                     (@prob_id_1, 3, '업무의 처리 방법에 따라 데이터가 어떻게 변화하는지 분석하는 **상관 관점**', NOW(), NOW()),
                                                                                                     (@prob_id_1, 4, '전체 데이터 아키텍처의 무결성을 실시간 제어하고 보안을 감시하는 **통제 관점**', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`)
VALUES (@workbook_id, @prob_id_1, 1, NOW(), NOW());


-- -----------------------------------------------------------------
-- [문제 2] 엔터티의 분류 (상세 특징 문항)
-- -----------------------------------------------------------------
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (
           @node_entity,
           'SINGLE_CHOICE',
           '### 엔터티(Entity)는 발생 시점에 따라 여러 종류로 분류할 수 있습니다. 다음 중 아래에서 설명하는 엔터티 유형으로 가장 올바른 것은?

       > "업무가 수행되는 과정에서 발생하며, 두 개 이상의 부모 엔터티로부터 속성을 상속받아 생성되는 경우가 많습니다. 데이터양이 가장 유동적이고 빠르게 누적되는 특성을 지닙니다. 대표적인 예시로는 주문, 결제, 배송 등이 있습니다."',
           2,
           '3',
           '**정답 설명:**
       발생 시점에 따른 엔터티 분류는 크게 3가지입니다.
       1. **기본 엔터티(Key Entity)**: 타 엔터티의 도움 없이 독자적으로 존재 가능 (예: 고객, 상품)
       2. **중심 엔터티(Main Entity)**: 기본 엔터티로부터 발생하며 업무의 핵심 역할 수행 (예: 계약, 접수)
       3. **행위 엔터티(Active Entity)**: 두 개 이상의 엔터티 작용에 의해 생성되며 가장 빈번하게 발생 (예: 주문, 결제)

       따라서 제시문은 `행위 엔터티`에 대한 설명입니다.',
           4, NOW(), NOW()
       );
SET @prob_id_2 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_2, 1, '스스로 독립적으로 생성되는 `기본 엔터티(Key Entity)`', NOW(), NOW()),
                                                                                                     (@prob_id_2, 2, '업무의 중심 골격을 형성하며 관계를 통해 전개되는 `중심 엔터티(Main Entity)`', NOW(), NOW()),
                                                                                                     (@prob_id_2, 3, '업무 행위에 의해 동적으로 끊임없이 발생하는 `행위 엔터티(Active Entity)`', NOW(), NOW()),
                                                                                                     (@prob_id_2, 4, '물리적인 형체 없이 개념적인 형태로만 존재하는 `무형 엔터티(Conceptual Entity)`', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`)
VALUES (@workbook_id, @prob_id_2, 2, NOW(), NOW());


-- -----------------------------------------------------------------
-- [문제 3] 속성의 특징 (설계 이론 문항)
-- -----------------------------------------------------------------
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (
           @node_attribute,
           'SINGLE_CHOICE',
           '### 다음 중 데이터 모델링의 핵심 구성 요소인 속성(Attribute)의 특징으로 가장 올바르지 않은 것은?',
           2,
           '2',
           '**정답 설명:**
       * **속성은 원자값(Single Value)**을 가져야 하므로 하나의 속성에 다중값(Multi-value)이 올 수 없습니다. 만약 다중값이 존재한다면 1차 정규화를 통해 엔터티를 분리해야 합니다. (따라서 2번 설명이 정답)
       * 엔터티는 두 개 이상의 인스턴스(Instance)의 집합이어야 하며, 하나의 속성은 반드시 하나의 엔터티에 종속됩니다.
       * 주식별자에 함수적으로 완전히 종속되어야 합니다.',
           4, NOW(), NOW()
       );
SET @prob_id_3 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_3, 1, '하나의 엔터티는 구체적인 특성을 규정하는 2개 이상의 속성을 가진다.', NOW(), NOW()),
                                                                                                     (@prob_id_3, 2, '업무 편의성을 위해 하나의 속성은 배열(Array)과 같은 다중값(Multi-value)을 가질 수 있다.', NOW(), NOW()),
                                                                                                     (@prob_id_3, 3, '속성도 인스턴스와 마찬가지로 속성이 가질 수 있는 값의 범위인 도메인(Domain)을 지닌다.', NOW(), NOW()),
                                                                                                     (@prob_id_3, 4, '사원 엔터티의 `사원번호`처럼 주식별자에 식별되거나 종속되는 성격을 띤다.', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`)
VALUES (@workbook_id, @prob_id_3, 3, NOW(), NOW());


-- -----------------------------------------------------------------
-- [문제 4] 식별자의 분류 (식별자 구분 문항)
-- -----------------------------------------------------------------
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (
           @node_identifier,
           'SINGLE_CHOICE',
           '### 다음 식별자(Identifier) 유형 중 분류 기준과 해당 명칭이 올바르게 짝지어지지 않은 것은?

       식별자는 엔터티 내에서 각 인스턴스들을 고유하게 구분해 주는 역할을 하며, 대표성 여부, 스스로 생성 여부 등에 따라 다양하게 분류할 수 있습니다.',
           2,
           '1',
           '**정답 설명:**
       * 대표성 여부에 따른 분류는 **주식별자(Primary Identifier)**와 **보조식별자(Alternate Identifier)**입니다.
       * 내부 식별자와 외부 식별자는 **스스로 생성 여부(엔터티 내 생성 여부)**에 따른 분류 기준입니다.
       * 단일 식별자와 복합 식별자는 **속성의 수(단일 속성인가, 복수 속성인가)**에 따른 분류 기준입니다.
       * 본질 식별자와 인조 식별자는 **대체 여부(원래 존재하던 업무 식별자인가, 임의 가공한 식별자인가)**에 따른 분류 기준입니다.',
           4, NOW(), NOW()
       );
SET @prob_id_4 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_4, 1, '대표성 여부 - `내부 식별자`, `외부 식별자`', NOW(), NOW()),
                                                                                                     (@prob_id_4, 2, '스스로 생성 여부 - `내부 식별자`, `외부 식별자`', NOW(), NOW()),
                                                                                                     (@prob_id_4, 3, '속성의 수 - `단일 식별자`, `복합 식별자`', NOW(), NOW()),
                                                                                                     (@prob_id_4, 4, '대체 여부 - `본질 식별자`, `인조 식별자`', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`)
VALUES (@workbook_id, @prob_id_4, 4, NOW(), NOW());


-- -----------------------------------------------------------------
-- [문제 5] 정규화 과정 (표/테이블 형태 마크다운 문항)
-- -----------------------------------------------------------------
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (
           @node_normalization,
           'SINGLE_CHOICE',
           '### 다음 테이블 구조를 분석하고, 테이블 설계의 이상 현상(Anomaly)을 근본적으로 제거하기 위해 가장 먼저 수행해야 할 정규화 단계는 무엇인가?

       | 사원번호 (PK) | 프로젝트코드 (PK) | 프로젝트명 | 급여 | 직무 |
       | :--- | :--- | :--- | :--- | :--- |
       | 201103 | P_SYSTEM | ERP 고도화 | 4500 | 개발 |
       | 201103 | P_SECURITY | 보안망 구축 | 4500 | 개발 |
       | 201205 | P_SYSTEM | ERP 고도화 | 3800 | 분석 |

       > **분석 전제**: 복합키(`사원번호 + 프로젝트코드`)가 기본키이며, 비식별자 속성 중 `프로젝트명`은 `프로젝트코드`에만 종속되고 `급여`와 `직무`는 `사원번호`에만 종속되어 있습니다.',
           2,
           '2',
           '**정답 설명:**
       현재 복합 기본키의 일부분에 종속되는 속성(`프로젝트명`, `급여`, `직무`)들이 존재하므로 **부분 함수 종속성(Partial Functional Dependency)**이 발생하고 있는 상태입니다.

       이러한 부분 함수 종속성을 제거하여 완전 함수 종속 관계로 테이블을 분리하는 단계를 **제2정규화(2NF)**라고 합니다.
       * 제1정규화: 모든 속성은 원자값을 가져야 함
       * 제3정규화: 이행적 함수 종속 제거 (A -> B, B -> C 관계 해소)
       * BCNF: 결정자이면서 후보키가 아닌 것 제거',
           4, NOW(), NOW()
       );
SET @prob_id_5 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_5, 1, '속성의 중복 값을 제거하고 도메인 원자성을 보장하는 `제1정규화(1NF)`', NOW(), NOW()),
                                                                                                     (@prob_id_5, 2, '부분 함수 종속을 분리하여 완전 함수 종속 구조를 생성하는 `제2정규화(2NF)`', NOW(), NOW()),
                                                                                                     (@prob_id_5, 3, '주식별자에 종속되지 않는 비식별자 간의 이행적 함수 종속을 분리하는 `제3정규화(3NF)`', NOW(), NOW()),
                                                                                                     (@prob_id_5, 4, '결정자 구조가 후보키에 포함되지 않는 복합 구조를 개선하는 `BCNF(Boyce-Codd Regularity)`', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`)
VALUES (@workbook_id, @prob_id_5, 5, NOW(), NOW());


-- -----------------------------------------------------------------
-- [문제 6] SELECT 문 NULL 연산 (코드 분석 마크다운 문항)
-- -----------------------------------------------------------------
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (
           @node_select_stmt,
           'SINGLE_CHOICE',
           '### 아래의 가상 테이블 데이터 및 SQL 쿼리를 실행하여 도출되는 최종 행(Row)의 개수로 올바른 것은?

       **[T1 테이블]**

       | ID | SCORE |
       | :--- | :--- |
       | 1 | 80 |
       | 2 | NULL |
       | 3 | 90 |

       ```sql
       SELECT SCORE + 10
       FROM T1
       WHERE SCORE IS NOT NULL OR SCORE = 80;
       ```',
           2,
           '2',
           '**정답 설명:**
       WHERE 절 조건을 면밀히 분석합니다.
       1. `SCORE IS NOT NULL`: ID 1번(80), ID 3번(90)이 참입니다. (2건)
       2. `SCORE = 80`: ID 1번(80)이 참이고, ID 2번(NULL)은 NULL에 대한 동등 연산이므로 거짓/알수없음(Unknown)이 됩니다.
       3. 두 조건이 `OR`로 연결되어 있으므로 최종 만족하는 행은 ID 1번과 ID 3번 두 가지입니다.

       따라서 연산 결과(`90`, `100`)를 포함한 최종 출력 행의 개수는 **2개**입니다.',
           4, NOW(), NOW()
       );
SET @prob_id_6 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_6, 1, '1개', NOW(), NOW()),
                                                                                                     (@prob_id_6, 2, '2개', NOW(), NOW()),
                                                                                                     (@prob_id_6, 3, '3개', NOW(), NOW()),
                                                                                                     (@prob_id_6, 4, '0개 (에러 발생)', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`)
VALUES (@workbook_id, @prob_id_6, 6, NOW(), NOW());


-- -----------------------------------------------------------------
-- [문제 7] WHERE 절 연산자 우선순위 (코드 블록 마크다운 문항)
-- -----------------------------------------------------------------
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (
           @node_where_clause,
           'SINGLE_CHOICE',
           '### 다음 중 아래 SQL 문의 WHERE 조건절이 해석되는 우선순위 순서로 가장 올바른 것은?

       ```sql
       SELECT emp_name
       FROM employee
       WHERE job_code = ''MANAGER''
          OR salary >= 5000000
         AND dept_id = 10;
       ```',
           2,
           '2',
           '**정답 설명:**
       SQL 조건절에서 연산자 우선순위는 다음과 같습니다.
       1. 산술 연산자
       2. 비교 연산자 (`=`, `>=`, `<` 등)
       3. `NOT`
       4. `AND`
       5. `OR`

       따라서 비교 연산이 먼저 일어난 뒤, `AND` 연산이 `OR` 연산보다 우선하여 해석됩니다. 즉 `(salary >= 5000000 AND dept_id = 10)` 연산이 평가된 뒤 그 결과가 `job_code = ''MANAGER''` 조건과 OR 연산으로 묶입니다.',
           4, NOW(), NOW()
       );
SET @prob_id_7 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_7, 1, '왼쪽에서 오른쪽으로 차례대로 평가되므로 `job_code` OR `salary` 비교가 먼저 실행된다.', NOW(), NOW()),
                                                                                                     (@prob_id_7, 2, '`AND` 연산자가 `OR` 연산자보다 우선순위가 높기 때문에 `salary`와 `dept_id` 조건 결합이 먼저 처리된다.', NOW(), NOW()),
                                                                                                     (@prob_id_7, 3, '`OR` 연산자가 전체 비교문에서 가장 높은 우선순위를 지닌다.', NOW(), NOW()),
                                                                                                     (@prob_id_7, 4, '우선순위가 동일하므로 DBMS 옵티마이저가 임의로 해석 순서를 결정한다.', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`)
VALUES (@workbook_id, @prob_id_7, 7, NOW(), NOW());


-- -----------------------------------------------------------------
-- [문제 8] HAVING 절과 GROUP BY (집계 연산 마크다운 문항)
-- -----------------------------------------------------------------
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (
           @node_groupby_clause,
           'SINGLE_CHOICE',
           '### 다음 SQL 문 중 실행 시 오류가 발생하는 잘못 작성된 쿼리는 어떤 것인가?

       * 전제: `employee` 테이블은 컬럼으로 `id`, `salary`, `dept_id`, `job_code`를 가지고 있습니다.',
           2,
           '2',
           '**정답 설명:**
       * **2번 쿼리**는 에러가 발생합니다. `GROUP BY dept_id`에 의해 부서 코드별로 그룹화가 되었지만, `HAVING salary >= 3000000`처럼 그룹 함수(`SUM`, `AVG`, `MAX` 등)를 통하지 않고 개별 원적외선 형태인 컬럼(`salary`)을 단독 조건으로 명시했기 때문입니다.
       * 개별 행 데이터 필터링은 반드시 `WHERE` 절에서 수행해야 하며, 만약 HAVING 절에 오려면 `AVG(salary) >= 3000000` 형태로 집계되어야 합니다.',
           4, NOW(), NOW()
       );
SET @prob_id_8 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_8, 1, '`SELECT dept_id, SUM(salary) FROM employee GROUP BY dept_id;`', NOW(), NOW()),
                                                                                                     (@prob_id_8, 2, '`SELECT dept_id, AVG(salary) FROM employee GROUP BY dept_id HAVING salary >= 3000000;`', NOW(), NOW()),
                                                                                                     (@prob_id_8, 3, '`SELECT dept_id, AVG(salary) FROM employee WHERE salary >= 3000000 GROUP BY dept_id;`', NOW(), NOW()),
                                                                                                     (@prob_id_8, 4, '`SELECT job_code, COUNT(*) FROM employee GROUP BY job_code HAVING COUNT(*) >= 5;`', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`)
VALUES (@workbook_id, @prob_id_8, 8, NOW(), NOW());


-- -----------------------------------------------------------------
-- [문제 9] 다중 행 서브쿼리 연산자 (서브쿼리 문항)
-- -----------------------------------------------------------------
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (
           @node_subquery,
           'SINGLE_CHOICE',
           '### 서브쿼리의 반환 데이터 형태에 따라 다중 행 서브쿼리 연산자를 사용해야 합니다. 다음 중 다중 행 서브쿼리 연산자와 관련 설명이 가장 올바르지 않은 것은?',
           2,
           '1',
           '**정답 설명:**
       * `IN` 연산자는 서브쿼리가 반환하는 값의 집합 중에서 하나라도 일치하면 참이 됩니다. (OR 연산 형태와 흡사)
       * **`ALL` 연산자**는 모든 값에 만족해야 참이 됩니다. 만약 서브쿼리 결과에 NULL이 존재하고 `salary > ALL (SELECT ...)` 형태의 연산을 적용하면, 어떠한 행도 결과로 추출되지 않게 되므로 1번 설명은 반대로 매핑되어 명확히 틀렸습니다.',
           4, NOW(), NOW()
       );
SET @prob_id_9 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_9, 1, '`ALL`은 서브쿼리의 결과값 중에서 최소한 한 개 이상 만족하는 조건을 충족하면 참이 된다.', NOW(), NOW()),
                                                                                                     (@prob_id_9, 2, '`ANY`는 서브쿼리가 반환하는 조건 중 어느 하나라도 만족하면 행을 가져온다.', NOW(), NOW()),
                                                                                                     (@prob_id_9, 3, '`EXISTS`는 메인쿼리의 데이터가 서브쿼리의 조건을 충족하는지 존재 유무만을 판별하여 불리언 값을 도출한다.', NOW(), NOW()),
                                                                                                     (@prob_id_9, 4, '다중 행 서브쿼리는 단일 행 연산자(예: `=`, `>`, `<`)와 혼용하여 직접 비교할 수 없다.', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`)
VALUES (@workbook_id, @prob_id_9, 9, NOW(), NOW());


-- -----------------------------------------------------------------
-- [문제 10] 윈도우 함수 (RANK vs DENSE_RANK 테이블 비교 마크다운 문항)
-- -----------------------------------------------------------------
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (
           @node_window_func,
           'SINGLE_CHOICE',
           '### 다음 중 동점이 존재할 때의 순위 산정 방식에 따라 출력되는 윈도우 함수(Window Function)의 결과 값으로 올바르지 않은 항목은?

       **[학생 성적 리스트]**

       | 이름 | 점수 |
       | :--- | :--- |
       | 김철수 | 95 |
       | 이영희 | 95 |
       | 박민수 | 80 |

       위 데이터를 기준으로 높은 점수 순으로 순위를 집계하고자 합니다.',
           2,
           '3',
           '**정답 설명:**
       * `RANK()`: 동일한 값에 동일한 순위를 부여하고 다음 순위는 건너뜁니다.
         * 김철수(1위), 이영희(1위), 박민수(**3위**)
       * `DENSE_RANK()`: 동일한 값에 동일한 순위를 부여하되 다음 순위를 건너뛰지 않고 순차적으로 나열합니다.
         * 김철수(1위), 이영희(1위), 박민수(**2위**)
       * `ROW_NUMBER()`: 값의 동일 여부와 무관하게 고유한 일련번호를 고정 부여합니다.
         * 김철수(1번), 이영희(2번), 박민수(3번) - 정렬 우선에 따라 다를 수 있음.

       따라서 3번 지문인 `DENSE_RANK() 적용 시 박민수의 순위는 3위이다`라는 진술은 틀렸습니다. (실제로는 2위가 됨)',
           4, NOW(), NOW()
       );
SET @prob_id_10 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_10, 1, '`RANK()` 함수를 사용하면 박민수의 순위는 3위가 된다.', NOW(), NOW()),
                                                                                                     (@prob_id_10, 2, '`ROW_NUMBER()` 함수를 사용하면 동일 점수라도 박민수의 순서 번호는 3번이 된다.', NOW(), NOW()),
                                                                                                     (@prob_id_10, 3, '`DENSE_RANK()` 함수를 사용하면 이영희와 공동 순위가 발생하여 박민수의 순위는 3위가 된다.', NOW(), NOW()),
                                                                                                     (@prob_id_10, 4, '`DENSE_RANK()` 함수 적용 시 박민수의 순위는 중복 순위를 축소하여 2위가 된다.', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`)
VALUES (@workbook_id, @prob_id_10, 10, NOW(), NOW());

COMMIT;

START TRANSACTION;

-- =================================================================
-- 1. 2회차 워크북 마스터 정보 등록
-- =================================================================
SET @exam_version_id_2 = (SELECT `id` FROM `exam_version` WHERE `version_name` = 'SQLD 2025 개정판' LIMIT 1);
SET @workbook_type_id_2 = (SELECT `id` FROM `workbook_type` WHERE `code` = 'PAST' LIMIT 1);

-- '2025년 제2회 복원기출문제집' 신규 등록
INSERT INTO `workbook` (`exam_version_id`, `workbook_type_id`, `status`, `created_at`, `updated_at`)
VALUES (@exam_version_id_2, @workbook_type_id_2, 'PUBLISHED', NOW(), NOW());
SET @workbook_id_2 = LAST_INSERT_ID();

-- 2회차 기출 세부 마스터 등록 (시험일자: 2026-06-01 기준 예시)
INSERT INTO `past_exam_workbook` (`workbook_id`, `is_reviewed`, `year`, `round_no`, `total_problem_count`, `time_limit`, `created_at`, `updated_at`, `exam_date`)
VALUES (@workbook_id_2, 1, 2025, 2, 10, 5400, NOW(), NOW(), '2026-06-01 09:00:00');


-- =================================================================
-- 2. 세부 과목 노드 ID 일괄 조회
-- =================================================================
SET @node_model_understand = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-MODELING-01-01' LIMIT 1); -- 데이터모델의 이해
SET @node_normalization    = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-MODELING-02-01' LIMIT 1); -- 정규화
SET @node_select_stmt      = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-SQL-01-02' LIMIT 1);   -- SELECT 문
SET @node_where_clause     = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-SQL-01-04' LIMIT 1);   -- WHERE 절
SET @node_groupby_clause   = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-SQL-01-05' LIMIT 1);   -- GROUP BY, HAVING 절
SET @node_join             = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-SQL-01-07' LIMIT 1);   -- 조인
SET @node_subquery         = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-SQL-02-01' LIMIT 1);   -- 서브쿼리
SET @node_group_func       = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-SQL-02-03' LIMIT 1);   -- 그룹 함수
SET @node_top_n            = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-SQL-02-05' LIMIT 1);   -- Top N 쿼리
SET @node_tcl              = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-SQL-03-02' LIMIT 1);   -- TCL


-- =================================================================
-- 3. 1번 ~ 10번 문제 등록 (2회차 복원기출)
-- =================================================================

-- [문제 1] 데이터 모델링의 특징 (추상화, 단순화, 명확화)
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (@node_model_understand, 'SINGLE_CHOICE', '### 다음 중 데이터 모델링의 주요 특징에 대한 설명으로 가장 올바르지 않은 것은?', 2, '3', '**정답 설명:** 데이터 모델링의 3대 특징은 추상화(단순화하여 표현), 단순화(쉽게 이해하도록 제한), 명확화(한 가지 의미로 해석)입니다. 현실 세계의 복잡한 시스템을 있는 그대로 정밀하게 묘사하는 것은 모델링의 지향점과 거리가 멉니다.', 4, NOW(), NOW());
SET @prob_id_2_1 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_2_1, 1, '추상화: 현실세계를 일정한 양식에 맞춰 간략하게 표현한다.', NOW(), NOW()),
                                                                                                     (@prob_id_2_1, 2, '단순화: 복잡한 현실세계를 누구나 이해하기 쉽게 제한된 표기법으로 표현한다.', NOW(), NOW()),
                                                                                                     (@prob_id_2_1, 3, '정밀화: 현실세계의 비즈니스 프로세스 전반을 왜곡 없이 최대한 구체적이고 정밀하게 누적 묘사한다.', NOW(), NOW()),
                                                                                                     (@prob_id_2_1, 4, '명확화: 대상에 대한 모호함을 제거하고 정확하게 현상을 해석할 수 있도록 설계한다.', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`) VALUES (@workbook_id_2, @prob_id_2_1, 1, NOW(), NOW());


-- [문제 2] 제3정규화 대상 식별 (이행적 함수 종속)
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (@node_normalization, 'SINGLE_CHOICE', '### 다음 엔터티 속성 관계 중 이행적 함수 종속(Transitive Functional Dependency)이 발생하여 제3정규화(3NF)의 대상이 되는 설계 구조는?', 2, '4', '**정답 설명:** 주식별자가 아닌 일반 속성 간에 종속 관계가 존재하는 경우(A -> B, B -> C)를 이행적 함수 종속이라고 합니다. 사원 엔터티에서 부서명 속성이 주식별자인 사원번호가 아닌, 또 다른 일반 속성인 부서코드에 종속되는 구조가 이에 해당합니다.', 4, NOW(), NOW());
SET @prob_id_2_2 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_2_2, 1, '주식별자가 복합키일 때, 일부 속성이 식별자 전체가 아닌 특정 단일 식별자에만 종속되는 경우', NOW(), NOW()),
                                                                                                     (@prob_id_2_2, 2, '하나의 속성이 도메인 원자성을 만족하지 못하고 복수의 속성 값을 지니고 있는 경우', NOW(), NOW()),
                                                                                                     (@prob_id_2_2, 3, '식별자가 타 엔터티의 주식별자를 참조하여 관계선 상에서 외부 식별자로 공유되는 경우', NOW(), NOW()),
                                                                                                     (@prob_id_2_2, 4, '일반 비식별자 속성인 `부서명`이 주식별자가 아닌 또 다른 일반 속성 `부서코드`에 종속되어 있는 경우', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`) VALUES (@workbook_id_2, @prob_id_2_2, 2, NOW(), NOW());


-- [문제 3] SELECT 문 문자열 함수 연산
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (@node_select_stmt, 'SINGLE_CHOICE', '### 다음 중 `SELECT SUBSTR(''SQLD_EXAM'', 1, 4)` 연산을 수행했을 때 반환되는 결과값으로 올바른 것은? (단, SQL 표준 및 Oracle 규칙을 따름)', 2, '1', '**정답 설명:** SUBSTR(문자열, 시작위치, 길이) 함수는 지정한 시작 위치부터 명시한 길이만큼의 문자열을 잘라냅니다. 1번째 글자인 ''S''부터 4글자를 가져오므로 결과는 ''SQLD''가 됩니다.', 4, NOW(), NOW());
SET @prob_id_2_3 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_2_3, 1, '`SQLD`', NOW(), NOW()),
                                                                                                     (@prob_id_2_3, 2, '`SQLD_`', NOW(), NOW()),
                                                                                                     (@prob_id_2_3, 3, '`EXAM`', NOW(), NOW()),
                                                                                                     (@prob_id_2_3, 4, '`SQL`', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`) VALUES (@workbook_id_2, @prob_id_2_3, 3, NOW(), NOW());


-- [문제 4] WHERE 절 NULL 비교 문법 오류 단골
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (@node_where_clause, 'SINGLE_CHOICE', '### 테이블에서 `COMM` 컬럼의 값이 비어있는(NULL) 행들만 올바르게 필터링하기 위한 SQL 조건절로 가장 적절한 것은?', 2, '2', '**정답 설명:** SQL에서 NULL 값은 알 수 없는 값(Unknown)이므로 비교 연산자(`=`, `!=`)로 동등 비교를 수행할 수 없습니다. NULL 데이터를 찾을 때는 반드시 전용 연산자인 `IS NULL`을 사용해야 합니다.', 4, NOW(), NOW());
SET @prob_id_2_4 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_2_4, 1, '`WHERE COMM = NULL`', NOW(), NOW()),
                                                                                                     (@prob_id_2_4, 2, '`WHERE COMM IS NULL`', NOW(), NOW()),
                                                                                                     (@prob_id_2_4, 3, '`WHERE COMM = ''''`', NOW(), NOW()),
                                                                                                     (@prob_id_2_4, 4, '`WHERE COMM IN (NULL)`', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`) VALUES (@workbook_id_2, @prob_id_2_4, 4, NOW(), NOW());


-- [문제 5] GROUP BY 절 수행 시 SELECT 제한 규칙
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (@node_groupby_clause, 'SINGLE_CHOICE', '### 다음 중 `GROUP BY dept_id` 절을 적용한 SQL 문에서 에러 없이 정상적으로 단독 조회할 수 있는 SELECT 절의 항목은?', 2, '1', '**정답 설명:** GROUP BY를 사용하면 그룹화의 기준이 된 컬럼(`dept_id`) 또는 집계 함수(`SUM`, `AVG` 등)가 적용된 데이터만 SELECT 절에 올 수 있습니다. 그룹 기준이 아닌 일반 컬럼(`emp_name`, `salary`)은 단독으로 명시할 수 없습니다.', 4, NOW(), NOW());
SET @prob_id_2_5 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_2_5, 1, '`dept_id` (그룹화 기준 컬럼)', NOW(), NOW()),
                                                                                                     (@prob_id_2_5, 2, '`emp_name` (개별 사원명 컬럼)', NOW(), NOW()),
                                                                                                     (@prob_id_2_5, 3, '`salary` (개별 급여 컬럼)', NOW(), NOW()),
                                                                                                     (@prob_id_2_5, 4, '`dept_id`를 제외한 나머지 모든 테이블 컬럼들', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`) VALUES (@workbook_id_2, @prob_id_2_5, 5, NOW(), NOW());


-- [문제 6] NATURAL JOIN의 특징
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (@node_join, 'SINGLE_CHOICE', '### 두 테이블을 연결하는 NATURAL JOIN에 대한 설명으로 가장 올바르지 않은 것은?', 2, '3', '**정답 설명:** NATURAL JOIN은 두 테이블에서 이름과 데이터 타입이 일치하는 모든 컬럼을 내부적으로 자동으로 찾아 조인 조건으로 삼습니다. 이때 사용자가 `ON` 절을 명시적으로 추가하여 조건을 별도로 기술하면 문법 에러(Syntax Error)가 발생합니다.', 4, NOW(), NOW());
SET @prob_id_2_6 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_2_6, 1, '두 테이블 간 식별자명이 동일한 컬럼을 기준으로 암묵적 조인을 수행한다.', NOW(), NOW()),
                                                                                                     (@prob_id_2_6, 2, '조인 대상이 되는 동일 이름 컬럼들은 데이터 타입 또한 완전히 일치해야 한다.', NOW(), NOW()),
                                                                                                     (@prob_id_2_6, 3, '식별을 보다 명확히 처리하기 위해 `ON` 조건절을 추가하여 조인 대상을 선언할 수 있다.', NOW(), NOW()),
                                                                                                     (@prob_id_2_6, 4, '조인 결과 데이터에서 기준이 된 공통 컬럼은 단 하나만 표현된다.', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`) VALUES (@workbook_id_2, @prob_id_2_6, 6, NOW(), NOW());


-- [문제 7] 스칼라 서브쿼리 특징
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (@node_subquery, 'SINGLE_CHOICE', '### 다음 중 SELECT 절에 사용되는 스칼라 서브쿼리(Scalar Subquery)의 제약 사항으로 가장 올바른 것은?', 2, '1', '**정답 설명:** 스칼라 서브쿼리는 반드시 **단일 행(1 Row)과 단일 컬럼(1 Column)** 형태의 원자값 하나만을 반환해야 합니다. 서브쿼리 결과가 2개 이상의 행이나 컬럼을 도출하면 런타임 에러가 터집니다.', 4, NOW(), NOW());
SET @prob_id_2_7 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_2_7, 1, '서브쿼리 결과는 반드시 하나의 행과 하나의 컬럼 값만 반환해야 한다.', NOW(), NOW()),
                                                                                                     (@prob_id_2_7, 2, '메인쿼리의 테이블 컬럼과 조인 관계를 맺어 연동할 수 없다.', NOW(), NOW()),
                                                                                                     (@prob_id_2_7, 3, '주로 WHERE 절에서 복합 집합 데이터를 필터링하기 위해 선언된다.', NOW(), NOW()),
                                                                                                     (@prob_id_2_7, 4, '성능 최적화를 위해 내부적으로 항상 다중 행 연산자(`IN`) 구조로 변환된다.', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`) VALUES (@workbook_id_2, @prob_id_2_7, 7, NOW(), NOW());


-- [문제 8] 그룹 함수 ROLLUP 데이터 집계
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (@node_group_func, 'SINGLE_CHOICE', '### `GROUP BY ROLLUP(A, B)` 구문을 실행했을 때 데이터가 집계되는 조합의 나열로 올바른 것은? (단, 공집합은 전체 집계를 의미함)', 2, '1', '**정답 설명:** ROLLUP은 계층 구조를 가지며 명시한 컬럼 순서에 따라 우측 컬럼부터 하나씩 제외하며 서브 토탈을 구합니다. `ROLLUP(A, B)`의 집계 그룹 조합은 1) `(A, B)` 조합, 2) `(A)` 소계 조합, 3) `()` 전체 집계 총 3가지입니다.', 4, NOW(), NOW());
SET @prob_id_2_8 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_2_8, 1, '`(A, B)`, `(A)`, `()`', NOW(), NOW()),
                                                                                                     (@prob_id_2_8, 2, '`(A, B)`, `(A)`, `(B)`, `()`', NOW(), NOW()),
                                                                                                     (@prob_id_2_8, 3, '`(A, B)`, `(B)`, `()`', NOW(), NOW()),
                                                                                                     (@prob_id_2_8, 4, '`(A)`, `(B)`, `()`', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`) VALUES (@workbook_id_2, @prob_id_2_8, 8, NOW(), NOW());


-- [문제 9] ROWNUM vs TOP N 절 주의점
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (@node_top_n, 'SINGLE_CHOICE', '### Oracle 환경에서 높은 급여를 받는 상위 3명의 사원을 추출하고자 합니다. 다음 중 잘못 작성되어 원하는 결과를 보장할 수 없는 쿼리는?', 2, '2', '**정답 설명:** Oracle의 `ROWNUM`은 WHERE 절이 평가되면서 임시 번호가 할당되기 때문에, `ORDER BY` 절보다 먼저 실행됩니다. 따라서 2번처럼 정렬 절과 `ROWNUM <= 3`을 한 레벨에 쓰면 무작위 3명을 뽑아서 정렬하게 되므로 잘못된 Top N 결과가 나옵니다. 인라인 뷰를 먼저 정렬하고 바깥에서 잘라내야 합니다.', 4, NOW(), NOW());
SET @prob_id_2_9 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_2_9, 1, '`SELECT * FROM (SELECT * FROM employee ORDER BY salary DESC) WHERE ROWNUM <= 3;`', NOW(), NOW()),
                                                                                                     (@prob_id_2_9, 2, '`SELECT * FROM employee WHERE ROWNUM <= 3 ORDER BY salary DESC;`', NOW(), NOW()),
                                                                                                     (@prob_id_2_9, 3, '`SELECT * FROM employee ORDER BY salary DESC FETCH FIRST 3 ROWS ONLY;`', NOW(), NOW()),
                                                                                                     (@prob_id_2_9, 4, '인라인 뷰 서브쿼리로 내부에서 정렬을 종결한 뒤 서브쿼리 외부에서 ROWNUM을 차단한 필터링 구문', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`) VALUES (@workbook_id_2, @prob_id_2_9, 9, NOW(), NOW());


-- [문제 10] TCL 트랜잭션 병합 관계 (ROLLBACK 범위)
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (@node_tcl, 'SINGLE_CHOICE', '### 다음 중 트랜잭션 제어 구문 사용 시 데이터 상태의 변화에 대한 설명으로 가장 올바르지 않은 항목은?', 2, '2', '**정답 설명:** `COMMIT` 명령어가 한 번 수행되면 해당 트랜잭션의 모든 변경 사항이 데이터베이스에 물리적으로 영구 반영됩니다. 커밋이 완료된 데이터 상태는 뒤이어 아무리 무거운 `ROLLBACK` 처리를 수행하더라도 커밋 이전 시점으로 되돌릴 수 없습니다.', 4, NOW(), NOW());
SET @prob_id_2_10 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_2_10, 1, '`SAVEPOINT`를 지정하면 트랜잭션 전체가 아닌 특정 지점으로만 부분 취소가 가능하다.', NOW(), NOW()),
                                                                                                     (@prob_id_2_10, 2, '데이터 수정 후 `COMMIT`을 완료했더라도 오류가 식별되면 `ROLLBACK`을 통해 복구할 수 있다.', NOW(), NOW()),
                                                                                                     (@prob_id_2_10, 3, '`ROLLBACK`이 실행되면 현재 트랜잭션 중에 발생한 비저장 데이터 변경분이 모두 초기화된다.', NOW(), NOW()),
                                                                                                     (@prob_id_2_10, 4, '트랜잭션 관리 도중 DDL 문이 실행되면 명시적 커밋 없이도 이전 내역들이 자동 커밋 처리되는 경우가 있다.', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`) VALUES (@workbook_id_2, @prob_id_2_10, 10, NOW(), NOW());


COMMIT;



START TRANSACTION;

-- =================================================================
-- 1. 3회차 워크북 마스터 정보 등록
-- =================================================================
SET @exam_version_id_3 = (SELECT `id` FROM `exam_version` WHERE `version_name` = 'SQLD 2025 개정판' LIMIT 1);
SET @workbook_type_id_3 = (SELECT `id` FROM `workbook_type` WHERE `code` = 'PAST' LIMIT 1);

-- '2025년 제3회 복원기출문제집' 신규 등록
INSERT INTO `workbook` (`exam_version_id`, `workbook_type_id`, `status`, `created_at`, `updated_at`)
VALUES (@exam_version_id_3, @workbook_type_id_3, 'PUBLISHED', NOW(), NOW());
SET @workbook_id_3 = LAST_INSERT_ID();

-- 3회차 기출 세부 마스터 등록 (시험일자: 2026-09-01 기준 예시)
INSERT INTO `past_exam_workbook` (`workbook_id`, `is_reviewed`, `year`, `round_no`, `total_problem_count`, `time_limit`, `created_at`, `updated_at`, `exam_date`)
VALUES (@workbook_id_3, 1, 2025, 3, 10, 5400, NOW(), NOW(), '2026-09-01 09:00:00');


-- =================================================================
-- 2. 세부 과목 노드 ID 일괄 조회
-- =================================================================
SET @node_model_understand = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-MODELING-01-01' LIMIT 1); -- 데이터모델의 이해
SET @node_identifier       = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-MODELING-01-05' LIMIT 1); -- 식별자
SET @node_normalization    = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-MODELING-02-01' LIMIT 1); -- 정규화
SET @node_select_stmt      = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-SQL-01-02' LIMIT 1);   -- SELECT 문
SET @node_std_join         = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-SQL-01-08' LIMIT 1);   -- 표준 조인
SET @node_set_operator     = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-SQL-02-02' LIMIT 1);   -- 집합 연산자
SET @node_group_func       = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-SQL-02-03' LIMIT 1);   -- 그룹 함수
SET @node_hierarchical     = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-SQL-02-06' LIMIT 1);   -- 계층형 질의와 셀프 조인
SET @node_pivot            = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-SQL-02-07' LIMIT 1);   -- PIVOT 절과 UNPIVOT 절
SET @node_ddl              = (SELECT `id` FROM `exam_scope_node` WHERE `code` = 'SQLD-SQL-03-03' LIMIT 1);   -- DDL


-- =================================================================
-- 3. 1번 ~ 10번 문제 등록 (3회차 복원기출)
-- =================================================================

-- [문제 1] 데이터 모델링의 세 단계 (개념, 논리, 물리)
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (@node_model_understand, 'SINGLE_CHOICE', '### 다음 중 데이터 모델링의 3단계 진행 과정에 대한 설명으로 가장 올바르지 않은 것은?', 2, '3', '**정답 설명:** 전사적 데이터 모델링을 수행하여 추상화 수준이 가장 높고 업무 중심의 거시적 스케치를 수행하는 단계는 `개념적 데이터 모델링`입니다. 성능, 저장 공간, 구체적인 테이블스페이스 등을 설계하는 단계를 물리적 데이터 모델링이라고 하므로 논리 단계에서 이를 수행한다는 설명은 틀렸습니다.', 4, NOW(), NOW());
SET @prob_id_3_1 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_3_1, 1, '개념적 데이터 모델링: 사용자의 핵심 요구사항을 도출하여 추상화 수준이 가장 높은 ERD를 생성한다.', NOW(), NOW()),
                                                                                                     (@prob_id_3_1, 2, '논리적 데이터 모델링: 특정 DBMS에 독립적인 상태로 비즈니스 식별자 정의 및 정규화를 완료한다.', NOW(), NOW()),
                                                                                                     (@prob_id_3_1, 3, '논리적 데이터 모델링: 데이터 아키텍처 구조의 성능 최적화를 위해 물리적 테이블 스페이스와 세부 인덱스 저장을 직접 결정한다.', NOW(), NOW()),
                                                                                                     (@prob_id_3_1, 4, '물리적 데이터 모델링: 논리 모델을 기반으로 특정 데이터베이스 하드웨어 및 DBMS 스펙에 맞게 테이블과 컬럼 구조를 변환한다.', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`) VALUES (@workbook_id_3, @prob_id_3_1, 1, NOW(), NOW());


-- [문제 2] 구성 속성의 수에 따른 식별자 분류
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (@node_identifier, 'SINGLE_CHOICE', '### 다음 중 엔터티의 식별자를 구성하는 **속성의 수(Number of Attributes)**를 기준으로 분류했을 때 올바르게 짝지어진 것은?', 2, '2', '**정답 설명:** 식별자를 구성하는 속성의 수에 따른 분류 기준은 단일 속성으로 이루어진 `단일 식별자`와 두 개 이상의 속성이 결합하여 만들어진 `복합 식별자`입니다. 주식별자와 보조식별자는 대표성 여부이며, 본질식별자와 인조식별자는 대체 여부에 따른 분류입니다.', 4, NOW(), NOW());
SET @prob_id_3_2 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_3_2, 1, '주식별자, 보조식별자', NOW(), NOW()),
                                                                                                     (@prob_id_3_2, 2, '단일 식별자, 복합 식별자', NOW(), NOW()),
                                                                                                     (@prob_id_3_2, 3, '내부 식별자, 외부 식별자', NOW(), NOW()),
                                                                                                     (@prob_id_3_2, 4, '본질 식별자, 인조 식별자', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`) VALUES (@workbook_id_3, @prob_id_3_2, 2, NOW(), NOW());


-- [문제 3] 제1정규화 대상 식별 (원자성 위배)
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (@node_normalization, 'SINGLE_CHOICE', '### 다음 테이블 구조에서 데이터 모델의 관계형 데이터베이스 무결성을 확보하기 위해 **제1정규화(1NF)**를 적용해야 하는 가장 근본적인 원인은 무엇인가?

| 사원번호 (PK) | 사원명 | 보유자격증 |
| :--- | :--- | :--- |
| 202601 | 김철수 | SQLD, SQLP, ADSP |
| 202602 | 이영희 | 정보처리기사 |', 2, '3', '**정답 설명:** 관계형 데이터베이스에서 모든 속성의 값은 반드시 하나의 값, 즉 **원자값(Atomic Value)**을 가져야 합니다. 현재 `보유자격증` 컬럼에 하나의 행 내부 데이터로 콤마(,)로 구분된 다중값(Multi-value)들이 결합하여 들어와 있으므로, 도메인의 원자성을 보장하기 위해 제1정규화를 수행하여 데이터를 분리해야 합니다.', 4, NOW(), NOW());
SET @prob_id_3_3 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_3_3, 1, '주식별자인 사원번호에 함수적으로 완전히 종속되지 않는 부분 함수 종속성이 존재하기 때문에', NOW(), NOW()),
                                                                                                     (@prob_id_3_3, 2, '식별자가 아닌 일반 비식별자 속성 간에 교차하는 이행적 함수 종속성이 관찰되기 때문에', NOW(), NOW()),
                                                                                                     (@prob_id_3_3, 3, '`보유자격증` 컬럼이 도메인 원자성을 만족하지 못하고 복수의 다중값(Multi-value)을 포함하기 때문에', NOW(), NOW()),
                                                                                                     (@prob_id_3_3, 4, '결정자 구조가 후보키 집합에 포함되지 않아 외래키 참조 무결성이 깨지기 때문에', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`) VALUES (@workbook_id_3, @prob_id_3_3, 3, NOW(), NOW());


-- [문제 4] SELECT 문 NULL 변환 함수 (NVL, COALESCE) 차이점
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (@node_select_stmt, 'SINGLE_CHOICE', '### 다음 중 각 데이터베이스 제품별 NULL 처리 함수의 연산 결과가 올바르지 않은 항목은? (단, 각 테이블의 해당 컬럼 값은 NULL 임)', 2, '4', '**정답 설명:** `COALESCE` 함수는 인자로 주어진 표현식 목록 중 **NULL이 아닌 첫 번째 값**을 반환하는 가변 인자 표준 함수입니다. `COALESCE(NULL, NULL, ''SQLD'')`를 수행하면 처음으로 NULL이 아닌 값인 `''SQLD''`가 반환되어야 하므로 NULL을 그대로 반환한다는 설명은 틀렸습니다.', 4, NOW(), NOW());
SET @prob_id_3_4 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_3_4, 1, 'Oracle에서 `NVL(NULL, 0)`의 결과값은 `0`이다.', NOW(), NOW()),
                                                                                                     (@prob_id_3_4, 2, 'SQL Server에서 `ISNULL(NULL, ''N'')`의 결과값은 `''N''`이다.', NOW(), NOW()),
                                                                                                     (@prob_id_3_4, 3, 'MySQL에서 `IFNULL(NULL, 100)`의 결과값은 `100`이다.', NOW(), NOW()),
                                                                                                     (@prob_id_3_4, 4, '표준 SQL 구문에서 `COALESCE(NULL, NULL, ''SQLD'')`의 결과값은 `NULL`이다.', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`) VALUES (@workbook_id_3, @prob_id_3_4, 4, NOW(), NOW());


-- [문제 5] 표준 조인 USING 절 식별자 사용 제약 조건
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (@node_std_join, 'SINGLE_CHOICE', '### ANSI 표준 SQL의 `USING` 조건절을 활용하여 JOIN을 수행할 때의 제약 사항으로 가장 올바른 구문 특징은?', 2, '2', '**정답 설명:** `USING` 절에 명시된 공통 조인 컬럼은 SELECT 절이나 조건절에서 테이블명이나 에일리어스(Alias) 같은 접두사를 붙여 참조할 수 없습니다. 즉 `E.dept_id`와 같이 별칭을 명시하면 컴파일/구문 문법 오류가 발생합니다.', 4, NOW(), NOW());
SET @prob_id_3_5 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_3_5, 1, 'USING 절에 명시하는 조인 대상 컬럼들은 두 테이블 간 이름이 서로 달라도 무방하다.', NOW(), NOW()),
                                                                                                     (@prob_id_3_5, 2, '조인 조건의 기준이 된 컬럼은 SELECT 절 등에서 테이블 별칭(Alias)이나 접두사를 붙여서 사용할 수 없다.', NOW(), NOW()),
                                                                                                     (@prob_id_3_5, 3, 'USING 절을 사용하면 내부적으로 반드시 OUTER JOIN 구조로만 강제 연동된다.', NOW(), NOW()),
                                                                                                     (@prob_id_3_5, 4, '상호 호환성을 위해 `USING` 절과 `ON` 절을 하나의 JOIN 문법 안에서 동시에 선언하여 복합 결합해야 한다.', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`) VALUES (@workbook_id_3, @prob_id_3_5, 5, NOW(), NOW());


-- [문제 6] 집합 연산자 UNION vs UNION ALL 중복 및 정렬 특성
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (@node_set_operator, 'SINGLE_CHOICE', '### 다음 중 집합 연산자(Set Operator)에 대한 설명으로 가장 올바르지 않은 것은?', 2, '1', '**정답 설명:** `UNION` 연산자는 두 집합의 결과를 병합할 때 **중복된 행을 제거**하며, 이 과정에서 내부적으로 시스템 정렬(Sort) 연산이 동반됩니다. 반면 중복을 포함한 모든 행을 정렬 없이 그대로 결합하여 성능상 유리한 연산자는 `UNION ALL`입니다.', 4, NOW(), NOW());
SET @prob_id_3_6 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_3_6, 1, '`UNION`은 두 집합의 결과를 합칠 때 중복 데이터를 포함하여 반환하므로 내부 정렬을 유발하지 않는다.', NOW(), NOW()),
                                                                                                     (@prob_id_3_6, 2, '`INTERSECT`는 두 집합의 교집합을 반환하며, 결과에서 중복된 행은 자동으로 제거된다.', NOW(), NOW()),
                                                                                                     (@prob_id_3_6, 3, '`MINUS`(Oracle) 또는 `EXCEPT`(SQL Server)는 첫 번째 집합에서 두 번째 집합의 결과 데이터를 제외한 차집합을 반환한다.', NOW(), NOW()),
                                                                                                     (@prob_id_3_6, 4, '집합 연산자로 연결되는 두 쿼리의 SELECT 절 컬럼 수와 데이터 타입 순서는 서로 상호 호환되도록 일치해야 한다.', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`) VALUES (@workbook_id_3, @prob_id_3_6, 6, NOW(), NOW());


-- [문제 7] 그룹 함수 CUBE 다차원 데이터 집계 조합 개수 구하기
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (@node_group_func, 'SINGLE_CHOICE', '### 다음 중 `GROUP BY CUBE(dept_id, job_code)` 구문을 실행했을 때 연산되어 출력되는 서브 토탈 데이터 집계 조합의 총 개수로 옳은 것은?', 2, '3', '**정답 설명:** `CUBE` 함수는 결합 가능한 모든 컬럼 조합에 대해 다차원 집계를 수행하는 전방위 그룹 함수입니다. 지정된 컬럼의 개수가 $n$개일 때 생성되는 소계 조합의 가짓수는 $2^n$개입니다. 컬럼이 2개(`dept_id`, `job_code`)이므로 $2^2 = 4$가지 조합 `(dept_id, job_code)`, `(dept_id)`, `(job_code)`, `()`가 도출됩니다.', 4, NOW(), NOW());
SET @prob_id_3_7 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_3_7, 1, '2개', NOW(), NOW()),
                                                                                                     (@prob_id_3_7, 2, '3개', NOW(), NOW()),
                                                                                                     (@prob_id_3_7, 3, '4개', NOW(), NOW()),
                                                                                                     (@prob_id_3_7, 4, '8개', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`) VALUES (@workbook_id_3, @prob_id_3_7, 7, NOW(), NOW());

-- [문제 8] 계층형 질의 PRIOR 연산자 위치에 따른 방향성 판별
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (
           @node_hierarchical,
           'SINGLE_CHOICE',
           '### Oracle의 계층형 질의문에서 다음 조건절이 의미하는 계층 구조의 전개 방향에 대한 설명으로 가장 올바른 것은?

```sql
START WITH id = 100
CONNECT BY id = PRIOR parent_id;
```',
           2,
           '2',
           '**정답 설명:**
       계층형 질의에서 전개 방향은 `PRIOR` 연산자가 어느 컬럼에 붙어있느냐에 따라 결정됩니다.
       * `CONNECT BY 자식_컬럼 = PRIOR 부모_컬럼` 구조인 경우, 부모에서 자식 방향으로 내려가는 **순방향 전개**입니다.
       * 본 문제처럼 `CONNECT BY id(부모) = PRIOR parent_id(자식)` 혹은 `PRIOR 자식_컬럼 = 부모_컬럼` 구조인 경우, 자식에서 부모 방향으로 올라가는 **역방향 전개**가 됩니다.

       따라서 `id가 100인 행을 시작으로 하위 노드에서 상위 상속 노드로 거슬러 올라가는 역방향 전개`인 2번이 정답입니다.',
           4, NOW(), NOW()
       );
SET @prob_id_3_8 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_3_8, 1, 'id가 100인 행을 루트 노드로 삼아 자식 노드 방향으로 내려가는 순방향 전개이다.', NOW(), NOW()),
                                                                                                     (@prob_id_3_8, 2, 'id가 100인 행을 기점으로 부모 노드를 찾아 거슬러 올라가는 역방향 전개이다.', NOW(), NOW()),
                                                                                                     (@prob_id_3_8, 3, 'PRIOR 연산자가 양쪽 컬럼에 영향을 주어 무한 루프 사이클을 유발하는 에러 구문이다.', NOW(), NOW()),
                                                                                                     (@prob_id_3_8, 4, 'START WITH 절의 조건과 상충되어 어떠한 행도 결과로 도출하지 못하는 무효 구문이다.', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`)
VALUES (@workbook_id_3, @prob_id_3_8, 8, NOW(), NOW());


-- [문제 9] PIVOT 절과 UNPIVOT 절의 기능적 특성
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (
           @node_pivot,
           'SINGLE_CHOICE',
           '### SQLD 교재 개정판에 추가된 PIVOT 및 UNPIVOT 절에 대한 설명 중 가장 올바르지 않은 항목은?',
           2,
           '4',
           '**정답 설명:**
       * `PIVOT` 절은 행(Row) 형태로 나열된 데이터를 열(Column) 형태로 전환하여 가로로 넓은 테이블 집계를 만듭니다. (1, 2번 설명 올바름)
       * `UNPIVOT` 절은 반대로 열(Column) 구조로 되어 있는 데이터를 행(Row) 구조로 쪼개어 세로로 긴 형태로 변환합니다. (3번 설명 올바름)

       4번 지문에서 `UNPIVOT을 수행하면 다차원 집계인 CUBE와 동일한 합계 데이터 행이 생성된다`고 기술하였으나, UNPIVOT은 단순히 컬럼을 행으로 회전(Unpivot)시키는 정형 변환 구문일 뿐, 소계나 총계를 계산하는 그룹 함수가 아닙니다. 따라서 4번 진술은 완전히 틀렸습니다.',
           4, NOW(), NOW()
       );
SET @prob_id_3_9 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_3_9, 1, '`PIVOT` 절은 행(Row) 데이터를 열(Column) 데이터로 회전시켜 전개하는 가독성 중심의 구문이다.', NOW(), NOW()),
                                                                                                     (@prob_id_3_9, 2, 'PIVOT 절 내부에서는 변환 기준이 되는 컬럼에 대해 반드시 집계 함수(SUM, COUNT 등)를 지정해야 한다.', NOW(), NOW()),
                                                                                                     (@prob_id_3_9, 3, '`UNPIVOT` 절은 PIVOT과 반대로 다중 열(Column) 구조를 단일 행(Row) 집합으로 가로에서 세로로 전개한다.', NOW(), NOW()),
                                                                                                     (@prob_id_3_9, 4, '`UNPIVOT` 연산을 수행하면 다차원 집계인 CUBE 연산과 동일하게 각 그룹별 합계 데이터 행이 하단에 자동 생성된다.', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`)
VALUES (@workbook_id_3, @prob_id_3_9, 9, NOW(), NOW());


-- [문제 10] DDL 문 명령어 특징 (TRUNCATE vs DROP vs DELETE 비교)
INSERT INTO `problem` (`exam_scope_node_id`, `format`, `content`, `score`, `answer`, `explanation`, `choice_count`, `created_at`, `updated_at`)
VALUES (
           @node_ddl,
           'SINGLE_CHOICE',
           '### 다음 중 데이터 가공 및 삭제 명령어에 대한 설명으로 가장 올바르지 않은 것은?',
           2,
           '1',
           '**정답 설명:**
       * **`TRUNCATE TABLE`** 명령어는 테이블 내의 모든 데이터 행을 삭제하고 사용 공간을 반납하는 **DDL(Data Definition Language)** 명령어입니다.
       * DDL은 실행 즉시 자동 커밋(Auto Commit)이 발생하기 때문에, `ROLLBACK`을 통해 데이터를 복구할 수 없습니다.
       * 트랜잭션 로그를 최소한으로 남겨 `DELETE`보다 속도가 빠르다는 장점이 있으나 복구가 불가능하므로 1번 진술은 거짓입니다.

       * 참고: `DELETE`는 DML이므로 롤백이 가능하고, `DROP`은 테이블 구조 자체를 스키마에서 완전히 제거합니다.',
           4, NOW(), NOW()
       );
SET @prob_id_3_10 = LAST_INSERT_ID();

INSERT INTO `problem_choice` (`problem_id`, `sort_order`, `content`, `created_at`, `updated_at`) VALUES
                                                                                                     (@prob_id_3_10, 1, '`TRUNCATE` 명령어는 복구를 위한 트랜잭션 세이브포인트를 유지하므로 수행 후 `ROLLBACK`이 가능하다.', NOW(), NOW()),
                                                                                                     (@prob_id_3_10, 2, '`DROP TABLE` 명령어를 실행하면 테이블 데이터뿐만 아니라 디스크 상의 테이블 물리 구조 자체가 영구 삭제된다.', NOW(), NOW()),
                                                                                                     (@prob_id_3_10, 3, '`DELETE` 명령어는 DML문으로 행 단위 삭감을 수행하며, 조건절(WHERE)을 부여하여 특정 행만 골라 제거할 수 있다.', NOW(), NOW()),
                                                                                                     (@prob_id_3_10, 4, '`TRUNCATE`는 디스크 공간을 초기화 상태로 반납시키고 테이블 정의 및 스키마 구조는 그대로 남겨둔다.', NOW(), NOW());

INSERT INTO `workbook_item` (`workbook_id`, `problem_id`, `sort_order`, `created_at`, `updated_at`)
VALUES (@workbook_id_3, @prob_id_3_10, 10, NOW(), NOW());


-- =================================================================
-- 4. 3회차 트랜잭션 최종 반영 및 종결
-- =================================================================
COMMIT;