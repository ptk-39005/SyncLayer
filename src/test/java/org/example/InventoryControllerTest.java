package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventoryManagement inventoryManager;

    @BeforeEach
    void setup() {
        inventoryManager.addProduct("TEST_IPHONE", 10);
    }

    @Test
    void testReserveAndPurchaseFlow() throws Exception {
        // 1. Test Successful Reservation
        String response = mockMvc.perform(post("/api/inventory/reserve")
                        .param("productId", "TEST_IPHONE")
                        .param("qty", "1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("Response: " + response);

        // 3. Verify Stock is now 9
        mockMvc.perform(get("/api/inventory/stock/TEST_IPHONE"))
                .andExpect(status().isOk())
                .andExpect(content().string("9"));
    }

    @Test
    void testInsufficientStock() throws Exception {
        // Try to reserve more than we have (10)
        mockMvc.perform(post("/api/inventory/reserve")
                        .param("productId", "TEST_IPHONE")
                        .param("qty", "50"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Insufficient Stock"));
    }
}