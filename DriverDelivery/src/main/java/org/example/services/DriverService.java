//package org.example.services;
//
//import org.example.model.Driver;
//
//import java.util.HashMap;
//
//public class DriverService {
//
//    HashMap<Integer, Driver> driverDb;
//
//    public void addDriver(int driverId, double hourlyCost) {
//        driverDb.put(driverId, new Driver(driverId, hourlyCost));
//    }
//
//    public boolean isDriverAdded(int driverId) {
//        return driverDb.containsKey(driverId);
//    }
//
//    public double getHourlyCost(int driverId) {
//        return driverDb.get(driverId).getHourlyCost();
//    }
//
//    public void setTotalCost(int driverId, double driverCost) {
//        driverDb.get(driverId).setTotalCost(driverCost);
//    }
//
//    public double getPendingAmount(int driverId) {
//        return driverDb.get(driverId).getPendingAmount();
//    }
//
//    public void payAmount(int driverId, double amount) {
//        driverDb.get(driverId).addPaidAmount(amount);
//    }
//
//    public double getTotalCount(int driverId) {
//        driverDb.get(driverId).getTotalCost();
//    }
//}
