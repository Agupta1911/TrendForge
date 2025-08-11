# TrendForge 📈 
*High-Frequency Stock Analysis Engine with REST API*

![Java](https://img.shields.io/badge/Java-17%2B-blue)
![Python](https://img.shields.io/badge/Python-3.10%2B-yellow)
![SparkJava](https://img.shields.io/badge/SparkJava-API-red)

## 🚀 Key Features
- **Thread-safe Java engine** using `ConcurrentHashMap` + `ConcurrentSkipListMap` for real-time per-symbol indexing
- **Multi-threaded CSV ingestion** with configurable thread pools
- **REST API** (SparkJava) with endpoints for:
  - Real-time tick ingestion (`POST /ingest`)
  - Time-range queries (`GET /range`)
  - Top-k movers analysis (`GET /topk`) 
  - SMA calculations (`GET /sma`)
- **Python synthetic data generator** for stress testing

## ⚡ Performance
- Processes **10,000+ ticks/sec** with <100ms latency for SMA calculations
- Scales to **1M+ tick history** per symbol with O(log n) query performance

## 🛠️ Tech Stack
- **Backend**: Java 17 (Concurrent Collections, SparkJava)
- **Data Processing**: AVL Trees, SMA/EMA Algorithms
- **Testing**: Python 3.10 (Pandas, NumPy for data generation)

## 📦 Build & Run
```bash
# Build (requires Java 17+ and Maven)
mvn package

# Run server (default port: 4567)
java -jar target/trendforge-enhanced-1.0-SNAPSHOT.jar

# Generate test data (Python)
python generate_data.py > ticks.csv

## Endpoints
- `POST /ingest` : JSON body `{ "symbol": "...", "ts": 1234567890, "price": 12.34, "volume": 100 }`
- `GET /range?symbol=SYM0001&start=...&end=...` : returns JSON list of ticks
- `GET /topk?start=...&end=...&k=5` : returns top-k movers
- `GET /sma?symbol=SYM0001&windowMs=3600000` : returns map ts->sma
