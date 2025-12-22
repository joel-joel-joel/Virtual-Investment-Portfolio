# React Navigation Migration Guide

## Completed Setup ✅

The core infrastructure has been set up:

- ✅ `src/navigation/index.tsx` - Complete navigation setup with all routes
- ✅ `App.tsx` - App entry point wrapping the Navigation component
- ✅ `index.js` - Root entry point
- ✅ `package.json` - Updated main entry from "expo-router/entry" to "index.js"
- ✅ `src/screens/auth/LoginScreen.tsx` - Auth screen moved and cleaned
- ✅ `src/components/stock/StockHeaderChart.tsx` - Example of navigation updates (DONE)

## What's Left to Do

### 1. Update 17 More Component Files

Replace `useRouter` hooks with `useNavigation` throughout these files:

#### Pattern 1: Simple Back Button (router.back() → navigation.goBack())

**Files affected:**
- src/components/stock/StockHeaderChart.tsx ✅ (DONE)
- src/components/profile/ProfileScreen.tsx

**Pattern:**
```typescript
// OLD
import { useRouter } from 'expo-router';
const router = useRouter();
router.back();

// NEW
import { useNavigation } from '@react-navigation/native';
const navigation = useNavigation();
navigation.goBack();
```

#### Pattern 2: Navigation with Parameter Passing

These files use `router.push()` with JSON.stringify for parameters.

**Key pattern changes:**

**Before (Expo Router):**
```typescript
router.push({
    pathname: '/stock/[ticker]',
    params: {
        ticker: symbol,
        stock: JSON.stringify(stockData)
    }
});

// Receiving params:
const params = useLocalSearchParams<{ stock: string }>();
const stock = JSON.parse(params.stock);
```

**After (React Navigation):**
```typescript
import { useNavigation } from '@react-navigation/native';
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';
import type { RootStackParamList } from '@/src/navigation';

const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();

// Navigate with object directly (no stringify needed!)
navigation.navigate('StockTicker', { stock: stockData });

// Receiving params:
import { useRoute } from '@react-navigation/native';
import type { RouteProp } from '@react-navigation/native';

const route = useRoute<RouteProp<RootStackParamList, 'StockTicker'>>();
const { stock } = route.params;
```

---

### 2. Files That Need Updating

#### High Priority (Uses router.navigate/push)

1. **src/components/portfolio/HoldingsList.tsx**
   - Usage: `router.push()` with stock data
   - Target: Navigate to StockTicker

2. **src/components/watchlist/WatchlistScreen.tsx**
   - Usage: `router.push()` to stock and transaction screens
   - Targets: StockTicker, BuyTransaction

3. **src/components/search/SearchScreen.tsx**
   - Usage: `router.push()` to stock and transaction screens
   - Targets: StockTicker, BuyTransaction

4. **app/transaction/sell.tsx**
   - Usage: `router.push()` and `router.back()`
   - Targets: Navigate to buy/sell screens

5. **app/transaction/buy.tsx**
   - Usage: `router.push()` and `router.back()`
   - Targets: Navigate to buy/sell screens

#### Medium Priority (Component logic updates)

6. **src/components/home/SuggestedForYou.tsx**
   - Usage: Stock navigation

7. **src/components/home/EarningsCalender.tsx**
   - Usage: Stock navigation

8. **src/components/home/QuickActions.tsx**
   - Usage: Navigation to various screens

#### Lower Priority (useRouter import cleanup)

9. **src/components/settings/SettingsScreen.tsx**
10. **src/components/transaction/TransactionHistory.tsx**
11. **app/stock/[ticker].tsx**
12. **app/transaction/history.tsx**

#### useLocalSearchParams Updates (4 files)

These files use `useLocalSearchParams` to receive route params:

- **app/transaction/sell.tsx** - receives `stock` and `stockId` params
- **app/transaction/buy.tsx** - receives `stock` param
- **app/stock/[ticker].tsx** - receives `stock` and `ticker` params
- **src/components/profile/ProfileScreen.tsx** - if any route params


---

### 3. Step-by-Step Update Instructions

#### For Navigation Calls (router.push → navigation.navigate)

Find all instances of:
```
router.push({ pathname: '/path', params: { ... } })
```

Replace with the navigation route name. Reference the RootStackParamList in `src/navigation/index.tsx` for available routes:
- `MainTabs` - Tab navigator
- `StockTicker` - Stock detail screen
- `BuyTransaction` - Buy transaction screen
- `SellTransaction` - Sell transaction screen
- `TransactionHistory` - Transaction history
- `Settings` - Settings screen
- `Login` - Login screen

#### For Each File Update

1. **Find imports:**
```bash
grep -n "useRouter\|useLocalSearchParams\|router\." <file>
```

2. **Replace imports:**
```typescript
// Remove
import { useRouter, useLocalSearchParams } from 'expo-router';

// Add
import { useNavigation, useRoute } from '@react-navigation/native';
import type { NativeStackNavigationProp, RouteProp } from '@react-navigation/native-stack';
import type { RootStackParamList } from '@/src/navigation';
```

3. **Replace hook usage:**
```typescript
// Remove
const router = useRouter();
const params = useLocalSearchParams();

// Add
const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
const route = useRoute<RouteProp<RootStackParamList, 'ScreenName'>>();
```

4. **Replace navigation calls:**

| Old Pattern | New Pattern |
|---|---|
| `router.push({ pathname: '/stock/[ticker]', params: { stock: JSON.stringify(data) } })` | `navigation.navigate('StockTicker', { stock: data })` |
| `router.push({ pathname: '/transaction/buy', params: { stock: JSON.stringify(data) } })` | `navigation.navigate('BuyTransaction', { stock: data })` |
| `router.back()` | `navigation.goBack()` |
| `router.replace('/(tabs)')` | `navigation.replace('MainTabs')` |

---

### 4. Critical Files to Update First

**Start with these (they're blocking others):**

1. **src/components/portfolio/HoldingsList.tsx** - navigates to StockTicker
2. **src/components/search/SearchScreen.tsx** - navigates to StockTicker and transactions
3. **app/transaction/buy.tsx** - uses useLocalSearchParams
4. **app/transaction/sell.tsx** - uses useLocalSearchParams

**Then update the dependent ones** that can now receive params correctly.

---

### 5. Testing Checklist

After each file update:
- [ ] No TypeScript errors (`npm run lint` or `npx tsc --noEmit`)
- [ ] Navigation to that screen works
- [ ] Parameters are passed correctly
- [ ] Back button works

Full integration test:
- [ ] App starts to Login screen (not authenticated)
- [ ] Login successful → MainTabs
- [ ] Tab navigation works (all 5 tabs clickable)
- [ ] Stock navigation from Holdings/Search/Watchlist works
- [ ] Stock detail screen displays correctly
- [ ] Buy/Sell buttons work from stock detail
- [ ] Transaction screens open with correct data
- [ ] Back button navigates correctly on all screens
- [ ] No "unmatched route" errors anywhere

---

### 6. Cleanup (After All Updates Complete)

Once all components are updated:

1. **Delete old Expo Router files:**
```bash
rm -rf app/_layout.tsx
rm -rf app/((tabs))/_layout.tsx
rm -rf app/auth/_layout.tsx
rm -rf app/auth/login.tsx
# Keep app/transaction and app/stock for now, can move later
```

2. **Create thin wrapper files in src/screens/ (optional):**
If you want to fully clean up the `app/` folder, create wrapper files in `src/screens/` that import from components.

Example: `src/screens/tabs/HomeScreen.tsx`
```typescript
import HomeScreenComponent from '@/app/(tabs)/index';
export default HomeScreenComponent;
```

---

### 7. Automated Update Help

If updating many files, create a script to help with replacements:

```bash
# Replace useRouter import with useNavigation
find src/components -name "*.tsx" -type f -exec sed -i '' \
  "s/import { useRouter } from 'expo-router';/import { useNavigation } from '@react-navigation\/native';/g" {} \;

# Replace router.back() with navigation.goBack()
find src/components -name "*.tsx" -type f -exec sed -i '' \
  "s/router\.back()/navigation.goBack()/g" {} \;

# Replace const router = useRouter() with const navigation = useNavigation()
find src/components -name "*.tsx" -type f -exec sed -i '' \
  "s/const router = useRouter();/const navigation = useNavigation();/g" {} \;
```

---

### 8. Troubleshooting

**Error: "Cannot read property 'navigate' of undefined"**
- Make sure `useNavigation()` is called inside a NavigationContainer context
- All screens must be within the Navigation component (already set up)

**Error: "Unmatched route"**
- Check that your route name matches exactly in RootStackParamList
- Verify NavigationContainer has proper initialRouteName

**Error: "params don't match type"**
- Make sure route types in RootStackParamList match your actual params
- Use TypeScript types from RootStackParamList for type safety

---

## Summary of Navigation Map

```
App.tsx (entry)
  ↓
Navigation (src/navigation/index.tsx)
  ├─ Auth Stack (when not authenticated)
  │   └─ Login → MainTabs (on successful login)
  │
  └─ Main Stack (when authenticated)
      ├─ MainTabs
      │   ├─ Home
      │   ├─ Portfolio
      │   ├─ Search
      │   ├─ Watchlist
      │   └─ Profile
      ├─ StockTicker (from any tab)
      ├─ BuyTransaction (from StockTicker, etc)
      ├─ SellTransaction (from StockTicker, etc)
      ├─ TransactionHistory (from Profile, etc)
      └─ Settings (from Profile)
```

All routes are explicitly defined. No file-based routing.
