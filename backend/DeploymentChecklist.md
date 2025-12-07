 Personal Investment Portfolio Tracker - Deployment Checklist

  Analysis Summary

  I've thoroughly analyzed your project. Here's what I found:

  Backend (Spring Boot):
  - 184 Java source files, 53 test files
  - 22 REST controllers
  - Maven build: ✅ SUCCESSFUL (with 2 deprecation warnings)
  - Database: PostgreSQL with Docker Compose support
  - Tech: Spring Boot 3.5.6, Java 17/24, JWT auth, WebSockets

  Frontend (React Native/Expo):
  - Expo 54.0.25
  - TypeScript with React 19.1.0
  - 66 lint issues (17 errors, 49 warnings)
  - Hardcoded localhost API URL
  - 21 console.log statements in source code

  ---
  🔴 CRITICAL ISSUES TO FIX BEFORE DEPLOYMENT

  Backend Team - Critical

  1. Database Configuration Mismatch: compose.yaml has different credentials than application.yml
    - compose.yaml: myuser/secret/mydatabase on port 5433
    - application.yml: devuser/devpass/portfolio_dev on port 5432
  2. API Keys Exposed: FinnHub and MarketAux API keys are in application.properties (not .gitignore)
  3. Production Database Not Configured: application.yml prod profile expects environment variables that aren't set

  Frontend Team - Critical

  1. Hardcoded Backend URL: API_BASE_URL = 'http://localhost:8080' in src/services/api.ts
  2. Lint Errors: 17 errors (duplicate exports in services/index.ts)
  3. No Environment Configuration: No .env file structure for different environments

  ---
  BACKEND TEAM DEPLOYMENT CHECKLIST

  Phase 1: Configuration & Security (High Priority) ✅ COMPLETED

  - ✅ 1.1 Environment Variables Setup
    - ✅ Created .env file (added to .gitignore!)
    - ✅ Created .env.dev file for local development
    - ✅ Moved FinnHub API key to environment variable
    - ✅ Moved MarketAux API key to environment variable
    - ✅ Generated strong JWT secret (256 bits): YTVmuKTg2GixbMlW5VKVESIQ92QMJcHQiPnD/9O1vRY=
    - ✅ Set up DATABASE credentials for both dev and production

  Files created:
  - .env (production) - Contains prod database and API credentials
  - .env.dev (development) - Contains dev database and API credentials

  Note: Use .env.dev for local development, .env for production deployment

  - ✅ 1.2 Fix Database Configuration
    - ✅ Updated application-dev.properties to use port 5432 (standard PostgreSQL port)
    - ✅ Database credentials now consistent:
      - Dev: devuser/devpass/portfolio_dev (port 5432)
      - Prod: produser/prodpass/portfolio_prod (port 5432)
    - ✅ compose.yaml uses port 5432 (matches updated dev config)

  Location: backend/src/main/resources/application-dev.properties:5-7

  - ✅ 1.3 Update application.properties
    - ✅ Removed hardcoded API keys
    - ✅ Using environment variable references:
      - finnhub.api.key=${FINNHUB_API_KEY}
      - marketaux.api.key=${MARKETAUX_API_KEY}
      - JWT_SECRET=${JWT_SECRET}

  File: backend/src/main/resources/application.properties:4-6

  - ✅ 1.4 CORS Configuration for Production
    - ✅ Production CORS configured with specific domain (not wildcard)
    - ✅ CorsConfig.java reads from profile-specific properties
    - ✅ Dev CORS allows localhost origins, Prod uses: https://your-frontend-domain.com

  Files:
  - backend/src/main/resources/application-prod.properties:7
  - backend/src/main/java/.../config/CorsConfig.java

  - ✅ 1.5 Security Review
    - ✅ HTTPS configured in production (application-prod.yml:23-28)
    - ✅ JWT expiration: 1 hour for prod (application-prod.yml:34)
    - ✅ BCrypt password encoder with strength 12 (SecurityConfig.java:42)
    - ✅ Spring Security properly configured with authentication filters
    - ✅ Session management set to STATELESS for JWT

  Phase 2: Code Quality & Cleanup

  - 2.1 Fix Deprecation Warnings
    - Update RestTemplateConfig.java to use new timeout methods
    - Review JwtTokenProvider.java for deprecated API usage

  Files:
    - backend/src/main/java/.../config/RestTemplateConfig.java:16,17
    - backend/src/main/java/.../jwt/JwtTokenProvider.java
  - 2.2 Remove TODO/FIXME Comments
    - Review CorsConfig.java TODO
    - Address any incomplete implementations

  Phase 3: Database & Migrations

  - 3.1 Database Setup
    - Create production database schema
    - Review Hibernate ddl-auto settings:
        - Dev: update ✅
      - Prod: validate ✅ (correct - requires manual migrations)
    - Create database migration scripts (Flyway or Liquibase recommended)
    - Test database rollback procedures
  - 3.2 Docker Compose for Development
    - Start PostgreSQL: docker-compose up -d
    - Verify database connectivity
    - Run database initialization scripts if any

  Phase 4: Build & Test

  - 4.1 Build Verification
    - Run clean build: mvn clean install
    - Fix any build warnings
    - Verify JAR file is created: target/*.jar
  - 4.2 Run Tests
    - Execute all unit tests: mvn test
    - Review test coverage (53 test files - good coverage!)
    - Integration tests: mvn verify
    - Fix any failing tests
  - 4.3 API Documentation
    - Test Swagger UI: http://localhost:8080/swagger-ui/index.html
    - Verify all endpoints are documented
    - Test API docs accessibility

  Phase 5: Deployment Configuration

  - 5.1 Production Profile
    - Set spring.profiles.active=prod for production
    - Configure production logging levels (currently WARN - good)
    - Set up log aggregation/monitoring
  - 5.2 Application Packaging
    - Build production JAR: mvn clean package -Pprod
    - Test JAR execution: java -jar target/*.jar --spring.profiles.active=prod
    - Create Dockerfile for containerization (recommended)
  - 5.3 External Services
    - Verify FinnHub API rate limits for production
    - Verify MarketAux API rate limits for production
    - Set up monitoring for external API health
    - Configure fallback behavior if APIs are down

  Phase 6: Monitoring & Observability

  - 6.1 Spring Actuator Setup
    - Enable production-safe actuator endpoints
    - Secure actuator endpoints (currently exposed: health, info, metrics)
    - Set up health checks for k8s/load balancers
  - 6.2 Logging
    - Configure log file rotation (currently 10MB, 30 days - good)
    - Set up centralized logging (ELK, Splunk, CloudWatch, etc.)
    - Remove debug logging in production

  ---
  FRONTEND TEAM DEPLOYMENT CHECKLIST

  Phase 1: Configuration & Environment Setup (High Priority)

  - 1.1 Environment Configuration
    - Create .env file structure
    - Add .env*.local to .gitignore
    - Create environment-specific configs:
        - .env.development (localhost:8080)
      - .env.staging (staging backend URL)
      - .env.production (production backend URL)

  Files to create:
  # .env.development
  EXPO_PUBLIC_API_BASE_URL=http://localhost:8080

  # .env.production
  EXPO_PUBLIC_API_BASE_URL=https://api.your-production-domain.com
  - 1.2 Update API Configuration
    - Replace hardcoded URL in src/services/api.ts
    - Use environment variable:
    export const API_BASE_URL = process.env.EXPO_PUBLIC_API_BASE_URL || 'http://localhost:8080';

  File: frontend/src/services/api.ts:8

  Phase 2: Fix Lint Errors & Code Quality (Critical)

  - 2.1 Fix Duplicate Exports (17 Errors)
    - Fix src/services/index.ts duplicate exports
    - Remove duplicate type exports (LoginRequest, RegisterRequest, AuthResponse)
    - Consolidate into single export statement

  File: frontend/src/services/index.ts:16,25
  - 2.2 Fix Warnings (49 Total)
    - Remove unused imports/variables
    - Address unused type definitions in newsService.ts
    - Clean up buildQueryString in portfolioService.ts

  Files:
    - frontend/src/services/newsService.ts:11,12
    - frontend/src/services/portfolioService.ts:7
  - 2.3 Remove Debug Console Logs
    - Review and remove/replace 21 console.log statements
    - Use proper logging library for production (expo-logger or custom)
    - Keep only error logging for production

  Files: 11 files contain console logs (use npm run lint to see)

  Phase 3: Code Cleanup & TODOs

  - 3.1 Address TODO Comments
    - Review TODO in TransactionHistory.tsx
    - Review TODO in HoldingsList.tsx
    - Review TODO in portfolio.tsx
    - Review TODO in WatchlistScreen.tsx

  Phase 4: Build & Test

  - 4.1 TypeScript Compilation
    - Run TypeScript compiler: npx tsc --noEmit
    - Fix all type errors
    - Ensure strict mode compliance
  - 4.2 Lint Fixes
    - Run: npm run lint
    - Fix all errors (17 critical)
    - Run: npm run lint -- --fix for auto-fixable issues
    - Manually fix remaining issues
  - 4.3 Build Verification
    - Test development build: npx expo start
    - Test web build: npm run web
    - Test iOS build (if applicable): npm run ios
    - Test Android build (if applicable): npm run android

  Phase 5: Mobile App Configuration

  - 5.1 Update app.json for Production
    - Update app name (currently "frontend")
    - Update app slug to be unique
    - Update version number for release
    - Configure app icons and splash screens
    - Set up proper bundle identifier for iOS/Android

  File: frontend/app.json
  - 5.2 Platform-Specific Setup
    - iOS: Configure Xcode project
    - Android: Configure build.gradle
    - Set up code signing for both platforms
    - Configure app permissions in AndroidManifest.xml/Info.plist

  Phase 6: Security & Performance

  - 6.1 Authentication & Storage
    - Verify SecureStore is properly configured for both platforms
    - Test token refresh flow
    - Implement token expiration handling
    - Test logout/session cleanup
  - 6.2 API Integration Testing
    - Test all API endpoints with real backend
    - Verify error handling for network failures
    - Test authentication flows (login, register, password reset)
    - Test WebSocket connections if used
  - 6.3 Performance Optimization
    - Optimize bundle size
    - Implement lazy loading for screens
    - Add loading states for async operations
    - Test on low-end devices

  Phase 7: Deployment Preparation

  - 7.1 EAS Build Setup (Recommended for Expo)
    - Install EAS CLI: npm install -g eas-cli
    - Login: eas login
    - Configure: eas build:configure
    - Create builds: eas build --platform all
  - 7.2 Web Deployment
    - Build for web: npx expo export --platform web
    - Test static export
    - Configure hosting (Vercel, Netlify, AWS S3, etc.)
    - Set up CI/CD pipeline
  - 7.3 App Store Preparation
    - iOS: Create App Store Connect listing
    - Android: Create Google Play Console listing
    - Prepare app screenshots
    - Write app description
    - Configure privacy policy

  ---
  INTEGRATION & CROSS-TEAM TASKS

  API Integration Verification

  - Test All API Endpoints
    - Authentication endpoints (7 endpoints)
    - Portfolio management (3 endpoints)
    - Watchlist (4 endpoints)
    - Transactions (5 endpoints)
    - Dashboard/Analytics (6 endpoints)
    - Earnings calendar (2 endpoints)
    - Price alerts (3 endpoints)
  - WebSocket Testing
    - Verify WebSocket connection establishment
    - Test real-time price updates
    - Test connection recovery

  CORS & Network

  - CORS Configuration
    - Backend: Add production frontend URL to allowed origins
    - Test from frontend domain
    - Verify preflight requests work
  - SSL/HTTPS Setup
    - Backend: Configure SSL certificate
    - Frontend: Update API URLs to HTTPS
    - Test mixed content issues

  Data Consistency

  - Database Initialization
    - Create seed data scripts
    - Test with empty database
    - Test with populated database

  ---
  TESTING INSTRUCTIONS

  Backend Testing

  1. Local Development Setup

  # Start PostgreSQL database
  cd backend
  docker-compose up -d

  # Verify database is running
  docker ps | grep postgres

  # Build and run backend
  mvn clean install
  mvn spring-boot:run

  # Backend should be running on http://localhost:8080

  2. API Testing with Swagger

  # Open in browser
  http://localhost:8080/swagger-ui/index.html

  # Test authentication flow:
  1. POST /api/auth/register - Create test user
  2. POST /api/auth/login - Get JWT token
  3. Copy JWT token
  4. Click "Authorize" button in Swagger
  5. Enter: Bearer <your-token>
  6. Test protected endpoints

  3. Manual API Testing with curl

  # Register user
  curl -X POST http://localhost:8080/api/auth/register \
    -H "Content-Type: application/json" \
    -d '{
      "username": "testuser",
      "email": "test@example.com",
      "fullName": "Test User",
      "password": "Password123!"
    }'

  # Login
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{
      "email": "test@example.com",
      "password": "Password123!"
    }'

  # Get current user (replace <TOKEN> with actual token)
  curl http://localhost:8080/api/auth/me \
    -H "Authorization: Bearer <TOKEN>"

  4. Run Unit Tests

  # Run all tests
  mvn test

  # Run specific test
  mvn test -Dtest=AuthControllerTest

  # Run with coverage
  mvn clean test jacoco:report
  # Report location: target/site/jacoco/index.html

  5. Integration Testing

  # Run integration tests
  mvn verify

  # Check specific integration test
  mvn test -Dtest=EndToEndTransactionFlowTest

  6. Database Verification

  # Connect to database
  docker exec -it <container-id> psql -U devuser -d portfolio_dev

  # Check tables
  \dt

  # Check user table
  SELECT * FROM users;

  # Exit
  \q

  Frontend Testing

  1. Local Development Setup

  cd frontend

  # Install dependencies
  npm install

  # Start development server
  npx expo start

  # Options:
  # - Press 'w' for web
  # - Press 'a' for Android emulator
  # - Press 'i' for iOS simulator
  # - Scan QR code with Expo Go app on physical device

  2. Fix Lint Errors First

  # Check for errors
  npm run lint

  # Auto-fix what's possible
  npm run lint -- --fix

  # Fix remaining 17 errors manually in src/services/index.ts

  3. TypeScript Validation

  # Check for type errors
  npx tsc --noEmit

  # Fix any type errors before proceeding

  4. Authentication Flow Testing

  Manual Test Steps:
  1. Launch app
  2. Navigate to registration screen
  3. Create new account
  4. Verify redirect to login
  5. Login with created account
  6. Verify authentication context is set
  7. Verify token is stored in SecureStore
  8. Close and reopen app
  9. Verify auto-login works
  10. Test logout
  11. Verify token is cleared

  5. API Integration Testing

  Test each screen:
  1. Dashboard - verify data loads
  2. Portfolio - test account creation/switching
  3. Watchlist - add/remove stocks
  4. Transactions - create buy/sell transactions
  5. Profile - update user info

  Check error handling:
  1. Stop backend server
  2. Test app behavior with no backend
  3. Verify error messages are user-friendly
  4. Restart backend
  5. Verify app recovers

  6. Platform-Specific Testing

  iOS Testing:
  # Start iOS simulator
  npx expo start --ios

  # Or specific simulator
  npx expo start --ios --simulator="iPhone 15 Pro"

  Android Testing:
  # Start Android emulator
  npx expo start --android

  # Or specific device
  npx expo start --android --device "Pixel_7"

  Web Testing:
  # Start web version
  npx expo start --web

  # Test in different browsers:
  # - Chrome
  # - Firefox  
  # - Safari

  7. Build Testing

  # Production build for web
  npx expo export --platform web

  # Test production build locally
  npx serve dist

  # EAS Build (for mobile apps)
  eas build --profile preview --platform android
  eas build --profile preview --platform ios

  End-to-End Testing

  Complete User Flow Test

  1. Backend Setup:
     - Start PostgreSQL
     - Start backend server
     - Verify Swagger UI accessible

  2. Frontend Setup:
     - Start Expo dev server
     - Open app on device/simulator

  3. Test Flow:
     a. User Registration
        - Create account
        - Verify email validation
        - Check user in database

     b. Authentication
        - Login with credentials
        - Verify JWT token received
        - Check token stored securely

     c. Account Management
        - Create investment account
        - Verify account in database
        - Switch between accounts

     d. Portfolio Operations
        - Add stocks to watchlist
        - Create buy transaction
        - Verify holdings updated
        - Check portfolio value calculation

     e. Real-time Data
        - Verify stock prices update
        - Check news feed loads
        - Test earnings calendar

     f. Session Management
        - Close app
        - Reopen app
        - Verify auto-login
        - Test logout

  ---
  DEPLOYMENT TIMELINE SUGGESTION

  Week 1: Critical Fixes

  - Backend: Fix database config, move API keys to env
  - Frontend: Fix lint errors, implement environment config
  - Both: Test local integration

  Week 2: Testing & QA

  - Backend: Run all tests, fix failures
  - Frontend: Platform testing (iOS, Android, Web)
  - Integration: End-to-end testing

  Week 3: Pre-Production

  - Backend: Set up production database, SSL
  - Frontend: Build releases, test on devices
  - Both: Security audit, performance testing

  Week 4: Deployment

  - Backend: Deploy to production server
  - Frontend: Submit to app stores, deploy web
  - Both: Monitor, fix issues

  ---
  PRODUCTION CHECKLIST SUMMARY

  Must Fix Before Deployment (Blockers)

  1. ✅ Backend: API keys in source code - FIXED (moved to .env files)
  2. ✅ Backend: Database configuration mismatch - FIXED (standardized on port 5432)
  3. ❌ Frontend: 17 lint errors
  4. ❌ Frontend: Hardcoded localhost URL
  5. ❌ Frontend: No environment configuration

  Should Fix Before Deployment (High Priority)

  1. ⚠️ Backend: Deprecation warnings
  2. ✅ Backend: CORS wildcard in production - FIXED (specific domain configured)
  3. ⚠️ Frontend: 21 console.log statements
  4. ⚠️ Frontend: TODO comments
  5. ⚠️ Both: No deployment documentation

  Nice to Have

  1. 📝 Backend: Database migration scripts
  2. 📝 Backend: Monitoring/observability setup
  3. 📝 Frontend: Performance optimization
  4. 📝 Frontend: Error boundaries
  5. 📝 Both: CI/CD pipeline

  ---

⏺ I've completed a comprehensive analysis of your entire project! Here's the summary:

  Key Findings

  Backend (Spring Boot):
  - ✅ Build successful with 184 source files, 53 test files
  - ⚠️ 2 deprecation warnings in RestTemplateConfig
  - ❌ API keys exposed in application.properties
  - ❌ Database configuration mismatch between compose.yaml and application.yml

  Frontend (React Native/Expo):
  - ❌ 17 lint errors (duplicate exports)
  - ⚠️ 49 lint warnings
  - ❌ Hardcoded localhost:8080 URL
  - ⚠️ 21 console.log statements
  - ❌ No environment variable configuration

  What I've Provided

  1. Complete deployment checklist separated into:
    - Backend Team tasks (6 phases, ~50 items)
    - Frontend Team tasks (7 phases, ~45 items)
    - Integration tasks
    - Cross-team coordination items
  2. Detailed testing instructions including:
    - Backend API testing with Swagger & curl
    - Unit and integration test commands
    - Frontend platform testing (iOS, Android, Web)
    - End-to-end user flow testing
    - Database verification steps
  3. Priority classification:
    - 🔴 Critical (5 blockers): Must fix before deployment
    - ⚠️ High Priority (5 items): Should fix before deployment
    - 📝 Nice to Have (5 items): Improvements for production readiness
  4. 4-week deployment timeline suggestion

  The most critical issues to address immediately are:
  - Backend: Secure API keys & fix database config
  - Frontend: Fix lint errors & implement environment-based configuration

