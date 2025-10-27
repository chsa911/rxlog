# RxLog — Employer-Friendly Microservices (v5)

**Services:** Gateway (Java), Register (Java + Flyway), Barcode (Go + pgx + Redis), Mobile (Kotlin),
Autocomplete (Go + Redis), Analytics (Go + pgx + Redis), Research (Go worker).  
**Infra:** PostgreSQL 16, Redis 7. **Frontend:** React (Vite) + Nginx.

## Quick start
```bash
docker compose up --build -d
open http://localhost:3000  # or just navigate in your browser
curl http://localhost:8080/actuator/health
```

## Sample API
```bash
curl -X POST http://localhost:8080/api/register/book -H 'content-type: application/json' -d '{
  "title":"American Gods","author":"Neil Gaiman","publisher":"HarperCollins",
  "barcode":"978031603","sizeRuleId":1
}'
```
