import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import "./LearningContentMarkdown.css";

const markdownComponents = {
    table: ({ children }) => (
        <div className="aha-markdown-table-wrap">
            <table>{children}</table>
        </div>
    ),
    a: ({ href, children }) => (
        <a href={href} target="_blank" rel="noreferrer noopener">
            {children}
        </a>
    ),
};

export function LearningContentSkeleton() {
    return (
        <div className="aha-learning-skeleton" aria-label="개념 설명 생성 중" aria-busy="true">
            <span className="wide" />
            <span />
            <span className="medium" />
            <div className="table-placeholder">
                <span /><span /><span />
            </div>
            <span />
            <span className="short" />
        </div>
    );
}

function LearningContentMarkdown({ content, loading = false }) {
    if (loading) {
        return <LearningContentSkeleton />;
    }

    return (
        <div className="aha-learning-markdown">
            <ReactMarkdown
                remarkPlugins={[remarkGfm]}
                components={markdownComponents}
                skipHtml
            >
                {content || ""}
            </ReactMarkdown>
        </div>
    );
}

export default LearningContentMarkdown;
