import { useEffect, useMemo, useState } from "react";
import "./SyllabusTree.css";

function SyllabusTree({
                          nodes = [],
                          selectedNodeId,
                          onSelectNode,
                          topicProgresses = [],
                      }) {
    const [openNodeIds, setOpenNodeIds] = useState([]);

    const progressMap = useMemo(() => {
        const map = new Map();

        topicProgresses.forEach((progress) => {
            map.set(progress.examScopeNodeId, progress.status);
        });

        return map;
    }, [topicProgresses]);

    useEffect(() => {
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
        setOpenNodeIds(openedIds);
    }, [nodes]);

    const toggleNode = (nodeId) => {
        setOpenNodeIds((prev) =>
            prev.includes(nodeId)
                ? prev.filter((id) => id !== nodeId)
                : [...prev, nodeId]
        );
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
                                    className={
                                        isSelected
                                            ? "syllabus-topic-button selected"
                                            : "syllabus-topic-button"
                                    }
                                    onClick={() => onSelectNode(node)}
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