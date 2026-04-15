//package org.example;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
//class Driver {
//    private String id;
//
//    public BigDecimal getHourlyRate() {
//        return hourlyRate;
//    }
//
//    private final BigDecimal hourlyRate;
//    private final List<Slot>  deliverySlot;
//
//    public Driver(String driverId, BigDecimal hourlyRate) {
//        this.id  = driverId;
//        this.hourlyRate = hourlyRate;
//        this.deliverySlot = new ArrayList<>();
//    }
//
//    public void addSlot(int start, int end) {
//        deliverySlot.add(new Slot(start, end));
//    }
//
//    public int getSlotCount(int timeStamp) {
//        int slotCount = 0;
//        for (Slot slot: deliverySlot) {
//            if (timeStamp < slot.getEndTime()) {
//                slotCount += (slot.getEndTime() - Math.max(slot.getStartTime(), timeStamp));
//            }
//        }
//        return slotCount;
//    }
//
//    public int getSlotsInBetween(int timeStamp) {
//        int slotCount = 0;
//        for (Slot slot: deliverySlot) {
//            if (timeStamp < slot.getEndTime() && timeStamp >= slot.getStartTime()) {
//                return 1;
//            }
//        }
//        return slotCount;
//    }
//}
//
//class Slot {
//    private int startTime;
//    private int endTime;
//
//    public Slot(int startTime, int endTime) {
//        this.startTime = startTime;
//        this.endTime = endTime;
//    }
//
//    public int getStartTime() {
//        return startTime;
//    }
//
//    public int getEndTime() {
//        return endTime;
//    }
//}
//
//public class DeliveryCostTracking {
//
//    HashMap<String, Driver> driversDB = new HashMap<>();
//    BigDecimal totalCost = BigDecimal.ZERO;
//    private int paidDelivery = 0;
//
//    public void addDriver(String driverId, BigDecimal hourlyRate) {
//        if (driverId.isBlank()) return;
//        if (driversDB.containsKey(driverId)) return;
//        driversDB.put(driverId, new Driver(driverId, hourlyRate));
//    }
//
//    public void recordDelivery(String driverId, int startTime, int endTime) {
//        if (endTime <= startTime || startTime < 0) return;
//        Driver driver = driversDB.get(driverId);
//        if (driver == null) return;
//        driver.addSlot(startTime, endTime);
//        int duration = endTime - startTime;
//        BigDecimal hours = BigDecimal.valueOf(duration)
//                .divide(BigDecimal.valueOf(3600), 10, RoundingMode.HALF_UP);
//        totalCost = totalCost.add(driver.getHourlyRate().multiply(hours));
//    }
//
//    public BigDecimal getTotalCost() {
//        return totalCost.setScale(2, RoundingMode.HALF_UP);
//    }
//
//    public void payUpTo(int timestamp) {
//        if (paidDelivery > timestamp) return;
//        paidDelivery = timestamp;
//    }
//
//    public BigDecimal getUnpaidCost() {
//        BigDecimal total = BigDecimal.ZERO;
//
//        for (Driver driver : driversDB.values()) {
//            int seconds = driver.getSlotCount(paidDelivery);
//            BigDecimal hours = BigDecimal.valueOf(seconds)
//                    .divide(BigDecimal.valueOf(3600), 10, RoundingMode.HALF_UP);
//            total = total.add(driver.getHourlyRate().multiply(hours));
//        }
//
//        return total.setScale(2, RoundingMode.HALF_UP);
//    }
//
//    public int getMaxActiveDrivers(int timestamp) {
//        int driverCount = 0;
//        for (Driver driver: driversDB.values()) {
//            if (driver.getSlotsInBetween(timestamp) > 0)
//                driverCount++;
//        }
//        return driverCount;
//    }
//}
