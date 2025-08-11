# TrendForge

This enhanced TrendForge prototype includes:
- Thread-safe Java engine using ConcurrentHashMap + ConcurrentSkipListMap for per-symbol indexing.
- Multi-threaded CSV ingestion using a thread pool.
- Small REST API using SparkJava exposing ingestion and query endpoints (JSON).
- Python data generator for synthetic ticks.
- Maven `pom.xml` to build the Java server.

## Build & Run (Java)
Requirements: Java 17+, Maven

1. Build:
   ```bash
   mvn package
   ```
2. Run server (after building):
   ```bash
   java -jar target/trendforge-enhanced-1.0-SNAPSHOT.jar
   ```
   The server listens on port 4567 by default.

## Endpoints
- `POST /ingest` : JSON body `{ "symbol": "...", "ts": 1234567890, "price": 12.34, "volume": 100 }`
- `GET /range?symbol=SYM0001&start=...&end=...` : returns JSON list of ticks
- `GET /topk?start=...&end=...&k=5` : returns top-k movers
- `GET /sma?symbol=SYM0001&windowMs=3600000` : returns map ts->sma

## Python
- `generate_data.py` : generate synthetic CSV `ticks.csv`
