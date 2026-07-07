export const getDefaultExpandedNodeIds = (nodes) => {
    const ids = [];

    const traverse = (items) => {
        items.forEach((item) => {
            ids.push(item.id);

            if (item.children?.length > 0) {
                traverse(item.children);
            }
        });
    };

    traverse(nodes || []);

    return ids;
};

export const findFirstSelectableNodeId = (nodes) => {
    for (const node of nodes || []) {
        if (node.children?.length > 0) {
            const childNodeId = findFirstSelectableNodeId(node.children);

            if (childNodeId) {
                return childNodeId;
            }

            continue;
        }

        return node.id;
    }

    return null;
};