/**
 * Dynamic Sector Color Service
 * Automatically assigns consistent colors to sectors from Finnhub
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
 */
let sectorColorCache: Map<string, SectorColor> = new Map();
let colorIndexCounter = 0;
let isInitialized = false;

/**
 * Initialize the sector color system by loading persisted mappings
 */
export const initializeSectorColors = async (): Promise<void> => {
  if (isInitialized) return;

  try {
    const stored = await AsyncStorage.getItem(STORAGE_KEY);
    if (stored) {
      const parsed = JSON.parse(stored);
      sectorColorCache = new Map(Object.entries(parsed));
      // Find the highest used index to continue from there
      colorIndexCounter = Math.max(...Array.from(sectorColorCache.values()).map(() => 1)) || 0;
    }
    isInitialized = true;
  } catch (error) {
    console.error('Failed to initialize sector colors:', error);
    isInitialized = true;
  }
};

/**
 * Get color for a sector, auto-assigning if new
 * Consistent: same sector always gets same color
 */
export const getSectorColor = (sector: string | null | undefined): SectorColor => {
  if (!sector) {
    return COLOR_PALETTE[0]; // Default to first color
  }

  const normalizedSector = sector.trim();

  // Return cached color if exists
  if (sectorColorCache.has(normalizedSector)) {
    return sectorColorCache.get(normalizedSector)!;
  }

  // Assign new color
  const colorIndex = colorIndexCounter % COLOR_PALETTE.length;
  const sectorColor = COLOR_PALETTE[colorIndex];
  sectorColorCache.set(normalizedSector, sectorColor);
  colorIndexCounter++;

  // Persist the new mapping
  persistSectorColors();

  return sectorColor;
};

/**
 * Get all sector-to-color mappings
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
 * Manually set color for a sector (for customization)
 */
export const setSectorColor = async (sector: string, color: SectorColor): Promise<void> => {
  sectorColorCache.set(sector.trim(), color);
  await persistSectorColors();
};

/**
 * Reset all sector colors to default (clear persistence)
 */
export const resetSectorColors = async (): Promise<void> => {
  sectorColorCache.clear();
  colorIndexCounter = 0;
  await AsyncStorage.removeItem(STORAGE_KEY);
};

/**
 * Persist current sector color mappings to AsyncStorage
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
