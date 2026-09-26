import { useEffect, useState } from "react";
import { getApiData } from "../../ailearn/utils/apiResponseUtils.js";

export function useConceptQuery(fetcher, id) {
    const [attempt, setAttempt] = useState(0);
    const [state, setState] = useState(null);

    useEffect(() => {
        if (!id) return;
        const controller = new AbortController();
        fetcher(id, controller.signal).then((response) => {
            if (!controller.signal.aborted) {
                setState({ id, attempt, data: getApiData(response), error: false });
            }
        }).catch(() => {
            if (!controller.signal.aborted) {
                setState({ id, attempt, data: null, error: true });
            }
        });
        return () => controller.abort();
    }, [fetcher, id, attempt]);

    const current = state?.id === id && state?.attempt === attempt;
    return {
        data: current ? state.data : null,
        error: current && state.error,
        loading: Boolean(id) && !current,
        retry: () => setAttempt((value) => value + 1),
    };
}
