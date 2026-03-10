const FALLBACK_STRATEGY = 'react';

const STRATEGY_TONES = {
    step: {
        light: {
            overlay:
                'linear-gradient(160deg, rgba(251,191,36,0.09), rgba(255,255,255,0) 46%), radial-gradient(circle at 100% 0, rgba(251,191,36,0.22), rgba(255,255,255,0) 54%)',
            badgeBg: 'rgba(251,191,36,0.12)',
            badgeBorder: 'rgba(245,158,11,0.28)',
            badgeText: '#92400e',
            forkBg: 'rgba(251,191,36,0.14)',
            forkBorder: 'rgba(245,158,11,0.45)',
            forkText: '#b45309',
            forkHoverBg: '#b45309',
            cardBg: 'rgba(255,255,255,0.96)',
            cardBorder: 'rgba(245,158,11,0.16)',
            cardShadow: '0 14px 34px rgba(15,23,42,0.06)',
            primaryBtnBg: 'rgba(255,251,235,0.92)',
            primaryBtnBorder: 'rgba(245,158,11,0.26)',
            primaryBtnText: '#b45309',
            primaryBtnHoverBg: 'rgba(255,247,237,0.98)',
            studioActiveBg: 'rgba(255,247,237,0.98)',
            studioActiveBorder: 'rgba(245,158,11,0.34)',
            studioActiveText: '#9a5a12',
            studioHoverBg: 'rgba(255,251,235,0.92)',
            studioHoverBorder: 'rgba(245,158,11,0.26)'
        },
        dark: {
            overlay:
                'linear-gradient(160deg, rgba(251,191,36,0.14), rgba(15,23,42,0) 48%), radial-gradient(circle at 100% 0, rgba(251,191,36,0.24), rgba(15,23,42,0) 56%)',
            badgeBg: 'rgba(251,191,36,0.16)',
            badgeBorder: 'rgba(251,191,36,0.45)',
            badgeText: '#fcd34d',
            forkBg: 'rgba(251,191,36,0.14)',
            forkBorder: 'rgba(251,191,36,0.45)',
            forkText: '#fbbf24',
            forkHoverBg: '#f59e0b',
            cardBg: 'rgba(18,30,54,0.72)',
            cardBorder: 'rgba(245,158,11,0.18)',
            cardShadow: '0 16px 34px rgba(2,8,23,0.18)',
            primaryBtnBg: 'rgba(251,191,36,0.08)',
            primaryBtnBorder: 'rgba(251,191,36,0.26)',
            primaryBtnText: '#f7d487',
            primaryBtnHoverBg: 'rgba(251,191,36,0.14)',
            studioActiveBg: 'rgba(251,191,36,0.2)',
            studioActiveBorder: 'rgba(251,191,36,0.48)',
            studioActiveText: '#f7d487',
            studioHoverBg: 'rgba(251,191,36,0.12)',
            studioHoverBorder: 'rgba(251,191,36,0.28)'
        }
    },
    loop: {
        light: {
            overlay:
                'linear-gradient(160deg, rgba(59,130,246,0.09), rgba(255,255,255,0) 46%), radial-gradient(circle at 100% 0, rgba(59,130,246,0.2), rgba(255,255,255,0) 54%)',
            badgeBg: 'rgba(59,130,246,0.11)',
            badgeBorder: 'rgba(59,130,246,0.25)',
            badgeText: '#1d4ed8',
            forkBg: 'rgba(59,130,246,0.13)',
            forkBorder: 'rgba(59,130,246,0.45)',
            forkText: '#2563eb',
            forkHoverBg: '#2563eb',
            cardBg: 'rgba(255,255,255,0.96)',
            cardBorder: 'rgba(59,130,246,0.14)',
            cardShadow: '0 14px 34px rgba(15,23,42,0.06)',
            primaryBtnBg: 'rgba(239,246,255,0.92)',
            primaryBtnBorder: 'rgba(59,130,246,0.22)',
            primaryBtnText: '#2563eb',
            primaryBtnHoverBg: 'rgba(219,234,254,0.98)',
            studioActiveBg: 'rgba(239,246,255,0.98)',
            studioActiveBorder: 'rgba(59,130,246,0.34)',
            studioActiveText: '#2563eb',
            studioHoverBg: 'rgba(239,246,255,0.9)',
            studioHoverBorder: 'rgba(59,130,246,0.26)'
        },
        dark: {
            overlay:
                'linear-gradient(160deg, rgba(59,130,246,0.14), rgba(15,23,42,0) 48%), radial-gradient(circle at 100% 0, rgba(59,130,246,0.24), rgba(15,23,42,0) 56%)',
            badgeBg: 'rgba(96,165,250,0.16)',
            badgeBorder: 'rgba(96,165,250,0.42)',
            badgeText: '#93c5fd',
            forkBg: 'rgba(96,165,250,0.14)',
            forkBorder: 'rgba(96,165,250,0.42)',
            forkText: '#60a5fa',
            forkHoverBg: '#3b82f6',
            cardBg: 'rgba(18,30,54,0.72)',
            cardBorder: 'rgba(96,165,250,0.18)',
            cardShadow: '0 16px 34px rgba(2,8,23,0.18)',
            primaryBtnBg: 'rgba(96,165,250,0.08)',
            primaryBtnBorder: 'rgba(96,165,250,0.26)',
            primaryBtnText: '#c9e0ff',
            primaryBtnHoverBg: 'rgba(96,165,250,0.14)',
            studioActiveBg: 'rgba(96,165,250,0.2)',
            studioActiveBorder: 'rgba(96,165,250,0.48)',
            studioActiveText: '#c9e0ff',
            studioHoverBg: 'rgba(96,165,250,0.12)',
            studioHoverBorder: 'rgba(96,165,250,0.28)'
        }
    },
    react: {
        light: {
            overlay:
                'linear-gradient(160deg, rgba(16,185,129,0.08), rgba(255,255,255,0) 46%), radial-gradient(circle at 100% 0, rgba(45,212,191,0.16), rgba(255,255,255,0) 54%)',
            badgeBg: 'rgba(16,185,129,0.1)',
            badgeBorder: 'rgba(16,185,129,0.22)',
            badgeText: '#047857',
            forkBg: 'rgba(16,185,129,0.12)',
            forkBorder: 'rgba(16,185,129,0.42)',
            forkText: '#047857',
            forkHoverBg: '#047857',
            cardBg: 'rgba(255,255,255,0.96)',
            cardBorder: 'rgba(16,185,129,0.16)',
            cardShadow: '0 14px 34px rgba(15,23,42,0.06)',
            primaryBtnBg: 'rgba(236,253,245,0.92)',
            primaryBtnBorder: 'rgba(16,185,129,0.24)',
            primaryBtnText: '#047857',
            primaryBtnHoverBg: 'rgba(220,252,231,0.98)',
            studioActiveBg: 'rgba(236,253,245,0.98)',
            studioActiveBorder: 'rgba(16,185,129,0.34)',
            studioActiveText: '#047857',
            studioHoverBg: 'rgba(236,253,245,0.9)',
            studioHoverBorder: 'rgba(16,185,129,0.26)'
        },
        dark: {
            overlay:
                'linear-gradient(160deg, rgba(16,185,129,0.14), rgba(15,23,42,0) 48%), radial-gradient(circle at 100% 0, rgba(45,212,191,0.22), rgba(15,23,42,0) 56%)',
            badgeBg: 'rgba(16,185,129,0.14)',
            badgeBorder: 'rgba(52,211,153,0.4)',
            badgeText: '#86efac',
            forkBg: 'rgba(16,185,129,0.14)',
            forkBorder: 'rgba(52,211,153,0.4)',
            forkText: '#6ee7b7',
            forkHoverBg: '#059669',
            cardBg: 'rgba(18,30,54,0.72)',
            cardBorder: 'rgba(16,185,129,0.18)',
            cardShadow: '0 16px 34px rgba(2,8,23,0.18)',
            primaryBtnBg: 'rgba(16,185,129,0.08)',
            primaryBtnBorder: 'rgba(52,211,153,0.26)',
            primaryBtnText: '#9decc0',
            primaryBtnHoverBg: 'rgba(16,185,129,0.14)',
            studioActiveBg: 'rgba(16,185,129,0.18)',
            studioActiveBorder: 'rgba(52,211,153,0.44)',
            studioActiveText: '#9decc0',
            studioHoverBg: 'rgba(16,185,129,0.12)',
            studioHoverBorder: 'rgba(52,211,153,0.28)'
        }
    }
};

const STUDIO_EXTRA_TONES = {
    model: {
        light: {
            activeBg: 'rgba(254,242,242,0.98)',
            activeBorder: 'rgba(220,38,38,0.34)',
            activeText: '#b91c1c',
            hoverBg: 'rgba(254,242,242,0.88)',
            hoverBorder: 'rgba(220,38,38,0.24)'
        },
        dark: {
            activeBg: 'rgba(127,29,29,0.26)',
            activeBorder: 'rgba(248,113,113,0.46)',
            activeText: '#fca5a5',
            hoverBg: 'rgba(127,29,29,0.18)',
            hoverBorder: 'rgba(248,113,113,0.3)'
        }
    },
    tool: {
        light: {
            activeBg: 'rgba(255,247,237,0.98)',
            activeBorder: 'rgba(249,115,22,0.34)',
            activeText: '#c2410c',
            hoverBg: 'rgba(255,247,237,0.88)',
            hoverBorder: 'rgba(249,115,22,0.24)'
        },
        dark: {
            activeBg: 'rgba(124,45,18,0.28)',
            activeBorder: 'rgba(251,146,60,0.46)',
            activeText: '#fdba74',
            hoverBg: 'rgba(124,45,18,0.2)',
            hoverBorder: 'rgba(251,146,60,0.3)'
        }
    }
};

const normalizeStrategy = (value) => {
    const normalized = (value || '').toString().trim().toLowerCase();
    if (normalized === 'step' || normalized === 'loop' || normalized === 'react') {
        return normalized;
    }
    return FALLBACK_STRATEGY;
};

const resolveMode = (isDark) => (isDark ? 'dark' : 'light');

export const getStrategyTone = (strategy, isDark = false) => {
    const key = normalizeStrategy(strategy);
    const mode = resolveMode(isDark);
    const tone = STRATEGY_TONES[key]?.[mode] || STRATEGY_TONES[FALLBACK_STRATEGY][mode];
    return {
        strategy: key,
        ...tone
    };
};

export const getStudioSelectionTone = (kind, isDark = false) => {
    const mode = resolveMode(isDark);
    const normalized = normalizeStrategy(kind);
    if (normalized === kind) {
        const tone = getStrategyTone(normalized, isDark);
        return {
            activeBg: tone.studioActiveBg,
            activeBorder: tone.studioActiveBorder,
            activeText: tone.studioActiveText,
            hoverBg: tone.studioHoverBg,
            hoverBorder: tone.studioHoverBorder
        };
    }

    const extraKey = (kind || '').toString().trim().toLowerCase();
    return STUDIO_EXTRA_TONES[extraKey]?.[mode] || {
        activeBg: 'rgba(226,232,240,0.7)',
        activeBorder: 'rgba(148,163,184,0.42)',
        activeText: '#334155',
        hoverBg: 'rgba(226,232,240,0.52)',
        hoverBorder: 'rgba(148,163,184,0.3)'
    };
};

export const normalizeStrategyType = normalizeStrategy;
