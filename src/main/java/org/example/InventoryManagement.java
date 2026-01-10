package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.PriorityQueue;
import java.util.concurrent.*;

public class InventoryManagement {
    private final WALService wal = new WALService();

    private ConcurrentHashMap<String, StockNode> inventory = new ConcurrentHashMap<>();
    private PriorityQueue<Reservation> expiryQueue = new PriorityQueue<>();
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public InventoryManagement(){
        executorService.scheduleAtFixedRate(this::cleanUp, 1,1, TimeUnit.SECONDS);
    }

    public WALService getWalService(){
        return wal;
    }

    public void addProduct(String id, int units){
        inventory.put(id, new StockNode(id, units));
    }

    public boolean tryReserve(String productId, int units, int ttlInSeconds){
        StockNode node = inventory.get(productId);

        if (node != null) {
            if(node.reserveStock(units)){
                long expiryTime = System.currentTimeMillis() + ttlInSeconds*100L;
                wal.log("RESERVE", productId, units);
                synchronized (expiryQueue){
                    expiryQueue.add(new Reservation(productId, expiryTime, units));
                }
                return true;
            }
        }
        return false;
    }

    public int getStockQuantity(String productId){
        if(inventory.containsKey(productId)){
            return inventory.get(productId).getCurrentStockUnits();
        }
        return 0;
    }

    public void cleanUp(){
        long now = System.currentTimeMillis();
        synchronized (expiryQueue) {
            while (!expiryQueue.isEmpty() && expiryQueue.peek().timeStamp <= now) {
                Reservation expired = expiryQueue.poll();
                StockNode node = inventory.get(expired.productId);
                wal.log("RELEASE", expired.productId, expired.quantity);
                node.releaseStock(expired.quantity);
            }
        }
    }

    public void recover() {
        Path walPath = Path.of("swiftstock_wal.log");

        if (Files.notExists(walPath)) {
            System.out.println("No WAL file found. Starting with a fresh state.");
            return;
        }

        System.out.println("--- System Recovery Started ---");
        long now = System.currentTimeMillis();
        int recordsProcessed = 0;

        try (BufferedReader reader = Files.newBufferedReader(walPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length < 4) continue;

                String op = parts[1].trim();
                String pId = parts[2];
                int qty = Integer.parseInt(parts[3]);

                StockNode node = inventory.get(pId);
                if (node == null) continue;

                if (op.equalsIgnoreCase("RESERVE")) {
                    node.forceReserve(qty);
                }
                else if (op.equalsIgnoreCase("RELEASE")) {
                    node.releaseStock(qty);
                }
                recordsProcessed++;
            }
            System.out.println("--- Recovery Complete. Replayed " + recordsProcessed + " transactions ---");
        } catch (IOException e) {
            System.err.println("Recovery failed: " + e.getMessage());
        }
    }

    public void release(String productId, int qty) {
        StockNode node = inventory.get(productId);
        if (node != null) {
            node.releaseStock(qty);
            wal.log("RELEASE", productId, qty);
        }
    }
}
