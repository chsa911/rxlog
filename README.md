# RxLog V5 


I have a space‑saving, mobile reading workflow: I remove covers 
to fit more books on a shelf and sometimes carry a few loose pages in my
pocket so I can read anywhere. That breaks the usual cues (cover/spine), 
so I created a barcode-system and RxLog to still identify books, track progress, and shelve them
quickly.

How the barcode works

I draw a simple three‑lane mark (left / middle / right) on a book’s top, bottom, or left edge using a text marker or pen.

In each lane I draw 0–5 short lines; the three lane counts form a 
unique code (215 usable combinations per color & edge placement).

Color = book width (quick visual grouping).

Edge placement (top/bottom/left) = book height (faster shelving).

What the app does

Generate & reserve codes automatically, including suggested color and edge placement.

Register the book (author, publisher, pages) and store it with the code.

Research the first‑published year automatically.

Autocomplete abbreviations for authors/publishers to speed input.

Mobile: look up books on the go and update reading status (reading → finished/abandoned). When a book is finished/abandoned, the code is freed for reuse.

Analytics: see your most‑read authors.

 At a glance
 ervices

Gateway (Java – Spring Boot)

Register (Java + Flyway)

Barcode (Go + pgx + Redis)

Mobile (Kotlin)

Autocomplete (Go + Redis)

Analytics (Go + pgx + Redis)

Research (Go worker)

Infra: PostgreSQL 16, Redis 7Frontend: React (Vite) served by Nginx

Default ports (local): Frontend 3000, Gateway 8080, Postgres 5432, Redis 6379.



**Services:** Gateway (Java), Register (Java + Flyway), Barcode (Go + pgx + Redis), Mobile (Kotlin),
Autocomplete (Go + Redis), Analytics (Go + pgx + Redis), Research (Go worker).  
**Infra:** PostgreSQL 16, Redis 7. **Frontend:** React (Vite) + Nginx.




## Quick start
```bash

Prereqs: Docker and Docker Compose installed.
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
### D) Feature status (helps employers know what’s testable)
```markdown
### Project status
| Service       | Runs with compose | Health status          | API in README | Tests | Notes |
|---------------|-------------------|------------------------|---------------|-------|-------|
| Gateway       | ✅ core            | `UP`                   | ✅            | ✅    | Routes & health |
| Register      | ✅ core            | `UP`                   | ✅            | ✅    | Flyway migrations |
| Barcode       | ⏳ extras          | `NOT_IMPLEMENTED` (501)| 🔶 stub only  | ⏳    | Allocation rules WIP |
| Autocomplete  | ⏳ extras          | `NOT_IMPLEMENTED` (501)| 🔶 stub only  | ⏳    | Prefix search WIP |
| Analytics     | ⏳ extras          | `NOT_IMPLEMENTED` (501)| 🔶 stub only  | ⏳    | Top authors WIP |
| Research      | ⏳ extras          | `OUT_OF_SERVICE` (503) | ❌            | ⏳    | Worker planned |

Legend: ✅ implemented · 🔶 stubbed · ⏳ in progress · ❌ not started


## 3-minute demo
```bash
docker compose down -v
docker compose up --build -d
curl -f http://localhost:8080/actuator/health

# Register two books
curl -s -X POST http://localhost:8080/api/register/book -H 'content-type: application/json' -d '{
  "title":"American Gods","author":"Neil Gaiman","publisher":"HarperCollins",
  "barcode":"L2-M1-R0","sizeRuleId":1
}'
curl -s -X POST http://localhost:8080/api/register/book -H 'content-type: application/json' -d '{
  "title":"The Hobbit","author":"J. R. R. Tolkien","publisher":"Allen & Unwin",
  "barcode":"L0-M3-R1","sizeRuleId":1
}'

# (Optional) Autocomplete / status / analytics
# curl -s 'http://localhost:8080/api/autocomplete?field=author&q=tolk'
# curl -s -X PATCH http://localhost:8080/api/mobile/books/1/status -H 'content-type: application/json' -d '{"status":"finished","lastPage":310}'
# curl -s 'http://localhost:8080/api/analytics/top-authors?limit=5'

### F) Architecture diagram (Mermaid—renders on GitHub and in recent IntelliJ)
```markdown
## Architecture
```mermaid
flowchart LR
  FE[Frontend (React + Nginx)] -->|HTTP| GW[Gateway (Spring Boot)]
  GW --> REG[Register (Java + Flyway)]
  GW --> BAR[Barcode (Go + pgx + Redis)]
  GW --> AUT[Autocomplete (Go + Redis)]
  GW --> ANA[Analytics (Go + pgx + Redis)]
  GW --> MOB[Mobile (Kotlin)]
  RES[Research (Go worker)] --> REG

  REG <-->|SQL| PG[(PostgreSQL 16)]
  BAR <-->|cache| RD[(Redis 7)]
  AUT <-->|cache| RD
  ANA <-->|read| PG


> If Mermaid preview doesn’t render in your IntelliJ, update the IDE or install a Mermaid plugin; GitHub will still render it.

---

## 3) Organize images & extras (optional but nice)
- Create `docs/` and `docs/images/` for any screenshots.
- Embed with: `![Screenshot](docs/images/app-home.png)`
- Add a Postman/Insomnia collection in `docs/` and link it.

---

## 4) Add a mini Table of Contents (optional)
Place this near the top and update as needed:
```markdown
- [What this is](#what-this-is-plain-english)
- [How the barcode works](#how-the-barcode-works)
- [What the app does](#what-the-app-does)
- [Quick start](#quick-start)
- [Sample API](#sample-api)
- [Feature status](#feature-status)
- [3-minute demo](#3-minute-demo)
- [Architecture](#architecture)

