export const DEFAULT_TYPEWRITER_SEGMENTS = [
    [
        '你好，我是 Dasi。',
        '我会帮你在 MiniAgent Studio 里快速组装专属 Agent。',
        '从提示词、执行策略到 MCP 工具，一次配置即可开跑。'
    ],
    [
        '在 Agent Plaza，你可以浏览热门案例、点赞收藏并一键 Fork。',
        '把灵感沉淀进 Repository，持续复用到你的 Work 流程。',
        '从想法到执行，只需要一条指令。'
    ],
    [
        '无论是 Chat 对话还是 Work 自动化任务，',
        '我都会按你的目标输出可执行、可追踪、可复用的结果。'
    ]
];

export const createTypewriter = ({
    segments = DEFAULT_TYPEWRITER_SEGMENTS,
    charDelay = 60,
    segmentPause = 3000,
    loop = true,
    onUpdate
} = {}) => {
    let typingTimer = null;
    let pauseTimer = null;
    let running = false;
    let segmentIndex = 0;
    let lineIndex = 0;
    let charIndex = 0;
    let displayLines = [];

    const emit = (playing) => {
        onUpdate &&
            onUpdate({
                lines: [...displayLines],
                segmentIndex,
                lineIndex,
                playing
            });
    };

    const clearTimers = () => {
        if (typingTimer) {
            clearTimeout(typingTimer);
            typingTimer = null;
        }
        if (pauseTimer) {
            clearTimeout(pauseTimer);
            pauseTimer = null;
        }
    };

    const stop = () => {
        if (!running) {
            clearTimers();
            return;
        }
        running = false;
        clearTimers();
        emit(false);
    };

    const resetState = () => {
        segmentIndex = 0;
        lineIndex = 0;
        charIndex = 0;
        displayLines = [];
    };

    const prepareSegment = () => {
        const segment = segments[segmentIndex] || [];
        displayLines = segment.map(() => '');
        lineIndex = 0;
        charIndex = 0;
        emit(true);
    };

    const tick = () => {
        if (!running) {
            return;
        }
        const segment = segments[segmentIndex];
        if (!segment || segment.length === 0) {
            stop();
            return;
        }
        const line = segment[lineIndex] || '';
        if (charIndex < line.length) {
            displayLines[lineIndex] = (displayLines[lineIndex] || '') + line[charIndex];
            charIndex += 1;
            emit(true);
            typingTimer = setTimeout(tick, charDelay);
            return;
        }

        lineIndex += 1;
        charIndex = 0;
        if (lineIndex < segment.length) {
            emit(true);
            typingTimer = setTimeout(tick, charDelay);
            return;
        }

        pauseTimer = setTimeout(() => {
            if (!running) {
                return;
            }
            advanceSegment();
        }, segmentPause);
    };

    const advanceSegment = () => {
        segmentIndex += 1;
        if (segmentIndex >= segments.length) {
            if (!loop) {
                stop();
                return;
            }
            segmentIndex = 0;
        }
        prepareSegment();
        typingTimer = setTimeout(tick, charDelay);
    };

    const start = () => {
        stop();
        resetState();
        if (!segments || segments.length === 0) {
            emit(false);
            return;
        }
        running = true;
        prepareSegment();
        typingTimer = setTimeout(tick, charDelay);
    };

    const reset = () => {
        stop();
        resetState();
        emit(false);
    };

    return {
        start,
        stop,
        reset
    };
};
