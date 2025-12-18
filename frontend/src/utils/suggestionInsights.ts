import type {
    StockAnalysisData,
    HoldingDTO,
    SuggestionReason,
} from '../types/api';

// ============================================================================
// Insight Templates by Suggestion Reason
// ============================================================================

const INSIGHT_TEMPLATES: Record<SuggestionReason, string[]> = {
    'High Volatility': [
        '{sector} momentum. Your holdings overlap with similar trends. Consider averaging up.',
        'Extreme price swings detected. Aligns with your risk tolerance for active trading.',
        'High volatility signals opportunity for traders in {sector}.',
        '{sector} price action is heating up. Complements your existing exposure.',
        'Volatile {sector} play. Strong technical momentum indicators.',
    ],

    'Trending News': [
        'Breaking {sector} news driving attention. Media buzz is spiking.',
        'Complements your exposure without sector overlap. High news volume indicates market interest.',
        'Media attention on {sector} is surging. Strong narrative momentum.',
        '{sector} in focus—{articleCount} articles this week. Growing investor interest.',
        'News catalyst potential in {sector}. Competitive dynamics shifting.',
    ],

    'Sector Diversification': [
        'Less correlated with holdings. Adds portfolio stability and reduces concentration risk.',
        'New sector exposure beyond current holdings. Reduces single-sector concentration.',
        '{sector} diversification opportunity. Complements your current allocation.',
        'Different sector from your top holding {topHolding}. Improves portfolio balance.',
        'Uncorrelated to your current {sector} holdings. Natural hedge.',
    ],

    'Correlated Holdings': [
        'Same sector as {topHolding}. Competitive play within {sector}.',
        'Complements your {topHolding} position. Sector-level exposure opportunity.',
        'Direct competitor to {topHolding}. Diversify within {sector}.',
        '{topHolding} is your largest holding—this is a comparable {sector} alternative.',
        'Sector-level play: pairs well with your {topHolding} strategy.',
    ],
};

// ============================================================================
// Dynamic Substitution Tags
// ============================================================================

interface SubstitutionContext {
    stock: StockAnalysisData;
    holdings: HoldingDTO[];
    reason: SuggestionReason;
}

/**
 * Extract substitution values from context
 */
const getSubstitutionValues = (context: SubstitutionContext): Record<string, string> => {
    const { stock, holdings, reason } = context;

    // Get top holding (largest by value)
    const topHolding = holdings.length > 0
        ? holdings.sort((a, b) => b.currentValue - a.currentValue)[0]
        : null;

    // Get sector of top holding
    const topHoldingSector = topHolding?.sector || 'your portfolio';

    // Count articles for news template
    const articleCount = stock.newsVolume.articleCount.toString();

    return {
        sector: stock.sector,
        topHolding: topHolding?.stockSymbol || 'your positions',
        articleCount,
        topHoldingSector,
        volatilityLevel: stock.volatility.volatilityLevel,
        newsLevel: stock.newsVolume.volumeLevel,
    };
};

/**
 * Apply substitutions to template string
 */
const substituteTemplate = (
    template: string,
    substitutions: Record<string, string>
): string => {
    let result = template;

    Object.entries(substitutions).forEach(([key, value]) => {
        const placeholder = new RegExp(`{${key}}`, 'g');
        result = result.replace(placeholder, value);
    });

    return result;
};

// ============================================================================
// Insight Generation Logic
// ============================================================================

/**
 * Generate personalized insight text for a stock suggestion
 *
 * @param stock - Stock analysis data including volatility, news metrics
 * @param holdings - User's current holdings for context
 * @param reason - Why this stock was suggested
 * @returns Personalized insight string
 */
export const generateInsight = (
    stock: StockAnalysisData,
    holdings: HoldingDTO[],
    reason: SuggestionReason
): string => {
    try {
        // Get templates for this reason
        const templates = INSIGHT_TEMPLATES[reason];
        if (!templates || templates.length === 0) {
            return `${stock.companyName} is suggested for ${reason.toLowerCase()}.`;
        }

        // Pick a random template
        const template = templates[Math.floor(Math.random() * templates.length)];

        // Get substitution values
        const substitutions = getSubstitutionValues({
            stock,
            holdings,
            reason,
        });

        // Apply substitutions
        const insight = substituteTemplate(template, substitutions);

        return insight;
    } catch (error) {
        console.error('Error generating insight:', error);
        return `${stock.companyName} matches your investment criteria.`;
    }
};

// ============================================================================
// Specialized Insights for Edge Cases
// ============================================================================

/**
 * Generate insight for when user has no holdings (diversification mode)
 */
export const generateInsightNoHoldings = (
    stock: StockAnalysisData,
    reason: SuggestionReason
): string => {
    const templates: Record<SuggestionReason, string[]> = {
        'High Volatility': [
            '{sector} volatility creates trading opportunities.',
            'High price swings in {sector}. Strong momentum signals.',
        ],
        'Trending News': [
            '{sector} news is heating up. Media attention is growing.',
            'Trending in {sector}. Strong investor interest detected.',
        ],
        'Sector Diversification': [
            '{sector} exposure starts your portfolio.',
            '{sector} is a solid first sector to explore.',
        ],
        'Correlated Holdings': [
            '{sector} is gaining attention across the market.',
            'Strong fundamentals in {sector} right now.',
        ],
    };

    const templates_for_reason = templates[reason] || [
        `${stock.companyName} is a solid addition to your portfolio.`,
    ];
    const template = templates_for_reason[Math.floor(Math.random() * templates_for_reason.length)];

    return substituteTemplate(template, {
        sector: stock.sector,
        companyName: stock.companyName,
    });
};

/**
 * Generate insight for when portfolio is fully diversified (all candidates owned)
 */
export const generateInsightFullyDiversified = (): string => {
    const insights = [
        'Your portfolio is fully diversified! Consider exploring emerging sectors.',
        'Impressive diversification. Look into growth opportunities in new industries.',
        'Well-balanced portfolio. Consider sector rotations or international exposure.',
        'Strong diversification across holdings. Watch for rebalancing opportunities.',
    ];
    return insights[Math.floor(Math.random() * insights.length)];
};

/**
 * Generate insight based on multiple correlated holdings
 */
export const generateInsightMultipleCorrelations = (
    stock: StockAnalysisData,
    correlatedCount: number,
    holdings: HoldingDTO[]
): string => {
    const topHolding = holdings.sort((a, b) => b.currentValue - a.currentValue)[0];

    if (correlatedCount === 1) {
        return `Sector play within ${stock.sector}. Diversify beyond ${topHolding?.stockSymbol || 'your largest position'}.`;
    }

    if (correlatedCount === 2) {
        return `Your ${stock.sector} exposure is already 2-stock deep. Consider other sectors first.`;
    }

    return `You're heavily concentrated in ${stock.sector} (${correlatedCount} holdings). Consider diversifying.`;
};

// ============================================================================
// Insight Quality Scoring (Optional - for testing/tuning)
// ============================================================================

/**
 * Score insight relevance (0-100)
 * Used for testing template effectiveness
 */
export const scoreInsightRelevance = (
    insight: string,
    reason: SuggestionReason,
    sector: string
): number => {
    let score = 50; // Base score

    // Bonus if contains reason keywords
    if (reason === 'High Volatility' && insight.toLowerCase().includes('volatil')) score += 15;
    if (reason === 'Trending News' && insight.toLowerCase().includes('news')) score += 15;
    if (reason === 'Sector Diversification' && insight.toLowerCase().includes('diversif')) score += 15;
    if (reason === 'Correlated Holdings' && insight.toLowerCase().includes('sector')) score += 15;

    // Bonus if contains sector
    if (insight.toLowerCase().includes(sector.toLowerCase())) score += 10;

    // Penalty if too generic
    if (insight.length < 20) score -= 20;

    return Math.min(100, Math.max(0, score));
};

// ============================================================================
// Insight Variants for A/B Testing (Optional)
// ============================================================================

/**
 * Generate multiple insight variants for A/B testing
 */
export const generateInsightVariants = (
    stock: StockAnalysisData,
    holdings: HoldingDTO[],
    reason: SuggestionReason,
    count: number = 3
): string[] => {
    const templates = INSIGHT_TEMPLATES[reason];
    if (!templates || templates.length === 0) return [];

    const substitutions = getSubstitutionValues({
        stock,
        holdings,
        reason,
    });

    // Pick random templates (without replacement if possible)
    const variants: string[] = [];
    const selectedIndices = new Set<number>();

    for (let i = 0; i < Math.min(count, templates.length); i++) {
        let idx: number;
        do {
            idx = Math.floor(Math.random() * templates.length);
        } while (selectedIndices.has(idx) && selectedIndices.size < templates.length);

        selectedIndices.add(idx);
        const variant = substituteTemplate(templates[idx], substitutions);
        variants.push(variant);
    }

    return variants;
};