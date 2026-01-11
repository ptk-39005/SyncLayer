# SyncLayer: High-Performance Inventory
**SyncLayer** is an high-throughput inventory management designed for "Thundering Herd" scenarios. While DB-backed systems struggle with row-level locking during flash sales, SyncLayer uses a non-blocking in-memory architecture.


### 1. Atomic (High-Speed State)
Utilizes Java's Memory Model with ConcurrentHashMap and AtomicInteger primitives

### 2. Write-Ahead Logging
Every state change is recorded in an asynchronous **Write-Ahead Log** before completion. 
* **Self-Healing:** If the system crashes, the engine replays the WAL on startup to reconstruct the inventory state perfectly.

### 3. Janitor Service (Automatic Cleanup)
To prevent "inventory hoarding," a background service monitors the lifecycle of every reservation.
* **Automatic Expiry:** Reservations not converted to purchases within the TTL window are automatically released back to the stock pool.
* **Non-Blocking:** Cleanup runs on a separate thread to ensure zero impact on purchase performance.

## Technology Stack
* **Language:** Java 21
* **Framework:** Spring Boot 3.2.0
* **API Documentation:** Swagger UI
* **Persistence:** Custom WAL (File-based)
* **Concurrency:** ThreadPoolExecutor

## Benchmark Results ##
**Intial stock of all items - 50**
<img width="1495" height="849" alt="Screenshot 2026-01-11 at 16 27 44" src="https://github.com/user-attachments/assets/c9870c20-1885-4b7e-83fa-7e17182126b4" />

{
  "testName": "Multi-Product Flash Sale (200k Requests)",
  "timeTakenMs": 112,
  "requestsPerSecond": 1785714.29,
  "successCount": 250,
  "outOfStockRejections": 199750,
  "systemMetrics": {
    "AIRPODS_MAX_stock": 0,
    "IPHONE_15_stock": 0,
    "total_initial_stock": 250,
    "APPLE_WATCH_stock": 0,
    "MACBOOK_PRO_stock": 0,
    "KINDLE_stock": 0
  }
}
