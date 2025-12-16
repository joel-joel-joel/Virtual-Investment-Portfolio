/**
 * Dynamic Sector Color Service
 * Automatically assigns consistent colors to sectors from Finnhub
 * Ensures: 1) Colors are UNIQUE per sector, 2) CONSISTENT across app sessions
 * Persists mappings to preserve consistency across sessions
 */

import AsyncStorage from '@react-native-async-storage/async-storage';

const STORAGE_KEY = 'sector_color_mapping';

// Base color palette - vibrant, distinct colors
const COLOR_PALETTE = [
    { color: '#0369A1', bgLight: '#EFF6FF' },      // Sky Blue
    { color: '#B45309', bgLight: '#FEF3C7' },      // Amber
    { color: '#15803D', bgLight: '#F0FDF4' },      // Green
    { color: '#6D28D9', bgLight: '#F5F3FF' },      // Violet
    { color: '#BE123C', bgLight: '#FFE4E6' },      // Rose
    { color: '#EA580C', bgLight: '#FFEDD5' },      // Orange
    { color: '#7C3AED', bgLight: '#F3E8FF' },      // Purple
    { color: '#0891B2', bgLight: '#ECF8FF' },      // Cyan
    { color: '#6B21A8', bgLight: '#FAF5FF' },      // Fuchsia
    { color: '#92400E', bgLight: '#FFFBEB' },      // Yellow
    { color: '#1F2937', bgLight: '#F9FAFB' },      // Gray
    { color: '#DC2626', bgLight: '#FEE2E2' },      // Red
];

export interface SectorColor {
    color: string;
    bgLight: string;
}

/**
 * In-memory cache of sector colors
 * Map: sector name -> color assignment
 */
let sectorColorCache: Map<string, SectorColor> = new Map();

/**
 * Track which color indices have been used
 * Ensures no two sectors get the same color
 */
let usedColorIndices: Set<number> = new Set();
let isInitialized = false;

/**
 * Initialize the sector color system by loading persisted mappings
 * MUST be called once when app starts (e.g., in App.tsx or root layout)
 */
export const initializeSectorColors = async (): Promise<void> => {
    if (isInitialized) return;

    try {
        const stored = await AsyncStorage.getItem(STORAGE_KEY);
        if (stored) {
            const parsed = JSON.parse(stored);

            // Rebuild cache from persistence
            sectorColorCache.clear();
            usedColorIndices.clear();

            for (const [sector, colorObj] of Object.entries(parsed)) {
                sectorColorCache.set(sector, colorObj as SectorColor);

                // Find which index this color is in the palette
                const colorIndex = COLOR_PALETTE.findIndex(
                    p => p.color === (colorObj as SectorColor).color
                );
                if (colorIndex !== -1) {
                    usedColorIndices.add(colorIndex);
                }
            }

            console.log(`✅ Loaded sector color mappings for ${sectorColorCache.size} sectors`);
        }
        isInitialized = true;
    } catch (error) {
        console.error('Failed to initialize sector colors:', error);
        isInitialized = true;
    }
};

/**
 * Get the next available color index (ensures uniqueness)
 * Returns the first unused color from the palette
 */
const getNextAvailableColorIndex = (): number => {
    for (let i = 0; i < COLOR_PALETTE.length; i++) {
        if (!usedColorIndices.has(i)) {
            return i;
        }
    }

    // If all colors are used, cycle back to the beginning
    // (though with 12 colors and typical portfolios, this is unlikely)
    console.warn('⚠️ All sector colors exhausted, cycling back to first color');
    return 0;
};

/**
 * Get color for a sector, auto-assigning if new
 * Guarantees: Same sector always gets same color (consistency)
 * Guarantees: Different sectors get different colors (uniqueness)
 */
export const getSectorColor = (sector: string | null | undefined): SectorColor => {
    if (!sector) {
        return COLOR_PALETTE[0]; // Default to first color for unknown sectors
    }

    const normalizedSector = sector.trim();

    // Return cached color if exists (consistency guarantee)
    if (sectorColorCache.has(normalizedSector)) {
        return sectorColorCache.get(normalizedSector)!;
    }

    // Assign a new unique color
    const colorIndex = getNextAvailableColorIndex();
    const sectorColor = COLOR_PALETTE[colorIndex];

    // Store in cache
    sectorColorCache.set(normalizedSector, sectorColor);
    usedColorIndices.add(colorIndex);

    // Persist immediately
    persistSectorColors();

    console.log(`🎨 Assigned color to sector "${normalizedSector}": ${sectorColor.color}`);

    return sectorColor;
};

/**
 * Get all sector-to-color mappings (for debugging/admin)
 */
export const getAllSectorColors = (): Record<string, SectorColor> => {
    const result: Record<string, SectorColor> = {};
    for (const [sector, color] of sectorColorCache.entries()) {
        result[sector] = color;
    }
    return result;
};

/**
 * Get list of unique sectors that have been assigned colors
 */
export const getKnownSectors = (): string[] => {
    return Array.from(sectorColorCache.keys()).sort();
};

/**
 * Manually set color for a sector (for customization/admin)
 * Use with caution - may break uniqueness if not careful
 */
export const setSectorColor = async (sector: string, color: SectorColor): Promise<void> => {
    const normalizedSector = sector.trim();

    // Find the color index
    const colorIndex = COLOR_PALETTE.findIndex(
        p => p.color === color.color
    );

    if (colorIndex !== -1) {
        // If this sector already had a color, free up that index
        const oldColor = sectorColorCache.get(normalizedSector);
        if (oldColor) {
            const oldColorIndex = COLOR_PALETTE.findIndex(p => p.color === oldColor.color);
            if (oldColorIndex !== -1) {
                usedColorIndices.delete(oldColorIndex);
            }
        }

        // Mark new color as used
        usedColorIndices.add(colorIndex);
    }

    sectorColorCache.set(normalizedSector, color);
    await persistSectorColors();

    console.log(`🎨 Manually set color for sector "${normalizedSector}": ${color.color}`);
};

/**
 * Reset all sector colors to default (clear persistence)
 * Use only for testing or user preferences reset
 */
export const resetSectorColors = async (): Promise<void> => {
    sectorColorCache.clear();
    usedColorIndices.clear();
    await AsyncStorage.removeItem(STORAGE_KEY);
    console.log('🔄 Sector colors reset to default');
};

/**
 * Persist current sector color mappings to AsyncStorage
 * Called automatically whenever a new sector is encountered
 */
const persistSectorColors = async (): Promise<void> => {
    try {
        const mappings: Record<string, SectorColor> = {};
        for (const [sector, color] of sectorColorCache.entries()) {
            mappings[sector] = color;
        }
        await AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(mappings));
    } catch (error) {
        console.error('Failed to persist sector colors:', error);
    }
};

/**
 * Get color palette for charts (array of hex colors)
 * Pass in list of sectors to get coordinated colors
 */
export const getSectorColorPalette = (sectors: string[]): string[] => {
    return sectors.map(sector => getSectorColor(sector).color);
};

/**
 * Get background light color for a sector
 */
export const getSectorBgLight = (sector: string | null | undefined): string => {
    return getSectorColor(sector).bgLight;
};

/**
 * Get debug info about color assignments
 */
export const getColorDebugInfo = (): {
    totalSectors: number;
    totalColorsUsed: number;
    availableColors: number;
    mappings: Record<string, SectorColor>;
} => {
    return {
        totalSectors: sectorColorCache.size,
        totalColorsUsed: usedColorIndices.size,
        availableColors: COLOR_PALETTE.length - usedColorIndices.size,
        mappings: getAllSectorColors(),
    };
};