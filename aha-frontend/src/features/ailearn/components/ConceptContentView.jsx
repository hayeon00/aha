import LearningContentMarkdown, { LearningContentSkeleton } from "./LearningContentMarkdown.jsx";
import "./ConceptLearningWorkspace.css";

export default function ConceptContentView({
    chapter,
    loading,
}) {
    if (loading) {
        return <section className="concept-content-surface loading"><LearningContentSkeleton /></section>;
    }

    if (!chapter) {
        return (
            <section className="concept-content-surface blank">
                <EmptyIllustration />
                <h3>학습할 목차를 선택해 주세요</h3>
                <p>왼쪽 표준 목차에서 단원을 선택하면 개념 설명이 바로 표시됩니다.</p>
            </section>
        );
    }

    const hasContent = Boolean(chapter.content);
    const source = chapter.mapped ? "document" : hasContent ? "ai" : "empty";
    const cleanedContent = sanitizeConceptContent(chapter.content);

    return (
        <section className={`concept-content-surface ${source}`}>
            <header className="selected-concept-header">
                <div className="selected-concept-meta">
                    <h2>{chapter.tocTitle}</h2>
                </div>
            </header>

            {cleanedContent ? (
                <div className="selected-concept-markdown">
                    <LearningContentMarkdown content={cleanedContent} />
                </div>
            ) : (
                <div className="selected-concept-empty">
                    <EmptyIllustration />
                    <h3>이 목차에는 연결된 문서 내용이 없습니다</h3>
                    <p>업로드한 자료에서 이 단원과 연결되는 내용을 찾지 못했어요.</p>
                </div>
            )}
        </section>
    );
}

function sanitizeConceptContent(rawContent) {
    if (typeof rawContent !== "string") return "";

    const sanitized = rawContent
        .replace(/\r\n?/g, "\n")
        .replace(/\\\*\\\*/g, "**")
        .replace(/\*\*[ \t]+([^*\n]+?)[ \t]+\*\*/g, "**$1**")
        .replace(/^\s*(?:#{1,6}\s*)?(?:\*\*)?제목(?:\*\*)?\s*:\s*[^\n]*(?:\n|$)/i, "")
        .replace(/^\s*(?:#{1,6}\s*)?(?:\*\*)?내용(?:\*\*)?\s*:\s*(?:\n|$)/i, "")
        .trim();

    return emphasizeTechnicalTerms(sanitized);
}

function emphasizeTechnicalTerms(content) {
    const protectedMarkdown = /(```[\s\S]*?```|`[^`\n]+`)/g;
    const technicalTerms = /(기본\s*키\s*\(PK\)|외래\s*키\s*\(FK\)|NOT\s+NULL|UNIQUE|MIN\s*\/\s*MAX|\b(?:PK|FK|COUNT|SUM|AVG|MIN|MAX)\b)/gi;

    return content
        .split(protectedMarkdown)
        .map((segment, index) => (
            index % 2 === 1
                ? segment
                : segment.replace(technicalTerms, (term) => `\`${term}\``)
        ))
        .join("");
}

function EmptyIllustration() {
    return (
        <div className="concept-empty-glyph" aria-hidden="true">
            <span />
            <span />
            <span />
        </div>
    );
}
