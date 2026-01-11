package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
class FlashSaleStressTest {

    @Autowired
    private InventoryManagement inventoryManager;

    @Test
    void simulateFlashSale() throws InterruptedException {
        String productId = "FLASH_SALE_ITEM";
        int initialStock = 100;
        int totalCompetitors = 2000;

        inventoryManager.addProduct(productId, initialStock);

        ExecutorService executor = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < totalCompetitors; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    boolean result = inventoryManager.tryReserve(productId, 1, 30000);
                    if (result) successCount.incrementAndGet();
                    else failureCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        long start = System.currentTimeMillis();
        latch.countDown();

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        long end = System.currentTimeMillis();

        System.out.println("--- FLASH SALE RESULTS ---");
        System.out.println("Time Taken: " + (end - start) + "ms");
        System.out.println("Successful Reservations: " + successCount.get());
        System.out.println("Failed (Out of Stock): " + failureCount.get());
        System.out.println("Final Stock Level: " + inventoryManager.getStockQuantity(productId));

        assert successCount.get() == initialStock;
        assert inventoryManager.getStockQuantity(productId) == 0;
    }
}