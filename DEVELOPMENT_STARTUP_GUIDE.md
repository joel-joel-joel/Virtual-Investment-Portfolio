# Development Startup Guide
## Personal Investment Portfolio Tracker

This guide provides step-by-step instructions to run the complete application locally using IntelliJ IDEA (backend) and WebStorm (frontend).

---

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Backend Setup (IntelliJ IDEA)](#backend-setup-intellij-idea)
3. [Frontend Setup (WebStorm)](#frontend-setup-webstorm)
4. [Verification](#verification)
5. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software
- **Java 17** or higher
- **Docker Desktop** (for PostgreSQL database)
- **Node.js 18+** and **npm**
- **IntelliJ IDEA** (for backend)
- **WebStorm** (for frontend)
- **Maven** (usually bundled with IntelliJ)

### Environment Variables
Create a file `backend/src/main/resources/application-secrets.properties` with:
```properties
FINNHUB_API_KEY=your_finnhub_api_key_here
MARKETAUX_API_KEY=your_marketaux_api_key_here
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production-min-256-bits
```

---

## Backend Setup (IntelliJ IDEA)

### Step 1: Start PostgreSQL Database with Docker

Open **Terminal** (in IntelliJ or your system terminal) and navigate to the backend directory:

```bash
cd /Users/joelong/Documents/SWE-Projects/Personal\ Investment\ Portfolio\ Tracker/backend
```

Start the PostgreSQL database using Docker Compose:

```bash
docker-compose -f compose.migration.yml up -d
```

**What this does:**
- Starts PostgreSQL 16 on port **5433** (not 5432 to avoid conflicts)
- Creates database: `portfolio_dev`
- Username: `devuser`
- Password: `devpass`

**Verify Docker is running:**
```bash
docker ps```

You should see `portfolio_postgres_migration_test` container running.

---

### Step 2: Configure IntelliJ Run Configuration

#### Option A: Using IntelliJ's Run Configuration (Recommended)

1. **Open the Backend Project in IntelliJ**
   - File → Open → Select the `backend` folder

2. **Create/Edit Run Configuration**
   - Click **Run** → **Edit Configurations**
   - Click **+** → **Spring Boot**
   - Configure as follows:
     - **Name:** `Portfolio Tracker - Dev`
     - **Main class:** `com.joelcode.personalinvestmentportfoliotracker.PersonalInvestmentPortfolioTrackerApplication`
     - **Active profiles:** `dev`
     - **VM options:** (Optional) `-Xmx512m`
     - **Environment variables:**
       ```
       FINNHUB_API_KEY=your_key_here;MARKETAUX_API_KEY=your_key_here
       ```
   - Click **Apply** → **OK**

3. **Run the Application**
   - Click the **green Run button** (▶️) or press `Ctrl+R` (Mac) / `Shift+F10` (Windows)

#### Option B: Using Maven Command in Terminal

In IntelliJ's **Terminal** tab (bottom):

```bash
cd /Users/joelong/Documents/SWE-Projects/Personal\ Investment\ Portfolio\ Tracker/backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

### Step 3: Verify Backend is Running

**Expected Console Output:**
```
Started PersonalInvestmentPortfolioTrackerApplication in X.XXX seconds
Tomcat started on port 8080
```

**Test the API:**

Open browser and navigate to:
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **Health Check:** http://localhost:8080/actuator/health

**Expected Response (Health Check):**
```json
{
  "status": "UP"
}
```

---

## Frontend Setup (WebStorm)

### Step 1: Install Dependencies

Open **WebStorm** and open the frontend project:
- File → Open → Select `/Users/joelong/Documents/SWE-Projects/Personal Investment Portfolio Tracker/frontend`

Open the **Terminal** in WebStorm and run:

```bash
npm install
```

---

### Step 2: Configure Environment

Verify the `.env.development` file exists with:
```
EXPO_PUBLIC_API_BASE_URL=http://localhost:8080
```

This is already configured in your project at:
`frontend/.env.development`

---

### Step 3: Start the Development Server

In WebStorm's **Terminal** tab, run:

```bash
npm start
```

**Alternative commands:**
- **Web only:** `npm run web`
- **iOS Simulator:** `npm run ios`
- **Android Emulator:** `npm run android`

---

### Step 4: Open the Application

After running `npm start`, Expo will show you options:

```
› Press w │ open web
› Press a │ open Android
› Press i │ open iOS simulator
```

**For Web Development:**
- Press **`w`** or navigate to: http://localhost:8081

**For Mobile Development:**
- Install **Expo Go** app on your phone
- Scan the QR code shown in the terminal

---

## Verification

### Complete System Check

#### 1. Backend Health
```bash
curl http://localhost:8080/actuator/health
```
Expected: `{"status":"UP"}`

#### 2. Database Connection
```bash
docker exec -it portfolio_postgres_migration_test psql -U devuser -d portfolio_dev -c "\dt"
```
Expected: List of database tables

#### 3. Frontend Running
Navigate to: http://localhost:8081
Expected: Login/Registration screen loads

#### 4. Frontend-Backend Connection
Try registering a new account in the frontend. If it succeeds or shows a specific error (not network error), the connection works.

---

## Troubleshooting

### Backend Issues

#### Problem: Port 8080 already in use
**Solution:**
```bash
# Find process using port 8080
lsof -ti:8080
# Kill the process
kill -9 <PID>
```

#### Problem: Database connection failed
**Solution:**
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Restart PostgreSQL
docker-compose -f compose.migration.yml down
docker-compose -f compose.migration.yml up -d

# Check logs
docker logs portfolio_postgres_migration_test
```

#### Problem: Flyway migration errors
**Solution:**
```bash
# Reset database
docker-compose -f compose.migration.yml down -v
docker-compose -f compose.migration.yml up -d

# Restart Spring Boot application
```

### Frontend Issues

#### Problem: Metro bundler cache issues
**Solution:**
```bash
# Clear cache and restart
npx expo start -c
```

#### Problem: Cannot connect to backend
**Solution:**
1. Verify backend is running: http://localhost:8080/actuator/health
2. Check `.env.development` has correct URL
3. Restart frontend: `Ctrl+C` then `npm start`

#### Problem: Port 8081 already in use
**Solution:**
```bash
# Kill process on port 8081
lsof -ti:8081 | xargs kill -9

# Or specify different port
npx expo start --port 8082
```

---

## Quick Reference Commands

### Start Everything (Run in Order)

**Terminal 1 - Docker:**
```bash
cd /Users/joelong/Documents/SWE-Projects/Personal\ Investment\ Portfolio\ Tracker/backend
docker-compose -f compose.migration.yml up -d
```

**Terminal 2 - Backend (IntelliJ):**
- Use the Run Configuration (green play button)
- OR: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`

**Terminal 3 - Frontend (WebStorm):**
```bash
cd /Users/joelong/Documents/SWE-Projects/Personal\ Investment\ Portfolio\ Tracker/frontend
npm start
```

### Stop Everything

**Stop Frontend:**
- Press `Ctrl+C` in WebStorm terminal

**Stop Backend:**
- Click the **red Stop button** (■) in IntelliJ
- OR: `Ctrl+C` in terminal

**Stop Docker:**
```bash
cd /Users/joelong/Documents/SWE-Projects/Personal\ Investment\ Portfolio\ Tracker/backend
docker-compose -f compose.migration.yml down
```

---

## Application URLs

| Service | URL | Description |
|---------|-----|-------------|
| Frontend (Web) | http://localhost:8081 | React Native Web UI |
| Backend API | http://localhost:8080 | Spring Boot REST API |
| Swagger UI | http://localhost:8080/swagger-ui/index.html | API Documentation |
| Actuator Health | http://localhost:8080/actuator/health | Health Check |
| PostgreSQL | localhost:5433 | Database (user: devuser, pass: devpass) |

---

## Database Access

### Using psql (Command Line)
```bash
docker exec -it portfolio_postgres_migration_test psql -U devuser -d portfolio_dev
```

### Using DataGrip / DBeaver / pgAdmin
- **Host:** localhost
- **Port:** 5433
- **Database:** portfolio_dev
- **Username:** devuser
- **Password:** devpass

---

## Configuration Details

### Backend Configuration
- **Profile:** `dev`
- **Port:** 8080
- **Database:** PostgreSQL on port 5433
- **Config File:** `backend/src/main/resources/application.yml`

### Frontend Configuration
- **Port:** 8081 (Expo default)
- **API Base URL:** http://localhost:8080
- **Environment File:** `frontend/.env.development`

---

## Notes

1. **Always start Docker first** before running the backend
2. **Backend must be running** before testing frontend API calls
3. **Frontend can run independently** for UI development
4. **Database persists** between restarts (use `docker-compose down -v` to reset)
5. **Hot reload enabled** for both frontend (Expo) and backend (Spring DevTools)
6. **JWT Filter is disabled in dev mode** - This allows authentication to work without requiring tokens for public endpoints
7. **After making backend changes**, restart the Spring Boot application in IntelliJ to see the changes

---

## Development Workflow

### Daily Startup
1. Start Docker: `docker-compose -f compose.migration.yml up -d`
2. Start Backend in IntelliJ (click Run)
3. Start Frontend in WebStorm: `npm start`
4. Open http://localhost:8081

### Daily Shutdown
1. Stop Frontend: `Ctrl+C` in WebStorm
2. Stop Backend: Click Stop in IntelliJ
3. Stop Docker: `docker-compose -f compose.migration.yml down` (optional - can leave running)

---

## Additional Resources

- **Spring Boot Docs:** https://docs.spring.io/spring-boot/docs/current/reference/html/
- **Expo Docs:** https://docs.expo.dev/
- **PostgreSQL Docs:** https://www.postgresql.org/docs/16/



