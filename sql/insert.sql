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