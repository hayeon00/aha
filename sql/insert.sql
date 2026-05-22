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
    'GROUP BY, HAVING 절',
    'GROUP BY는 행을 특정 기준으로 그룹화하고, HAVING은 그룹화된 결과에 조건을 적용하는 절입니다. SQLD에서는 WHERE와 HAVING의 실행 시점 차이, 집계 함수 사용 위치, GROUP BY에 포함되지 않은 컬럼 사용 가능 여부가 자주 출제됩니다.',
    TRUE,
    TRUE,
    1
FROM exam_scope_node esn
WHERE esn.code = 'SQLD-SQL-01-05';


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
    'GROUP BY와 HAVING의 기본 개념',
    'GROUP BY는 여러 행을 특정 컬럼 기준으로 묶어 하나의 그룹으로 만드는 SQL 절입니다. 예를 들어 부서별 평균 급여, 상품별 주문 수, 사용자별 결제 금액처럼 같은 기준을 가진 데이터를 묶어서 집계할 때 사용합니다.

HAVING은 GROUP BY로 만들어진 그룹 결과에 조건을 적용할 때 사용합니다. WHERE가 개별 행을 대상으로 조건을 거는 절이라면, HAVING은 그룹화가 끝난 뒤 만들어진 집계 결과를 대상으로 조건을 거는 절입니다.

예를 들어 “부서별 평균 급여가 3000 이상인 부서만 조회하라”는 조건은 개별 직원 행에 대한 조건이 아니라, 부서별 평균이라는 집계 결과에 대한 조건입니다. 따라서 이 경우에는 WHERE가 아니라 HAVING을 사용해야 합니다.',
    1,
    1,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-SQL-01-05';


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
    'GROUP BY, HAVING 절에서 가장 중요한 핵심은 “조건이 적용되는 시점”입니다.

1. WHERE는 GROUP BY보다 먼저 실행됩니다.
2. WHERE는 개별 행을 필터링합니다.
3. GROUP BY는 WHERE를 통과한 행들을 그룹화합니다.
4. HAVING은 GROUP BY 이후의 그룹 결과를 필터링합니다.
5. 집계 함수 조건은 일반적으로 HAVING에서 사용합니다.

SQL 실행 흐름을 단순화하면 다음과 같습니다.

FROM → WHERE → GROUP BY → HAVING → SELECT → ORDER BY

따라서 WHERE 절에서는 아직 그룹화가 이루어지기 전이므로 AVG, COUNT, SUM 같은 집계 결과에 대한 조건을 직접 판단하기 어렵습니다. 반면 HAVING은 그룹화 이후 실행되기 때문에 COUNT(*) > 1, AVG(score) >= 60 같은 집계 조건을 사용할 수 있습니다.',
    2,
    2,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-SQL-01-05';


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
    '예를 들어 employee 테이블에 직원 정보가 있고, 부서별 평균 급여를 조회한다고 가정합니다.

SELECT department_id, AVG(salary)
FROM employee
GROUP BY department_id;

이 SQL은 department_id를 기준으로 직원을 그룹화한 뒤, 각 부서의 평균 급여를 계산합니다.

여기서 평균 급여가 3000 이상인 부서만 조회하려면 다음처럼 작성합니다.

SELECT department_id, AVG(salary)
FROM employee
GROUP BY department_id
HAVING AVG(salary) >= 3000;

이 조건은 개별 직원의 salary가 3000 이상인지 보는 것이 아니라, 부서별 평균 급여가 3000 이상인지를 보는 것입니다. 그래서 WHERE가 아니라 HAVING을 사용합니다.

반대로 급여가 1000 이상인 직원만 대상으로 부서별 평균을 구하고 싶다면 WHERE를 사용합니다.

SELECT department_id, AVG(salary)
FROM employee
WHERE salary >= 1000
GROUP BY department_id;

이 경우 WHERE가 먼저 실행되어 salary가 1000 이상인 직원만 남고, 그 결과를 기준으로 GROUP BY가 수행됩니다.',
    3,
    3,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-SQL-01-05';


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
    'WHERE와 HAVING 헷갈리지 않기',
    'WHERE와 HAVING은 둘 다 조건을 거는 절이지만, 조건을 적용하는 대상이 다릅니다.

WHERE는 그룹화 전의 “개별 행”을 필터링합니다.
HAVING은 그룹화 후의 “그룹 결과”를 필터링합니다.

헷갈릴 때는 조건에 집계 함수가 있는지 확인하면 좋습니다.

예를 들어 salary >= 3000은 개별 행의 급여를 비교하는 조건이므로 WHERE에 사용할 수 있습니다.
반면 AVG(salary) >= 3000은 여러 행을 그룹화한 뒤 계산한 평균 급여에 대한 조건이므로 HAVING에 사용해야 합니다.

자주 나오는 함정은 다음과 같습니다.

잘못된 예:
SELECT department_id, AVG(salary)
FROM employee
WHERE AVG(salary) >= 3000
GROUP BY department_id;

이 SQL은 WHERE 절에서 집계 결과를 조건으로 사용하려고 했기 때문에 잘못된 형태입니다.

올바른 예:
SELECT department_id, AVG(salary)
FROM employee
GROUP BY department_id
HAVING AVG(salary) >= 3000;

또 하나의 함정은 SELECT 절에 GROUP BY 기준 컬럼이 아닌 일반 컬럼을 함께 쓰는 경우입니다. GROUP BY를 사용할 때 SELECT 절에는 GROUP BY에 포함된 컬럼이나 집계 함수가 와야 합니다.',
    4,
    4,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-SQL-01-05';


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
    'SQLD 시험에서는 GROUP BY와 HAVING이 단순 문법보다 실행 순서와 사용 가능 조건 중심으로 출제되는 경우가 많습니다.

자주 출제되는 포인트는 다음과 같습니다.

1. WHERE와 HAVING의 차이
- WHERE는 그룹화 전 조건
- HAVING은 그룹화 후 조건

2. 집계 함수 조건 위치
- COUNT(*), SUM(), AVG(), MAX(), MIN() 등의 집계 결과 조건은 HAVING에서 판단하는 경우가 많습니다.

3. GROUP BY 사용 시 SELECT 절 제한
- SELECT 절에는 GROUP BY에 포함된 컬럼 또는 집계 함수가 와야 합니다.
- GROUP BY에 없는 일반 컬럼을 SELECT에 그대로 쓰면 오류가 발생할 수 있습니다.

4. 실행 순서
- FROM → WHERE → GROUP BY → HAVING → SELECT → ORDER BY 순서를 이해해야 합니다.

5. 문제 풀이 판단 기준
- 조건이 개별 행에 대한 조건이면 WHERE
- 조건이 집계 결과에 대한 조건이면 HAVING

시험에서 “부서별”, “상품별”, “사용자별”처럼 ~별이라는 표현이 나오면 GROUP BY를 먼저 의심하고, “평균이 얼마 이상”, “개수가 몇 개 이상”처럼 집계 결과에 대한 조건이 나오면 HAVING을 떠올리면 좋습니다.',
    5,
    5,
    TRUE
FROM learning_content lc
         JOIN exam_scope_node esn ON lc.exam_scope_node_id = esn.id
WHERE esn.code = 'SQLD-SQL-01-05';



