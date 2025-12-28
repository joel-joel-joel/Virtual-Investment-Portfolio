# Pegasus - Virtual Investment Portfolio Tracker

A production-ready, full-stack mobile application for real-time investment portfolio management, featuring multi-account support, limit order execution, and comprehensive market analytics.

## Project Overview

Pegasus is a sophisticated investment tracking platform that enables users to manage multiple portfolios, execute trades with limit orders, monitor real-time market data, and analyze performance metrics. Built with enterprise-grade architecture and deployed on cloud infrastructure, this project demonstrates end-to-end proficiency in modern full-stack development.

The application integrates live market data from Finnhub and Yahoo Finance APIs, implements automated background processing for order execution and portfolio snapshots, and delivers a responsive mobile experience through React Native.

## Tech Stack

### Frontend
- **Framework:** React Native (Expo SDK 54) with TypeScript
- **Navigation:** React Navigation (Bottom Tabs + Stack Navigator)
- **State Management:** React Context API
- **Security:** Expo SecureStore for encrypted JWT storage
- **Charts:** Victory Native for data visualization
- **HTTP Client:** Axios with interceptor-based authentication

### Backend
- **Framework:** Spring Boot 3.5.6 (Java 17)
- **Architecture:** Spring Modulith (modular monolith)
- **Security:** Spring Security with JWT authentication (BCrypt password hashing)
- **Database Access:** Spring Data JPA + Hibernate
- **Migration:** Flyway (8 migrations)
- **WebSocket:** STOMP protocol for real-time updates
- **Monitoring:** Spring Boot Actuator + Micrometer (Prometheus metrics)
- **API Documentation:** SpringDoc OpenAPI 3 (Swagger)

### Database & Deployment
- **Database:** PostgreSQL 16 (Supabase cloud hosting)
- **Backend Hosting:** Fly.io (London region, Dockerized deployment)
- **Development DB:** Docker Compose
- **Mobile Build:** Expo Go (development) + EAS Build (production-ready)

### External Integrations
- **Finnhub API:** Real-time quotes, company profiles, financial metrics, historical data
- **Yahoo Finance API:** Fallback provider for market data and news
- **BetterStack:** Application logging and monitoring

## Key Features

### Multi-Account Portfolio Management
- Create and manage multiple investment accounts (e.g., Retirement, Trading, Long-term)
- Real-time portfolio valuation and performance tracking
- Account-level aggregation with unrealized/realized gains
- Automated daily portfolio snapshots for historical analysis

### Advanced Trading Capabilities
- **Market Orders:** Instant buy/sell execution
- **Limit Orders:** Automated execution when target price is reached
- Background scheduler checks prices every minute and executes pending orders
- Transaction history with comprehensive filtering
- Accurate cost basis tracking (FIFO/average methods)

### Market Data & Analytics
- Real-time stock quotes (auto-refresh every 30 seconds)
- 12+ fundamental metrics (P/E, EPS, Beta, ROE, Dividend Yield, etc.)
- Historical price charts (1D, 1W, 1M, 3M, 1Y) with OHLC candlesticks
- Sector allocation breakdown with visual analytics
- Earnings calendar and dividend tracking

### Discovery & Research
- Intelligent stock search with autocomplete
- Watchlist management for quick access to favorite stocks
- Financial news feed with stock-specific filtering
- Company profiles with comprehensive business metrics
- Top movers dashboard (portfolio gainers/losers)

### Security & Personalization
- JWT-based authentication with secure token storage
- Role-based authorization (Spring Security)
- Customizable notification preferences (price alerts, portfolio updates, earnings, dividends)
- Dark/Light theme toggle
- User profile management

## Architecture

### System Design
```
┌─────────────────────────────────────────────────────────────┐
│                     Mobile Client (React Native)            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Portfolio UI │  │  Trading UI  │  │  Market Data │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         │                  │                  │             │
│         └──────────────────┴──────────────────┘             │
│                            │                                │
│                    ┌───────▼────────┐                       │
│                    │  Service Layer │                       │
│                    │   (14 services)│                       │
│                    └───────┬────────┘                       │
└────────────────────────────┼──────────────────────────────┘
                             │ HTTPS + JWT
                    ┌────────▼─────────┐
                    │   API Gateway    │
                    │   (Spring Boot)  │
                    └────────┬─────────┘
                             │
         ┌───────────────────┼───────────────────┐
         │                   │                   │
┌────────▼────────┐ ┌────────▼────────┐ ┌────────▼────────┐
│  Controllers    │ │   Schedulers    │ │   WebSocket     │
│  (23 endpoints) │ │  - Limit Orders │ │   - Real-time   │
│  - Auth         │ │  - Snapshots    │ │   - Updates     │
│  - Portfolio    │ │  (every minute) │ │   (STOMP)       │
│  - Trading      │ └─────────────────┘ └─────────────────┘
│  - Market Data  │
└────────┬────────┘
         │
┌────────▼────────┐
│  Services (19+) │
│  - Portfolio    │
│  - Transaction  │
│  - Order Exec   │
│  - API Gateway  │
└────────┬────────┘
         │
┌────────▼────────────────────┐
│  Spring Data JPA/Hibernate  │
└────────┬────────────────────┘
         │
┌────────▼────────┐  ┌───────────────────┐
│   PostgreSQL    │  │  External APIs    │
│   (Supabase)    │  │  - Finnhub        │
│  - 12+ tables   │  │  - Yahoo Finance  │
│  - Flyway       │  │  - Marketaux      │
└─────────────────┘  └───────────────────┘
```

### Data Flow
1. **Authentication:** User credentials → Spring Security → JWT token → SecureStore
2. **Real-time Data:** Finnhub API → Service Layer → REST/WebSocket → Mobile UI
3. **Trading:** UI → Transaction Service → Validation → JPA → PostgreSQL → Account Update
4. **Scheduled Jobs:** Cron Scheduler → Price Check → Order Execution → Notification
5. **Portfolio Sync:** Mobile → REST API → Business Logic → Database → Response

### Database Schema Highlights
- **Users & Accounts:** Multi-account support with role-based access
- **Holdings & Transactions:** Complete trade history with cost basis tracking
- **Orders:** Limit order queue with status management (PENDING/EXECUTED/CANCELLED)
- **Market Data:** Stock metadata, price history, watchlist, price alerts
- **Analytics:** Portfolio snapshots, dividend payments, earnings calendar

## Screenshots

> **Note:** Add screenshots of the following screens to showcase the UI:
> - Dashboard with portfolio overview
> - Stock detail page with charts and metrics
> - Transaction history
> - Multi-account switcher
> - Watchlist management
> - Dark/Light theme comparison

## Setup Instructions

### Prerequisites
- **Backend:** Java 17+, Maven 3.8+, Docker (for local database)
- **Frontend:** Node.js 18+, npm/yarn, Expo CLI
- **Database:** Docker Compose or PostgreSQL 16+
- **APIs:** Finnhub API key (free tier available at https://finnhub.io)

### Backend Setup

1. **Clone the repository**
```bash
git clone <repository-url>
cd "Personal Investment Portfolio Tracker"
```

2. **Configure environment variables**
```bash
cd backend
cp .env.example .env.prod
```

Edit `.env.prod` with your credentials:
```properties
DATABASE_URL=jdbc:postgresql://<supabase-url>:5432/<database-name>
DATABASE_USERNAME=<your-db-username>
DATABASE_PASSWORD=<your-db-password>
APP_JWT_SECRET=<generate-a-secure-256-bit-secret>
FINNHUB_API_KEY=<your-finnhub-api-key>
```

3. **Start the backend**

**Option A: Production mode (recommended)**
```bash
mvn clean package -DskipTests
java -jar target/PersonalInvestmentPortfolioTracker-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

**Option B: Development mode (local database)**
```bash
# Start PostgreSQL via Docker
docker-compose -f compose.migration.yml up -d

# Run Spring Boot
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

4. **Verify backend health**
```bash
curl http://localhost:8080/actuator/health
# Should return: {"status":"UP"}
```

5. **Access API documentation**
```
Open: http://localhost:8080/swagger-ui/index.html
```

### Frontend Setup

1. **Navigate to frontend directory**
```bash
cd frontend
```

2. **Install dependencies**
```bash
npm install
```

3. **Configure API endpoint**

Edit `src/services/configService.ts` to point to your backend:
```typescript
// For local development
export const API_BASE_URL = 'http://localhost:8080';

// For production backend
export const API_BASE_URL = 'https://portfolio-tracker-backend.fly.dev';
```

4. **Start Expo development server**
```bash
npm start
```

5. **Run on device/simulator**

**Expo Go (Development)**
- Install Expo Go app on your iOS/Android device
- Scan the QR code from terminal
- App will load and connect to your backend

**iOS Simulator (Mac only)**
```bash
npm run ios
```

**Android Emulator**
```bash
npm run android
```

### Deployment to Production

**Backend Deployment (Fly.io)**
```bash
cd backend
flyctl deploy
```

**Frontend Deployment (EAS Build for TestFlight/App Store)**

The project is configured for EAS Build with three build profiles:

1. **Development Build**
```bash
eas build --profile development --platform ios
```
- Internal distribution
- Development client
- Fast iteration

2. **Preview Build**
```bash
eas build --profile preview --platform ios
```
- Internal testing
- Production-like environment
- TestFlight ready

3. **Production Build**
```bash
eas build --profile production --platform ios
```
- App Store distribution
- Full optimization
- Release-ready binary

**Note:** Currently configured for Expo Go rapid development. Transition to EAS Build enables:
- Deployment to TestFlight for iOS beta testing
- Submission to App Store and Google Play
- Custom native modules and configurations
- Over-the-air (OTA) updates via EAS Update

## Development Workflow

### Running Tests
```bash
# Backend unit tests
cd backend
mvn test

# Frontend TypeScript compilation check
cd frontend
npx tsc --noEmit
```

### Database Migrations
```bash
# Flyway migrations run automatically on application startup
# Located in: backend/src/main/resources/db/migration/

# Check migration status
mvn flyway:info

# Manually apply migrations (if needed)
mvn flyway:migrate
```

### API Testing
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **Health Check:** http://localhost:8080/actuator/health
- **Metrics:** http://localhost:8080/actuator/prometheus

## Project Structure

```
Personal Investment Portfolio Tracker/
├── backend/
│   ├── src/main/java/com/joelcode/personalinvestmentportfoliotracker/
│   │   ├── controllers/        # 23+ REST controllers
│   │   ├── services/           # Business logic layer
│   │   ├── repositories/       # JPA repositories
│   │   ├── entities/           # JPA entities (12+ tables)
│   │   ├── dto/                # Data Transfer Objects
│   │   ├── config/             # Spring configurations
│   │   └── jwt/                # JWT authentication
│   ├── src/main/resources/
│   │   ├── db/migration/       # Flyway migrations
│   │   └── application.yml     # Configuration
│   ├── Dockerfile              # Production container
│   ├── fly.toml                # Fly.io deployment config
│   └── pom.xml                 # Maven dependencies
│
├── frontend/
│   ├── app/                    # Expo Router screens
│   │   ├── (tabs)/             # Bottom tab navigation
│   │   ├── auth/               # Authentication flow
│   │   ├── account/            # Account management
│   │   └── stock/              # Stock detail pages
│   ├── src/
│   │   ├── components/         # Reusable UI components
│   │   ├── services/           # API service layer (14 services)
│   │   ├── context/            # React Context (Auth, Theme)
│   │   ├── hooks/              # Custom React hooks
│   │   └── types/              # TypeScript definitions
│   ├── eas.json                # EAS Build configuration
│   └── app.json                # Expo configuration
│
└── README.md
```

## Technical Highlights

### Backend Engineering
- **Modular Architecture:** Spring Modulith for clean separation of concerns
- **Automated Processing:** Scheduled tasks for limit order execution and portfolio snapshots
- **Real-time Communication:** WebSocket integration with STOMP protocol
- **API Resilience:** Intelligent fallback from Finnhub to Yahoo Finance on rate limits
- **Security:** JWT authentication, BCrypt hashing, CORS protection, input validation
- **Observability:** Actuator health checks, Prometheus metrics, BetterStack logging
- **Database Versioning:** Flyway migrations for reproducible schema management
- **Cloud-Native:** Dockerized deployment on Fly.io with auto-scaling

### Frontend Engineering
- **Type Safety:** Full TypeScript coverage for compile-time error detection
- **State Management:** Context API for global auth and theme state
- **Secure Storage:** Encrypted JWT token storage via Expo SecureStore
- **Service Architecture:** Clean separation between UI and API logic
- **Navigation:** File-based routing with Expo Router for scalable navigation
- **Error Handling:** Graceful degradation and user-friendly error messages
- **Performance:** Optimized re-renders, lazy loading, efficient list rendering
- **Cross-Platform:** Single codebase for iOS and Android via React Native

### Database Design
- **Normalization:** Proper entity relationships and foreign key constraints
- **Indexing:** Optimized queries for portfolio aggregation and transaction history
- **Audit Trail:** Complete transaction history with timestamps
- **Data Integrity:** Referential integrity and business rule constraints
- **Scalability:** Prepared for read replicas and connection pooling

## Challenges & Solutions

### Challenge 1: Real-time Market Data with API Rate Limits
**Solution:** Implemented intelligent caching and automatic failover mechanism. When Finnhub rate limit is reached, the system seamlessly switches to Yahoo Finance API, ensuring uninterrupted service.

### Challenge 2: Accurate Cost Basis Calculation
**Solution:** Developed transaction processing service that maintains running average cost basis using FIFO methodology, accounting for partial sales and stock splits.

### Challenge 3: Automated Limit Order Execution
**Solution:** Built scheduled background service that polls prices every minute, executes orders when conditions are met, and updates holdings atomically using database transactions.

### Challenge 4: Cross-Platform Mobile Development
**Solution:** Leveraged Expo managed workflow for rapid iteration during development, with EAS Build configuration ready for production deployment to TestFlight and App Stores.

### Challenge 5: Secure Multi-Account Architecture
**Solution:** Designed user-account-holding hierarchy with row-level security through JPA queries, ensuring users can only access their own accounts and transactions.

## Future Enhancements

- [ ] Real-time streaming quotes via WebSocket (currently 30s polling)
- [ ] Options trading support (calls/puts)
- [ ] Advanced charting with technical indicators (RSI, MACD, Bollinger Bands)
- [ ] Portfolio rebalancing recommendations
- [ ] Tax loss harvesting suggestions
- [ ] Social features (share portfolio performance)
- [ ] Push notifications for price alerts
- [ ] Integration with actual brokerage APIs (Alpaca, Interactive Brokers)

## Professional Takeaways

This project demonstrates comprehensive proficiency in:

1. **Full-Stack Development:** End-to-end ownership from database design to mobile UI
2. **Enterprise Architecture:** Modular design, separation of concerns, scalable patterns
3. **Cloud Infrastructure:** Production deployment on modern cloud platforms (Fly.io, Supabase)
4. **API Integration:** Consumption and orchestration of third-party financial APIs
5. **Security Best Practices:** Authentication, authorization, encryption, input validation
6. **Mobile Development:** Cross-platform app development with React Native/Expo
7. **DevOps:** Containerization, CI/CD readiness, monitoring, logging
8. **Database Engineering:** Schema design, migrations, query optimization
9. **Asynchronous Processing:** Scheduled tasks, background jobs, real-time updates
10. **Product Thinking:** User-centric features, error handling, performance optimization

**Why This Project Matters:**

Building a production-grade investment portfolio tracker requires deep understanding of financial domain logic (cost basis, unrealized gains, order execution), real-time data processing, secure authentication, and scalable architecture. This project showcases the ability to:
- Design and implement complex business logic with data integrity
- Integrate multiple external APIs with fault tolerance
- Build secure, user-friendly mobile experiences
- Deploy and operate services in cloud environments
- Make architectural decisions that balance complexity and maintainability

The codebase reflects industry best practices and is structured to evolve from MVP to enterprise-scale platform.

## License

This project is private and intended for portfolio demonstration purposes.

## Author

**Joel ng**
Full-Stack Software Engineer

Github: https://github.com/joel-joel-joel | LinkedIn: https://linkedin.com/in/yourprofile](https://www.linkedin.com/in/joel-ong-2b82a3362/

---

**Built with:**
Spring Boot • React Native • PostgreSQL • Docker • Fly.io • Supabase • Expo • TypeScript • Java
