import { useCallback, useEffect, useMemo, useState } from "react";
import { assignDocumentChunk, getUnassignedDocumentChunks } from "../api/documentMappingApi.js";
import { getApiData } from "../utils/apiResponseUtils.js";
import "./UnassignedChunksPanel.css";

const flattenLeafNodes = (nodes = []) => nodes.flatMap((node) =>
    node.children?.length ? flattenLeafNodes(node.children) : [node]
);

function UnassignedChunksPanel({ userExamId, scopeNodes, onAssigned }) {
    const [open, setOpen] = useState(false);
    const [chunks, setChunks] = useState([]);
    const [selectedScopes, setSelectedScopes] = useState({});
    const [assigningChunkId, setAssigningChunkId] = useState(null);
    const [errorMessage, setErrorMessage] = useState("");
    const leafScopes = useMemo(() => flattenLeafNodes(scopeNodes), [scopeNodes]);

    const fetchChunks = useCallback(async () => {
        if (!userExamId) {
            setChunks([]);
            return;
        }

        try {
            const response = await getUnassignedDocumentChunks(userExamId);
            const data = getApiData(response);
            setChunks(Array.isArray(data) ? data : []);
            setErrorMessage("");
        } catch (error) {
            console.error("미분류 문서 조회 실패:", error);
            setErrorMessage("미분류 문서를 불러오지 못했습니다.");
        }
    }, [userExamId]);

    useEffect(() => {
        queueMicrotask(fetchChunks);
    }, [fetchChunks]);

    const handleAssign = async (chunkId) => {
        const scopeNodeId = Number(selectedScopes[chunkId]);
        if (!scopeNodeId || assigningChunkId) return;

        try {
            setAssigningChunkId(chunkId);
            await assignDocumentChunk(chunkId, scopeNodeId);
            setChunks((current) => current.filter((chunk) => chunk.documentChunkId !== chunkId));
            await onAssigned?.();
        } catch (error) {
            console.error("수동 목차 지정 실패:", error);
            setErrorMessage(error.response?.data?.message || "목차를 지정하지 못했습니다.");
        } finally {
            setAssigningChunkId(null);
        }
    };

    return (
        <>
            <button className="unassigned-panel-trigger" type="button" onClick={() => setOpen(true)}>
                미분류 검토
                <span>{chunks.length}</span>
            </button>

            {open && (
                <div className="unassigned-panel-backdrop" onClick={() => setOpen(false)}>
                    <aside className="unassigned-panel" onClick={(event) => event.stopPropagation()}>
                        <header>
                            <div>
                                <p>매핑 보류함</p>
                                <h2>미분류 문서 조각</h2>
                            </div>
                            <button type="button" aria-label="닫기" onClick={() => setOpen(false)}>×</button>
                        </header>

                        {errorMessage && <p className="unassigned-panel-error">{errorMessage}</p>}

                        <div className="unassigned-panel-list">
                            {chunks.length === 0 ? (
                                <div className="unassigned-panel-empty">검토할 미분류 문서가 없습니다.</div>
                            ) : chunks.map((chunk) => (
                                <article className="unassigned-chunk-card" key={chunk.documentChunkId}>
                                    <div className="unassigned-chunk-meta">
                                        <span>{chunk.fileName}</span>
                                        {chunk.pageNo && <span>{chunk.pageNo}페이지</span>}
                                    </div>
                                    <h3>{chunk.sectionTitle || "제목 없는 문서 조각"}</h3>
                                    <p>{chunk.contentText}</p>
                                    <div className="unassigned-chunk-action">
                                        <select
                                            aria-label="목차 선택"
                                            value={selectedScopes[chunk.documentChunkId] || ""}
                                            onChange={(event) => setSelectedScopes((current) => ({
                                                ...current,
                                                [chunk.documentChunkId]: event.target.value,
                                            }))}
                                        >
                                            <option value="">목차 선택 지정</option>
                                            {leafScopes.map((scope) => (
                                                <option value={scope.id} key={scope.id}>{scope.title}</option>
                                            ))}
                                        </select>
                                        <button
                                            type="button"
                                            disabled={!selectedScopes[chunk.documentChunkId] || assigningChunkId === chunk.documentChunkId}
                                            onClick={() => handleAssign(chunk.documentChunkId)}
                                        >
                                            {assigningChunkId === chunk.documentChunkId ? "지정 중" : "지정"}
                                        </button>
                                    </div>
                                </article>
                            ))}
                        </div>
                    </aside>
                </div>
            )}
        </>
    );
}

export default UnassignedChunksPanel;
