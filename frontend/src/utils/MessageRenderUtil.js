const toText = (value) => {
    if (value === null || value === undefined) return '';
    return String(value);
};

const hashText = (input) => {
    let hash = 0x811c9dc5;
    const text = toText(input);
    for (let i = 0; i < text.length; i += 1) {
        hash ^= text.charCodeAt(i);
        hash = Math.imul(hash, 0x01000193);
    }
    return (hash >>> 0).toString(36);
};

export const toSafeTimestamp = (value) => {
    if (value === null || value === undefined || value === '') return 0;
    if (typeof value === 'number' && Number.isFinite(value)) return value;
    const parsed = Date.parse(value);
    if (Number.isNaN(parsed)) return 0;
    return parsed;
};

export const createStableRecordId = ({ sourceId, sessionId = '', prefix = 'item', signatureParts = [], seenMap }) => {
    const explicitId = toText(sourceId).trim();
    if (explicitId) {
        return explicitId;
    }
    const signature = `${toText(sessionId)}|${signatureParts.map((item) => toText(item)).join('|')}`;
    const duplicateIndex = seenMap.get(signature) || 0;
    seenMap.set(signature, duplicateIndex + 1);
    return `${prefix}_${hashText(signature)}_${duplicateIndex}`;
};

export const areMessageListsEqual = (prevList = [], nextList = []) => {
    if (prevList === nextList) return true;
    if (!Array.isArray(prevList) || !Array.isArray(nextList)) return false;
    if (prevList.length !== nextList.length) return false;
    for (let i = 0; i < prevList.length; i += 1) {
        const prev = prevList[i] || {};
        const next = nextList[i] || {};
        if (
            toText(prev.id) !== toText(next.id) ||
            toText(prev.role) !== toText(next.role) ||
            toText(prev.content) !== toText(next.content) ||
            Number(prev.createdAt || 0) !== Number(next.createdAt || 0)
        ) {
            return false;
        }
    }
    return true;
};

export const areCardListsEqual = (prevList = [], nextList = []) => {
    if (prevList === nextList) return true;
    if (!Array.isArray(prevList) || !Array.isArray(nextList)) return false;
    if (prevList.length !== nextList.length) return false;
    for (let i = 0; i < prevList.length; i += 1) {
        const prev = prevList[i] || {};
        const next = nextList[i] || {};
        if (
            toText(prev.id) !== toText(next.id) ||
            toText(prev.sectionType) !== toText(next.sectionType) ||
            toText(prev.sectionContent) !== toText(next.sectionContent) ||
            toText(prev.round) !== toText(next.round) ||
            toText(prev.step) !== toText(next.step) ||
            toText(prev.timestamp) !== toText(next.timestamp)
        ) {
            return false;
        }
    }
    return true;
};
