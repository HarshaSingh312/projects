package org.example.model;

public class Delivery {
    private int driverId;
    private int startTime;
    private int endTime;

    public Delivery(int driverId, int startTime, int endTime) {
        this.driverId = driverId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }
}
