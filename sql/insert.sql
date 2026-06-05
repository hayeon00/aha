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
SELECT p.`exam_version_id`, p.`exam_part_id`, x.`code`, p.`id`, 'TOPIC', 2, x.`title`, 0, 1, x.`display_order`
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


INSERT INTO `exam_scope_node` (`exam_version_id`, `exam_part_id`, `code`, `parent_id`, `node_type`, `depth`, `title`, `is_leaf`, `is_active`, `display_order`)
SELECT p.`exam_version_id`, p.`exam_part_id`, CONCAT(p.`code`, '-', LPAD(CAST(x.`display_order` AS CHAR), 2, '0')), p.`id`, 'CONCEPT', 3, x.`title`, 1, 1, x.`display_order`
FROM `exam_scope_node` p
         JOIN (
    SELECT 'SQLD-MODELING-01-01' parent_code, 1 display_order, '데이터 모델링의 정의' title
    UNION ALL SELECT 'SQLD-MODELING-01-01', 2, '데이터 모델링의 목적'
    UNION ALL SELECT 'SQLD-MODELING-01-01', 3, '데이터 모델링의 특징'
    UNION ALL SELECT 'SQLD-MODELING-01-01', 4, '데이터 모델링의 단계'
    UNION ALL SELECT 'SQLD-MODELING-01-01', 5, '개념적·논리적·물리적 모델링'
    UNION ALL SELECT 'SQLD-MODELING-01-01', 6, '데이터 독립성'
    UNION ALL SELECT 'SQLD-MODELING-01-02', 1, '엔터티의 정의'
    UNION ALL SELECT 'SQLD-MODELING-01-02', 2, '엔터티의 특징'
    UNION ALL SELECT 'SQLD-MODELING-01-02', 3, '엔터티의 분류'
    UNION ALL SELECT 'SQLD-MODELING-01-02', 4, '기본 엔터티와 중심 엔터티'
    UNION ALL SELECT 'SQLD-MODELING-01-02', 5, '행위 엔터티'
    UNION ALL SELECT 'SQLD-MODELING-01-02', 6, '엔터티 명명 규칙'
    UNION ALL SELECT 'SQLD-MODELING-01-03', 1, '속성의 정의'
    UNION ALL SELECT 'SQLD-MODELING-01-03', 2, '속성의 특징'
    UNION ALL SELECT 'SQLD-MODELING-01-03', 3, '속성의 분류'
    UNION ALL SELECT 'SQLD-MODELING-01-03', 4, '기본 속성·설계 속성·파생 속성'
    UNION ALL SELECT 'SQLD-MODELING-01-03', 5, '단일값 속성과 다중값 속성'
    UNION ALL SELECT 'SQLD-MODELING-01-03', 6, '도메인'
    UNION ALL SELECT 'SQLD-MODELING-01-04', 1, '관계의 정의'
    UNION ALL SELECT 'SQLD-MODELING-01-04', 2, '관계의 구성 요소'
    UNION ALL SELECT 'SQLD-MODELING-01-04', 3, '관계 차수'
    UNION ALL SELECT 'SQLD-MODELING-01-04', 4, '관계 선택성'
    UNION ALL SELECT 'SQLD-MODELING-01-04', 5, '식별 관계와 비식별 관계'
    UNION ALL SELECT 'SQLD-MODELING-01-04', 6, '관계 명명 규칙'
    UNION ALL SELECT 'SQLD-MODELING-01-05', 1, '식별자의 정의'
    UNION ALL SELECT 'SQLD-MODELING-01-05', 2, '주식별자'
    UNION ALL SELECT 'SQLD-MODELING-01-05', 3, '보조식별자'
    UNION ALL SELECT 'SQLD-MODELING-01-05', 4, '후보식별자'
    UNION ALL SELECT 'SQLD-MODELING-01-05', 5, '내부식별자와 외부식별자'
    UNION ALL SELECT 'SQLD-MODELING-01-05', 6, '단일식별자와 복합식별자'
    UNION ALL SELECT 'SQLD-MODELING-01-05', 7, '본질식별자와 인조식별자'
    UNION ALL SELECT 'SQLD-MODELING-02-01', 1, '정규화의 목적'
    UNION ALL SELECT 'SQLD-MODELING-02-01', 2, '이상 현상'
    UNION ALL SELECT 'SQLD-MODELING-02-01', 3, '제1정규형'
    UNION ALL SELECT 'SQLD-MODELING-02-01', 4, '제2정규형'
    UNION ALL SELECT 'SQLD-MODELING-02-01', 5, '제3정규형'
    UNION ALL SELECT 'SQLD-MODELING-02-01', 6, '반정규화'
    UNION ALL SELECT 'SQLD-MODELING-02-01', 7, '정규화와 성능'
    UNION ALL SELECT 'SQLD-MODELING-02-02', 1, '관계형 모델에서의 관계'
    UNION ALL SELECT 'SQLD-MODELING-02-02', 2, '부모 테이블과 자식 테이블'
    UNION ALL SELECT 'SQLD-MODELING-02-02', 3, '기본키와 외래키'
    UNION ALL SELECT 'SQLD-MODELING-02-02', 4, '관계를 이용한 조인'
    UNION ALL SELECT 'SQLD-MODELING-02-02', 5, '조인 결과 해석'
    UNION ALL SELECT 'SQLD-MODELING-02-03', 1, '트랜잭션의 의미'
    UNION ALL SELECT 'SQLD-MODELING-02-03', 2, '업무 규칙과 트랜잭션'
    UNION ALL SELECT 'SQLD-MODELING-02-03', 3, '필수 관계와 선택 관계'
    UNION ALL SELECT 'SQLD-MODELING-02-03', 4, '모델에서 트랜잭션 읽는 방법'
    UNION ALL SELECT 'SQLD-MODELING-02-04', 1, 'NULL의 의미'
    UNION ALL SELECT 'SQLD-MODELING-02-04', 2, 'NULL과 공백의 차이'
    UNION ALL SELECT 'SQLD-MODELING-02-04', 3, 'NULL과 연산 결과'
    UNION ALL SELECT 'SQLD-MODELING-02-04', 4, 'NULL과 집계 함수'
    UNION ALL SELECT 'SQLD-MODELING-02-04', 5, '모델링에서 NULL 허용 여부'
    UNION ALL SELECT 'SQLD-MODELING-02-05', 1, '본질식별자의 의미'
    UNION ALL SELECT 'SQLD-MODELING-02-05', 2, '인조식별자의 의미'
    UNION ALL SELECT 'SQLD-MODELING-02-05', 3, '본질식별자의 장단점'
    UNION ALL SELECT 'SQLD-MODELING-02-05', 4, '인조식별자의 장단점'
    UNION ALL SELECT 'SQLD-MODELING-02-05', 5, '식별자 선택 기준'
    UNION ALL SELECT 'SQLD-SQL-01-01', 1, '관계형 데이터베이스의 개념'
    UNION ALL SELECT 'SQLD-SQL-01-01', 2, '테이블·행·열'
    UNION ALL SELECT 'SQLD-SQL-01-01', 3, '기본키와 외래키'
    UNION ALL SELECT 'SQLD-SQL-01-01', 4, 'SQL의 종류'
    UNION ALL SELECT 'SQLD-SQL-01-01', 5, 'SQL 문장 작성 규칙'
    UNION ALL SELECT 'SQLD-SQL-01-02', 1, 'SELECT 문의 기본 구조'
    UNION ALL SELECT 'SQLD-SQL-01-02', 2, 'SELECT 절과 FROM 절'
    UNION ALL SELECT 'SQLD-SQL-01-02', 3, '컬럼 별칭'
    UNION ALL SELECT 'SQLD-SQL-01-02', 4, '산술 표현식'
    UNION ALL SELECT 'SQLD-SQL-01-02', 5, 'DISTINCT'
    UNION ALL SELECT 'SQLD-SQL-01-02', 6, 'NULL 값 조회'
    UNION ALL SELECT 'SQLD-SQL-01-03', 1, '문자 함수'
    UNION ALL SELECT 'SQLD-SQL-01-03', 2, '숫자 함수'
    UNION ALL SELECT 'SQLD-SQL-01-03', 3, '날짜 함수'
    UNION ALL SELECT 'SQLD-SQL-01-03', 4, '변환 함수'
    UNION ALL SELECT 'SQLD-SQL-01-03', 5, 'NULL 관련 함수'
    UNION ALL SELECT 'SQLD-SQL-01-03', 6, 'CASE 표현식'
    UNION ALL SELECT 'SQLD-SQL-01-03', 7, '함수 중첩 사용'
    UNION ALL SELECT 'SQLD-SQL-01-04', 1, 'WHERE 절의 역할'
    UNION ALL SELECT 'SQLD-SQL-01-04', 2, '비교 연산자'
    UNION ALL SELECT 'SQLD-SQL-01-04', 3, '논리 연산자'
    UNION ALL SELECT 'SQLD-SQL-01-04', 4, 'BETWEEN'
    UNION ALL SELECT 'SQLD-SQL-01-04', 5, 'IN'
    UNION ALL SELECT 'SQLD-SQL-01-04', 6, 'LIKE'
    UNION ALL SELECT 'SQLD-SQL-01-04', 7, 'IS NULL'
    UNION ALL SELECT 'SQLD-SQL-01-05', 1, '집계 함수'
    UNION ALL SELECT 'SQLD-SQL-01-05', 2, 'GROUP BY 절의 역할'
    UNION ALL SELECT 'SQLD-SQL-01-05', 3, 'GROUP BY 사용 시 SELECT 절 제한'
    UNION ALL SELECT 'SQLD-SQL-01-05', 4, 'HAVING 절의 역할'
    UNION ALL SELECT 'SQLD-SQL-01-05', 5, 'WHERE와 HAVING의 차이'
    UNION ALL SELECT 'SQLD-SQL-01-06', 1, 'ORDER BY 절의 역할'
    UNION ALL SELECT 'SQLD-SQL-01-06', 2, '오름차순과 내림차순'
    UNION ALL SELECT 'SQLD-SQL-01-06', 3, '여러 컬럼 정렬'
    UNION ALL SELECT 'SQLD-SQL-01-06', 4, 'NULL 정렬'
    UNION ALL SELECT 'SQLD-SQL-01-06', 5, 'SELECT 실행 순서와 ORDER BY'
    UNION ALL SELECT 'SQLD-SQL-01-07', 1, '조인의 개념'
    UNION ALL SELECT 'SQLD-SQL-01-07', 2, 'EQUI JOIN'
    UNION ALL SELECT 'SQLD-SQL-01-07', 3, 'NON EQUI JOIN'
    UNION ALL SELECT 'SQLD-SQL-01-07', 4, 'INNER JOIN'
    UNION ALL SELECT 'SQLD-SQL-01-07', 5, 'OUTER JOIN'
    UNION ALL SELECT 'SQLD-SQL-01-07', 6, 'SELF JOIN'
    UNION ALL SELECT 'SQLD-SQL-01-08', 1, 'ANSI 표준 JOIN 문법'
    UNION ALL SELECT 'SQLD-SQL-01-08', 2, 'INNER JOIN'
    UNION ALL SELECT 'SQLD-SQL-01-08', 3, 'LEFT OUTER JOIN'
    UNION ALL SELECT 'SQLD-SQL-01-08', 4, 'RIGHT OUTER JOIN'
    UNION ALL SELECT 'SQLD-SQL-01-08', 5, 'FULL OUTER JOIN'
    UNION ALL SELECT 'SQLD-SQL-01-08', 6, 'CROSS JOIN'
    UNION ALL SELECT 'SQLD-SQL-01-08', 7, 'NATURAL JOIN'
    UNION ALL SELECT 'SQLD-SQL-02-01', 1, '서브쿼리의 개념'
    UNION ALL SELECT 'SQLD-SQL-02-01', 2, '단일 행 서브쿼리'
    UNION ALL SELECT 'SQLD-SQL-02-01', 3, '다중 행 서브쿼리'
    UNION ALL SELECT 'SQLD-SQL-02-01', 4, '다중 컬럼 서브쿼리'
    UNION ALL SELECT 'SQLD-SQL-02-01', 5, '상호연관 서브쿼리'
    UNION ALL SELECT 'SQLD-SQL-02-01', 6, '스칼라 서브쿼리'
    UNION ALL SELECT 'SQLD-SQL-02-01', 7, 'EXISTS'
    UNION ALL SELECT 'SQLD-SQL-02-02', 1, 'UNION'
    UNION ALL SELECT 'SQLD-SQL-02-02', 2, 'UNION ALL'
    UNION ALL SELECT 'SQLD-SQL-02-02', 3, 'INTERSECT'
    UNION ALL SELECT 'SQLD-SQL-02-02', 4, 'MINUS 또는 EXCEPT'
    UNION ALL SELECT 'SQLD-SQL-02-02', 5, '집합 연산자 사용 시 주의점'
    UNION ALL SELECT 'SQLD-SQL-02-03', 1, 'ROLLUP'
    UNION ALL SELECT 'SQLD-SQL-02-03', 2, 'CUBE'
    UNION ALL SELECT 'SQLD-SQL-02-03', 3, 'GROUPING SETS'
    UNION ALL SELECT 'SQLD-SQL-02-03', 4, 'GROUPING 함수'
    UNION ALL SELECT 'SQLD-SQL-02-03', 5, '소계와 합계 결과 해석'
    UNION ALL SELECT 'SQLD-SQL-02-04', 1, '윈도우 함수의 개념'
    UNION ALL SELECT 'SQLD-SQL-02-04', 2, 'OVER 절'
    UNION ALL SELECT 'SQLD-SQL-02-04', 3, 'PARTITION BY'
    UNION ALL SELECT 'SQLD-SQL-02-04', 4, 'ORDER BY'
    UNION ALL SELECT 'SQLD-SQL-02-04', 5, '순위 함수'
    UNION ALL SELECT 'SQLD-SQL-02-04', 6, '집계 윈도우 함수'
    UNION ALL SELECT 'SQLD-SQL-02-04', 7, 'LAG와 LEAD'
    UNION ALL SELECT 'SQLD-SQL-02-04', 8, 'ROWS와 RANGE'
    UNION ALL SELECT 'SQLD-SQL-02-05', 1, 'Top N 쿼리의 개념'
    UNION ALL SELECT 'SQLD-SQL-02-05', 2, 'ROWNUM'
    UNION ALL SELECT 'SQLD-SQL-02-05', 3, 'ROW_NUMBER'
    UNION ALL SELECT 'SQLD-SQL-02-05', 4, 'RANK와 DENSE_RANK'
    UNION ALL SELECT 'SQLD-SQL-02-05', 5, 'Top N 쿼리 작성 시 주의점'
    UNION ALL SELECT 'SQLD-SQL-02-06', 1, '계층형 데이터의 개념'
    UNION ALL SELECT 'SQLD-SQL-02-06', 2, 'START WITH'
    UNION ALL SELECT 'SQLD-SQL-02-06', 3, 'CONNECT BY'
    UNION ALL SELECT 'SQLD-SQL-02-06', 4, 'PRIOR'
    UNION ALL SELECT 'SQLD-SQL-02-06', 5, 'LEVEL'
    UNION ALL SELECT 'SQLD-SQL-02-06', 6, '셀프 조인의 개념'
    UNION ALL SELECT 'SQLD-SQL-02-06', 7, '계층형 질의와 셀프 조인의 차이'
    UNION ALL SELECT 'SQLD-SQL-02-07', 1, 'PIVOT의 개념'
    UNION ALL SELECT 'SQLD-SQL-02-07', 2, 'PIVOT 문법'
    UNION ALL SELECT 'SQLD-SQL-02-07', 3, 'UNPIVOT의 개념'
    UNION ALL SELECT 'SQLD-SQL-02-07', 4, 'UNPIVOT 문법'
    UNION ALL SELECT 'SQLD-SQL-02-07', 5, '행과 열 변환 결과 해석'
    UNION ALL SELECT 'SQLD-SQL-02-08', 1, '정규 표현식의 개념'
    UNION ALL SELECT 'SQLD-SQL-02-08', 2, 'REGEXP_LIKE'
    UNION ALL SELECT 'SQLD-SQL-02-08', 3, 'REGEXP_REPLACE'
    UNION ALL SELECT 'SQLD-SQL-02-08', 4, 'REGEXP_SUBSTR'
    UNION ALL SELECT 'SQLD-SQL-02-08', 5, '주요 패턴 문자'
    UNION ALL SELECT 'SQLD-SQL-02-08', 6, '정규 표현식 사용 시 주의점'
    UNION ALL SELECT 'SQLD-SQL-03-01', 1, 'INSERT'
    UNION ALL SELECT 'SQLD-SQL-03-01', 2, 'UPDATE'
    UNION ALL SELECT 'SQLD-SQL-03-01', 3, 'DELETE'
    UNION ALL SELECT 'SQLD-SQL-03-01', 4, 'MERGE'
    UNION ALL SELECT 'SQLD-SQL-03-01', 5, 'DML 사용 시 주의점'
    UNION ALL SELECT 'SQLD-SQL-03-02', 1, '트랜잭션의 개념'
    UNION ALL SELECT 'SQLD-SQL-03-02', 2, 'COMMIT'
    UNION ALL SELECT 'SQLD-SQL-03-02', 3, 'ROLLBACK'
    UNION ALL SELECT 'SQLD-SQL-03-02', 4, 'SAVEPOINT'
    UNION ALL SELECT 'SQLD-SQL-03-02', 5, '트랜잭션 제어 시 주의점'
    UNION ALL SELECT 'SQLD-SQL-03-03', 1, 'CREATE'
    UNION ALL SELECT 'SQLD-SQL-03-03', 2, 'ALTER'
    UNION ALL SELECT 'SQLD-SQL-03-03', 3, 'DROP'
    UNION ALL SELECT 'SQLD-SQL-03-03', 4, 'TRUNCATE'
    UNION ALL SELECT 'SQLD-SQL-03-03', 5, '제약조건'
    UNION ALL SELECT 'SQLD-SQL-03-03', 6, 'DDL과 자동 COMMIT'
    UNION ALL SELECT 'SQLD-SQL-03-04', 1, '권한의 개념'
    UNION ALL SELECT 'SQLD-SQL-03-04', 2, 'GRANT'
    UNION ALL SELECT 'SQLD-SQL-03-04', 3, 'REVOKE'
    UNION ALL SELECT 'SQLD-SQL-03-04', 4, 'ROLE'
    UNION ALL SELECT 'SQLD-SQL-03-04', 5, '권한 관리 시 주의점'
) x ON p.`code` = x.`parent_code`
WHERE p.`exam_version_id` = @exam_version_id AND p.`node_type` = 'TOPIC';

COMMIT;



/*========================================
  문제 삽입
   ========================================*/
START TRANSACTION;

SET @exam_id = (
    SELECT id
    FROM exam
    WHERE code = 'SQLD'
    LIMIT 1
);

SET @exam_version_id = (
    SELECT id
    FROM exam_version
    WHERE exam_id = @exam_id
      AND version_no = 2025
    LIMIT 1
);

/* =========================================================
   1번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id,
    exam_version_id,
    exam_scope_node_id,
    ai_generated_problem_id,
    expression_type,
    difficulty,
    question_content_json,
    explanation_json,
    answer_type,
    answer_json,
    choice_type,
    source_type,
    is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-MODELING-01-02'
               LIMIT 1
           ),
           NULL,
           'CONCEPT_CLASSIFICATION',
           'EASY',
           JSON_OBJECT(
                   'stem', '다음 설명에 해당하는 엔터티 분류는?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'QUOTE',
                                   'content', '해당 엔터티는 업무 수행 과정에서 지속적으로 이벤트가 쌓이므로 데이터가 자주 변경되고 저장되는 양 또한 매우 많다.'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', '업무 처리 과정에서 이벤트가 지속적으로 발생하고 데이터가 빈번히 누적·변경되는 엔터티는 행위 엔터티다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '행위 엔터티는 주문, 결제, 이용내역처럼 업무 행위가 발생할 때마다 데이터가 쌓이는 엔터티다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(1)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '행위 엔터티')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '개념 엔터티')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '중심 엔터티')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '기본 엔터티'))));


/* =========================================================
   2번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-MODELING-01-03'
               LIMIT 1
           ),
           NULL,
           'CONCEPT_CLASSIFICATION',
           'EASY',
           JSON_OBJECT(
                   'stem', '아래 [직원] 엔터티의 속성 중 성격이 나머지와 다른 것은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'TABLE',
                                   'caption', '직원 엔터티',
                                   'content', '| 속성명 | 예시 값 |\n|---|---|\n| 사번 | E1001 |\n| 생년월일 | 1990-05-14 |\n| 연령 | 35 |\n| 부서코드 | D10 |'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', '연령은 생년월일로부터 계산 가능한 파생 속성으로 볼 수 있어 성격이 다르다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '사번, 생년월일, 부서코드는 원천 데이터에 가깝고, 연령은 시점에 따라 계산되는 파생 속성이다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(1)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '연령')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '사번')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '생년월일')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '부서코드'))));


/* =========================================================
   3번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-MODELING-01-03'
               LIMIT 1
           ),
           NULL,
           'CONCEPT_TRUE_FALSE',
           'EASY',
           JSON_OBJECT(
                   'stem', '엔터티·인스턴스·속성에 대한 설명 중 옳지 않은 것은?',
                   'content_blocks', JSON_ARRAY()
           ),
           JSON_OBJECT(
                   'summary', '하나의 속성은 한 인스턴스에서 하나의 값만 가져야 한다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '속성에 여러 값이 들어가면 다중값 속성이 되어 정규화 대상이 되며, 일반적인 엔터티 속성 설계 원칙에 어긋난다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(2)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '엔터티는 두 개 이상의 인스턴스를 가진다.')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '하나의 속성에는 두 개 이상의 값이 들어갈 수 있다.')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '인스턴스는 두 가지 이상의 속성값을 가진다.')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '하나의 엔터티는 두 개 이상의 속성을 가진다.'))));


/* =========================================================
   4번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-MODELING-01-02'
               LIMIT 1
           ),
           NULL,
           'DIAGRAM_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '아래 ERD에 포함된 각 엔터티의 성격 분류에 대한 설명 중 옳지 않은 것은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'IMAGE',
                                   'id', 1,
                                   'url', 'TODO_UPLOAD_IMAGE_URL',
                                   'description', '서비스 - 서비스이용 - 청구 - 납부로 이어지는 ERD'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', '서비스이용은 실제 서비스 이용이라는 업무 이벤트를 담으므로 개념 엔터티보다는 행위/중심 엔터티 성격에 가깝다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '서비스는 상대적으로 기준 정보인 기본 엔터티이고, 청구·납부는 업무 발생 이력인 행위 엔터티다. 서비스이용을 개념 엔터티라고 보기는 어렵다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(2)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '서비스는 기본 엔터티에 해당한다.')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '서비스이용은 개념 엔터티에 해당한다.')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '청구는 행위 엔터티에 해당한다.')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '납부는 행위 엔터티에 해당한다.'))));


/* =========================================================
   5번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-01-05'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '다음 SQL의 수행 목적을 가장 적절히 기술한 것은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'generic',
                                   'content', 'SELECT 고객ID, 주문번호, SUM(수량) AS 주문수량\nFROM 주문\nGROUP BY 고객ID, 주문번호\nHAVING SUM(수량) >= 10;'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', 'GROUP BY가 고객ID와 주문번호 기준이므로 주문 단위 집계이며, HAVING SUM(수량) >= 10은 한 주문의 총 수량이 10 이상인 경우를 찾는다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '고객 전체 누적 수량이 아니라 주문번호별 합계를 보는 쿼리다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(1)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '한 주문에 10개 이상인 고객 목록')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '누적 주문 수량이 10개 이상인 고객 목록')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '주문 횟수가 10회 이상인 고객 목록')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '10개 이상의 서로 다른 상품을 구매한 고객 목록'))));


/* =========================================================
   6번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-MODELING-01-04'
               LIMIT 1
           ),
           NULL,
           'CONCEPT_TRUE_FALSE',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '비식별자 관계에 대한 설명 중 옳지 않은 것은?',
                   'content_blocks', JSON_ARRAY()
           ),
           JSON_OBJECT(
                   'summary', '비식별자 관계에서는 부모의 키가 자식의 일반 속성으로 들어가며, 자식은 독립적인 주식별자를 가질 수 있다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '부모 키가 자식의 주식별자에 포함되는 것은 식별자 관계의 특징이므로 3번이 부적절하다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(3)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '부모의 기본키를 자식의 일반 속성으로 상속한다.')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '부모 엔터티의 생성과 자식 엔터티의 생성 시점이 분리될 수 있다.')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '주식별자와 식별자가 같다.')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '자식 엔터티에서 독립된 주식별자를 정의할 수 있다.'))));


/* =========================================================
   7번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-MODELING-01-04'
               LIMIT 1
           ),
           NULL,
           'CONCEPT_RECALL',
           'EASY',
           JSON_OBJECT(
                   'stem', '피터 첸(Peter Chen) ERD 표기법에서 관계(Relationship)를 나타내는 도형은?',
                   'content_blocks', JSON_ARRAY()
           ),
           JSON_OBJECT(
                   'summary', 'Peter Chen 표기법에서 관계는 마름모(◇)로 표시한다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '사각형은 엔터티, 타원은 속성, 마름모는 관계다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(3)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '□')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '△')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '◇')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '○'))));


/* =========================================================
   8번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-MODELING-01-05'
               LIMIT 1
           ),
           NULL,
           'CONCEPT_RECALL',
           'EASY',
           JSON_OBJECT(
                   'stem', '주식별자를 구성하는 속성 중 한 개가 삭제되었을 때 인스턴스가 구분되지 않는다. 이러한 주식별자가 충족해야 하는 특성은?',
                   'content_blocks', JSON_ARRAY()
           ),
           JSON_OBJECT(
                   'summary', '주식별자의 일부를 제거하면 식별이 안 되는 것은 최소성을 의미한다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '최소성은 식별에 꼭 필요한 최소 속성만으로 구성되어 있음을 뜻한다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(1)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '최소성')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '대표성')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '불변성')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '고립성'))));


/* =========================================================
   9번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-MODELING-02-01'
               LIMIT 1
           ),
           NULL,
           'CONCEPT_COMPARISON',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '다음 중 주식별자 선정과 가장 관련이 적은 정규화는?',
                   'content_blocks', JSON_ARRAY()
           ),
           JSON_OBJECT(
                   'summary', '1정규화는 원자값 보장과 관련이 크고, 주식별자 선정과 직접적인 연관은 상대적으로 적다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '2정규화, 3정규화, BCNF는 함수 종속성과 결정자/후보키 성격과 밀접해 식별자 선정과의 관련성이 더 크다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(1)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '1정규화')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '2정규화')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '3정규화')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'BCNF'))));


/* =========================================================
   10번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-MODELING-02-05'
               LIMIT 1
           ),
           NULL,
           'CONCEPT_TRUE_FALSE',
           'EASY',
           JSON_OBJECT(
                   'stem', '인조 식별자와 본질 식별자에 대한 설명 중 옳지 않은 것은?',
                   'content_blocks', JSON_ARRAY()
           ),
           JSON_OBJECT(
                   'summary', '업무 의미 없이 임의로 부여되는 것은 인조 식별자 쪽 설명이다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '본질 식별자는 업무적으로 의미가 있는 값이고, 인조 식별자는 시스템/관리 편의상 부여하는 값이다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(2)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '본질 식별자는 업무 수행 과정에서 쉽게 파악된다.')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '본질 식별자는 어떠한 업무 행위 없이도 부여될 수 있다.')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '인조 식별자는 본질 식별자가 존재하여도 관리 편의를 위해 별도로 부여될 수 있다.')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '본질 식별자는 업무 규칙 변화에 따라 값이 변경될 수 있다.'))));

COMMIT;

START TRANSACTION;

SET @exam_id = (
    SELECT id
    FROM exam
    WHERE code = 'SQLD'
    LIMIT 1
);

SET @exam_version_id = (
    SELECT id
    FROM exam_version
    WHERE exam_id = @exam_id
      AND version_no = 2025
    LIMIT 1
);

/* =========================================================
   11번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id,
    exam_version_id,
    exam_scope_node_id,
    ai_generated_problem_id,
    expression_type,
    difficulty,
    question_content_json,
    explanation_json,
    answer_type,
    answer_json,
    choice_type,
    source_type,
    is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-02-06'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'HARD',
           JSON_OBJECT(
                   'stem', '다음 계층형 질의에서 사용하는 가상 컬럼 및 연산자 중, 전제 조건 없이 단독으로 동작하지 않거나 특정 옵션이 선행되어야 유효한 값을 반환하는 것은 무엇인가?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '* 가. LEVEL\n* 나. ISLEAF\n* 다. ISCYCLE\n* 라. CONNECT_BY_ROOT'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', '계층형 질의에서 CONNECT_BY_ISCYCLE은 NOCYCLE 옵션이 있어야 의미 있게 사용되며, CONNECT_BY_ROOT도 계층 구조를 전제로 동작한다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', 'LEVEL은 계층형 질의에서 기본적으로 계층 레벨을 반환한다. ISLEAF, ISCYCLE, CONNECT_BY_ROOT는 계층 구조와 특정 조건을 전제로 해석해야 한다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(3)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '가')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '나, 다')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '나, 다, 라')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '가, 나, 다, 라'))));


/* =========================================================
   12번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-02-01'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '아래 SQL의 결과로 옳은 것은? 단, MGR 컬럼에는 NULL 값이 존재한다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'TABLE',
                                   'caption', 'EMP 테이블',
                                   'content', '| EMPNO | ENAME | MGR |\n|---|---|---|\n| 7369 | SMITH | 7902 |\n| 7499 | ALLEN | 7698 |\n| 7839 | KING | NULL |\n| 7902 | FORD | 7566 |\n| 7566 | JONES | NULL |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 2,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'oracle',
                                   'content', 'SELECT *\nFROM EMP\nWHERE EMPNO NOT IN (SELECT MGR FROM EMP);'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', 'NOT IN의 서브쿼리 결과에 NULL이 포함되면 비교 결과가 UNKNOWN이 되어 조건을 만족하는 행이 나오지 않는다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '서브쿼리의 MGR 컬럼에 NULL이 있으므로 EMPNO NOT IN (...) 조건은 모든 행에 대해 참이 되지 않는다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(3)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'EMP 테이블의 모든 행')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '매니저로 지정되지 않은 직원의 행')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '공집합')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '오류 발생'))));


/* =========================================================
   13번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-02-07'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '아래 결과를 얻기 위해 빈칸에 들어갈 SQL로 가장 적절한 것은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'TABLE',
                                   'caption', '급여 테이블',
                                   'content', '| 부서 | 사원 | 대리 | 팀장 | 부장 |\n|---|---:|---:|---:|---:|\n| 인사팀 | 3500 | 4500 | 6000 | 8000 |\n| IT팀 | 4000 | 5000 | 7000 | 9000 |\n| 행정팀 | 3200 | 4200 | 5800 | 7500 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 2,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'oracle',
                                   'content', 'SELECT *\nFROM 급여\n( ? );'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', '사원, 대리, 팀장, 부장 컬럼을 직급 행으로 변환하려면 UNPIVOT을 사용한다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', 'PIVOT은 행을 컬럼으로 바꾸고, UNPIVOT은 컬럼을 행으로 바꾼다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(4)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'GROUP BY 부서 HAVING 직급 IN (''사원'',''대리'',''팀장'',''부장'')')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'PIVOT ( SUM(연봉) FOR 직급 IN (''사원'',''대리'',''팀장'',''부장'') )')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'WHERE 직급 LIKE ''%사원%'' OR 직급 LIKE ''%부장%''')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'UNPIVOT (연봉 FOR 직급 IN (사원, 대리, 팀장, 부장))'))));


/* =========================================================
   14번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-02-04'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '아래 데이터와 SQL의 결과로 올바른 것은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'TABLE',
                                   'caption', 'T 테이블',
                                   'content', '| SAL |\n|---:|\n| 1000 |\n| 2000 |\n| 3000 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 2,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'oracle',
                                   'content', 'SELECT SAL,\n       FIRST_VALUE(SAL) OVER (ORDER BY SAL\n                              ROWS BETWEEN UNBOUNDED PRECEDING\n                              AND CURRENT ROW) AS MIN_SAL\nFROM T;'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', 'FIRST_VALUE는 현재 윈도우 프레임에서 첫 번째 값을 반환한다. 정렬 기준이 SAL 오름차순이고 프레임 시작이 UNBOUNDED PRECEDING이므로 모든 행의 MIN_SAL은 1000이다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '각 행의 프레임은 첫 행부터 현재 행까지이므로 첫 번째 값은 항상 1000이다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(1)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '1000 1000 1000')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '1000 2000 3000')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '3000 2000 1000')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '3000 3000 3000'))));


/* =========================================================
   15번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-01-04'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '아래 SQL과 동일한 의미를 가지는 조건식은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'TABLE',
                                   'caption', 'T 테이블',
                                   'content', '| 사번 | 회원번호 |\n|---:|---:|\n| 10005 | 2003 |\n| 10005 | 1500 |\n| 20007 | 2003 |\n| 30001 | 2003 |\n| 10005 | 2003 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 2,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'oracle',
                                   'content', 'SELECT *\nFROM T\nWHERE (사번, 회원번호) = ((10005, 2003));'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', '다중 컬럼 비교에서 (사번, 회원번호) = (10005, 2003)은 두 컬럼이 각각 같은 값을 가져야 한다는 의미다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '따라서 사번 = 10005 이면서 회원번호 = 2003인 행만 조회된다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(2)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '(사번 = 10005) OR (회원번호 = 2003)')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '(사번 = 10005) AND (회원번호 = 2003)')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '(사번 = 10005) OR (회원번호 <> 2003)')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'NOT ((사번 = 10005) AND (회원번호 = 2003))'))));


/* =========================================================
   16번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-02-04'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '아래 8건의 데이터에 대해 NTILE 함수가 반환하는 값으로 가장 옳은 것은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'TABLE',
                                   'caption', 'EMP 테이블',
                                   'content', '| EMPNO |\n|---:|\n| 1 |\n| 2 |\n| 3 |\n| 4 |\n| 5 |\n| 6 |\n| 7 |\n| 8 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 2,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'oracle',
                                   'content', 'SELECT EMPNO,\n       NTILE(3) OVER (ORDER BY EMPNO) AS GRP\nFROM EMP;'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', 'NTILE(3)은 정렬된 8개 행을 3개 그룹으로 최대한 균등하게 나누며, 앞쪽 그룹부터 하나씩 더 많은 행을 배정한다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '8개를 3그룹으로 나누면 3, 3, 2건이 되어 결과는 1 1 1 2 2 2 3 3 이다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(1)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '1 1 1 2 2 2 3 3')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '1 1 2 2 3 3 3 3')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '1 2 3 1 2 3 1 2')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '3 3 3 2 2 2 1 1'))));


/* =========================================================
   17번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-01-08'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '아래 두 테이블을 조인하여 양쪽의 모든 행을 포함하되 NULL 값은 0으로 치환한 결과를 얻으려 한다. 가장 적절한 SQL은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'TABLE',
                                   'caption', 'T1',
                                   'content', '| ID | VAL |\n|---:|---:|\n| 1 | 100 |\n| 2 | 150 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 2,
                                   'type', 'TABLE',
                                   'caption', 'T2',
                                   'content', '| ID | VAL |\n|---:|---:|\n| 2 | 200 |\n| 3 | 300 |'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', '양쪽 테이블의 모든 행을 포함하려면 FULL OUTER JOIN을 사용하고, NULL 치환은 NVL을 사용한다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', 'CROSS JOIN은 모든 조합을 만들고, UNION은 조인 조건을 처리하지 않는다. FULL OUTER JOIN이 양쪽 미매칭 행을 모두 포함한다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(2)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT NVL(T1.VAL, 0), NVL(T2.VAL, 0) FROM T1 CROSS JOIN T2;')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT NVL(T1.VAL, 0), NVL(T2.VAL, 0) FROM T1 FULL OUTER JOIN T2 ON T1.ID = T2.ID;')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT T1.VAL, T2.VAL FROM T1 FULL OUTER JOIN T2 ON T1.ID = T2.ID;')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT T1.VAL, T2.VAL FROM T1 UNION T2;'))));


/* =========================================================
   18번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-03-04'
               LIMIT 1
           ),
           NULL,
           'CONCEPT_RECALL',
           'EASY',
           JSON_OBJECT(
                   'stem', '사용자 R1이 테이블을 생성하고 접속을 종료한 뒤, 다른 사용자 R2가 접속하여 R1의 테이블을 조회하려 하자 오류가 발생하였다. 해결 방안으로 가장 적절한 것은?',
                   'content_blocks', JSON_ARRAY()
           ),
           JSON_OBJECT(
                   'summary', '다른 사용자의 테이블을 조회하려면 해당 테이블에 대한 SELECT 권한이 필요하다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', 'R1이 R2에게 GRANT SELECT 권한을 부여해야 R2가 R1의 테이블을 조회할 수 있다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(2)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'R2가 R1의 계정으로 새로고침하여 조회한다.')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'GRANT SELECT 권한을 부여한다.')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'R2 계정에서 SYNONYM만 생성하면 자동으로 접근이 허용된다.')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'R2가 관리자 권한으로 테이블을 강제 복제한다.'))));


/* =========================================================
   19번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-02-08'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '다음 SQL의 실행 결과로 옳은 것은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'oracle',
                                   'content', 'SELECT REGEXP_REPLACE(\n           ''2026/02/25'',\n           ''([0-9]{4})/([0-9]{2})/([0-9]{2})'',\n           ''\\3/\\2/\\1''\n       )\nFROM DUAL;'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', '정규식 그룹 1은 연도, 그룹 2는 월, 그룹 3은 일이다. 치환식 \\3/\\2/\\1에 따라 일/월/연도 형식으로 변환된다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '2026/02/25는 25/02/2026으로 변환된다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(2)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '2026/02/25')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '25/02/2026')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '02/25/2026')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '2026-02-25'))));


/* =========================================================
   20번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-01-03'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '아래 데이터에 대한 SQL의 결과로 옳은 것은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'TABLE',
                                   'caption', 'T 테이블',
                                   'content', '| SAL |\n|---:|\n| 4 |\n| NULL |\n| NULL |\n| 9 |\n| 10 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 2,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'oracle',
                                   'content', 'SELECT SUM(CASE WHEN SAL = 4 THEN SAL END),\n       SUM(SAL)\nFROM T;'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', 'CASE 조건에 맞는 SAL = 4만 첫 번째 SUM에 포함되고, SUM(SAL)은 NULL을 제외한 4, 9, 10을 합산한다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '첫 번째 결과는 4이고, 두 번째 결과는 4 + 9 + 10 = 23이다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(1)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '4, 23')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'null, 23')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '4, null')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '0, 0'))));

COMMIT;


START TRANSACTION;

SET @exam_id = (
    SELECT id
    FROM exam
    WHERE code = 'SQLD'
    LIMIT 1
);

SET @exam_version_id = (
    SELECT id
    FROM exam_version
    WHERE exam_id = @exam_id
      AND version_no = 2025
    LIMIT 1
);

/* =========================================================
   21번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-02-05'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '아래 T 테이블에 대해 SQL 들 중 결과 행 수가 나머지와 다른 하나는?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'TABLE',
                                   'caption', 'T 테이블',
                                   'content', '| 행 순서 | COL |\n|---:|---|\n| 1 | A |\n| 2 | B |\n| 3 | C |'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', 'ROWNUM IN (1, 2)는 첫 번째와 두 번째 행을 반환할 수 있지만, ROWNUM < 1, ROWNUM > 1, ROWNUM = 2는 행을 반환하지 않는다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', 'Oracle에서 ROWNUM 조건은 행이 반환되는 과정에서 부여된다. ROWNUM > 1 또는 ROWNUM = 2는 첫 행부터 조건을 만족하지 못해 결과가 없다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(1)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT * FROM T WHERE ROWNUM IN (1, 2);')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT * FROM T WHERE ROWNUM < 1;')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT * FROM T WHERE ROWNUM > 1;')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT * FROM T WHERE ROWNUM = 2;'))));


/* =========================================================
   22번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-01-05'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '아래 SQL에서 WHERE 1 = 2 조건에 의해 어떤 데이터도 선택되지 않았을 때, 반환 값이 NULL이 아닌 집계 함수는?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'oracle',
                                   'content', 'SELECT SUM(SAL),\n       AVG(SAL),\n       MIN(SAL),\n       COUNT(*)\nFROM EMP\nWHERE 1 = 2;'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', '조건을 만족하는 행이 없을 때 SUM, AVG, MIN은 NULL을 반환하지만 COUNT(*)는 0을 반환한다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', 'COUNT(*)는 행 개수를 세는 함수이므로 대상 행이 없으면 NULL이 아니라 0을 반환한다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(4)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'SUM(SAL)')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'AVG(SAL)')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'MIN(SAL)')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'COUNT(*)'))));


/* =========================================================
   23번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-01-08'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'HARD',
           JSON_OBJECT(
                   'stem', '아래 네 개의 SQL 중 결과가 나머지와 다른 하나는? 단, A, B 테이블은 동일하며 B.FLG 값은 일부 행에만 ''Y''이다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'TABLE',
                                   'caption', 'A 테이블',
                                   'content', '| ID | NAME |\n|---:|---|\n| 1 | 가 |\n| 2 | 나 |\n| 3 | 다 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 2,
                                   'type', 'TABLE',
                                   'caption', 'B 테이블',
                                   'content', '| ID | VAL | FLG |\n|---:|---|---|\n| 2 | x | Y |\n| 3 | y | N |'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', 'LEFT OUTER JOIN 이후 WHERE B.FLG = ''Y''를 적용하면 NULL 확장 행이 제거되어 INNER JOIN처럼 동작한다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '2번은 조인 후 WHERE 절에서 B.FLG를 필터링하므로 A의 미매칭 행이 제거된다. 나머지는 조건을 조인 조건 안에 두거나 사전 필터링하여 A의 모든 행을 유지한다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(2)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT * FROM A, B WHERE A.ID = B.ID(+) AND B.FLG(+) = ''Y'';')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT * FROM A LEFT OUTER JOIN B ON (A.ID = B.ID) WHERE B.FLG = ''Y'';')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT * FROM A LEFT OUTER JOIN B ON (A.ID = B.ID AND B.FLG = ''Y'');')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT * FROM A LEFT OUTER JOIN (SELECT * FROM B WHERE FLG = ''Y'') B ON A.ID = B.ID;'))));


/* =========================================================
   24번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-02-07'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '아래 결과를 출력하기 위해 가장 적절한 SQL은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'TABLE',
                                   'caption', '원본 매출 테이블',
                                   'content', '| 지역 | 월 | 매출 |\n|---|---|---:|\n| 서울 | 1월 | 12000 |\n| 서울 | 2월 | 13500 |\n| 서울 | 3월 | 14200 |\n| 서울 | 4월 | 15000 |\n| 부산 | 1월 | 8500 |\n| 부산 | 2월 | 9200 |\n| 부산 | 3월 | 10100 |\n| 부산 | 4월 | 11000 |\n| 대구 | 1월 | 6800 |\n| 대구 | 2월 | 7400 |\n| 대구 | 3월 | 8000 |\n| 대구 | 4월 | 8600 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 2,
                                   'type', 'TABLE',
                                   'caption', '기대 결과',
                                   'content', '| 지역 | 1월 | 2월 | 3월 | 4월 |\n|---|---:|---:|---:|---:|\n| 서울 | 12000 | 13500 | 14200 | 15000 |\n| 부산 | 8500 | 9200 | 10100 | 11000 |\n| 대구 | 6800 | 7400 | 8000 | 8600 |'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', '행으로 존재하는 월 값을 컬럼으로 전환해야 하므로 PIVOT을 사용한다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '월별 행 데이터를 1월, 2월, 3월, 4월 컬럼으로 바꾸는 문제이므로 PIVOT이 적절하다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(3)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT 지역, SUM(CASE WHEN 월 = ''1월'' THEN 매출 END) FROM 매출 GROUP BY 지역;')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT * FROM 매출 UNPIVOT (매출 FOR 월 IN (''1월'', ''2월'', ''3월'', ''4월''));')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT * FROM 매출 PIVOT (SUM(매출) FOR 월 IN (''1월'' AS ''1월'', ''2월'' AS ''2월'', ''3월'' AS ''3월'', ''4월'' AS ''4월''));')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT 지역, AVG(매출) FROM 매출 GROUP BY CUBE(지역, 월);'))));


/* =========================================================
   25번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-02-01'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '아래 SQL의 서브쿼리가 항상 1건 이하만 반환하도록 보장하기 위해 T2.A 컬럼이 가져야 할 제약 조건은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'TABLE',
                                   'caption', 'T1 테이블',
                                   'content', '| COL2 |\n|---:|\n| 10 |\n| 20 |\n| 30 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 2,
                                   'type', 'TABLE',
                                   'caption', 'T2 테이블',
                                   'content', '| A | COL2 |\n|---|---:|\n| A | 10 |\n| A | 20 |\n| B | 30 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 3,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'oracle',
                                   'content', 'SELECT COL2\nFROM T1\nWHERE COL2 = (SELECT COL2\n              FROM T2\n              WHERE A = ''A'');'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', '단일 행 비교 연산자 = 에 사용되는 서브쿼리는 한 행만 반환해야 한다. A 컬럼에 UNIQUE 제약이 있으면 A = ''A'' 조건 결과가 1건 이하로 보장된다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', 'NOT NULL은 중복을 막지 못하고, FOREIGN KEY나 CHECK도 A 값의 유일성을 직접 보장하지 않는다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(1)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'UNIQUE')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'NOT NULL')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'FOREIGN KEY')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'CHECK'))));


/* =========================================================
   26번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-02-04'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '아래 쿼리의 ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW 결과와 동일한 의미를 가진 절은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'oracle',
                                   'content', 'SELECT SAL,\n       SUM(SAL) OVER (ORDER BY SAL ROWS UNBOUNDED PRECEDING) AS 누적합계\nFROM EMP;'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', 'ROWS UNBOUNDED PRECEDING은 ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW의 축약 표현이다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '현재 행까지의 누적 집계를 의미한다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(1)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'ROWS UNBOUNDED PRECEDING')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'ROWS BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'RANGE UNBOUNDED PRECEDING')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'RANGE BETWEEN CURRENT ROW AND UNBOUNDED FOLLOWING'))));


/* =========================================================
   27번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-03-02'
               LIMIT 1
           ),
           NULL,
           'CONCEPT_RECALL',
           'EASY',
           JSON_OBJECT(
                   'stem', '트랜잭션의 고립성(Isolation)에 대한 설명으로 가장 옳은 것은?',
                   'content_blocks', JSON_ARRAY()
           ),
           JSON_OBJECT(
                   'summary', '고립성은 동시에 실행되는 트랜잭션들이 서로 부정확한 영향을 주지 않도록 격리되어야 한다는 성질이다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '트랜잭션의 ACID 중 Isolation은 다른 트랜잭션으로부터 독립적으로 수행되는 성질을 의미한다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(1)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '다른 트랜잭션으로부터 영향을 받지 않는다.')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '트랜잭션 전후 데이터의 정합성이 유지된다.')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '전체가 반영되거나 전혀 반영되지 않는다.')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '커밋된 결과는 시스템 장애에도 보존된다.'))));


/* =========================================================
   28번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-01-01'
               LIMIT 1
           ),
           NULL,
           'CONCEPT_RECALL',
           'EASY',
           JSON_OBJECT(
                   'stem', '기본키(Primary Key) 컬럼이 반드시 만족해야 하는 제약 조건의 조합은?',
                   'content_blocks', JSON_ARRAY()
           ),
           JSON_OBJECT(
                   'summary', '기본키는 각 행을 유일하게 식별해야 하므로 UNIQUE와 NOT NULL 조건을 모두 만족해야 한다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '기본키는 중복될 수 없고 NULL도 허용되지 않는다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(3)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'NULL 허용 + UNIQUE')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'NULL 허용 + NOT NULL')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'NOT NULL + UNIQUE')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'NOT NULL 단독'))));


/* =========================================================
   29번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-02-06'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '아래 EMP 테이블에서 사원과 매니저 관계의 계층 트리를 조회하기 위한 CONNECT BY 절로 옳은 것은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'TABLE',
                                   'caption', 'EMP 테이블',
                                   'content', '| 사원 | 매니저 |\n|---:|---:|\n| 1001 | NULL |\n| 1002 | 1001 |\n| 1003 | 1001 |\n| 1004 | 1002 |\n| 1005 | 1003 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 2,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'oracle',
                                   'content', 'SELECT 사원, 매니저, LEVEL\nFROM EMP\nSTART WITH 매니저 IS NULL\nCONNECT BY ( ? );'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', '상위 사원의 사원번호가 하위 사원의 매니저 번호와 같아야 하므로 PRIOR 사원 = 매니저 조건을 사용한다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', 'PRIOR가 붙은 컬럼은 부모 행의 값을 의미한다. 부모의 사원 번호가 자식의 매니저 번호와 연결된다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(2)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'PRIOR 매니저 = 사원')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'PRIOR 사원 = 매니저')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '사원 = 매니저')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'PRIOR 사원 = PRIOR 매니저'))));


/* =========================================================
   30번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-02-06'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'HARD',
           JSON_OBJECT(
                   'stem', '아래 부서 테이블에 대한 계층형 질의의 결과에 대한 설명으로 가장 옳은 것은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'TABLE',
                                   'caption', '부서 테이블',
                                   'content', '| 부서ID | 상위부서ID | COL3 |\n|---|---|---:|\n| D | NULL | 1 |\n| D1 | D | 1 |\n| D2 | D | 2 |\n| D3 | D | 1 |\n| D2-1 | D2 | 1 |\n| D2-2 | D2 | 1 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 2,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'oracle',
                                   'content', 'SELECT *\nFROM 부서\nSTART WITH 부서ID = ''D''\nCONNECT BY PRIOR 부서ID = 상위부서ID\n       AND COL3 <> 2;'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', 'CONNECT BY 절의 AND COL3 <> 2 조건은 자식 후보 행에 적용된다. 중간 노드 D2의 COL3 값이 2이므로 D2가 제외되고 그 하위 노드도 탐색되지 않는다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', 'D2가 계층 결과에서 제외되면 D2-1, D2-2로 내려가는 경로도 끊긴다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(2)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '루트 노드가 여러 건이어서 오류가 발생한다.')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'COL3 값이 2인 중간 노드가 제외되면서 그 아래 가지가 트리에서 단절된다.')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'PRIOR 연산자는 CONNECT BY와 함께 사용할 수 없다.')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'AND 조건은 계층형 질의에서 지원되지 않는다.'))));

COMMIT;


START TRANSACTION;

SET @exam_id = (
    SELECT id
    FROM exam
    WHERE code = 'SQLD'
    LIMIT 1
);

SET @exam_version_id = (
    SELECT id
    FROM exam_version
    WHERE exam_id = @exam_id
      AND version_no = 2025
    LIMIT 1
);

/* =========================================================
   31번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-02-05'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'EASY',
           JSON_OBJECT(
                   'stem', '아래 테이블에서 FETCH 절로 상위 2건을 반환한 결과로 옳은 것은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'TABLE',
                                   'caption', '매달 테이블',
                                   'content', '| 매달 | 개수 |\n|---|---:|\n| 골드 | 1 |\n| 실버 | 2 |\n| 브론즈 | 1 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 2,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'oracle',
                                   'content', 'SELECT 매달, 개수\nFROM 매달\nFETCH FIRST 2 ROWS ONLY;'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', 'FETCH FIRST 2 ROWS ONLY는 조회 결과 중 앞의 2건만 반환한다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '주어진 표시 순서를 기준으로 앞의 두 행은 골드 1, 실버 2이다. 단, 실제 SQL에서는 ORDER BY가 없으면 반환 순서가 보장되지 않는다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(2)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '골드 1, 실버 2, 브론즈 1')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '골드 1, 실버 2')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '실버 2, 골드 1')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '실버 2, 브론즈 1'))));


/* =========================================================
   32번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-03-03'
               LIMIT 1
           ),
           NULL,
           'CONCEPT_TRUE_FALSE',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '다음 중 ALTER TABLE 수행 시 오류가 발생하지 않는 것은?',
                   'content_blocks', JSON_ARRAY()
           ),
           JSON_OBJECT(
                   'summary', '일반적으로 기존 테이블에 NULL 허용 TIMESTAMP 컬럼을 추가하는 것은 오류 없이 가능하다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '중복값이 있는 컬럼에 PRIMARY KEY를 추가하면 오류가 발생한다. 기존 데이터보다 작은 크기로 컬럼을 줄이는 것도 데이터 손실 가능성 때문에 오류가 발생할 수 있다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(4)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '이미 값이 들어 있는 NOT NULL 제약을 추가하는 경우')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '기존 자리수보다 짧게 NUMBER 크기를 변경하는 경우')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '중복 값이 있는 컬럼에 PRIMARY KEY를 추가하는 경우')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'TIMESTAMP 컬럼을 추가하는 경우'))));


/* =========================================================
   33번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-02-01'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '아래 SQL의 결과로 옳은 것은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'TABLE',
                                   'caption', '주문',
                                   'content', '| ITEM |\n|---:|\n| 1 |\n| 2 |\n| 3 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 2,
                                   'type', 'TABLE',
                                   'caption', '판매',
                                   'content', '| ITEM |\n|---:|\n| 3 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 3,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'oracle',
                                   'content', 'SELECT ITEM\nFROM 주문 O\nWHERE NOT EXISTS (\n    SELECT 1\n    FROM 판매 P\n    WHERE P.ITEM = O.ITEM\n);'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', 'NOT EXISTS는 서브쿼리 결과가 존재하지 않는 행을 반환한다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '판매 테이블에는 ITEM 3만 있으므로 주문 ITEM 1, 2가 결과로 반환된다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(2)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '아이템 1')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '아이템 1, 2')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '공집합')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '오류'))));


/* =========================================================
   34번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-02-02'
               LIMIT 1
           ),
           NULL,
           'CONCEPT_RECALL',
           'EASY',
           JSON_OBJECT(
                   'stem', '다음 집합 연산자 중 중복을 허용하는 것은?',
                   'content_blocks', JSON_ARRAY()
           ),
           JSON_OBJECT(
                   'summary', 'UNION ALL은 중복 행을 제거하지 않고 그대로 반환한다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', 'UNION, INTERSECT, EXCEPT는 중복 제거 성격을 가지지만 UNION ALL은 중복을 허용한다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(2)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'UNION')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'UNION ALL')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'INTERSECT')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'EXCEPT'))));


/* =========================================================
   35번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-02-02'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'EASY',
           JSON_OBJECT(
                   'stem', '아래 SQL 결과의 SELECT 컬럼 개수로 옳은 것은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'TABLE',
                                   'caption', 'EMP 테이블',
                                   'content', '| EMPNO | ENAME | HIREDATE | SAL |\n|---:|---|---|---:|\n| 7839 | KING | 1981-11-17 | 5000 |\n| 7566 | JONES | 1981-04-02 | 2975 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 2,
                                   'type', 'TABLE',
                                   'caption', 'EMP_HIST 테이블',
                                   'content', '| EMPNO | ENAME | HIREDATE | SAL |\n|---:|---|---|---:|\n| 7369 | SMITH | 1980-12-17 | 800 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 3,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'oracle',
                                   'content', 'SELECT EMPNO,\n       ENAME,\n       EXTRACT(YEAR FROM HIREDATE) AS YR,\n       SAL\nFROM EMP\nUNION\nSELECT EMPNO,\n       ENAME,\n       EXTRACT(YEAR FROM HIREDATE),\n       SAL\nFROM EMP_HIST;'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', 'UNION 결과의 컬럼 개수는 SELECT 절의 컬럼 개수와 동일하며, 첫 번째 SELECT 기준으로 4개다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', 'EMPNO, ENAME, EXTRACT(YEAR FROM HIREDATE), SAL 총 4개 컬럼이 반환된다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(4)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '1개')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '2개')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '3개')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '4개'))));


/* =========================================================
   36번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-01-01'
               LIMIT 1
           ),
           NULL,
           'CONCEPT_TRUE_FALSE',
           'EASY',
           JSON_OBJECT(
                   'stem', 'DML/DDL/DCL 구분에 대한 설명 중 잘못된 것은?',
                   'content_blocks', JSON_ARRAY()
           ),
           JSON_OBJECT(
                   'summary', 'TRUNCATE는 테이블 데이터를 빠르게 제거하는 명령이지만 DDL로 분류된다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', 'INSERT, UPDATE, DELETE, MERGE는 DML이고, CREATE, ALTER, DROP, TRUNCATE는 DDL이며, GRANT와 REVOKE는 DCL이다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(1)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'TRUNCATE는 DML이다.')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'INSERT, UPDATE, DELETE, MERGE는 DML이다.')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'CREATE, ALTER, DROP은 DDL이다.')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'GRANT, REVOKE는 DCL이다.'))));


/* =========================================================
   37번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-01-07'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '아래 두 테이블을 조인한 결과의 SUM 값으로 옳은 것은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'TABLE',
                                   'caption', '테이블1',
                                   'content', '| 컬럼1 | 컬럼2 |\n|---:|---:|\n| 1 | 10 |\n| 2 | 20 |\n| 3 | 30 |\n| 4 | 40 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 2,
                                   'type', 'TABLE',
                                   'caption', '테이블2',
                                   'content', '| 컬럼1 |\n|---:|\n| 1 |\n| 2 |\n| 3 |\n| 4 |\n| 5 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 3,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'oracle',
                                   'content', 'SELECT SUM(A.컬럼2)\nFROM 테이블1 A, 테이블2 B\nWHERE A.컬럼1 = B.컬럼1;'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', '조인 조건에 의해 컬럼1 값 1, 2, 3, 4만 매칭되며 A.컬럼2 합계는 10 + 20 + 30 + 40 = 100이다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '테이블2의 컬럼1 = 5는 테이블1에 매칭 행이 없어 결과에 포함되지 않는다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(1)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '100')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '120')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '140')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'NULL'))));


/* =========================================================
   38번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-01-07'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'EASY',
           JSON_OBJECT(
                   'stem', '아래 두 테이블 A, B에 대한 SQL의 결과로 옳은 것은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'TABLE',
                                   'caption', 'A 테이블',
                                   'content', '| COL |\n|---|\n| a1 |\n| a2 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 2,
                                   'type', 'TABLE',
                                   'caption', 'B 테이블',
                                   'content', '| COL |\n|---|\n| b1 |\n| b2 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 3,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'oracle',
                                   'content', 'SELECT COUNT(*)\nFROM A, B;'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', 'FROM A, B처럼 조인 조건이 없으면 카테시안 곱이 발생한다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', 'A가 2행, B가 2행이므로 결과 행 수는 2 × 2 = 4이다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(1)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '4')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '2')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '0')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '오류'))));


/* =========================================================
   39번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-03-01'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '아래 TGT, SRC 테이블에 대해 MERGE 문 수행 후 TGT의 ID=2 행의 V1, V2, V3 값으로 옳은 것은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'TABLE',
                                   'caption', 'TGT 테이블 - MERGE 전',
                                   'content', '| ID | V1 | V2 | V3 |\n|---:|---:|---:|---|\n| 1 | 10 | 20 | NULL |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 2,
                                   'type', 'TABLE',
                                   'caption', 'SRC 테이블',
                                   'content', '| ID |\n|---:|\n| 2 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 3,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'oracle',
                                   'content', 'MERGE INTO TGT T\nUSING SRC S\nON (T.ID = S.ID)\nWHEN MATCHED THEN\n    UPDATE SET T.V1 = 100, T.V2 = 100\nWHEN NOT MATCHED THEN\n    INSERT (ID, V1, V2, V3) VALUES (S.ID, 100, 100, 100);'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', 'SRC의 ID=2는 TGT에 존재하지 않으므로 NOT MATCHED 절이 수행되어 새 행이 삽입된다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '따라서 ID=2 행의 V1, V2, V3 값은 각각 100, 100, 100이다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(1)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '100 100 100')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '100 100 NULL')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'NULL 100 100')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '100 NULL 100'))));


/* =========================================================
   40번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-01-02'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '아래 SQL에서 오류가 발생하는 절은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'oracle',
                                   'content', 'SELECT PRODUCT_CD AS 제품코드\nFROM PRODUCT\nWHERE 제품코드 = ''A01''\n  AND 제품코드 LIKE ''A%''\nORDER BY 제품코드;'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', 'SELECT 절에서 지정한 컬럼 별칭은 WHERE 절에서 사용할 수 없다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', 'ORDER BY 절에서는 SELECT 별칭을 사용할 수 있지만, WHERE 절은 SELECT 절보다 논리적으로 먼저 처리되므로 별칭 제품코드를 인식하지 못한다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(3)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'SELECT 절')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'FROM 절')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'WHERE 절의 제품코드 별칭 사용')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'ORDER BY 절의 제품코드 별칭 사용'))));

COMMIT;

START TRANSACTION;

SET @exam_id = (
    SELECT id
    FROM exam
    WHERE code = 'SQLD'
    LIMIT 1
);

SET @exam_version_id = (
    SELECT id
    FROM exam_version
    WHERE exam_id = @exam_id
      AND version_no = 2025
    LIMIT 1
);

/* =========================================================
   41번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-01-03'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'EASY',
           JSON_OBJECT(
                   'stem', '아래 SQL 들 중 결과가 나머지와 다른 하나는?',
                   'content_blocks', JSON_ARRAY()
           ),
           JSON_OBJECT(
                   'summary', 'NULL과의 비교 결과는 UNKNOWN이므로 WHERE 1 > NULL 조건은 행을 반환하지 않는다. 나머지는 한 행을 반환하되 결과값이 NULL이다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', 'NULL 산술 연산 결과는 NULL이지만, WHERE 조건에서 UNKNOWN은 필터링되어 결과 행이 없다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(1)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT * FROM DUAL WHERE 1 > NULL;')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT NULL + 1 FROM DUAL;')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT NULL * 1 FROM DUAL;')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT NULL * NULL FROM DUAL;'))));


/* =========================================================
   42번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-01-03'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'EASY',
           JSON_OBJECT(
                   'stem', '다음 SELECT 문 중 반환 값이 나머지와 다른 것은?',
                   'content_blocks', JSON_ARRAY()
           ),
           JSON_OBJECT(
                   'summary', 'NULL과의 산술 연산은 NULL을 반환하지만, 문자열 상수 ''X''는 NULL이 아닌 값이다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '1 + NULL, 1 * NULL, NULL 표현식은 NULL을 반환한다. SELECT ''X'' FROM DUAL은 문자 X를 반환한다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(2)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT 1 + NULL FROM DUAL;')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT ''X'' FROM DUAL;')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT 1 * NULL FROM DUAL;')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT NULL, NULL FROM DUAL;'))));


/* =========================================================
   43번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-03-03'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '아래 CREATE TABLE과 INSERT 수행 후 최종 COUNT 값으로 옳은 것은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'oracle',
                                   'content', 'CREATE TABLE T (\n    ID NUMBER GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1),\n    VAL NUMBER CHECK (VAL > 0)\n);\n\nINSERT INTO T(VAL) VALUES (-1);\nINSERT INTO T(VAL) VALUES (0);\nINSERT INTO T(VAL) VALUES (1);\nCOMMIT;\n\nSELECT COUNT(*) FROM T;'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', 'CHECK (VAL > 0) 제약 조건 때문에 -1과 0은 삽입되지 않고, 1만 삽입된다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', 'VAL > 0 조건을 만족하는 INSERT는 VAL = 1 한 건뿐이므로 COUNT(*)는 1이다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(2)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '0')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '1')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '2')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '3'))));


/* =========================================================
   44번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-03-03'
               LIMIT 1
           ),
           NULL,
           'CONCEPT_RECALL',
           'EASY',
           JSON_OBJECT(
                   'stem', 'DROP TABLE ... RESTRICT 옵션 사용 시 해당 테이블을 참조하는 뷰가 존재하는 경우의 결과로 옳은 것은?',
                   'content_blocks', JSON_ARRAY()
           ),
           JSON_OBJECT(
                   'summary', 'RESTRICT 옵션은 해당 객체를 참조하는 객체가 있으면 삭제를 제한한다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', '테이블을 참조하는 뷰가 있으면 DROP이 수행되지 않으므로 테이블과 뷰 모두 삭제되지 않는다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(1)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '어떤 객체도 삭제되지 않는다.')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '테이블만 삭제되고 뷰는 남는다.')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '테이블과 뷰가 함께 삭제된다.')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '뷰가 먼저 삭제된 뒤 테이블이 삭제된다.'))));


/* =========================================================
   45번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-01-03'
               LIMIT 1
           ),
           NULL,
           'CONCEPT_TRUE_FALSE',
           'EASY',
           JSON_OBJECT(
                   'stem', 'NVL(COMM, SAL)에 대한 설명 중 옳지 않은 것은?',
                   'content_blocks', JSON_ARRAY()
           ),
           JSON_OBJECT(
                   'summary', 'NVL(COMM, SAL)은 COMM이 NULL이면 SAL을 반환하고, NULL이 아니면 COMM을 반환한다. 덧셈 연산이 아니다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', 'NVL의 두 인자는 데이터 타입이 호환되어야 한다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(1)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'COMM과 SAL의 덧셈 연산을 수행한다.')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'COMM 값이 NULL일 때 SAL을 반환한다.')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'COMM과 SAL의 데이터 타입이 호환되어야 한다.')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'COMM 값이 NULL이 아니면 COMM을 그대로 반환한다.'))));


/* =========================================================
   46번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-02-04'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '아래 EMP 테이블에서 부서별 급여 상위 3명을 조회하려 한다. 빈칸에 들어갈 분석 함수로 가장 적절한 것은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'TABLE',
                                   'caption', 'EMP 테이블',
                                   'content', '| 부서 | 사원 | 급여 |\n|---|---|---:|\n| A | 김철수 | 5000 |\n| A | 이영희 | 4000 |\n| A | 박민수 | 4000 |\n| A | 최지훈 | 3000 |\n| B | 정수민 | 4500 |\n| B | 강호동 | 3500 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 2,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'oracle',
                                   'content', 'SELECT 부서, 사원, 급여\nFROM (\n    SELECT 부서, 사원, 급여,\n           ( ? ) OVER (PARTITION BY 부서 ORDER BY 급여 DESC) AS RNK\n    FROM EMP\n)\nWHERE RNK <= 3;'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', '부서별 급여 순위를 계산하려면 순위 분석 함수가 필요하며, 제시된 보기 중에는 DENSE_RANK가 가장 적절하다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', 'DENSE_RANK는 같은 급여에 같은 순위를 부여하고 다음 순위를 건너뛰지 않는다. 단, 정확히 3명만 자르려면 ROW_NUMBER 계열이 필요하지만 보기에는 없다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(1)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'DENSE_RANK')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'ROWNUM')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'NTILE')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'CUME_DIST'))));


/* =========================================================
   47번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-02-01'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '어느 부서에도 속하지 않는 직원을 조회하는 SQL로 가장 적절한 것은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'TABLE',
                                   'caption', 'EMP',
                                   'content', '| EMPNO | ENAME | DEPTNO |\n|---:|---|---:|\n| 1 | A | 10 |\n| 2 | B | NULL |\n| 3 | C | 99 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 2,
                                   'type', 'TABLE',
                                   'caption', 'DEPT',
                                   'content', '| DEPTNO | DNAME |\n|---:|---|\n| 10 | 영업 |\n| 20 | 인사 |'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', '부서가 없다는 것은 DEPTNO가 NULL이거나 DEPT 테이블에 존재하지 않는 부서번호를 가진 경우다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', 'DEPTNO IS NULL 조건과 DEPTNO NOT IN (...) 조건을 함께 사용해야 NULL 부서와 미존재 부서를 모두 조회할 수 있다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(2)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT * FROM EMP E, DEPT D WHERE E.DEPTNO = D.DEPTNO;')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT * FROM EMP WHERE DEPTNO IS NULL OR DEPTNO NOT IN (SELECT DEPTNO FROM DEPT);')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT * FROM EMP LEFT JOIN DEPT D ON E.DEPTNO = D.DEPTNO WHERE D.DEPTNO IS NOT NULL;')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'CODE', 'language', 'sql', 'dbms', 'oracle', 'content', 'SELECT * FROM EMP WHERE DEPTNO NOT IN (SELECT DEPTNO FROM DEPT);'))));


/* =========================================================
   48번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-02-03'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '다음 중 ROLLUP(A, B)에는 포함되지 않지만 CUBE(A, B)에는 포함되는 집계 조합은?',
                   'content_blocks', JSON_ARRAY()
           ),
           JSON_OBJECT(
                   'summary', 'ROLLUP(A, B)는 (A, B), (A), 전체 합계를 생성하고, CUBE(A, B)는 가능한 모든 조합을 생성한다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', 'CUBE(A, B)에는 (A, B), (A), (B), 전체 합계가 포함되므로 ROLLUP에 없는 조합은 (NULL, B)이다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(3)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '(A, B)')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '(A, NULL)')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '(NULL, B)')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '(NULL, NULL)'))));


/* =========================================================
   49번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-03-03'
               LIMIT 1
           ),
           NULL,
           'CONCEPT_RECALL',
           'EASY',
           JSON_OBJECT(
                   'stem', '아래 DDL에서 성별 컬럼에 ''M'' 또는 ''F''만 허용하도록 강제하는 제약 조건은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'oracle',
                                   'content', 'CREATE TABLE MEMBER (\n    ID VARCHAR2(10) PRIMARY KEY,\n    NAME VARCHAR2(30) NOT NULL,\n    SEX CHAR(1) ( ? ) (SEX IN (''M'', ''F''))\n);'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', '특정 컬럼 값이 지정된 조건을 만족하도록 제한하려면 CHECK 제약 조건을 사용한다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', 'SEX IN (''M'', ''F'')는 허용 가능한 값의 범위를 제한하는 조건이므로 CHECK가 적절하다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(2)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'NOT NULL')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'CHECK')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'PRIMARY KEY')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', 'FOREIGN KEY'))));


/* =========================================================
   50번 문제
   ========================================================= */

INSERT INTO problem (
    exam_id, exam_version_id, exam_scope_node_id, ai_generated_problem_id,
    expression_type, difficulty, question_content_json, explanation_json,
    answer_type, answer_json, choice_type, source_type, is_active
)
VALUES (
           @exam_id,
           @exam_version_id,
           (
               SELECT id
               FROM exam_scope_node
               WHERE exam_version_id = @exam_version_id
                 AND code = 'SQLD-SQL-01-05'
               LIMIT 1
           ),
           NULL,
           'SQL_ANALYSIS',
           'MEDIUM',
           JSON_OBJECT(
                   'stem', '아래 주문 테이블에 대한 SQL이 반환하는 결과에 대한 설명으로 가장 적절한 것은?',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'TABLE',
                                   'caption', '주문 테이블',
                                   'content', '| 고객ID | 주문번호 | 수량 |\n|---|---|---:|\n| C001 | 0100 | 5 |\n| C001 | 0101 | 12 |\n| C001 | 0102 | 3 |\n| C002 | 0103 | 8 |\n| C002 | 0104 | 9 |\n| C003 | 0105 | 15 |'
                           ),
                           JSON_OBJECT(
                                   'sequence', 2,
                                   'type', 'CODE',
                                   'language', 'sql',
                                   'dbms', 'oracle',
                                   'content', 'SELECT 고객ID, COUNT(*) AS 주문건수\nFROM 주문\nGROUP BY 고객ID\nHAVING MAX(수량) > 10;'
                           )
                                     )
           ),
           JSON_OBJECT(
                   'summary', '고객별로 그룹화한 뒤, 해당 고객의 주문 중 최대 수량이 10을 초과하는 고객만 남기고 주문 건수를 반환한다.',
                   'content_blocks', JSON_ARRAY(
                           JSON_OBJECT(
                                   'sequence', 1,
                                   'type', 'MARKDOWN',
                                   'content', 'HAVING MAX(수량) > 10은 주문 수량이 10을 넘는 주문이 하나라도 있는 고객을 찾는 조건이다.'
                           )
                                     )
           ),
           'SINGLE_CHOICE',
           JSON_OBJECT('correctChoiceNos', JSON_ARRAY(1)),
           'TEXT',
           'PAST_EXAM',
           TRUE
       );

SET @problem_id = LAST_INSERT_ID();

INSERT INTO problem_choice (problem_id, choice_no, choice_content_json)
VALUES
    (@problem_id, 1, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '주문 수량이 10을 넘는 건이 있는 고객의 주문 건수를 조회한다.')))),
    (@problem_id, 2, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '주문 수량 총 합계가 10을 넘는 고객의 주문 건수를 조회한다.')))),
    (@problem_id, 3, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '주문 횟수가 10건을 넘는 고객의 주문 건수를 조회한다.')))),
    (@problem_id, 4, JSON_OBJECT('content_blocks', JSON_ARRAY(JSON_OBJECT('sequence', 1, 'type', 'MARKDOWN', 'content', '수량 컬럼이 NULL이 아닌 고객의 주문 건수를 조회한다.'))));

COMMIT;

START TRANSACTION;

INSERT INTO workbook_type (
    code,
    name,
    display_order
)
VALUES (
           'PAST',
           '복원 기출',
           1
       );

SET @exam_id = (
    SELECT id
    FROM exam
    WHERE code = 'SQLD'
    LIMIT 1
);

SET @exam_version_id = (
    SELECT id
    FROM exam_version
    WHERE exam_id = @exam_id
      AND version_no = 2025
    LIMIT 1
);

SET @past_workbook_type_id = (
    SELECT id
    FROM workbook_type
    WHERE code = 'PAST'
    LIMIT 1
);

INSERT INTO exam_workbook_type (
    exam_id,
    workbook_type_id,
    is_active
)
VALUES (
           @exam_id,
           @past_workbook_type_id,
           TRUE
       );

INSERT INTO workbook (
    exam_workbook_type_id,
    no,
    exam_year,
    total_question_count,
    time_limit,
    status
)
VALUES (
           @past_workbook_type_id,
           1,
           2026,
           50,
           5400,
           'REVIEWED'
       );

SET @workbook_id = LAST_INSERT_ID();

INSERT INTO workbook_item (
    workbook_id,
    problem_id,
    item_no
)
SELECT
    @workbook_id AS workbook_id,
    ordered_problem.problem_id,
    ordered_problem.item_no
FROM (
         SELECT
             p.id AS problem_id,
             ROW_NUMBER() OVER (ORDER BY p.id) AS item_no
         FROM problem p
         WHERE p.exam_id = @exam_id
           AND p.exam_version_id = @exam_version_id
           AND p.source_type = 'PAST_EXAM'
           AND p.is_active = TRUE
         ORDER BY p.id
         LIMIT 50
     ) ordered_problem;

COMMIT;

SELECT
    wi.item_no,
    wi.problem_id,
    JSON_UNQUOTE(JSON_EXTRACT(p.question_content_json, '$.stem')) AS stem
FROM workbook_item wi
         JOIN problem p ON p.id = wi.problem_id
WHERE wi.workbook_id = @workbook_id
ORDER BY wi.item_no;