# JDBC vs JPA
Native SQL vs JPA implentation are two different patterns for extracting data.
Below is a flow chart to help remember when to use which.

Start
 │
 ├─ Is this CRUD on domain entities with relationships (1–*/*–*)?
 │        └─ Yes → Use JPA (entities, repos, JPQL, pagination, caching)
 │
 ├─ Is the SQL complex (CTEs, window funcs, PERCENTILE_CONT, JSONB ops,
 │   LATERAL joins, vendor-specific hints/RETURNING/ON CONFLICT)?
 │        └─ Yes → Use JDBC (NamedParameterJdbcTemplate, native SQL)
 │
 ├─ Do you need DB portability (avoid vendor-specific SQL)?
 │        └─ Yes → Prefer JPA/JPQL (fall back to native only when needed)
 │
 ├─ Is this bulk work (batch inserts/updates, upserts/merge)?
 │        └─ Often easier/safer with JDBC (explicit SQL)
 │
 ├─ Is this read-mostly analytics over large tables?
 │        └─ Create aggregate table/MV → Read with JPA entity
 │           (refresh/rollup job implemented with JDBC SQL)
 │
 ├─ Are simple read DTOs/projections enough (no entity graph needed)?
 │        └─ Either works:
 │              • Simple: JPA projection (@Query JPQL)
 │              • Many joins/math: JDBC
 │
 └─ Need lazy loading/2nd-level cache/entity lifecycle events?
          └─ JPA

## Rules of thumb

- CRUD & domain logic → JPA
- Analytics & heavy SQL → JDBC
- Aggregates layer (daily metrics/MV) → write/refresh with JDBC, expose as a JPA entity for clean reads
- When in doubt: start JDBC for the gnarly query; if it stabilizes into a table/view, promote it to a JPA entity.