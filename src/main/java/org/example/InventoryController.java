package org.example;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryManagement inventoryManager;

    public InventoryController(InventoryManagement inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    @GetMapping("/stock/{id}")
    public ResponseEntity<Integer> getStock(@PathVariable String id) {
        return ResponseEntity.ok(inventoryManager.getStockQuantity(id));
    }

    @PostMapping("/reserve")
    public ResponseEntity<String> reserve(@RequestParam String productId, @RequestParam int qty) {
        boolean resId = inventoryManager.tryReserve(productId, qty, 300000); // 5 min TTL
        if (resId) {
            return ResponseEntity.ok("Reserved! ID: " + resId);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient Stock");
    }
}