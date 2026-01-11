package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        String logName = "swiftstock_wal.log";
        Path path = Path.of(logName);

        // Product Keys
        String IPHONE = "IPHONE_15";
        String MACBOOK = "MACBOOK_PRO";

        //CLEANUP
        Files.deleteIfExists(path);
        System.out.println("Step 1: Clean slate for multi-product test.");

        InventoryManagement managerA = new InventoryManagement();
        managerA.addProduct(IPHONE, 100);
        managerA.addProduct(MACBOOK, 100);

        System.out.println("Step 2: Executing mixed transactions...");
        managerA.tryReserve(IPHONE, 1, 300); // iPhone: 99
        managerA.tryReserve(IPHONE, 1, 300); // iPhone: 98

        managerA.tryReserve(MACBOOK, 1, 300); // Mac: 99
        managerA.tryReserve(MACBOOK, 1, 300); // Mac: 98
        managerA.tryReserve(MACBOOK, 1, 300); // Mac: 97

        managerA.release(IPHONE, 1);       // iPhone: 99 (Release 1)

        System.out.println("Manager A Final Stock -> iPhone: " + managerA.getStockQuantity(IPHONE) + ", Mac: " + managerA.getStockQuantity(MACBOOK));

        managerA.getWalService().shutDown();

        System.out.println("\n--- SIMULATING RESTART ---");
        InventoryManagement managerB = new InventoryManagement();

        managerB.addProduct(IPHONE, 100);
        managerB.addProduct(MACBOOK, 100);

        System.out.println("Manager B Initial Stock (Pre-Recovery) -> iPhone: " + managerB.getStockQuantity(IPHONE));

        managerB.recover();

        int finalIphone = managerB.getStockQuantity(IPHONE);
        int finalMac = managerB.getStockQuantity(MACBOOK);

        System.out.println("iPhone Stock: " + finalIphone + " (Expected: 99)");
        System.out.println("MacBook Stock: " + finalMac + " (Expected: 97)");

        boolean success = (finalIphone == 99 && finalMac == 97);

        if (success) {
            System.out.println("PRODUCTION READY: Multi-product state reconstruction successful!");
        } else {
            System.out.println("FAILURE: State mismatch after recovery.");
        }
        //System.exit(0);
    }
}