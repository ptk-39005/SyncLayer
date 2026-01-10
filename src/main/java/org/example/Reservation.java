package org.example;

public class Reservation implements Comparable<Reservation>{
    String productId;
    long timeStamp;
    int quantity;

    public Reservation(String productId, long timeStamp, int quantity){
        this.productId = productId;
        this.timeStamp = timeStamp;
        this.quantity = quantity;
    }

    @Override
    public int compareTo(Reservation other){
        return Long.compare(this.timeStamp, other.timeStamp);
    }
}
