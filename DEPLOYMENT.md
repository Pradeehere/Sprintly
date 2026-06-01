# Sprintly — Deployment Guide

## Architecture

| Layer    | Tech                        | Hosting              |
|----------|-----------------------------|----------------------|
| Database | PostgreSQL 15               | Neon (free tier)     |
| Backend  | Spring Boot 3.4 / Java 23   | Render (free tier)   |
| Frontend | Vite + React 19 / TypeScript | Vercel (free tier)   |

---

## Step 1 — Database (Neon)

1. Go to [neon.tech](https://neon.tech) and create a free account.
2. Create a new project and database (e.g., `sprintly`).
3. From the Neon console, copy the **Connection String** (JDBC format):
   ```
   jdbc:postgresql://<host>/sprintly?sslmode=require
   ```
4. Note the **username** and **password**.
5. Run the schema SQL against your Neon database:
   - Use the Neon SQL editor or `psql` with your connection string.
   - Execute `c:\Sprintly\backend\src\main\resources\schema.sql`.

---

## Step 2 — Backend (Render)

1. Push your code to GitHub (include the `backend/` folder).
2. Go to [render.com](https://render.com) and create a **Web Service**.
3. Connect your GitHub repo and set the **Root Directory** to `backend`.
4. Set the **Build Command**:
   ```
   mvn package -DskipTests
   ```
5. Set the **Start Command**:
   ```
   java -jar target/backend-0.0.1-SNAPSHOT.jar
   ```
6. Add the following **Environment Variables**:

| Key | Value |
|-----|-------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<neon-host>/sprintly?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | Your Neon username |
| `SPRING_DATASOURCE_PASSWORD` | Your Neon password |
| `JWT_SECRET` | A long random string (min 64 chars) |
| `JWT_EXPIRATION` | `3600000` |
| `JWT_REFRESH_EXPIRATION` | `604800000` |
| `SPRINTLY_CORS_ALLOWED_ORIGINS` | Your Vercel frontend URL (e.g., `https://sprintly.vercel.app`) |

7. Deploy. Once deployed, copy the **Service URL** (e.g., `https://sprintly-api.onrender.com`).

---

## Step 3 — Frontend (Vercel)

1. Go to [vercel.com](https://vercel.com) and create a new project from your GitHub repo.
2. Set the **Root Directory** to `frontend`.
3. Vercel auto-detects Vite. Confirm:
   - **Framework**: Vite
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
4. Add the following **Environment Variable**:

| Key | Value |
|-----|-------|
| `VITE_API_BASE_URL` | `https://sprintly-api.onrender.com` |

5. Update `vite.config.ts` for production (the proxy only works in dev):
   - Ensure axios base URL falls back to `VITE_API_BASE_URL` when not proxied.

6. Deploy. Your app is live!

---

## Local Development (Quick Start)

```bash
# 1. Start PostgreSQL (Docker)
docker-compose up -d

# 2. Start Backend (from /backend)
mvn spring-boot:run

# 3. Start Frontend (from /frontend)
npm run dev
```

Access at: **http://localhost:5173**

---

## Docker (Optional Self-Host)

Build and run the backend container:
```bash
cd backend
docker build -t sprintly-backend .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=... \
  -e SPRING_DATASOURCE_USERNAME=... \
  -e SPRING_DATASOURCE_PASSWORD=... \
  -e JWT_SECRET=... \
  sprintly-backend
```
