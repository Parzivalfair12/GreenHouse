# Deploy Guide — GreenHouse Manager

## Architecture

```
Frontend (Vercel)          Backend (Railway)         Database (Neon)
    │                           │                        │
    │  HTTPS                    │  HTTPS                 │  TLS
    │                           │                        │
    └───────── API ─────────────┘──────── PostgreSQL ────┘
```

## 1. Database — Neon PostgreSQL

1. Create account at https://neon.tech
2. Create a new project → database `greenhouse`
3. Copy connection string:
   ```
   postgresql://user:pass@ep-xxx.us-east-2.aws.neon.tech/greenhouse
   ```
4. Convert to JDBC format for `DB_URL`:
   ```
   jdbc:postgresql://ep-xxx.us-east-2.aws.neon.tech/greenhouse?sslmode=require
   ```

### Neon Checklist
- [ ] Project created
- [ ] Connection string copied
- [ ] SSL mode enabled
- [ ] IP allowed (Neon allows all by default)

## 2. Backend — Railway

1. Create account at https://railway.app
2. New project → Deploy from GitHub repo
3. Set root directory: `backend/`
4. Build command: `./mvnw package -DskipTests -B`
5. Start command: `java -jar target/*.jar --spring.profiles.active=prod`

### Environment Variables (Railway)

| Variable | Value |
|----------|-------|
| `DB_URL` | `jdbc:postgresql://...neon.tech/greenhouse?sslmode=require` |
| `DB_USERNAME` | (from Neon) |
| `DB_PASSWORD` | (from Neon) |
| `JWT_SECRET` | Generate a 64-char random string |
| `FRONTEND_URL` | `https://greenhouse.vercel.app` |
| `GOOGLE_CLIENT_ID` | From Google Cloud Console |
| `GOOGLE_CLIENT_SECRET` | From Google Cloud Console |
| `PORT` | `8080` (Railway sets this) |
| `BASE_URL` | `https://greenhouse-api.up.railway.app` |

### Railway Checklist
- [ ] GitHub repo connected
- [ ] Root directory set to `backend/`
- [ ] Build/start commands configured
- [ ] All env vars set
- [ ] PostgreSQL health check passes
- [ ] `GET /api/health` returns 200
- [ ] Swagger UI at `/swagger-ui.html`

## 3. Frontend — Vercel

1. Create account at https://vercel.com
2. Import GitHub repo
3. Root directory: `frontend/`
4. Build command: `npm run build`
5. Output directory: `dist/`

### Environment Variables (Vercel)

| Variable | Value |
|----------|-------|
| `VITE_API_URL` | `https://greenhouse-api.up.railway.app` |

### Vercel Checklist
- [ ] GitHub repo connected
- [ ] Root directory set to `frontend/`
- [ ] Build command configured
- [ ] `VITE_API_URL` set
- [ ] SPA routing works (Vercel handles this natively)

## 4. Google OAuth Configuration

1. Go to https://console.cloud.google.com/apis/credentials
2. Select or create a project
3. Create OAuth 2.0 Client ID (Web application)

### Authorized JavaScript origins
```
https://greenhouse.vercel.app
http://localhost:5173
```

### Authorized redirect URIs
```
https://greenhouse-api.up.railway.app/login/oauth2/code/google
http://localhost:8080/login/oauth2/code/google
```

### OAuth Checklist
- [ ] Client ID and Secret created
- [ ] Origins configured
- [ ] Redirect URIs configured
- [ ] Env vars set on Railway: `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`

## 5. Full Deployment Sequence

```bash
# Step 1: Push to GitHub
git add .
git commit -m "Production ready"
git push origin main

# Step 2: Verify CI/CD
# Wait for GitHub Actions to complete (all green)

# Step 3: Check Railway deploy
# https://greenhouse-api.up.railway.app/api/health
# Expected: {"status":"UP","service":"greenhouse-api",...}

# Step 4: Check Flyway migrations
# Railway logs should show: "Successfully applied 2 migrations"

# Step 5: Check Swagger
# https://greenhouse-api.up.railway.app/swagger-ui.html

# Step 6: Check Vercel deploy
# https://greenhouse.vercel.app

# Step 7: Test full flow
# Login → Dashboard → CRUD → i18n → Dark mode
```

## 6. Post-Deploy Validation

| Check | Endpoint / Action | Expected |
|-------|------------------|----------|
| Health | `GET /api/health` | `{"status":"UP"}` |
| Swagger | `GET /swagger-ui.html` | Swagger UI loads |
| Login | POST `/api/auth/login` | JWT token returned |
| Google OAuth | Click "Continuar con Google" | OAuth flow completes |
| Greenhouses | GET `/api/greenhouses` | Array of greenhouses |
| Flyway | App startup logs | "Successfully applied" |

## 7. Troubleshooting

| Problem | Solution |
|---------|----------|
| Backend won't start (DB error) | Check `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` |
| Flyway migration fails | Run `baseline-on-migrate: true` or reset Neon DB |
| OAuth redirect loop | Check redirect URIs in Google Console |
| CORS errors in browser | Check `FRONTEND_URL` matches Vercel URL |
| 401 on all requests | Check `JWT_SECRET` is the same across restarts |
| Frontend shows fallback data | Check `VITE_API_URL` and Network tab |
| Login returns 500 | Check `GOOGLE_CLIENT_ID` and `SECRET` |
| Swagger not loading | Check Railway logs for startup errors |

## 8. Local Docker Test

```bash
docker compose up --build

# Backend: http://localhost:8080
# Frontend: http://localhost:5173
# Swagger:  http://localhost:8080/swagger-ui.html
# Health:   http://localhost:8080/api/health
```
