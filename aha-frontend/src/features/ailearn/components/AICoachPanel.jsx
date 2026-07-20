import "./ConceptLearningWorkspace.css";

const actions = [
    ["💡", "쉬운 설명", "복잡한 내용을 쉬운 말로 풀어드려요."],
    ["🎯", "출제 포인트", "시험에서 중요한 지점을 짚어드려요."],
    ["✓", "실력 점검", "짧은 질문으로 이해도를 확인해요."],
];

export default function AICoachPanel({ chapter, generating, onGenerate }) {
    const hasContent = Boolean(chapter?.content);
    const isMapped = Boolean(chapter?.mapped);
    const isEmpty = Boolean(chapter) && !hasContent && !isMapped;

    const message = !chapter
        ? "왼쪽 목차에서 학습할 단원을 선택해 주세요."
        : isEmpty
            ? "이 단원은 업로드하신 문서에 빠져 있는 빈틈 영역이에요! 🤖 AI 코치에게 이 단원의 맞춤형 개념 설명을 생성해달라고 요청해볼까요?"
            : isMapped
                ? "내가 정리한 문서 내용을 바탕으로 실력을 점검해봐요!"
                : "AI가 보완한 설명이 준비됐어요. 핵심 내용을 읽고 실력을 점검해봐요!";

    return (
        <div className={`smart-coach ${isEmpty ? "focused" : ""}`}>
            <div className="coach-avatar" aria-hidden="true">
                <span className="coach-antenna" />
                <div><i /><i /></div>
            </div>
            <div className="coach-speech-bubble">
                {isEmpty && <span className="coach-sparkle">✦</span>}
                <p>{message}</p>
            </div>

            {isEmpty && (
                <button className="coach-generate-button" onClick={onGenerate} disabled={generating}>
                    {generating ? <span className="coach-button-spinner" /> : "🤖"}
                    {generating ? "설명을 만들고 있어요..." : "AI에게 이 단원 설명 요청하기"}
                </button>
            )}

            <div className="coach-action-heading">이 단원에서 도와드릴 수 있어요</div>
            <div className="smart-coach-actions">
                {actions.map(([icon, title, description]) => (
                    <button key={title} disabled={!hasContent}>
                        <span>{icon}</span><div><strong>{title}</strong><small>{description}</small></div>
                    </button>
                ))}
            </div>
        </div>
    );
}
