# RxLog — Architecture

```mermaid
flowchart LR
  Client[React Frontend] -->|/api/*| GW[Spring Cloud Gateway]
  GW --> REG[Register (Java)]
  GW --> BAR[Barcode (Go)]
  GW --> MOB[Mobile (Kotlin)]
  GW --> AUT[Autocomplete (Go)]
  GW --> ANA[Analytics (Go)]
  REG --> PG[(PostgreSQL)]
  BAR --> PG
  MOB --> PG
  ANA --> PG
  AUT <--> R[(Redis)]
  BAR <--> R
```
