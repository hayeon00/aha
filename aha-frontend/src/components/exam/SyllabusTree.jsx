function SyllabusTree({
                          nodes,
                          onSelectNode,
                          selectedNodeId,
                          topicProgresses = [],
                      }) {
    if (!nodes || nodes.length === 0) {
        return <p className="empty-text">조회된 목차가 없습니다.</p>;
    }

    const progressMap = topicProgresses.reduce((acc, item) => {
        acc[item.examScopeNodeId] = item.status;
        return acc;
    }, {});

    return (
        <ul className="syllabus-tree">
            {nodes.map((node) => {
                const progressStatus = progressMap[node.id];

                return (
                    <li key={node.id} className={`tree-item depth-${node.depth}`}>
                        <button
                            type="button"
                            className={`tree-node ${selectedNodeId === node.id ? "selected" : ""}`}
                            onClick={() => onSelectNode(node)}
                        >
                            <span className={`node-badge ${node.nodeType.toLowerCase()}`}>
                                {node.nodeType}
                            </span>

                            <span className="node-title">{node.title}</span>

                            {node.isLeaf && progressStatus && (
                                <span className={`topic-status-badge ${progressStatus.toLowerCase()}`}>
                                    {convertProgressStatus(progressStatus)}
                                </span>
                            )}
                        </button>

                        {node.children && node.children.length > 0 && (
                            <SyllabusTree
                                nodes={node.children}
                                onSelectNode={onSelectNode}
                                selectedNodeId={selectedNodeId}
                                topicProgresses={topicProgresses}
                            />
                        )}
                    </li>
                );
            })}
        </ul>
    );
}

function convertProgressStatus(status) {
    const labels = {
        NOT_STARTED: "미완료",
        IN_PROGRESS: "진행중",
        COMPLETED: "완료",
    };

    return labels[status] || status;
}

export default SyllabusTree;