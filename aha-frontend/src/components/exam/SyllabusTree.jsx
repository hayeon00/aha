function SyllabusTree({ nodes, onSelectNode, selectedNodeId }) {
    if (!nodes || nodes.length === 0) {
        return <p className="empty-text">조회된 목차가 없습니다.</p>;
    }

    return (
        <ul className="syllabus-tree">
            {nodes.map((node) => (
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
                    </button>

                    {node.children && node.children.length > 0 && (
                        <SyllabusTree
                            nodes={node.children}
                            onSelectNode={onSelectNode}
                            selectedNodeId={selectedNodeId}
                        />
                    )}
                </li>
            ))}
        </ul>
    );
}

export default SyllabusTree;