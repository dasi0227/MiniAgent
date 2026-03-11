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
            studioHoverBorder: 'rgba(245,158,11,0.26)',
            glowPrimary: 'rgba(251,191,36,0.3)',
            glowSecondary: 'rgba(245,158,11,0.18)',
            sectionTintBg: 'rgba(255,251,235,0.5)',
            sectionBorder: 'rgba(245,158,11,0.2)',
            divider: 'rgba(245,158,11,0.18)',
            focus: 'rgba(217,119,6,0.48)'
        },
        dark: {
            overlay:
                'linear-gradient(160deg, rgba(236,201,75,0.12), rgba(15,23,42,0) 48%), radial-gradient(circle at 100% 0, rgba(229,188,72,0.2), rgba(15,23,42,0) 58%)',
            badgeBg: 'rgba(236,201,75,0.14)',
            badgeBorder: 'rgba(236,201,75,0.38)',
            badgeText: '#e8cd81',
            forkBg: 'rgba(236,201,75,0.12)',
            forkBorder: 'rgba(236,201,75,0.36)',
            forkText: '#e2bf66',
            forkHoverBg: '#c39a3f',
            cardBg: 'rgba(18,27,44,0.78)',
            cardBorder: 'rgba(226,189,89,0.2)',
            cardShadow: '0 16px 34px rgba(2,8,23,0.24)',
            primaryBtnBg: 'rgba(236,201,75,0.1)',
            primaryBtnBorder: 'rgba(236,201,75,0.24)',
            primaryBtnText: '#e8cd81',
            primaryBtnHoverBg: 'rgba(236,201,75,0.16)',
            studioActiveBg: 'rgba(236,201,75,0.18)',
            studioActiveBorder: 'rgba(236,201,75,0.42)',
            studioActiveText: '#edd69b',
            studioHoverBg: 'rgba(236,201,75,0.1)',
            studioHoverBorder: 'rgba(236,201,75,0.24)',
            glowPrimary: 'rgba(236,201,75,0.24)',
            glowSecondary: 'rgba(197,157,53,0.14)',
            sectionTintBg: 'rgba(236,201,75,0.06)',
            sectionBorder: 'rgba(236,201,75,0.22)',
            divider: 'rgba(236,201,75,0.2)',
            focus: 'rgba(236,201,75,0.42)'
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
            studioHoverBorder: 'rgba(59,130,246,0.26)',
            glowPrimary: 'rgba(59,130,246,0.3)',
            glowSecondary: 'rgba(14,165,233,0.16)',
            sectionTintBg: 'rgba(239,246,255,0.48)',
            sectionBorder: 'rgba(59,130,246,0.18)',
            divider: 'rgba(59,130,246,0.16)',
            focus: 'rgba(37,99,235,0.46)'
        },
        dark: {
            overlay:
                'linear-gradient(160deg, rgba(101,153,241,0.12), rgba(15,23,42,0) 48%), radial-gradient(circle at 100% 0, rgba(88,136,227,0.2), rgba(15,23,42,0) 58%)',
            badgeBg: 'rgba(113,162,243,0.14)',
            badgeBorder: 'rgba(113,162,243,0.38)',
            badgeText: '#b8d2ff',
            forkBg: 'rgba(113,162,243,0.12)',
            forkBorder: 'rgba(113,162,243,0.36)',
            forkText: '#9dc0fb',
            forkHoverBg: '#5c8fe0',
            cardBg: 'rgba(18,27,44,0.78)',
            cardBorder: 'rgba(113,162,243,0.2)',
            cardShadow: '0 16px 34px rgba(2,8,23,0.24)',
            primaryBtnBg: 'rgba(113,162,243,0.09)',
            primaryBtnBorder: 'rgba(113,162,243,0.24)',
            primaryBtnText: '#bdd6ff',
            primaryBtnHoverBg: 'rgba(113,162,243,0.15)',
            studioActiveBg: 'rgba(113,162,243,0.18)',
            studioActiveBorder: 'rgba(113,162,243,0.42)',
            studioActiveText: '#d0e2ff',
            studioHoverBg: 'rgba(113,162,243,0.1)',
            studioHoverBorder: 'rgba(113,162,243,0.24)',
            glowPrimary: 'rgba(113,162,243,0.24)',
            glowSecondary: 'rgba(71,127,226,0.14)',
            sectionTintBg: 'rgba(113,162,243,0.06)',
            sectionBorder: 'rgba(113,162,243,0.22)',
            divider: 'rgba(113,162,243,0.2)',
            focus: 'rgba(113,162,243,0.42)'
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
            studioHoverBorder: 'rgba(16,185,129,0.26)',
            glowPrimary: 'rgba(16,185,129,0.3)',
            glowSecondary: 'rgba(20,184,166,0.16)',
            sectionTintBg: 'rgba(236,253,245,0.48)',
            sectionBorder: 'rgba(16,185,129,0.18)',
            divider: 'rgba(16,185,129,0.16)',
            focus: 'rgba(5,150,105,0.44)'
        },
        dark: {
            overlay:
                'linear-gradient(160deg, rgba(72,181,146,0.12), rgba(15,23,42,0) 48%), radial-gradient(circle at 100% 0, rgba(72,181,146,0.2), rgba(15,23,42,0) 58%)',
            badgeBg: 'rgba(72,181,146,0.14)',
            badgeBorder: 'rgba(72,181,146,0.38)',
            badgeText: '#a4e0c7',
            forkBg: 'rgba(72,181,146,0.12)',
            forkBorder: 'rgba(72,181,146,0.36)',
            forkText: '#95d7bc',
            forkHoverBg: '#3f9e7f',
            cardBg: 'rgba(18,27,44,0.78)',
            cardBorder: 'rgba(72,181,146,0.2)',
            cardShadow: '0 16px 34px rgba(2,8,23,0.24)',
            primaryBtnBg: 'rgba(72,181,146,0.09)',
            primaryBtnBorder: 'rgba(72,181,146,0.24)',
            primaryBtnText: '#b0e4d0',
            primaryBtnHoverBg: 'rgba(72,181,146,0.15)',
            studioActiveBg: 'rgba(72,181,146,0.18)',
            studioActiveBorder: 'rgba(72,181,146,0.42)',
            studioActiveText: '#bdebd9',
            studioHoverBg: 'rgba(72,181,146,0.1)',
            studioHoverBorder: 'rgba(72,181,146,0.24)',
            glowPrimary: 'rgba(72,181,146,0.24)',
            glowSecondary: 'rgba(53,150,121,0.14)',
            sectionTintBg: 'rgba(72,181,146,0.06)',
            sectionBorder: 'rgba(72,181,146,0.22)',
            divider: 'rgba(72,181,146,0.2)',
            focus: 'rgba(72,181,146,0.42)'
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
