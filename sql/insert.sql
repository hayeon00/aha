INSERT INTO exam (code,name,is_active) VALUES ('SQLD','SQLD',1);

SET @exam_id = (
    SELECT id
    FROM exam
    WHERE code = 'SQLD'
    LIMIT 1
);

-- exam 테이블에 SQLD가 없으면 먼저 생성
INSERT INTO exam (
    code,
    name,
    is_active
)
SELECT
    'SQLD',
    'SQL 개발자(SQLD)',
    1
WHERE @exam_id IS NULL;

-- exam_id 다시 설정
SET @exam_id = (
    SELECT id
    FROM exam
    WHERE code = 'SQLD'
    LIMIT 1
);


INSERT INTO exam_version (
    exam_id,
    version_no,
    version_name,
    default_question_count,
    duration_type,
    default_duration_seconds,
    total_score,
    passing_rule_type,
    passing_score,
    has_subject_fail_rule,
    subject_fail_threshold,
    status
)
SELECT
    @exam_id,
    2025,
    'SQLD 2025 개정판',
    50,
    'FIXED',
    5400,
    100,
    'TOTAL_AND_SUBJECT_FAIL',
    60,
    1,
    40,
    'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM exam_version
    WHERE exam_id = @exam_id
      AND version_no = 2025
);

-- 생성된 exam_version_id 가져오기
SET @exam_version_id = (
    SELECT id
    FROM exam_version
    WHERE exam_id = @exam_id
      AND version_no = 2025
    LIMIT 1
);


INSERT INTO exam_part (
    exam_version_id,
    code,
    name,
    default_question_count,
    default_duration_seconds,
    total_score,
    is_subject_fail_target,
    subject_fail_threshold_score,
    is_active,
    display_order
)
SELECT
    @exam_version_id,
    'SQLD_PART_01',
    '데이터 모델링의 이해',
    10,
    NULL,
    20,
    1,
    8,
    1,
    1
WHERE NOT EXISTS (
    SELECT 1
    FROM exam_part
    WHERE exam_version_id = @exam_version_id
      AND code = 'SQLD_PART_01'
);

INSERT INTO exam_part (
    exam_version_id,
    code,
    name,
    default_question_count,
    default_duration_seconds,
    total_score,
    is_subject_fail_target,
    subject_fail_threshold_score,
    is_active,
    display_order
)
SELECT
    @exam_version_id,
    'SQLD_PART_02',
    'SQL 기본 및 활용',
    40,
    NULL,
    80,
    1,
    32,
    1,
    2
WHERE NOT EXISTS (
    SELECT 1
    FROM exam_part
    WHERE exam_version_id = @exam_version_id
      AND code = 'SQLD_PART_02'
);


SELECT *
FROM exam
WHERE id = @exam_id;

SELECT *
FROM exam_version
WHERE id = @exam_version_id;

SELECT *
FROM exam_part
WHERE exam_version_id = @exam_version_id
ORDER BY display_order;


INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-MODELING-01', NULL, 'SECTION', 1, '데이터 모델링의 이해', 0, 1, 1 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_01';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-MODELING-02', NULL, 'SECTION', 1, '데이터 모델과 SQL', 0, 1, 2 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_01';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-SQL-01', NULL, 'SECTION', 1, 'SQL 기본', 0, 1, 1 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_02';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-SQL-02', NULL, 'SECTION', 1, 'SQL 활용', 0, 1, 2 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_02';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-SQL-03', NULL, 'SECTION', 1, '관리 구문', 0, 1, 3 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_02';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-MODELING-01-01', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-MODELING-01' LIMIT 1), 'TOPIC', 2, '데이터모델의 이해', 1, 1, 1 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_01';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-MODELING-01-02', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-MODELING-01' LIMIT 1), 'TOPIC', 2, '엔터티', 1, 1, 2 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_01';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-MODELING-01-03', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-MODELING-01' LIMIT 1), 'TOPIC', 2, '속성', 1, 1, 3 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_01';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-MODELING-01-04', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-MODELING-01' LIMIT 1), 'TOPIC', 2, '관계', 1, 1, 4 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_01';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-MODELING-01-05', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-MODELING-01' LIMIT 1), 'TOPIC', 2, '식별자', 1, 1, 5 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_01';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-MODELING-02-01', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-MODELING-02' LIMIT 1), 'TOPIC', 2, '정규화', 1, 1, 1 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_01';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-MODELING-02-02', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-MODELING-02' LIMIT 1), 'TOPIC', 2, '관계와 조인의 이해', 1, 1, 2 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_01';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-MODELING-02-03', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-MODELING-02' LIMIT 1), 'TOPIC', 2, '모델이 표현하는 트랜잭션의 이해', 1, 1, 3 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_01';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-MODELING-02-04', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-MODELING-02' LIMIT 1), 'TOPIC', 2, 'Null 속성의 이해', 1, 1, 4 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_01';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-MODELING-02-05', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-MODELING-02' LIMIT 1), 'TOPIC', 2, '본질식별자 vs 인조식별자', 1, 1, 5 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_01';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-SQL-01-01', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-SQL-01' LIMIT 1), 'TOPIC', 2, '관계형 데이터베이스 개요', 1, 1, 1 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_02';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-SQL-01-02', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-SQL-01' LIMIT 1), 'TOPIC', 2, 'SELECT 문', 1, 1, 2 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_02';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-SQL-01-03', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-SQL-01' LIMIT 1), 'TOPIC', 2, '함수', 1, 1, 3 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_02';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-SQL-01-04', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-SQL-01' LIMIT 1), 'TOPIC', 2, 'WHERE 절', 1, 1, 4 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_02';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-SQL-01-05', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-SQL-01' LIMIT 1), 'TOPIC', 2, 'GROUP BY, HAVING 절', 1, 1, 5 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_02';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-SQL-01-06', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-SQL-01' LIMIT 1), 'TOPIC', 2, 'ORDER BY 절', 1, 1, 6 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_02';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-SQL-01-07', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-SQL-01' LIMIT 1), 'TOPIC', 2, '조인', 1, 1, 7 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_02';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-SQL-01-08', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-SQL-01' LIMIT 1), 'TOPIC', 2, '표준 조인', 1, 1, 8 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_02';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-SQL-02-01', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-SQL-02' LIMIT 1), 'TOPIC', 2, '서브쿼리', 1, 1, 1 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_02';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-SQL-02-02', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-SQL-02' LIMIT 1), 'TOPIC', 2, '집합 연산자', 1, 1, 2 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_02';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-SQL-02-03', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-SQL-02' LIMIT 1), 'TOPIC', 2, '그룹 함수', 1, 1, 3 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_02';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-SQL-02-04', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-SQL-02' LIMIT 1), 'TOPIC', 2, '윈도우 함수', 1, 1, 4 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_02';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-SQL-02-05', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-SQL-02' LIMIT 1), 'TOPIC', 2, 'Top N 쿼리', 1, 1, 5 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_02';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-SQL-02-06', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-SQL-02' LIMIT 1), 'TOPIC', 2, '계층형 질의와 셀프 조인', 1, 1, 6 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_02';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-SQL-02-07', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-SQL-02' LIMIT 1), 'TOPIC', 2, 'PIVOT 절과 UNPIVOT절', 1, 1, 7 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_02';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-SQL-02-08', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-SQL-02' LIMIT 1), 'TOPIC', 2, '정규 표현식', 1, 1, 8 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_02';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-SQL-03-01', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-SQL-03' LIMIT 1), 'TOPIC', 2, 'DML', 1, 1, 1 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_02';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-SQL-03-02', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-SQL-03' LIMIT 1), 'TOPIC', 2, 'TCL', 1, 1, 2 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_02';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-SQL-03-03', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-SQL-03' LIMIT 1), 'TOPIC', 2, 'DDL', 1, 1, 3 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_02';

INSERT INTO exam_scope_node (exam_version_id, exam_part_id, code, parent_id, node_type, depth, title, is_leaf, is_active, display_order) SELECT ev.id, ep.id, 'SQLD-SQL-03-04', (SELECT id FROM exam_scope_node WHERE code = 'SQLD-SQL-03' LIMIT 1), 'TOPIC', 2, 'DCL', 1, 1, 4 FROM exam_version ev JOIN exam e ON ev.exam_id = e.id JOIN exam_part ep ON ep.exam_version_id = ev.id WHERE e.code = 'SQLD' AND ev.version_no = 2025 AND ep.code = 'SQLD_PART_02';


INSERT INTO domain_type (
    code,
    name,
    is_active
) VALUES
      ('CONCEPT', '개념 문제', TRUE),
      ('WORKBOOK', '워크북', TRUE);


-- =========================================================
-- SQLD 1과목: 데이터 모델링의 이해
-- 대상 소목차:
-- SQLD-MODELING-01-01 데이터 모델의 이해
-- SQLD-MODELING-01-02 엔터티
-- SQLD-MODELING-01-03 속성
-- SQLD-MODELING-01-04 관계
-- SQLD-MODELING-01-05 식별자
-- =========================================================


-- =========================================================
-- 1. 데이터 모델의 이해
-- =========================================================

INSERT INTO learning_content (
    exam_scope_node_id,
    title,
    summary,
    rag_enabled,
    is_active,
    display_order
)
SELECT
    esn.id,
    '데이터 모델의 이해',
    '데이터 모델은 현실 세계의 업무 데이터를 정보시스템에서 관리할 수 있도록 구조화하여 표현한 것입니다. SQLD에서는 데이터 모델링의 목적, 특징, 단계, 유의점을 중심으로 출제됩니다.',
    TRUE,
    TRUE,
    1
FROM exam_scope_node esn
WHERE esn.code = 'SQLD-MODELING-01-01';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'BASE_EXPLANATION',
    '데이터 모델이란?',
    '데이터 모델은 현실 세계에서 업무에 필요한 데이터를 정보시스템에서 관리할 수 있도록 정리한 구조입니다. 현실 세계에는 회원, 주문, 상품, 결제, 게시글처럼 다양한 데이터가 존재합니다.

데이터 모델링은 이런 데이터를 아무렇게나 저장하는 것이 아니라, 어떤 데이터를 관리해야 하는지, 데이터 사이에 어떤 관계가 있는지, 각 데이터가 어떤 속성을 가지는지를 체계적으로 표현하는 과정입니다.

예를 들어 쇼핑몰 서비스를 만든다고 생각해보면 회원, 상품, 주문, 결제는 각각 중요한 데이터입니다. 회원은 여러 번 주문할 수 있고, 주문은 여러 상품을 포함할 수 있으며, 결제는 특정 주문에 대해 발생합니다. 이런 업무 구조를 데이터 관점에서 정리하는 것이 데이터 모델링입니다.

데이터 모델링은 단순히 테이블을 만드는 작업이 아니라, 업무를 데이터 중심으로 분석하고 구조화하는 작업입니다. 좋은 데이터 모델은 중복을 줄이고, 데이터의 의미를 명확하게 하며, 시스템이 변경되더라도 유연하게 대응할 수 있도록 도와줍니다.',
    1,
    1,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-01';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'CORE_POINT',
    '핵심 포인트',
    '데이터 모델의 이해에서 가장 중요한 핵심은 현실 세계의 업무를 데이터 구조로 표현한다는 점입니다.

데이터 모델링의 대표적인 특징은 추상화, 단순화, 명확화입니다.

추상화는 현실 세계의 모든 정보를 다 표현하는 것이 아니라, 시스템에 필요한 핵심 정보만 뽑아내는 것입니다. 단순화는 복잡한 업무 구조를 이해하기 쉬운 형태로 정리하는 것입니다. 명확화는 데이터의 의미와 관계를 누구나 동일하게 이해할 수 있도록 표현하는 것입니다.

데이터 모델링은 보통 개념적 모델링, 논리적 모델링, 물리적 모델링 단계로 나눌 수 있습니다. 개념적 모델링은 업무 전체의 큰 데이터 구조를 파악하는 단계이고, 논리적 모델링은 엔터티, 속성, 관계, 식별자를 구체화하는 단계입니다. 물리적 모델링은 실제 DBMS에 맞게 테이블, 컬럼, 인덱스 등을 설계하는 단계입니다.',
    2,
    2,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-01';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'EXAMPLE',
    '예시로 이해하기',
    '온라인 쇼핑몰을 예로 들어보겠습니다.

쇼핑몰에는 회원, 상품, 주문, 주문상품, 결제 같은 데이터가 필요합니다. 회원은 서비스를 이용하는 사람이고, 상품은 판매되는 물건이며, 주문은 회원이 상품을 구매한 기록입니다. 주문상품은 하나의 주문에 포함된 상품 목록이고, 결제는 주문에 대해 실제 금액을 지불한 기록입니다.

이때 회원과 주문은 관계가 있습니다. 한 명의 회원은 여러 번 주문할 수 있고, 하나의 주문은 한 명의 회원에게 속합니다. 따라서 회원과 주문은 1:N 관계로 볼 수 있습니다.

주문과 상품은 바로 연결하면 다대다 관계가 될 수 있습니다. 하나의 주문에는 여러 상품이 들어갈 수 있고, 하나의 상품은 여러 주문에 포함될 수 있기 때문입니다. 그래서 보통 주문상품이라는 중간 엔터티를 두어 주문과 상품의 관계를 풀어냅니다.

이처럼 데이터 모델링은 업무에서 발생하는 데이터를 엔터티, 속성, 관계, 식별자로 정리하는 과정입니다.',
    3,
    3,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-01';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'CONFUSION_NOTE',
    '헷갈리는 개념 정리',
    '데이터 모델링과 데이터베이스 설계를 같은 개념으로 생각하기 쉽지만, 정확히는 차이가 있습니다.

데이터 모델링은 업무 데이터를 분석하고 구조화하는 과정입니다. 어떤 데이터를 관리해야 하는지, 데이터 간 관계가 무엇인지, 각 데이터가 어떤 의미를 가지는지를 정리하는 단계입니다.

반면 데이터베이스 설계는 데이터 모델을 실제 DBMS에 맞게 구현하는 과정에 가깝습니다. 테이블명, 컬럼명, 데이터 타입, 기본키, 외래키, 인덱스 등을 정하는 작업은 물리적 설계에 해당합니다.

개념적 모델링, 논리적 모델링, 물리적 모델링도 구분해야 합니다. 개념적 모델링은 업무 중심의 큰 구조를 파악하는 단계이고, 논리적 모델링은 엔터티, 속성, 관계, 식별자를 구체적으로 표현하는 단계이며, 물리적 모델링은 실제 DBMS에 맞게 테이블과 컬럼을 설계하는 단계입니다.',
    4,
    4,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-01';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'EXAM_POINT',
    'SQLD 출제 포인트',
    'SQLD에서는 데이터 모델의 이해 파트에서 데이터 모델링의 목적, 특징, 단계, 유의점이 자주 출제됩니다.

데이터 모델링의 목적은 업무에서 필요한 데이터를 구조화하고, 데이터 간 관계를 명확하게 표현하며, 데이터 중복과 불일치를 줄이는 것입니다. 또한 시스템 변경에 유연하게 대응할 수 있는 구조를 만드는 것도 중요합니다.

모델링의 특징은 추상화, 단순화, 명확화입니다. 데이터 모델링 단계는 개념적 모델링, 논리적 모델링, 물리적 모델링으로 구분합니다.

데이터 모델링의 유의점으로는 중복, 비유연성, 비일관성이 자주 등장합니다. 문제에서 현실 세계를 일정한 표기법으로 표현한다, 업무 데이터를 구조화한다, 데이터 간 관계를 명확히 한다는 표현이 나오면 데이터 모델링의 특징이나 목적과 연결해서 판단하면 됩니다.',
    5,
    5,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-01';



-- =========================================================
-- 2. 엔터티
-- =========================================================

INSERT INTO learning_content (
    exam_scope_node_id,
    title,
    summary,
    rag_enabled,
    is_active,
    display_order
)
SELECT
    esn.id,
    '엔터티',
    '엔터티는 업무에서 관리해야 하는 데이터의 대상입니다. SQLD에서는 엔터티의 정의, 특징, 유형 엔터티·개념 엔터티·사건 엔터티, 기본 엔터티·중심 엔터티·행위 엔터티 구분이 자주 출제됩니다.',
    TRUE,
    TRUE,
    2
FROM exam_scope_node esn
WHERE esn.code = 'SQLD-MODELING-01-02';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'BASE_EXPLANATION',
    '엔터티란?',
    '엔터티는 업무에서 관리해야 하는 데이터의 대상입니다. 쉽게 말하면 시스템에서 정보를 저장하고 관리할 필요가 있는 사람, 사물, 사건, 개념 등을 엔터티라고 합니다.

예를 들어 학사관리 시스템에서는 학생, 교수, 과목, 수강신청이 엔터티가 될 수 있습니다. 쇼핑몰 시스템에서는 회원, 상품, 주문, 결제, 배송이 엔터티가 될 수 있습니다.

엔터티는 단순히 명사라고 해서 모두 엔터티가 되는 것은 아닙니다. 업무적으로 관리할 필요가 있어야 하고, 여러 개의 인스턴스를 가질 수 있어야 하며, 속성을 가져야 합니다. 또한 다른 엔터티와 관계를 가질 수 있어야 합니다.

예를 들어 회원은 여러 명의 회원 인스턴스를 가질 수 있고, 회원ID, 이름, 이메일 같은 속성을 가지며, 주문 엔터티와 관계를 맺을 수 있으므로 엔터티로 볼 수 있습니다.',
    1,
    1,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-02';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'CORE_POINT',
    '핵심 포인트',
    '엔터티를 판단할 때는 업무에서 필요한 대상인지, 여러 개의 인스턴스를 가지는지, 속성을 가지는지, 다른 엔터티와 관계를 가질 수 있는지를 확인해야 합니다.

엔터티는 시스템에서 반드시 관리해야 하는 정보여야 합니다. 또한 하나의 데이터가 아니라 같은 성격을 가진 데이터 집합이어야 합니다. 엔터티는 자신을 설명하는 속성을 가져야 하고, 업무 흐름 속에서 다른 엔터티와 연결될 수 있어야 합니다.

엔터티는 유무형에 따라 유형 엔터티, 개념 엔터티, 사건 엔터티로 나눌 수 있습니다. 유형 엔터티는 물리적 형태가 있는 대상이고, 개념 엔터티는 물리적 형태는 없지만 업무적으로 관리해야 하는 개념입니다. 사건 엔터티는 업무 수행 과정에서 발생하는 사건이나 행위를 의미합니다.

발생 시점에 따라 기본 엔터티, 중심 엔터티, 행위 엔터티로도 분류할 수 있습니다.',
    2,
    2,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-02';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'EXAMPLE',
    '예시로 이해하기',
    '학사관리 시스템을 예로 들어보겠습니다.

학생 엔터티는 학교에 소속된 학생 정보를 관리하기 위한 대상입니다. 학생 엔터티에는 학번, 이름, 학과, 입학일자 같은 속성이 있을 수 있습니다.

과목 엔터티는 학교에서 개설하는 과목 정보를 관리하기 위한 대상입니다. 과목코드, 과목명, 학점 같은 속성을 가질 수 있습니다.

수강신청 엔터티는 학생이 과목을 신청한 사건을 관리하기 위한 대상입니다. 수강신청은 학생과 과목 사이에서 발생하는 행위이므로 행위 엔터티로 볼 수 있습니다.

쇼핑몰에서는 회원과 상품이 기본 엔터티가 될 수 있고, 주문은 업무 흐름의 중심이 되는 중심 엔터티가 될 수 있습니다. 주문상품은 주문과 상품 사이에서 발생하는 상세 구매 내역이므로 행위 엔터티로 볼 수 있습니다.',
    3,
    3,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-02';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'CONFUSION_NOTE',
    '엔터티와 인스턴스 헷갈리지 않기',
    '엔터티와 인스턴스는 반드시 구분해야 합니다.

엔터티는 같은 성격의 데이터를 모아놓은 집합입니다. 인스턴스는 엔터티에 속하는 실제 하나의 데이터입니다.

예를 들어 학생은 엔터티입니다. 김하연 학생, 이민수 학생은 학생 엔터티에 속하는 인스턴스입니다. 상품은 엔터티이고, 노트북, 키보드, 마우스는 상품 엔터티의 인스턴스가 될 수 있습니다.

또 헷갈리는 부분은 엔터티와 속성입니다. 회원은 관리해야 하는 대상이므로 엔터티이고, 회원명은 회원을 설명하는 정보이므로 속성입니다. 주문은 업무상 관리해야 하는 사건이므로 엔터티이고, 주문일자는 주문 엔터티의 속성입니다.',
    4,
    4,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-02';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'EXAM_POINT',
    'SQLD 출제 포인트',
    '엔터티 파트에서는 정의와 특징, 분류 기준이 자주 출제됩니다.

엔터티는 업무에서 필요로 하는 정보여야 하고, 유일하게 식별 가능한 인스턴스를 가져야 하며, 속성을 가져야 합니다. 또한 다른 엔터티와 관계를 가질 수 있고, 두 개 이상의 인스턴스를 가져야 합니다.

유형 엔터티는 물리적 형태가 있는 엔터티입니다. 개념 엔터티는 물리적 형태는 없지만 관리해야 하는 개념입니다. 사건 엔터티는 업무 수행 과정에서 발생하는 사건입니다.

발생 시점에 따른 분류도 중요합니다. 기본 엔터티는 독립적으로 존재하는 기준 정보이고, 중심 엔터티는 업무의 중심이 되는 정보이며, 행위 엔터티는 두 엔터티 사이의 행위나 거래 결과로 발생하는 정보입니다.

시험에서는 학생은 엔터티인가, 수강신청은 어떤 엔터티인가, 주문상품은 어떤 유형인가처럼 예시를 주고 분류를 묻는 문제가 나올 수 있습니다.',
    5,
    5,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-02';



-- =========================================================
-- 3. 속성
-- =========================================================

INSERT INTO learning_content (
    exam_scope_node_id,
    title,
    summary,
    rag_enabled,
    is_active,
    display_order
)
SELECT
    esn.id,
    '속성',
    '속성은 엔터티가 가지는 구체적인 정보 항목입니다. SQLD에서는 속성의 정의, 특징, 기본속성·설계속성·파생속성 구분이 자주 출제됩니다.',
    TRUE,
    TRUE,
    3
FROM exam_scope_node esn
WHERE esn.code = 'SQLD-MODELING-01-03';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'BASE_EXPLANATION',
    '속성이란?',
    '속성은 엔터티가 가지는 구체적인 정보 항목입니다. 엔터티가 업무에서 관리해야 하는 대상이라면, 속성은 그 대상을 설명하는 세부 정보입니다.

예를 들어 회원 엔터티가 있다면 회원ID, 이름, 이메일, 가입일자, 전화번호 같은 항목이 속성이 될 수 있습니다. 상품 엔터티가 있다면 상품코드, 상품명, 가격, 재고수량 같은 항목이 속성이 될 수 있습니다.

속성은 업무적으로 의미가 있어야 하며, 하나의 엔터티에 종속되어야 합니다. 또한 하나의 속성은 하나의 의미를 가지는 것이 좋습니다.

예를 들어 주소라는 속성에 우편번호, 기본주소, 상세주소를 모두 하나로 합쳐 저장하면 나중에 검색이나 변경이 어려울 수 있습니다. 따라서 속성은 데이터의 의미를 명확하게 표현하고, 업무에서 필요한 단위로 적절히 분리하는 것이 중요합니다.',
    1,
    1,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-03';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'CORE_POINT',
    '핵심 포인트',
    '속성에서 중요한 핵심은 엔터티를 설명하는 정보 항목이라는 점입니다.

속성은 엔터티에 종속됩니다. 속성은 독립적으로 존재하기보다는 특정 엔터티를 설명합니다. 속성은 업무적으로 의미가 있어야 합니다. 시스템에서 관리할 필요가 없는 값은 속성으로 보기 어렵습니다.

또한 속성은 하나의 의미를 가지는 것이 좋습니다. 하나의 속성에 여러 의미가 섞이면 데이터 관리가 어려워집니다. 속성은 인스턴스마다 값을 가질 수 있습니다. 회원 엔터티의 각 회원 인스턴스는 이름, 이메일, 가입일자 등의 값을 가집니다.

속성은 성격에 따라 기본속성, 설계속성, 파생속성으로 나눌 수 있습니다. 기본속성은 업무에서 자연스럽게 발생하는 속성이고, 설계속성은 시스템 설계를 위해 만든 속성입니다. 파생속성은 다른 속성으로부터 계산되거나 도출되는 속성입니다.',
    2,
    2,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-03';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'EXAMPLE',
    '예시로 이해하기',
    '회원 엔터티를 예로 들어보겠습니다.

회원 엔터티의 속성으로는 회원ID, 이름, 이메일, 비밀번호, 가입일자, 생년월일, 전화번호가 있을 수 있습니다.

이 중 이름, 이메일, 생년월일, 전화번호는 업무에서 자연스럽게 수집되는 정보이므로 기본속성으로 볼 수 있습니다.

회원ID는 시스템에서 회원을 구분하기 위해 부여한 값이라면 설계속성으로 볼 수 있습니다.

나이는 생년월일을 기준으로 계산할 수 있으므로 파생속성으로 볼 수 있습니다. 나이를 직접 저장할 수도 있지만, 생년월일이 변경되거나 시간이 지나면 나이 값도 바뀌어야 하므로 관리에 주의해야 합니다.

주문 엔터티에서는 주문번호, 주문일자, 주문상태, 총주문금액 등이 속성이 될 수 있습니다. 총주문금액은 주문상품의 가격과 수량을 계산해서 얻을 수 있다면 파생속성으로 볼 수 있습니다.',
    3,
    3,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-03';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'CONFUSION_NOTE',
    '속성 분류 헷갈리지 않기',
    '속성 분류에서 가장 자주 헷갈리는 부분은 기본속성, 설계속성, 파생속성입니다.

기본속성은 업무에서 원래부터 존재하는 데이터입니다. 예를 들어 회원 이름, 생년월일, 이메일은 회원 업무에서 자연스럽게 발생하는 기본속성입니다.

설계속성은 업무상 원래 존재했다기보다는 시스템 설계 과정에서 필요해서 만든 속성입니다. 회원번호, 주문번호, 게시글번호처럼 시스템에서 식별이나 관리를 위해 부여하는 값이 대표적입니다.

파생속성은 다른 속성으로부터 계산할 수 있는 속성입니다. 나이, 총주문금액, 평균점수, 잔액 등이 대표적입니다.

헷갈릴 때는 이 값이 업무에서 원래 존재하는가, 시스템이 관리하려고 만든 값인가, 다른 값으로 계산할 수 있는가를 기준으로 판단하면 됩니다.',
    4,
    4,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-03';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'EXAM_POINT',
    'SQLD 출제 포인트',
    '속성 파트에서는 정의와 분류 문제가 자주 출제됩니다.

속성은 엔터티를 설명하는 정보 항목이고, 업무적으로 관리할 필요가 있는 값이며, 엔터티의 인스턴스가 가지는 구체적인 값입니다.

속성의 분류도 중요합니다. 기본속성은 업무에서 자연스럽게 발생하는 속성입니다. 설계속성은 시스템 설계 과정에서 만든 속성입니다. 파생속성은 다른 속성으로부터 계산되는 속성입니다.

시험에서 자주 나오는 예시는 다음과 같습니다. 이름, 생년월일, 이메일은 기본속성으로 볼 수 있습니다. 회원번호, 주문번호는 설계속성으로 볼 수 있습니다. 나이, 합계금액, 평균점수는 파생속성으로 볼 수 있습니다.

하나의 속성에는 하나의 의미를 담는 것이 좋고, 파생속성은 원본 데이터와 불일치가 발생하지 않도록 관리해야 합니다.',
    5,
    5,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-03';



-- =========================================================
-- 4. 관계
-- =========================================================

INSERT INTO learning_content (
    exam_scope_node_id,
    title,
    summary,
    rag_enabled,
    is_active,
    display_order
)
SELECT
    esn.id,
    '관계',
    '관계는 엔터티와 엔터티 사이의 업무적 연관성을 의미합니다. SQLD에서는 관계의 정의, 관계 차수, 선택성, 식별관계와 비식별관계의 차이가 자주 출제됩니다.',
    TRUE,
    TRUE,
    4
FROM exam_scope_node esn
WHERE esn.code = 'SQLD-MODELING-01-04';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'BASE_EXPLANATION',
    '관계란?',
    '관계는 엔터티와 엔터티 사이의 업무적 연관성을 의미합니다. 데이터는 하나의 엔터티만으로 의미가 완성되는 경우도 있지만, 대부분 다른 엔터티와 연결될 때 더 정확한 의미를 가집니다.

예를 들어 회원과 주문을 생각해보면, 회원은 주문을 할 수 있고 주문은 특정 회원에 의해 생성됩니다. 이때 회원과 주문 사이에는 회원은 주문을 한다는 관계가 존재합니다.

관계를 정의할 때는 단순히 두 엔터티가 화면에서 함께 보인다는 이유만으로 관계를 만들면 안 됩니다. 업무 규칙상 두 엔터티가 실제로 연결되어 있어야 합니다.

관계는 관계명, 관계 차수, 선택성 등을 통해 표현할 수 있습니다. 관계 차수는 하나의 인스턴스가 상대 엔터티의 몇 개 인스턴스와 연결될 수 있는지를 나타내고, 선택성은 관계가 반드시 필요한지 선택적인지를 나타냅니다.',
    1,
    1,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-04';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'CORE_POINT',
    '핵심 포인트',
    '관계에서 가장 중요한 핵심은 엔터티 간 업무적 연결입니다.

관계를 이해할 때는 관계명, 관계 차수, 관계 선택성, 식별 여부를 함께 봐야 합니다.

관계명은 두 엔터티가 어떤 의미로 연결되는지 표현합니다. 예를 들어 회원은 주문을 한다는 관계명이 될 수 있습니다.

관계 차수는 두 엔터티 인스턴스가 몇 개씩 연결될 수 있는지 나타냅니다. 대표적으로 1:1, 1:N, M:N 관계가 있습니다.

관계 선택성은 관계가 필수인지 선택인지 나타냅니다. 주문은 반드시 회원이 있어야 하지만, 회원은 주문이 없어도 존재할 수 있습니다.

식별 여부는 부모 엔터티의 식별자가 자식 엔터티의 주식별자에 포함되는지 여부에 따라 식별관계와 비식별관계로 나눌 수 있습니다.',
    2,
    2,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-04';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'EXAMPLE',
    '예시로 이해하기',
    '쇼핑몰 시스템을 예로 들어보겠습니다.

회원과 주문 사이에는 관계가 있습니다. 한 명의 회원은 여러 주문을 할 수 있고, 하나의 주문은 한 명의 회원에게 속합니다. 따라서 회원과 주문은 1:N 관계입니다.

주문과 상품 사이도 관계가 있습니다. 하나의 주문에는 여러 상품이 포함될 수 있고, 하나의 상품은 여러 주문에 포함될 수 있습니다. 이 관계는 M:N 관계입니다. 하지만 실제 데이터베이스에서는 M:N 관계를 그대로 구현하기 어렵기 때문에 주문상품이라는 중간 엔터티를 만들어 관계를 해소합니다.

회원과 회원상세정보는 1:1 관계로 설계할 수도 있습니다. 하나의 회원은 하나의 상세정보를 가지고, 하나의 상세정보는 하나의 회원에게만 속하는 구조라면 1:1 관계가 됩니다.

관계 차수는 업무 규칙에 따라 결정됩니다. 단순히 테이블을 어떻게 만들고 싶은지가 아니라, 실제 업무에서 데이터가 어떻게 연결되는지를 기준으로 판단해야 합니다.',
    3,
    3,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-04';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'CONFUSION_NOTE',
    '관계 차수와 선택성 헷갈리지 않기',
    '관계에서 자주 헷갈리는 부분은 차수와 선택성입니다.

관계 차수는 몇 개와 연결되는가를 나타냅니다. 선택성은 반드시 연결되어야 하는가를 나타냅니다.

예를 들어 회원과 주문 관계를 보면, 한 명의 회원은 여러 주문을 할 수 있으므로 회원과 주문은 1:N 관계입니다. 하지만 회원이 반드시 주문을 해야 하는 것은 아닙니다. 회원가입만 하고 주문을 하지 않은 회원도 있을 수 있습니다. 따라서 회원 입장에서 주문은 선택 관계일 수 있습니다.

반대로 주문은 반드시 주문한 회원이 있어야 합니다. 회원 없이 주문만 존재하는 것은 일반적인 쇼핑몰 업무에서는 자연스럽지 않습니다. 따라서 주문 입장에서 회원은 필수 관계일 수 있습니다.

즉 1:N 관계라고 해서 양쪽이 모두 필수 관계인 것은 아닙니다. 차수와 선택성은 따로 판단해야 합니다.',
    4,
    4,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-04';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'EXAM_POINT',
    'SQLD 출제 포인트',
    '관계 파트에서는 관계의 정의, 관계 차수, 선택성과 필수성, M:N 관계 해소, 식별관계와 비식별관계가 자주 출제됩니다.

관계는 엔터티와 엔터티 사이의 업무적 연관성입니다. 두 엔터티 사이에 의미 있는 연결이 있을 때 관계를 정의합니다.

관계 차수에는 1:1, 1:N, M:N 관계가 있습니다. 선택성과 필수성도 함께 구분해야 합니다. 어떤 엔터티는 상대 엔터티 없이 존재할 수 있고, 어떤 엔터티는 상대 엔터티가 반드시 있어야 존재할 수 있습니다.

M:N 관계는 실제 설계에서 중간 엔터티를 통해 해소하는 경우가 많습니다. 예를 들어 주문과 상품 사이에 주문상품 엔터티를 둡니다.

식별관계와 비식별관계도 자주 출제됩니다. 부모 식별자가 자식의 주식별자에 포함되면 식별관계이고, 부모 식별자가 자식의 일반 속성으로만 존재하면 비식별관계입니다.',
    5,
    5,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-04';



-- =========================================================
-- 5. 식별자
-- =========================================================

INSERT INTO learning_content (
    exam_scope_node_id,
    title,
    summary,
    rag_enabled,
    is_active,
    display_order
)
SELECT
    esn.id,
    '식별자',
    '식별자는 엔터티의 인스턴스를 유일하게 구분할 수 있는 속성 또는 속성의 집합입니다. SQLD에서는 주식별자, 보조식별자, 내부식별자, 외부식별자, 식별관계와 비식별관계가 자주 출제됩니다.',
    TRUE,
    TRUE,
    5
FROM exam_scope_node esn
WHERE esn.code = 'SQLD-MODELING-01-05';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'BASE_EXPLANATION',
    '식별자란?',
    '식별자는 엔터티에 속한 각각의 인스턴스를 유일하게 구분할 수 있는 속성 또는 속성의 집합입니다.

예를 들어 회원 엔터티에서 회원ID가 각 회원을 유일하게 구분할 수 있다면 회원ID는 식별자가 될 수 있습니다. 학생 엔터티에서는 학번이 식별자가 될 수 있고, 주문 엔터티에서는 주문번호가 식별자가 될 수 있습니다.

식별자는 데이터 중복과 혼동을 막기 위해 매우 중요합니다. 만약 회원을 이름만으로 구분한다면 같은 이름을 가진 회원이 여러 명 있을 수 있기 때문에 정확한 식별이 어렵습니다. 따라서 식별자는 유일하게 구분 가능하고, 값이 반드시 존재하며, 가능하면 변경되지 않는 값이어야 합니다.

식별자는 대표성에 따라 주식별자와 보조식별자로 나눌 수 있고, 생성 위치에 따라 내부식별자와 외부식별자로 나눌 수 있습니다. 또한 속성 수에 따라 단일식별자와 복합식별자로 나눌 수 있습니다.',
    1,
    1,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-05';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'CORE_POINT',
    '핵심 포인트',
    '식별자의 핵심은 각 인스턴스를 유일하게 구분할 수 있어야 한다는 점입니다.

좋은 식별자는 유일성, 최소성, 불변성, 존재성을 가져야 합니다.

유일성은 하나의 식별자 값이 하나의 인스턴스만 가리켜야 한다는 의미입니다. 최소성은 인스턴스를 식별하는 데 꼭 필요한 속성만 사용해야 한다는 의미입니다. 불변성은 식별자 값이 가능하면 자주 바뀌지 않아야 한다는 의미입니다. 존재성은 식별자 값이 반드시 존재해야 하며 NULL이면 안 된다는 의미입니다.

주식별자는 대표로 선택된 식별자입니다. 보조식별자는 유일하게 식별할 수 있지만 대표 식별자로 선택되지 않은 식별자입니다. 내부식별자는 엔터티 내부에서 생성되는 식별자이고, 외부식별자는 다른 엔터티와의 관계를 통해 가져온 식별자입니다.',
    2,
    2,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-05';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'EXAMPLE',
    '예시로 이해하기',
    '회원 엔터티를 예로 들어보겠습니다.

회원 엔터티에는 회원ID, 이메일, 이름, 전화번호 같은 속성이 있을 수 있습니다. 이 중 회원ID가 각 회원을 유일하게 구분하는 대표 값이라면 회원ID는 주식별자가 될 수 있습니다.

이메일도 중복을 허용하지 않는다면 회원을 구분할 수 있습니다. 하지만 이메일은 사용자가 변경할 가능성이 있기 때문에 주식별자보다는 보조식별자로 두는 것이 적절할 수 있습니다.

주문 엔터티에서는 주문번호가 주문을 구분하는 주식별자가 될 수 있습니다. 주문 테이블에 회원ID가 있다면 이 회원ID는 회원 엔터티를 참조하는 외부식별자입니다.

주문상세 엔터티를 생각해보면, 주문번호와 상품번호를 함께 사용해서 하나의 주문상세를 구분할 수도 있습니다. 이런 경우 여러 속성을 조합해서 식별하므로 복합식별자라고 볼 수 있습니다.',
    3,
    3,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-05';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'CONFUSION_NOTE',
    '식별자 분류 헷갈리지 않기',
    '식별자에서 자주 헷갈리는 부분은 주식별자, 보조식별자, 내부식별자, 외부식별자입니다.

주식별자는 엔터티의 대표 식별자입니다. 실제 테이블에서는 기본키로 구현되는 경우가 많습니다.

보조식별자는 인스턴스를 유일하게 구분할 수는 있지만 대표 식별자로 선택되지 않은 식별자입니다. 예를 들어 회원ID를 주식별자로 선택했다면, 중복되지 않는 이메일은 보조식별자가 될 수 있습니다.

내부식별자는 자기 엔터티 안에서 만들어지는 식별자입니다. 주문 엔터티의 주문번호처럼 해당 엔터티 자체에서 생성되는 값입니다.

외부식별자는 다른 엔터티와의 관계를 통해 가져온 식별자입니다. 주문 엔터티의 회원ID는 회원 엔터티를 참조하는 값이므로 외부식별자로 볼 수 있습니다.

식별관계와 비식별관계도 함께 헷갈리기 쉽습니다. 부모 엔터티의 식별자가 자식 엔터티의 주식별자에 포함되면 식별관계이고, 자식의 일반 속성으로만 존재하면 비식별관계입니다.',
    4,
    4,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-05';


INSERT INTO learning_content_body (
    learning_content_id,
    body_type,
    title,
    body_text,
    display_order,
    rag_chunk_order,
    is_active
)
SELECT
    lc.id,
    'EXAM_POINT',
    'SQLD 출제 포인트',
    '식별자 파트에서는 식별자의 특징과 분류가 자주 출제됩니다.

식별자의 특징은 유일성, 최소성, 불변성, 존재성입니다. 유일성은 각 인스턴스를 유일하게 구분할 수 있어야 한다는 의미입니다. 최소성은 불필요한 속성을 포함하지 않아야 한다는 의미입니다. 불변성은 값이 자주 변경되지 않아야 한다는 의미입니다. 존재성은 반드시 값이 존재해야 한다는 의미입니다.

주식별자는 대표로 선택된 식별자이고, 보조식별자는 유일하게 식별 가능하지만 대표로 선택되지 않은 식별자입니다. 내부식별자는 엔터티 내부에서 생성된 식별자이고, 외부식별자는 다른 엔터티와의 관계로 가져온 식별자입니다.

단일식별자는 하나의 속성으로 식별하고, 복합식별자는 둘 이상의 속성을 조합해서 식별합니다. 부모 식별자가 자식 주식별자에 포함되면 식별관계이고, 부모 식별자가 자식 일반 속성으로만 존재하면 비식별관계입니다.

시험에서는 이메일은 주식별자로 적절한가, 주문번호는 내부식별자인가, 부모 식별자가 자식의 기본키에 포함되는 관계는 무엇인가처럼 개념 구분 문제가 자주 나옵니다.',
    5,
    5,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-MODELING-01-05';




START TRANSACTION;

-- =========================================================
-- problem 25개 INSERT
-- =========================================================

INSERT INTO problem (
    exam_id,
    exam_version_id,
    content_hash,
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
SELECT
    1 AS exam_id,
    esn.exam_version_id,
    q.content_hash,
    esn.id AS exam_scope_node_id,
    NULL AS ai_generated_problem_id,
    'TEXT' AS expression_type,
    'BASIC' AS difficulty,
    JSON_OBJECT('questionText', q.question_text) AS question_content_json,
    JSON_OBJECT('explanationText', q.explanation_text) AS explanation_json,
    'SINGLE_CHOICE' AS answer_type,
    JSON_OBJECT('correctChoiceNo', q.correct_choice_no) AS answer_json,
    'FOUR_CHOICE' AS choice_type,
    'SEED' AS source_type,
    TRUE AS is_active
FROM (
         SELECT 'SQLD-MODELING-01-01' AS node_code, SHA2('SQLD-MODELING-01-01-CONCEPT-Q01', 256) AS content_hash,
                '데이터 모델링에 대한 설명으로 가장 적절한 것은?' AS question_text,
                '데이터 모델링은 현실 세계의 업무 데이터를 분석하고, 정보시스템에서 관리할 수 있도록 엔터티, 속성, 관계 등으로 구조화하는 과정입니다.' AS explanation_text,
                2 AS correct_choice_no
         UNION ALL
         SELECT 'SQLD-MODELING-01-01', SHA2('SQLD-MODELING-01-01-CONCEPT-Q02', 256),
                '모델링의 대표적인 특징으로 적절하지 않은 것은?',
                '모델링의 대표적인 특징은 추상화, 단순화, 명확화입니다. 암호화는 데이터 보안과 관련된 개념이지 모델링의 대표 특징은 아닙니다.',
                4
         UNION ALL
         SELECT 'SQLD-MODELING-01-01', SHA2('SQLD-MODELING-01-01-CONCEPT-Q03', 256),
                '개념적 데이터 모델링에 대한 설명으로 가장 적절한 것은?',
                '개념적 데이터 모델링은 업무 중심으로 전체 데이터 구조를 파악하는 단계입니다. 테이블, 컬럼 타입, 인덱스 등은 물리적 모델링과 더 관련이 있습니다.',
                2
         UNION ALL
         SELECT 'SQLD-MODELING-01-01', SHA2('SQLD-MODELING-01-01-CONCEPT-Q04', 256),
                '데이터 모델링 시 유의해야 할 점으로 적절하지 않은 것은?',
                '모든 데이터를 하나의 테이블에 저장하면 중복, 비일관성, 변경 어려움이 발생할 수 있습니다. 데이터 모델링에서는 중복, 비유연성, 비일관성을 주의해야 합니다.',
                4
         UNION ALL
         SELECT 'SQLD-MODELING-01-01', SHA2('SQLD-MODELING-01-01-CONCEPT-Q05', 256),
                '논리적 데이터 모델링 단계에서 주로 다루는 내용으로 가장 적절한 것은?',
                '논리적 모델링은 DBMS에 종속되기 전 단계에서 엔터티, 속성, 관계, 식별자 등을 구체적으로 정의하는 단계입니다.',
                1

         UNION ALL
         SELECT 'SQLD-MODELING-01-02', SHA2('SQLD-MODELING-01-02-CONCEPT-Q01', 256),
                '엔터티에 대한 설명으로 가장 적절한 것은?',
                '엔터티는 업무적으로 관리할 필요가 있는 데이터의 대상입니다. 회원, 상품, 주문, 학생, 과목 등이 엔터티가 될 수 있습니다.',
                1
         UNION ALL
         SELECT 'SQLD-MODELING-01-02', SHA2('SQLD-MODELING-01-02-CONCEPT-Q02', 256),
                '엔터티와 인스턴스의 관계에 대한 설명으로 옳은 것은?',
                '엔터티는 같은 성격의 데이터 집합이고, 인스턴스는 그 엔터티에 속하는 실제 하나의 데이터입니다. 학생은 엔터티, 김철수 학생은 인스턴스입니다.',
                2
         UNION ALL
         SELECT 'SQLD-MODELING-01-02', SHA2('SQLD-MODELING-01-02-CONCEPT-Q03', 256),
                '엔터티의 특징으로 적절하지 않은 것은?',
                '엔터티는 물리적인 형태가 있는 유형 엔터티뿐 아니라, 개념 엔터티나 사건 엔터티처럼 물리적 형태가 없는 대상도 포함할 수 있습니다.',
                4
         UNION ALL
         SELECT 'SQLD-MODELING-01-02', SHA2('SQLD-MODELING-01-02-CONCEPT-Q04', 256),
                '다음 중 행위 엔터티에 가장 가까운 것은?',
                '주문상품은 주문과 상품 사이에서 발생하는 상세 구매 내역으로 볼 수 있으므로 행위 엔터티에 가깝습니다.',
                3
         UNION ALL
         SELECT 'SQLD-MODELING-01-02', SHA2('SQLD-MODELING-01-02-CONCEPT-Q05', 256),
                '기본 엔터티에 대한 설명으로 가장 적절한 것은?',
                '기본 엔터티는 업무의 기반이 되는 독립적인 엔터티입니다. 회원, 상품, 부서, 직원 등이 기본 엔터티가 될 수 있습니다.',
                2

         UNION ALL
         SELECT 'SQLD-MODELING-01-03', SHA2('SQLD-MODELING-01-03-CONCEPT-Q01', 256),
                '속성에 대한 설명으로 가장 적절한 것은?',
                '속성은 엔터티를 설명하는 구체적인 정보 항목입니다. 예를 들어 회원 엔터티의 이름, 이메일, 가입일자 등이 속성입니다.',
                1
         UNION ALL
         SELECT 'SQLD-MODELING-01-03', SHA2('SQLD-MODELING-01-03-CONCEPT-Q02', 256),
                '다음 중 파생속성에 가장 가까운 것은?',
                '나이는 생년월일을 기준으로 계산할 수 있으므로 파생속성으로 볼 수 있습니다.',
                3
         UNION ALL
         SELECT 'SQLD-MODELING-01-03', SHA2('SQLD-MODELING-01-03-CONCEPT-Q03', 256),
                '설계속성에 대한 설명으로 가장 적절한 것은?',
                '설계속성은 시스템에서 식별이나 관리를 위해 설계 과정에서 만든 속성입니다. 회원번호, 주문번호 등이 예시가 될 수 있습니다.',
                2
         UNION ALL
         SELECT 'SQLD-MODELING-01-03', SHA2('SQLD-MODELING-01-03-CONCEPT-Q04', 256),
                '속성 설계 시 바람직한 설명은?',
                '하나의 속성에 여러 의미가 섞이면 검색, 수정, 검증이 어려워질 수 있으므로 하나의 속성은 하나의 의미를 가지도록 설계하는 것이 좋습니다.',
                3
         UNION ALL
         SELECT 'SQLD-MODELING-01-03', SHA2('SQLD-MODELING-01-03-CONCEPT-Q05', 256),
                '기본속성에 해당하는 예시로 가장 적절한 것은?',
                '회원의 이름은 업무에서 자연스럽게 발생하는 속성이므로 기본속성에 가깝습니다. 회원번호는 설계속성, 나이와 총주문금액은 파생속성으로 볼 수 있습니다.',
                1

         UNION ALL
         SELECT 'SQLD-MODELING-01-04', SHA2('SQLD-MODELING-01-04-CONCEPT-Q01', 256),
                '관계에 대한 설명으로 가장 적절한 것은?',
                '관계는 엔터티와 엔터티 사이의 업무적 연관성을 의미합니다. 예를 들어 회원과 주문 사이에는 회원이 주문을 한다는 관계가 있습니다.',
                1
         UNION ALL
         SELECT 'SQLD-MODELING-01-04', SHA2('SQLD-MODELING-01-04-CONCEPT-Q02', 256),
                '회원과 주문의 관계를 설명한 것으로 가장 적절한 것은?',
                '일반적으로 한 명의 회원은 여러 주문을 할 수 있고, 하나의 주문은 한 명의 회원에게 속하므로 회원과 주문은 1:N 관계로 볼 수 있습니다.',
                1
         UNION ALL
         SELECT 'SQLD-MODELING-01-04', SHA2('SQLD-MODELING-01-04-CONCEPT-Q03', 256),
                'M:N 관계에 대한 설명으로 적절한 것은?',
                'M:N 관계는 실제 테이블 설계에서 중간 엔터티를 통해 해소하는 경우가 많습니다. 예를 들어 주문과 상품 사이에 주문상품 엔터티를 둘 수 있습니다.',
                2
         UNION ALL
         SELECT 'SQLD-MODELING-01-04', SHA2('SQLD-MODELING-01-04-CONCEPT-Q04', 256),
                '관계 선택성에 대한 설명으로 가장 적절한 것은?',
                '관계 선택성은 특정 엔터티가 상대 엔터티와 반드시 연결되어야 하는지, 없어도 되는지를 나타냅니다.',
                2
         UNION ALL
         SELECT 'SQLD-MODELING-01-04', SHA2('SQLD-MODELING-01-04-CONCEPT-Q05', 256),
                '식별관계에 대한 설명으로 가장 적절한 것은?',
                '식별관계는 부모 엔터티의 식별자가 자식 엔터티의 주식별자에 포함되는 관계입니다.',
                1

         UNION ALL
         SELECT 'SQLD-MODELING-01-05', SHA2('SQLD-MODELING-01-05-CONCEPT-Q01', 256),
                '식별자에 대한 설명으로 가장 적절한 것은?',
                '식별자는 엔터티에 속한 각각의 인스턴스를 유일하게 구분할 수 있는 속성 또는 속성의 집합입니다.',
                1
         UNION ALL
         SELECT 'SQLD-MODELING-01-05', SHA2('SQLD-MODELING-01-05-CONCEPT-Q02', 256),
                '식별자의 특징으로 적절하지 않은 것은?',
                '식별자의 대표적인 특징은 유일성, 최소성, 불변성, 존재성입니다. 중복성은 식별자의 특징으로 적절하지 않습니다.',
                4
         UNION ALL
         SELECT 'SQLD-MODELING-01-05', SHA2('SQLD-MODELING-01-05-CONCEPT-Q03', 256),
                '주식별자에 대한 설명으로 가장 적절한 것은?',
                '주식별자는 엔터티의 인스턴스를 대표적으로 구분하기 위해 선택된 식별자입니다. 실제 테이블에서는 기본키로 구현되는 경우가 많습니다.',
                1
         UNION ALL
         SELECT 'SQLD-MODELING-01-05', SHA2('SQLD-MODELING-01-05-CONCEPT-Q04', 256),
                '외부식별자에 대한 설명으로 가장 적절한 것은?',
                '외부식별자는 다른 엔터티와의 관계를 통해 가져온 식별자입니다. 예를 들어 주문 엔터티의 회원ID는 회원 엔터티를 참조하는 외부식별자로 볼 수 있습니다.',
                2
         UNION ALL
         SELECT 'SQLD-MODELING-01-05', SHA2('SQLD-MODELING-01-05-CONCEPT-Q05', 256),
                '복합식별자에 대한 설명으로 가장 적절한 것은?',
                '복합식별자는 둘 이상의 속성을 조합하여 하나의 인스턴스를 식별하는 식별자입니다. 예를 들어 주문번호와 상품번호를 함께 사용해 주문상세를 식별할 수 있습니다.',
                2
     ) q
         JOIN exam_scope_node esn ON esn.code = q.node_code;

-- =========================================================
-- problem_choice 100개 INSERT
-- =========================================================

INSERT INTO problem_choice (
    problem_id,
    choice_no,
    choice_content_json
)
SELECT
    p.id,
    c.choice_no,
    JSON_OBJECT('choiceText', c.choice_text) AS choice_content_json
FROM (
         SELECT SHA2('SQLD-MODELING-01-01-CONCEPT-Q01', 256) AS content_hash, 1 AS choice_no, '화면 디자인을 중심으로 사용자 인터페이스를 설계하는 작업이다.' AS choice_text
         UNION ALL SELECT SHA2('SQLD-MODELING-01-01-CONCEPT-Q01', 256), 2, '현실 세계의 업무 데이터를 정보시스템에서 관리할 수 있도록 구조화하는 과정이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-01-CONCEPT-Q01', 256), 3, 'SQL 문장을 작성하여 데이터를 조회하는 작업이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-01-CONCEPT-Q01', 256), 4, '서버의 네트워크 설정을 최적화하는 작업이다.'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-01-CONCEPT-Q02', 256), 1, '추상화'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-01-CONCEPT-Q02', 256), 2, '단순화'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-01-CONCEPT-Q02', 256), 3, '명확화'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-01-CONCEPT-Q02', 256), 4, '암호화'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-01-CONCEPT-Q03', 256), 1, '실제 DBMS에 맞게 테이블과 인덱스를 설계하는 단계이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-01-CONCEPT-Q03', 256), 2, '업무 전체의 주요 데이터 구조를 큰 관점에서 파악하는 단계이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-01-CONCEPT-Q03', 256), 3, '컬럼의 데이터 타입과 저장 공간을 결정하는 단계이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-01-CONCEPT-Q03', 256), 4, 'SQL 튜닝을 통해 실행 계획을 개선하는 단계이다.'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-01-CONCEPT-Q04', 256), 1, '데이터 중복을 최소화한다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-01-CONCEPT-Q04', 256), 2, '업무 변화에 유연하게 대응할 수 있도록 설계한다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-01-CONCEPT-Q04', 256), 3, '같은 의미의 데이터가 일관되게 관리되도록 한다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-01-CONCEPT-Q04', 256), 4, '조회 속도를 위해 모든 데이터를 하나의 테이블에 저장한다.'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-01-CONCEPT-Q05', 256), 1, '엔터티, 속성, 관계, 식별자를 구체적으로 정의한다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-01-CONCEPT-Q05', 256), 2, '서버의 CPU와 메모리 사용량을 측정한다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-01-CONCEPT-Q05', 256), 3, '사용자 화면의 색상과 레이아웃을 정한다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-01-CONCEPT-Q05', 256), 4, 'DBMS별 저장 공간과 인덱스 구조를 결정한다.'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-02-CONCEPT-Q01', 256), 1, '엔터티는 업무에서 관리해야 하는 데이터의 대상이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-02-CONCEPT-Q01', 256), 2, '엔터티는 반드시 하나의 인스턴스만 가져야 한다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-02-CONCEPT-Q01', 256), 3, '엔터티는 SQL 실행 결과만을 의미한다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-02-CONCEPT-Q01', 256), 4, '엔터티는 화면에 표시되는 버튼을 의미한다.'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-02-CONCEPT-Q02', 256), 1, '학생은 인스턴스이고, 김철수 학생은 엔터티이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-02-CONCEPT-Q02', 256), 2, '학생은 엔터티이고, 김철수 학생은 인스턴스이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-02-CONCEPT-Q02', 256), 3, '엔터티와 인스턴스는 항상 같은 의미이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-02-CONCEPT-Q02', 256), 4, '인스턴스는 엔터티가 가질 수 없는 속성이다.'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-02-CONCEPT-Q03', 256), 1, '업무에서 필요로 하는 정보여야 한다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-02-CONCEPT-Q03', 256), 2, '속성을 가질 수 있다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-02-CONCEPT-Q03', 256), 3, '다른 엔터티와 관계를 가질 수 있다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-02-CONCEPT-Q03', 256), 4, '항상 물리적인 형태가 있어야 한다.'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-02-CONCEPT-Q04', 256), 1, '회원'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-02-CONCEPT-Q04', 256), 2, '상품'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-02-CONCEPT-Q04', 256), 3, '주문상품'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-02-CONCEPT-Q04', 256), 4, '상품분류'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-02-CONCEPT-Q05', 256), 1, '다른 엔터티 사이의 행위로 인해 발생하는 엔터티이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-02-CONCEPT-Q05', 256), 2, '업무의 기반이 되는 독립적인 엔터티이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-02-CONCEPT-Q05', 256), 3, '반드시 두 개 이상의 부모 엔터티를 가져야 한다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-02-CONCEPT-Q05', 256), 4, '항상 집계 결과로만 생성되는 엔터티이다.'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-03-CONCEPT-Q01', 256), 1, '엔터티가 가지는 구체적인 정보 항목이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-03-CONCEPT-Q01', 256), 2, '엔터티와 엔터티 사이의 연결을 의미한다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-03-CONCEPT-Q01', 256), 3, '데이터베이스 서버의 물리적 저장 장치를 의미한다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-03-CONCEPT-Q01', 256), 4, 'SQL 실행 순서를 의미한다.'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-03-CONCEPT-Q02', 256), 1, '회원 이름'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-03-CONCEPT-Q02', 256), 2, '생년월일'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-03-CONCEPT-Q02', 256), 3, '나이'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-03-CONCEPT-Q02', 256), 4, '이메일'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-03-CONCEPT-Q03', 256), 1, '업무에서 자연스럽게 발생하는 원래의 속성이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-03-CONCEPT-Q03', 256), 2, '시스템 설계 과정에서 필요에 의해 만든 속성이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-03-CONCEPT-Q03', 256), 3, '다른 속성으로부터 계산되는 속성이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-03-CONCEPT-Q03', 256), 4, '엔터티 간의 관계를 표현하는 선이다.'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-03-CONCEPT-Q04', 256), 1, '하나의 속성에는 여러 의미를 최대한 많이 포함하는 것이 좋다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-03-CONCEPT-Q04', 256), 2, '속성은 업무적으로 의미가 없어도 반드시 많이 만드는 것이 좋다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-03-CONCEPT-Q04', 256), 3, '하나의 속성은 가능하면 하나의 의미를 가지도록 설계하는 것이 좋다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-03-CONCEPT-Q04', 256), 4, '속성은 엔터티와 무관하게 독립적으로만 존재해야 한다.'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-03-CONCEPT-Q05', 256), 1, '회원의 이름'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-03-CONCEPT-Q05', 256), 2, '회원번호'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-03-CONCEPT-Q05', 256), 3, '나이'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-03-CONCEPT-Q05', 256), 4, '총주문금액'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-04-CONCEPT-Q01', 256), 1, '엔터티와 엔터티 사이의 업무적 연관성을 의미한다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-04-CONCEPT-Q01', 256), 2, '하나의 엔터티가 가지는 세부 정보 항목이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-04-CONCEPT-Q01', 256), 3, '인스턴스를 유일하게 구분하는 속성이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-04-CONCEPT-Q01', 256), 4, '물리적 저장 공간의 크기를 의미한다.'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-04-CONCEPT-Q02', 256), 1, '한 명의 회원은 여러 주문을 할 수 있으므로 1:N 관계로 볼 수 있다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-04-CONCEPT-Q02', 256), 2, '한 명의 회원은 반드시 하나의 주문만 할 수 있으므로 1:1 관계이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-04-CONCEPT-Q02', 256), 3, '회원과 주문은 업무적으로 아무 관계가 없다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-04-CONCEPT-Q02', 256), 4, '주문은 항상 여러 명의 회원에게 동시에 속한다.'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-04-CONCEPT-Q03', 256), 1, '항상 하나의 테이블로만 구현해야 한다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-04-CONCEPT-Q03', 256), 2, '실제 설계에서는 중간 엔터티를 두어 해소하는 경우가 많다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-04-CONCEPT-Q03', 256), 3, '두 엔터티가 서로 전혀 연결되지 않는 관계이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-04-CONCEPT-Q03', 256), 4, '반드시 1:1 관계로만 변환해야 한다.'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-04-CONCEPT-Q04', 256), 1, '두 엔터티가 몇 개씩 연결될 수 있는지를 의미한다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-04-CONCEPT-Q04', 256), 2, '관계가 필수인지 선택인지를 의미한다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-04-CONCEPT-Q04', 256), 3, '엔터티의 속성 개수를 의미한다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-04-CONCEPT-Q04', 256), 4, '식별자의 최소성을 의미한다.'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-04-CONCEPT-Q05', 256), 1, '부모 엔터티의 식별자가 자식 엔터티의 주식별자에 포함되는 관계이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-04-CONCEPT-Q05', 256), 2, '부모 엔터티와 자식 엔터티가 아무런 관련이 없는 관계이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-04-CONCEPT-Q05', 256), 3, '자식 엔터티가 부모 엔터티의 일반 속성을 절대 가질 수 없는 관계이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-04-CONCEPT-Q05', 256), 4, '두 엔터티가 모두 속성을 가지지 않는 관계이다.'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-05-CONCEPT-Q01', 256), 1, '엔터티의 인스턴스를 유일하게 구분할 수 있는 속성 또는 속성의 집합이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-05-CONCEPT-Q01', 256), 2, '엔터티와 엔터티 사이의 업무적 연결이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-05-CONCEPT-Q01', 256), 3, '엔터티가 가지는 모든 속성의 설명문이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-05-CONCEPT-Q01', 256), 4, '데이터를 화면에 출력하는 방식이다.'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-05-CONCEPT-Q02', 256), 1, '유일성'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-05-CONCEPT-Q02', 256), 2, '최소성'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-05-CONCEPT-Q02', 256), 3, '불변성'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-05-CONCEPT-Q02', 256), 4, '중복성'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-05-CONCEPT-Q03', 256), 1, '엔터티의 대표 식별자로 선택된 식별자이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-05-CONCEPT-Q03', 256), 2, '반드시 NULL 값을 가져야 하는 속성이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-05-CONCEPT-Q03', 256), 3, '항상 다른 엔터티에서 가져온 속성이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-05-CONCEPT-Q03', 256), 4, '중복을 허용해야 하는 속성이다.'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-05-CONCEPT-Q04', 256), 1, '엔터티 내부에서 자체적으로 생성된 식별자이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-05-CONCEPT-Q04', 256), 2, '다른 엔터티와의 관계를 통해 가져온 식별자이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-05-CONCEPT-Q04', 256), 3, '반드시 파생속성으로만 구성된 식별자이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-05-CONCEPT-Q04', 256), 4, '항상 중복을 허용하는 식별자이다.'

         UNION ALL SELECT SHA2('SQLD-MODELING-01-05-CONCEPT-Q05', 256), 1, '하나의 속성만으로 인스턴스를 식별하는 식별자이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-05-CONCEPT-Q05', 256), 2, '둘 이상의 속성을 조합하여 인스턴스를 식별하는 식별자이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-05-CONCEPT-Q05', 256), 3, '식별자 값이 반드시 중복되어야 하는 식별자이다.'
         UNION ALL SELECT SHA2('SQLD-MODELING-01-05-CONCEPT-Q05', 256), 4, '업무적으로 의미가 없는 임시 값만을 의미한다.'
     ) c
         JOIN problem p ON p.content_hash = c.content_hash
WHERE p.source_type = 'SEED'
  AND p.is_active = TRUE;

COMMIT;