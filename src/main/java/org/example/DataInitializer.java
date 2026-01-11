package org.example;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DataInitializer {

    private final InventoryManagement inventoryManager;

    public DataInitializer(InventoryManagement inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedInventory() {
        System.out.println("🚀 Application is Ready! Seeding Initial Data...");

        Map<String, Integer> initialStock = Map.of(
                "IPHONE_15", 100,
                "MACBOOK_PRO", 50,
                "SONY_WH1000XM5", 30,
                "KINDLE_PAPERWHITE", 200
        );

        initialStock.forEach(inventoryManager::addProduct);

        System.out.println(initialStock.size() + " products are now live in the engine.");
    }
}