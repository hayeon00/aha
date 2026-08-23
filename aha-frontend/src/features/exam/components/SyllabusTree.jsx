import { useMemo, useState } from "react";
import "./SyllabusTree.css";

const collectDefaultOpenNodeIds = (nodes = []) => {
    const openedIds = [];

    const collectOpenIds = (items) => {
        items.forEach((node) => {
            const children = node.children || node.childNodes || [];

            if (children.length > 0) {
                openedIds.push(node.id);
                collectOpenIds(children);
            }
        });
    };

    collectOpenIds(nodes);
    return openedIds;
};

function SyllabusTree({
                          nodes = [],
                          selectedNodeId,
                          onSelectNode,
                          topicProgresses = [],
                          selectable = true,
                      }) {
    const [closedNodeIds, setClosedNodeIds] = useState([]);

    const progressMap = useMemo(() => {
        const map = new Map();

        topicProgresses.forEach((progress) => {
            map.set(progress.examScopeNodeId, progress.status);
        });

        return map;
    }, [topicProgresses]);

    const defaultOpenNodeIds = useMemo(() => {
        return collectDefaultOpenNodeIds(nodes);
    }, [nodes]);

    const openNodeIds = useMemo(() => {
        const closedNodeIdSet = new Set(closedNodeIds);

        return defaultOpenNodeIds.filter(
            (nodeId) => !closedNodeIdSet.has(nodeId)
        );
    }, [defaultOpenNodeIds, closedNodeIds]);

    const toggleNode = (nodeId) => {
        setClosedNodeIds((prev) => {
            if (openNodeIds.includes(nodeId)) {
                return prev.includes(nodeId) ? prev : [...prev, nodeId];
            }

            return prev.filter((id) => id !== nodeId);
        });
    };

    const getProgressStatus = (nodeId) => {
        return progressMap.get(nodeId) || "NOT_STARTED";
    };

    const getNodeType = (node) => {
        return node.nodeType || node.type || node.scopeNodeType || node.level;
    };

    const isSectionNode = (node) => {
        return getNodeType(node) === "SECTION";
    };

    const isTopicNode = (node) => {
        return getNodeType(node) === "TOPIC";
    };

    const renderNodes = (items, depth = 0) => {
        return (
            <ul className={depth === 0 ? "syllabus-tree" : "syllabus-subtree"}>
                {items.map((node) => {
                    const children = node.children || node.childNodes || [];
                    const hasChildren = children.length > 0;
                    const isOpen = openNodeIds.includes(node.id);
                    const isSelected = selectedNodeId === node.id;
                    const status = getProgressStatus(node.id);
                    const isCompleted = status === "COMPLETED";

                    if (isSectionNode(node)) {
                        return (
                            <li key={node.id} className="syllabus-group">
                                <button
                                    type="button"
                                    className={
                                        isOpen
                                            ? "syllabus-section-button open"
                                            : "syllabus-section-button"
                                    }
                                    onClick={() => toggleNode(node.id)}
                                >
                                    <span className="section-left">
                                        <span className="section-mark" />

                                        <span className="section-title">
                                            {node.title}
                                        </span>
                                    </span>

                                    <span className="section-arrow" />
                                </button>

                                {hasChildren &&
                                    isOpen &&
                                    renderNodes(children, depth + 1)}
                            </li>
                        );
                    }

                    if (isTopicNode(node)) {
                        return (
                            <li key={node.id} className="syllabus-topic-item">
                                <button
                                    type="button"
                                    className={[
                                        "syllabus-topic-button",
                                        isSelected ? "selected" : "",
                                        !selectable ? "preview-only" : "",
                                    ].filter(Boolean).join(" ")}
                                    onClick={() => selectable && onSelectNode?.(node)}
                                    aria-disabled={!selectable}
                                >
                                    <span
                                        className={[
                                            "topic-dot",
                                            isCompleted ? "completed" : "",
                                            isSelected ? "selected" : "",
                                        ]
                                            .filter(Boolean)
                                            .join(" ")}
                                    />

                                    <span className="topic-title">
                                        {node.title}
                                    </span>

                                    {isCompleted && (
                                        <span className="topic-status completed">
                                            완료
                                        </span>
                                    )}
                                </button>
                            </li>
                        );
                    }

                    return null;
                })}
            </ul>
        );
    };

    if (!nodes || nodes.length === 0) {
        return (
            <div className="syllabus-empty">
                <p>표시할 목차가 없습니다.</p>
            </div>
        );
    }

    return renderNodes(nodes);
}

export default SyllabusTree;
