START TRANSACTION;

INSERT INTO `exam` (`code`, `name`, `status`) VALUES ('SQLD', 'SQL 개발자(SQLD)', 'ACTIVE');
SET @exam_id = LAST_INSERT_ID();

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

COMMIT;

