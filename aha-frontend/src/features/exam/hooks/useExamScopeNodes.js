import { useCallback, useEffect, useState } from "react";
import { getExamScopeNodes } from "../api/examApi.js";
import { getApiData } from "../../ailearn/utils/apiResponseUtils.js";
import {
    findFirstSelectableNodeId,
    getDefaultExpandedNodeIds,
} from "../utils/scopeTreeUtils.js";

export const useExamScopeNodes = ({ examVersionId, onResetContent } = {}) => {
    const [scopeNodes, setScopeNodes] = useState([]);
    const [expandedNodeIds, setExpandedNodeIds] = useState([]);
    const [selectedNodeId, setSelectedNodeId] = useState(null);
    const [isScopeLoading, setIsScopeLoading] = useState(false);
    const [scopeMessage, setScopeMessage] = useState("");

    const resetScope = useCallback(() => {
        setScopeNodes([]);
        setExpandedNodeIds([]);
        setSelectedNodeId(null);
    }, []);

    const fetchScopeNodes = useCallback(async () => {
        if (!examVersionId) {
            resetScope();
            return;
        }

        try {
            setIsScopeLoading(true);
            setScopeMessage("");
            onResetContent?.();

            const response = await getExamScopeNodes(examVersionId);
            const nodes = getApiData(response) || [];
            const nextScopeNodes = Array.isArray(nodes) ? nodes : [];

            setScopeNodes(nextScopeNodes);
            setExpandedNodeIds(getDefaultExpandedNodeIds(nextScopeNodes));
            setSelectedNodeId(findFirstSelectableNodeId(nextScopeNodes));
        } catch (error) {
            console.error("시험 목차 조회 실패:", error);

            resetScope();
            onResetContent?.();
            setScopeMessage("시험 목차를 불러오지 못했습니다.");
        } finally {
            setIsScopeLoading(false);
        }
    }, [examVersionId, resetScope, onResetContent]);

    useEffect(() => {
        queueMicrotask(() => {
            fetchScopeNodes();
        });
    }, [fetchScopeNodes]);

    const toggleNode = useCallback((nodeId) => {
        setExpandedNodeIds((prev) => {
            if (prev.includes(nodeId)) {
                return prev.filter((id) => id !== nodeId);
            }

            return [...prev, nodeId];
        });
    }, []);

    const handleSelectNode = useCallback((node) => {
        if (node.children?.length > 0) {
            toggleNode(node.id);
            return;
        }

        setSelectedNodeId(node.id);
        onResetContent?.();
    }, [toggleNode, onResetContent]);

    const selectNodeById = useCallback((nodeId) => {
        if (!nodeId) return;
        setSelectedNodeId(Number(nodeId));
        onResetContent?.();
    }, [onResetContent]);

    return {
        scopeNodes,
        expandedNodeIds,
        selectedNodeId,
        isScopeLoading,
        scopeMessage,
        toggleNode,
        handleSelectNode,
        selectNodeById,
        resetScope,
        refetchScopeNodes: fetchScopeNodes,
    };
};
