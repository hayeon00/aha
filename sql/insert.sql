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