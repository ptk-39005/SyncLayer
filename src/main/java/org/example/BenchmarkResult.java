package org.example;

import java.util.Map;

public record BenchmarkResult(
        String testName,
        long timeTakenMs,
        double requestsPerSecond,
        int successCount,
        int outOfStockRejections,
        Map<String, Object> systemMetrics
) {}