package org.example;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class StockNode {
    private String productId;
    private AtomicInteger unitsPresent;
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public StockNode(String id, int units){
        this.productId = id;
        this.unitsPresent = new AtomicInteger(units);
    }

    public boolean reserveStock(Integer unitsToReserve){
        if(unitsPresent.get() <= 0){
            return false;
        }

        int remUnits = unitsPresent.addAndGet(-unitsToReserve);

        if(remUnits < 0){
            unitsPresent.addAndGet(unitsToReserve);
            return false;
        }

        return true;
    }

    public void releaseStock(Integer unitsToRelease){
        unitsPresent.addAndGet(unitsToRelease);
    }

    public int getCurrentStockUnits(){
        return unitsPresent.get();
    }

    public void forceReserve(int qty) {
        unitsPresent.addAndGet(-qty);
    }

}
