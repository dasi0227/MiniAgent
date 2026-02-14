const pickResultData = (resp) => {
    if (resp && typeof resp === 'object' && Object.prototype.hasOwnProperty.call(resp, 'code')) {
        if (resp.code !== 200) {
            throw new Error(resp.info || '操作失败');
        }
        return resp.data;
    }
    return resp?.data ?? resp?.result ?? resp;
};

const requireText = (value, field) => {
    const normalized = typeof value === 'string' ? value.trim() : '';
    if (!normalized) {
        throw new Error(`登录信息异常：${field} 缺失`);
    }
    return normalized;
};

const requireNumber = (value, field) => {
    const normalized = Number(value);
    if (!Number.isFinite(normalized)) {
        throw new Error(`登录信息异常：${field} 缺失`);
    }
    return normalized;
};

export const parseAuthPayload = (resp) => {
    const payload = pickResultData(resp);
    const token = requireText(payload?.token, 'token');
    const userId = requireNumber(payload?.userId, 'userId');
    const username = requireText(payload?.username, 'username');
    const role = requireText(payload?.role, 'role');

    return {
        token,
        user: {
            userId,
            username,
            role,
            userStatus: payload?.userStatus
        }
    };
};
