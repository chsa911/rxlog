# RxLog V5 

An App for my special mobile reading and storing workflow. Basically i remove covers and pages to store them in my trousers pockets to always be able to read in any place.
To identify the remaining pages and to facilitate storage i have developed a barcode system 
which i apply with textmarker and ballpoint pencil on the down, left or upper side of a book. 
In three areas on the left middle and right i apply up to five lines which generates
a unique code. The colour of the lines depends on the width of the book.
The position down left or up is determined by the height of the book. 

The app provides a frontend and services to automatically provide unique barcodes.
 
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
