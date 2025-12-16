# Backend URL Configuration Guide

## Overview

The app now uses a **hostname-based approach** instead of hardcoded IP addresses, making it work across different networks without manual changes.

## How It Works

### Default Configuration
- **Hostname**: `Joels-MacBook-Pro.local`
- **Port**: `8080`
- **Full URL**: `http://Joels-MacBook-Pro.local:8080`

The hostname is automatically broadcast by macOS via Bonjour (mDNS), so it works on any network your machine connects to.

### What Happens When the App Starts

1. The app reads `EXPO_PUBLIC_API_BASE_URL` from `.env` (hostname-based)
2. On first API call, it caches the resolved URL
3. If you manually set a custom URL, it uses that instead
4. All subsequent requests use the cached URL for performance

## Changing the Backend URL

### Option 1: Change the Environment File (Easiest)
Edit `.env` or `.env.development`:
```
EXPO_PUBLIC_API_BASE_URL=http://your-custom-url:8080
```

### Option 2: Programmatically at Runtime
If the hostname doesn't resolve on your network, you can set a custom URL:

```typescript
import { setBackendUrl, clearBaseUrlCache } from '@/src/services/configService';
import { clearBaseUrlCache } from '@/src/services/api';

// Set a custom backend URL
await setBackendUrl('http://192.168.1.100:8080');

// Clear cache to use new URL immediately
clearBaseUrlCache();
```

### Option 3: Check Current Configuration
```typescript
import {
  getBackendUrl,
  getDefaultBackendUrl,
  hasCustomBackendUrl
} from '@/src/services/configService';

// Get current URL (respects custom override if set)
const currentUrl = await getBackendUrl();
console.log('Current backend URL:', currentUrl);

// Get default URL from env
const defaultUrl = getDefaultBackendUrl();
console.log('Default backend URL:', defaultUrl);

// Check if custom URL is set
const hasCustom = await hasCustomBackendUrl();
console.log('Using custom URL:', hasCustom);
```

## Troubleshooting

### "Network timeout" errors
- **Problem**: Hostname doesn't resolve on your network
- **Solution**: Either:
  1. Use your machine's IP address instead (check with `ifconfig | grep inet`)
  2. Add your machine to your network's DNS
  3. Use the programmatic method above to set a custom IP

### "Invalid URL" errors
- **Problem**: URL format is incorrect
- **Solution**: Ensure URL starts with `http://` or `https://`

### Backend is running but app can't connect
- **Check 1**: Backend is listening on port 8080
  ```bash
  lsof -i :8080
  ```
- **Check 2**: Both devices are on the same network
- **Check 3**: Firewall isn't blocking port 8080
- **Check 4**: Try pinging the hostname
  ```bash
  ping Joels-MacBook-Pro.local
  ```

## Environment Files

- `.env` - Default environment (used by `expo start`)
- `.env.development` - Development overrides
- `.env.production` - Production configuration
- `.env.staging` - Staging configuration

## Related Services

- `configService.ts` - Manages backend URL configuration
- `api.ts` - HTTP client that uses the configured URL
- `authService.ts` - Uses the API client for authentication
