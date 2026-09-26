import { useState } from "react";
import { contentText, formatCell, parseConceptBody, resultTable } from "../utils/conceptBody.js";

function SqlBlock({ sql }) {
    const [feedback, setFeedback] = useState("");
    const copy = async () => {
        try {
            await navigator.clipboard.writeText(sql);
            setFeedback("복사했습니다.");
        } catch {
            setFeedback("복사하지 못했습니다. 코드를 직접 선택해 복사해 주세요.");
        }
    };
    return <div className="concept-sql">
        <div className="concept-sql-head"><span>SQL</span><button type="button" onClick={copy} aria-label="SQL 코드 복사">복사</button></div>
        <pre><code>{sql}</code></pre>
        <span className="concept-copy-feedback" role="status">{feedback}</span>
    </div>;
}

function ResultView({ result }) {
    const table = resultTable(result);
    return <div className="concept-result">
        {table ? <div className="concept-table-scroll" tabIndex={0} role="region" aria-label="SQL 실행 결과">
            <table><caption>실행 결과 · {table.rows.length}행</caption>
                <thead><tr>{table.columns.map((column) => <th scope="col" key={column}>{column}</th>)}</tr></thead>
                <tbody>{table.rows.map((row, index) => <tr key={index}>{table.columns.map((column) => <td key={column} className={row[column] === null ? "concept-null" : undefined}>{formatCell(row[column])}</td>)}</tr>)}</tbody>
            </table>
        </div> : <><h4>실행 결과</h4>{Array.isArray(result) && !result.length ? <p>조회된 행이 없습니다.</p> : <pre>{formatCell(result)}</pre>}</>}
    </div>;
}

export default function ConceptBlocks({ body }) {
    const blocks = parseConceptBody(body);
    if (!blocks?.length) return <p className="concept-body-fallback" role="status">{blocks === null ? "내용을 표시할 수 없습니다. 데이터 형식을 확인해 주세요." : "아직 등록된 설명이 없습니다."}</p>;
    return <div className="concept-blocks">{blocks.map((block, index) => {
        if (block.type === "definition") return <section className="concept-definition" key={index}><h3>핵심 정의</h3><p>{contentText(block.text)}</p></section>;
        if (block.type === "exam_tip") return <aside className="concept-tip" key={index}><h3><span aria-hidden="true">✦</span> 시험 포인트</h3><p>{contentText(block.text)}</p></aside>;
        return <section className="concept-example" key={index}>
            <h3>예시로 이해하기</h3>
            {contentText(block.text) && <p>{contentText(block.text)}</p>}
            {contentText(block.sql) && <SqlBlock sql={contentText(block.sql)} />}
            {block.result != null && <ResultView result={block.result} />}
        </section>;
    })}</div>;
}
