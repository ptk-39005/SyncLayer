package org.example;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/benchmark")
@Tag(name = "Performance Benchmarking", description = "Run real-time stress tests on the SyncLayer")
public class BenchmarkController {

    private final InventoryManagement inventoryManager;

    public BenchmarkController(InventoryManagement inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    @Operation(summary = "Run Multi-Product Flash Sale", description = "Simulates a massive sale across 5 different products with 200,000 total requests.")
    @PostMapping("/run-multi-product-sale")
    public BenchmarkResult runMultiProductStressTest() throws InterruptedException {
        String[] products = {"IPHONE_15", "MACBOOK_PRO", "AIRPODS_MAX", "APPLE_WATCH", "KINDLE"};
        int stockPerProduct = 50;
        int totalRequests = 200000;

        for (String id : products) {
            inventoryManager.addProduct(id, stockPerProduct);
        }

        // 2. Multithreading Setup
        ExecutorService executor = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failure = new AtomicInteger(0);

        for (int i = 0; i < totalRequests; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    latch.await();
                    String targetProduct = products[index % products.length];

                    if (inventoryManager.tryReserve(targetProduct, 1, 30000)) {
                        success.incrementAndGet();
                    } else {
                        failure.incrementAndGet();
                    }
                } catch (Exception ignored) {}
            });
        }

        long startTime = System.currentTimeMillis();
        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.MINUTES);
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        double rps = (duration > 0) ? (totalRequests / (duration / 1000.0)) : totalRequests * 1000;

        Map<String, Object> finalStats = new HashMap<>();
        for (String id : products) {
            finalStats.put(id + "_stock", inventoryManager.getStockQuantity(id));
        }
        finalStats.put("total_initial_stock", stockPerProduct * products.length);

        return new BenchmarkResult(
                "Multi-Product Flash Sale (200k Requests)",
                duration,
                Math.round(rps * 100.0) / 100.0,
                success.get(),
                failure.get(),
                finalStats
        );
    }
}